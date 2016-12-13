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

package views.nonResident

import controllers.helpers.FakeRequestHelper
import forms.nonresident.SoldOrGivenAwayForm._
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.nonresident.soldOrGivenAway
import assets.MessageLookup.NonResident.{SoldOrGivenAway => messages}
import assets.MessageLookup.{NonResident => commonMessages}

class SoldOrGivenAwayViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper{

  "The Sold Or Given Away View" when{

    "not supplied with a pre-existing model" should {
      lazy val view = soldOrGivenAway(soldOrGivenAwayForm)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "have a 'back-link' that" should {
        lazy val backLink = document.body().select("#back-link")

        "have the class of 'back-link'" in {
          backLink.attr("class") shouldBe "back-link"
        }

        s"have the text of ${commonMessages.back}" in {
          backLink.text() shouldBe commonMessages.back
        }

        "has the route to Disposal Date" in {
          backLink.attr("href") shouldBe controllers.nonresident.routes.DisposalDateController.disposalDate().url
        }
      }

      "have a heading that" should {
        lazy val heading = document.select("h1")

        "have the class of heading-large" in {
          heading.attr("class") shouldBe "heading-xlarge"
        }

        s"have the text of ${messages.question}" in {
          heading.text shouldBe messages.question
        }
      }

      s"have a home link to '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
      }

      "have a form that" should {
        lazy val form = document.body().select("form")

        "has a post method" in {
          form.attr("method") shouldBe "POST"
        }

        s"have an action of ${controllers.nonresident.routes.SoldOrGivenAwayController.soldOrGivenAway().url}" in {
          form.attr("action") shouldBe controllers.nonresident.routes.SoldOrGivenAwayController.soldOrGivenAway().url
        }
      }

      "have an options input with id 'soldIt" in {
        document.body().select("input[type=radio]").attr("id") should include ("soldIt")
      }

      "have a legend that" should {
        lazy val legend = document.body.select("legend")

        s"have the text of ${messages.question}" in {
          legend.text shouldBe messages.question
        }

        "be visually hidden" in {
          legend.hasClass("visuallyhidden") shouldEqual true
        }
      }

      "have a button that" should {
        lazy val button = document.select("button")

        "has the class of button" in {
          button.attr("class") shouldBe "button"
        }

        s"has the text of ${commonMessages.continue}" in {
          button.text shouldBe commonMessages.continue
        }

        "has the type submit" in {
          button.attr("type") shouldBe "submit"
        }

        "has the id of 'continue-button'" in {
          button.attr("id") shouldBe "continue-button"
        }
      }
    }

    "provided with errors" should {
      lazy val form = soldOrGivenAwayForm.bind(Map("soldIt" -> "999"))
      lazy val view = soldOrGivenAway(form)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size shouldBe 1
      }
    }
  }
}
