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

import helpers.AssertHelpers
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.{NonResident => messages}
import models.nonresident.{CalculationResultsWithTaxOwedModel, QuestionAnswerModel, TotalTaxOwedModel}

class CalculationDetailsWithAllAnswersConstructorSpec extends UnitSpec with WithFakeApplication with AssertHelpers {

  private def assertExpectedResult[T](option: Option[T])(test: T => Unit) = assertOption("expected option is None")(option)(test)

  "Calling buildSection" should {
    val model = CalculationResultsWithTaxOwedModel(
      TotalTaxOwedModel(
        1000, 10000, 10, None, None, 12000, -10000, Some(100), Some(1), Some(50), Some(25), 5000, Some(12)
      ), None, None
    )

    lazy val result = CalculationDetailsWithAllAnswersConstructor.buildSection(model, "flat", "2016/17")

    "return a sequence with the election details row" in {
      result.contains(CalculationDetailsConstructor.calculationElection("flat").get)
    }

    "return a sequence containing the total gain row" in {
      result.contains(CalculationDetailsConstructor.totalGain(12000).get)
    }

    "return a sequence containing the prr used row" in {
      result.contains(CalculationDetailsWithPRRConstructor.prrUsedDetails(100).get)
    }

    "return a sequence containing the allowable losses used row" in {
      result.contains(CalculationDetailsWithAllAnswersConstructor.allowableLossesUsedRow(Some(50), "2016/17").get)
    }

    "return a sequence containing the aea used row" in {
      result.contains(CalculationDetailsWithAllAnswersConstructor.aeaUsedRow(Some(25)).get)
    }

    "return a sequence containing the aea remaining row" in {
      result.contains(CalculationDetailsWithAllAnswersConstructor.aeaRemainingRow(5000).get)
    }

    "return a sequence containing the brought forward losses row" in {
      result.contains(CalculationDetailsWithAllAnswersConstructor.broughtForwardLossesUsedRow(Some(12), "2016/17").get)
    }

    "return a sequence containing the tax rates row" in {
      result.contains(CalculationDetailsWithAllAnswersConstructor.taxRatesRow(10000, 10, None, None).get)
    }
  }

  "Calling allowableLossesUsed" when {

    "an allowable loss has been used" should {
      lazy val result = CalculationDetailsWithAllAnswersConstructor.allowableLossesUsedRow(Some(1000), "2016/17")

      "have a question answer model" in {
        result.isDefined shouldBe true
      }

      s"have a question of ${messages.Summary.usedAllowableLosses("2016/17")}" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.question shouldBe messages.Summary.usedAllowableLosses("2016/17"))
      }

      "have an id of calcDetails:allowableLossesUsed" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.id shouldBe "calcDetails:allowableLossesUsed")
      }

      "have a value of 1000" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.data shouldBe 1000)
      }

      "have no link" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.link shouldBe None)
      }
    }

    "an allowable loss of 0 has been used" should {
      lazy val result = CalculationDetailsWithAllAnswersConstructor.allowableLossesUsedRow(Some(0), "2016/17")

      "return a None" in {
        result.isDefined shouldBe false
      }
    }

    "a None is returned for allowable losses used" should {
      lazy val result = CalculationDetailsWithAllAnswersConstructor.allowableLossesUsedRow(None, "2016/17")

      "return a None" in {
        result.isDefined shouldBe false
      }
    }
  }

  "Calling aeaUsedRow" when {

    "an aea has been used" should {
      lazy val result = CalculationDetailsWithAllAnswersConstructor.aeaUsedRow(Some(1000))

      "have a question answer model" in {
        result.isDefined shouldBe true
      }

      s"have a question of ${messages.Summary.usedAEA}" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.question shouldBe messages.Summary.usedAEA)
      }

      "have an id of calcDetails:allowableLossesUsed" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.id shouldBe "calcDetails:annualExemptAmountUsed")
      }

      "have a value of 1000" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.data shouldBe 1000)
      }

      "have no link" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.link shouldBe None)
      }
    }

    "an aea of 0 has been used" should {
      lazy val result = CalculationDetailsWithAllAnswersConstructor.aeaUsedRow(Some(0))

      "return a None" in {
        result.isDefined shouldBe false
      }
    }

    "a None is returned for aea used" should {
      lazy val result = CalculationDetailsWithAllAnswersConstructor.aeaUsedRow(None)

      "return a None" in {
        result.isDefined shouldBe false
      }
    }
  }

  "Calling aeaRemainingRow" when {

    "an aea amount is left over" should {
      lazy val result = CalculationDetailsWithAllAnswersConstructor.aeaRemainingRow(1000)

      "have a question answer model" in {
        result.isDefined shouldBe true
      }

      s"have a question of ${messages.Summary.remainingAEA}" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.question shouldBe messages.Summary.remainingAEA)
      }

      "have an id of calcDetails:annualExemptAmountRemaining" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.id shouldBe "calcDetails:annualExemptAmountRemaining")
      }

      "have a value of 1000" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.data shouldBe 1000)
      }

      "have no link" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.link shouldBe None)
      }
    }
  }

  "Calling broughtForwardLossesRemainingRow" when {

    "a brought forward loss has been used" should {
      lazy val result = CalculationDetailsWithAllAnswersConstructor.broughtForwardLossesUsedRow(Some(1000), "2016/17")

      "have a question answer model" in {
        result.isDefined shouldBe true
      }

      s"have a question of ${messages.Summary.usedBroughtForwardLosses("2016/17")}" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.question shouldBe messages.Summary.usedBroughtForwardLosses("2016/17"))
      }

      "have an id of calcDetails:broughtForwardLossesUsed" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.id shouldBe "calcDetails:broughtForwardLossesUsed")
      }

      "have a value of 1000" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.data shouldBe 1000)
      }

      "have no link" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.link shouldBe None)
      }
    }

    "a brought forward loss of 0 has been used" should {
      lazy val result = CalculationDetailsWithAllAnswersConstructor.broughtForwardLossesUsedRow(Some(0), "2016/17")

      "return a None" in {
        result.isDefined shouldBe false
      }
    }

    "a None is returned for brought forward losses used" should {
      lazy val result = CalculationDetailsWithAllAnswersConstructor.broughtForwardLossesUsedRow(None, "2016/17")

      "return a None" in {
        result.isDefined shouldBe false
      }
    }
  }

  "Calling lossesRemainingRow" when {

    "a loss is remaining" should {
      lazy val result = CalculationDetailsWithAllAnswersConstructor.lossesRemainingRow(-1000)

      "have a question answer model" in {
        result.isDefined shouldBe true
      }

      s"have a question of ${messages.Summary.lossesRemaining}" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.question shouldBe messages.Summary.lossesRemaining)
      }

      "have an id of calcDetails:lossesCarriedForward" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.id shouldBe "calcDetails:lossesRemaining")
      }

      "have a value of 1000" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.data shouldBe 1000)
      }

      "have no link" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.link shouldBe None)
      }
    }

    "a zero taxable gain is returned" should {
      lazy val result = CalculationDetailsWithAllAnswersConstructor.lossesRemainingRow(0)

      "return a None" in {
        result.isDefined shouldBe false
      }
    }

    "a positive taxable gain is returned" should {
      lazy val result = CalculationDetailsWithAllAnswersConstructor.lossesRemainingRow(1000)

      "return a None" in {
        result.isDefined shouldBe false
      }
    }
  }

  "Calling taxRatesRow" when {

    "there is no positive taxable gain" should {
      lazy val result = CalculationDetailsWithAllAnswersConstructor.taxRatesRow(0, 10, None, None)

      "return a None" in {
        result.isDefined shouldBe false
      }
    }

    "there is no additional rate" should {
      lazy val result = CalculationDetailsWithAllAnswersConstructor.taxRatesRow(1000, 10, None, None)

      "have a question answer model" in {
        result.isDefined shouldBe true
      }

      s"have a question of ${messages.Summary.taxRate}" in {
        assertExpectedResult[QuestionAnswerModel[Any]](result)(_.question shouldBe messages.Summary.taxRate)
      }

      "have an id of calcDetails:taxRate" in {
        assertExpectedResult[QuestionAnswerModel[Any]](result)(_.id shouldBe "calcDetails:taxRate")
      }

      "have a value of 1000" in {
        assertExpectedResult[QuestionAnswerModel[Any]](result)(_.data shouldBe "£1,000.00 at 10%")
      }

      "have no link" in {
        assertExpectedResult[QuestionAnswerModel[Any]](result)(_.link shouldBe None)
      }
    }

    "there is an additional rate" should {
      lazy val result = CalculationDetailsWithAllAnswersConstructor.taxRatesRow(1000, 10, Some(2000), Some(20))

      "have a question answer model" in {
        result.isDefined shouldBe true
      }

      s"have a question of ${messages.Summary.taxRate}" in {
        assertExpectedResult[QuestionAnswerModel[Any]](result)(_.question shouldBe messages.Summary.taxRate)
      }

      "have an id of calcDetails:taxRate" in {
        assertExpectedResult[QuestionAnswerModel[Any]](result)(_.id shouldBe "calcDetails:taxRate")
      }

      "have a tuple containing all values" in {
        assertExpectedResult[QuestionAnswerModel[Any]](result)(_.data shouldBe ((1000, 10, 2000, 20)))
      }

      "have no link" in {
        assertExpectedResult[QuestionAnswerModel[Any]](result)(_.link shouldBe None)
      }
    }

    "there is only an additional rate" should {
      lazy val result = CalculationDetailsWithAllAnswersConstructor.taxRatesRow(0, 0, Some(2000), Some(20))

      "have a question answer model" in {
        result.isDefined shouldBe true
      }

      s"have a question of ${messages.Summary.taxRate}" in {
        assertExpectedResult[QuestionAnswerModel[Any]](result)(_.question shouldBe messages.Summary.taxRate)
      }

      "have an id of calcDetails:taxRate" in {
        assertExpectedResult[QuestionAnswerModel[Any]](result)(_.id shouldBe "calcDetails:taxRate")
      }

      "have a tuple containing the upper tax rate" in {
        assertExpectedResult[QuestionAnswerModel[Any]](result)(_.data shouldBe "£2,000.00 at 20%")
      }

      "have no link" in {
        assertExpectedResult[QuestionAnswerModel[Any]](result)(_.link shouldBe None)
      }
    }
  }

  "Calling otherReliefsUsedRow" should {

    "other reliefs have been used" should {
      lazy val result = CalculationDetailsWithAllAnswersConstructor.otherReliefsUsedRow(Some(1000))

      "have a question answer model" in {
        result.isDefined shouldBe true
      }

      s"have a question of ${messages.Summary.otherReliefsUsed}" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.question shouldBe messages.Summary.otherReliefsUsed)
      }

      "have an id of calcDetails:otherReliefsUsed" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.id shouldBe "calcDetails:otherReliefsUsed")
      }

      "have a value of 1000" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.data shouldBe 1000)
      }

      "have no link" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.link shouldBe None)
      }
    }

    "an aea of 0 has been used" should {
      lazy val result = CalculationDetailsWithAllAnswersConstructor.otherReliefsUsedRow(Some(0))

      "return a None" in {
        result.isDefined shouldBe false
      }
    }

    "a None is returned for aea used" should {
      lazy val result = CalculationDetailsWithAllAnswersConstructor.otherReliefsUsedRow(None)

      "return a None" in {
        result.isDefined shouldBe false
      }
    }
  }
}
