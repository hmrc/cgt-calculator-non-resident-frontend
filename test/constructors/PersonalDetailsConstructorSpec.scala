/*
 * Copyright 2019 HM Revenue & Customs
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
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.TestModels._
import common.nonresident.PreviousGainOrLossKeys
import controllers.helpers.FakeRequestHelper
import controllers.routes
import models._
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.MessagesProvider
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class PersonalDetailsConstructorSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {
  implicit val mockMessagesProvider = mock[MessagesProvider]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)


  val target = new PersonalDetailsConstructor

  "calling .getCurrentIncomeAnswer" when {

    "a current income greater than 0" should {

      lazy val result = target.getCurrentIncomeAnswer(CurrentIncomeModel(1000))

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

      lazy val result = target.getCurrentIncomeAnswer(CurrentIncomeModel(0))

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
          item.data shouldBe summaryFlatNoIncomeOtherPropNo.currentIncomeModel.currentIncome
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

  "calling .getPersonalAllowanceAnswer" when {

    "a personal allowance of greater than 0 is given" should {

      lazy val result = target.getPersonalAllowanceAnswer(Some(PersonalAllowanceModel(10000)),
        CurrentIncomeModel(1000))

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

      lazy val result = target.getPersonalAllowanceAnswer(Some(PersonalAllowanceModel(0)),
        CurrentIncomeModel(10000))

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

      lazy val result = target.getPersonalAllowanceAnswer(Some(PersonalAllowanceModel(100)),
        CurrentIncomeModel(0))

      "return a None" in {
        result shouldBe None
      }
    }
  }

  "calling .getOtherPropertiesAnswer" when {


    "a otherPropertiesAnswer of yes is given" should {

      lazy val result = target.getOtherPropertiesAnswer(OtherPropertiesModel("Yes"))

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

      lazy val result = target.getOtherPropertiesAnswer(OtherPropertiesModel("No"))

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
      lazy val result = target.previousGainOrLossAnswer(OtherPropertiesModel("Yes"),
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

      s"return an id of ${KeystoreKeys.previousLossOrGain}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.previousLossOrGain
        }
      }

      s"return a link of ${routes.PreviousGainOrLossController.previousGainOrLoss().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link shouldBe Some(routes.PreviousGainOrLossController.previousGainOrLoss().url)
        }
      }
    }

    "no other properties have been disposed" should {
      lazy val result = target.previousGainOrLossAnswer(OtherPropertiesModel("No"), None)

      "return a None" in {
        result shouldBe None
      }
    }
  }

  "Calling howMuchGainAnswer" when {

    "no other properties have been disposed" should {
      lazy val result = target.howMuchGainAnswer(OtherPropertiesModel("No"), None, None)

      "return a None" in {
        result shouldBe None
      }
    }

    "other properties have not made a gain" should {
      lazy val result = target.howMuchGainAnswer(OtherPropertiesModel("Yes"),
        Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.loss)), None)

      "return a None" in {
        result shouldBe None
      }
    }

    "other properties have made a gain" should {
      lazy val result = target.howMuchGainAnswer(OtherPropertiesModel("Yes"),
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
      lazy val result = target.howMuchLossAnswer(OtherPropertiesModel("No"), None, None)

      "return a None" in {
        result shouldBe None
      }
    }

    "other properties have not made a loss" should {
      lazy val result = target.howMuchLossAnswer(OtherPropertiesModel("Yes"),
        Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.gain)), None)

      "return a None" in {
        result shouldBe None
      }
    }

    "other properties have made a loss" should {
      lazy val result = target.howMuchLossAnswer(OtherPropertiesModel("Yes"),
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
      lazy val result = target.getAnnualExemptAmountAnswer(OtherPropertiesModel("Yes"),
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
      lazy val result = target.getAnnualExemptAmountAnswer(OtherPropertiesModel("Yes"),
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
      lazy val result = target.getAnnualExemptAmountAnswer(OtherPropertiesModel("Yes"),
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
      lazy val result = target.getAnnualExemptAmountAnswer(OtherPropertiesModel("Yes"), None, None, None, None)

      "return a None" in {
        result shouldBe None
      }
    }
  }

  "Calling .broughtForwardLossesQuestion" should {
    lazy val result = target.getBroughtForwardLossesQuestion(BroughtForwardLossesModel(false, None))

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
      lazy val result = target.getBroughtForwardLossesAnswer(BroughtForwardLossesModel(false, None))

      "return a None" in {
        result shouldBe None
      }
    }

    "an answer of yes is given" should {
      lazy val result = target.getBroughtForwardLossesAnswer(BroughtForwardLossesModel(true, Some(1000)))

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

  "Calling getPersonalDetailsSection" when {
    "there is no TotalPersonalDetailsCalculationModel" should {
      "return an empty list" in {
        target.getPersonalDetailsSection(None) shouldBe Seq.empty
      }
    }

    "there is TotalPersonalDetailsCalculationModel" should {
      "return a populated list of QuestionAnswerModels" in {
        val model = TotalPersonalDetailsCalculationModel(currentIncomeModel = CurrentIncomeModel(BigDecimal(100)),
          personalAllowanceModel = Some(PersonalAllowanceModel(BigDecimal(200))),
          otherPropertiesModel = OtherPropertiesModel(""),
          previousGainOrLoss = Some(PreviousLossOrGainModel("loss")),
          howMuchLossModel = Some(HowMuchLossModel(BigDecimal(300))),
          howMuchGainModel = Some(HowMuchGainModel(BigDecimal(400))),
          annualExemptAmountModel = Some(AnnualExemptAmountModel(BigDecimal(500))),
          broughtForwardLossesModel = BroughtForwardLossesModel(false, None))
        val result = target.getPersonalDetailsSection(Some(model))

        result.head shouldBe QuestionAnswerModel("nr:currentIncome", 100, "What was your total UK income in the tax year when you stopped owning the property?", Some("/calculate-your-capital-gains/non-resident/current-income"), None)
        result(1) shouldBe QuestionAnswerModel("nr:personalAllowance",200,"What was your UK Personal Allowance in the tax year when you stopped owning the property?",Some("/calculate-your-capital-gains/non-resident/personal-allowance"),None)
        result(2) shouldBe QuestionAnswerModel("nr:otherProperties","","Did you sell or give away other UK residential properties in the tax year when you stopped owning the property?",Some("/calculate-your-capital-gains/non-resident/other-properties"),None)
        result(3) shouldBe QuestionAnswerModel("nr:broughtForwardLosses-question",false,"Do you have losses you want to bring forward from previous tax years?",Some("/calculate-your-capital-gains/non-resident/brought-forward-losses"),None)
      }
    }
  }

  "Calling personal details section should invoke correct handlers" when {

    val model = Some(TotalPersonalDetailsCalculationModel(
      CurrentIncomeModel(20000),
      Some(PersonalAllowanceModel(0)),
      OtherPropertiesModel("Yes"),
      Some(PreviousLossOrGainModel("Neither")),
      None,
      None,
      Some(AnnualExemptAmountModel(0)),
      BroughtForwardLossesModel(isClaiming = false, None))
    )

    lazy val flattenedResult = new PersonalDetailsConstructor().getPersonalDetailsSection(model)

    "return get Personal Allowance Answer" in {
      val linksFound = flattenedResult.map(_.link.get)
      linksFound.isEmpty shouldBe false
    }
  }
}