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

package views

import assets.MessageLookup.{NonResident => messages}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import forms.DisposalValueForm._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.disposalValue

class DisposalValueViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  "Disposal value view" when {

    "supplied with no errors" should {
      lazy val view = disposalValue(disposalValueForm)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '${messages.DisposalValue.question}'" in {
        document.title() shouldBe messages.DisposalValue.question
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has the text" in {
          backLink.text shouldBe messages.back
        }

        s"has a route to 'sold-for-less'" in {
          backLink.attr("href") shouldBe controllers.routes.SoldForLessController.soldForLess().url
        }
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-xlarge" in {
          heading.attr("class") shouldBe "heading-xlarge"
        }

        s"has the text '${messages.DisposalValue.question}'" in {
          heading.text shouldBe messages.DisposalValue.question
        }
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate().url
      }

      s"have a label" which {

        lazy val label = document.select("label span").first()

        s"has the question '${messages.DisposalValue.question}'" in {
          label.text shouldBe messages.DisposalValue.question
        }

        "has the class visuallyhidden" in {
          label.hasClass("visuallyhidden") shouldEqual true
        }
      }

      "have an input with the id 'disposalValue" in {
        document.body().select("input").attr("id") shouldBe "disposalValue"
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.DisposalValueController.submitDisposalValue().url}'" in {
          form.attr("action") shouldBe controllers.routes.DisposalValueController.submitDisposalValue().url
        }
      }

      "have a button" which {
        lazy val button = document.select("button")

        "has the class 'button'" in {
          button.attr("class") shouldBe "button"
        }

        "has the type 'submit'" in {
          button.attr("type") shouldBe "submit"
        }

        "has the id 'continue-button'" in {
          button.attr("id") shouldBe "continue-button"
        }
      }
    }

    "supplied with a form with errors" should {
      lazy val form = disposalValueForm.bind(Map("disposalValue" -> "testData"))
      lazy val view = disposalValue(form)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
