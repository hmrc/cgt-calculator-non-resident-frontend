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

import assets.MessageLookup.{NonResident => messages}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import forms.RebasedCostsForm._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.rebasedCosts
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class RebasedCostsViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {



  "The rebased value view" when {

    "not supplied with a pre-existing stored model" should {
      lazy val view = rebasedCosts(rebasedCostsForm)
      lazy val document = Jsoup.parse(view.body)

      s"Have the title ${messages.RebasedCosts.question}" in {
        document.title shouldEqual messages.RebasedCosts.question
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has the text" in {
          backLink.text shouldBe messages.back
        }

        "has the class 'back-link'" in {
          backLink.attr("class") shouldBe "back-link"
        }

        s"has a route to 'rebased-value'" in {
          backLink.attr("href") shouldBe controllers.routes.RebasedValueController.rebasedValue().url
        }
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-xlarge" in {
          heading.attr("class") shouldBe "heading-xlarge"
        }

        s"has the text '${messages.RebasedCosts.question}'" in {
          heading.text shouldBe messages.RebasedCosts.question
        }
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate().url
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.RebasedCostsController.rebasedCosts().url}'" in {
          form.attr("action") shouldBe controllers.routes.RebasedCostsController.rebasedCosts().url
        }
      }

      s"have the question '${messages.RebasedCosts.question}'" in {
        document.body.select("legend").first().text shouldBe messages.RebasedCosts.question
      }

      "have option inputs with id 'hasRebasedCosts'" in {
        document.body().select("input[type=radio]").attr("id") should include ("hasRebasedCosts")
      }

      s"have the input question '${messages.RebasedCosts.inputQuestion}'" in {
        document.body().select("label[for=rebasedCosts] div").first().text() shouldBe messages.RebasedCosts.inputQuestion
      }

      s"have joint ownership text of ${messages.RebasedCosts.jointOwnership}" in {
        document.body().select("label[for=rebasedCosts] p").text() shouldBe messages.RebasedCosts.jointOwnership
      }

      "have a value input with the id 'rebasedCosts'" in {
        document.body().select("input[type=number]").attr("id") should include ("rebasedCosts")
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
      lazy val form = rebasedCostsForm.bind(Map(
        "hasRebasedCosts" -> "Yes",
        "rebasedCosts" -> ""
      ))
      lazy val view = rebasedCosts(form)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
