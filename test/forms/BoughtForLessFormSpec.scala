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

package forms.nonResident

import models.BoughtForLessModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.{NonResident => messages}
import forms.BoughtForLessForm

class BoughtForLessFormSpec extends UnitSpec with WithFakeApplication {

  def assertOption[T](message: String)(option: Option[T])(test: T => Unit): Unit = {
    option.fold(cancel(message)) { value =>
      test(value)
    }
  }

  def assertNoErrors[T](option: Option[T])(test: T => Unit): Unit = assertOption("expected form contains errors")(option)(test)
  def assertSomeErrors[T](option: Option[T])(test:T => Unit): Unit = assertOption("expected form contains no errors")(option)(test)

  "Creating a form" when {

    "supplied with a model" should {
      val model = BoughtForLessModel(true)
      val form = boughtForLessForm.fill(model)

      "return a valid map" in {
        form.data shouldBe Map("boughtForLess" -> "Yes")
      }
    }

    "supplied with a valid map with 'Yes'" should {
      val map = Map("boughtForLess" -> "Yes")
      val form = boughtForLessForm.bind(map)

      "return a form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a model containing a true" in {
        assertNoErrors(form.value)(_ shouldBe BoughtForLessModel(true))
      }
    }

    "supplied with a valid map with 'No'" should {
      val map = Map("boughtForLess" -> "No")
      val form = boughtForLessForm.bind(map)

      "return a form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a model containing a false" in {
        assertNoErrors(form.value)(_ shouldBe BoughtForLessModel(false))
      }
    }

    "supplied with an invalid map with an empty value" should {
      val map = Map("boughtForLess" -> "")
      lazy val form = boughtForLessForm.bind(map)

      "return a form with one error" in {
        form.errors.size shouldBe 1
      }

      s"contain an error message of ${messages.errorRequired}" in {
        assertSomeErrors(form.error("boughtForLess"))(_.message shouldBe messages.errorRequired)
      }
    }

    "supplied with an invalid map with a non-yes-no value" should {
      val map = Map("boughtForLess" -> "invalid text")
      lazy val form = boughtForLessForm.bind(map)

      "return a form with one error" in {
        form.errors.size shouldBe 1
      }

      s"contain an error message of ${messages.errorRequired}" in {
        assertSomeErrors(form.error("boughtForLess"))(_.message shouldBe messages.errorRequired)
      }
    }
  }

}
