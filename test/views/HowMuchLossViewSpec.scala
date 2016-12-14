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
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class HowMuchLossViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Previous Loss view" when {

    "provided with no errors" should {
      lazy val view = views.html.calculation.nonresident.howMuchLoss(howMuchLossForm)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of ${messages.HowMuchLoss.question}" in {
        document.title shouldBe messages.HowMuchLoss.question
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has the text" in {
          backLink.text shouldBe messages.back
        }

        "has the class back-link" in {
          backLink.attr("class") shouldBe "back-link"
        }

        s"has a route to 'Previous Gain Or Loss'" in {
          backLink.attr("href") shouldBe controllers.nonresident.routes.PreviousGainOrLossController.previousGainOrLoss().url
        }
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-xlarge" in {
          heading.attr("class") shouldBe "heading-xlarge"
        }

        s"has the text '${messages.HowMuchLoss.question}'" in {
          heading.text shouldBe messages.HowMuchLoss.question
        }
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.nonresident.routes.HowMuchLossController.submitHowMuchLoss().url}'" in {
          form.attr("action") shouldBe controllers.nonresident.routes.HowMuchLossController.submitHowMuchLoss().url
        }
      }

      s"have a label" which {

        lazy val label = document.select("label span").first()

        s"has the question '${messages.HowMuchLoss.question}'" in {
          label.text shouldBe messages.HowMuchLoss.question
        }

        "has the class visuallyhidden" in {
          label.hasClass("visuallyhidden") shouldEqual true
        }
      }

      "have an input with the id 'loss" in {
        document.body().select("input").attr("id") shouldBe "loss"
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

    "provided with some errors" should {
      lazy val form = howMuchLossForm.bind(Map("loss" -> ""))
      lazy val view = views.html.calculation.nonresident.howMuchLoss(form)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
