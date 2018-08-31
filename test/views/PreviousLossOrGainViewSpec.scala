/*
 * Copyright 2018 HM Revenue & Customs
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
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import forms.PreviousLossOrGainForm._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.previousLossOrGain
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class PreviousLossOrGainViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {



  "The PreviousLossOrGain view" should {

    lazy val view = previousLossOrGain(previousLossOrGainForm)
    lazy val document = Jsoup.parse(view.body)

    "return some HTML" which {
      s"has the title ${messages.question}" in {
        document.title shouldEqual messages.question
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-large" in {
          heading.attr("class") shouldBe "heading-xlarge"
        }
      }
    }

    s"has guidance that includes the text '${messages.hintOne}'" in {
      document.select("article p").get(0).text() shouldBe messages.hintOne
    }

    s"has guidance that includes the text '${messages.hintTwo}'" in {
      document.select("article p").get(1).text() shouldBe messages.hintTwo
    }

    "have a legend that" should {
      lazy val legend = document.body.select("legend")

      s"have the text of ${messages.question}" in {
        legend.text shouldBe messages.question
      }

      "be visually hidden" in {
        legend.hasClass("visuallyhidden") shouldEqual true
      }
    }

    "have a back button that" should {
      lazy val backLink = document.select("a#back-link")

      "have the correct back link text" in {
        backLink.text shouldBe commonMessages.back
      }

      "have the back-link class" in {
        backLink.hasClass("back-link") shouldBe true
      }

      "have a link to Other Properties" in {
        backLink.attr("href") shouldBe controllers.routes.OtherPropertiesController.otherProperties().url
      }
    }

    "has a form" which {
      lazy val form = document.getElementsByTag("form")

      s"has the action '${controllers.routes.PreviousGainOrLossController.submitPreviousGainOrLoss().toString}" in {
        form.attr("action") shouldBe controllers.routes.PreviousGainOrLossController.submitPreviousGainOrLoss().toString()
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
      lazy val continueButton = document.select("button#continue-button")

      s"have the button text '${commonMessages.continue}" in {
        continueButton.text shouldBe commonMessages.continue
      }

      "be of type submit" in {
        continueButton.attr("type") shouldBe "submit"
      }

      "have the class 'button'" in {
        continueButton.hasClass("button") shouldBe true
      }
    }
  }

  "PreviousLossOrGainView with form errors" should {
    lazy val form = previousLossOrGainForm.bind(Map("previousLossOrGain" -> ""))
    lazy val view = previousLossOrGain(form)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message regarding incorrect value being inputted" in {
      doc.body.select(".form-group .error-notification").size shouldBe 1
    }
  }

}
