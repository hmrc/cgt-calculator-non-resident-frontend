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

package views

import assets.MessageLookup.NonResident.{AnnualExemptAmount => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import forms.AnnualExemptAmountForm._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.annualExemptAmount
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class AnnualExemptAmountViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {



  "Annual exempt amount view" when {

    "supplied with no errors" should {
      lazy val view = annualExemptAmount(annualExemptAmountForm(BigDecimal(10000)), 11100, "back-url")
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '${messages.question}'" in {
        document.title() shouldBe messages.question
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        s"has the text '${commonMessages.back}'" in {
          backLink.text shouldBe commonMessages.back
        }

        s"has a link to 'back-url'" in {
          backLink.attr("href") shouldBe "back-url"
        }
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate().url
      }

      "have a title" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-large" in {
          heading.attr("class") shouldBe "heading-xlarge"
        }

        s"has the text '${messages.question}'" in {
          heading.text shouldBe messages.question
        }
      }

      "have a sidebar" which {
        lazy val sidebar = document.body().select("aside")
        lazy val link = sidebar.select("a")

        "contains only one link" in {
          link.size() shouldBe 1
        }

        "has a link with the class 'external-link'" in {
          link.attr("class") shouldBe "external-link"
        }

        "has a link with a rel of 'external'" in {
          link.attr("rel") shouldBe "external"
        }

        "has a link with a target of '_blank'" in {
          link.attr("target") shouldBe "_blank"
        }

        "has a link with an href to 'https://www.gov.uk/capital-gains-tax/allowances'" in {
          link.attr("href") shouldBe "https://www.gov.uk/capital-gains-tax/allowances"
        }

        "has a link with the correct text" in {
          link.text() shouldBe s"${messages.link} ${commonMessages.externalLink}"
        }
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.AnnualExemptAmountController.submitAnnualExemptAmount().url}'" in {
          form.attr("action") shouldBe controllers.routes.AnnualExemptAmountController.submitAnnualExemptAmount().url
        }
      }

      s"have the question '${messages.question}'" in {
        document.body.select("label span").first().text shouldBe messages.question
      }

      "have an input with the id 'annualExemptAmount" in {
        document.body().select("input").attr("id") shouldBe "annualExemptAmount"
      }

      "have a button" which {
        lazy val button = document.select("button")

        "has the class 'button'" in {
          button.attr("class") shouldBe "button"
        }

        "has the type 'submit'" in {
          button.attr("type") shouldBe "submit"
        }

        "has the id 'continue-button'" in {
          button.attr("id") shouldBe "continue-button"
        }
      }
    }

    "supplied with errors" should {
      lazy val form = annualExemptAmountForm(BigDecimal(10000)).bind(Map("annualExemptAmount" -> "15000"))
      lazy val view = annualExemptAmount(form, 11100, "back-url")
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
