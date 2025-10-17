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

import assets.MessageLookup.{NonResident => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import controllers.routes
import forms.ImprovementsForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.improvementsRebased

class ImprovementsRebasedViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  val mockMessagesApi: MessagesApi = mock[MessagesApi]
  lazy val improvementsRebasedView: improvementsRebased = fakeApplication.injector.instanceOf[improvementsRebased]
  lazy val pageHeading: String = messages.ImprovementsRebased.title
  lazy val pageTitle: String = s"$pageHeading - ${messages.serviceName} - GOV.UK"

  "Improvements view" should {

    "supplied with no errors, improvementsOptions = true and is owner after legislation start" should {

      lazy val view = improvementsRebasedView(improvementsForm(true))(using fakeRequest, mockMessage, mockMessagesApi)
      lazy val document = Jsoup.parse(view.body)

      "return some HTML that" should {

        s"has the title of $pageTitle" in {
          document.title shouldBe pageTitle
        }

        s"has the heading of $pageHeading" in {
          document.body().getElementsByTag("h1").first().text shouldBe pageHeading
        }

        "has a class of govuk-heading-l" in {
          document.body().getElementsByTag("h1").hasClass("govuk-heading-l") shouldBe true
        }

        "contains 2 inputs for before and after" in {
          document.body.select("input.govuk-input").first().attr("id") shouldBe "improvementsAmt"
          document.body.select("input.govuk-input").last().attr("id") shouldBe "improvementsAmtAfter"
        }

        "have a back link" which {

          lazy val backLink = document.body().select(".govuk-back-link")

          s"has the text ${messages.back}" in {
            backLink.text shouldEqual messages.back
          }

          "has a class of 'back-link'" in {
            backLink.attr("class") shouldBe "govuk-back-link"
          }

          s"has a route to 'back-link'" in {
            backLink.attr("href") shouldBe "#"
          }
        }

        s"have a home link to '${controllers.routes.DisposalDateController.disposalDate.url}'" in {
          document.getElementsByClass("govuk-header__link govuk-header__service-name").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate.url
        }

        "have inset text" which {

          lazy val helpText = document.getElementsByClass("govuk-inset-text")

          s"should have help text of ${messages.ImprovementsRebased.jointOwnership}" in {
            helpText.text() shouldBe messages.ImprovementsRebased.jointOwnership
          }

        }

        "have a form" which {
          lazy val form = document.body().select("form")

          "has a method of POST" in {
            form.attr("method") shouldBe "POST"
          }

          s"has an action of '${routes.ImprovementsController.submitImprovementsRebased.url}'" in {
            form.attr("action") shouldBe controllers.routes.ImprovementsController.submitImprovementsRebased.url
          }

          s"has one input for improvements amounts before tax started" in {
            document.select("label.govuk-label").first().text() shouldBe messages.ImprovementsRebased.questionThree
          }

          s"has one input for improvements amounts after tax started" in {
            document.select("label.govuk-label").last().text() shouldBe messages.ImprovementsRebased.questionFour
          }
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

      "should produce the same output when render and f are called" in {
        improvementsRebasedView.f(improvementsForm(true))(fakeRequest, mockMessage, mockMessagesApi) shouldBe improvementsRebasedView.render(improvementsForm(true), fakeRequest, mockMessage, mockMessagesApi)
      }
    }

    "supplied with errors" should {
      lazy val form = improvementsForm(true).bind(Map("improvementsAmt" -> "testData"))
      lazy val view = improvementsRebasedView(form)(using fakeRequest, mockMessage, mockMessagesApi)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.getElementsByClass("govuk-error-summary").size() shouldBe 1
      }
    }
  }
}
