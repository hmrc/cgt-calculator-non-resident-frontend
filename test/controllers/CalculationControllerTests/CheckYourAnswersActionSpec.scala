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
import assets.MessageLookup.{NonResident => messages}
import config.ApplicationConfig
import connectors.CalculatorConnector
import constructors.{AnswersConstructor, DefaultCalculationElectionConstructor}
import controllers.{CalculationElectionController, CheckYourAnswersController, routes}
import controllers.helpers.FakeRequestHelper
import javax.inject.Inject
import models._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.logging.SessionId
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

class CheckYourAnswersActionSpec()
  extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))

  val materializer = mock[Materializer]
  val mockHttp =mock[DefaultHttpClient]
  val mockCalcConnector =mock[CalculatorConnector]
  val defaultCache = mock[CacheMap]
  val mockAnswersConstructor = mock[AnswersConstructor]
  val mockDefaultCalElecConstructor = mock[DefaultCalculationElectionConstructor]
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]


  class Setup {
    val controller = new CheckYourAnswersController(
      mockHttp,
      mockCalcConnector,
      mockAnswersConstructor,
      mockDefaultCalElecConstructor,
      mockMessagesControllerComponents
    )(mockConfig, fakeApplication)
  }

  def setupTarget(totalGainAnswersModel: TotalGainAnswersModel,
                  totalGainsModel: Option[TotalGainResultsModel],
                  privateResidenceReliefModel: Option[PrivateResidenceReliefModel] = None,
                  totalPersonalDetailsModel: Option[TotalPersonalDetailsCalculationModel] = None,
                  calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel] = None): CheckYourAnswersController = {

    when(mockAnswersConstructor.getNRTotalGainAnswers(ArgumentMatchers.any())).thenReturn(Future.successful(totalGainAnswersModel))

    when(mockAnswersConstructor.getPersonalDetailsAndPreviousCapitalGainsAnswers(
      ArgumentMatchers.any())).thenReturn(Future.successful(totalPersonalDetailsModel))

    when(mockCalcConnector.calculateTaxableGainAfterPRR(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(calculationResultsWithPRRModel))

    when(mockCalcConnector.calculateTotalGain(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(Some(totalGainResultsModel)))

    when(mockCalcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(privateResidenceReliefModel)

    when(mockCalcConnector.calculateTotalGain(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(totalGainsModel)

    new CheckYourAnswersController(mockHttp, mockCalcConnector, mockAnswersConstructor, mockDefaultCalElecConstructor, mockMessagesControllerComponents)(mockConfig, fakeApplication) {
      val answersConstructor: AnswersConstructor = mockAnswersConstructor
      val calculatorConnector: CalculatorConnector = mockCalcConnector
    }
  }

  val modelWithMultipleGains = TotalGainAnswersModel(DateModel(5, 10, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(false)),
    DisposalValueModel(1000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Gifted")),
    Some(BoughtForLessModel(false)),
    AcquisitionValueModel(2000),
    AcquisitionCostsModel(200),
    DateModel(4, 10, 2013),
    Some(RebasedValueModel(3000)),
    Some(RebasedCostsModel("Yes", Some(300))),
    ImprovementsModel("Yes", Some(10), Some(20)),
    Some(OtherReliefsModel(30)))

  val totalGainResultsModel = TotalGainResultsModel(0, Some(0), Some(0))
  val totalGainWithValueResultsModel = TotalGainResultsModel(100, Some(-100), None)

  val modelWithOnlyFlat = TotalGainAnswersModel(DateModel(5, 10, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(false)),
    DisposalValueModel(1000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Gifted")),
    Some(BoughtForLessModel(false)),
    AcquisitionValueModel(2000),
    AcquisitionCostsModel(200),
    DateModel(4, 10, 2016),
    None,
    None,
    ImprovementsModel("Yes", Some(10), Some(20)),
    Some(OtherReliefsModel(30)))

  val personalDetailsModel = TotalPersonalDetailsCalculationModel(CurrentIncomeModel(9000),
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
      lazy val document = Jsoup.parse(bodyOf(result)(materializer))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the check your answers page" in {
        document.title() shouldBe messages.CheckYourAnswers.question
      }

      "have a back link to the improvements page" in {
        document.select("#back-link").attr("href") shouldBe routes.ImprovementsController.improvements().url
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
      lazy val document = Jsoup.parse(bodyOf(result)(materializer))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the check your answers page" in {
        document.title() shouldBe messages.CheckYourAnswers.question
      }

      "have a back link to the private residence relief page" in {
        document.select("#back-link").attr("href") shouldBe routes.PrivateResidenceReliefController.privateResidenceRelief().url
      }
    }

    "provided with a valid session when the final answers have been answered and are applicable" should {

      lazy val target = setupTarget(modelWithOnlyFlat, Some(totalGainWithValueResultsModel), None, Some(personalDetailsModel))
      lazy val result = target.checkYourAnswers(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the check your answers page" in {
        document.title() shouldBe messages.CheckYourAnswers.question
      }

      "have a back link to the brought forward losses page" in {
        document.select("#back-link").attr("href") shouldBe routes.BroughtForwardLossesController.broughtForwardLosses().url
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
        redirectLocation(result).get shouldBe controllers.routes.ClaimingReliefsController.claimingReliefs().url
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
        redirectLocation(result).get shouldBe controllers.routes.OtherReliefsController.otherReliefs().url
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
        redirectLocation(result).get shouldBe controllers.routes.CalculationElectionController.calculationElection().url
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

      redirectLocation(result) shouldBe Some(controllers.routes.OtherReliefsController.otherReliefs().url)
    }

    "route to the summary when a taxable gain of 0 or less is found" in {
      val prrCalcModel = CalculationResultsWithPRRModel(GainsAfterPRRModel(100, 0, 100), None, None)
      val totalGainModel = TotalGainResultsModel(100, None, None)
      val target = setupTarget(modelWithMultipleGains, None)
      lazy val result = target.redirectRoute(Some(prrCalcModel), totalGainModel)

      redirectLocation(result) shouldBe Some(controllers.routes.SummaryController.summary().url)
    }

    "route to other reliefs when no taxable gain is found with a total gain greater than 0" in {
      val totalGainModel = TotalGainResultsModel(100, None, None)
      val target = setupTarget(modelWithMultipleGains, None)
      lazy val result = target.redirectRoute(None, totalGainModel)

      redirectLocation(result) shouldBe Some(controllers.routes.OtherReliefsController.otherReliefs().url)
    }

    "route to the summary when no taxable gain is found with a total gain of 0 or less" in {
      val totalGainModel = TotalGainResultsModel(0, None, None)
      val target = setupTarget(modelWithMultipleGains, None)
      lazy val result = target.redirectRoute(None, totalGainModel)

      redirectLocation(result) shouldBe Some(controllers.routes.SummaryController.summary().url)
    }
  }
}
