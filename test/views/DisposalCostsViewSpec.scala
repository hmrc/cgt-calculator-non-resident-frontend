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

import assets.MessageLookup.NonResident.{DisposalCosts => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import forms.DisposalCostsForm._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.disposalCosts
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class DisposalCostsViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  implicit val fr = fakeRequest

  "Disposal Costs view" when {

    "supplied with no errors" should {
      lazy val view = disposalCosts(disposalCostsForm, "back-link")
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '${messages.question}'" in {
        document.title shouldBe messages.question
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has a class of 'back-link'" in {
          backLink.attr("class") shouldBe "back-link"
        }

        "has the text" in {
          backLink.text shouldBe commonMessages.back
        }

        s"has a route to 'back-link'" in {
          backLink.attr("href") shouldBe "back-link"
        }
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-xlarge" in {
          heading.attr("class") shouldBe "heading-xlarge"
        }

        s"has the text '${messages.question}'" in {
          heading.text shouldBe messages.question
        }
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate().url
      }

      "have a hint" which {

        lazy val hint = document.select("#input-hint")

        "should have the class form-hint" in {
          hint.hasClass("form-hint") shouldEqual true
        }

        "should contain a bullet list" which {

          s"has the title ${messages.helpTitle}" in {
            hint.select("p").text shouldEqual messages.helpTitle
          }

          s"has a bullet point with the text ${messages.helpBulletOne}" in {
            hint.select("ul li").text should include(messages.helpBulletOne)
          }

          s"has a bullet point with the text ${messages.helpBulletTwo}" in {
            hint.select("ul li").text should include(messages.helpBulletTwo)
          }

          s"has a bullet point with the text ${messages.helpBulletThree}" in {
            hint.select("ul li").text should include(messages.helpBulletThree)
          }

          s"has a bullet point with the text ${messages.helpBulletFour}" in {
            hint.select("ul li").text should include(messages.helpBulletFour)
          }
        }
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a POST method" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.DisposalCostsController.submitDisposalCosts().url}'" in {
          form.attr("action") shouldBe controllers.routes.DisposalCostsController.submitDisposalCosts().url
        }
      }

      s"have the question ${messages.question}" in {
        document.body.select("label span").first.text shouldBe messages.question
      }

      "have an input with the id 'disposalCosts" in {
        document.body.select("input").attr("id") shouldBe "disposalCosts"
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
      lazy val form = disposalCostsForm.bind(Map("disposalCosts" -> "a"))
      lazy val view = disposalCosts(form, "back-link")
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
