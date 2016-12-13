/*
 * Copyright 2016 HM Revenue & Customs
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
import constructors.nonresident.AnswersConstructor
import controllers.helpers.FakeRequestHelper
import models.nonresident._
import models.resident.TaxYearModel
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class TaxableGainCalculationSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  val mockCalcConnector = mock[CalculatorConnector]
  val mockAnswersConstructor = mock[AnswersConstructor]

  val calculationResultsWithPRRModel = CalculationResultsWithPRRModel(GainsAfterPRRModel(1000, 1500, 2000), None, None)
  val prrModel = PrivateResidenceReliefModel("No", None, None)
  val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")
  val personalDetailsModel = TotalPersonalDetailsCalculationModel(
    CustomerTypeModel("individual"),
    Some(CurrentIncomeModel(20000)),
    Some(PersonalAllowanceModel(0)),
    None,
    OtherPropertiesModel("Yes"),
    Some(PreviousLossOrGainModel("Neither")),
    None,
    None,
    Some(AnnualExemptAmountModel(0)),
    BroughtForwardLossesModel(false, None)
  )
  val totalGainAnswersModel = TotalGainAnswersModel(
    DisposalDateModel(5, 6, 2015),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(true)),
    DisposalValueModel(950000),
    DisposalCostsModel(15000),
    Some(HowBecameOwnerModel("Bought")),
    Some(BoughtForLessModel(false)),
    AcquisitionValueModel(1250000),
    AcquisitionCostsModel(20000),
    AcquisitionDateModel("Yes", Some(10), Some(10), Some(2001)),
    Some(RebasedValueModel(Some(950000))),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel("No", None),
    Some(OtherReliefsModel(0))
  )
  val calculationResultsModel = CalculationResultsWithTaxOwedModel(
    TotalTaxOwedModel(100, 100, 20, None, None, 200, 100, None, None, None, None, 0, None),
    None,
    None
  )

  when(mockCalcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](Matchers.eq(KeystoreKeys.privateResidenceRelief))(Matchers.any(), Matchers.any()))
    .thenReturn(Future.successful(Some(prrModel)))

  when(mockCalcConnector.calculateTaxableGainAfterPRR(Matchers.any(), Matchers.any())(Matchers.any()))
    .thenReturn(Some(calculationResultsWithPRRModel))

  when(mockAnswersConstructor.getPersonalDetailsAndPreviousCapitalGainsAnswers(Matchers.any()))
    .thenReturn(Future.successful(Some(personalDetailsModel)))

  when(mockCalcConnector.getPartialAEA(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(Some(BigDecimal(5500))))

  when(mockCalcConnector.getFullAEA(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(Some(BigDecimal(11000))))

  when(mockCalcConnector.getTaxYear(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(Some(TaxYearModel("2015/16", isValidYear = true, "2015/16"))))

  when(mockCalcConnector.calculateNRCGTTotalTax(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(Some(calculationResultsModel)))

  "Calling .getPRRResponse" should {

    "when supplied with a totalGainResultsModel with at least one positive gain" should {

      val totalGainModel = TotalGainResultsModel(BigDecimal(200), None, None)
      val result = TaxableGainCalculation.getPRRResponse(totalGainModel, mockCalcConnector)

      "return a PRR model" in {
        await(result) shouldBe Some(prrModel)
      }
    }

    "when supplied with a totalGainResultsModel no positive gains" should {

      val totalGainModel = TotalGainResultsModel(BigDecimal(-200), None, None)
      val result = TaxableGainCalculation.getPRRResponse(totalGainModel, mockCalcConnector)

      "return a None" in {
        await(result) shouldBe None
      }
    }
  }

  "Calling .getPRRIfApplicable" should {

    "when supplied with no PRR model" should {
      val result = TaxableGainCalculation.getPRRIfApplicable(totalGainAnswersModel, None, mockCalcConnector)

      "return a None" in {
        await(result) shouldBe None
      }
    }

    "when supplied with a PRR model" should {
      val result = TaxableGainCalculation.getPRRIfApplicable(totalGainAnswersModel, Some(prrModel), mockCalcConnector)

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
          mockCalcConnector, mockAnswersConstructor)

        "return a TotalPersonalDetailsCalculationModel" in {
          await(result) shouldBe Some(personalDetailsModel)
        }
      }

      "along with a TotalGainModel that has no positive taxable gains" should {
        val totalGainModel = TotalGainResultsModel(BigDecimal(-200), Some(BigDecimal(-200)), Some(BigDecimal(-200)))
        val calculationResultsWithPRRModel = CalculationResultsWithPRRModel(GainsAfterPRRModel(1000, taxableGain = -1000, 0), None, None)

        val result = TaxableGainCalculation.getFinalSectionsAnswers(totalGainModel, Some(calculationResultsWithPRRModel),
          mockCalcConnector, mockAnswersConstructor)

        "return a value of None" in {
          await(result) shouldBe None
        }
      }
    }

    "not supplied with a calculationResultsWithPrrModel" when {
      "with at least one positive gain" should {
        val totalGainModel = TotalGainResultsModel(BigDecimal(200), None, None)
        val result = TaxableGainCalculation.getFinalSectionsAnswers(totalGainModel, None, mockCalcConnector, mockAnswersConstructor)

        "return a TotalPersonalDetailsCalculationModel" in {
          await(result) shouldBe Some(personalDetailsModel)
        }
      }

      "with no positive gains" should {
        val totalGainModel = TotalGainResultsModel(BigDecimal(-200), None, None)
        val result = TaxableGainCalculation.getFinalSectionsAnswers(totalGainModel, None, mockCalcConnector, mockAnswersConstructor)

        "return a value of None" in {
          await(result) shouldBe None
        }
      }
    }
  }

  "Calling .getMaxAEA" when {

    "CustomerType is individual" should {
      val personalDetailsModel = TotalPersonalDetailsCalculationModel(
        CustomerTypeModel("individual"),
        Some(CurrentIncomeModel(20000)),
        Some(PersonalAllowanceModel(0)),
        None,
        OtherPropertiesModel("Yes"),
        Some(PreviousLossOrGainModel("Neither")),
        None,
        None,
        Some(AnnualExemptAmountModel(0)),
        BroughtForwardLossesModel(false, None)
      )
      val result = TaxableGainCalculation.getMaxAEA(Some(personalDetailsModel), Some(taxYearModel), mockCalcConnector)

      "return the full AEA of 11000" in {
        await(result) shouldBe Some(BigDecimal(11000))
      }
    }

    "CustomerType is trustee and not trustee for a vulnerable person" should {
      val personalDetailsModel = TotalPersonalDetailsCalculationModel(
        CustomerTypeModel("trustee"),
        Some(CurrentIncomeModel(20000)),
        Some(PersonalAllowanceModel(0)),
        Some(DisabledTrusteeModel("No")),
        OtherPropertiesModel("Yes"),
        Some(PreviousLossOrGainModel("Neither")),
        None,
        None,
        Some(AnnualExemptAmountModel(0)),
        BroughtForwardLossesModel(false, None)
      )
      val result = TaxableGainCalculation.getMaxAEA(Some(personalDetailsModel), Some(taxYearModel), mockCalcConnector)

      "return the partial AEA of 5500" in {
        await(result) shouldBe Some(BigDecimal(5500))
      }
    }

    "CustomerType is trustee and trustee for a vulnerable person" should {
      val personalDetailsModel = TotalPersonalDetailsCalculationModel(
        CustomerTypeModel("trustee"),
        Some(CurrentIncomeModel(20000)),
        Some(PersonalAllowanceModel(0)),
        Some(DisabledTrusteeModel("Yes")),
        OtherPropertiesModel("Yes"),
        Some(PreviousLossOrGainModel("Neither")),
        None,
        None,
        Some(AnnualExemptAmountModel(0)),
        BroughtForwardLossesModel(false, None)
      )
      val result = TaxableGainCalculation.getMaxAEA(Some(personalDetailsModel), Some(taxYearModel), mockCalcConnector)

      "return the full AEA of 11000" in {
        await(result) shouldBe Some(BigDecimal(11000))
      }
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

      val result = TaxableGainCalculation.getChargeableGain(totalGainAnswersModel, Some(prrModel), Some(personalDetailsModel), 11000, mockCalcConnector)

      "return a CalculationResultsWithTaxOwedModel" in {
        await(result) shouldBe Some(calculationResultsModel)
      }
    }

    "supplied with no totalPersonalDetailsCalculationModel" should {

      val result = TaxableGainCalculation.getChargeableGain(totalGainAnswersModel, Some(prrModel), None, 11000, mockCalcConnector)

      "return a None" in {
        await(result) shouldBe None
      }
    }
  }
}