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
import models.QuestionAnswerModel
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.summary
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class SummaryViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {


  "Summary view" when {

    "supplied with a disposal date within the valid tax years" should {
      val questionAnswer = QuestionAnswerModel[String]("text", "1000", "test-question", None)
      val seqQuestionAnswers = Seq(questionAnswer, questionAnswer)

      lazy val view = summary(seqQuestionAnswers, "back-link", displayDateWarning = false, "flat", None)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '${messages.Summary.title}'" in {
        document.title() shouldBe messages.Summary.title
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has a class of 'back-link'" in {
          backLink.attr("class") shouldBe "back-link"
        }

        "has the text" in {
          backLink.text shouldBe messages.back
        }

        s"has a route to 'back-link'" in {
          backLink.attr("href") shouldBe "back-link"
        }
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate().url
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-xlarge" in {
          heading.attr("class") shouldBe "heading-xlarge"
        }

        s"has a span with the text ${messages.Summary.calculationDetailsTitle}" in {
          heading.text() shouldBe messages.Summary.calculationDetailsTitle
        }
      }


      "have a what to do next section" which {
        lazy val whatToDoNext = document.select("#whatToDoNext")

        "have a heading with the class 'heading-medium'" in {
          whatToDoNext.select("h2").attr("class") shouldBe "heading-medium"
        }

        "has the heading 'What to do next'" in {
          whatToDoNext.select("h2").text() shouldBe messages.Summary.whatToDoNextText
        }

        "should have the text describing what to do next" in {
          whatToDoNext.select("p").text() shouldBe s"${messages.Summary.whatToDoNextContent}"
        }
      }

      "have a continue button" which {
        lazy val continue = document.select("a.button")

        "have the text 'Continue" in {
          continue.text() shouldBe messages.continue
        }

        "have a link to /calculate-your-capital-gains/non-resident/what-next" in {
          continue.attr("href") shouldBe controllers.routes.WhatNextController.whatNext().url
        }
      }

      "have a save pdf button" which {
        lazy val savePDF = document.select("a.save-pdf-link")

        "which has the text 'Save as PDF'" in {
          savePDF.text() shouldBe messages.Summary.saveAsPdf
        }

        "which has the link to the summary report" in {
          savePDF.attr("href") shouldBe controllers.routes.ReportController.summaryReport().url
        }
      }
    }

    "supplied with a disposal date within the valid tax years" should {
      val questionAnswer = QuestionAnswerModel[String]("text", "1000", "test-question", None)
      val seqQuestionAnswers = Seq(questionAnswer, questionAnswer)
      lazy val view = summary(seqQuestionAnswers, "back-link", displayDateWarning = true, "flat", Some(100))
      lazy val document = Jsoup.parse(view.body)

      "display a tax year warning" in {
        document.select("div.notice-wrapper").size() shouldBe 1
      }
    }
  }
}
