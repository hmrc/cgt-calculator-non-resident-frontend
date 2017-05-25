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

import assets.MessageLookup.{NonResident => messages}
import common.YesNoKeys
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.nonresident.{CustomerTypeKeys, PreviousGainOrLossKeys}
import models._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import controllers.routes

class PersonalAndPreviousDetailsConstructorSpec extends UnitSpec with WithFakeApplication {

  val modelIndividual = TotalPersonalDetailsCalculationModel(
    CurrentIncomeModel(25000),
    Some(PersonalAllowanceModel(10000)),
    OtherPropertiesModel(YesNoKeys.yes),
    Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.loss)),
    Some(HowMuchLossModel(15000)),
    Some(HowMuchGainModel(13000)),
    Some(AnnualExemptAmountModel(5000)),
    BroughtForwardLossesModel(isClaiming = true, Some(3000))
  )

  "Calling .personalAndPreviousDetailsRows" should {

    lazy val result = PersonalAndPreviousDetailsConstructor.personalAndPreviousDetailsRows(Some(modelIndividual))

    "when called with no model return an empty sequence" in {
      PersonalAndPreviousDetailsConstructor.personalAndPreviousDetailsRows(None) shouldEqual Seq()
    }

    "when called with a valid model return a sequence of size 8" in {
      PersonalAndPreviousDetailsConstructor.personalAndPreviousDetailsRows(Some(modelIndividual)).size shouldEqual 7
    }

    "return a CurrentIncome item" in {
      result.exists(qa => qa.id == s"${KeystoreKeys.currentIncome}-question") shouldBe true
    }

    "return a PersonalAllowance item" in {
      result.exists(qa => qa.id == s"${KeystoreKeys.personalAllowance}-question") shouldBe true
    }

    "not return a DisabledTrustee item" in {
      result.exists(qa => qa.id == s"${KeystoreKeys.disabledTrustee}-question") shouldBe false
    }

    "return a OtherProperties item" in {
      result.exists(qa => qa.id == s"${KeystoreKeys.otherProperties}-question") shouldBe true
    }

    "return a PreviousLossOrGain item" in {
      result.exists(qa => qa.id == s"${KeystoreKeys.previousLossOrGain}-question") shouldBe true
    }

    "return a HowMuchLoss item" in {
      result.exists(qa => qa.id == s"${KeystoreKeys.howMuchLoss}-question") shouldBe true
    }

    "not return a HowMuchGain item" in {
      result.exists(qa => qa.id == s"${KeystoreKeys.howMuchGain}-question") shouldBe false
    }

    "not return a AnnualExemptAmount item" in {
      result.exists(qa => qa.id == s"${KeystoreKeys.annualExemptAmount}-question") shouldBe false
    }

    "return a BroughtForwardLosses item" in {
      result.exists(qa => qa.id == s"${KeystoreKeys.broughtForwardLosses}-question") shouldBe true
    }

    "return a BroughtForwardLossesValue item" in {
      result.exists(qa => qa.id == s"${KeystoreKeys.broughtForwardLosses}-value-question") shouldBe true
    }
  }

  "calling .currentIncomeAnswerRow" when {

    "a customer type of individual and some income" should {

      lazy val result =
        PersonalAndPreviousDetailsConstructor.currentIncomeAnswerRow(CurrentIncomeModel(1000))

      "return some details for the CurrentIncome" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.currentIncome}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe s"${KeystoreKeys.currentIncome}-question"
        }
      }

      s"return a question of ${messages.CurrentIncome.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.CurrentIncome.question
        }
      }

      s"return the correct answer" in {
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
  }

  "calling .personalAllowanceAnswerRow" when {

    "a current income greater than 0 and some allowance" should {

      lazy val result =
        PersonalAndPreviousDetailsConstructor.personalAllowanceAnswerRow(CurrentIncomeModel(1), Some(PersonalAllowanceModel(1000)))

      "return some details for the PersonalAllowance" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.personalAllowance}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe s"${KeystoreKeys.personalAllowance}-question"
        }
      }

      s"return a question of ${messages.PersonalAllowance.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.PersonalAllowance.question
        }
      }

      s"return the correct answer" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe 1000
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

    "a current income of 0 and some allowance" should {

      lazy val result =
        PersonalAndPreviousDetailsConstructor.personalAllowanceAnswerRow(CurrentIncomeModel(0), Some(PersonalAllowanceModel(1000)))

      "not return some details for the PersonalAllowance" in {
        result shouldEqual None
      }
    }
  }

  "calling .otherPropertiesAnswerRow" when {

    "some other properties is claimed" should {

      lazy val result = PersonalAndPreviousDetailsConstructor.otherPropertiesAnswerRow(OtherPropertiesModel(YesNoKeys.yes))

      "return some details for the OtherProperties" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.otherProperties}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe s"${KeystoreKeys.otherProperties}-question"
        }
      }

      s"return a question of ${messages.OtherProperties.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.OtherProperties.question
        }
      }

      s"return the correct answer" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe YesNoKeys.yes
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

    "no other properties is claimed" should {

      lazy val result = PersonalAndPreviousDetailsConstructor.otherPropertiesAnswerRow(OtherPropertiesModel(YesNoKeys.no))

      "return some details for the OtherProperties" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.otherProperties}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe s"${KeystoreKeys.otherProperties}-question"
        }
      }

      s"return a question of ${messages.OtherProperties.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.OtherProperties.question
        }
      }

      s"return the correct answer" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe YesNoKeys.no
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

  "calling .previousGainsOrLossAnswerRow" when {

    "some other properties is claimed and the previous is a loss" should {

      lazy val result = PersonalAndPreviousDetailsConstructor.previousGainsOrLossAnswerRow(
        OtherPropertiesModel(YesNoKeys.yes),
        Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.loss)))

      "return some details for the PreviousLossOrGains" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.previousLossOrGain}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe s"${KeystoreKeys.previousLossOrGain}-question"
        }
      }

      s"return a question of ${messages.PreviousLossOrGain.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.PreviousLossOrGain.question
        }
      }

      s"return the correct answer" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe PreviousGainOrLossKeys.loss
        }
      }

      s"return a link of ${routes.PreviousGainOrLossController.previousGainOrLoss().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.PreviousGainOrLossController.previousGainOrLoss().url
          }
        }
      }
    }

    "some other properties is claimed and the previous is a gain" should {

      lazy val result = PersonalAndPreviousDetailsConstructor.previousGainsOrLossAnswerRow(
        OtherPropertiesModel(YesNoKeys.yes),
        Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.gain)))

      "return some details for the PreviousLossOrGains" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.previousLossOrGain}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe s"${KeystoreKeys.previousLossOrGain}-question"
        }
      }

      s"return a question of ${messages.PreviousLossOrGain.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.PreviousLossOrGain.question
        }
      }

      s"return the correct answer" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe PreviousGainOrLossKeys.gain
        }
      }

      s"return a link of ${routes.PreviousGainOrLossController.previousGainOrLoss().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.PreviousGainOrLossController.previousGainOrLoss().url
          }
        }
      }
    }

    "some other properties is claimed and the previous neither" should {

      lazy val result = PersonalAndPreviousDetailsConstructor.previousGainsOrLossAnswerRow(
        OtherPropertiesModel(YesNoKeys.yes),
        Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.neither)))

      "return some details for the PreviousLossOrGains" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.previousLossOrGain}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe s"${KeystoreKeys.previousLossOrGain}-question"
        }
      }

      s"return a question of ${messages.PreviousLossOrGain.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.PreviousLossOrGain.question
        }
      }

      s"return the correct answer" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe messages.PreviousLossOrGain.neither
        }
      }

      s"return a link of ${routes.PreviousGainOrLossController.previousGainOrLoss().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.PreviousGainOrLossController.previousGainOrLoss().url
          }
        }
      }
    }

    "a no other properties have been claimed" should {

      lazy val result = PersonalAndPreviousDetailsConstructor.previousGainsOrLossAnswerRow(
        OtherPropertiesModel(YesNoKeys.no),
        Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.loss)))

      "return some details for the PreviousGainsOrLosses" in {
        result shouldEqual None
      }
    }
  }

  "calling .howMuchLossAnswerRow" when {

    "some other properties is claimed and the previous loss or gain is a loss" should {

      lazy val result = PersonalAndPreviousDetailsConstructor.howMuchLossAnswerRow(
        OtherPropertiesModel(YesNoKeys.yes),
        Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.loss)),
        Some(HowMuchLossModel(2000)))

      "return some details for the HowMuchLoss" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.howMuchLoss}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe s"${KeystoreKeys.howMuchLoss}-question"
        }
      }

      s"return a question of ${messages.HowMuchLoss.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.HowMuchLoss.question
        }
      }

      s"return the correct answer" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe 2000
        }
      }

      s"return a link of ${routes.HowMuchLossController.howMuchLoss().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.HowMuchLossController.howMuchLoss().url
          }
        }
      }
    }

    "a no other properties have been claimed" should {

      lazy val result = PersonalAndPreviousDetailsConstructor.howMuchLossAnswerRow(
        OtherPropertiesModel(YesNoKeys.no),
        Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.loss)),
        Some(HowMuchLossModel(2000)))

      "return some details for the PreviousGainsOrLosses" in {
        result shouldEqual None
      }
    }

    "other properties have been claimed but the previous gain or loss was a gain" should {

      lazy val result = PersonalAndPreviousDetailsConstructor.howMuchLossAnswerRow(
        OtherPropertiesModel(YesNoKeys.yes),
        Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.gain)),
        Some(HowMuchLossModel(2000)))

      "return some details for the PreviousGainsOrLosses" in {
        result shouldEqual None
      }
    }
  }

  "calling .howMuchGainAnswerRow" when {

    "some other properties is claimed and the previous loss or gain is a gain" should {

      lazy val result = PersonalAndPreviousDetailsConstructor.howMuchGainAnswerRow(
        OtherPropertiesModel(YesNoKeys.yes),
        Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.gain)),
        Some(HowMuchGainModel(2000)))

      "return some details for the HowMuchLoss" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.howMuchGain}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe s"${KeystoreKeys.howMuchGain}-question"
        }
      }

      s"return a question of ${messages.HowMuchGain.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.HowMuchGain.question
        }
      }

      s"return the correct answer" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe 2000
        }
      }

      s"return a link of ${routes.HowMuchGainController.howMuchGain().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.HowMuchGainController.howMuchGain().url
          }
        }
      }
    }

    "a no other properties have been claimed" should {

      lazy val result = PersonalAndPreviousDetailsConstructor.howMuchGainAnswerRow(
        OtherPropertiesModel(YesNoKeys.no),
        Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.loss)),
        Some(HowMuchGainModel(2000)))

      "return some details for the PreviousGainsOrLosses" in {
        result shouldEqual None
      }
    }

    "other properties have been claimed but the previous gain or loss was a loss" should {

      lazy val result = PersonalAndPreviousDetailsConstructor.howMuchGainAnswerRow(
        OtherPropertiesModel(YesNoKeys.yes),
        Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.loss)),
        Some(HowMuchGainModel(2000)))

      "return some details for the PreviousGainsOrLosses" in {
        result shouldEqual None
      }
    }
  }

  "calling .annualExemptAmountAnswerRow" when {

    "some other properties is claimed and the previous loss or gain is a gain of 0" should {

      lazy val result = PersonalAndPreviousDetailsConstructor.annualExemptAmountAnswerRow(
        OtherPropertiesModel(YesNoKeys.yes),
        Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.gain)),
        Some(HowMuchLossModel(100)),
        Some(HowMuchGainModel(0.0)),
        Some(AnnualExemptAmountModel(1000)))

      "return some details for the AnnualExemptAmount" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.annualExemptAmount}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe s"${KeystoreKeys.annualExemptAmount}-question"
        }
      }

      s"return a question of ${messages.AnnualExemptAmount.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.AnnualExemptAmount.question
        }
      }

      s"return the correct answer" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe 1000
        }
      }

      s"return a link of ${routes.AnnualExemptAmountController.annualExemptAmount().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.AnnualExemptAmountController.annualExemptAmount().url
          }
        }
      }
    }

    "some other properties is claimed and the previous loss or gain is a loss of 0" should {

      lazy val result = PersonalAndPreviousDetailsConstructor.annualExemptAmountAnswerRow(
        OtherPropertiesModel(YesNoKeys.yes),
        Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.loss)),
        Some(HowMuchLossModel(0.0)),
        None,
        Some(AnnualExemptAmountModel(1000)))

      "return some details for the AnnualExemptAmount" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.annualExemptAmount}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe s"${KeystoreKeys.annualExemptAmount}-question"
        }
      }

      s"return a question of ${messages.AnnualExemptAmount.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.AnnualExemptAmount.question
        }
      }

      s"return the correct answer" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe 1000
        }
      }

      s"return a link of ${routes.AnnualExemptAmountController.annualExemptAmount().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.AnnualExemptAmountController.annualExemptAmount().url
          }
        }
      }
    }

    "some other properties is claimed and the previous loss or gain is neither a loss or gain" should {

      lazy val result = PersonalAndPreviousDetailsConstructor.annualExemptAmountAnswerRow(
        OtherPropertiesModel(YesNoKeys.yes),
        Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.neither)),
        None,
        None,
        Some(AnnualExemptAmountModel(1000)))

      "return some details for the AnnualExemptAmount" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.annualExemptAmount}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe s"${KeystoreKeys.annualExemptAmount}-question"
        }
      }

      s"return a question of ${messages.AnnualExemptAmount.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.AnnualExemptAmount.question
        }
      }

      s"return the correct answer" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe 1000
        }
      }

      s"return a link of ${routes.AnnualExemptAmountController.annualExemptAmount().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.AnnualExemptAmountController.annualExemptAmount().url
          }
        }
      }
    }

    "other properties have not been claimed" should {

      lazy val result = PersonalAndPreviousDetailsConstructor.annualExemptAmountAnswerRow(
        OtherPropertiesModel(YesNoKeys.no),
        Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.neither)),
        None,
        None,
        Some(AnnualExemptAmountModel(1000)))

      "return some details for the AnnualExemptAmount" in {
        result shouldEqual None
      }
    }
  }

  "calling .broughtForwardLossesAnswerRow" when {

    "some broughtForwardLosses are claimed" should {

      lazy val result = PersonalAndPreviousDetailsConstructor.broughtForwardLossesAnswerRow(BroughtForwardLossesModel(isClaiming = true, None))

      "return some details for the broughtForwardLosses" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.broughtForwardLosses}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe s"${KeystoreKeys.broughtForwardLosses}-question"
        }
      }

      s"return a question of ${messages.BroughtForwardLosses.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.BroughtForwardLosses.question
        }
      }

      s"return the correct answer" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe YesNoKeys.yes
        }
      }

      s"return a link of ${routes.BroughtForwardLossesController.broughtForwardLosses().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.BroughtForwardLossesController.broughtForwardLosses().url
          }
        }
      }
    }

    "no broughtForwardLosses are claimed" should {

      lazy val result = PersonalAndPreviousDetailsConstructor.broughtForwardLossesAnswerRow(BroughtForwardLossesModel(isClaiming = false, None))

      "return some details for the broughtForwardLosses" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.broughtForwardLosses}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe s"${KeystoreKeys.broughtForwardLosses}-question"
        }
      }

      s"return a question of ${messages.BroughtForwardLosses.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.BroughtForwardLosses.question
        }
      }

      s"return the correct answer" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe YesNoKeys.no
        }
      }

      s"return a link of ${routes.BroughtForwardLossesController.broughtForwardLosses().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.BroughtForwardLossesController.broughtForwardLosses().url
          }
        }
      }
    }
  }

  "calling .broughtForwardLossesValueAnswerRow" when {

    "some broughtForwardLosses are claimed" should {

      lazy val result = PersonalAndPreviousDetailsConstructor.broughtForwardLossesValueAnswerRow(BroughtForwardLossesModel(isClaiming = true, Some(200)))

      "return some details for the broughtForwardLosses" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.broughtForwardLosses}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe s"${KeystoreKeys.broughtForwardLosses}-value-question"
        }
      }

      s"return a question of ${messages.BroughtForwardLosses.inputQuestion}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.BroughtForwardLosses.inputQuestion
        }
      }

      s"return the correct answer" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe 200
        }
      }

      s"return a link of ${routes.BroughtForwardLossesController.broughtForwardLosses().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.BroughtForwardLossesController.broughtForwardLosses().url
          }
        }
      }
    }

    "no brought forward losses are claimed" should {

      lazy val result = PersonalAndPreviousDetailsConstructor.broughtForwardLossesValueAnswerRow(BroughtForwardLossesModel(isClaiming = false, Some(200)))

      "return some details for the AnnualExemptAmount" in {
        result shouldEqual None
      }
    }
  }
}
