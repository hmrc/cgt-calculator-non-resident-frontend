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

import assets.MessageLookup.NonResident.{WorthBeforeLegislationStart, AcquisitionMarketValue => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class WorthBeforeLegislationStartViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  "The Worth Before Legislation Start view spec" when {

    "supplied with no errors" should {

      lazy val view = worthBeforeLegislationStart(worthBeforeLegislationStartForm)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '${WorthBeforeLegislationStart.question}'" in {
        document.title() shouldBe WorthBeforeLegislationStart.question
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has a class of 'back-link'" in {
          backLink.attr("class") shouldBe "back-link"
        }

        "has the text" in {
          backLink.text shouldBe commonMessages.back
        }

        s"has a route to 'acquisition-date'" in {
          backLink.attr("href") shouldBe controllers.nonresident.routes.AcquisitionDateController.acquisitionDate().url
        }
      }

      s"have a home link to '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
      }

      "have a heading" which {

        lazy val heading = document.body().select("h1")

        "has a class of heading-large" in {
          heading.attr("class") shouldBe "heading-xlarge"
        }

        s"has the text '${WorthBeforeLegislationStart.question}'" in {
          heading.text shouldBe WorthBeforeLegislationStart.question
        }
      }

      "have help text" which {

        lazy val helpText = document.body().select("#helpText")

        s"contains help text '${messages.hintTwo}'" in {
          helpText.text() should include (messages.hintTwo)
        }
      }

      "have a drop down section" which {

        lazy val example = document.select("details")

        "should have a span for the title" which {

          lazy val exampleTitle = example.select("span")

          s"should have the text ${WorthBeforeLegislationStart.expandableTitle}" in {
            exampleTitle.text shouldEqual WorthBeforeLegislationStart.expandableTitle
          }
        }

        "should have a div" which {

          lazy val exampleContent = example.select("div")

          s"should have the text ${WorthBeforeLegislationStart.expandableText}" in {
            exampleContent.select("p").text shouldEqual WorthBeforeLegislationStart.expandableText
          }
        }
      }

      "have input containing the id 'worthBeforeLegislationStart'" in {
        document.body().select("input").attr("id") should include("worthBeforeLegislationStart")
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.nonresident.routes.WorthBeforeLegislationStartController.submitWorthBeforeLegislationStart().url}'" in {
          form.attr("action") shouldBe controllers.nonresident.routes.WorthBeforeLegislationStartController.submitWorthBeforeLegislationStart().url
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

      lazy val form = worthBeforeLegislationStartForm.bind(Map("worthBeforeLegislationStart" -> "a"))
      lazy val view = worthBeforeLegislationStart(form)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
