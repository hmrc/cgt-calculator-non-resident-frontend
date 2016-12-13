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

package constructors.nonresident

import assets.MessageLookup.{NonResident => messages}
import controllers.nonresident.{routes => routes}
import common.KeystoreKeys
import common.TestModels._
import common.nonresident.{CustomerTypeKeys, PreviousGainOrLossKeys}
import models.nonresident._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class PersonalDetailsConstructorSpec extends UnitSpec with WithFakeApplication {

  val target = PersonalDetailsConstructor

  "calling .getCustomerTypeAnswer" when {

    "a customer type of individual" should {

      lazy val result = target.getCustomerTypeAnswer(CustomerTypeModel(CustomerTypeKeys.individual))

      "return some details for the CustomerType" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.customerType}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.customerType
        }
      }

      s"return a question of ${messages.CustomerType.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.CustomerType.question
        }
      }

      s"return the correct answer" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe messages.CustomerType.individual
        }
      }

      s"return a link of ${routes.CustomerTypeController.customerType().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.CustomerTypeController.customerType().url
          }
        }
      }
    }

    "a customer type of trustee" should {

      lazy val result = target.getCustomerTypeAnswer(CustomerTypeModel(CustomerTypeKeys.trustee))

      "return some details for the CustomerType" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.customerType}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.customerType
        }
      }

      s"return a question of ${messages.CustomerType.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.CustomerType.question
        }
      }

      s"return the correct answer" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe messages.CustomerType.trustee
        }
      }

      s"return a link of ${routes.CustomerTypeController.customerType().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.CustomerTypeController.customerType().url
          }
        }
      }
    }

    "a customer type of personal rep" should {

      lazy val result = target.getCustomerTypeAnswer(CustomerTypeModel(CustomerTypeKeys.personalRep))

      "return some details for the CustomerType" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.customerType}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.customerType
        }
      }

      s"return a question of ${messages.CustomerType.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.CustomerType.question
        }
      }

      s"return the correct answer" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe messages.CustomerType.personalRep
        }
      }

      s"return a link of ${routes.CustomerTypeController.customerType().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.CustomerTypeController.customerType().url
          }
        }
      }
    }
  }

  "calling .getCurrentIncomeAnswer" when {

    "a current income greater than 0" should {

      lazy val result = target.getCurrentIncomeAnswer(CustomerTypeModel(CustomerTypeKeys.individual), Some(CurrentIncomeModel(1000)))

      "return some details for the CurrentIncome" in {
        result should not be None
      }

      s"return and id of ${KeystoreKeys.currentIncome}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.currentIncome
        }
      }

      s"return a question of ${messages.CurrentIncome.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.CurrentIncome.question
        }
      }

      "return data of greater than 0" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe 1000
        }
      }

      s"return a link of ${routes.CurrentIncomeController.currentIncome().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.CurrentIncomeController.currentIncome().url
          }
        }
      }
    }

    "a current income of 0.0" should {

      lazy val result = target.getCurrentIncomeAnswer(CustomerTypeModel(CustomerTypeKeys.individual), Some(CurrentIncomeModel(0)))

      "return some details for the CurrentIncome" in {
        result should not be None
      }

      s"return and id of ${KeystoreKeys.currentIncome}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.currentIncome
        }
      }

      s"return a question of ${messages.CurrentIncome.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.CurrentIncome.question
        }
      }

      "return data of 0.0" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe summaryIndividualFlatNoIncomeOtherPropNo.currentIncomeModel.get.currentIncome
        }
      }

      s"return a link of ${routes.CurrentIncomeController.currentIncome().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.CurrentIncomeController.currentIncome().url
          }
        }
      }

    }

    "no current income is given" should {

      lazy val result = target.getCurrentIncomeAnswer(CustomerTypeModel(CustomerTypeKeys.trustee), None)

      "return a None" in {
        result shouldBe None
      }
    }
  }

  "calling .getPersonalAllowanceAnswer" when {

    "a personal allowance of greater than 0 is given" should {

      lazy val result = target.getPersonalAllowanceAnswer(CustomerTypeModel(CustomerTypeKeys.individual), Some(PersonalAllowanceModel(10000)),
        Some(CurrentIncomeModel(10000)))

      "return some details for the PersonalAllowance" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.personalAllowance}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.personalAllowance
        }
      }

      "return data of greater than 0 " in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe 10000
        }
      }

      s"return a question of ${messages.PersonalAllowance.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.PersonalAllowance.question
        }
      }

      s"return a link of ${routes.PersonalAllowanceController.personalAllowance().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.PersonalAllowanceController.personalAllowance().url
          }
        }
      }
    }

    "a personal allowance of 0 is given" should {

      lazy val result = target.getPersonalAllowanceAnswer(CustomerTypeModel(CustomerTypeKeys.individual), Some(PersonalAllowanceModel(0)),
        Some(CurrentIncomeModel(10000)))

      "return some details for the PersonalAllowance" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.personalAllowance}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.personalAllowance
        }
      }

      "return data of 0.0 " in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe 0.0
        }
      }

      s"return a question of ${messages.PersonalAllowance.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.PersonalAllowance.question
        }
      }

      s"return a link of ${routes.PersonalAllowanceController.personalAllowance().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.PersonalAllowanceController.personalAllowance().url
          }
        }
      }
    }

    "personal allowance is not given as current income of 0 is given" should {

      lazy val result = target.getPersonalAllowanceAnswer(CustomerTypeModel(CustomerTypeKeys.individual), Some(PersonalAllowanceModel(100)),
        Some(CurrentIncomeModel(0)))

      "return a None" in {
        result shouldBe None
      }
    }

    "no personal allowance is given" should {

      lazy val result = target.getPersonalAllowanceAnswer(CustomerTypeModel(CustomerTypeKeys.trustee), None, None)

      "return a None" in {
        result shouldBe None
      }
    }
  }

  "calling .getDisabledTrusteeAnswer" when {

    "no disabled trustee is given" should {

      lazy val result = target.getDisabledTrusteeAnswer(CustomerTypeModel(CustomerTypeKeys.individual), None)

      "return a None" in {
        result shouldBe None
      }
    }

    "a disabled trustee with Yes is supplied" should {

      lazy val result = target.getDisabledTrusteeAnswer(CustomerTypeModel(CustomerTypeKeys.trustee), Some(DisabledTrusteeModel("Yes")))

      "return some details for the DisabledTrustee" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.disabledTrustee}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.disabledTrustee
        }
      }

      s"return data of ${messages.yes}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe messages.yes
        }
      }

      s"return a question of ${messages.DisabledTrustee.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.DisabledTrustee.question
        }
      }

      s"return an id of ${routes.DisabledTrusteeController.disabledTrustee().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.DisabledTrusteeController.disabledTrustee().url
          }
        }
      }
    }

    "a disabled trustee with No is supplied" should {

      lazy val result = target.getDisabledTrusteeAnswer(CustomerTypeModel(CustomerTypeKeys.trustee), Some(DisabledTrusteeModel("No")))

      "return some details for the DisabledTrustee" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.disabledTrustee}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.disabledTrustee
        }
      }

      s"return data of ${messages.no}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe messages.no
        }
      }

      s"return a question of ${messages.DisabledTrustee.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.DisabledTrustee.question
        }
      }

      s"return an id of ${routes.DisabledTrusteeController.disabledTrustee().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.DisabledTrusteeController.disabledTrustee().url
          }
        }
      }
    }
  }

  "calling .getOtherPropertiesAnswer" when {

    "a otherPropertiesAnswer of yes is given" should {

      lazy val result = PersonalDetailsConstructor.getOtherPropertiesAnswer(OtherPropertiesModel("Yes"))

      "return some details for the OtherProperties" in {
        result should not be None
      }

      s"return ${messages.yes}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe messages.yes
        }
      }

      s"return an ID of ${KeystoreKeys.otherProperties}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.otherProperties
        }
      }

      s"return a question of ${messages.OtherProperties.question} " in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.OtherProperties.question
        }
      }

      s"return a link of ${routes.OtherPropertiesController.otherProperties().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.OtherPropertiesController.otherProperties().url
          }
        }
      }
    }

    "a otherPropertiesAnswer of no is given" should {

      lazy val result = PersonalDetailsConstructor.getOtherPropertiesAnswer(OtherPropertiesModel("No"))

      "return some details for the OtherProperties" in {
        result should not be None
      }

      s"return data of ${messages.no}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe messages.no
        }
      }

      s"return an ID of ${KeystoreKeys.otherProperties}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.otherProperties
        }
      }

      s"return a question of ${messages.OtherProperties.question} " in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.OtherProperties.question
        }
      }

      s"return a link of ${routes.OtherPropertiesController.otherProperties().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.OtherPropertiesController.otherProperties().url
          }
        }
      }
    }
  }

  "Calling previousGainOrLossAnswer" when {

    "other properties have been disposed of" should {
      lazy val result = PersonalDetailsConstructor.previousGainOrLossAnswer(OtherPropertiesModel("Yes"),
        Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.gain)))

      "return a Some" in {
        result.isDefined shouldBe true
      }

      "return data of 'Gain'" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe PreviousGainOrLossKeys.gain
        }
      }

      s"return a question of ${messages.PreviousLossOrGain.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.PreviousLossOrGain.question
        }
      }

      s"return an id of ${KeystoreKeys.NonResidentKeys.previousLossOrGain}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.NonResidentKeys.previousLossOrGain
        }
      }

      s"return a link of ${routes.PreviousGainOrLossController.previousGainOrLoss().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link shouldBe Some(routes.PreviousGainOrLossController.previousGainOrLoss().url)
        }
      }
    }

    "no other properties have been disposed" should {
      lazy val result = PersonalDetailsConstructor.previousGainOrLossAnswer(OtherPropertiesModel("No"), None)

      "return a None" in {
        result shouldBe None
      }
    }
  }

  "Calling howMuchGainAnswer" when {

    "no other properties have been disposed" should {
      lazy val result = PersonalDetailsConstructor.howMuchGainAnswer(OtherPropertiesModel("No"), None, None)

      "return a None" in {
        result shouldBe None
      }
    }

    "other properties have not made a gain" should {
      lazy val result = PersonalDetailsConstructor.howMuchGainAnswer(OtherPropertiesModel("Yes"),
        Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.loss)), None)

      "return a None" in {
        result shouldBe None
      }
    }

    "other properties have made a gain" should {
      lazy val result = PersonalDetailsConstructor.howMuchGainAnswer(OtherPropertiesModel("Yes"),
        Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.gain)), Some(HowMuchGainModel(1000)))

      "return a Some" in {
        result.isDefined shouldBe true
      }

      "return data of '1000'" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe 1000
        }
      }

      s"return a question of ${messages.HowMuchGain.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.HowMuchGain.question
        }
      }

      s"return an id of ${KeystoreKeys.howMuchGain}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.howMuchGain
        }
      }

      s"return a link of ${routes.HowMuchGainController.howMuchGain().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link shouldBe Some(routes.HowMuchGainController.howMuchGain().url)
        }
      }
    }
  }

  "Calling howMuchLossAnswer" when {

    "no other properties have been disposed" should {
      lazy val result = PersonalDetailsConstructor.howMuchLossAnswer(OtherPropertiesModel("No"), None, None)

      "return a None" in {
        result shouldBe None
      }
    }

    "other properties have not made a loss" should {
      lazy val result = PersonalDetailsConstructor.howMuchLossAnswer(OtherPropertiesModel("Yes"),
        Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.gain)), None)

      "return a None" in {
        result shouldBe None
      }
    }

    "other properties have made a loss" should {
      lazy val result = PersonalDetailsConstructor.howMuchLossAnswer(OtherPropertiesModel("Yes"),
        Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.loss)), Some(HowMuchLossModel(1000)))

      "return a Some" in {
        result.isDefined shouldBe true
      }

      "return data of '1000'" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe 1000
        }
      }

      s"return a question of ${messages.HowMuchLoss.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.HowMuchLoss.question
        }
      }

      s"return an id of ${KeystoreKeys.howMuchLoss}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.howMuchLoss
        }
      }

      s"return a link of ${routes.HowMuchLossController.howMuchLoss().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link shouldBe Some(routes.HowMuchLossController.howMuchLoss().url)
        }
      }
    }
  }

  "Calling getAnnualExemptAmountAnswer" when {

    "properties disposed broke even" should {
      lazy val result = PersonalDetailsConstructor.getAnnualExemptAmountAnswer(OtherPropertiesModel("Yes"),
        Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.neither)), Some(AnnualExemptAmountModel(11000)),
        None, None)

      "return a Some" in {
        result.isDefined shouldBe true
      }

      "return data of '11000'" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe 11000
        }
      }

      s"return a question of ${messages.AnnualExemptAmount.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.AnnualExemptAmount.question
        }
      }

      s"return an id of ${KeystoreKeys.annualExemptAmount}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.annualExemptAmount
        }
      }

      s"return a link of ${routes.AnnualExemptAmountController.annualExemptAmount().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link shouldBe Some(routes.AnnualExemptAmountController.annualExemptAmount().url)
        }
      }
    }

    "properties disposed had a gain of 0" should {
      lazy val result = PersonalDetailsConstructor.getAnnualExemptAmountAnswer(OtherPropertiesModel("Yes"),
        Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.gain)), Some(AnnualExemptAmountModel(11000)),
        Some(HowMuchGainModel(0)), None)

      "return a Some" in {
        result.isDefined shouldBe true
      }

      "return data of '11000'" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe 11000
        }
      }

      s"return a question of ${messages.AnnualExemptAmount.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.AnnualExemptAmount.question
        }
      }

      s"return an id of ${KeystoreKeys.annualExemptAmount}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.annualExemptAmount
        }
      }

      s"return a link of ${routes.AnnualExemptAmountController.annualExemptAmount().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link shouldBe Some(routes.AnnualExemptAmountController.annualExemptAmount().url)
        }
      }
    }

    "properties disposed had a loss of 0" should {
      lazy val result = PersonalDetailsConstructor.getAnnualExemptAmountAnswer(OtherPropertiesModel("Yes"),
        Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.loss)), Some(AnnualExemptAmountModel(11000)),
        None, Some(HowMuchLossModel(0)))

      "return a Some" in {
        result.isDefined shouldBe true
      }

      "return data of '11000'" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe 11000
        }
      }

      s"return a question of ${messages.AnnualExemptAmount.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.AnnualExemptAmount.question
        }
      }

      s"return an id of ${KeystoreKeys.annualExemptAmount}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.annualExemptAmount
        }
      }

      s"return a link of ${routes.AnnualExemptAmountController.annualExemptAmount().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link shouldBe Some(routes.AnnualExemptAmountController.annualExemptAmount().url)
        }
      }
    }

    "has no previous disposals" should {
      lazy val result = PersonalDetailsConstructor.getAnnualExemptAmountAnswer(OtherPropertiesModel("Yes"), None, None, None, None)

      "return a None" in {
        result shouldBe None
      }
    }
  }

  "Calling .broughtForwardLossesQuestion" should {
    lazy val result = PersonalDetailsConstructor.getBroughtForwardLossesQuestion(BroughtForwardLossesModel(false, None))

    "return a Some" in {
      result.isDefined shouldBe true
    }

    "return data of false" in {
      result.fold(cancel("expected result not computed")) { item =>
        item.data shouldBe false
      }
    }

    s"return a question of ${messages.BroughtForwardLosses.question}" in {
      result.fold(cancel("expected result not computed")) { item =>
        item.question shouldBe messages.BroughtForwardLosses.question
      }
    }

    s"return an id of ${KeystoreKeys.broughtForwardLosses}" in {
      result.fold(cancel("expected result not computed")) { item =>
        item.id shouldBe s"${KeystoreKeys.broughtForwardLosses}-question"
      }
    }

    s"return a link of ${routes.BroughtForwardLossesController.broughtForwardLosses().url}" in {
      result.fold(cancel("expected result not computed")) { item =>
        item.link shouldBe Some(routes.BroughtForwardLossesController.broughtForwardLosses().url)
      }
    }
  }

  "Calling .broughtForwardLossesAnswer" when {

    "an answer of no is given" should {
      lazy val result = PersonalDetailsConstructor.getBroughtForwardLossesAnswer(BroughtForwardLossesModel(false, None))

      "return a None" in {
        result shouldBe None
      }
    }

    "an answer of yes is given" should {
      lazy val result = PersonalDetailsConstructor.getBroughtForwardLossesAnswer(BroughtForwardLossesModel(true, Some(1000)))

      "return a Some" in {
        result.isDefined shouldBe true
      }

      "return data of 1000" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe 1000
        }
      }

      s"return a question of ${messages.BroughtForwardLosses.inputQuestion}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.BroughtForwardLosses.inputQuestion
        }
      }

      s"return an id of ${KeystoreKeys.broughtForwardLosses}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe s"${KeystoreKeys.broughtForwardLosses}"
        }
      }

      s"return a link of ${routes.BroughtForwardLossesController.broughtForwardLosses().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link shouldBe Some(routes.BroughtForwardLossesController.broughtForwardLosses().url)
        }
      }
    }
  }
}
