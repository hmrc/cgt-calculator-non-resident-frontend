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

import assets.MessageLookup.NonResident.{OtherReliefs => messages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.{CommonPlaySpec, TestModels, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import constructors.AnswersConstructor
import controllers.OtherReliefsController
import controllers.helpers.FakeRequestHelper
import models._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.otherReliefs

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class OtherReliefsActionSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockHttp: DefaultHttpClient =mock[DefaultHttpClient]
  val mockCalcConnector: CalculatorConnector =mock[CalculatorConnector]
  val mockAnswersConstructor: AnswersConstructor = mock[AnswersConstructor]
  val mockMessagesControllerComponents: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val otherReliefsView: otherReliefs = fakeApplication.injector.instanceOf[otherReliefs]
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]

  class Setup {
    val controller = new OtherReliefsController(
      mockCalcConnector,
      mockSessionCacheService,
      mockAnswersConstructor,
      mockMessagesControllerComponents,
      otherReliefsView
    )
  }
  def setupTarget(getData: Option[OtherReliefsModel],
                  calculationResultsModel: CalculationResultsWithTaxOwedModel,
                  personalDetailsModel: TotalPersonalDetailsCalculationModel,
                  totalGainResultModel: TotalGainResultsModel = TotalGainResultsModel(200, None, None),
                  calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel] = None
                 ): OtherReliefsController = {

    when(mockSessionCacheService.fetchAndGetFormData[OtherReliefsModel](
      ArgumentMatchers.eq(KeystoreKeys.otherReliefsFlat))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheService.fetchAndGetFormData[PrivateResidenceReliefModel](
      ArgumentMatchers.eq(KeystoreKeys.privateResidenceRelief))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(PrivateResidenceReliefModel("No", None))))

    when(mockAnswersConstructor.getNRTotalGainAnswers(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(TestModels.businessScenarioFiveModel))

    when(mockCalcConnector.calculateTotalGain(ArgumentMatchers.any())(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainResultModel)))

    when(mockAnswersConstructor.getPersonalDetailsAndPreviousCapitalGainsAnswers(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(personalDetailsModel)))

    when(mockCalcConnector.calculateTaxableGainAfterPRR(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(calculationResultsWithPRRModel))

    when(mockCalcConnector.getFullAEA(ArgumentMatchers.any())(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(BigDecimal(11000))))

    when(mockCalcConnector.calculateNRCGTTotalTax(
      ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(),
      ArgumentMatchers.any())(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(calculationResultsModel)))

    when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(TaxYearModel("2015/16", isValidYear = true, "2015/16"))))

    when(mockSessionCacheService.fetchAndGetFormData[PropertyLivedInModel](ArgumentMatchers.eq(KeystoreKeys.propertyLivedIn))
      (using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(PropertyLivedInModel(true))))

    when(mockSessionCacheService.saveFormData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(("", "")))

    new OtherReliefsController(mockCalcConnector, mockSessionCacheService, mockAnswersConstructor, mockMessagesControllerComponents, otherReliefsView)
  }

  val personalDetailsModel: TotalPersonalDetailsCalculationModel = TotalPersonalDetailsCalculationModel(
    CurrentIncomeModel(20000),
    Some(PersonalAllowanceModel(0)),
    OtherPropertiesModel("Yes"),
    Some(PreviousLossOrGainModel("Neither")),
    None,
    None,
    Some(AnnualExemptAmountModel(0)),
    BroughtForwardLossesModel(isClaiming = false, None)
  )

  val calculationResultsModel: CalculationResultsWithTaxOwedModel = CalculationResultsWithTaxOwedModel(
    TotalTaxOwedModel(100, 100, 20, None, None, 200, 100, None, None, None, None, 0, None , None , None , None , None , None , None),
    None,
    None
  )

  "Calling the .otherReliefs action " when {

    "not supplied with a pre-existing stored model and a chargeable gain of £100 and total gain of £200" should {
      val target = setupTarget(None,
        calculationResultsModel,
        personalDetailsModel)
      lazy val result = target.otherReliefs(fakeRequestWithSession)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the other reliefs page" in {
        document.title() shouldBe messages.title
      }

      s"show body text with text '${messages.additionalHelp(200, 100)}'" in {
        document.body().select("#main-content > div > div > p").text() shouldBe messages.additionalHelp(200, 100)
      }
    }

    "supplied with a pre-existing stored model" should {
      val testOtherReliefsModel = OtherReliefsModel(5000)
      val target = setupTarget(Some(testOtherReliefsModel),
        calculationResultsModel,
        personalDetailsModel)
      lazy val result = target.otherReliefs(fakeRequestWithSession)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the other reliefs page" in {
        document.title() shouldBe messages.title
      }
    }

    "supplied without a valid session" should {
      val target = setupTarget(None,
        calculationResultsModel,
        personalDetailsModel)
      lazy val result = target.otherReliefs(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling the .submitOtherReliefs action" when {

    "submitting a valid form" should {
      val target = setupTarget(None,
        calculationResultsModel,
        personalDetailsModel)
      lazy val request = fakeRequestToPOSTWithSession("otherReliefs" -> "1000").withMethod("POST")
      lazy val result = target.submitOtherReliefs(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the summary page" in {
        redirectLocation(result).get shouldBe controllers.routes.SummaryController.summary.url
      }
    }

    "submitting an invalid form" should {
      val target = setupTarget(None,
        calculationResultsModel,
        personalDetailsModel)
      lazy val request = fakeRequestToPOSTWithSession("otherReliefs" -> "")
      lazy val result = target.submitOtherReliefs(request)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "return to the other reliefs page" in {
        document.title() shouldBe s"Error: ${messages.title}"
      }
    }
  }
}
