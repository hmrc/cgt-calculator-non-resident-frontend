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

import controllers.helpers.FakeRequestHelper
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.nonresident.acquisitionDate
import forms.nonresident.AcquisitionDateForm._
import org.jsoup.Jsoup
import assets.MessageLookup.{NonResident => messages}

class AcquisitionDateViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  "Acquisition date view" when {

    "supplied with no errors" should {
      lazy val view = acquisitionDate(acquisitionDateForm)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '${messages.AcquisitionDate.question}'" in {
        document.title shouldBe messages.AcquisitionDate.question
      }

      "have a back link" which {

        "should have the text" in {
          document.body.getElementById("back-link").text shouldEqual messages.back
        }

        s"should have a route to 'back-link'" in {
          document.body.getElementById("back-link").attr("href") shouldEqual controllers.nonresident.routes.DisposalCostsController.disposalCosts().url
        }
      }

      s"have a home link to '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-xlarge" in {
          heading.attr("class") shouldBe "heading-xlarge"
        }

        s"has the text '${messages.AcquisitionDate.question}'" in {
          heading.text shouldBe messages.AcquisitionDate.question
        }
      }

      "have a legend that" should {

        lazy val legend = document.body.select("legend")

        s"have the question '${messages.AcquisitionDate.question}'" in {
          legend.text.stripSuffix(" ") shouldBe messages.AcquisitionDate.question
        }

        "be visually hidden" in {
          legend.hasClass("visuallyhidden") shouldEqual true
        }
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.nonresident.routes.AcquisitionDateController.submitAcquisitionDate().url}'" in {
          form.attr("action") shouldBe controllers.nonresident.routes.AcquisitionDateController.submitAcquisitionDate().url
        }
      }

      "have inputs using the id 'hasAcquisitionDate'" in {
        document.body().select("input[type=radio]").attr("id") should include ("hasAcquisitionDate")
      }

      "have inputs using the id acquisitionDate" in {
        document.body().select("input[type=number]").attr("id") should include ("acquisitionDate")
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

    "supplied with errors" should {
      lazy val form = acquisitionDateForm.bind(Map("hasAcquisitionDate" -> "Yes"))
      lazy val view = acquisitionDate(form)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
