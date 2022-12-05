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

import assets.MessageLookup.NonResident.WorthWhenGiftedTo
import assets.MessageLookup.{NonResident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.WorthWhenGiftedTo.worthWhenGiftedToForm
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.worthWhenGiftedTo

class WorthWhenGiftedToViewSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  val worthWhenGiftedToView = fakeApplication.injector.instanceOf[worthWhenGiftedTo]
  lazy val pageTitle = s"""${WorthWhenGiftedTo.question} - ${commonMessages.pageHeading} - GOV.UK"""

  "The Worth When Gifted To view spec" when {

    "supplied with no errors" should {

      lazy val view = worthWhenGiftedToView(worthWhenGiftedToForm)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '${pageTitle}'" in {
        document.title() shouldBe pageTitle
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has a class of 'back-link'" in {
          backLink.attr("class") shouldBe "govuk-back-link"
        }

        "has the text" in {
          backLink.text shouldBe commonMessages.back
        }

        s"has a route to 'how-became-owner'" in {
          backLink.attr("href") shouldBe "javascript:history.back()"
        }
      }

      "have a heading" which {

        lazy val heading = document.body().select("h1")

        "has a class of heading-large" in {
          heading.attr("class") shouldBe "govuk-heading-xl"
        }

        s"has the text '${WorthWhenGiftedTo.question}'" in {
          heading.text shouldBe WorthWhenGiftedTo.question
        }
      }

      s"has the hint text ${WorthWhenGiftedTo.hintText}" in {
        document.getElementsByClass("govuk-hint").text() shouldEqual WorthWhenGiftedTo.hintText
      }

      s"has the joint ownership text ${WorthWhenGiftedTo.jointOwnership}" in {
        document.getElementsByClass("govuk-inset-text").text shouldEqual WorthWhenGiftedTo.jointOwnership
      }

      "have input containing the id 'acquisitionMarketValue'" in {
        document.body().select("input").attr("id") should include("acquisitionMarketValue")
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.WorthWhenGiftedToController.submitWorthWhenGiftedTo.url}'" in {
          form.attr("action") shouldBe controllers.routes.WorthWhenGiftedToController.submitWorthWhenGiftedTo.url
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
      }

      "should produce the same output when render and f are called" in {
        worthWhenGiftedToView.f(worthWhenGiftedToForm)(fakeRequest, mockMessage) shouldBe worthWhenGiftedToView.render(worthWhenGiftedToForm, fakeRequest, mockMessage)
      }
    }

    "supplied with a form with errors" should {

      lazy val form = worthWhenGiftedToForm.bind(Map("acquisitionMarketValue" -> "a"))
      lazy val view = worthWhenGiftedToView(form)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.getElementsByClass("govuk-error-summary").size() shouldBe 1
      }
    }
  }
}
