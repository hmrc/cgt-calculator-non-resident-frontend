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

import models.nonresident.{CalculationElectionModel, OtherReliefsModel, QuestionAnswerModel}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import common.nonresident.CalculationType
import helpers.AssertHelpers
import assets.MessageLookup.{NonResident => messages}
import common.KeystoreKeys

class OtherReliefsDetailsConstructorSpec extends UnitSpec with AssertHelpers with WithFakeApplication {

  private def assertExpectedResult[T](option: Option[T])(test: T => Unit) = assertOption("expected option is None")(option)(test)

  "Calling .getOtherReliefsRebasedRow" when {

    "provided with no model" should {
      val result = OtherReliefsDetailsConstructor.getOtherReliefsRebasedRow(None, CalculationType.rebased)

      "return a None" in {
        result shouldBe None
      }
    }

    "provided with no value for other reliefs" should {
      val result = OtherReliefsDetailsConstructor.getOtherReliefsRebasedRow(Some(OtherReliefsModel(0)), CalculationType.rebased)

      "should return a None" in {
        result shouldBe None
      }
    }

    "provided with a calculation type which is not rebased" should {
      val result = OtherReliefsDetailsConstructor.getOtherReliefsRebasedRow(Some(OtherReliefsModel(10)), CalculationType.flat)

      "return a None" in {
        result shouldBe None
      }
    }

    "provided with a rebased calculation and other reliefs value" should {
      lazy val result = OtherReliefsDetailsConstructor.getOtherReliefsRebasedRow(Some(OtherReliefsModel(10)), CalculationType.rebased)

      "return a QuestionAnswerModel" in {
        result.isDefined shouldBe true
      }

      s"have a question of ${messages.OtherReliefs.question}" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.question shouldBe messages.OtherReliefs.question)
      }

      s"have an id of ${KeystoreKeys.otherReliefsRebased}" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.id shouldBe KeystoreKeys.otherReliefsRebased)
      }

      "have a value of 10" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.data shouldBe 10)
      }

      "have no link" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.link shouldBe None)
      }
    }
  }

  "Calling .getOtherReliefsTimeApportionedRow" when {

    "provided with no model" should {
      val result = OtherReliefsDetailsConstructor.getOtherReliefsTimeApportionedRow(None, CalculationType.timeApportioned)

      "return a None" in {
        result shouldBe None
      }
    }

    "provided with no value for other reliefs" should {
      val result = OtherReliefsDetailsConstructor.getOtherReliefsTimeApportionedRow(Some(OtherReliefsModel(0)),
        CalculationType.timeApportioned)

      "should return a None" in {
        result shouldBe None
      }
    }

    "provided with a calculation type which is not time apportioned" should {
      val result = OtherReliefsDetailsConstructor.getOtherReliefsTimeApportionedRow(Some(OtherReliefsModel(10)), CalculationType.flat)

      "return a None" in {
        result shouldBe None
      }
    }

    "provided with a time apportioned calculation and other reliefs value" should {
      lazy val result = OtherReliefsDetailsConstructor.getOtherReliefsTimeApportionedRow(Some(OtherReliefsModel(10)),
        CalculationType.timeApportioned)

      "return a QuestionAnswerModel" in {
        result.isDefined shouldBe true
      }

      s"have a question of ${messages.OtherReliefs.question}" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.question shouldBe messages.OtherReliefs.question)
      }

      s"have an id of ${KeystoreKeys.otherReliefsTA}" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.id shouldBe KeystoreKeys.otherReliefsTA)
      }

      "have a value of 10" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.data shouldBe 10)
      }

      "have no link" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.link shouldBe None)
      }
    }
  }

  "Calling .getOtherReliefsFlatRow" when {

    "provided with no model" should {
      val result = OtherReliefsDetailsConstructor.getOtherReliefsFlatRow(None, CalculationType.flat)

      "return a None" in {
        result shouldBe None
      }
    }

    "provided with no value for other reliefs" should {
      val result = OtherReliefsDetailsConstructor.getOtherReliefsFlatRow(Some(OtherReliefsModel(0)),
        CalculationType.flat)

      "should return a None" in {
        result shouldBe None
      }
    }

    "provided with a calculation type which is not flat" should {
      val result = OtherReliefsDetailsConstructor.getOtherReliefsFlatRow(Some(OtherReliefsModel(10)), CalculationType.rebased)

      "return a None" in {
        result shouldBe None
      }
    }

    "provided with a flat calculation and other reliefs value" should {
      lazy val result = OtherReliefsDetailsConstructor.getOtherReliefsFlatRow(Some(OtherReliefsModel(10)),
        CalculationType.flat)

      "return a QuestionAnswerModel" in {
        result.isDefined shouldBe true
      }

      s"have a question of ${messages.OtherReliefs.question}" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.question shouldBe messages.OtherReliefs.question)
      }

      s"have an id of ${KeystoreKeys.otherReliefsFlat}" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.id shouldBe KeystoreKeys.otherReliefsFlat)
      }

      "have a value of 10" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.data shouldBe 10)
      }

      "have no link" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.link shouldBe None)
      }
    }
  }
}
