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

import assets.MessageLookup.NonResident.{RebasedValue => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.RebasedValueForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.rebasedValue

class RebasedValueViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val rebasedValueView: rebasedValue = fakeApplication.injector.instanceOf[rebasedValue]
  val pageTitle: String = s"""${messages.h1} - ${commonMessages.serviceName} - GOV.UK"""

  "The rebased value view" when {

    "not supplied with a pre-existing stored model" should {

      lazy val view = rebasedValueView(rebasedValueForm, "google.com")(using fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      s"Have the title $pageTitle" in {
        document.title shouldEqual pageTitle
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-xlarge" in {
          heading.attr("class") shouldBe "govuk-heading-xl"
        }

        s"has the text '${messages.h1}'" in {
          heading.text shouldBe messages.h1
        }
      }

      "have a dynamic back link" which {
        lazy val backLink = document.body().select(".govuk-back-link")

        "has a class of 'back-link'" in {
          backLink.attr("class") shouldBe "govuk-back-link"
        }

        "has the text" in {
          backLink.text shouldBe commonMessages.back
        }

        s"has a route to 'google.com'" in {
          backLink.attr("href") shouldBe "#"
        }
      }

      s"NOT have a paragraph with the text ${messages.questionOptionalText}" in {
        document.select("""article > p[class=""]""").isEmpty shouldBe true
      }

      "have some body text" which {
        lazy val bodyText = document.getElementsByClass("govuk-body")

        s"should have the text ${messages.inputHintText}" in {
          bodyText.text shouldEqual messages.inputHintText
        }
      }

      s"have a joint ownership section with the text ${messages.jointOwnership}" in {
        document.getElementsByClass("govuk-inset-text").first().text() shouldBe messages.jointOwnership
      }

      s"Have a hidden help section" which {

        s"has a title ${messages.additionalContentTitle}" in {
          document.getElementsByClass("govuk-details__summary-text").text shouldEqual messages.additionalContentTitle
        }

        s"has the content ${messages.helpHiddenContent}" in {
          document.getElementById("help-text").text shouldEqual messages.helpHiddenContent
        }
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.RebasedValueController.submitRebasedValue.url}'" in {
          form.attr("action") shouldBe controllers.routes.RebasedValueController.submitRebasedValue.url
        }
      }

      "have a button" which {
        lazy val button = document.select("button")

        "has the class 'button'" in {
          button.attr("class") shouldBe "govuk-button"
        }

        "has the id 'continue-button'" in {
          button.attr("id") shouldBe "submit"
        }

        "has the text 'Continue'" in {
          button.text shouldEqual commonMessages.continue
        }
      }

      "should produce the same output when render and f are called" in {
        rebasedValueView.f(rebasedValueForm, "google.com")(fakeRequest, mockMessage) shouldBe rebasedValueView.render(rebasedValueForm, "google.com", fakeRequest, mockMessage)
      }
    }

    "supplied with a form with errors" should {

      lazy val form = rebasedValueForm.bind(Map("rebasedValueAmt" -> ""))
      lazy val view = rebasedValueView(form, "google.com")(using fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.getElementsByClass("govuk-error-summary").size() shouldBe 1
      }
    }
  }
}
