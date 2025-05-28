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

import assets.MessageLookup.NonResident.{CheckYourAnswers => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import constructors.{AnswersConstructor, DefaultCalculationElectionConstructor}
import controllers.CheckYourAnswersController
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
import views.html.calculation.checkYourAnswers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckYourAnswersActionSpec
  extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockHttp: DefaultHttpClient =mock[DefaultHttpClient]
  val mockCalcConnector: CalculatorConnector =mock[CalculatorConnector]
  val mockAnswersConstructor: AnswersConstructor = mock[AnswersConstructor]
  val mockDefaultCalElecConstructor: DefaultCalculationElectionConstructor = mock[DefaultCalculationElectionConstructor]
  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val checkYourAnswersView: checkYourAnswers = fakeApplication.injector.instanceOf[checkYourAnswers]
  val pageTitle: String = s"""${messages.question} - ${commonMessages.serviceName} - GOV.UK"""
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]

  class Setup {
    val controller = new CheckYourAnswersController(
      mockHttp,
      mockCalcConnector,
      mockSessionCacheService,
      mockAnswersConstructor,
      mockMessagesControllerComponents,
      checkYourAnswersView
    )
  }

  def setupTarget(totalGainAnswersModel: TotalGainAnswersModel,
                  totalGainsModel: Option[TotalGainResultsModel],
                  privateResidenceReliefModel: Option[PrivateResidenceReliefModel] = None,
                  totalPersonalDetailsModel: Option[TotalPersonalDetailsCalculationModel] = None,
                  calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel] = None): CheckYourAnswersController = {

    when(mockAnswersConstructor.getNRTotalGainAnswers(using ArgumentMatchers.any())).thenReturn(Future.successful(totalGainAnswersModel))

    when(mockAnswersConstructor.getPersonalDetailsAndPreviousCapitalGainsAnswers(
      using ArgumentMatchers.any())).thenReturn(Future.successful(totalPersonalDetailsModel))

    when(mockCalcConnector.calculateTaxableGainAfterPRR(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(calculationResultsWithPRRModel))

    when(mockCalcConnector.calculateTotalGain(ArgumentMatchers.any())(using ArgumentMatchers.any())).thenReturn(Future.successful(Some(totalGainResultsModel)))

    when(mockSessionCacheService.fetchAndGetFormData[PrivateResidenceReliefModel](ArgumentMatchers.any())(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(privateResidenceReliefModel))

    when(mockCalcConnector.calculateTotalGain(ArgumentMatchers.any())(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGainsModel))

    new CheckYourAnswersController(mockHttp, mockCalcConnector, mockSessionCacheService, mockAnswersConstructor, mockMessagesControllerComponents, checkYourAnswersView)
  }

  val modelWithMultipleGains: TotalGainAnswersModel = TotalGainAnswersModel(DateModel(5, 10, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(false)),
    DisposalValueModel(1000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Gifted")),
    Some(BoughtForLessModel(false)),
    AcquisitionValueModel(2000),
    Some(AcquisitionCostsModel(200)),
    DateModel(4, 10, 2013),
    Some(RebasedValueModel(3000)),
    Some(RebasedCostsModel("Yes", Some(300))),
    IsClaimingImprovementsModel(true),
    Some(ImprovementsModel(10, Some(20))),
    Some(OtherReliefsModel(30)))

  val totalGainResultsModel: TotalGainResultsModel = TotalGainResultsModel(0, Some(0), Some(0))
  val totalGainWithValueResultsModel: TotalGainResultsModel = TotalGainResultsModel(100, Some(-100), None)

  val modelWithOnlyFlat: TotalGainAnswersModel = TotalGainAnswersModel(DateModel(5, 10, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(false)),
    DisposalValueModel(1000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Gifted")),
    Some(BoughtForLessModel(false)),
    AcquisitionValueModel(2000),
    Some(AcquisitionCostsModel(200)),
    DateModel(4, 10, 2016),
    None,
    None,
    IsClaimingImprovementsModel(true),
    Some(ImprovementsModel(10, Some(20))),
    Some(OtherReliefsModel(30)))

  val personalDetailsModel: TotalPersonalDetailsCalculationModel = TotalPersonalDetailsCalculationModel(CurrentIncomeModel(9000),
    Some(PersonalAllowanceModel(1000)),
    OtherPropertiesModel("No"),
    Some(PreviousLossOrGainModel("gain")),
    None,
    Some(HowMuchGainModel(9000)),
    None,
    BroughtForwardLossesModel(isClaiming = false, None))


  "Calling .checkYourAnswers" when {

    "provided with a valid session" should {

      lazy val target = setupTarget(modelWithOnlyFlat, Some(totalGainResultsModel))
      lazy val result = target.checkYourAnswers(fakeRequestWithSession)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the check your answers page" in {
        document.title() shouldBe pageTitle
      }

      "have a back link to the improvements page" in {
        document.select(".govuk-back-link").attr("href") shouldBe "#"
      }
    }

    "provided with an invalid session" should {

      lazy val target = setupTarget(modelWithOnlyFlat, Some(totalGainResultsModel))
      lazy val result = target.checkYourAnswers(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect the user to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }

    "provided with a valid session when eligible for prr and a total gain value" should {
      lazy val target = setupTarget(modelWithMultipleGains, Some(totalGainWithValueResultsModel))
      lazy val result = target.checkYourAnswers(fakeRequestWithSession)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the check your answers page" in {
        document.title() shouldBe pageTitle
      }

      "have a back link to the private residence relief page" in {
        document.select(".govuk-back-link").attr("href") shouldBe "#"
      }
    }

    "provided with a valid session when the final answers have been answered and are applicable" should {

      lazy val target = setupTarget(modelWithOnlyFlat, Some(totalGainWithValueResultsModel), None, Some(personalDetailsModel))
      lazy val result = target.checkYourAnswers(fakeRequestWithSession)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the check your answers page" in {
        document.title() shouldBe pageTitle
      }

      "have a back link to the brought forward losses page" in {
        document.select(".govuk-back-link").attr("href") shouldBe "#"
      }
    }
  }

  "Calling .submitCheckYourAnswers" when {


    "provided with a valid model with multiple calculations available" should {
      lazy val results = Some(TotalGainResultsModel(0.0, Some(2.0), None))
      lazy val target = setupTarget(modelWithMultipleGains, results)
      lazy val result = target.submitCheckYourAnswers(fakeRequestWithSession)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect the user to the claiming other tax reliefs page" in {
        redirectLocation(result).get shouldBe controllers.routes.ClaimingReliefsController.claimingReliefs.url
      }
    }

    "provided with a valid model with only one calculation available" should {
      lazy val results = Some(TotalGainResultsModel(1.0, None, None))
      lazy val target = setupTarget(modelWithOnlyFlat, results)
      lazy val result = target.submitCheckYourAnswers(fakeRequestWithSession)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect the user to the other reliefs page" in {
        redirectLocation(result).get shouldBe controllers.routes.OtherReliefsController.otherReliefs.url
      }
    }

    "provided with a valid model with only losses" should {
      lazy val results = Some(TotalGainResultsModel(-1000, Some(0), None))
      lazy val target = setupTarget(modelWithOnlyFlat, results)
      lazy val result = target.submitCheckYourAnswers(fakeRequestWithSession)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect the user to the calculation election page" in {
        redirectLocation(result).get shouldBe controllers.routes.CalculationElectionController.calculationElection.url
      }
    }

    "provided with an invalid session" should {

      lazy val target = setupTarget(modelWithOnlyFlat, Some(totalGainResultsModel))
      lazy val result = target.submitCheckYourAnswers(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect the user to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling .redirectRoute" should {

    "route to other reliefs when a taxable gain greater than 0 is found" in {
      val prrCalcModel = CalculationResultsWithPRRModel(GainsAfterPRRModel(100, 10, 90), None, None)
      val totalGainModel = TotalGainResultsModel(100, None, None)
      val target = setupTarget(modelWithMultipleGains, None)
      lazy val result = target.redirectRoute(Some(prrCalcModel), totalGainModel)

      redirectLocation(result) shouldBe Some(controllers.routes.OtherReliefsController.otherReliefs.url)
    }

    "route to the summary when a taxable gain of 0 or less is found" in {
      val prrCalcModel = CalculationResultsWithPRRModel(GainsAfterPRRModel(100, 0, 100), None, None)
      val totalGainModel = TotalGainResultsModel(100, None, None)
      val target = setupTarget(modelWithMultipleGains, None)
      lazy val result = target.redirectRoute(Some(prrCalcModel), totalGainModel)

      redirectLocation(result) shouldBe Some(controllers.routes.SummaryController.summary.url)
    }

    "route to other reliefs when no taxable gain is found with a total gain greater than 0" in {
      val totalGainModel = TotalGainResultsModel(100, None, None)
      val target = setupTarget(modelWithMultipleGains, None)
      lazy val result = target.redirectRoute(None, totalGainModel)

      redirectLocation(result) shouldBe Some(controllers.routes.OtherReliefsController.otherReliefs.url)
    }

    "route to the summary when no taxable gain is found with a total gain of 0 or less" in {
      val totalGainModel = TotalGainResultsModel(0, None, None)
      val target = setupTarget(modelWithMultipleGains, None)
      lazy val result = target.redirectRoute(None, totalGainModel)

      redirectLocation(result) shouldBe Some(controllers.routes.SummaryController.summary.url)
    }
  }
}
