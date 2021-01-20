/*
 * Copyright 2021 HM Revenue & Customs
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
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.CalculationElectionForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import views.html.{calculation => views}
import _root_.views.html.calculation.calculationElection
import common.{CommonPlaySpec, WithCommonFakeApplication}

class CalculationElectionViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  "The Calculation Election View" should {

    lazy val form = calculationElectionForm
    lazy val seq: Seq[(String, String, String, String, Option[String], Option[BigDecimal])] =
      Seq(("flat", "2000", Messages("calc.calculationElection.message.flat"), Messages("calc.calculationElection.description.flat"), None, None))
    lazy val view = views.calculationElection(form, seq)(fakeRequest,mockMessage, fakeApplication, mockConfig)
    lazy val doc = Jsoup.parse(view.body)

    s"have a title of '${messages.heading}" in {
      doc.title() shouldBe messages.heading
    }

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

    "have a dynamic back button" which {

      "has the correct back link text" in {
        doc.select("a#back-link").text shouldBe commonMessages.back
      }

      "has the back-link class" in {
        doc.select("a#back-link").hasClass("back-link") shouldBe true
      }

      "has a back link to 'claiming-reliefs'" in {
        doc.select("a#back-link").attr("href") shouldBe controllers.routes.ClaimingReliefsController.claimingReliefs().url
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

    "have the text in paragraphs" which {

      s"contains the text ${messages.moreInfoFirstP}" in {
        doc.body().getElementsByTag("p").text should include(messages.moreInfoFirstP)
      }

      s"contains the text ${messages.moreInfoSecondP}" in {
        doc.body().getElementsByTag("p").text should include(messages.moreInfoSecondP)
      }
    }

    "display a 'Continue' button " in {
      doc.body.getElementById("continue-button").text shouldEqual commonMessages.continue
    }

    "have no pre-selected option" in {
      doc.body.getElementById("calculationElection-flat").parent.classNames().contains("selected") shouldBe false
    }

    "supplied with errors" should {
      lazy val form = calculationElectionForm.bind(Map("calculationElection" -> "a"))
      lazy val view = views.calculationElection(form, seq)(fakeRequest,mockMessage, fakeApplication, mockConfig)
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

      calculationElection.f(calculationElectionForm, flatLowestTaxOwed)(fakeRequest, mockMessage, fakeApplication, mockConfig) shouldBe calculationElection.render(calculationElectionForm, flatLowestTaxOwed, fakeRequest, mockMessage, fakeApplication, mockConfig)
    }
  }

}
