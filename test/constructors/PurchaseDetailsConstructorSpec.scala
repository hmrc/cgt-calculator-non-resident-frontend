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

import java.time.LocalDate

import assets.MessageLookup.{NonResident => messages}
import models._
import helpers.AssertHelpers
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class PurchaseDetailsConstructorSpec extends UnitSpec with WithFakeApplication with AssertHelpers {

  val totalGainGiven = TotalGainAnswersModel(
    DisposalDateModel(10, 10, 2010),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(150000),
    DisposalCostsModel(600),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(300000),
    AcquisitionCostsModel(2500),
    AcquisitionDateModel(1, 4, 2013),
    Some(RebasedValueModel(None)),
    None,
    ImprovementsModel("No", None, None),
    None
  )

  val totalGainInherited = TotalGainAnswersModel(
    DisposalDateModel(10, 10, 2010),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(150000),
    DisposalCostsModel(600),
    Some(HowBecameOwnerModel("Inherited")),
    None,
    AcquisitionValueModel(300000),
    AcquisitionCostsModel(2500),
    AcquisitionDateModel(1, 4, 2013),
    None,
    None,
    ImprovementsModel("No", None, None),
    None
  )

  val totalGainSold = TotalGainAnswersModel(
    DisposalDateModel(10, 10, 2018),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(false)),
    DisposalValueModel(90000),
    DisposalCostsModel(0),
    Some(HowBecameOwnerModel("Bought")),
    Some(BoughtForLessModel(false)),
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    AcquisitionDateModel(1, 4, 2013),
    Some(RebasedValueModel(Some(7500))),
    Some(RebasedCostsModel("Yes", Some(150))),
    ImprovementsModel("Yes", Some(50), Some(25)),
    None
  )

  val totalGainForLess = TotalGainAnswersModel(
    DisposalDateModel(10, 10, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(true)),
    DisposalValueModel(10000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Bought")),
    Some(BoughtForLessModel(true)),
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    AcquisitionDateModel(1, 4, 2013),
    Some(RebasedValueModel(Some(7500))),
    Some(RebasedCostsModel("Yes", Some(150))),
    ImprovementsModel("Yes", Some(50), Some(25)),
    None
  )

  private def assertExpectedResult[T](option: Option[T])(test: T => Unit) = assertOption("expected option is None")(option)(test)

  "Calling purchaseDetailsRow" when {

    "using the totalGainForLess model" should {
      lazy val result = PurchaseDetailsConstructor.getPurchaseDetailsSection(totalGainForLess)

      "will return a Sequence with size 9" in {
        result.size shouldBe 9
      }

      "return a Sequence that will contain an acquisitionDate data item" in {
        result.contains(PurchaseDetailsConstructor.acquisitionDateRow(totalGainForLess).get) shouldBe true
      }

      "return a Sequence that will contain an acquisitionCost data item" in {
        result.contains(PurchaseDetailsConstructor.acquisitionCostsRow(totalGainForLess).get) shouldBe true
      }

      "return a Sequence that will contain an acquisitionValue data item" in {
        result.contains(PurchaseDetailsConstructor.acquisitionValueRow(totalGainForLess, useWorthBeforeLegislationStart = false).get) shouldBe true
      }

      "return a Sequence that will contain a howBecameOwner data item" in {
        result.contains(PurchaseDetailsConstructor.howBecameOwnerRow(totalGainForLess).get) shouldBe true
      }

      "return a Sequence that will contain a boughtForLess data item" in {
        result.contains(PurchaseDetailsConstructor.boughtForLessRow(totalGainForLess).get) shouldBe true
      }

      "return a Sequence that will contain a rebasedValue data item" in {
        result.contains(PurchaseDetailsConstructor.rebasedValueRow(totalGainForLess.rebasedValueModel, useRebasedValues = true).get) shouldBe true
      }

      "return a Sequence that will contain a rebasedCostsQuestion data item" in {
        result.contains(PurchaseDetailsConstructor.rebasedCostsQuestionRow(totalGainForLess.rebasedCostsModel, useRebasedValues = true).get) shouldBe true
      }

      "return a Sequence that will contain a rebasedCosts data item" in {
        result.contains(PurchaseDetailsConstructor.rebasedCostsRow(totalGainForLess.rebasedCostsModel, useRebasedValues = true).get) shouldBe true
      }
    }

    "using the totalGainGiven model" should {
      lazy val result = PurchaseDetailsConstructor.getPurchaseDetailsSection(totalGainGiven)

      "will return a Sequence with size 4" in {
        result.size shouldBe 4
      }

      "return a Sequence that will contain an acquisitionCost data item" in {
        result.contains(PurchaseDetailsConstructor.acquisitionCostsRow(totalGainGiven).get) shouldBe true
      }

      "return a Sequence that will contain an acquisitionValue data item" in {
        result.contains(PurchaseDetailsConstructor.acquisitionValueRow(totalGainGiven, useWorthBeforeLegislationStart = false).get) shouldBe true
      }

      "return a Sequence that will contain a howBecameOwner data item" in {
        result.contains(PurchaseDetailsConstructor.howBecameOwnerRow(totalGainGiven).get) shouldBe true
      }
    }
  }

  "Calling .acquisitionDateRow" when {

    "no acquisition date is given" should {
      lazy val result = PurchaseDetailsConstructor.acquisitionDateRow(totalGainGiven)

      "return a None" in {
        result shouldBe None
      }
    }

    "an acquisition date is given" should {
      lazy val result = PurchaseDetailsConstructor.acquisitionDateRow(totalGainForLess)

      "return Some value" in {
        result.isDefined shouldBe true
      }

      "have an id of nr:acquisitionDate" in {
        assertExpectedResult[QuestionAnswerModel[LocalDate]](result)(_.id shouldBe "nr:acquisitionDate")
      }

      "have the date for 2013-04-01" in {
        assertExpectedResult[QuestionAnswerModel[LocalDate]](result)(_.data shouldBe LocalDate.parse("2013-04-01"))
      }

      "have the question for acquisition date entry" in {
        assertExpectedResult[QuestionAnswerModel[LocalDate]](result)(_.question shouldBe messages.AcquisitionDate.questionTwo)
      }

      "have a link to the acquisition date page" in {
        assertExpectedResult[QuestionAnswerModel[LocalDate]](result)(_.link shouldBe
          Some(controllers.routes.AcquisitionDateController.acquisitionDate().url))
      }
    }
  }

  "Calling .howBecameOwnerRow" when {

    "the property was received as a gift" should {
      lazy val result = PurchaseDetailsConstructor.howBecameOwnerRow(totalGainGiven).get

      "have an id of nr:howBecameOwner" in {
        result.id shouldBe "nr:howBecameOwner"
      }

      "have the value of Gifted" in {
        result.data shouldBe messages.HowBecameOwner.gifted
      }

      "have the question for how became owner" in {
        result.question shouldBe messages.HowBecameOwner.question
      }

      "have a link to the how became owner page" in {
        result.link shouldBe Some(controllers.routes.HowBecameOwnerController.howBecameOwner().url)
      }
    }

    "the property was bought" should {
      lazy val result = PurchaseDetailsConstructor.howBecameOwnerRow(totalGainSold).get

      "have the value of Bought" in {
        result.data shouldBe messages.HowBecameOwner.bought
      }
    }

    "the property was inherited" should {
      lazy val result = PurchaseDetailsConstructor.howBecameOwnerRow(totalGainInherited).get

      "have the value of Inherited" in {
        result.data shouldBe messages.HowBecameOwner.inherited
      }
    }
  }

  "Calling .boughtForLessRow" when {

    "the property was not bought" should {
      lazy val result = PurchaseDetailsConstructor.boughtForLessRow(totalGainGiven)

      "return a None" in {
        result shouldBe None
      }
    }

    "the property was bought" should {
      lazy val result = PurchaseDetailsConstructor.boughtForLessRow(totalGainSold)

      "return Some value" in {
        result.isDefined shouldBe true
      }

      "have an id of nr:boughtForLess" in {
        assertExpectedResult[QuestionAnswerModel[Boolean]](result)(_.id shouldBe "nr:boughtForLess")
      }

      "have a value of false" in {
        assertExpectedResult[QuestionAnswerModel[Boolean]](result)(_.data shouldBe false)
      }

      "have the question for bought for less" in {
        assertExpectedResult[QuestionAnswerModel[Boolean]](result)(_.question shouldBe messages.BoughtForLess.question)
      }

      "have a link to the bought for less page" in {
        assertExpectedResult[QuestionAnswerModel[Boolean]](result)(_.link
          shouldBe Some(controllers.routes.BoughtForLessController.boughtForLess().url))
      }
    }

    "the property was bought for less" should {
      lazy val result = PurchaseDetailsConstructor.boughtForLessRow(totalGainForLess)

      "return Some value" in {
        result.isDefined shouldBe true
      }

      "have a value of true" in {
        assertExpectedResult[QuestionAnswerModel[Boolean]](result)(_.data shouldBe true)
      }
    }
  }

  "Calling .acquisitionValueRow" when {

    "a value of 300000 is given" should {
      lazy val result = PurchaseDetailsConstructor.acquisitionValueRow(totalGainGiven, useWorthBeforeLegislationStart = false).get

      "have an id of nr:acquisitionValue" in {
        result.id shouldBe "nr:acquisitionValue"
      }

      "have the data for '300000'" in {
        result.data shouldBe 300000
      }

      "have the question for worth when gifted to" in {
        result.question shouldBe messages.WorthWhenGiftedTo.question
      }

      "have a link to the acquisition date page" in {
        result.link shouldBe Some(controllers.routes.AcquisitionValueController.acquisitionValue().url)
      }
    }

    "a value of 5000 is given" should {
      lazy val result = PurchaseDetailsConstructor.acquisitionValueRow(totalGainSold, useWorthBeforeLegislationStart = false).get

      "have the data for '5000'" in {
        result.data shouldBe 5000
      }
    }

    "a value for acquisition is given" should {
      lazy val result = PurchaseDetailsConstructor.acquisitionValueRow(totalGainSold, useWorthBeforeLegislationStart = false).get

      "have the question for acquisition value" in {
        result.question shouldBe messages.AcquisitionValue.question
      }
    }

    "a worth when inherited is given" should {
      lazy val result = PurchaseDetailsConstructor.acquisitionValueRow(totalGainInherited, useWorthBeforeLegislationStart = false).get

      "have the question for worth when inherited" in {
        result.question shouldBe messages.WorthWhenInherited.question
      }
    }

    "a worth before legislation start value is given" should {
      lazy val result = PurchaseDetailsConstructor.acquisitionValueRow(totalGainSold, useWorthBeforeLegislationStart = true).get

      "have the question for worth before legislation start" in {
        result.question shouldBe messages.WorthBeforeLegislationStart.question
      }
    }

    "a value for bought for less is given" should {
      lazy val result = PurchaseDetailsConstructor.acquisitionValueRow(totalGainForLess, useWorthBeforeLegislationStart = false).get

      "have the question for market value when bought for less" in {
        result.question shouldBe messages.WorthWhenBoughtForLess.question
      }
    }
  }

  "Calling .acquisitionCostsRow" when {

    "a value of 2500 is given" should {
      lazy val result = PurchaseDetailsConstructor.acquisitionCostsRow(totalGainGiven).get

      "have an id of nr:acquisitionCosts" in {
        result.id shouldBe "nr:acquisitionCosts"
      }

      "have the data for '2500'" in {
        result.data shouldBe 2500
      }

      "have the question for acquisition value" in {
        result.question shouldBe messages.AcquisitionCosts.question
      }

      "have a link to the acquisition date page" in {
        result.link shouldBe Some(controllers.routes.AcquisitionCostsController.acquisitionCosts().url)
      }
    }

    "a value of 200 is given" should {
      lazy val result = PurchaseDetailsConstructor.acquisitionCostsRow(totalGainSold).get

      "have the data for '200'" in {
        result.data shouldBe 200
      }
    }
  }

  "Calling .rebasedValueRow" when {

    "a value is applicable" should {
      lazy val result = PurchaseDetailsConstructor.rebasedValueRow(Some(RebasedValueModel(Some(10))), useRebasedValues = true)

      "return Some value" in {
        result.isDefined shouldBe true
      }

      "have an id of nr:rebasedValue" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.id shouldBe "nr:rebasedValue")
      }

      "have a value of 10" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.data shouldBe 10)
      }

      "have the question for rebased value" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.question shouldBe messages.RebasedValue.question)
      }

      "have a link to the rebased value page" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.link
          shouldBe Some(controllers.routes.RebasedValueController.rebasedValue().url))
      }
    }

    "a value is provided but not applicable" should {
      lazy val result = PurchaseDetailsConstructor.rebasedValueRow(Some(RebasedValueModel(Some(10))), useRebasedValues = false)

      "return a None" in {
        result shouldBe None
      }
    }

    "a value is not provided or applicable" should {
      lazy val result = PurchaseDetailsConstructor.rebasedValueRow(Some(RebasedValueModel(None)), useRebasedValues = false)

      "return a None" in {
        result shouldBe None
      }
    }

    "a value is not found or applicable" should {
      lazy val result = PurchaseDetailsConstructor.rebasedValueRow(None, useRebasedValues = false)

      "return a None" in {
        result shouldBe None
      }
    }
  }

  "Calling .rebasedCostsQuestionRow" when {

    "a value is applicable" should {
      lazy val result = PurchaseDetailsConstructor.rebasedCostsQuestionRow(Some(RebasedCostsModel("Yes", None)), useRebasedValues = true)

      "return Some value" in {
        result.isDefined shouldBe true
      }

      "have an id of nr:rebasedCosts-question" in {
        assertExpectedResult[QuestionAnswerModel[String]](result)(_.id shouldBe "nr:rebasedCosts-question")
      }

      "have a value of Yes" in {
        assertExpectedResult[QuestionAnswerModel[String]](result)(_.data shouldBe "Yes")
      }

      "have the question for rebased costs" in {
        assertExpectedResult[QuestionAnswerModel[String]](result)(_.question shouldBe messages.RebasedCosts.question)
      }

      "have a link to the rebased costs page" in {
        assertExpectedResult[QuestionAnswerModel[String]](result)(_.link
          shouldBe Some(controllers.routes.RebasedCostsController.rebasedCosts().url))
      }
    }

    "a value is not applicable" should {
      lazy val result = PurchaseDetailsConstructor.rebasedCostsQuestionRow(Some(RebasedCostsModel("No", None)), useRebasedValues = false)

      "should return a None" in {
        result shouldBe None
      }
    }

    "a value is not found" should {
      lazy val result = PurchaseDetailsConstructor.rebasedCostsQuestionRow(None, useRebasedValues = false)

      "should return a None" in {
        result shouldBe None
      }
    }
  }

  "Calling .rebasedCostsRow" when {

    "an applicable value is provided" should {
      lazy val result = PurchaseDetailsConstructor.rebasedCostsRow(Some(RebasedCostsModel("Yes", Some(1))), useRebasedValues = true)

      "return Some value" in {
        result.isDefined shouldBe true
      }

      "have an id of nr:rebasedCosts" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.id shouldBe "nr:rebasedCosts")
      }

      "have a value of Yes" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.data shouldBe 1)
      }

      "have the question for rebased costs" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.question shouldBe messages.RebasedCosts.inputQuestion)
      }

      "have a link to the rebased costs page" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.link
          shouldBe Some(controllers.routes.RebasedCostsController.rebasedCosts().url))
      }
    }

    "an applicable value is not provided" should {
      lazy val result = PurchaseDetailsConstructor.rebasedCostsRow(Some(RebasedCostsModel("No", None)), useRebasedValues = true)

      "return a None" in {
        result shouldBe None
      }
    }

    "an applicable value is not found" should {
      lazy val result = PurchaseDetailsConstructor.rebasedCostsRow(None, useRebasedValues = true)

      "return a None" in {
        result shouldBe None
      }
    }

    "no applicable value is required" should {
      lazy val result = PurchaseDetailsConstructor.rebasedCostsRow(Some(RebasedCostsModel("Yes", Some(1))), useRebasedValues = false)

      "return a None" in {
        result shouldBe None
      }
    }
  }
}
