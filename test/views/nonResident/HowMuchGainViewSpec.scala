/*
 * Copyright 2016 HM Revenue & Customs
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

package views.nonResident

import assets.MessageLookup.{NonResident => messages}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.nonresident.howMuchGain
import forms.nonresident.HowMuchGainForm._
import models.nonresident.HowMuchGainModel

class HowMuchGainViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  "How Much Gain view" when {

    "supplied with no errors" should {
      lazy val view = howMuchGain(howMuchGainForm)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '${messages.HowMuchGain.question}'" in {
        document.title() shouldBe messages.HowMuchGain.question
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has the text" in {
          backLink.text shouldBe messages.back
        }

        "has the class back-link" in {
          backLink.attr("class") shouldBe "back-link"
        }

        s"has a route to 'previous-gain-or-loss'" in {
          backLink.attr("href") shouldBe controllers.nonresident.routes.PreviousGainOrLossController.previousGainOrLoss().url
        }
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-xlarge" in {
          heading.attr("class") shouldBe "heading-xlarge"
        }

        s"has the text '${messages.HowMuchGain.question}'" in {
          heading.text shouldBe messages.HowMuchGain.question
        }
      }

      s"have a home link to '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
      }

      s"have a label" which {

        lazy val label = document.select("label span").first()

        s"has the question '${messages.HowMuchGain.question}'" in {
          label.text shouldBe messages.HowMuchGain.question
        }

        "has the class visuallyhidden" in {
          label.hasClass("visuallyhidden") shouldEqual true
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

        s"has an action of '${controllers.nonresident.routes.HowMuchGainController.submitHowMuchGain().url}'" in {
          form.attr("action") shouldBe controllers.nonresident.routes.HowMuchGainController.submitHowMuchGain().url
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

    "supplied with a form with errors" should {
      lazy val form = howMuchGainForm.bind(Map("howMuchGain" -> "testData"))
      lazy val view = howMuchGain(form)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
