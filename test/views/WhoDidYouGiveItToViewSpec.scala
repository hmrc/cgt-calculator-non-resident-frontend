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

import assets.MessageLookup.{NonResident => commonMessages, WhoDidYouGiveItTo => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.WhoDidYouGiveItToForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.whoDidYouGiveItTo


class WhoDidYouGiveItToViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {
  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  val whoDidYouGiveItToView: whoDidYouGiveItTo = fakeApplication.injector.instanceOf[whoDidYouGiveItTo]
  val pageTitle: String = s"""${messages.title} - ${commonMessages.serviceName} - GOV.UK"""

  "Property Recipient view" should {

    lazy val view = whoDidYouGiveItToView(whoDidYouGiveItToForm)(using fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title of $pageTitle" in {
      doc.title() shouldBe pageTitle
    }

    "have a back button that" should {
      lazy val backLink = doc.select("a.govuk-back-link")

      "have the correct back link text" in {
        backLink.text shouldBe commonMessages.back
      }

      "have the govuk-back-link class" in {
        backLink.hasClass("govuk-back-link") shouldBe true
      }

      "have a link to Did You Sell or Give Away" in {
        backLink.attr("href") shouldBe "#"
      }
    }

    "have a H1 tag that" should {
      lazy val heading = doc.select("h1")

      s"have the page heading '${messages.title}'" in {
        heading.text shouldBe messages.title
      }
      "have the govuk-fieldset__heading class" in {
        heading.hasClass("govuk-fieldset__heading") shouldBe true
      }
    }

    "have a form" which {
      lazy val form = doc.getElementsByTag("form")

      s"has the action '${controllers.routes.WhoDidYouGiveItToController.submitWhoDidYouGiveItTo.toString}" in {
        form.attr("action") shouldBe controllers.routes.WhoDidYouGiveItToController.submitWhoDidYouGiveItTo.toString
      }

      "has the method of POST" in {
        form.attr("method") shouldBe "POST"
      }

    }

    "have additional content that" should {
      s"have a label for the Your Spouse or Civil Partner option" in {
        doc.select("label").get(0).text() shouldEqual messages.spouse
      }

      s"have a label for the A Charity option" in {
        doc.select("label").get(1).text() shouldEqual messages.charity
      }

      s"have a label for the Someone Else option" in {
        doc.select("label").get(2).text() shouldEqual messages.other
      }
    }

    "has a continue button that" should {
      lazy val continueButton = doc.select("button#submit")

      s"have the button text '${commonMessages.continue}'" in {
        continueButton.text shouldBe commonMessages.continue
      }

      "have the class 'govuk-button'" in {
        continueButton.hasClass("govuk-button") shouldBe true
      }
    }

    "should produce the same output when render and f are called" in {
      whoDidYouGiveItToView.f(whoDidYouGiveItToForm)(fakeRequest, mockMessage) shouldBe whoDidYouGiveItToView.render(whoDidYouGiveItToForm, fakeRequest, mockMessage)
    }
  }

  "WhoDidYouGiveItToView with form with errors" should {
    lazy val form = whoDidYouGiveItToForm.bind(Map("whoDidYouGiveItTo" -> ""))
    lazy val view = whoDidYouGiveItToView(form)(using fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message regarding incorrect value being inputted" in {
      doc.getElementsByClass("govuk-error-summary").size shouldBe 1
    }
  }
}
