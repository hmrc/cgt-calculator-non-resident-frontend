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

import assets.MessageLookup.NonResident.{CalculationElectionNoReliefs => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.CalculationElectionForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.calculationElectionNoReliefs

class CalculationElectionNoReliefsViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val calculationElectionNoReliefsView = fakeApplication.injector.instanceOf[calculationElectionNoReliefs]
  lazy val pageTitle = s"""${messages.title} - ${commonMessages.pageHeading} - GOV.UK"""


  "Calculation Election No Reliefs View" when {

    lazy val rebasedLowestTaxOwed: Seq[(String, String, String, String, Option[String], Option[BigDecimal])] =
      Seq(
        ("rebased", "0", "description", Messages("calc.calculationElection.description.rebased"), None, None),
        ("flat", "1000", "description", Messages("calc.calculationElection.description.flat"), None, None),
        ("time", "2000", "description", Messages("calc.calculationElection.description.time"), None, None)
      )

    "supplied with no errors and lowest tax owed is rebased method" should {

      lazy val view = calculationElectionNoReliefsView(calculationElectionForm, rebasedLowestTaxOwed)(fakeRequest, mockMessage)
      lazy val doc = Jsoup.parse(view.body)

      "have a heading" which {

        s"has the text $pageTitle" in {
          doc.title shouldBe pageTitle
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

        s"has a link to 'javascript:history.back()'" in {
          back.attr("href") shouldBe "javascript:history.back()"
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

        "contains inputs" which {

          lazy val input = form.select("input")

          "has the first element calculationElection-rebased'" in {
            input.get(0).attr("id") shouldBe "calculationElection"
          }

          "has the second element calculationElection-flat'" in {
            input.get(1).attr("id") shouldBe "calculationElection-2"
          }

          "has the third element calculationElection-time'" in {
            input.get(2).attr("id") shouldBe "calculationElection-3"
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

      lazy val view = calculationElectionNoReliefsView(calculationElectionForm, flatLowestTaxOwed)(fakeRequest, mockMessage)
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
            input.get(0).attr("id") shouldBe "calculationElection"
          }

          "has the second element calculationElection-rebased'" in {
            input.get(1).attr("id") shouldBe "calculationElection-2"
          }

          "has the third element calculationElection-time'" in {
            input.get(2).attr("id") shouldBe "calculationElection-3"
          }
        }
      }
    }

    "supplied with errors" should {
      lazy val form = calculationElectionForm.bind(Map("calculationElection" -> "a"))
      lazy val view = calculationElectionNoReliefsView(form, rebasedLowestTaxOwed)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select(".govuk-error-summary").size() shouldBe 1
      }
    }

    "should produce the same output when render and f are called" in {

      lazy val flatLowestTaxOwed: Seq[(String, String, String, String, Option[String], Option[BigDecimal])] =
        Seq(
          ("flat", "0", "description", Messages("calc.calculationElection.description.flat"), None, None),
          ("rebased", "1000", "description", Messages("calc.calculationElection.description.rebased"), None, None),
          ("time", "2000", "description", Messages("calc.calculationElection.description.time"), None, None)
        )

      calculationElectionNoReliefsView.f(calculationElectionForm, flatLowestTaxOwed)(fakeRequest, mockMessage) shouldBe calculationElectionNoReliefsView.render(calculationElectionForm, flatLowestTaxOwed, fakeRequest, mockMessage)
    }
  }
}
