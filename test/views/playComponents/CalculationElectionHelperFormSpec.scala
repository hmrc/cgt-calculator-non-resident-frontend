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

package views.helpers

import assets.MessageLookup
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.helpers.FakeRequestHelper
import forms.CalculationElectionForm
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import views.html.playComponents.calculationElectionHelperForm

class CalculationElectionHelperFormSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  implicit lazy val calculationElectionHelperFormView = fakeApplication.injector.instanceOf[calculationElectionHelperForm]

  "Creating a calculationElectionHelperForm" should {

    "when passing in a single element with other reliefs to render" should {

      val seq: Seq[(String, String, String, String, Option[String], Option[BigDecimal])] =
        Seq(("flat", "1000", "messages", "calcType", Some("dateMessages"), Some(BigDecimal(2000))))
      lazy val partial = calculationElectionHelperFormView(
        seq,
        Messages("calc.calculationElectionNoReliefs.title"),
        field = CalculationElectionForm.calculationElectionForm("calculationElection")
      )
      lazy val doc = Jsoup.parse(partial.body)

      "has a value for the input of 'flat'" in {
        doc.select("input").attr("value") shouldBe "flat"
      }

      "has a value in a span for the amount" in {
        doc.getElementsByClass("govuk-body govuk-!-font-weight-bold").get(1).text shouldBe "£1,000.00"
      }

      "has the message messages.en" in {
        doc.getElementsByClass("govuk-body").get(1).text should include("messages")
      }

      "has the calculation type calcType" in {
        doc.getElementsByClass("govuk-body govuk-!-font-weight-bold").get(0).text shouldBe "calcType"
      }

      "displays the date message dateMessages" in {
        doc.getElementsByClass("govuk-body").get(1).text should include("dateMessages")
      }

      "displays the other reliefs change link" in {
        doc.getElementsByClass("govuk-button").text shouldBe MessageLookup.NonResident.CalculationElection.someOtherTaxReliefButton
      }
    }

    "when passing in a single element with no other reliefs to render" should {

      val seq: Seq[(String, String, String, String, Option[String], Option[BigDecimal])] =
        Seq(("flat", "1000", "messages", "calcType", None, None))
      lazy val partial = calculationElectionHelperFormView(
        seq,
        Messages("calc.calculationElectionNoReliefs.title"),
        field = CalculationElectionForm.calculationElectionForm("calculationElection")
      )
      lazy val doc = Jsoup.parse(partial.body)

      "does not display the date message dateMessages" in {
        doc.select("div.form-group > span > span.no-wrap").size shouldBe 0
      }

      "does not display the other reliefs addition link" in {
        doc.select("div.panel-indent span.align-bottom").size shouldBe 0
      }
    }

    "when passing in a single element when other reliefs is defined but = 0" should {

      val seq: Seq[(String, String, String, String, Option[String], Option[BigDecimal])] =
        Seq(("flat", "1000", "messages", "calcType", None, Some(0)))
      lazy val partial = calculationElectionHelperFormView(
        seq,
        Messages("calc.calculationElectionNoReliefs.title"),
        field = CalculationElectionForm.calculationElectionForm("calculationElection")
      )
      lazy val doc = Jsoup.parse(partial.body)

      "displays the other reliefs change link" in {
        doc.getElementsByClass("govuk-button").text shouldBe MessageLookup.NonResident.CalculationElection.otherTaxRelief
      }
    }

    "when passing in two elements" should {
      val seq: Seq[(String, String, String, String, Option[String], Option[BigDecimal])] =
        Seq(
          ("one", "1000", "messages", "calcType", None, Some(0)),
          ("two", "1000", "messages", "calcType", None, Some(0))
        )
      lazy val partial = calculationElectionHelperFormView(
        seq,
        Messages("calc.calculationElectionNoReliefs.title"),
        field = CalculationElectionForm.calculationElectionForm("calculationElection")
      )
      lazy val doc = Jsoup.parse(partial.body)

      "render the first element" in {
        doc.select("input").get(0).attr("value") shouldBe "one"
      }

      "render the second element" in {
        doc.select("input").get(1).attr("value") shouldBe "two"
      }
    }

    "when passing in three elements" should {
      val seq: Seq[(String, String, String, String, Option[String], Option[BigDecimal])] =
        Seq(
          ("one", "1000", "messages", "calcType", None, Some(0)),
          ("two", "1000", "messages", "calcType", None, Some(0)),
          ("three", "1000", "messages", "calcType", None, Some(0))
        )
      lazy val partial = calculationElectionHelperFormView(
        seq,
        Messages("calc.calculationElectionNoReliefs.title"),
        field = CalculationElectionForm.calculationElectionForm("calculationElection")
      )
      lazy val doc = Jsoup.parse(partial.body)

      "render the first element" in {
        doc.select("input").get(0).attr("value") shouldBe "one"
      }

      "render the second element" in {
        doc.select("input").get(1).attr("value") shouldBe "two"
      }

      "render the third element" in {
        doc.select("input").get(2).attr("value") shouldBe "three"
      }
    }
  }
}
