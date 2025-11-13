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
import forms.RebasedCostsForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.rebasedCosts

class RebasedCostsViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {
  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val rebasedCostsView: rebasedCosts = fakeApplication.injector.instanceOf[rebasedCosts]

  "The rebased value view" when {

    "not supplied with a pre-existing stored model" should {
      lazy val view = rebasedCostsView(rebasedCostsForm)(using fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      s"Have the title ${messages.RebasedCosts.question}" in {
        document.title shouldEqual messages.RebasedCosts.title
      }

      "have a back link" which {
        lazy val backLink = document.body().select(".govuk-back-link")

        "has the text" in {
          backLink.text shouldBe messages.back
        }

        "has the class 'back-link'" in {
          backLink.attr("class") shouldBe "govuk-back-link"
        }

        s"has a route to 'rebased-value'" in {
          backLink.attr("href") shouldBe "#"
        }
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading" in {
          heading.attr("class") shouldBe "govuk-fieldset__heading"
        }
        "has a class - legend " in {
          document.body().select("legend").attr("class") shouldBe "govuk-fieldset__legend govuk-fieldset__legend--l"
        }

        s"has the text '${messages.RebasedCosts.question}'" in {
          heading.text shouldBe messages.RebasedCosts.question
        }
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate.url}'" in {
        document.select("body > header > section > div > div > span.govuk-service-navigation__service-name > a").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate.url
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.RebasedCostsController.rebasedCosts.url}'" in {
          form.attr("action") shouldBe controllers.routes.RebasedCostsController.rebasedCosts.url
        }
      }

      s"have the question '${messages.RebasedCosts.question}'" in {
        document.body.select("legend").first().text shouldBe messages.RebasedCosts.question
      }

      "have option inputs with id 'hasRebasedCosts'" in {
        document.body().select("input[type=radio]").attr("id") should include("hasRebasedCosts")
      }

      s"have the input question '${messages.RebasedCosts.inputQuestion}'" in {
        document.body().select("#conditional-hasRebasedCosts > div > label").first().text() shouldBe messages.RebasedCosts.inputQuestion
      }

      s"have joint ownership text of ${messages.RebasedCosts.jointOwnership}" in {
        document.body().select("#rebasedCosts-hint").text() shouldBe messages.RebasedCosts.jointOwnership
      }

      "have a value input with the id 'rebasedCosts'" in {
        document.select("#rebasedCosts").attr("id") should include("rebasedCosts")
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
        rebasedCostsView.f(rebasedCostsForm)(fakeRequest, mockMessage) shouldBe rebasedCostsView.render(rebasedCostsForm, fakeRequest, mockMessage)
      }
    }

    "supplied with errors" should {
      lazy val form = rebasedCostsForm.bind(Map(
        "hasRebasedCosts" -> "Yes",
        "rebasedCosts" -> ""
      ))
      lazy val view = rebasedCostsView(form)(using fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select(".govuk-error-summary").size() shouldBe 1
      }
    }
  }
}
