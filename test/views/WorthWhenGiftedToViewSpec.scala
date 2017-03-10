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

import assets.MessageLookup.NonResident.{WorthWhenGiftedTo, AcquisitionMarketValue => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import forms.AcquisitionMarketValueForm._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.worthWhenGiftedTo
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class WorthWhenGiftedToViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {



  "The Worth When Gifted To view spec" when {

    "supplied with no errors" should {

      lazy val view = worthWhenGiftedTo(acquisitionMarketValueForm)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '${WorthWhenGiftedTo.question}'" in {
        document.title() shouldBe WorthWhenGiftedTo.question
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has a class of 'back-link'" in {
          backLink.attr("class") shouldBe "back-link"
        }

        "has the text" in {
          backLink.text shouldBe commonMessages.back
        }

        s"has a route to 'how-became-owner'" in {
          backLink.attr("href") shouldBe controllers.routes.HowBecameOwnerController.howBecameOwner().url
        }
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate().url
      }

      "have a heading" which {

        lazy val heading = document.body().select("h1")

        "has a class of heading-large" in {
          heading.attr("class") shouldBe "heading-xlarge"
        }

        s"has the text '${WorthWhenGiftedTo.question}'" in {
          heading.text shouldBe WorthWhenGiftedTo.question
        }
      }

      "have help text" which {

        lazy val helpText = document.body().select("#helpText")

        s"contains help text '${messages.hintOne}'" in {
          helpText.text() should include(messages.hintOne)
        }

        s"contains help text '${messages.hintTwo}'" in {
          helpText.text() should include (messages.hintTwo)
        }
      }

      "have input containing the id 'acquisitionMarketValue'" in {
        document.body().select("input").attr("id") should include("acquisitionMarketValue")
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.WorthWhenGiftedToController.submitWorthWhenGiftedTo().url}'" in {
          form.attr("action") shouldBe controllers.routes.WorthWhenGiftedToController.submitWorthWhenGiftedTo().url
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

      lazy val form = acquisitionMarketValueForm.bind(Map("acquisitionMarketValue" -> "a"))
      lazy val view = worthWhenGiftedTo(form)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
