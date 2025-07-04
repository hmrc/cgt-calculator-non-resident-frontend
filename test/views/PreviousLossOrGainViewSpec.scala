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

import assets.MessageLookup.NonResident.{PreviousLossOrGain => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.PreviousLossOrGainForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.previousLossOrGain

class PreviousLossOrGainViewSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val previousLossOrGainView: previousLossOrGain = fakeApplication.injector.instanceOf[previousLossOrGain]

  "The PreviousLossOrGain view" should {

    lazy val view = previousLossOrGainView(previousLossOrGainForm())(using fakeRequest, mockMessage)
    lazy val document = Jsoup.parse(view.body)

    "return some HTML" which {
      s"has the title ${messages.title}" in {
        document.title shouldEqual messages.title
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-large" in {
          heading.attr("class") shouldBe "govuk-heading-xl"
        }
      }
    }

    s"has guidance that includes the text '${messages.hintOne}'" in {
      document.select("main p").get(0).text() shouldBe messages.hintOne
    }

    s"has guidance that includes the text '${messages.hintTwo}'" in {
      document.select("main p").get(1).text() shouldBe messages.hintTwo
    }

    "have a legend that" should {
      lazy val label = document.body.select("h1")

      s"have the text of ${messages.question}" in {
        label.text shouldBe messages.question
      }

    }

    "have a back button that" should {
      lazy val backLink = document.select("a.govuk-back-link")

      "have the correct back link text" in {
        backLink.text shouldBe commonMessages.back
      }

      "have the back-link class" in {
        backLink.hasClass("govuk-back-link") shouldBe true
      }

      "have a link to Other Properties" in {
        backLink.attr("href") shouldBe "#"
      }
    }

    "has a form" which {
      lazy val form = document.getElementsByTag("form")

      s"has the action '${controllers.routes.PreviousGainOrLossController.submitPreviousGainOrLoss.toString}" in {
        form.attr("action") shouldBe controllers.routes.PreviousGainOrLossController.submitPreviousGainOrLoss.toString()
      }

      "has the method of POST" in {
        form.attr("method") shouldBe "POST"
      }
    }

    "has a series of options that" should {
      s"have a label for the Loss option" in {
        document.select("label").get(0).text() shouldEqual messages.loss
      }
      s"have a label for the Gain option" in {
        document.select("label").get(1).text() shouldEqual messages.gain
      }
      s"have a label for the Neither option" in {
        document.select("label").get(2).text() shouldEqual messages.neither
      }
    }

    "has a continue button that" should {
      lazy val continueButton = document.select("button#submit")

      s"have the button text '${commonMessages.continue}" in {
        continueButton.text shouldBe commonMessages.continue
      }

      "have the class 'button'" in {
        continueButton.hasClass("govuk-button") shouldBe true
      }
    }

    "should produce the same output when render and f are called" in {
      previousLossOrGainView.f(previousLossOrGainForm())(fakeRequest, mockMessage) shouldBe previousLossOrGainView.render(previousLossOrGainForm(), fakeRequest, mockMessage)
    }
  }

  "PreviousLossOrGainView with form errors" should {
    lazy val form = previousLossOrGainForm().bind(Map("previousLossOrGain" -> ""))
    lazy val view = previousLossOrGainView(form)(using fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message regarding incorrect value being inputted" in {
      doc.getElementsByClass("govuk-error-summary").size shouldBe 1
    }
  }

}
