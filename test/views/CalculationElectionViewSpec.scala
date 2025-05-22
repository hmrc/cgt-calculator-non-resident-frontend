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

import assets.MessageLookup.NonResident.{CalculationElection => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.CalculationElectionForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.calculationElection

class CalculationElectionViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val calculationElectionView: calculationElection = fakeApplication.injector.instanceOf[calculationElection]
  lazy val pageTitle: String = s"""${messages.heading} - ${commonMessages.serviceName} - GOV.UK"""

  "The Calculation Election View" should {

    lazy val form = calculationElectionForm
    lazy val seq: Seq[(String, String, String, String, Option[String], Option[BigDecimal])] =
      Seq(("flat", "2000", Messages("calc.calculationElection.message.flat"), Messages("calc.calculationElection.description.flat"), None, None))
    lazy val view = calculationElectionView(form, seq)(fakeRequest,mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    s"have a title of '${messages.heading}" in {
      doc.title() shouldBe pageTitle
    }

    "have a h1 tag that" should {

      s"have the question of ${messages.heading}" in {
        doc.select("h1").text() shouldBe messages.heading
      }

      "have the govuk-heading-xl class" in {
        doc.select("h1").hasClass("govuk-heading-xl") shouldBe true
      }
    }

    "have a dynamic back button" which {

      "has the correct back link text" in {
        doc.select("a.govuk-back-link").text shouldBe commonMessages.back
      }

      "has the govuk-back-link class" in {
        doc.select("a.govuk-back-link").hasClass("govuk-back-link") shouldBe true
      }

      "has a back link to 'claiming-reliefs'" in {
        doc.select("a.govuk-back-link").attr("href") shouldBe "#"
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
      doc.body.getElementById("submit").text shouldEqual commonMessages.continue
    }

    "have no pre-selected option" in {
      doc.body.getElementById("calculationElection").parent.classNames().contains("selected") shouldBe false
    }

    "should produce the same output when render and f are called" in {

      lazy val flatLowestTaxOwed: Seq[(String, String, String, String, Option[String], Option[BigDecimal])] =
        Seq(
          ("flat", "0", "description", Messages("calc.calculationElection.description.flat"), None, None),
          ("rebased", "1000", "description", Messages("calc.calculationElection.description.rebased"), None, None),
          ("timeApportioned", "2000", "description", Messages("calc.calculationElection.description.time"), None, None)
        )

      calculationElectionView.f(calculationElectionForm, flatLowestTaxOwed)(fakeRequest, mockMessage) shouldBe calculationElectionView.render(calculationElectionForm, flatLowestTaxOwed, fakeRequest, mockMessage)
    }
  }

}
