/*
 * Copyright 2019 HM Revenue & Customs
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
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.CurrentIncomeForm._
import org.jsoup.Jsoup
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.currentIncome

class CurrentIncomeViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  "Current Income view" when {

    "supplied with no errors" should {
      lazy val view = currentIncome(currentIncomeForm, "google.com")(fakeRequest, mockMessage, fakeApplication, mockConfig)
      lazy val document = Jsoup.parse(view.body)

      s"have the correct title" in {
        document.title() shouldBe messages.CurrentIncome.question
      }

      "have a dynamically provided back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has the text" in {
          backLink.text shouldBe messages.back
        }

        s"has a dynamic back link to 'google.com'" in {
          backLink.attr("href") shouldBe "google.com"
        }
      }

      s"have a home link to the disposal date view'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate().url
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
        document.body.select("label div").first().text shouldBe messages.CurrentIncome.question
      }

      s"have the correct hint text" in {
        document.body.select("span.form-hint").text() shouldBe messages.CurrentIncome.helpText
      }

      "have an input with the correct id" in {
        document.body().select("input").attr("id") shouldBe "currentIncome"
      }

      "have a button" which {
        lazy val button = document.select("button")

        "has the type 'submit'" in {
          button.attr("type") shouldBe "submit"
        }

        "has the id 'continue-button'" in {
          button.attr("id") shouldBe "continue-button"
        }
      }

      "should produce the same output when render and f are called" in {
        currentIncome.f(currentIncomeForm, "google.com")(fakeRequest, mockMessage, fakeApplication, mockConfig) shouldBe currentIncome.render(currentIncomeForm, "google.com", fakeRequest, mockMessage, fakeApplication, mockConfig)
      }
    }

    "supplied with errors" should {
      lazy val form = currentIncomeForm.bind(Map("currentIncome" -> "a"))
      lazy val view = currentIncome(form, "")(fakeRequest, mockMessage, fakeApplication, mockConfig)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
