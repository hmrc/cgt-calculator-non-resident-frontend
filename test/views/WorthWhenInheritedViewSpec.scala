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

import assets.MessageLookup.NonResident.WorthWhenInherited
import assets.MessageLookup.{NonResident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.WorthWhenInherited.worthWhenInheritedForm
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.worthWhenInherited

class WorthWhenInheritedViewSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val worthWhenInheritedView = fakeApplication.injector.instanceOf[worthWhenInherited]
  lazy val pageTitle = s"""${WorthWhenInherited.question} - ${commonMessages.pageHeading} - GOV.UK"""

  "The Worth When Inherited To view spec" when {

    "supplied with no errors" should {

      lazy val view = worthWhenInheritedView(worthWhenInheritedForm)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '$pageTitle'" in {
        document.title() shouldBe pageTitle
      }

      "have a back link" which {
        lazy val backLink = document.body().select(".govuk-back-link")

        "has a class of 'govuk-back-link'" in {
          backLink.attr("class") shouldBe "govuk-back-link"
        }

        "has the text" in {
          backLink.text shouldBe commonMessages.back
        }

        s"has a route to 'how-became-owner'" in {
          backLink.attr("href") shouldBe "#"
        }
      }

      "have a heading" which {

        lazy val heading = document.body().select("h1")

        "has a class of govuk-heading-xl" in {
          heading.attr("class") shouldBe "govuk-heading-xl"
        }

        s"has the text '${WorthWhenInherited.question}'" in {
          heading.text shouldBe WorthWhenInherited.question
        }
      }

      "have help text" which {

        lazy val helpText = document.body().select(".govuk-inset-text")
        lazy val hintText = document.body().select(".govuk-hint")


        s"contains hint text '${WorthWhenInherited.hint}'" in {
          hintText.text() should include(WorthWhenInherited.hint)
        }

        s"contains help text '${WorthWhenInherited.helpText}'" in {
          helpText.text() should include(WorthWhenInherited.helpText)
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

        s"has an action of '${controllers.routes.WorthWhenInheritedController.submitWorthWhenInherited.url}'" in {
          form.attr("action") shouldBe controllers.routes.WorthWhenInheritedController.submitWorthWhenInherited.url
        }
      }

      "have a button" which {
        lazy val button = document.select("button")

        "has the class 'govuk-button'" in {
          button.attr("class") shouldBe "govuk-button"
        }

        "has the id 'submit'" in {
          button.attr("id") shouldBe "submit"
        }
      }

      "should produce the same output when render and f are called" in {
        worthWhenInheritedView.f(worthWhenInheritedForm)(fakeRequest, mockMessage) shouldBe worthWhenInheritedView.render(worthWhenInheritedForm, fakeRequest, mockMessage)
      }
    }

    "supplied with a form with errors" should {

      lazy val form = worthWhenInheritedForm.bind(Map("acquisitionMarketValue" -> "a"))
      lazy val view = worthWhenInheritedView(form)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select(".govuk-error-summary").size() shouldBe 1
      }
    }
  }
}
