/*
 * Copyright 2018 HM Revenue & Customs
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

import assets.MessageLookup.NonResident.{DisposalDate => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import forms.DisposalDateForm._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.disposalDate
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class DisposalDateViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "The Disposal Date View" should {

    "return some HTML" which {

      lazy val view = disposalDate(disposalDateForm)
      lazy val document = Jsoup.parse(view.body)

      "have the title 'When did you sign the contract that made someone else the owner?'" in {
        document.title shouldEqual messages.question
      }

      s"have the heading ${messages.question} " in {
        document.body.getElementsByTag("h1").text shouldEqual messages.question
      }

      s"have the Welsh language option on the first page" in {
        document.body.getElementById("cymraeg-switch").attr("href") shouldEqual "/calculate-your-capital-gains/non-resident/language/cymraeg"
      }

      s"have the English language option on the first page" in {
        document.body.getElementById("english-switch").attr("href") shouldEqual "/calculate-your-capital-gains/non-resident/language/english"
      }

      s"have the question '${messages.question}'" in {
        document.body.getElementsByTag("fieldset").text should include(messages.question)
      }

      "have inputs using the id acquisitionDate" in {
        document.body().select("input[type=number]").attr("id") should include ("disposalDate")
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate().url
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

    "supplied with errors" should {
      lazy val form = disposalDateForm.bind(Map("disposalDateDay" -> "a"))
      lazy val view = disposalDate(form)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
