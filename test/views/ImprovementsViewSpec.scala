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

import assets.MessageLookup.{NonResident => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import controllers.routes
import forms.ImprovementsForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.improvements

class ImprovementsViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  val mockMessagesApi = mock[MessagesApi]

  "Improvements view" should {

    "supplied with no errors, improvementsOptions = true and is owner after legislation start" should {

      lazy val view = improvements(improvementsForm(true), improvementsOptions = false,
        "back-link", ownerBeforeLegislationStart = false)(fakeRequest, mockMessage, fakeApplication, mockMessagesApi, mockConfig)
      lazy val document = Jsoup.parse(view.body)

      "return some HTML that" should {

        s"has the title of ${messages.Improvements.question}" in {
          document.title shouldBe messages.Improvements.question
        }

        s"has the heading of ${messages.Improvements.question}" in {
          document.body().getElementsByTag("h1").text shouldBe messages.Improvements.question
        }

        "does not contain another component with an input box" in {
          document.body.select("input").attr("id") should not include "improvementsAmtAfter"
        }

        "have a back link" which {

          lazy val backLink = document.body().select("#back-link")

          s"has the text ${messages.back}" in {
            backLink.text shouldEqual messages.back
          }

          "has a class of 'back-link'" in {
            backLink.attr("class") shouldBe "back-link"
          }

          s"has a route to 'back-link'" in {
            backLink.attr("href") shouldBe "back-link"
          }
        }

        s"have a home link to '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
          document.select("#homeNavHref").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate().url
        }

        "have hint text" which {

          lazy val helpText = document.select(".form-hint")

          s"should have a first sentence of ${messages.Improvements.helpOne}" in {
            helpText.select("p").text() should include(messages.Improvements.helpOne)
          }

          s"should have a second sentence of ${messages.Improvements.helpTwo}" in {
            helpText.select("p").text() should include(messages.Improvements.helpTwo)
          }
        }

        "have a drop down example section" which {

          lazy val example = document.select("details")

          "should have a span for the title" which {

            lazy val exampleTitle = example.select("span")

            s"should have the text ${messages.Improvements.exampleTitle}" in {
              exampleTitle.text shouldEqual messages.Improvements.exampleTitle
            }
          }

          "should have a div" which {

            lazy val exampleContent = example.select("div")

            s"should have a first sentence of ${messages.Improvements.exampleOne}" in {
              exampleContent.select("p").text should include(messages.Improvements.exampleOne)
            }

            s"should have a second sentence of ${messages.Improvements.exampleTwo}" in {
              exampleContent.select("p").text should include(messages.Improvements.exampleTwo)
            }
          }
        }

        "have a legend that" should {

          lazy val legend = document.select("legend")

          s"have the text ${messages.Improvements.question}" in {
            legend.text shouldEqual messages.Improvements.question
          }

          "have the class visuallyhidden" in {
            legend.hasClass("visuallyhidden") shouldEqual true
          }
        }

        s"has the correct id for the radio options 'isClaimingImprovements'" in {
          document.body.select("input").attr("id") should include("isClaimingImprovements")
        }

        "have a form" which {
          lazy val form = document.body().select("form")

          "has a method of POST" in {
            form.attr("method") shouldBe "POST"
          }

          s"has an action of '${routes.ImprovementsController.submitImprovements().url}'" in {
            form.attr("action") shouldBe controllers.routes.ImprovementsController.submitImprovements().url
          }
        }

        s"have a paragraph with the text ${messages.Improvements.jointOwnership}" in {
          document.getElementById("jointOwner").text shouldBe messages.Improvements.jointOwnership
        }

        "have some hidden content" which {
          lazy val hiddenContent = document.body().select("#hidden")

          "which has a single div with a class of form-group" in {
            hiddenContent.select("div.form-group").size() shouldBe 1
          }

          "contains an input with the id 'improvementsAmt'" in {
            hiddenContent.select("input").attr("id") shouldBe "improvementsAmt"
          }

          s"contains the question ${messages.Improvements.questionTwo}" in {
            hiddenContent.select("label").text() startsWith messages.Improvements.questionTwo
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
        }
      }

      "should produce the same output when render and f are called" in {
        improvements.f(improvementsForm(true), false,
          "back-link",  false)(fakeRequest, mockMessage, fakeApplication, mockMessagesApi, mockConfig) shouldBe improvements.render(improvementsForm(true), false,
          "back-link", false, fakeRequest, mockMessage, fakeApplication, mockMessagesApi, mockConfig)
      }
    }

    "supplied with no errors and improvementsOptions = false" should {

      lazy val view = improvements(improvementsForm(true), improvementsOptions = true, "back-link", ownerBeforeLegislationStart = false)(fakeRequest, mockMessage, fakeApplication, mockMessagesApi, mockConfig)
      lazy val document = Jsoup.parse(view.body)

      "return some HTML that" should {

        "have that content" which {
          s"display the correct wording for radio option 'isClaimingImprovements'" in {
            document.body.select("input").attr("id") should include("isClaimingImprovements")
          }
        }

        "have some hidden content" which {
          lazy val hiddenContent = document.body().select("#hidden")

          "which has a single div with a class of form-group" in {
            hiddenContent.select("div.form-group").size() shouldBe 3
          }

          s"contains the question ${messages.Improvements.questionThree}" in {
            hiddenContent.select("label").text() should include(messages.Improvements.questionThree)
          }

          s"contains the question ${messages.Improvements.questionFour}" in {
            hiddenContent.select("label").text() should include(messages.Improvements.questionFour)
          }
        }
      }
    }

    "supplied with no errors and is owner before legislation start" should {

      lazy val view = improvements(improvementsForm(true), improvementsOptions = true, "back-link", ownerBeforeLegislationStart = true)(fakeRequest, mockMessage, fakeApplication, mockMessagesApi, mockConfig)
      lazy val document = Jsoup.parse(view.body)

      "return some HTML that" should {

        "have that content" which {

          s"has the title of ${messages.Improvements.ownerBeforeLegislationStartQuestion}" in {
            document.title shouldBe messages.Improvements.ownerBeforeLegislationStartQuestion
          }

          s"has the heading of ${messages.Improvements.ownerBeforeLegislationStartQuestion}" in {
            document.body().getElementsByTag("h1").text shouldBe messages.Improvements.ownerBeforeLegislationStartQuestion
          }

          "have a legend that" should {

            lazy val legend = document.select("legend")

            s"have the text ${messages.Improvements.ownerBeforeLegislationStartQuestion}" in {
              legend.text shouldEqual messages.Improvements.ownerBeforeLegislationStartQuestion
            }
          }
        }
      }
    }

    "supplied with errors" should {
      lazy val form = improvementsForm(true).bind(Map("improvements" -> "testData"))
      lazy val view = improvements(form, improvementsOptions = true, "back-link", ownerBeforeLegislationStart = false)(fakeRequest, mockMessage, fakeApplication, mockMessagesApi, mockConfig)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
