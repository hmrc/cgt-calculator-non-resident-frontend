/*
 * Copyright 2020 HM Revenue & Customs
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
import config.ApplicationConfig
import constructors.helpers.AssertHelpers
import controllers.helpers.FakeRequestHelper
import forms.BroughtForwardLossesForm._
import org.jsoup.Jsoup
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.broughtForwardLosses

class BroughtForwardLossesViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with AssertHelpers with MockitoSugar{

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
 implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  "Brought forward losses view" when {

    "provided with no errors" should {
      lazy val view = broughtForwardLosses(broughtForwardLossesForm, "back-link")(fakeRequest,mockMessage, fakeApplication, mockConfig)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of ${messages.BroughtForwardLosses.question}" in {
        document.title() shouldBe messages.BroughtForwardLosses.question
      }

      "have a back link" which {
        lazy val backLink = document.select("#back-link")

        "has only a single back link" in {
          backLink.size() shouldBe 1
        }

        "has a class of back-link" in {
          assertHTML(backLink)(_.attr("class") shouldBe "back-link")
        }

        "has a message of back-link" in {
          assertHTML(backLink)(_.text() shouldBe messages.back)
        }

        "has a link to back-link" in {
          assertHTML(backLink)(_.attr("href") shouldBe "back-link")
        }
      }

      "have a H1 tag" which {
        lazy val header = document.select("h1")

        "has only a single header" in {
          header.size() shouldBe 1
        }

        s"has the text of ${messages.BroughtForwardLosses.question}" in {
          assertHTML(header)(_.text() shouldBe messages.BroughtForwardLosses.question)
        }

        "has the class" in {
          assertHTML(header)(_.attr("class") shouldBe "heading-xlarge")
        }
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.BroughtForwardLossesController.submitBroughtForwardLosses().url}'" in {
          form.attr("action") shouldBe controllers.routes.BroughtForwardLossesController.submitBroughtForwardLosses().url
        }
      }

      "have help text" which {
        lazy val helpText = document.select("div.form-hint")

        "has only a single div with a class of form-hint" in {
          helpText.size() shouldBe 1
        }

        s"has a paragraph with the text ${messages.BroughtForwardLosses.helpText}" in {
          helpText.select("p").text shouldBe messages.BroughtForwardLosses.helpText
        }

      }

      s"have a legend with the text ${messages.BroughtForwardLosses.question}" in {
        document.select("legend").text() shouldBe messages.BroughtForwardLosses.question
      }

      "have an input id of 'isClaiming'" in {
        document.select("input").attr("id") should include("isClaiming")
      }

      s"have a hidden question of ${messages.BroughtForwardLosses.inputQuestion}" in {
        document.select("label[for=broughtForwardLoss]").text() startsWith messages.BroughtForwardLosses.inputQuestion
      }

      "have an input id of 'broughtForwardLoss'" in {
        document.select("label[for=broughtForwardLoss] input").attr("id") should include("broughtForwardLoss")
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

    "provided with errors" should {
      lazy val form = broughtForwardLossesForm.bind(Map("isClaiming" -> "Yes", "broughtForwardLoss" -> ""))
      lazy val view = broughtForwardLosses(form, "back-link")(fakeRequest,mockMessage, fakeApplication, mockConfig)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }

    "should produce the same output when render and f are called" in {
      broughtForwardLosses.f(broughtForwardLossesForm, "back-link")(fakeRequest,mockMessage, fakeApplication, mockConfig) shouldBe
        broughtForwardLosses.render(broughtForwardLossesForm, "back-link", fakeRequest,mockMessage, fakeApplication, mockConfig)
    }
  }
}
