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

import assets.MessageLookup.{NonResident => commonMessages}
import assets.MessageLookup.NonResident.{CurrentIncome => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.CurrentIncomeForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.currentIncome

class CurrentIncomeViewSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  val currentIncomeView = fakeApplication.injector.instanceOf[currentIncome]
  val pageTitle = s"${messages.question} - ${commonMessages.pageHeading} - GOV.UK"


  "Current Income view" when {

    "supplied with no errors" should {
      lazy val view = currentIncomeView(currentIncomeForm)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      s"have the correct title" in {
        document.title() shouldBe pageTitle
      }

      "have a dynamically provided back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has the text" in {
          backLink.text shouldBe commonMessages.back
        }

        s"has a dynamic back link 'javascript:history.back()'" in {
          backLink.attr("href") shouldBe "javascript:history.back()"
        }
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has a the correct action" in {
          form.attr("action") shouldBe controllers.routes.CurrentIncomeController.submitCurrentIncome().url
        }
      }

      s"have the correct question" in {
        document.body.select("h1").first().text shouldBe messages.question
      }

      s"have the correct body text" in {
        document.body.select("p.govuk-body").text() shouldBe messages.helpText
      }

      "have an input with the correct id" in {
        document.body().select("input").attr("id") shouldBe "currentIncome"
      }

      "have a button" which {
        lazy val button = document.select("button")

        "has the id 'continue-button'" in {
          button.attr("id") shouldBe "submit"
        }
      }

      "should produce the same output when render and f are called" in {
        currentIncomeView.f(currentIncomeForm)(fakeRequest, mockMessage) shouldBe currentIncomeView.render(currentIncomeForm, fakeRequest, mockMessage)
      }
    }

    "supplied with errors" should {
      lazy val form = currentIncomeForm.bind(Map("currentIncome" -> "a"))
      lazy val view = currentIncomeView(form)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select(".govuk-error-summary").size() shouldBe 1
      }
    }
  }
}
