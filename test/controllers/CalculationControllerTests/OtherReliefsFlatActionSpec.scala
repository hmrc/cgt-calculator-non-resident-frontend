/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.CalculationControllerTests

import akka.stream.Materializer
import assets.MessageLookup.NonResident.{OtherReliefs => messages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.{CommonPlaySpec, TestModels, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import constructors.AnswersConstructor
import controllers.OtherReliefsFlatController
import controllers.helpers.FakeRequestHelper
import models.{TaxYearModel, _}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.logging.SessionId
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.concurrent.Future

class OtherReliefsFlatActionSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val materializer = mock[Materializer]
  val mockHttp =mock[DefaultHttpClient]
  val mockCalcConnector =mock[CalculatorConnector]
  val defaultCache = mock[CacheMap]
  val mockAnswersConstructor = mock[AnswersConstructor]
  val mockMessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]


  class Setup {
    val controller = new OtherReliefsFlatController(
      mockHttp,
      mockCalcConnector,
      mockAnswersConstructor,
      mockMessagesControllerComponents
    )(mockConfig, fakeApplication)
  }

  def setupTarget(
                   getData: Option[OtherReliefsModel],
                   gainAnswers: TotalGainAnswersModel,
                   calculationResultsModel: CalculationResultsWithTaxOwedModel,
                   personalDetailsModel: TotalPersonalDetailsCalculationModel,
                   totalGainResultModel: TotalGainResultsModel = TotalGainResultsModel(200, Some(100), None),
                   calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel] = None
                 ): OtherReliefsFlatController = {

    when(mockCalcConnector.fetchAndGetFormData[OtherReliefsModel](
      ArgumentMatchers.eq(KeystoreKeys.otherReliefsFlat))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](
      ArgumentMatchers.eq(KeystoreKeys.privateResidenceRelief))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(PrivateResidenceReliefModel("No", None))))

    when(mockAnswersConstructor.getNRTotalGainAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(gainAnswers))

    when(mockCalcConnector.calculateTotalGain(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainResultModel)))

    when(mockAnswersConstructor.getPersonalDetailsAndPreviousCapitalGainsAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(personalDetailsModel)))

    when(mockCalcConnector.calculateTaxableGainAfterPRR(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(calculationResultsWithPRRModel)

    when(mockCalcConnector.getFullAEA(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(BigDecimal(11000))))

    when(mockCalcConnector.calculateNRCGTTotalTax(
      ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(),
      ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(TestModels.calculationResultsModelWithRebased)))

    when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(TaxYearModel("2015/16", isValidYear = true, "2015/16"))))

    when(mockCalcConnector.fetchAndGetFormData[PropertyLivedInModel](ArgumentMatchers.eq(KeystoreKeys.propertyLivedIn))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(PropertyLivedInModel(true))))

    when(mockCalcConnector.saveFormData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map.empty)))

    new OtherReliefsFlatController(mockHttp, mockCalcConnector, mockAnswersConstructor, mockMessagesControllerComponents)(mockConfig, fakeApplication)
  }

  "Calling the .otherReliefsFlat action " when {

    "not supplied with a pre-existing stored model and a chargeable gain of £100 and total gain of £200" should {

      val target = setupTarget(
        None,
        TestModels.totalGainAnswersModelWithRebasedTA,
        TestModels.calculationResultsModelWithRebased,
        TestModels.personalDetailsCalculationModel)
      lazy val result = target.otherReliefsFlat(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer))

      "return a 200 with a valid calculation result" in {
        status(result) shouldBe 200
      }

      "load the otherReliefs flat page" in {
        document.title() shouldBe messages.question
      }

      s"have a total gain message with text '${messages.totalGain}' £200" in {
        document.getElementById("totalGain").text() shouldBe s"${messages.totalGain} £200"
      }

      s"have a taxable gain message with text '${messages.taxableGain}' £100" in {
        document.getElementById("taxableGain").text() shouldBe s"${messages.taxableGain} £100"
      }
    }

    "supplied with a pre-existing stored model" should {
      val testOtherReliefsModel = OtherReliefsModel(5000)
      val target = setupTarget(
        Some(testOtherReliefsModel),
        TestModels.totalGainAnswersModelWithRebasedTA,
        TestModels.calculationResultsModelWithRebased,
        TestModels.personalDetailsCalculationModel)
      lazy val result = target.otherReliefsFlat(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the otherReliefs flat page" in {
        document.title() shouldBe messages.question
      }
    }

    "supplied with an invalid session" should {
      val target = setupTarget(
        None,
        TestModels.totalGainAnswersModelWithRebasedTA,
        TestModels.calculationResultsModelWithRebased,
        TestModels.personalDetailsCalculationModel)
      lazy val result = target.otherReliefsFlat(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling the .submitOtherReliefsFlat action" when {

    "submitting a valid form" should {
      val target = setupTarget(
        None,
        TestModels.totalGainAnswersModelWithRebasedTA,
        TestModels.calculationResultsModelWithRebased,
        TestModels.personalDetailsCalculationModel)
      lazy val request = fakeRequestToPOSTWithSession(("isClaimingOtherReliefs", "Yes"), ("otherReliefs", "1000"))
      lazy val result = target.submitOtherReliefsFlat(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the calculation election page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.CalculationElectionController.calculationElection().url)
      }
    }

    "submitting an invalid form" should {
      val target = setupTarget(
        None,
        TestModels.totalGainAnswersModelWithRebasedTA,
        TestModels.calculationResultsModelWithRebased,
        TestModels.personalDetailsCalculationModel)
      lazy val request = fakeRequestToPOSTWithSession(("isClaimingOtherReliefs", "Yes"), ("otherReliefs", "-1000"))
      lazy val result = target.submitOtherReliefsFlat(request)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "return to the other reliefs flat page" in {
        document.title() shouldBe messages.question
      }
    }
  }
}
