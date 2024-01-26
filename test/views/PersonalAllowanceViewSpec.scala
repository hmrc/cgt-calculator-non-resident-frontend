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

import assets.MessageLookup.NonResident.{PersonalAllowance => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import controllers.routes
import forms.PersonalAllowanceForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.personalAllowance

class PersonalAllowanceViewSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val personalAllowanceView = fakeApplication.injector.instanceOf[personalAllowance]
  lazy val pageTitle = s"""${messages.question} - ${commonMessages.pageHeading} - GOV.UK"""

  "The Personal Allowance View" should {

    "return some HTML" which {

      lazy val view = personalAllowanceView(personalAllowanceForm(11000))(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      s"has the title ${pageTitle}" in {
        document.title shouldEqual pageTitle
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-large" in {
          heading.attr("class") shouldBe "govuk-heading-xl"
        }

        s"has the text '${messages.question}'" in {
          heading.text shouldBe messages.question
        }
      }

      "have a back link" which {
        lazy val backLink = document.body().select(".govuk-back-link")

        "has a class of 'back-link'" in {
          backLink.attr("class") shouldBe "govuk-back-link"
        }

        "has the text" in {
          backLink.text shouldBe commonMessages.back
        }

        s"has a route to 'customer-type'" in {
          backLink.attr("href") shouldBe "#"
        }
      }

      "has a label for the input" which {

        s"has the question '${messages.question}'" in {
          document.body.getElementsByTag("label").text should include(messages.question)
        }
      }

      "display an input box for the Personal Allowance" in {
        document.body.getElementById("personalAllowance").tagName() shouldEqual "input"
      }

      "have a button" which {
        lazy val button = document.select("button")

        "has the class 'button'" in {
          button.attr("class") shouldBe "govuk-button"
        }

        "has the id 'submit'" in {
          button.attr("id") shouldBe "submit"
        }

        "has the text 'Continue'" in {
          button.text shouldEqual commonMessages.continue
        }
      }

      s"should have help text that" should {

        lazy val helpParagraph = document.select("p.govuk-body")

        s"have the text ${messages.help}" in {

        }

        "have a link" which {

          lazy val link = helpParagraph.select("a")

          s"have text ${messages.link} ${commonMessages.externalLink}" in {
            link.text shouldEqual s"${messages.link} ${commonMessages.externalLink}"
          }

          "have an href to 'https://www.gov.uk/income-tax-rates/current-rates-and-allowances'" in {
            link.attr("href") shouldEqual "https://www.gov.uk/income-tax-rates/current-rates-and-allowances"
          }

          "have the class 'external-link'" in {
            link.attr("class") shouldBe "govuk-link"
          }

          "have a rel of 'external'" in {
            link.attr("rel") shouldBe "external"
          }

          "have a target of '_blank'" in {
            link.attr("target") shouldBe "_blank"
          }
        }
      }

      "should produce the same output when render and f are called" in {
        personalAllowanceView.f(personalAllowanceForm(11000))(fakeRequest, mockMessage) shouldBe personalAllowanceView.render(personalAllowanceForm(11000), fakeRequest, mockMessage)
      }
    }

    "when supplied with a form with errors" should {

      lazy val form = personalAllowanceForm(11000).bind(Map("personalAllowance" -> "132891"))
      lazy val view = personalAllowanceView(form)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.getElementsByClass("govuk-error-summary").size() shouldBe 1
      }
    }
  }
}
