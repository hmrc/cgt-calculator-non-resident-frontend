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

import assets.MessageLookup.{NonResident => messages}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class DisabledTrusteeViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  "Disabled Trustee view" when {

    "supplied with no errors" should {
      lazy val view = disabledTrustee(disabledTrusteeForm)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '${messages.DisabledTrustee.question}'" in {
        document.title() shouldBe messages.DisabledTrustee.question
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has the text" in {
          backLink.text shouldBe messages.back
        }

        s"has a route to 'customer-type'" in {
          backLink.attr("href") shouldBe controllers.nonresident.routes.CustomerTypeController.customerType().url
        }
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-large" in {
          heading.attr("class") shouldBe "heading-xlarge"
        }

        s"has the text '${messages.DisabledTrustee.question}'" in {
          heading.text shouldBe messages.DisabledTrustee.question
        }
      }

      s"have a home link to '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
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

        "has a link with an href to 'https://www.gov.uk/trusts-taxes/trusts-and-capital-gains-tax'" in {
          link.attr("href") shouldBe "https://www.gov.uk/trusts-taxes/trusts-and-capital-gains-tax"
        }

        "has a link with the correct text" in {
          link.text() shouldBe s"${messages.DisabledTrustee.linkOne} ${messages.externalLink}"
        }
      }

      s"have the question '${messages.DisabledTrustee.question}'" in {
        document.body.select("legend").first().text shouldBe messages.DisabledTrustee.question
      }

      "have a visuallyhidden legend" in {
        document.body.select("legend").first().attr("class") shouldBe "visuallyhidden"
      }

      s"have the help text '${messages.DisabledTrustee.helpText}'" in {
        document.body.select("span.form-hint").text() shouldBe messages.DisabledTrustee.helpText
      }

      "have inputs containing the id 'isVulnerable'" in {
        document.body().select("input").attr("id") should include("isVulnerable")
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.nonresident.routes.DisabledTrusteeController.submitDisabledTrustee().url}'" in {
          form.attr("action") shouldBe controllers.nonresident.routes.DisabledTrusteeController.submitDisabledTrustee().url
        }
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
      val form = disabledTrusteeForm.bind(Map("isVulnerable" -> "a"))
      lazy val view = disabledTrustee(form)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
