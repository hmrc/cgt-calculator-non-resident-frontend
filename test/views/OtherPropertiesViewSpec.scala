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

import assets.MessageLookup.NonResident.{OtherProperties => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import forms.OtherPropertiesForm._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.otherProperties

class OtherPropertiesViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "The other properties view" should {

    "return some HTML that, when the hidden question is displayed" should {

      lazy val view = otherProperties(otherPropertiesForm, "back-link")(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      s"have the title '${messages.question}'" in {
        document.title shouldEqual messages.question
      }

      s"have the heading ${messages.question}" in {
        document.body.getElementsByTag("h1").text shouldEqual messages.question
      }

      s"have a 'Back' link to back-link" which {

        "should have the text" in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
        }

        "should have an href to 'back-link'" in {
          document.body.getElementById("back-link").attr("href") shouldEqual "back-link"
        }
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate().url
      }

      s"have a legend of the input" which {
        lazy val legend = document.body.getElementsByTag("legend")
        s"has the text ${messages.question}" in {
          legend.text should include(messages.question)
        }
        "has the class 'visuallyhidden'" in {
          legend.attr("class") shouldBe "visuallyhidden"
        }
      }

      "include a read more section" which {

        "include twos links of which link one" should {

          lazy val linkOne = document.body.getElementById("helpLink1")

          s"link one should have text ${messages.linkOne} ${commonMessages.externalLink}" in {
            linkOne.text shouldEqual s"${messages.linkOne} ${commonMessages.externalLink}"
          }

          s"link one should have an href to 'https://www.gov.uk/capital-gains-tax'" in {
            linkOne.attr("href") shouldEqual "https://www.gov.uk/capital-gains-tax"
          }

          "has a link with the class 'external-link'" in {
            linkOne.attr("class") shouldBe "external-link"
          }

          "has a link with a rel of 'external'" in {
            linkOne.attr("rel") shouldBe "external"
          }

          "has a link with a target of '_blank'" in {
            linkOne.attr("target") shouldBe "_blank"
          }
        }

        "include twos links of which link two" should {

          lazy val linkTwo = document.body.getElementById("helpLink2")

          s"link two should have text ${messages.linkTwo} ${commonMessages.externalLink}" in {
            linkTwo.text shouldEqual s"${messages.linkTwo} ${commonMessages.externalLink}"
          }

          s"link two should have an href to 'https://www.gov.uk/income-tax-rates/previous-tax-years'" in {
            linkTwo.attr("href") shouldEqual "https://www.gov.uk/income-tax-rates/previous-tax-years"
          }
          "has a link with the class 'external-link'" in {
            linkTwo.attr("class") shouldBe "external-link"
          }

          "has a link with a rel of 'external'" in {
            linkTwo.attr("rel") shouldBe "external"
          }

          "has a link with a target of '_blank'" in {
            linkTwo.attr("target") shouldBe "_blank"
          }
        }
      }

      "have inputs using the id 'otherProperties'" in {
        document.body().select("input[type=radio]").attr("id") should include("otherProperties")
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

        "has the text 'Continue'" in {
          button.text shouldEqual commonMessages.continue
        }
      }
    }

    "return some HTML that, when the hidden question is not displayed" should {

      lazy val view = otherProperties(otherPropertiesForm, "back-link")(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      s"have the title '${messages.question}'" in {
        document.title shouldEqual messages.question
      }

      "have inputs using the id 'otherProperties'" in {
        document.body().select("input[type=radio]").attr("id") should include("otherProperties")
      }

      "have inputs using the id otherPropertiesAmt" in {
        document.body().select("input[type=number]").attr("id") shouldEqual ""
      }
    }

    "when passed a form with errors" should {

      lazy val form = otherPropertiesForm.bind(Map("otherProperties" -> "bad-data"))
      lazy val view = otherProperties(form, "back-link")(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
