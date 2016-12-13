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

import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.nonresident.privateResidenceRelief
import forms.nonresident.PrivateResidenceReliefForm._
import assets.MessageLookup.{NonResident => messages}

class PrivateResidenceReliefViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  "Private Residence Relief view" when {

    "supplied with no errors and neither day inputs displayed" should {
      lazy val view = privateResidenceRelief(privateResidenceReliefForm(false, false), false, false, "")(fakeRequest)
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
          backLink.attr("href") shouldBe controllers.nonresident.routes.ImprovementsController.improvements().url
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

      s"have a home link to '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.nonresident.routes.PrivateResidenceReliefController.submitPrivateResidenceRelief().url}'" in {
          form.attr("action") shouldBe controllers.nonresident.routes.PrivateResidenceReliefController.submitPrivateResidenceRelief().url
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

      "have a sidebar" which {
        lazy val sidebar = document.body().select("aside")
        lazy val link = sidebar.select("a")

        "contains one link" in {
          link.size() shouldBe 1
        }

        "has a link with the class 'external-link'" in {
          link.attr("class") shouldBe "external-link"
        }

        "has a link with a rel of 'external'" in {
          link.attr("rel") shouldBe "external"
        }

        "has a link with a target of '_blank'" in {
          link.attr("target") shouldBe "_blank"
        }

        "has a link with an href to 'https://www.gov.uk/tax-sell-home/private-residence-relief'" in {
          sidebar.select("#helpLink1").attr("href") shouldBe "https://www.gov.uk/tax-sell-home/private-residence-relief"
        }

        "has a link with the correct text" in {
          sidebar.select("#helpLink1").text() shouldBe s"${messages.PrivateResidenceRelief.helpLink} ${messages.externalLink}"
        }
      }
    }

    "supplied with no errors and the days before input displayed" should {
      lazy val view = privateResidenceRelief(privateResidenceReliefForm(true, false), false, true, "date-input")(fakeRequest)
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
      lazy val view = privateResidenceRelief(privateResidenceReliefForm(false, true), true, false, "date-input-two")(fakeRequest)
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
      lazy val view = privateResidenceRelief(privateResidenceReliefForm(true, true), true, true, "date-input")(fakeRequest)
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
      lazy val view = privateResidenceRelief(privateResidenceReliefForm(false, false).bind(map), false, false, "")(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
