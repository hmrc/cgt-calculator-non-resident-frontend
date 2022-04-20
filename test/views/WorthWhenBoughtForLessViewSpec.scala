/*
 * Copyright 2022 HM Revenue & Customs
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

import assets.MessageLookup.NonResident.WorthWhenBoughtForLess
import assets.MessageLookup.{NonResident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.AcquisitionMarketValueForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.worthWhenBoughtForLess

class WorthWhenBoughtForLessViewSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val worthWhenBoughtForLessView = fakeApplication.injector.instanceOf[worthWhenBoughtForLess]

  "The Worth When Bought For Less view spec" when {

    "supplied with no errors" should {

      lazy val view = worthWhenBoughtForLessView(acquisitionMarketValueForm)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '${WorthWhenBoughtForLess.question}'" in {
        document.title() shouldBe WorthWhenBoughtForLess.question + " - Calculate your Non-Resident Capital Gains Tax - GOV.UK"

      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has a class of 'back-link'" in {
          backLink.attr("class") shouldBe "govuk-back-link"
        }

        "has the text" in {
          backLink.text shouldBe commonMessages.back
        }

        s"has a route to 'bought-for-less'" in {
          backLink.attr("href") shouldBe "javascript:history.back()"
        }
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
        document.getElementsByClass("govuk-header__link govuk-header__link--service-name").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate().url
      }


      "have a heading" which {

        lazy val heading = document.body().select("h1")

        "has a class of heading-large" in {
          heading.attr("class") shouldBe "govuk-heading-xl"
        }

        s"has the text '${WorthWhenBoughtForLess.question}'" in {
          heading.text shouldBe WorthWhenBoughtForLess.question
        }
      }

      "have help text" which {

        lazy val hintText = document.getElementsByClass("govuk-hint")
        lazy val helpText = document.getElementsByClass("govuk-inset-text")

        s"contains hint text '${WorthWhenBoughtForLess.hintOne}'" in {
          hintText.text() should include(WorthWhenBoughtForLess.hintOne)
        }

        s"contains help text '${WorthWhenBoughtForLess.helpText}'" in {
          helpText.text() should include(WorthWhenBoughtForLess.helpText)
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
          button.attr("class") shouldBe "govuk-button"
        }

        "has the type 'submit'" in {
          button.attr("id") shouldBe "submit"
        }

        "has the id 'continue-button'" in {
          button.attr("id") shouldBe "submit"
        }
      }

      "should produce the same output when render and f are called" in {
        worthWhenBoughtForLessView.f(acquisitionMarketValueForm)(fakeRequest, mockMessage) shouldBe worthWhenBoughtForLessView.render(acquisitionMarketValueForm, fakeRequest, mockMessage)
      }
    }

    "supplied with a form with errors" should {

      lazy val form = acquisitionMarketValueForm.bind(Map("acquisitionMarketValue" -> "a"))
      lazy val view = worthWhenBoughtForLessView(form)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.getElementsByClass("govuk-error-summary").size() shouldBe 1      }
    }
  }
}
