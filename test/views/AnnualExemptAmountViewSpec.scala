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

package views

import assets.MessageLookup.NonResident.{AnnualExemptAmount => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.AnnualExemptAmountForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.annualExemptAmount

class AnnualExemptAmountViewSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit val mockLang = mock[Lang]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  val mockMessagesApi = mock[MessagesApi]
  lazy val annualExemptAmountView = fakeApplication.injector.instanceOf[annualExemptAmount]

  "Annual exempt amount view" when {

    "supplied with no errors" should {
      lazy val view = annualExemptAmountView(annualExemptAmountForm(BigDecimal(10000)), 11100, "back-url")(fakeRequest,mockMessage)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '${messages.question}'" in {
        document.title() shouldBe s"${messages.question} - Calculate your Non-Resident Capital Gains Tax - GOV.UK"
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        s"has the text '${commonMessages.back}'" in {
          backLink.text shouldBe commonMessages.back
        }

        s"has a link to 'back-url'" in {
          backLink.attr("href") shouldBe "javascript:history.back()"
        }
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate.url}'" in {
        document.select("body > header > div > div > div.govuk-header__content > a").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate.url
      }

      "have a title" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-large" in {
          heading.attr("class") shouldBe "govuk-heading-xl"
        }

        s"has the text '${messages.question}'" in {
          heading.text shouldBe messages.question
        }
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.AnnualExemptAmountController.submitAnnualExemptAmount.url}'" in {
          form.attr("action") shouldBe controllers.routes.AnnualExemptAmountController.submitAnnualExemptAmount.url
        }
      }

      s"have the question '${messages.question}'" in {
        document.getElementsByClass("govuk-template__body").text should include {messages.question}
      }

      "have an input with the id 'annualExemptAmount" in {
        document.body().select("input").attr("id") shouldBe "annualExemptAmount"
      }

      "have a button" which {
        lazy val button = document.select("button")

        "has the class 'button'" in {
          button.attr("class") shouldBe "govuk-button"
        }

        "has the type 'submit'" in {
          button.attr("id") shouldBe "submit"
        }

        "has the id 'continue-button'" in {
          button.attr("id") shouldBe "submit"
        }
      }
    }

    "supplied with errors" should {
      lazy val form = annualExemptAmountForm(BigDecimal(10000)).bind(Map("annualExemptAmount" -> "15000"))
      lazy val view = annualExemptAmountView(form, 11100, "back-url")(fakeRequest,mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.getElementsByClass("govuk-error-summary").size() shouldBe 1
      }
    }

    "should produce the same output when render and f are called" in {
      annualExemptAmountView.f(annualExemptAmountForm(BigDecimal(10000)), 11100, "back-url")(fakeRequest,mockMessage) shouldBe annualExemptAmountView.render(annualExemptAmountForm(BigDecimal(10000)), 11100, "back-url", fakeRequest,mockMessage)
    }
  }
}
