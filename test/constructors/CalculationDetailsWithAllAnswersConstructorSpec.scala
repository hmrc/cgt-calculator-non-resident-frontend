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

import helpers.AssertHelpers
import assets.MessageLookup.{NonResident => messages}
import common.nonresident.Flat
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.helpers.FakeRequestHelper
import models.{CalculationResultsWithTaxOwedModel, QuestionAnswerModel, TotalTaxOwedModel}
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.MessagesProvider
import play.api.mvc.MessagesControllerComponents

class CalculationDetailsWithAllAnswersConstructorSpec extends CommonPlaySpec with WithCommonFakeApplication with AssertHelpers with MockitoSugar with FakeRequestHelper {

  implicit val mockMessagesProvider = mock[MessagesProvider]
  private def assertExpectedResult[T](option: Option[T])(test: T => Unit) = assertOption("expected option is None")(option)(test)
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val calcDetails = new CalculationDetailsConstructor
  val calcDetailWithPRR =  new CalculationDetailsWithPRRConstructor
  val calcDetailsWithAllAnswers = new CalculationDetailsWithAllAnswersConstructor(calcDetails, calcDetailWithPRR)

  "Calling buildSection" should {
    val model = CalculationResultsWithTaxOwedModel(
      TotalTaxOwedModel(
        1000,
        10000,
        10,
        None,
        None,
        12000,
        -10000,
        Some(100),
        Some(1),
        Some(50),
        Some(25),
        5000,
        Some(12),
        None,
        None,
        None,
        None,
        None,
        None
      ), None, None
    )

    lazy val result = calcDetailsWithAllAnswers.buildSection(model, Flat, "2016/17")

    "return a sequence with the election details row" in {
      result.contains(calcDetails.calculationElection(Flat).get)
    }

    "return a sequence containing the total gain row" in {
      result.contains(calcDetails.totalGain(12000).get)
    }

    "return a sequence containing the prr used row" in {
      result.contains(calcDetailWithPRR.prrUsedDetails(100).get)
    }

    "return a sequence containing the allowable losses used row" in {
      result.contains(calcDetailsWithAllAnswers.allowableLossesUsedRow(Some(50), "2016/17").get)
    }

    "return a sequence containing the aea used row" in {
      result.contains(calcDetailsWithAllAnswers.aeaUsedRow(Some(25)).get)
    }

    "return a sequence containing the aea remaining row" in {
      result.contains(calcDetailsWithAllAnswers.aeaRemainingRow(5000).get)
    }

    "return a sequence containing the brought forward losses row" in {
      result.contains(calcDetailsWithAllAnswers.broughtForwardLossesUsedRow(Some(12), "2016/17").get)
    }

    "return a sequence containing the tax rates row" in {
      result.contains(calcDetailsWithAllAnswers.taxRatesRow(10000, 10, None, None).get)
    }
  }

  "Calling allowableLossesUsed" when {

    "an allowable loss has been used" should {
      lazy val result = calcDetailsWithAllAnswers.allowableLossesUsedRow(Some(1000), "2016/17")

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
      lazy val result = calcDetailsWithAllAnswers.allowableLossesUsedRow(Some(0), "2016/17")

      "return a None" in {
        result.isDefined shouldBe false
      }
    }

    "a None is returned for allowable losses used" should {
      lazy val result = calcDetailsWithAllAnswers.allowableLossesUsedRow(None, "2016/17")

      "return a None" in {
        result.isDefined shouldBe false
      }
    }
  }

  "Calling aeaUsedRow" when {

    "an aea has been used" should {
      lazy val result = calcDetailsWithAllAnswers.aeaUsedRow(Some(1000))

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
      lazy val result = calcDetailsWithAllAnswers.aeaUsedRow(Some(0))

      "return a None" in {
        result.isDefined shouldBe false
      }
    }

    "a None is returned for aea used" should {
      lazy val result = calcDetailsWithAllAnswers.aeaUsedRow(None)

      "return a None" in {
        result.isDefined shouldBe false
      }
    }
  }

  "Calling aeaRemainingRow" when {

    "an aea amount is left over" should {
      lazy val result = calcDetailsWithAllAnswers.aeaRemainingRow(1000)

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
      lazy val result = calcDetailsWithAllAnswers.broughtForwardLossesUsedRow(Some(1000), "2016/17")

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
      lazy val result = calcDetailsWithAllAnswers.broughtForwardLossesUsedRow(Some(0), "2016/17")

      "return a None" in {
        result.isDefined shouldBe false
      }
    }

    "a None is returned for brought forward losses used" should {
      lazy val result = calcDetailsWithAllAnswers.broughtForwardLossesUsedRow(None, "2016/17")

      "return a None" in {
        result.isDefined shouldBe false
      }
    }
  }

  "Calling lossesRemainingRow" when {

    "a loss is remaining" should {
      lazy val result = calcDetailsWithAllAnswers.lossesRemainingRow(-1000)

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
      lazy val result = calcDetailsWithAllAnswers.lossesRemainingRow(0)

      "return a None" in {
        result.isDefined shouldBe false
      }
    }

    "a positive taxable gain is returned" should {
      lazy val result = calcDetailsWithAllAnswers.lossesRemainingRow(1000)

      "return a None" in {
        result.isDefined shouldBe false
      }
    }
  }

  "Calling taxRatesRow" when {

    "there is no positive taxable gain" should {
      lazy val result = calcDetailsWithAllAnswers.taxRatesRow(0, 10, None, None)

      "return a None" in {
        result.isDefined shouldBe false
      }
    }

    "there is no additional rate" should {
      lazy val result = calcDetailsWithAllAnswers.taxRatesRow(1000, 10, None, None)

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
      lazy val result = calcDetailsWithAllAnswers.taxRatesRow(1000, 10, Some(2000), Some(20))

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
      lazy val result = calcDetailsWithAllAnswers.taxRatesRow(0, 0, Some(2000), Some(20))

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
      lazy val result = calcDetailsWithAllAnswers.otherReliefsUsedRow(Some(1000))

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
      lazy val result = calcDetailsWithAllAnswers.otherReliefsUsedRow(Some(0))

      "return a None" in {
        result.isDefined shouldBe false
      }
    }

    "a None is returned for aea used" should {
      lazy val result = calcDetailsWithAllAnswers.otherReliefsUsedRow(None)

      "return a None" in {
        result.isDefined shouldBe false
      }
    }
  }
}
