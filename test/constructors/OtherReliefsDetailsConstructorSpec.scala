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

import assets.MessageLookup.{NonResident => messages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.nonresident.{Flat, Rebased, TimeApportioned}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import constructors.helpers.AssertHelpers
import controllers.helpers.FakeRequestHelper
import models.{OtherReliefsModel, QuestionAnswerModel}
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{Messages, MessagesProvider}
import play.api.mvc.MessagesControllerComponents

class OtherReliefsDetailsConstructorSpec extends CommonPlaySpec with AssertHelpers with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  private def assertExpectedResult[T](option: Option[T])(test: T => Unit): Unit = assertOption("expected option is None")(option)(test)
  implicit val mockMessagesProvider: MessagesProvider = mock[MessagesProvider]
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val target = new OtherReliefsDetailsConstructor

  "Calling .getOtherReliefsRebasedRow" when {

    "provided with no model" should {
      val result = target.getOtherReliefsRebasedRow(None, Rebased)

      "return a None" in {
        result shouldBe None
      }
    }

    "provided with no value for other reliefs" should {
      val result = target.getOtherReliefsRebasedRow(Some(OtherReliefsModel(0)), Rebased)

      "should return a None" in {
        result shouldBe None
      }
    }

    "provided with a calculation type which is not rebased" should {
      val result = target.getOtherReliefsRebasedRow(Some(OtherReliefsModel(10)), Flat)

      "return a None" in {
        result shouldBe None
      }
    }

    "provided with a rebased calculation and other reliefs value" should {
      lazy val result = target.getOtherReliefsRebasedRow(Some(OtherReliefsModel(10)), Rebased)

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
      val result = target.getOtherReliefsTimeApportionedRow(None, TimeApportioned)

      "return a None" in {
        result shouldBe None
      }
    }

    "provided with no value for other reliefs" should {
      val result = target.getOtherReliefsTimeApportionedRow(Some(OtherReliefsModel(0)),
        TimeApportioned)

      "should return a None" in {
        result shouldBe None
      }
    }

    "provided with a calculation type which is not time apportioned" should {
      val result = target.getOtherReliefsTimeApportionedRow(Some(OtherReliefsModel(10)), Flat)

      "return a None" in {
        result shouldBe None
      }
    }

    "provided with a time apportioned calculation and other reliefs value" should {
      lazy val result = target.getOtherReliefsTimeApportionedRow(Some(OtherReliefsModel(10)),
        TimeApportioned)

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
      val result = target.getOtherReliefsFlatRow(None, Flat)

      "return a None" in {
        result shouldBe None
      }
    }

    "provided with no value for other reliefs" should {
      val result = target.getOtherReliefsFlatRow(Some(OtherReliefsModel(0)),
        Flat)

      "should return a None" in {
        result shouldBe None
      }
    }

    "provided with a calculation type which is not flat" should {
      val result = target.getOtherReliefsFlatRow(Some(OtherReliefsModel(10)), Rebased)

      "return a None" in {
        result shouldBe None
      }
    }

    "provided with a flat calculation and other reliefs value" should {
      lazy val result = target.getOtherReliefsFlatRow(Some(OtherReliefsModel(10)),
        Flat)

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

  "Calling . getOtherReliefsSection" when {
    "No otherReliefs are given" should {
      "return an empty list" in {
        val result = target.getOtherReliefsSection(None, Flat)

        result shouldBe Seq.empty
      }
    }

    "otherReliefs are given" should {
      "return a list with otherReliefsFlat" in {
        val model = OtherReliefsModel(BigDecimal(100))
        val result = target.getOtherReliefsSection(Some(model), Flat)

        result shouldBe List(QuestionAnswerModel("nr:otherReliefsFlat",100,"How much extra tax relief are you claiming?",None,None))
      }


      "return a list with otherReliefsRebased" in {
        val model = OtherReliefsModel(BigDecimal(100))
        val result = target.getOtherReliefsSection(Some(model), Rebased)

        result shouldBe List(QuestionAnswerModel("nr:otherReliefsRebased",100,"How much extra tax relief are you claiming?",None,None))
      }

      "return a list with otherReliefsTA" in {
        val model = OtherReliefsModel(BigDecimal(100))
        val result = target.getOtherReliefsSection(Some(model), TimeApportioned)

        result shouldBe List(QuestionAnswerModel("nr:otherReliefsTA",100,"How much extra tax relief are you claiming?",None,None))
      }

    }
  }
}
