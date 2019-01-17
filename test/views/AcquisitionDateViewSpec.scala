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

import java.time.LocalDate

import assets.MessageLookup.{NonResident => messages}
import controllers.helpers.FakeRequestHelper
import forms.AcquisitionDateForm._
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.acquisitionDate

class AcquisitionDateViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {


  "Acquisition date view" when {

    "supplied with no errors" should {
      lazy val view = acquisitionDate(acquisitionDateForm)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '${messages.AcquisitionDate.question}'" in {
        document.title shouldBe messages.AcquisitionDate.question
      }

      "have a back link" which {

        "should have the text" in {
          document.body.getElementById("back-link").text shouldEqual messages.back
        }

        s"should have a route to 'back-link'" in {
          document.body.getElementById("back-link").attr("href") shouldEqual controllers.routes.DisposalCostsController.disposalCosts().url
        }
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate().url
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

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

        s"has an action of '${controllers.routes.AcquisitionDateController.submitAcquisitionDate().url}'" in {
          form.attr("action") shouldBe controllers.routes.AcquisitionDateController.submitAcquisitionDate().url
        }


        s"have the hintText '${messages.AcquisitionDate.hintText}'" in {
          document.select(".form-hint").first().text.stripSuffix(" ") shouldBe messages.AcquisitionDate.hintText
        }
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
    }

    "supplied with an invalid date error day" should {
      lazy val form = acquisitionDateForm.bind(Map("acquisitionDateDay" -> "",
        "acquisitionDateMonth" -> "1",
        "acquisitionDateYear" -> "2015"))
      lazy val view = acquisitionDate(form)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" which {

        "has size 1" in {
          document.select("#error-summary-display").size() shouldBe 1
        }

        s"has the text ${messages.errorInvalidDay}" in {
          document.select("#acquisitionDateDay-error-summary").text() shouldBe messages.errorInvalidDay
        }
      }
    }

    "supplied with an invalid date error month" should {
      lazy val form = acquisitionDateForm.bind(Map("acquisitionDateDay" -> "1",
        "acquisitionDateMonth" -> "",
        "acquisitionDateYear" -> "2015"))
      lazy val view = acquisitionDate(form)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" which {

        "has size 1" in {
          document.select("#error-summary-display").size() shouldBe 1
        }

        s"has the text ${messages.errorInvalidMonth}" in {
          document.select("#acquisitionDateMonth-error-summary").text() shouldBe messages.errorInvalidMonth
        }
      }
    }

    "supplied with an invalid date error year" should {
      lazy val form = acquisitionDateForm.bind(Map("acquisitionDateDay" -> "1",
        "acquisitionDateMonth" -> "1",
        "acquisitionDateYear" -> ""))
      lazy val view = acquisitionDate(form)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" which {

        "has size 1" in {
          document.select("#error-summary-display").size() shouldBe 1
        }

        s"has the text ${messages.errorInvalidYear}" in {
          document.select("#acquisitionDateYear-error-summary").text() shouldBe messages.errorInvalidYear
        }
      }
    }

    "supplied with a future date error" should {
      val date: LocalDate = LocalDate.now().plusDays(1)
      lazy val map = Map(
        "acquisitionDateDay" -> date.getDayOfMonth.toString,
        "acquisitionDateMonth" -> date.getMonthValue.toString,
        "acquisitionDateYear" -> date.getYear.toString)

      lazy val form = acquisitionDateForm.bind(map)
      lazy val view = acquisitionDate(form)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" which {

        "has size 1" in {
          document.select("#error-summary-display").size() shouldBe 1
        }

        s"has the text ${messages.AcquisitionDate.errorFutureDate}" in {
          document.select("#acquisitionDateDay-error-summary").text() shouldBe messages.AcquisitionDate.errorFutureDate
        }
      }
    }
  }
}
