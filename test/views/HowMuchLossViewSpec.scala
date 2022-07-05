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

import assets.MessageLookup.{NonResident => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.HowMuchLossForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.howMuchLoss

class HowMuchLossViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val howMuchLossView = fakeApplication.injector.instanceOf[howMuchLoss]
  val pageTitle = s"""${messages.HowMuchLoss.question} - ${messages.pageHeading} - GOV.UK"""

  "Previous Loss view" when {

    "provided with no errors" should {
      lazy val view = howMuchLossView(howMuchLossForm)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of ${pageTitle}" in {
        document.title shouldBe pageTitle
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has the text" in {
          backLink.text shouldBe messages.back
        }

        "has the class back-link" in {
          backLink.attr("class") shouldBe "govuk-back-link"
        }

        s"has a route to 'Previous Gain Or Loss'" in {
          backLink.attr("href") shouldBe "javascript:history.back()"
        }
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-xlarge" in {
          heading.attr("class") shouldBe "govuk-heading-xl"
        }

        s"has the text '${messages.HowMuchLoss.question}'" in {
          heading.text shouldBe messages.HowMuchLoss.question
        }
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.HowMuchLossController.submitHowMuchLoss.url}'" in {
          form.attr("action") shouldBe controllers.routes.HowMuchLossController.submitHowMuchLoss.url
        }
      }

      s"have a label" which {

        lazy val label = document.body().select("h1")

        s"has the question '${messages.HowMuchLoss.question}'" in {
          label.text shouldBe messages.HowMuchLoss.question
        }
      }

      "have an input with the id 'loss" in {
        document.body().select("input").attr("id") shouldBe "loss"
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
        howMuchLossView.f(howMuchLossForm)(fakeRequest, mockMessage) shouldBe howMuchLossView.render(howMuchLossForm, fakeRequest, mockMessage)
      }
    }

    "provided with some errors" should {
      lazy val form = howMuchLossForm.bind(Map("loss" -> ""))
      lazy val view = howMuchLossView(form)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.getElementsByClass("govuk-error-summary").size() shouldBe 1
      }
    }
  }
}
