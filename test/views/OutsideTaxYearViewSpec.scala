
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

import assets.MessageLookup.{NonResident => commonMessages, OutsideTaxYears => messages}
import constructors.helpers.AssertHelpers
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.outsideTaxYear
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.data.Form

class OutsideTaxYearViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper with AssertHelpers {


  "OutsideTaxYear view" when {

    "supplied with no errors" should {
      lazy val view = outsideTaxYear()
      lazy val document = Jsoup.parse(view.body)

      s"have a title of ${messages.title}" in {
        document.title() shouldBe messages.title
      }

      "have a back link" which {
        lazy val backLink = document.select("#back-link")

        "has only a single back link" in {
          backLink.size() shouldBe 1
        }

        "has a class of back-link" in {
          assertHTML(backLink)(_.attr("class") shouldBe "back-link")
        }

        "have a H1 tag that" should {

          lazy val h1Tag = document.select("h1")

          s"have the page heading '${messages.title}'" in {
            h1Tag.text shouldBe messages.title
          }

          "have the heading-large class" in {
            h1Tag.hasClass("heading-large") shouldBe true
          }
        }

        "have text explaining why tax date is incorrect" in {
          document.body().select("div#content p").text() shouldBe messages.message
      }


        "have a back button" which {

          lazy val backLink = document.select("a#back-link")

          "has the correct back link text" in {
            backLink.text shouldBe commonMessages.back
          }

          "has the back-link class" in {
            backLink.hasClass("back-link") shouldBe true
          }

          "has a back link to 'back'" in {
            backLink.attr("href") shouldBe controllers.routes.DisposalDateController.disposalDate().url
          }
        }

        "have a legend for the radio inputs" which {

          lazy val legend = document.select("legend")

          s"contain the text ${messages.title}" in {
            legend.text should include(s"${messages.title}")
          }
        }

        "have a continue button" which {

          lazy val button = document.select("button")

          "has class 'button'" in {
            button.hasClass("button") shouldEqual true
          }

          "has attribute 'type'" in {
            button.hasAttr("type") shouldEqual true
          }

          "has type value of 'submit'" in {
            button.attr("type") shouldEqual "submit"
          }

          "has attribute id" in {
            button.hasAttr("id") shouldEqual true
          }

          "has id equal to continue-button" in {
            button.attr("id") shouldEqual "continue-button"
          }

          s"has the text ${commonMessages.continue}" in {
            button.text shouldEqual s"${commonMessages.continue}"
          }
        }
      }


    }
  }
}

