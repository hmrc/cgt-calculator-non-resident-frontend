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

import assets.MessageLookup.NonResident.{Summary => messages}
import common.nonresident.CalculationType
import common.{KeystoreKeys, TestModels}
import controllers.nonresident.routes
import helpers.AssertHelpers
import models.nonresident.{CalculationResultModel, TotalGainResultsModel}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class CalculationDetailsConstructorSpec extends UnitSpec with WithFakeApplication with AssertHelpers {

  val target = CalculationDetailsConstructor

  private def assertExpectedResult[T](option: Option[T])(test: T => Unit) = assertOption("expected option is None")(option)(test)

  private def assertExpectedLink[T](option: Option[T])(test: T => Unit) = assertOption("expected link is None")(option)(test)

  "Calling buildSection" when {
    val calculation = TotalGainResultsModel(-1000, Some(2000), Some(0))
    "a loss has been made" should {

      lazy val result = target.buildSection(calculation, CalculationType.flat)

      "have a calc election question" in {
        result.exists(qa => qa.id == KeystoreKeys.calculationElection) shouldBe true
      }

      "not have a gain question" in {
        result.exists(qa => qa.id == "calcDetails:totalGain") shouldBe false
      }

      "have a loss question" in {
        result.exists(qa => qa.id == "calcDetails:totalLoss") shouldBe true
      }
    }

    "a gain has been made" should {
      val calculation = TotalGainResultsModel(-1000, Some(2000), Some(0))
      lazy val result = target.buildSection(calculation, CalculationType.timeApportioned)

      "have a calc election question" in {
        result.exists(qa => qa.id == KeystoreKeys.calculationElection) shouldBe true
      }

      "have a gain question" in {
        result.exists(qa => qa.id == "calcDetails:totalGain") shouldBe true
      }

      "not have a loss question" in {
        result.exists(qa => qa.id == "calcDetails:totalLoss") shouldBe false
      }
    }

    "a zero gain has been made" should {
      val calculation = TotalGainResultsModel(-1000, Some(2000), Some(0))
      lazy val result = target.buildSection(calculation, CalculationType.rebased)

      "have a calc election question" in {
        result.exists(qa => qa.id == KeystoreKeys.calculationElection) shouldBe true
      }

      "have a gain question" in {
        result.exists(qa => qa.id == "calcDetails:totalGain") shouldBe true
      }

      "not have a loss question" in {
        result.exists(qa => qa.id == "calcDetails:totalLoss") shouldBe false
      }
    }
  }

  "Calling calculationElection" when {

    "the calculation type is a flat calc" should {
      lazy val result = target.calculationElection(CalculationType.flat)

      "return some details for the calculation election" in {
        result should not be None
      }

      "return correct ID for the calculation election details" in {
        assertExpectedResult(result)(_.id shouldBe KeystoreKeys.calculationElection)
      }

      "return correct question for the calculation election details" in {
        assertExpectedResult(result)(_.question shouldBe messages.calculationElection)
      }

      "return correct answer for the calculation election details" in {
        assertExpectedResult(result)(_.data shouldBe messages.flatCalculation)
      }

      "return a link for the calculation election details" in {
        assertExpectedResult(result)(_.link should not be None)
      }

      "return correct link for the calculation election details" in {
        assertExpectedResult(result) { item =>
          assertExpectedLink(item.link) { link =>
            link shouldBe routes.CalculationElectionController.calculationElection().url
          }
        }
      }
    }

    "the calculation type is a rebased calc" should {
      lazy val result = target.calculationElection(CalculationType.rebased)

      "return some details for the calculation election" in {
        result should not be None
      }

      "return correct ID for the calculation election details" in {
        assertExpectedResult(result)(_.id shouldBe KeystoreKeys.calculationElection)
      }

      "return correct question for the calculation election details" in {
        assertExpectedResult(result)(_.question shouldBe messages.calculationElection)
      }

      "return correct answer for the calculation election details" in {
        assertExpectedResult(result)(_.data shouldBe messages.rebasedCalculation)
      }

      "return a link for the calculation election details" in {
        assertExpectedResult(result)(_.link should not be None)
      }

      "return correct link for the calculation election details" in {
        assertExpectedResult(result) { item =>
          assertExpectedLink(item.link) { link =>
            link shouldBe routes.CalculationElectionController.calculationElection().url
          }
        }
      }
    }

    "the calculation type is a time apportioned calc" should {
      lazy val result = target.calculationElection(CalculationType.timeApportioned)

      "return some details for the calculation election" in {
        result should not be None
      }

      "return correct ID for the calculation election details" in {
        assertExpectedResult(result)(_.id shouldBe KeystoreKeys.calculationElection)
      }

      "return correct question for the calculation election details" in {
        assertExpectedResult(result)(_.question shouldBe messages.calculationElection)
      }

      "return correct answer for the calculation election details" in {
        assertExpectedResult(result)(_.data shouldBe messages.timeCalculation)
      }

      "return a link for the calculation election details" in {
        assertExpectedResult(result)(_.link should not be None)
      }

      "return correct link for the calculation election details" in {
        assertExpectedResult(result) { item =>
          assertExpectedLink(item.link) { link =>
            link shouldBe routes.CalculationElectionController.calculationElection().url
          }
        }
      }
    }
  }

  "Calling totalGain" when {

    "the gain is zero" should {

      lazy val result = target.totalGain(0)

      "return some total gain details" in {
        result should not be None
      }

      "return correct ID for the total gain details" in {
        assertExpectedResult(result)(_.id shouldBe "calcDetails:totalGain")
      }

      "return correct question for the total gain details" in {
        assertExpectedResult(result)(_.question shouldBe messages.totalGain)
      }

      "return correct answer for the total gain details" in {
        assertExpectedResult(result)(_.data shouldBe 0)
      }

      "not return a link for the total gain details" in {
        assertExpectedResult(result)(_.link shouldBe None)
      }
    }

    "the gain is greater than zero" should {

      lazy val result = target.totalGain(1)

      "return some total gain details" in {
        result should not be None
      }

      "return correct ID for the total gain details" in {
        assertExpectedResult(result)(_.id shouldBe "calcDetails:totalGain")
      }

      "return correct question for the total gain details" in {
        assertExpectedResult(result)(_.question shouldBe messages.totalGain)
      }

      "return correct answer for the total gain details" in {
        assertExpectedResult(result)(_.data shouldBe 1)
      }

      "not return a link for the total gain details" in {
        assertExpectedResult(result)(_.link shouldBe None)
      }
    }

    "the total gain is less than zero" should {

      lazy val result = target.totalGain(-1)

      "return no total gain details" in {
        result shouldBe None
      }
    }
  }

  "Calling totalLoss" when {

    "the gain is zero" should {

      lazy val result = target.totalLoss(0)

      "return no total loss details" in {
        result shouldBe None
      }
    }

    "the gain is greater than zero" should {

      lazy val result = target.totalLoss(1)

      "return no total loss details" in {
        result shouldBe None
      }
    }

    "the total gain is less than zero" should {

      lazy val result = target.totalLoss(-1)

      "return some total loss details" in {
        result should not be None
      }

      "return correct ID for the total loss details" in {
        assertExpectedResult(result)(_.id shouldBe "calcDetails:totalLoss")
      }

      "return correct question for the total loss details" in {
        assertExpectedResult(result)(_.question shouldBe messages.totalLoss)
      }

      "return correct answer for the total loss details" in {
        assertExpectedResult(result)(_.data shouldBe 1)
      }

      "not return a link for the total loss details" in {
        assertExpectedResult(result)(_.link shouldBe None)
      }
    }
  }
}
