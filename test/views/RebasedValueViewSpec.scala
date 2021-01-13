/*
 * Copyright 2021 HM Revenue & Customs
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
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.RebasedValueForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.rebasedValue

class RebasedValueViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  "The rebased value view" when {

    "not supplied with a pre-existing stored model" should {

      lazy val view = rebasedValue(rebasedValueForm, "google.com")(fakeRequest, mockMessage, fakeApplication, mockConfig)
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

      "have a dynamic back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has a class of 'back-link'" in {
          backLink.attr("class") shouldBe "back-link"
        }

        "has the text" in {
          backLink.text shouldBe commonMessages.back
        }

        s"has a route to 'google.com'" in {
          backLink.attr("href") shouldBe "google.com"
        }
      }

      s"NOT have a paragraph with the text ${messages.questionOptionalText}" in {
        document.select("""article > p[class=""]""").isEmpty shouldBe true
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

      s"have a joint ownership section with the text ${messages.jointOwnership}" in {
        document.select("p.panel-indent").first().text() shouldBe messages.jointOwnership
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

      "should produce the same output when render and f are called" in {
        rebasedValue.f(rebasedValueForm, "google.com")(fakeRequest, mockMessage, fakeApplication, mockConfig) shouldBe rebasedValue.render(rebasedValueForm, "google.com", fakeRequest, mockMessage, fakeApplication, mockConfig)
      }
    }

    "supplied with a form with errors" should {

      lazy val form = rebasedValueForm.bind(Map("rebasedValueAmt" -> ""))
      lazy val view = rebasedValue(form, "google.com")(fakeRequest, mockMessage, fakeApplication, mockConfig)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
