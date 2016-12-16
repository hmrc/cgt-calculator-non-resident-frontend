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

package views

import assets.MessageLookup.NonResident.{CalculationElection => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import play.api.i18n.Messages
import forms.CalculationElectionForm._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.{calculation => views}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class CalculationElectionViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  implicit val fr = fakeRequest

  "The Calculation Election View" should {

    lazy val form = calculationElectionForm
    lazy val seq: Seq[(String, String, String, Option[String], Option[BigDecimal])] =
      Seq(("flat", "2000", Messages("calc.calculationElection.message.flat"), None, None))
    lazy val view = views.calculationElection(form, seq)
    lazy val doc = Jsoup.parse(view.body)

    "have a h1 tag that" should {

      s"have the question of ${messages.heading}" in {
        doc.select("h1").text() shouldBe messages.heading
      }

      "have the heading-xlarge class" in {
        doc.select("h1").hasClass("heading-xlarge") shouldBe true
      }
    }

    s"have a home link to '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
      doc.select("#homeNavHref").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate().url
    }

    "have a back button" which {

      "has the correct back link text" in {
        doc.select("a#back-link").text shouldBe commonMessages.back
      }

      "has the back-link class" in {
        doc.select("a#back-link").hasClass("back-link") shouldBe true
      }

      "has a back link to 'back'" in {
        doc.select("a#back-link").attr("href") shouldBe "/calculate-your-capital-gains/non-resident/check-your-answers"
      }
    }

    "render a form tag" which {

      lazy val form = doc.select("form")

      "has a submit action" in {
        form.attr("action") shouldEqual "/calculate-your-capital-gains/non-resident/calculation-election"
      }

      "with method type POST" in {
        form.attr("method") shouldBe "POST"
      }
    }

    "have a read more sidebar" which {

      s"contains the text ${commonMessages.readMore}" in {
        doc.select("aside h2").text shouldBe commonMessages.readMore
      }

      s"contains the link ${commonMessages.externalLink}" in {
        doc.select("aside a").text shouldBe s"${messages.linkOne} ${commonMessages.externalLink}"
      }
    }

    "contains a h2 heading" which {

      "has the class of heading-small" in {
        doc.select("h2").hasClass("heading-small") shouldBe true
      }

      s"contains the text ${messages.moreInformation}" in {
        doc.body().getElementsByTag("h2").text should include(messages.moreInformation)
      }
    }

    "have the text in paragraphs" which {

      s"contains the text ${messages.moreInfoFirstP}" in {
        doc.body().getElementsByTag("p").text should include(messages.moreInfoFirstP)
      }

      s"contains the text ${messages.moreInfoSecondP}" in {
        doc.body().getElementsByTag("p").text should include(messages.moreInfoSecondP)
      }

      s"contains the text ${messages.moreInfoThirdP}" in {
        doc.body().getElementsByTag("p").text should include(messages.moreInfoThirdP)
      }
    }

    "display a 'Continue' button " in {
      doc.body.getElementById("continue-button").text shouldEqual commonMessages.continue
    }

    s"display a concertina information box with '${messages.whyMore} " in {
      doc.select("summary span.summary").text shouldEqual messages.whyMore
    }

    s"display a concertina information box with '${messages.whyMoreDetailsOne} " in {
      doc.select("div#details-content-0 p").text should include(messages.whyMoreDetailsOne)
    }

    s"display a concertina information box with '${messages.whyMoreDetailsTwo} " in {
      doc.select("div#details-content-0 p").text should include(messages.whyMoreDetailsTwo)
    }

    "have no pre-selected option" in {
      doc.body.getElementById("calculationElection-flat").parent.classNames().contains("selected") shouldBe false
    }

    "supplied with errors" should {
      lazy val form = calculationElectionForm.bind(Map("calculationElection" -> "a"))
      lazy val view = views.calculationElection(form, seq)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }

}
