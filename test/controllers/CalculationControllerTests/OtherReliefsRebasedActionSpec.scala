/*
 * Copyright 2020 HM Revenue & Customs
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
import common.TestModels
import config.ApplicationConfig
import connectors.CalculatorConnector
import constructors.AnswersConstructor
import controllers.OtherReliefsRebasedController
import controllers.helpers.FakeRequestHelper
import models.{TaxYearModel, _}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{MessagesControllerComponents, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.logging.SessionId
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class OtherReliefsRebasedActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper with BeforeAndAfterEach {

  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))

  val materializer = mock[Materializer]
  val mockHttp =mock[DefaultHttpClient]
  val mockCalcConnector =mock[CalculatorConnector]
  val mockAnswerConstuctor = mock[AnswersConstructor]
  val defaultCache = mock[CacheMap]
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]


  class Setup {
    val controller = new OtherReliefsRebasedController(
      mockHttp,
      mockCalcConnector,
      mockAnswerConstuctor,
      mockMessagesControllerComponents
    )(mockConfig, fakeApplication)
  }

  def setupTarget(getData: Option[OtherReliefsModel],
                  gainAnswers: TotalGainAnswersModel,
                  calculationResultsModel: CalculationResultsWithTaxOwedModel,
                  personalDetailsModel: TotalPersonalDetailsCalculationModel,
                  totalGainResultModel: TotalGainResultsModel = TotalGainResultsModel(200, Some(500), None),
                  calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel] = None
                 ): OngoingStubbing[Future[CacheMap]] = {
    when(mockCalcConnector.fetchAndGetFormData[OtherReliefsModel](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](
      ArgumentMatchers.eq(KeystoreKeys.privateResidenceRelief))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(PrivateResidenceReliefModel("No", None))))

    when(mockAnswerConstuctor.getNRTotalGainAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(gainAnswers))

    when(mockCalcConnector.calculateTotalGain(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainResultModel)))

    when(mockAnswerConstuctor.getPersonalDetailsAndPreviousCapitalGainsAnswers(ArgumentMatchers.any()))
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
  }

  def document(result : Future[Result]) = Jsoup.parse(bodyOf(result)(materializer))

  "Calling the .otherReliefsRebased action " when {

    "not supplied with a pre-existing stored model and a chargeable gain of £500 and total gain of £500" should {

      val gainAnswers = TestModels.totalGainAnswersModelWithRebasedTA
      def setupMocks() = setupTarget(
        None,
        gainAnswers,
        TestModels.calculationResultsModelWithRebased,
        TestModels.personalDetailsCalculationModel
      )

      "return a 200 with a valid calculation result" in new Setup {
        setupMocks()
        lazy val result = controller.otherReliefsRebased(fakeRequestWithSession)
        status(result) shouldBe 200
      }

      "load the otherReliefs rebased page" in new Setup {
        setupMocks()
        lazy val result = controller.otherReliefsRebased(fakeRequestWithSession)
        document(result).title() shouldBe messages.question
      }

      s"have a total gain message with text '${messages.totalGain}' £500" in new Setup {
        setupMocks()
        lazy val result = controller.otherReliefsRebased(fakeRequestWithSession)
        document(result).getElementById("totalGain").text() shouldBe s"${messages.totalGain} £500"
      }

      s"have a taxable gain message with text '${messages.taxableGain}' £500" in new Setup {
        setupMocks()
        lazy val result = controller.otherReliefsRebased(fakeRequestWithSession)
        document(result).getElementById("taxableGain").text() shouldBe s"${messages.taxableGain} £500"
      }
    }

    "supplied with a pre-existing stored model" should {
      val testOtherReliefsModel = OtherReliefsModel(5000)
      def setupStubs() = setupTarget(
        Some(testOtherReliefsModel),
        TestModels.totalGainAnswersModelWithRebasedTA,
        TestModels.calculationResultsModelWithRebased,
        TestModels.personalDetailsCalculationModel
      )

      "return a status of 200" in new Setup() {
        setupStubs()
        lazy val result = controller.otherReliefsRebased(fakeRequestWithSession)
        status(result) shouldBe 200
      }

      "load the otherReliefs rebased page" in new Setup() {
        setupStubs()
        lazy val result = controller.otherReliefsRebased(fakeRequestWithSession)
        val doc = document(result)
        doc.title() shouldBe messages.question
      }
    }

    "supplied with an invalid session" should {
      def setupStubs = setupTarget(
        None,
        TestModels.totalGainAnswersModelWithRebasedTA,
        TestModels.calculationResultsModelWithRebased,
        TestModels.personalDetailsCalculationModel
      )

      "return a status of 303" in new Setup() {
        setupStubs
        val result = controller.otherReliefsRebased(fakeRequest)
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in new Setup() {
        setupStubs
        val result = controller.otherReliefsRebased(fakeRequest)
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "In CalculationController calling the .submitOtherReliefsRebased action" when {

    "submitting a valid form" should  {
      def setupStubs = setupTarget(
        None,
        TestModels.totalGainAnswersModelWithRebasedTA,
        TestModels.calculationResultsModelWithRebased,
        TestModels.personalDetailsCalculationModel
      )


      "return a status of 303" in new Setup {
        setupStubs
        lazy val request = fakeRequestToPOSTWithSession(("isClaimingOtherReliefs", "Yes"), ("otherReliefs", "1000"))
        lazy val result = controller.submitOtherReliefsRebased(request)
        status(result) shouldBe 303
      }

      "redirect to the calculation election page" in new Setup {
        setupStubs
        lazy val request = fakeRequestToPOSTWithSession(("isClaimingOtherReliefs", "Yes"), ("otherReliefs", "1000"))
        lazy val result = controller.submitOtherReliefsRebased(request)
        redirectLocation(result) shouldBe Some(controllers.routes.CalculationElectionController.calculationElection().url)
      }
    }

    "submitting an invalid form" should {
      def setupStubs = setupTarget(
        None,
        TestModels.totalGainAnswersModelWithRebasedTA,
        TestModels.calculationResultsModelWithRebased,
        TestModels.personalDetailsCalculationModel
      )

      "return a status of 400" in new Setup {
        setupStubs
        lazy val request = fakeRequestToPOSTWithSession(("isClaimingOtherReliefs", "Yes"), ("otherReliefs", "-1000"))
        lazy val result = controller.submitOtherReliefsRebased(request)
        status(result) shouldBe 400
      }

      "return to the other reliefs rebased page" in new Setup{
        setupStubs
        lazy val request = fakeRequestToPOSTWithSession(("isClaimingOtherReliefs", "Yes"), ("otherReliefs", "-1000"))
        lazy val result = controller.submitOtherReliefsRebased(request)
        document(result).title() shouldBe messages.question
      }
    }
  }
}
