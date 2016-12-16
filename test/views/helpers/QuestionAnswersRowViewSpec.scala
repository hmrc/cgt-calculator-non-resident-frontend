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

package views.helpers

import java.time.LocalDate

import models.QuestionAnswerModel
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.UnitSpec
import views.html.helpers.questionAnswerRow
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class QuestionAnswersRowViewSpec extends UnitSpec {

  "Creating questionAnswerRow" when {

    "passing in a String answer" should {
      val model = QuestionAnswerModel[String]("id", "answer", "question", Some("change-link"))
      val result = questionAnswerRow(model, 2)
      val doc = Jsoup.parse(result.body)

      "have a div for the question with an id of 'id-question' which" which {

        "has a class of 'grid-layout__column grid-layout__column--1-2'" in {
          doc.select("div#id-question").attr("class") shouldBe "grid-layout__column grid-layout__column--1-2"
        }

        "has a span with a class of 'lede'" in {
          doc.select("div#id-question span").attr("class") shouldBe "lede"
        }

        "has the text 'question'" in {
          doc.select("div#id-question span").text() shouldBe "question"
        }
      }

      "have a div for the answer with an id of 'id-answer'" which {

        "has a class of 'grid-layout__column grid-layout__column--1-2'" in {
          doc.select("div#id-answer").attr("class") shouldBe "grid-layout__column grid-layout__column--1-2"
        }

        "has a change link with a class of 'lede' and 'summary-answer" in {
          doc.select("div#id-answer a").attr("class") shouldBe "lede summary-answer"
        }

        "has a change link to 'change-link'" in {
          doc.select("div#id-answer a").attr("href") shouldBe "change-link"
        }

        "has the text 'answer'" in {
          doc.select("div#id-answer a").text() shouldBe "answer"
        }
      }
    }

    "passing in an Int answer" should {
      val model = QuestionAnswerModel[Int]("id-two", 1, "question-two", Some("other-change-link"))
      val result = questionAnswerRow(model, 2)
      val doc = Jsoup.parse(result.body)

      "have the question with the text 'question-two'" in {
        doc.select("div#id-two-question span").text() shouldBe "question-two"
      }

      "have the answer '1'" in {
        doc.select("div#id-two-answer a").text() shouldBe "1"
      }

      "have a change link to 'other-change-link'" in {
        doc.select("div#id-two-answer a").attr("href") shouldBe "other-change-link"
      }
    }

    "passing in a BigDecimal answer" should {
      val model = QuestionAnswerModel[BigDecimal]("id", BigDecimal(1000.53), "question", Some("change-link"))
      val result = questionAnswerRow(model, 2)
      val doc = Jsoup.parse(result.body)

      "have the answer '£1,000'" in {
        doc.select("div#id-answer a").text() shouldBe "£1,000.53"
      }
    }

    "passing in a Date answer" should {
      val model = QuestionAnswerModel[LocalDate]("id", LocalDate.parse("2016-05-04"), "question", Some("change-link"))
      val result = questionAnswerRow(model, 2)
      val doc = Jsoup.parse(result.body)

      "have the answer '04 May 2016'" in {
        doc.select("div#id-answer a").text() shouldBe "4 May 2016"
      }
    }

    "passing in a non-matching type" should {
      val model = QuestionAnswerModel[Double]("id", 52.3, "question", Some("change-link"))
      val result = questionAnswerRow(model, 2)
      val doc = Jsoup.parse(result.body)

      "have a blank answer'" in {
        doc.select("div#id-answer a").text() shouldBe ""
      }
    }
  }
}
