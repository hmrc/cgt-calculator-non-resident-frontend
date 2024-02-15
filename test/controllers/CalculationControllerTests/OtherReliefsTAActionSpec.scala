/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.OtherReliefsTAController
import controllers.helpers.FakeRequestHelper
import models._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.otherReliefsTA

import scala.concurrent.{ExecutionContext, Future}

class OtherReliefsTAActionSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {
  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))

  val materializer = mock[Materializer]
  val ec = fakeApplication.injector.instanceOf[ExecutionContext]
  val mockHttp =mock[DefaultHttpClient]
  val mockCalcConnector =mock[CalculatorConnector]
  val mockAnswersConstructor = mock[AnswersConstructor]
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val otherReliefsTAView = fakeApplication.injector.instanceOf[otherReliefsTA]
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]

  class Setup {
    val controller = new OtherReliefsTAController(
      mockCalcConnector,
      mockSessionCacheService,
      mockAnswersConstructor,
      mockMessagesControllerComponents,
      otherReliefsTAView
    )(ec)
  }
  def setupTarget(
                   getData: Option[OtherReliefsModel],
                   gainAnswers: TotalGainAnswersModel,
                   calculationResultsModel: CalculationResultsWithTaxOwedModel,
                   personalDetailsModel: TotalPersonalDetailsCalculationModel,
                   totalGainResultModel: TotalGainResultsModel = TotalGainResultsModel(200, Some(100), Some(200)),
                   calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel] = None
                 ): OtherReliefsTAController = {

    when(mockSessionCacheService.fetchAndGetFormData[OtherReliefsModel](
      ArgumentMatchers.eq(KeystoreKeys.otherReliefsTA))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheService.fetchAndGetFormData[PrivateResidenceReliefModel](
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
      ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(),
      ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(TestModels.calculationResultsModelWithTA)))

    when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(TaxYearModel("2015/16", isValidYear = true, "2015/16"))))

    when(mockSessionCacheService.fetchAndGetFormData[PropertyLivedInModel](ArgumentMatchers.eq(KeystoreKeys.propertyLivedIn))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(PropertyLivedInModel(true))))

    when(mockSessionCacheService.saveFormData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(("", "")))

    new OtherReliefsTAController(mockCalcConnector, mockSessionCacheService, mockAnswersConstructor, mockMessagesControllerComponents, otherReliefsTAView)(ec)
  }

  "Calling the .otherReliefsTA action " when {

    "not supplied with a pre-existing stored model and a chargeable gain of £100 and total gain of £200" should {

      val target = setupTarget(
        None,
        TestModels.totalGainAnswersModelWithRebasedTA,
        TestModels.calculationResultsModelWithTA,
        TestModels.personalDetailsCalculationModel
      )
      lazy val result = target.otherReliefsTA(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a 200 with a valid calculation result" in {
        status(result) shouldBe 200
      }

      "load the otherReliefs TA page" in {
        document.title() shouldBe messages.title
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
        TestModels.calculationResultsModelWithTA,
        TestModels.personalDetailsCalculationModel
      )
      lazy val result = target.otherReliefsTA(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the otherReliefs TA page" in {
        document.title() shouldBe messages.title
      }
    }

    "supplied with an invalid session" should {
      val target = setupTarget(
        None,
        TestModels.totalGainAnswersModelWithRebasedTA,
        TestModels.calculationResultsModelWithTA,
        TestModels.personalDetailsCalculationModel
      )
      lazy val result = target.otherReliefsTA(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling the .submitOtherReliefsTA action" when {

    "submitting a valid form" should {
      val target = setupTarget(
        None,
        TestModels.totalGainAnswersModelWithRebasedTA,
        TestModels.calculationResultsModelWithTA,
        TestModels.personalDetailsCalculationModel
      )
      lazy val request = fakeRequestToPOSTWithSession(("isClaimingOtherReliefs", "Yes"), ("otherReliefs", "1000")).withMethod("POST")
      lazy val result = target.submitOtherReliefsTA(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the calculation election page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.CalculationElectionController.calculationElection.url)
      }
    }

    "submitting an invalid form" should {
      val target = setupTarget(
        None,
        TestModels.totalGainAnswersModelWithRebasedTA,
        TestModels.calculationResultsModelWithTA,
        TestModels.personalDetailsCalculationModel
      )
      lazy val request = fakeRequestToPOSTWithSession(("isClaimingOtherReliefs", "Yes"), ("otherReliefs", "-1000"))
      lazy val result = target.submitOtherReliefsTA(request)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "return to the other reliefs TA page" in {
        document.title() shouldBe s"Error: ${messages.title}"
      }
    }
  }
}
