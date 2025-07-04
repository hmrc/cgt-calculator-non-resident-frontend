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

import assets.MessageLookup.NonResident.{AcquisitionValue => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import controllers.routes
import forms.AcquisitionValueForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.acquisitionValue

class AcquisitionValueViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val acquisitionValueView: acquisitionValue = fakeApplication.injector.instanceOf[acquisitionValue]

  "the Acquisition Value View" should {

    lazy val view = acquisitionValueView(acquisitionValueForm)(using fakeRequest,mockMessage)
    lazy val document = Jsoup.parse(view.body)

    "have a h1 tag that" should {

      s"have the heading ${messages.question}" in {
        document.select("h1 > label").text shouldEqual messages.question
      }

      "wraps the form label" in {
        document.select("h1").hasClass("govuk-label-wrapper") shouldBe true
      }

      "contains an extra large label" in {
        document.select("h1 > label").hasClass("govuk-label--xl")
      }
    }

    s"have a home link to '${controllers.routes.DisposalDateController.disposalDate.url}'" in {
      document.getElementsByClass("govuk-header__link govuk-header__service-name").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate.url
    }

    "have a 'Back link' that" should{

      lazy val helpText = document.getElementsByClass("govuk-hint")

      s"have the text of ${commonMessages.back}" in {
        document.select("a.govuk-back-link").text shouldEqual commonMessages.back
      }

      s"have a link to ${routes.BoughtForLessController.boughtForLess.url}" in {
        document.select("a.govuk-back-link").attr("href") shouldEqual "#"
      }

      "has the back-link class" in {
        document.select(".govuk-back-link").attr("href") shouldBe "#"
      }

      s"contains help text '${messages.helpText}'" in {
        helpText.text should include(messages.helpText)
      }
    }

    "render a form tag" which {

      lazy val form = document.select("form")

      "has a submit action" in {
        form.attr("action") shouldEqual "/calculate-your-capital-gains/non-resident/acquisition-value"
      }

      "with method type POST" in {
        form.attr("method") shouldBe "POST"
      }
    }

    "display an input box for the Acquisition Value" in {
      document.select("input").attr("id") shouldBe "acquisitionValue"
    }

    "have a button that" should {
      lazy val button = document.select("button")

      s"have the message of ${commonMessages.continue}" in {
        button.text shouldEqual commonMessages.continue
      }

      "have the class 'button'" in {
        button.attr("class") shouldBe "govuk-button"
      }

      "have the type 'submit'" in {
        button.attr("id") shouldBe "submit"
      }

      "have the id 'continue-button'" in {
        button.attr("id") shouldBe "submit"
      }
    }

    "supplied with errors" should {
      lazy val form = acquisitionValueForm.bind(Map("acquisitionValue" -> "a"))
      lazy val view = acquisitionValueView(form)(using fakeRequest,mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.getElementsByClass("govuk-error-summary").size() shouldBe 1
      }
    }

    "produce the same output when render and f are called " in {
      acquisitionValueView.render(acquisitionValueForm, fakeRequest,mockMessage) shouldBe
        acquisitionValueView.f(acquisitionValueForm)(fakeRequest,mockMessage)
    }
  }
}
