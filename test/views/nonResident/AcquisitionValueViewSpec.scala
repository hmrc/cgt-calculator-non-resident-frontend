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

import controllers.nonresident.routes
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import forms.nonresident.AcquisitionValueForm._
import assets.MessageLookup.{NonResident => commonMessages}
import assets.MessageLookup.NonResident.{AcquisitionValue => messages}
import controllers.helpers.FakeRequestHelper
import views.html.calculation.{nonresident => views}


class AcquisitionValueViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper{


  "the Acquisition Value View" should {

    lazy val view = views.acquisitionValue(acquisitionValueForm)(fakeRequest)
    lazy val document = Jsoup.parse(view.body)

    "have a h1 tag that" should {

      s"have the heading ${messages.question}" in {
        document.select("h1").text shouldEqual messages.question
      }

      "have the heading-large class" in {
        document.select("h1").hasClass("heading-xlarge") shouldBe true
      }
    }

    s"have a home link to '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
      document.select("#homeNavHref").attr("href") shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
    }

    "have a 'Back link' that" should{

      s"have the text of ${commonMessages.back}" in {
        document.select("a#back-link").text shouldEqual commonMessages.back
      }

      s"have a link to ${routes.BoughtForLessController.boughtForLess().url}" in {
       document.select("a#back-link").attr("href") shouldEqual routes.BoughtForLessController.boughtForLess().url
      }

      "has the back-link class" in {
        document.select("a#back-link").hasClass("back-link") shouldBe true
      }
    }

    "render a form tag" which {

      lazy val form = document.select("form")

      "has a submit action" in {
        form.attr("action") shouldEqual "/calculate-your-capital-gains/non-resident/acquisition-value"
      }

      "with method type POST" in {
        form.attr("method") shouldBe "POST"
      }
    }

    "display an input box for the Acquisition Value" in {
      document.select("input").attr("id") shouldBe "acquisitionValue"
    }

    "have a button that" should {
      lazy val button = document.select("button")

      s"have the message of ${commonMessages.continue}" in {
        button.text shouldEqual commonMessages.continue
      }

      "have the class 'button'" in {
        button.attr("class") shouldBe "button"
      }

      "have the type 'submit'" in {
        button.attr("type") shouldBe "submit"
      }

      "have the id 'continue-button'" in {
        button.attr("id") shouldBe "continue-button"
      }
    }

    "supplied with errors" should {
      lazy val form = acquisitionValueForm.bind(Map("acquisitionValue" -> "a"))
      lazy val view = views.acquisitionValue(form)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}