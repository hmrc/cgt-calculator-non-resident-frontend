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

package common

import common.nonresident.TaxableGainCalculation
import connectors.CalculatorConnector
import constructors.AnswersConstructor
import controllers.helpers.FakeRequestHelper
import models._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import services.SessionCacheService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TaxableGainCalculationSpec extends CommonPlaySpec with GuiceOneAppPerSuite with MockitoSugar with FakeRequestHelper {
  implicit val hc: HeaderCarrier = HeaderCarrier()
  val mockCalcConnector: CalculatorConnector = mock[CalculatorConnector]
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockAnswersConstructor: AnswersConstructor = mock[AnswersConstructor]

  val calculationResultsWithPRRModel: CalculationResultsWithPRRModel = CalculationResultsWithPRRModel(GainsAfterPRRModel(1000, 1500, 2000), None, None)
  val prrModel: PrivateResidenceReliefModel = PrivateResidenceReliefModel("No", None)
  val taxYearModel: TaxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")
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
  val totalGainAnswersModel = TotalGainAnswersModel(
    DateModel(5, 6, 2015),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(true)),
    DisposalValueModel(950000),
    DisposalCostsModel(15000),
    Some(HowBecameOwnerModel("Bought")),
    Some(BoughtForLessModel(false)),
    AcquisitionValueModel(1250000),
    AcquisitionCostsModel(20000),
    DateModel(10, 10, 2001),
    Some(RebasedValueModel(950000)),
    Some(RebasedCostsModel("No", None)),
    IsClaimingImprovementsModel(false),
    None,
    Some(OtherReliefsModel(0))
  )
  val calculationResultsModel = CalculationResultsWithTaxOwedModel(
    TotalTaxOwedModel(100, 100, 20, None, None, 200, 100, None, None, None, None, 0, None, None, None, None, None, None, None),
    None,
    None
  )
  val propertyLivedIn = PropertyLivedInModel(true)

  when(mockSessionCacheService.fetchAndGetFormData[PrivateResidenceReliefModel](ArgumentMatchers.eq(KeystoreKeys.NonResidentKeys.privateResidenceRelief))
    (ArgumentMatchers.any(), ArgumentMatchers.any()))
    .thenReturn(Future.successful(Some(prrModel)))

  when(mockCalcConnector.calculateTaxableGainAfterPRR(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
    .thenReturn(Future.successful(Some(calculationResultsWithPRRModel)))

  when(mockAnswersConstructor.getPersonalDetailsAndPreviousCapitalGainsAnswers(ArgumentMatchers.any()))
    .thenReturn(Future.successful(Some(personalDetailsModel)))

  when(mockCalcConnector.getFullAEA(ArgumentMatchers.any())(ArgumentMatchers.any()))
    .thenReturn(Future.successful(Some(BigDecimal(11000))))

  when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(ArgumentMatchers.any()))
    .thenReturn(Future.successful(Some(TaxYearModel("2015/16", isValidYear = true, "2015/16"))))

  when(mockCalcConnector.calculateNRCGTTotalTax(
    ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(),
    ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
    .thenReturn(Future.successful(Some(calculationResultsModel)))

  "Calling .getPrrResponse" should {

    "when supplied with a property lived in model with true" should {

      val result = TaxableGainCalculation.getPrrResponse(Some(propertyLivedIn), mockSessionCacheService)

      "return a PRR model" in {
        await(result) shouldBe Some(prrModel)
      }
    }

    "when supplied with a property lived in model with false" should {

      val propertyLivedIn = PropertyLivedInModel(false)
      val result = TaxableGainCalculation.getPrrResponse(Some(propertyLivedIn), mockSessionCacheService)

      "return a None" in {
        await(result) shouldBe None
      }
    }
  }

  "Calling .getPrrIfApplicable" should {

    "when supplied with no PRR model and no property lived in model" should {
      val result = TaxableGainCalculation.getPrrIfApplicable(totalGainAnswersModel, None, None, mockCalcConnector)

      "return a None" in {
        await(result) shouldBe None
      }
    }

    "when supplied with a PRR model and property lived in model" should {
      val result = TaxableGainCalculation.getPrrIfApplicable(totalGainAnswersModel, Some(prrModel), Some(propertyLivedIn), mockCalcConnector)

      "return a CalculationResultsWithPRRModel" in {
        await(result) shouldBe Some(calculationResultsWithPRRModel)
      }
    }
  }

  "Calling the .getFinalSectionAnswers" when {

    "supplied with a calculationResultsWithPrrModel" when {

      "along with at least one taxable gain greater than zero" should {
        val totalGainModel = TotalGainResultsModel(BigDecimal(200), None, None)
        val calculationResultsWithPRRModel = CalculationResultsWithPRRModel(GainsAfterPRRModel(1000, taxableGain = 1000, 0), None, None)

        val result = TaxableGainCalculation.getFinalSectionsAnswers(totalGainModel, Some(calculationResultsWithPRRModel),
          mockAnswersConstructor)

        "return a TotalPersonalDetailsCalculationModel" in {
          await(result) shouldBe Some(personalDetailsModel)
        }
      }

      "along with a TotalGainModel that has no positive taxable gains" should {
        val totalGainModel = TotalGainResultsModel(BigDecimal(-200), Some(BigDecimal(-200)), Some(BigDecimal(-200)))
        val calculationResultsWithPRRModel = CalculationResultsWithPRRModel(GainsAfterPRRModel(1000, taxableGain = -1000, 0), None, None)

        val result = TaxableGainCalculation.getFinalSectionsAnswers(totalGainModel, Some(calculationResultsWithPRRModel),
          mockAnswersConstructor)

        "return a value of None" in {
          await(result) shouldBe None
        }
      }
    }

    "not supplied with a calculationResultsWithPrrModel" when {
      "with at least one positive gain" should {
        val totalGainModel = TotalGainResultsModel(BigDecimal(200), None, None)
        val result = TaxableGainCalculation.getFinalSectionsAnswers(totalGainModel, None, mockAnswersConstructor)

        "return a TotalPersonalDetailsCalculationModel" in {
          await(result) shouldBe Some(personalDetailsModel)
        }
      }

      "with no positive gains" should {
        val totalGainModel = TotalGainResultsModel(BigDecimal(-200), None, None)
        val result = TaxableGainCalculation.getFinalSectionsAnswers(totalGainModel, None, mockAnswersConstructor)

        "return a value of None" in {
          await(result) shouldBe None
        }
      }
    }
  }

  "Calling .getMaxAEA" should {

    val result = TaxableGainCalculation.getMaxAEA(Some(taxYearModel), mockCalcConnector)

    "return the full AEA of 11000" in {
      await(result) shouldBe Some(BigDecimal(11000))
    }
  }

  "Calling .getTaxYear" should {

    val result = TaxableGainCalculation.getTaxYear(totalGainAnswersModel, mockCalcConnector)

    "return a tax year model" in {
      await(result) shouldBe Some(taxYearModel)
    }
  }

  "Calling .getChargeableGain" when {

    "supplied with a totalPersonalDetailsCalculationModel" should {

      val result = TaxableGainCalculation.getChargeableGain(totalGainAnswersModel, Some(prrModel), Some(propertyLivedIn),
        Some(personalDetailsModel), 11000, mockCalcConnector)

      "return a CalculationResultsWithTaxOwedModel" in {
        await(result) shouldBe Some(calculationResultsModel)
      }
    }

    "supplied with no totalPersonalDetailsCalculationModel" should {

      val result = TaxableGainCalculation.getChargeableGain(totalGainAnswersModel, Some(prrModel), Some(propertyLivedIn), None, 11000, mockCalcConnector)

      "return a None" in {
        await(result) shouldBe None
      }
    }
  }

  "Calling .getPropertyLivedInResponse" when {

    "when no gain exists" should {
      val result = TaxableGainCalculation.getPropertyLivedInResponse(false, mockSessionCacheService)

      "return None" in {
        await(result) shouldBe None
      }
    }
  }
}
