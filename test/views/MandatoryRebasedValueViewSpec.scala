/*
 * Copyright 2017 HM Revenue & Customs
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

import assets.MessageLookup.NonResident.{RebasedValue => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import forms.RebasedValueForm._
import controllers.routes
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.mandatoryRebasedValue
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class MandatoryRebasedValueViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {



  "The mandatory rebased value view" when {

    "not supplied with a pre-existing stored model" should {

      lazy val view = mandatoryRebasedValue(rebasedValueForm(true))
      lazy val document = Jsoup.parse(view.body)

      s"Have the title ${messages.question}" in {
        document.title shouldEqual messages.question
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate().url
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-xlarge" in {
          heading.attr("class") shouldBe "heading-xlarge"
        }

        s"has the text '${messages.question}'" in {
          heading.text shouldBe messages.question
        }
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has a class of 'back-link'" in {
          backLink.attr("class") shouldBe "back-link"
        }

        "has the text" in {
          backLink.text shouldBe commonMessages.back
        }

        s"has a route to 'acquisition-costs'" in {
          backLink.attr("href") shouldBe routes.AcquisitionCostsController.acquisitionCosts().url
        }
      }

      s"NOT have a paragraph with the text ${messages.questionOptionalText}" in {
        document.select("article > p").text shouldEqual ""
      }

      "have some hint text" which {
        lazy val hintText = document.select("article > span")

        "should have the class form-hint" in {
          hintText.hasClass("form-hint") shouldEqual true
        }

        s"should have the text ${messages.inputHintText}" in {
          hintText.text shouldEqual messages.inputHintText
        }
      }

      s"Have a hidden help section" which {
        lazy val hiddenHelp = document.select("details")

        s"has a title ${messages.additionalContentTitle}" in {
          hiddenHelp.select(".summary").text shouldEqual messages.additionalContentTitle
        }

        s"has the content ${messages.helpHiddenContent}" in {
          hiddenHelp.select("div > p").text shouldEqual messages.helpHiddenContent
        }
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.RebasedValueController.submitRebasedValue().url}'" in {
          form.attr("action") shouldBe controllers.routes.RebasedValueController.submitRebasedValue().url
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

        "has the text 'Continue'" in {
          button.text shouldEqual commonMessages.continue
        }
      }
    }

    "supplied with a form with errors" should {

      lazy val form = rebasedValueForm(true).bind(Map("rebasedValueAmt" -> ""))
      lazy val view = mandatoryRebasedValue(form)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
