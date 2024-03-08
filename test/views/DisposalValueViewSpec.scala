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

import assets.MessageLookup.NonResident.{DisposalValue => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.DisposalValueForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.disposalValue

class DisposalValueViewSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val disposalValueView = fakeApplication.injector.instanceOf[disposalValue]
  lazy val pageTitle = s"${messages.question} - ${commonMessages.serviceName} - GOV.UK"

  "Disposal value view" when {

    "supplied with no errors" should {
      lazy val view = disposalValueView(disposalValueForm)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '$pageTitle'" in {
        document.title() shouldBe pageTitle
      }

      "have a back link" which {
        lazy val backLink = document.body().select(".govuk-back-link")

        "has the text" in {
          backLink.text shouldBe commonMessages.back
        }

        s"has a route to 'sold-for-less'" in {
          backLink.attr("href") shouldBe "#"
        }
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of govuk-label-wrapper" in {
          heading.attr("class") shouldBe "govuk-label-wrapper"
        }

        s"has the text '${messages.question}'" in {
          heading.text shouldBe messages.question
        }
      }

      s"have a label" which {

        lazy val label = document.select("label").first()

        s"has the question '${messages.question}'" in {
          label.text shouldBe messages.question
        }

      }

      "have an input with the id 'disposalValue" in {
        document.body().select("input").attr("id") shouldBe "disposalValue"
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.DisposalValueController.submitDisposalValue.url}'" in {
          form.attr("action") shouldBe controllers.routes.DisposalValueController.submitDisposalValue.url
        }

        s"has a paragraph with the text ${messages.jointOwnership}" in {
          document.body().select(".govuk-hint").text shouldBe messages.jointOwnership
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
        disposalValueView.f(disposalValueForm)(fakeRequest, mockMessage) shouldBe disposalValueView.render(disposalValueForm, fakeRequest, mockMessage)
      }
    }

    "supplied with a form with errors" should {
      lazy val form = disposalValueForm.bind(Map("disposalValue" -> "testData"))
      lazy val view = disposalValueView(form)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select(".govuk-error-summary").size() shouldBe 1
      }
    }
  }
}
