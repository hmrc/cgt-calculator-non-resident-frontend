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

package views.nonResident

import assets.MessageLookup.{NonResident => commonMessages}
import assets.MessageLookup.NonResident.{DisposalDate => messages}
import controllers.helpers.FakeRequestHelper
import forms.nonresident.DisposalDateForm._
import org.jsoup.Jsoup
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.nonresident.disposalDate

class DisposalDateViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "The Disposal Date View" should {

    "return some HTML" which {

      lazy val view = disposalDate(disposalDateForm)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "have the title 'When did you sign the contract that made someone else the owner?'" in {
        document.title shouldEqual messages.question
      }

      s"have the heading ${Messages("calc.disposalDate.question")} " in {
        document.body.getElementsByTag("h1").text shouldEqual messages.question
      }

      s"have the question '${Messages("calc.disposalDate.question")}'" in {
        document.body.getElementsByTag("fieldset").text should include(messages.question)
      }

      "have inputs using the id acquisitionDate" in {
        document.body().select("input[type=number]").attr("id") should include ("disposalDate")
      }

      s"have a home link to '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
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
      lazy val view = disposalDate(form)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
