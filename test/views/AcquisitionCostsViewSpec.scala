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

import assets.MessageLookup
import assets.MessageLookup.{NonResident => messages}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import forms.AcquisitionCostsForm._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.acquisitionCosts
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class AcquisitionCostsViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {



  "Acquisition costs view" when {

    "supplied with no errors" should {
      lazy val view = acquisitionCosts(acquisitionCostsForm, "back-link")
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '${messages.AcquisitionCosts.question}'" in {
        document.title() shouldBe messages.AcquisitionCosts.question
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has the text" in {
          backLink.text shouldBe MessageLookup.NonResident.back
        }

        s"has a route to 'back-link'" in {
          backLink.attr("href") shouldBe "back-link"
        }

        "has the class 'back-link'" in {
          backLink.attr("class") shouldBe "back-link"
        }
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate().url
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-large" in {
          heading.attr("class") shouldBe "heading-xlarge"
        }

        s"has the text '${messages.AcquisitionCosts.question}'" in {
          heading.text shouldBe messages.AcquisitionCosts.question
        }
      }

      s"have the question '${messages.AcquisitionCosts.question}'" in {
        document.body.select("label div").first().text shouldBe messages.AcquisitionCosts.question
      }
      s"have a paragraph that has the text '${messages.AcquisitionCosts.bulletTitle}" in {
        document.body.select("p#bulletTitle").text() shouldBe messages.AcquisitionCosts.bulletTitle
      }

      "have a list" which {
        lazy val list = document.body().select("#helpList")

        s"has a bullet point with the text '${messages.AcquisitionCosts.bulletOne}'" in {
          list.select(":first-child").text() shouldBe messages.AcquisitionCosts.bulletOne
        }

        s"has a bullet point with the text '${messages.AcquisitionCosts.bulletTwo}'" in {
          list.select(":nth-child(2)").text() shouldBe messages.AcquisitionCosts.bulletTwo
        }

        s"has a bullet point with the text '${messages.AcquisitionCosts.bulletThree}'" in {
          list.select(":last-child").text() shouldBe messages.AcquisitionCosts.bulletThree
        }

        "has the class 'list list-bullet'" in {
          list.attr("class") shouldBe "list list-bullet"
        }

      }


      "have an input with the id 'acquisitionCosts" in {
        document.body().select("input").attr("id") shouldBe "acquisitionCosts"
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.AcquisitionCostsController.submitAcquisitionCosts().url}'" in {
          form.attr("action") shouldBe controllers.routes.AcquisitionCostsController.submitAcquisitionCosts().url
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
    }

    "supplied with errors" should {
      lazy val form = acquisitionCostsForm.bind(Map("acquisitionCosts" -> "a"))
      lazy val view = acquisitionCosts(form, "back-link")
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
}
