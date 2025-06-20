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

package views

import assets.MessageLookup
import assets.MessageLookup.NonResident.{MarketValue => MarketValueMessages}
import assets.MessageLookup.{NonResident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.MarketValueGaveAwayForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.marketValueGaveAway

class MarketValueGaveAwayViewSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  val marketValueGaveAwayView: marketValueGaveAway = fakeApplication.injector.instanceOf[marketValueGaveAway]

  "The market value when gave away page" should {

    lazy val view = marketValueGaveAwayView(marketValueForm)(using fakeRequest, mockMessage)
    lazy val document = Jsoup.parse(view.body)

    "supplied with no errors" should {
      s"have a title of ${MarketValueMessages.disposalGaveAwayQuestion}" in {
        document.title() shouldBe s"${MarketValueMessages.disposalGaveAwayQuestion} - Calculate your Non-Resident Capital Gains Tax - GOV.UK"
      }

      s"have a header" which {
        lazy val header = document.select("h1")
        s"has the text '${MarketValueMessages.disposalGaveAwayQuestion}'" in {
          header.text() shouldBe MarketValueMessages.disposalGaveAwayQuestion
        }

        s"has the class 'head-xlarge'" in {
          header.attr("class") shouldBe "govuk-heading-xl"
        }
      }

      s"have a paragraph" which {
        lazy val helpText = document.getElementsByClass("govuk-body")
        s"has the help text'${MarketValueMessages.disposalHelpText}'" in {
          helpText.text should include(MarketValueMessages.disposalHelpText)
          helpText.text should include(MarketValueMessages.disposalHelpTextAdditional)
        }
        s"has the class 'govuk-body'" in {
          helpText.attr("class") shouldBe "govuk-body"
        }
      }

      "have a back link" which {
        lazy val backLink = document.select(".govuk-back-link")

        "has a class of 'back-link'" in {
          backLink.attr("class") shouldBe "govuk-back-link"
        }

        "has the text" in {
          backLink.text shouldBe commonMessages.back
        }

        s"has a route to 'who-did-you-give-it-to'" in {
          backLink.attr("href") shouldBe "#"
        }
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.MarketValueWhenSoldOrGaveAwayController.submitMarketValueWhenGaveAway}'" in {
          form.attr("action") shouldBe controllers.routes.MarketValueWhenSoldOrGaveAwayController.submitMarketValueWhenGaveAway.url
        }

        s"has the hidden text ${MessageLookup.NonResident.MarketValue.disposalGaveAwayQuestion}" in {
          document.getElementsByClass("govuk-heading-xl").text() shouldBe MessageLookup.NonResident.MarketValue.disposalGaveAwayQuestion
        }

        s"has the input ID disposalValue" in {
          form.select("input").attr("id") shouldBe "disposalValue"
        }

        s"that has a paragraph with the text ${MarketValueMessages.jointOwnership}" in {
          document.getElementsByClass("govuk-inset-text").text shouldBe MarketValueMessages.jointOwnership
        }
      }

      "have a button" which {
        lazy val button = document.getElementsByClass("govuk-button")

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
        marketValueGaveAwayView.f(marketValueForm)(fakeRequest, mockMessage) shouldBe marketValueGaveAwayView.render(marketValueForm, fakeRequest, mockMessage)
      }
    }

    "supplied with a form with errors" should {
      lazy val form = marketValueForm.bind(Map("disposalValue" -> "testData"))
      lazy val view = marketValueGaveAwayView(form)(using fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.getElementsByClass("govuk-error-summary").size() shouldBe 1
      }
    }
  }
}
