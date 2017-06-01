/*
 * Copyright 2017 HM Revenue & Customs
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
import forms.CalculationElectionForm
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class CalculationElectionHelperFormSpec extends UnitSpec with WithFakeApplication {

  "Creating a calculationElectionHelperForm" should {

    "when passing in a single element with other reliefs to render" should {

      val seq: Seq[(String, String, String, String, Option[String], Option[BigDecimal])] =
        Seq(("flat", "1000", "messages", "calcType", Some("dateMessages"), Some(BigDecimal(2000))))
      lazy val partial = views.html.helpers.calculationElectionHelperForm(CalculationElectionForm.calculationElectionForm("calculationElection"), seq)
      lazy val doc = Jsoup.parse(partial.body)

      "has a value for the input of 'flat'" in {
        doc.select("input").attr("value") shouldBe "flat"
      }

      "has a value in a span for the amount" in {
        doc.select("label > div > span.bold-small").text shouldBe "£1,000.00"
      }

      "has the message messages" in {
        doc.select("div.form-group > span > span").get(0).text shouldBe "messages"
      }

      "has the calculation type calcType" in {
        doc.select("label > span.bold-small").text shouldBe "calcType"
      }

      "displays the date message dateMessages" in {
        doc.select("div.form-group > span > span").get(1).text shouldBe "dateMessages"
      }

      "displays the other reliefs change link" in {
        doc.select("div.panel-indent span.align-bottom").text shouldBe MessageLookup.NonResident.CalculationElection.someOtherTaxRelief
      }
    }

    "when passing in a single element with no other reliefs to render" should {

      val seq: Seq[(String, String, String, String, Option[String], Option[BigDecimal])] =
        Seq(("flat", "1000", "messages", "calcType", None, None))
      lazy val partial = views.html.helpers.calculationElectionHelperForm(CalculationElectionForm.calculationElectionForm("calculationElection"), seq)
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
      lazy val partial = views.html.helpers.calculationElectionHelperForm(CalculationElectionForm.calculationElectionForm("calculationElection"), seq)
      lazy val doc = Jsoup.parse(partial.body)

      "displays the other reliefs change link" in {
        doc.select("div.panel-indent button.button").text shouldBe MessageLookup.NonResident.CalculationElection.otherTaxRelief
      }
    }

    "when passing in two elements" should {
      val seq: Seq[(String, String, String, String, Option[String], Option[BigDecimal])] =
        Seq(
          ("one", "1000", "messages", "calcType", None, Some(0)),
          ("two", "1000", "messages", "calcType", None, Some(0))
        )
      lazy val partial = views.html.helpers.calculationElectionHelperForm(CalculationElectionForm.calculationElectionForm("calculationElection"), seq)
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
      lazy val partial = views.html.helpers.calculationElectionHelperForm(CalculationElectionForm.calculationElectionForm("calculationElection"), seq)
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
