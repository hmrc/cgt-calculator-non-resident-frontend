/*
 * Copyright 2018 HM Revenue & Customs
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

import assets.MessageLookup.NonResident.{Summary => messages}
import common.nonresident.CalculationType
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import connectors.CalculatorConnector
import constructors.AnswersConstructor
import controllers.SummaryController
import controllers.helpers.FakeRequestHelper
import common.TestModels
import models.{TaxYearModel, _}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

class SummaryActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(summary: TotalGainAnswersModel,
                  result: TotalGainResultsModel,
                  calculationElectionModel: CalculationElectionModel,
                  privateResidenceReliefModel: Option[PrivateResidenceReliefModel],
                  calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel],
                  finalSummaryModel: TotalPersonalDetailsCalculationModel,
                  taxOwedResult: Option[CalculationResultsWithTaxOwedModel]
                 ): SummaryController = {

    lazy val mockCalcConnector = mock[CalculatorConnector]
    lazy val mockAnswersConstructor = mock[AnswersConstructor]

    when(mockAnswersConstructor.getNRTotalGainAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(summary))

    when(mockAnswersConstructor.getPersonalDetailsAndPreviousCapitalGainsAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(finalSummaryModel)))

    when(mockCalcConnector.fetchAndGetFormData[CalculationElectionModel](
      ArgumentMatchers.eq(KeystoreKeys.calculationElection))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(calculationElectionModel)))

    when(mockCalcConnector.calculateTotalGain(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(result)))

    when(mockCalcConnector.calculateTotalCosts(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(BigDecimal(100)))

    when(mockCalcConnector.calculateTaxableGainAfterPRR(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(calculationResultsWithPRRModel))

    when(mockCalcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](
      ArgumentMatchers.eq(KeystoreKeys.privateResidenceRelief))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(privateResidenceReliefModel)

    when(mockCalcConnector.getFullAEA(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(BigDecimal(11000))))

    when(mockCalcConnector.calculateNRCGTTotalTax(
      ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(),
      ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(taxOwedResult))

    when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(TaxYearModel("2015/16", isValidYear = true, "2015/16"))))

    when(mockCalcConnector.fetchAndGetFormData[OtherReliefsModel](
      ArgumentMatchers.eq(KeystoreKeys.otherReliefsFlat))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(None))

    when(mockCalcConnector.fetchAndGetFormData[OtherReliefsModel](
      ArgumentMatchers.eq(KeystoreKeys.otherReliefsRebased))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(None))

    when(mockCalcConnector.fetchAndGetFormData[OtherReliefsModel](
      ArgumentMatchers.eq(KeystoreKeys.otherReliefsTA))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(None))

    when(mockCalcConnector.fetchAndGetFormData[PropertyLivedInModel](ArgumentMatchers.eq(KeystoreKeys.propertyLivedIn))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(PropertyLivedInModel(true))))

    new SummaryController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val answersConstructor: AnswersConstructor = mockAnswersConstructor
    }
  }

  lazy val answerModel = TotalGainAnswersModel(
    DateModel(10, 10, 2016),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(10000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    DateModel(1, 1, 2016),
    None,
    None,
    ImprovementsModel("No", None, None),
    None
  )

  lazy val finalAnswersModel = TotalPersonalDetailsCalculationModel(
    CurrentIncomeModel(0),
    None,
    OtherPropertiesModel("No"),
    None,
    None,
    None,
    None,
    BroughtForwardLossesModel(isClaiming = false, None)
  )

  "Calling the .summary action" when {

    "provided with a valid session and three potential calculations" should {
      lazy val target = setupTarget(
        answerModel,
        TotalGainResultsModel(1000, Some(2000), Some(3000)),
        CalculationElectionModel(CalculationType.flat),
        Some(PrivateResidenceReliefModel("Yes", Some(1000), Some(10))),
        Some(CalculationResultsWithPRRModel(GainsAfterPRRModel(100, 0, 100), None, None)),
        finalAnswersModel,
        Some(TestModels.calculationResultsModelWithAll)
      )

      lazy val result = target.summary()(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the summary page" in {
        document.title() shouldBe messages.title
      }

      "has a back-link to the calculation election page" in {
        document.select("#back-link").attr("href") shouldEqual controllers.routes.CalculationElectionController.calculationElection().url
      }
    }

    "provided with a valid session and only one (flat) calculation" should {
      lazy val target = setupTarget(
        answerModel,
        TotalGainResultsModel(1000, None, None),
        CalculationElectionModel(CalculationType.flat),
        None,
        None,
        finalAnswersModel,
        Some(TestModels.calculationResultsModelWithRebased)
      )
      lazy val result = target.summary()(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the summary page" in {
        document.title() shouldBe messages.title
      }

      "has a back-link to the check your answers page" in {
        document.select("#back-link").attr("href") shouldEqual controllers.routes.CheckYourAnswersController.checkYourAnswers().url
      }
    }

    "provided with an invalid session" should {
      lazy val target = setupTarget(
        answerModel,
        TotalGainResultsModel(1000, Some(2000), Some(3000)),
        CalculationElectionModel(CalculationType.flat),
        Some(PrivateResidenceReliefModel("Yes", Some(1000), Some(10))),
        Some(CalculationResultsWithPRRModel(GainsAfterPRRModel(100, 0, 100), None, None)),
        finalAnswersModel,
        Some(TestModels.calculationResultsModelWithRebased)
      )
      lazy val result = target.summary()(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "calling the .restart action" should {
    lazy val target = setupTarget(
      answerModel,
      TotalGainResultsModel(1000, Some(2000), Some(3000)),
      CalculationElectionModel(CalculationType.flat),
      Some(PrivateResidenceReliefModel("Yes", Some(1000), Some(10))),
      Some(CalculationResultsWithPRRModel(GainsAfterPRRModel(100, 0, 100), None, None)),
      finalAnswersModel,
      Some(TestModels.calculationResultsModelWithRebased)
    )
    lazy val result = target.restart()(fakeRequestWithSession)

    "return a 303" in {
      status(result) shouldBe 303
    }

    "redirect to the start page" in {
      redirectLocation(result).get shouldBe controllers.routes.DisposalDateController.disposalDate().url
    }
  }
}
