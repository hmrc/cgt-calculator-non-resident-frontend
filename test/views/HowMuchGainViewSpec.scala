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

import assets.MessageLookup.{NonResident => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.HowMuchGainForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.howMuchGain

class HowMuchGainViewSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  lazy val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val howMuchGainView = fakeApplication.injector.instanceOf[howMuchGain]

  "How Much Gain view" should {

    "supplied with no errors" when {
      lazy val view = howMuchGainView(howMuchGainForm)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)
      val pageTitle = s"""${messages.HowMuchGain.question} - ${messages.pageHeading} - GOV.UK"""

      s"have a title of '${pageTitle}'" in {
        document.title() shouldBe pageTitle
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has the text" in {
          backLink.text shouldBe messages.back
        }

        "has the class back-link" in {
          backLink.attr("class") shouldBe "govuk-back-link"
        }

        s"has a route to 'previous-gain-or-loss'" in {
          backLink.attr("href") shouldBe "javascript:history.back()"
        }
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-xlarge" in {
          heading.attr("class") shouldBe "govuk-heading-xl"
        }

        s"has the text '${messages.HowMuchGain.question}'" in {
          heading.text shouldBe messages.HowMuchGain.question
        }
      }

      s"have a label" which {

        lazy val label = document.body().select("h1")

        s"has the question '${messages.HowMuchGain.question}'" in {
          label.text shouldBe messages.HowMuchGain.question
        }
      }

      "have an input with the id 'howMuchGain" in {
        document.body().select("input").attr("id") shouldBe "howMuchGain"
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.HowMuchGainController.submitHowMuchGain.url}'" in {
          form.attr("action") shouldBe controllers.routes.HowMuchGainController.submitHowMuchGain.url
        }
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
        howMuchGainView.f(howMuchGainForm)(fakeRequest, mockMessage) shouldBe howMuchGainView.render(howMuchGainForm, fakeRequest, mockMessage)
      }
    }

    "supplied with a form with errors" should {
      lazy val form = howMuchGainForm.bind(Map("howMuchGain" -> "testData"))
      lazy val view = howMuchGainView(form)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.getElementsByClass("govuk-error-summary").size() shouldBe 1
      }
    }
  }
}
