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

import assets.MessageLookup.{NonResident => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.AcquisitionDateForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.acquisitionDate

import java.time.LocalDate

class AcquisitionDateViewSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  lazy val acquisitionCostsView: acquisitionDate = fakeApplication.injector.instanceOf[acquisitionDate]
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  "Acquisition date view" when {

    "supplied with no errors" should {
      lazy val view = acquisitionCostsView(acquisitionDateForm)(fakeRequest,mockMessage)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '${messages.AcquisitionDate.question}'" in {
        document.title shouldBe s"${messages.AcquisitionDate.question} - Calculate your Non-Resident Capital Gains Tax - GOV.UK"
      }

      "have a back link" which {

        "should have the text" in {
          document.body.getElementsByClass("govuk-back-link").text shouldEqual messages.back
        }

        s"should have a route to 'back-link'" in {
          document.body.getElementsByClass("govuk-back-link").attr("href") shouldEqual "#"
        }
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate.url}'" in {
        document.getElementsByClass("hmrc-header__service-name hmrc-header__service-name--linked").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate.url
      }

      s"have a heading of '${messages.AcquisitionDate.question}'" in {
        document.select("h1").text() shouldBe messages.AcquisitionDate.question
      }

      "have a legend that" should {

        lazy val legend = document.body.select("legend")

        s"have the question '${messages.AcquisitionDate.question}'" in {
          legend.text.stripSuffix(" ") shouldBe messages.AcquisitionDate.question
        }

        "be visually hidden" in {
          legend.hasClass("govuk-visually-hidden") shouldEqual true
        }
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.AcquisitionDateController.submitAcquisitionDate.url}'" in {
          form.attr("action") shouldBe controllers.routes.AcquisitionDateController.submitAcquisitionDate.url
        }


        s"have the hintText '${messages.AcquisitionDate.hintText}'" in {
          document.select("#main-content > div > div > p").first().text.stripSuffix(" ") shouldBe messages.AcquisitionDate.hintText
        }
      }

      "have a button" which {
        lazy val button = document.select("button")


        "has the id 'submit'" in {
          button.attr("id") shouldBe "submit"
        }
      }

      "produce the same output when render and f are called" in {
        acquisitionCostsView.render(acquisitionDateForm, fakeRequest,mockMessage) shouldBe
          acquisitionCostsView(acquisitionDateForm)(fakeRequest,mockMessage)
      }
    }

    "supplied with an invalid date error day" should {
      lazy val form = acquisitionDateForm.bind(Map("acquisitionDate.day" -> "",
        "acquisitionDate.month" -> "1",
        "acquisitionDate.year" -> "2015"))
      lazy val view = acquisitionCostsView(form)(fakeRequest,mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" which {

        "has size 1" in {
          document.getElementsByClass("govuk-error-summary").size() shouldBe 1
        }

        s"has the text ${messages.AcquisitionDate.errorRequiredDay}" in {
          document.getElementById("acquisitionDate-error").text() shouldBe s"Error: ${messages.AcquisitionDate.errorRequiredDay}"
        }
      }
    }

    "supplied with an invalid date error month" should {
      lazy val form = acquisitionDateForm.bind(Map("acquisitionDate.day" -> "1",
        "acquisitionDate.month" -> "",
        "acquisitionDate.year" -> "2015"))
      lazy val view = acquisitionCostsView(form)(fakeRequest,mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" which {

        "has size 1" in {
          document.getElementsByClass("govuk-error-summary").size() shouldBe 1
        }

        s"has the text ${messages.AcquisitionDate.errorRequiredMonth}" in {
          document.getElementById("acquisitionDate-error").text() shouldBe s"Error: ${messages.AcquisitionDate.errorRequiredMonth}"
        }
      }
    }

    "supplied with an invalid date error year" should {
      lazy val form = acquisitionDateForm.bind(Map("acquisitionDate.day" -> "1",
        "acquisitionDate.month" -> "1",
        "acquisitionDate.year" -> ""))
      lazy val view = acquisitionCostsView(form)(fakeRequest,mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" which {

        "has size 1" in {
          document.getElementsByClass("govuk-error-summary").size() shouldBe 1
        }

        s"has the text ${messages.AcquisitionDate.errorRequiredYear}" in {
          document.getElementById("acquisitionDate-error").text() shouldBe s"Error: ${messages.AcquisitionDate.errorRequiredYear}"
        }
      }
    }

    "supplied with a future date error" should {
      val date: LocalDate = LocalDate.now().plusDays(1)
      lazy val map = Map(
        "acquisitionDate.day" -> date.getDayOfMonth.toString,
        "acquisitionDate.month" -> date.getMonthValue.toString,
        "acquisitionDate.year" -> date.getYear.toString)

      lazy val form = acquisitionDateForm.bind(map)
      lazy val view = acquisitionCostsView(form)(fakeRequest,mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" which {

        "has size 1" in {
          document.getElementsByClass("govuk-error-summary").size() shouldBe 1
        }

        s"has the text ${messages.AcquisitionDate.errorFutureDate}" in {
          document.getElementsByClass("govuk-list govuk-error-summary__list").text() shouldBe messages.AcquisitionDate.errorFutureDate
        }
      }
    }
  }
}
