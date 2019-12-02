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

import assets.MessageLookup.NonResident.{CalculationElectionNoReliefs => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.CalculationElectionForm._
import org.jsoup.Jsoup
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.calculationElectionNoReliefs

class CalculationElectionNoReliefsViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  "Calculation Election No Reliefs View" when {

    lazy val rebasedLowestTaxOwed: Seq[(String, String, String, String, Option[String], Option[BigDecimal])] =
      Seq(
        ("rebased", "0", "description", Messages("calc.calculationElection.description.rebased"), None, None),
        ("flat", "1000", "description", Messages("calc.calculationElection.description.flat"), None, None),
        ("time", "2000", "description", Messages("calc.calculationElection.description.time"), None, None)
      )

    "supplied with no errors and lowest tax owed is rebased method" should {

      lazy val view = calculationElectionNoReliefs(calculationElectionForm, rebasedLowestTaxOwed, "back-link")(fakeRequest, mockMessage, fakeApplication, mockConfig)
      lazy val doc = Jsoup.parse(view.body)

      "have a heading" which {

        s"has the text ${messages.title}" in {
          doc.title shouldBe messages.title
        }
      }

      "have a h1" which {

        s"has the text ${messages.title}" in {
          doc.select("h1").text shouldBe messages.title
        }
      }

      "have a back link" which {
        lazy val back = doc.select("#back-link")

        s"has the text ${commonMessages.back}" in {
          back.text() shouldBe commonMessages.back
        }

        s"has a link to 'back-link'" in {
          back.attr("href") shouldBe "back-link"
        }
      }

      "have text in paragraphs" which {

        lazy val helpText = doc.select("#help-text")

        s"contains the text ${messages.helpText}" in {
          helpText.select("p").get(0).text should include(messages.helpText)
        }

        s"contains the text ${messages.helpTextMethodType(messages.rebasing)}" in {
          helpText.select("p").get(1).text should include(messages.helpTextMethodType(messages.rebasing))
        }
      }

      "has a form" which {

        lazy val form = doc.select("form")

        "has a submit action" in {
          form.attr("action") shouldEqual "/calculate-your-capital-gains/non-resident/calculation-election"
        }

        "has method type POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"contains the text ${messages.helpTextChooseMethod}" in {
          form.select("p").text should include(messages.helpTextChooseMethod)
        }

        "contains inputs" which {

          lazy val input = form.select("input")

          "has the first element calculationElection-rebased'" in {
            input.get(0).attr("id") shouldBe "calculationElection-rebased"
          }

          "has the second element calculationElection-flat'" in {
            input.get(1).attr("id") shouldBe "calculationElection-flat"
          }

          "has the third element calculationElection-time'" in {
            input.get(2).attr("id") shouldBe "calculationElection-time"
          }
        }
      }
    }

    "supplied with no errors and lowest tax owed is flat method" should {

      lazy val flatLowestTaxOwed: Seq[(String, String, String, String, Option[String], Option[BigDecimal])] =
        Seq(
          ("flat", "0", "description", Messages("calc.calculationElection.description.flat"), None, None),
          ("rebased", "1000", "description", Messages("calc.calculationElection.description.rebased"), None, None),
          ("time", "2000", "description", Messages("calc.calculationElection.description.time"), None, None)
        )

      lazy val view = calculationElectionNoReliefs(calculationElectionForm, flatLowestTaxOwed, "back-link")(fakeRequest, mockMessage, fakeApplication, mockConfig)
      lazy val doc = Jsoup.parse(view.body)

      "have text in a paragraph" which {

        lazy val helpText = doc.select("#help-text")

        s"contains the text ${messages.helpTextMethodType(messages.flatGain)}" in {
          helpText.text should include(messages.helpTextMethodType(messages.flatGain))
        }
      }

      "has a form" which {

        lazy val form = doc.select("form")

        "contains inputs" which {

          lazy val input = form.select("input")

          "has the first element calculationElection-flat'" in {
            input.get(0).attr("id") shouldBe "calculationElection-flat"
          }

          "has the second element calculationElection-rebased'" in {
            input.get(1).attr("id") shouldBe "calculationElection-rebased"
          }

          "has the third element calculationElection-time'" in {
            input.get(2).attr("id") shouldBe "calculationElection-time"
          }
        }
      }
    }

    "supplied with errors" should {
      lazy val form = calculationElectionForm.bind(Map("calculationElection" -> "a"))
      lazy val view = calculationElectionNoReliefs(form, rebasedLowestTaxOwed, "back-link")(fakeRequest, mockMessage, fakeApplication, mockConfig)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }

    "should produce the same output when render and f are called" in {

      lazy val flatLowestTaxOwed: Seq[(String, String, String, String, Option[String], Option[BigDecimal])] =
        Seq(
          ("flat", "0", "description", Messages("calc.calculationElection.description.flat"), None, None),
          ("rebased", "1000", "description", Messages("calc.calculationElection.description.rebased"), None, None),
          ("time", "2000", "description", Messages("calc.calculationElection.description.time"), None, None)
        )

      calculationElectionNoReliefs.f(calculationElectionForm, flatLowestTaxOwed, "back-link")(fakeRequest, mockMessage, fakeApplication, mockConfig) shouldBe calculationElectionNoReliefs.render(calculationElectionForm, flatLowestTaxOwed, "back-link", fakeRequest, mockMessage, fakeApplication, mockConfig)
    }
  }
}
