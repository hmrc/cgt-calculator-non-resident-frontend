/*
 * Copyright 2023 HM Revenue & Customs
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

package constructors

import common.CommonPlaySpec
import models._

class FinalTaxAnswersRequestConstructorSpec extends CommonPlaySpec {

  "Calling .currentIncome" should {
    "produce a valid query string for current income" in {
      FinalTaxAnswersRequestConstructor.currentIncome(Some(CurrentIncomeModel(10000))) shouldEqual
      "&currentIncome=10000.0"
    }

    "produce an empty query string for current income if not provided" in {
      FinalTaxAnswersRequestConstructor.currentIncome(None) shouldEqual ""
    }
  }

  "Calling .personalAllowanceAmt" should {
    "produce a valid query string when a personal allowance is supplied" in {
      FinalTaxAnswersRequestConstructor.personalAllowanceAmt(Some(PersonalAllowanceModel(10000))) shouldEqual
      "&personalAllowanceAmt=10000.0"
    }

    "produce a blank query string when no personal allowance is present is anything else" in {
      FinalTaxAnswersRequestConstructor.personalAllowanceAmt(None) shouldEqual ""
    }
  }

  "Calling .allowableLoss" should {
    "produce a valid query string when the user claims other properties and that was a loss" in {
      FinalTaxAnswersRequestConstructor.allowableLoss(
        Some(OtherPropertiesModel("Yes")),
        Some(PreviousLossOrGainModel("Loss")),
        Some(HowMuchLossModel(100))) shouldEqual "&allowableLoss=100.0"
    }

    "produce a blank query string when the user claims other properties and that was a gain" in {
      FinalTaxAnswersRequestConstructor.allowableLoss(
        Some(OtherPropertiesModel("Yes")),
        Some(PreviousLossOrGainModel("Gain")),
        Some(HowMuchLossModel(100))) shouldEqual ""
    }

    "produce a blank query string when the user does not claim other properties" in {
      FinalTaxAnswersRequestConstructor.allowableLoss(
        Some(OtherPropertiesModel("No")),
        Some(PreviousLossOrGainModel("Loss")),
        Some(HowMuchLossModel(100))) shouldEqual ""
    }

    "produce a blank query string with no data" in {
      FinalTaxAnswersRequestConstructor.allowableLoss(None, None, None) shouldBe ""
    }
  }

  "Calling .previousGain" should {
    "produce a valid query string when the user claims other properties and that was a gain" in {
      FinalTaxAnswersRequestConstructor.previousGain(
        Some(OtherPropertiesModel("Yes")),
        Some(PreviousLossOrGainModel("Gain")),
        Some(HowMuchGainModel(100))) shouldEqual "&previousGain=100.0"
    }

    "produce a blank query string when the user claims other properties and that was a loss" in {
      FinalTaxAnswersRequestConstructor.previousGain(
        Some(OtherPropertiesModel("Yes")),
        Some(PreviousLossOrGainModel("Loss")),
        Some(HowMuchGainModel(100))) shouldEqual ""
    }

    "produce a blank query string when the user does not claim other properties" in {
      FinalTaxAnswersRequestConstructor.previousGain(
        Some(OtherPropertiesModel("No")),
        Some(PreviousLossOrGainModel("Loss")),
        Some(HowMuchGainModel(100))) shouldEqual ""
    }

    "produce a blank query string with no data" in {
      FinalTaxAnswersRequestConstructor.previousGain(None, None, None) shouldBe ""
    }
  }

  "Calling .annualExemptAmount" should {

    "produce the entered amount for aea if otherProperties = Yes, previousLossOrGain = Loss, howMuchLoss == 0.0" in {
      FinalTaxAnswersRequestConstructor.annualExemptAmount(
        Some(OtherPropertiesModel("Yes")),
        Some(PreviousLossOrGainModel("Loss")),
        Some(HowMuchLossModel(0.0)),
        None,
        Some(AnnualExemptAmountModel(2000)),
        1000.0
      ) shouldEqual "&annualExemptAmount=2000.0"
    }

    "produce the entered amount for aea if otherProperties = Yes, previousLossOrGain = Gain, howMuchGain == 0.0" in {
      FinalTaxAnswersRequestConstructor.annualExemptAmount(
        Some(OtherPropertiesModel("Yes")),
        Some(PreviousLossOrGainModel("Gain")),
        Some(HowMuchLossModel(1.0)),
        Some(HowMuchGainModel(0.0)),
        Some(AnnualExemptAmountModel(2000)),
        1000.0
      ) shouldEqual "&annualExemptAmount=2000.0"
    }

    "produce the entered amount for aea if otherProperties = Yes, previousLossOrGain = Gain, howMuchGain == 10000.0" in {
      FinalTaxAnswersRequestConstructor.annualExemptAmount(
        Some(OtherPropertiesModel("Yes")),
        Some(PreviousLossOrGainModel("Gain")),
        Some(HowMuchLossModel(1.0)),
        Some(HowMuchGainModel(10000.0)),
        Some(AnnualExemptAmountModel(2000)),
        1000.0
      ) shouldEqual "&annualExemptAmount=0"
    }

    "produce the entered amount for aea if otherProperties = Yes, previousLossOrGain = Neither" in {
      FinalTaxAnswersRequestConstructor.annualExemptAmount(
        Some(OtherPropertiesModel("Yes")),
        Some(PreviousLossOrGainModel("Neither")),
        None,
        None,
        Some(AnnualExemptAmountModel(2000)),
        1000.0
      ) shouldEqual "&annualExemptAmount=2000.0"
    }

    "otherwise produce the maximum annual exempt amount supplied" in {
      FinalTaxAnswersRequestConstructor.annualExemptAmount(
        Some(OtherPropertiesModel("No")),
        Some(PreviousLossOrGainModel("Neither")),
        None,
        None,
        Some(AnnualExemptAmountModel(2000)),
        1000
      ) shouldEqual "&annualExemptAmount=1000.0"
    }

    "produce the maximum AEA value if no data is provided" in {
      FinalTaxAnswersRequestConstructor.annualExemptAmount(None, None, None, None, None, 1000) shouldEqual
        "&annualExemptAmount=1000.0"
    }
  }

  "Calling .broughtForwardLosses" should {

    "produce a valid query string when claiming" in {
      FinalTaxAnswersRequestConstructor.broughtForwardLosses(Some(BroughtForwardLossesModel(isClaiming = true,
        Some(10000)))) shouldEqual "&broughtForwardLoss=10000.0"
    }

    "produce a blank query string when not claiming" in {
      FinalTaxAnswersRequestConstructor.broughtForwardLosses(Some(BroughtForwardLossesModel(isClaiming = false, Some(10000)))) shouldEqual ""
    }

    "produce a blank query string with no data" in {
      FinalTaxAnswersRequestConstructor.broughtForwardLosses(None) shouldBe ""
    }
  }

  "Calling .additionalParametersQuery" should {
    "Return a String only containing the annualExemptAmount" when {
      "No TotalPersonalDetailsCalculationModel is passed" in {
        FinalTaxAnswersRequestConstructor.additionalParametersQuery(None, BigDecimal(100)) shouldBe
          "&annualExemptAmount=100.0"
      }
    }

    "Return a String containing currentIncome & annualExemptAmount" when {
      "A  minimum TotalPersonalDetailsCalculationModel is passed" in {
        val model = TotalPersonalDetailsCalculationModel(currentIncomeModel = CurrentIncomeModel(BigDecimal(100)),
          personalAllowanceModel = None,
          otherPropertiesModel = OtherPropertiesModel(""),
          previousGainOrLoss = None,
          howMuchLossModel = None,
          howMuchGainModel = None,
          annualExemptAmountModel = None,
          broughtForwardLossesModel = BroughtForwardLossesModel(false, None))

        FinalTaxAnswersRequestConstructor.additionalParametersQuery(Some(model), BigDecimal(100)) shouldBe
          "&currentIncome=100.0&annualExemptAmount=100.0"
      }
    }

    "Return a String containing all data" when {
      "A TotalPersonalDetailsCalculationModel is passed" in {
        val model = TotalPersonalDetailsCalculationModel(currentIncomeModel = CurrentIncomeModel(BigDecimal(100)),
          personalAllowanceModel = Some(PersonalAllowanceModel(BigDecimal(200))),
          otherPropertiesModel = OtherPropertiesModel(""),
          previousGainOrLoss = Some(PreviousLossOrGainModel("loss")),
          howMuchLossModel = Some(HowMuchLossModel(BigDecimal(300))),
          howMuchGainModel = Some(HowMuchGainModel(BigDecimal(400))),
          annualExemptAmountModel = Some(AnnualExemptAmountModel(BigDecimal(500))),
          broughtForwardLossesModel = BroughtForwardLossesModel(false, None))

        FinalTaxAnswersRequestConstructor.additionalParametersQuery(Some(model), model.annualExemptAmountModel.get.annualExemptAmount) shouldBe
          "&currentIncome=100.0&personalAllowanceAmt=200.0&annualExemptAmount=500.0"
      }
    }

  }

}
