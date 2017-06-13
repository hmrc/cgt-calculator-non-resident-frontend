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

import assets.MessageLookup.{NonResident => messages}
import controllers.helpers.FakeRequestHelper
import forms.CostsAtLegislationStartForm._
import org.jsoup.Jsoup
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.costsAtLegislationStart

class CostsAtLegislationStartViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "The costs at legislation start date view" when {

    "not supplied with a pre-existing stored model" should {
      lazy val view = costsAtLegislationStart(costsAtLegislationStartForm)
      lazy val document = Jsoup.parse(view.body)

      s"Have the title ${messages.CostsAtLegislationStart.title}" in {
        document.title shouldEqual messages.CostsAtLegislationStart.title
      }

      "have a back link" which {
        lazy val backLink = document.select("#back-link")

        "has the correct text" in {
          backLink.text shouldBe messages.back
        }

        s"has a route to 'worth-before-legislation-start'" in {
          backLink.attr("href") shouldBe controllers.routes.WorthBeforeLegislationStartController.worthBeforeLegislationStart().url
        }
      }

      "have a heading" which {
        lazy val heading = document.select("h1")

        "has a class of heading-xlarge" in {
          heading.attr("class") shouldBe "heading-xlarge"
        }

        s"has the text '${messages.CostsAtLegislationStart.title}'" in {
          heading.text shouldBe messages.CostsAtLegislationStart.title
        }
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate().url
      }

      "have a form" which {
        lazy val form = document.select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.CostsAtLegislationStartController.costsAtLegislationStart().url}'" in {
          form.attr("action") shouldBe controllers.routes.CostsAtLegislationStartController.costsAtLegislationStart().url
        }
      }

      s"have an primary question with the correct text" in {
        document.select("#hasCosts").first().text shouldBe messages.CostsAtLegislationStart.title
      }

      s"have a secondary question with the correct text" in {
        document.select("label[for=costs] > div").first().text() shouldBe messages.CostsAtLegislationStart.howMuch
      }

      s"have help text for the secondary question" in {
        document.select("label[for=costs] p").text() shouldBe messages.CostsAtLegislationStart.helpText
      }

      "have a value input with the id 'costs'" in {
        document.select("#costs").size() shouldBe 1
      }

      "have a continue button" which {
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
      lazy val form = costsAtLegislationStartForm.bind(Map(
        "hasCosts" -> "Yes",
        "costs" -> ""
      ))
      lazy val view = costsAtLegislationStart(form)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
