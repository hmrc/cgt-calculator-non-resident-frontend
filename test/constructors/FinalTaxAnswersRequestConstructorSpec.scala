/*
 * Copyright 2017 HM Revenue & Customs
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

import models._
import uk.gov.hmrc.play.test.UnitSpec

class FinalTaxAnswersRequestConstructorSpec extends UnitSpec {

  "Calling .customerType" should {
    "produce a valid query string" in {
      FinalTaxAnswersRequestConstructor.customerType(CustomerTypeModel("individual")) shouldEqual "&customerType=individual"
    }
  }

  "Calling .currentIncome" should {
    "produce a valid query string when Customer type is individual" in {
      FinalTaxAnswersRequestConstructor.currentIncome(CustomerTypeModel("individual"), Some(CurrentIncomeModel(10000))) shouldEqual
      "&currentIncome=10000"
    }

    "produce a blank query string when Customer type is anything else" in {
      FinalTaxAnswersRequestConstructor.currentIncome(CustomerTypeModel("trustee"), Some(CurrentIncomeModel(10000))) shouldEqual ""
    }
  }

  "Calling .personalAllowanceAmt" should {
    "produce a valid query string when Customer type is individual" in {
      FinalTaxAnswersRequestConstructor.personalAllowanceAmt(CustomerTypeModel("individual"), Some(PersonalAllowanceModel(10000))) shouldEqual
      "&personalAllowanceAmt=10000"
    }

    "produce a blank query string when Customer type is anything else" in {
      FinalTaxAnswersRequestConstructor.personalAllowanceAmt(CustomerTypeModel("trustee"), Some(PersonalAllowanceModel(10000))) shouldEqual ""
    }
  }

  "Calling .allowableLoss" should {
    "produce a valid query string when the user claims other properties and that was a loss" in {
      FinalTaxAnswersRequestConstructor.allowableLoss(
        OtherPropertiesModel("Yes"),
        Some(PreviousLossOrGainModel("Loss")),
        Some(HowMuchLossModel(100))) shouldEqual "&allowableLoss=100"
    }

    "produce a blank query string when the user claims other properties and that was a gain" in {
      FinalTaxAnswersRequestConstructor.allowableLoss(
        OtherPropertiesModel("Yes"),
        Some(PreviousLossOrGainModel("Gain")),
        Some(HowMuchLossModel(100))) shouldEqual ""
    }

    "produce a blank query string when the user does not claim other properties" in {
      FinalTaxAnswersRequestConstructor.allowableLoss(
        OtherPropertiesModel("No"),
        Some(PreviousLossOrGainModel("Loss")),
        Some(HowMuchLossModel(100))) shouldEqual ""
    }
  }

  "Calling .previousGain" should {
    "produce a valid query string when the user claims other properties and that was a gain" in {
      FinalTaxAnswersRequestConstructor.previousGain(
        OtherPropertiesModel("Yes"),
        Some(PreviousLossOrGainModel("Gain")),
        Some(HowMuchGainModel(100))) shouldEqual "&previousGain=100"
    }

    "produce a blank query string when the user claims other properties and that was a loss" in {
      FinalTaxAnswersRequestConstructor.previousGain(
        OtherPropertiesModel("Yes"),
        Some(PreviousLossOrGainModel("Loss")),
        Some(HowMuchGainModel(100))) shouldEqual ""
    }

    "produce a blank query string when the user does not claim other properties" in {
      FinalTaxAnswersRequestConstructor.previousGain(
        OtherPropertiesModel("No"),
        Some(PreviousLossOrGainModel("Loss")),
        Some(HowMuchGainModel(100))) shouldEqual ""
    }
  }

  "Calling .annualExemptAmount" should {

    "produce the entered amount for aea if otherProperties = Yes, previousLossOrGain = Loss, howMuchLoss == 0.0" in {
      FinalTaxAnswersRequestConstructor.annualExemptAmount(
        OtherPropertiesModel("Yes"),
        Some(PreviousLossOrGainModel("Loss")),
        Some(HowMuchLossModel(0.0)),
        None,
        Some(AnnualExemptAmountModel(2000)),
        1000.0
      ) shouldEqual "&annualExemptAmount=2000"
    }

    "produce the entered amount for aea if otherProperties = Yes, previousLossOrGain = Gain, howMuchGain == 0.0" in {
      FinalTaxAnswersRequestConstructor.annualExemptAmount(
        OtherPropertiesModel("Yes"),
        Some(PreviousLossOrGainModel("Gain")),
        Some(HowMuchLossModel(1.0)),
        Some(HowMuchGainModel(0.0)),
        Some(AnnualExemptAmountModel(2000)),
        1000.0
      ) shouldEqual "&annualExemptAmount=2000"
    }

    "produce the entered amount for aea if otherProperties = Yes, previousLossOrGain = Neither" in {
      FinalTaxAnswersRequestConstructor.annualExemptAmount(
        OtherPropertiesModel("Yes"),
        Some(PreviousLossOrGainModel("Neither")),
        None,
        None,
        Some(AnnualExemptAmountModel(2000)),
        1000.0
      ) shouldEqual "&annualExemptAmount=2000"
    }

    "otherwise produce the maximum annual exempt amount supplied" in {
      FinalTaxAnswersRequestConstructor.annualExemptAmount(
        OtherPropertiesModel("No"),
        Some(PreviousLossOrGainModel("Neither")),
        None,
        None,
        Some(AnnualExemptAmountModel(2000)),
        1000
      ) shouldEqual "&annualExemptAmount=1000"
    }
  }

  "Calling .broughtForwardLosses" should {

    "produce a valid query string when claiming" in {
      FinalTaxAnswersRequestConstructor.broughtForwardLosses(BroughtForwardLossesModel(true, Some(10000))) shouldEqual "&broughtForwardLoss=10000"
    }

    "produce a blank query string when not claiming" in {
      FinalTaxAnswersRequestConstructor.broughtForwardLosses(BroughtForwardLossesModel(false, Some(10000))) shouldEqual ""
    }
  }
}
