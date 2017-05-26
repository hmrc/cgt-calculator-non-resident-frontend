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
import org.jsoup.Jsoup
import forms.PrivateResidenceReliefForm._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.privateResidenceRelief
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class PrivateResidenceReliefViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {



  "Private Residence Relief view" when {

    "supplied with no errors and neither day inputs displayed" should {
      lazy val view = privateResidenceRelief(privateResidenceReliefForm(false, false), false, false, "")
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '${messages.PrivateResidenceRelief.question}'" in {
        document.title() shouldBe messages.PrivateResidenceRelief.question
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has a class of 'back-link'" in {
          backLink.attr("class") shouldBe "back-link"
        }

        "has the text" in {
          backLink.text shouldBe messages.back
        }

        s"has a route to 'improvements'" in {
          backLink.attr("href") shouldBe controllers.routes.PropertyLivedInController.propertyLivedIn().url
        }
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-xlarge" in {
          heading.attr("class") shouldBe "heading-xlarge"
        }

        s"has the text '${messages.PrivateResidenceRelief.question}'" in {
          heading.text shouldBe messages.PrivateResidenceRelief.question
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

        s"has an action of '${controllers.routes.PrivateResidenceReliefController.submitPrivateResidenceRelief().url}'" in {
          form.attr("action") shouldBe controllers.routes.PrivateResidenceReliefController.submitPrivateResidenceRelief().url
        }
      }

      s"have a hidden legend '${messages.PrivateResidenceRelief.question}'" which {
        lazy val legend = document.body.select("legend")
        s"has the text '${messages.PrivateResidenceRelief.question}'" in {
          legend.text shouldBe messages.PrivateResidenceRelief.question
        }
        s"has the class 'visuallyhidden'" in {
          legend.attr("class") shouldBe "visuallyhidden"
        }
      }

      "have inputs containing the id isClaimingPRR" in {
        document.body().select("input").attr("id") should include ("isClaimingPRR")
      }

      "have no hidden content" in {
        document.body().select("#hidden").size() shouldBe 0
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

    "supplied with no errors and the days before input displayed" should {
      lazy val view = privateResidenceRelief(privateResidenceReliefForm(true, false), false, true, "date-input")
      lazy val document = Jsoup.parse(view.body)

      s"have the question a hidden legend" which {
        lazy val legend = document.body.select("legend")

        s"has the text ${messages.PrivateResidenceRelief.question}" in {
          legend.text shouldBe messages.PrivateResidenceRelief.question
        }
        "has the class 'visuallyhidden'" in {
          legend.attr("class") shouldBe "visuallyhidden"
        }
      }

      "have inputs containing the id isClaimingPRR" in {
        document.body().select("input").attr("id") should include ("isClaimingPRR")
      }

      "have some hidden content" which {
        lazy val hiddenContent = document.body().select("#hidden")

        "which has a single div with a class of form-group" in {
          hiddenContent.select("div.form-group").size() shouldBe 1
        }

        "contains an input with the id 'daysClaimed'" in {
          hiddenContent.select("input").attr("id") shouldBe "daysClaimed"
        }

        s"contains the question ${messages.PrivateResidenceRelief.questionBefore}" in {
          hiddenContent.select("label").text() shouldBe s"${messages.PrivateResidenceRelief.questionBefore} " +
            s"date-input ${messages.PrivateResidenceRelief.questionEnd}"
        }
      }
    }

    "supplied with no errors and the days after input displayed" should {
      lazy val view = privateResidenceRelief(privateResidenceReliefForm(false, true), true, false, "date-input-two")
      lazy val document = Jsoup.parse(view.body)

      "have some hidden content" which {
        lazy val hiddenContent = document.body().select("#hidden")

        "which has a single div with a class of form-group" in {
          hiddenContent.select("div.form-group").size() shouldBe 1
        }

        "contains an input with the id 'daysClaimedAfter'" in {
          hiddenContent.select("input").attr("id") shouldBe "daysClaimedAfter"
        }

        s"contains the question ${messages.PrivateResidenceRelief.questionBetween}" in {
          hiddenContent.select("label").text() shouldBe s"${messages.PrivateResidenceRelief.questionBetween} " +
            s"date-input-two ${messages.PrivateResidenceRelief.questionEnd}"
        }
      }
    }

    "supplied with no errors and both inputs displayed" should {
      lazy val view = privateResidenceRelief(privateResidenceReliefForm(true, true), true, true, "date-input")
      lazy val document = Jsoup.parse(view.body)

      "have some hidden content" which {
        lazy val hiddenContent = document.body().select("#hidden")

        "which has two divs with a class of form-group" in {
          hiddenContent.select("div.form-group").size() shouldBe 2
        }
      }
    }

    "supplied with errors" should {
      val map = Map("isClaimingPRR" -> "")
      lazy val view = privateResidenceRelief(privateResidenceReliefForm(false, false).bind(map), false, false, "")
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
