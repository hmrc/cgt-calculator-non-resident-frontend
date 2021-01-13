/*
 * Copyright 2021 HM Revenue & Customs
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

import controllers.helpers.FakeRequestHelper
import models.QuestionAnswerModel
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Lang
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.helpers.questionAnswerRowNoLink

class QuestionAnswersRowNoLinkViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  implicit lazy val lang = mockMessage.lang

  "Creating questionAnswerRow" when {

    "passing in a String answer" should {
      val model = QuestionAnswerModel[String]("id", "answer", "question", None)
      lazy val result = questionAnswerRowNoLink(model, 2)
      lazy val doc = Jsoup.parse(result.body)

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
          doc.select("div#id-answer span").attr("class") shouldBe "lede summary-answer"
        }

        "has the text 'answer'" in {
          doc.select("div#id-answer span").text() shouldBe "answer"
        }
      }
    }

    "passing in an Int answer" should {
      val model = QuestionAnswerModel[Int]("id-two", 1, "question-two", Some("change-link"))
      lazy val result = questionAnswerRowNoLink(model, 2)
      lazy val doc = Jsoup.parse(result.body)

      "have the question with the text 'question-two'" in {
        doc.select("div#id-two-question span").text() shouldBe "question-two"
      }

      "have the answer '1'" in {
        doc.select("div#id-two-answer span").text() shouldBe "1"
      }
    }

    "passing in a BigDecimal answer" should {
      val model = QuestionAnswerModel[BigDecimal]("id", BigDecimal(1000.53), "question", Some("change-link"))
      lazy val result = questionAnswerRowNoLink(model, 2)
      lazy val doc = Jsoup.parse(result.body)

      "have the answer '£1,000'" in {
        doc.select("div#id-answer span").text() shouldBe "£1,000.53"
      }
    }

    "passing in a Date answer" should {
      val model = QuestionAnswerModel[LocalDate]("id", LocalDate.parse("2016-05-04"), "question", Some("change-link"))
      lazy val result = questionAnswerRowNoLink(model, 2)
      lazy val doc = Jsoup.parse(result.body)

      "have the answer '04 May 2016'" in {
        doc.select("div#id-answer span").text() shouldBe "4 May 2016"
      }
    }

    "passing in a non-matching type" should {
      val model = QuestionAnswerModel[Double]("id", 52.3, "question", Some("change-link"))
      lazy val result = questionAnswerRowNoLink(model, 2)
      lazy val doc = Jsoup.parse(result.body)

      "have a blank answer'" in {
        doc.select("div#id-answer span").text() shouldBe ""
      }
    }

    "passing in a boolean type with true" should {
      val model = QuestionAnswerModel[Boolean]("id", true, "question", Some("change-link"))
      lazy val result = questionAnswerRowNoLink(model, 2)
      lazy val doc = Jsoup.parse(result.body)

      "have an answer 'Yes'" in {
        doc.select("div#id-answer span").text() shouldBe "Yes"
      }
    }

    "passing in a boolean type with false" should {
      val model = QuestionAnswerModel[Boolean]("id", false, "question", Some("change-link"))
      lazy val result = questionAnswerRowNoLink(model, 2)
      lazy val doc = Jsoup.parse(result.body)

      "have an answer 'No'" in {
        doc.select("div#id-answer span").text() shouldBe "No"
      }
    }

    "passing in a tax rates tuple" should {
      val model = QuestionAnswerModel[(BigDecimal, Int, BigDecimal, Int)]("id", (1000, 10, 2000, 20), "question", Some("change-link"))
      lazy val result = questionAnswerRowNoLink(model, 2)
      lazy val doc = Jsoup.parse(result.body)

      "have a message for one tax rate" in {
        doc.select("div#id-answer span p").first().text() shouldBe "£1,000.00 at 10%"
      }

      "have a message for the additional tax rate" in {
        doc.select("div#id-answer span p").last().text() shouldBe "£2,000.00 at 20%"
      }
    }
  }
}
