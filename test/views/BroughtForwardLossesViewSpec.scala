/*
 * Copyright 2023 HM Revenue & Customs
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
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import constructors.helpers.AssertHelpers
import controllers.helpers.FakeRequestHelper
import forms.BroughtForwardLossesForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.broughtForwardLosses

class BroughtForwardLossesViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with AssertHelpers with MockitoSugar{

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val broughtForwardLossesView = fakeApplication.injector.instanceOf[broughtForwardLosses]
  lazy val pageTitle = s"""${messages.BroughtForwardLosses.question} - ${messages.pageHeading} - GOV.UK"""

  "Brought forward losses view" when {

    "provided with no errors" should {
      lazy val view = broughtForwardLossesView(broughtForwardLossesForm, "back-link")(fakeRequest,mockMessage)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of ${pageTitle}" in {
        document.title() shouldBe pageTitle
      }

      "have a back link" which {
        lazy val backLink = document.select("#back-link")

        "has only a single back link" in {
          backLink.size() shouldBe 1
        }

        "has a class of back-link" in {
          assertHTML(backLink)(_.attr("class") shouldBe "govuk-back-link")
        }

        "has a message of back-link" in {
          assertHTML(backLink)(_.text() shouldBe messages.back)
        }

        "has a link to back-link" in {
          assertHTML(backLink)(_.attr("href") shouldBe "javascript:history.back()")
        }
      }

      "have a H1 tag" which {
        lazy val header = document.select("h1")

        "has the class" in {
          header.attr("class") shouldBe "govuk-fieldset__heading"
        }
      }

      "have a form" which {
        lazy val form = document.select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.BroughtForwardLossesController.submitBroughtForwardLosses.url}'" in {
          form.attr("action") shouldBe controllers.routes.BroughtForwardLossesController.submitBroughtForwardLosses.url
        }
      }

      "have body text" which {
        lazy val bodyText = document.body().select(".govuk-hint")
        "has only a single div with a class of govuk-hint" in {
          bodyText.size() shouldBe 1
        }

        s"has a paragraph with the text ${messages.BroughtForwardLosses.helpText}" in {
          bodyText.text shouldBe messages.BroughtForwardLosses.helpText
        }
      }

      "have a heading" which {
        lazy val heading = document.body.select("h1")

        "has the class govuk-fieldset__heading" in {
          heading.attr("class") shouldBe "govuk-fieldset__heading"
        }
          s"with the text ${messages.BroughtForwardLosses.question}" in {
            heading.text() should include(messages.BroughtForwardLosses.question)
          }
        }

      "have an input id of 'isClaiming'" in {
        document.select("input").attr("id") should include("isClaiming")
      }

      s"have a hidden question of ${messages.BroughtForwardLosses.inputQuestion}" in {
        document.select("#conditional-broughtForwardLoss > div > label").text() startsWith messages.BroughtForwardLosses.inputQuestion
      }

      "have an input id of 'broughtForwardLoss'" in {
        document.select("#broughtForwardLoss").attr("id") should include("broughtForwardLoss")
      }

      "have a button" which {
        lazy val button = document.select("button")

        "has the class 'button'" in {
          button.attr("class") shouldBe "govuk-button"
        }

        "has the id 'submit'" in {
          button.attr("id") shouldBe "submit"
        }
      }
    }

    "provided with errors" should {
      lazy val form = broughtForwardLossesForm.bind(Map("isClaiming" -> "Yes", "broughtForwardLoss" -> ""))
      lazy val view = broughtForwardLossesView(form, "back-link")(fakeRequest,mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select(".govuk-error-summary").size() shouldBe 1
      }
    }

    "should produce the same output when render and f are called" in {
      broughtForwardLossesView.f(broughtForwardLossesForm, "back-link")(fakeRequest,mockMessage) shouldBe
        broughtForwardLossesView.render(broughtForwardLossesForm, "back-link", fakeRequest,mockMessage)
    }
  }
}
