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

package constructors

import common.CommonPlaySpec
import models._
import util.Util.trim

class FinalTaxAnswersRequestConstructorSpec extends CommonPlaySpec {

  def trimAndSort(map: Map[String, Option[String]]): Seq[(String, String)] = trim(map).toSeq.sorted

  "Calling .annualExemptAmount" should {

    "produce the entered amount for aea if otherProperties = Yes, previousLossOrGain = Loss, howMuchLoss == 0.0" in {
      FinalTaxAnswersRequestConstructor.annualExemptAmount(
        OtherPropertiesModel("Yes"),
        Some(PreviousLossOrGainModel("Loss")),
        Some(HowMuchLossModel(0.0)),
        None,
        Some(AnnualExemptAmountModel(2000)),
        1000.0
      ) shouldEqual 2000.0
    }

    "produce the entered amount for aea if otherProperties = Yes, previousLossOrGain = Gain, howMuchGain == 0.0" in {
      FinalTaxAnswersRequestConstructor.annualExemptAmount(
        OtherPropertiesModel("Yes"),
        Some(PreviousLossOrGainModel("Gain")),
        Some(HowMuchLossModel(1.0)),
        Some(HowMuchGainModel(0.0)),
        Some(AnnualExemptAmountModel(2000)),
        1000.0
      ) shouldEqual 2000.0
    }

    "produce the entered amount for aea if otherProperties = Yes, previousLossOrGain = Gain, howMuchGain == 10000.0" in {
      FinalTaxAnswersRequestConstructor.annualExemptAmount(
        OtherPropertiesModel("Yes"),
        Some(PreviousLossOrGainModel("Gain")),
        Some(HowMuchLossModel(1.0)),
        Some(HowMuchGainModel(10000.0)),
        Some(AnnualExemptAmountModel(2000)),
        1000.0
      ) shouldEqual 0
    }

    "produce the entered amount for aea if otherProperties = Yes, previousLossOrGain = Neither" in {
      FinalTaxAnswersRequestConstructor.annualExemptAmount(
        OtherPropertiesModel("Yes"),
        Some(PreviousLossOrGainModel("Neither")),
        None,
        None,
        Some(AnnualExemptAmountModel(2000)),
        1000.0
      ) shouldEqual 2000.0
    }

    "otherwise produce the maximum annual exempt amount supplied" in {
      FinalTaxAnswersRequestConstructor.annualExemptAmount(
        OtherPropertiesModel("No"),
        Some(PreviousLossOrGainModel("Neither")),
        None,
        None,
        Some(AnnualExemptAmountModel(2000)),
        1000
      ) shouldEqual 1000.0
    }
  }

  "Calling .additionalParametersQuery" should {
    "Return a String only containing the annualExemptAmount" when {
      "No TotalPersonalDetailsCalculationModel is passed" in {
        trimAndSort(FinalTaxAnswersRequestConstructor.additionalParametersQuery(None, BigDecimal(100))) shouldBe
          Seq("annualExemptAmount" -> "100")
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
          broughtForwardLossesModel = BroughtForwardLossesModel(isClaiming = false, None))

        trimAndSort(FinalTaxAnswersRequestConstructor.additionalParametersQuery(Some(model), BigDecimal(100))) shouldBe
          Seq("annualExemptAmount" -> "100", "currentIncome" -> "100")
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
          broughtForwardLossesModel = BroughtForwardLossesModel(isClaiming = false, None))

        val result = FinalTaxAnswersRequestConstructor.additionalParametersQuery(Some(model), model.annualExemptAmountModel.get.annualExemptAmount)

        trimAndSort(result) shouldBe Seq("annualExemptAmount" -> "500", "currentIncome" -> "100", "personalAllowanceAmt" -> "200")
      }
    }

  }

}
