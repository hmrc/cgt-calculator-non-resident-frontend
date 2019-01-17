/*
 * Copyright 2019 HM Revenue & Customs
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

import java.time.LocalDate

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

  private val testDate = LocalDate.of(2018, 2, 19)
  private val testDateString = "19 February 2018"

  "Private Residence Relief view" when {

    "there are no errors" should {

      "if both inputs should be displayed" should {

        lazy val view = privateResidenceRelief(
          privateResidenceReliefForm(
            showBefore = true,
            showAfter = true),
          daysBetweenShow = true,
          showFirstQuestion = true,
          Some(testDate),
          showOnlyFlatQuestion = false)
        lazy val document = Jsoup.parse(view.body)

        s"have a title of '${messages.PrivateResidenceRelief.question}'" in {
          document.title() shouldBe messages.PrivateResidenceRelief.question
        }

        "have a back link" which {
          lazy val backLink = document.body().select("#back-link")

          "has the text" in {
            backLink.text shouldBe messages.back
          }

          s"has a route to 'improvements'" in {
            backLink.attr("href") shouldBe controllers.routes.PropertyLivedInController.propertyLivedIn().url
          }
        }

        s"have a heading of '${messages.PrivateResidenceRelief.question}'" in {
          document.body().select("h1").text shouldBe messages.PrivateResidenceRelief.question
        }

        s"has the a paragraph with the text '${messages.PrivateResidenceRelief.intro}' " in {
          document.select("p#intro").text() shouldBe messages.PrivateResidenceRelief.intro
        }

        "has a section with a help link" which {
          lazy val containingDiv = document.select("#privateResidenceReliefLink")
          lazy val link = containingDiv.select("a")

          "has a target value of _blank" in {
            link.attr("target") shouldEqual "_blank"
          }

          s"has the text ${messages.PrivateResidenceRelief.findOut} ${messages.PrivateResidenceRelief.findOutAboutPRRLink}" in {
            containingDiv.text() shouldBe {s"${messages.PrivateResidenceRelief.findOut} " +
              s"${messages.PrivateResidenceRelief.findOutAboutPRRLink} ${messages.externalLink}"}
          }
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

        "have some hidden content" which {

          lazy val hiddenContent = document.body().select("#hidden")

          "which has a two divs with a class of form-group for the two questions" in {
            hiddenContent.select("div.form-group").size() shouldBe 2
          }

          "contains an input with the id 'daysClaimed'" in {
            hiddenContent.select("input").attr("id") shouldBe "daysClaimed"
          }

          s"has a paragraph with the text ${messages.PrivateResidenceRelief.helpTextSubtitle}" in {
            document.getElementById("formExplanation").text() shouldBe messages.PrivateResidenceRelief.formHelp
          }

          s"contains the questions ${messages.PrivateResidenceRelief.questionBefore}" in {
            hiddenContent.select("label").text() shouldBe messages.PrivateResidenceRelief.questionBefore + " " +
              s"${messages.PrivateResidenceRelief.questionBetween} " +
              s"$testDateString ${messages.PrivateResidenceRelief.questionBetweenEnd}"
          }

          s"contains the label ${messages.PrivateResidenceRelief.questionBefore}" in {
            document.getElementsByTag("label").get(2).text() shouldBe messages.PrivateResidenceRelief.questionBefore
          }

          s"contains the label ${messages.PrivateResidenceRelief.questionBetween} $testDateString " +
            s"${messages.PrivateResidenceRelief.questionBetweenEnd}" in {
            document.getElementsByTag("label").get(3).text() shouldBe s"${messages.PrivateResidenceRelief.questionBetween} " +
              s"$testDateString ${messages.PrivateResidenceRelief.questionBetweenEnd}"
          }

          "has an expandable help section" which {

            s"has the progressive disclosure title text ${messages.PrivateResidenceRelief.helpTextBeforeAfter}" in {
              hiddenContent.select("span.summary").text() shouldBe messages.PrivateResidenceRelief.helpTextBeforeAfter
            }

            s"has form help text ${messages.PrivateResidenceRelief.helpTextSubtitle}" in {
              hiddenContent.select("#bulletPointTitle").text() shouldBe messages.PrivateResidenceRelief.helpTextSubtitle
            }

            s"contains a bullet list of additional information list" which {

              s"has a bullet point with the text ${messages.PrivateResidenceRelief.questionBeforeWhyThisDate}" in {
                document.getElementsByTag("li").get(1).text() shouldBe messages.PrivateResidenceRelief.questionBeforeWhyThisDate
              }

              s"has a bullet point with the text '$testDateString ${messages.PrivateResidenceRelief.questionBeforeWhyThisDate}'" in {
                document.getElementsByTag("li").get(2).text() shouldBe s"$testDateString " + messages.PrivateResidenceRelief.questionBetweenWhyThisDate
              }
            }
          }
        }

        "have a button" which {
          lazy val button = document.select("button")

          "has the type 'submit'" in {
            button.attr("type") shouldBe "submit"
          }

          "has the id 'continue-button'" in {
            button.attr("id") shouldBe "continue-button"
          }
        }
      }

      "if only the acquisition date after tax start date input is displayed" should {

        lazy val view = privateResidenceRelief(
          privateResidenceReliefForm(
            showBefore = true,
            showAfter = false),
          daysBetweenShow = false,
          showFirstQuestion = true,
          Some(testDate),
          showOnlyFlatQuestion = true)
        lazy val document = Jsoup.parse(view.body)

        s"have a title of '${messages.PrivateResidenceRelief.question}'" in {
          document.title() shouldBe messages.PrivateResidenceRelief.question
        }

        "have some hidden content" which {

          lazy val hiddenContent = document.body().select("#hidden")

          "which has a two divs with a class of form-group for one question" in {
            hiddenContent.select("div.form-group").size() shouldBe 1
          }

          "contains an input with the id 'daysClaimed'" in {
            hiddenContent.select("input").attr("id") shouldBe "daysClaimed"
          }
          s"contains the label ${messages.PrivateResidenceRelief.questionAcquisitionDateAfterStartDate(testDateString)}" in {
            document.getElementsByTag("label").get(2).text() shouldBe messages.PrivateResidenceRelief.questionAcquisitionDateAfterStartDate(testDateString)
          }

          "has an expandable help section" which {

            s"has the progressive disclosure title text ${messages.PrivateResidenceRelief.helpTextJustBefore}" in {
              hiddenContent.select("span.summary").text() shouldBe messages.PrivateResidenceRelief.helpTextJustBefore
            }

            s"has form help text ${s"$testDateString " + messages.PrivateResidenceRelief.questionBetweenWhyThisDate}" in {
              hiddenContent.select("p#helpTextBetween").text() shouldBe s"$testDateString " + messages.PrivateResidenceRelief.questionBetweenWhyThisDate
            }
          }
        }
      }

      "if neither of the inputs are displayed" should {

        lazy val view = privateResidenceRelief(
          privateResidenceReliefForm(
            showBefore = false,
            showAfter = false),
          daysBetweenShow = false,
          showFirstQuestion = false,
          Some(testDate),
          showOnlyFlatQuestion = true)
        lazy val document = Jsoup.parse(view.body)

        s"have a title of '${messages.PrivateResidenceRelief.question}'" in {
          document.title() shouldBe messages.PrivateResidenceRelief.question
        }

        "display the yes/no buttons" in {
          document.select("label").size() shouldBe 2
        }

        "not have some hidden content" in {
          document.body().select("#formExplanation").size() shouldBe 0
        }
      }
    }

    "supplied with errors" should {
      val map = Map("isClaimingPRR" -> "")
      lazy val view = privateResidenceRelief(privateResidenceReliefForm(false, false).bind(map), false, false, None, false)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
