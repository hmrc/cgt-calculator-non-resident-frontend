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

import assets.MessageLookup.NonResident.{WorthWhenBoughtForLess, AcquisitionMarketValue => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import forms.AcquisitionMarketValueForm._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.worthWhenBoughtForLess
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class WorthWhenBoughtForLessViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {



  "The Worth When Bought For Less view spec" when {

    "supplied with no errors" should {

      lazy val view = worthWhenBoughtForLess(acquisitionMarketValueForm)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '${WorthWhenBoughtForLess.question}'" in {
        document.title() shouldBe WorthWhenBoughtForLess.question
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has a class of 'back-link'" in {
          backLink.attr("class") shouldBe "back-link"
        }

        "has the text" in {
          backLink.text shouldBe commonMessages.back
        }

        s"has a route to 'bought-for-less'" in {
          backLink.attr("href") shouldBe controllers.routes.BoughtForLessController.boughtForLess().url
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

        s"has the text '${WorthWhenBoughtForLess.question}'" in {
          heading.text shouldBe WorthWhenBoughtForLess.question
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

        s"has an action of '${controllers.routes.WorthWhenBoughtForLessController.submitWorthWhenBoughtForLess().url}'" in {
          form.attr("action") shouldBe controllers.routes.WorthWhenBoughtForLessController.submitWorthWhenBoughtForLess().url
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
      lazy val view = worthWhenBoughtForLess(form)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
