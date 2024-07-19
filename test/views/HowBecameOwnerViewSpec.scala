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

import assets.MessageLookup.NonResident.{HowBecameOwner => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.HowBecameOwnerForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.howBecameOwner

class HowBecameOwnerViewSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val howBecameOwnerView: howBecameOwner = fakeApplication.injector.instanceOf[howBecameOwner]
  val pageTitle = s"""${messages.question} - ${commonMessages.serviceName} - GOV.UK"""

  "The Sold for Less view spec" when {

    "supplied with no errors" should {

      lazy val view = howBecameOwnerView(howBecameOwnerForm)(fakeRequest, mockMessage)
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

        s"has a route to acquisition date" in {
          backLink.attr("href") shouldBe "#"
        }
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate.url}'" in {
        document.select("body > header > div > div > div.govuk-header__content > a")
          .attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate.url
      }

      "have a heading" which {

        lazy val heading = document.body().select("h1")

        "has a class of govuk-fieldset__heading" in {
          heading.attr("class") shouldBe "govuk-fieldset__heading"
        }

        s"has the text '${messages.question}'" in {
          heading.text shouldBe messages.question
        }
      }

      s"have the legend" which {

        lazy val legend = document.select("legend")

        s"has the question ${messages.question}" in {
          legend.text shouldEqual messages.question
        }
      }

      "have inputs containing the id 'gainedBy'" in {
        document.body().select("input").attr("id") should include("gainedBy")
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.HowBecameOwnerController.submitHowBecameOwner.url}'" in {
          form.attr("action") shouldBe controllers.routes.HowBecameOwnerController.submitHowBecameOwner.url
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
        howBecameOwnerView.f(howBecameOwnerForm)(fakeRequest, mockMessage) shouldBe howBecameOwnerView.render(howBecameOwnerForm, fakeRequest, mockMessage)
      }
    }

    "supplied with a form with errors" should {

      lazy val form = howBecameOwnerForm.bind(Map("gainedBy" -> "a"))
      lazy val view = howBecameOwnerView(form)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.getElementsByClass("govuk-error-summary").size() shouldBe 1
      }
    }

  }

}
