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

package views

import java.time.LocalDate

import assets.MessageLookup
import assets.MessageLookup.NonResident.{CheckYourAnswers => messages}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import models.QuestionAnswerModel
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.checkYourAnswers

class CheckYourAnswersViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  lazy val lang: Lang = Lang("cy")
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  val mockMessagesApi = mock[MessagesApi]


  "The check your answers view" when {

    "provided with a valid sequence of question answers" should {

      val answersSequence = Seq(QuestionAnswerModel("dummyId", 200, "dummyQuestion", Some("google.com")))
      lazy val view = checkYourAnswers(answersSequence, "some-back-link")(fakeRequest, mockMessage, lang, fakeApplication, Some(mockMessagesApi), mockConfig)

      lazy val document = Jsoup.parse(view.body)

      s"has the title text ${messages.question}" in {
        document.title shouldBe messages.question
      }

      "have a back link" which {

        "should have the text" in {
          document.body.getElementById("back-link").text shouldEqual MessageLookup.NonResident.back
        }

        "has the back-link class" in {
          document.select("a#back-link").hasClass("back-link") shouldBe true
        }

        s"should have a route too 'back-link'" in {
          document.body.getElementById("back-link").attr("href") shouldEqual "some-back-link"
        }
      }

      "have a heading" which {
        lazy val heading = document.select("h1")
        s"has the title text ${messages.question}" in {
          heading.text() shouldBe messages.question
        }

        "has a class of 'heading-xlarge'" in {
          heading.attr("class") shouldBe "heading-xlarge"
        }
      }

      "has a continue button" which {
        lazy val continueButton = document.select("#continue-button")

        s"have the text ${MessageLookup.NonResident.continue}" in {
          continueButton.text() shouldBe MessageLookup.NonResident.continue
        }
      }
    }

    "should produce the same output when render and f are called" in {
      val answersSequence = Seq(QuestionAnswerModel("dummyId", 200, "dummyQuestion", Some("google.com")))

      checkYourAnswers.f(answersSequence, "back-link")(fakeRequest, mockMessage, lang, fakeApplication, Some(mockMessagesApi), mockConfig) shouldBe checkYourAnswers.render(answersSequence, "back-link", fakeRequest, mockMessage, lang, fakeApplication, Some(mockMessagesApi), mockConfig)
    }
  }

  "Creating a single table of one row" when {

    "passing in a String answer" should {
      lazy val model = Seq(QuestionAnswerModel[String]("id", "answer", "question", Some("change-link")))
      lazy val result = checkYourAnswers(model, "hello")(fakeRequest, mockMessage, lang, fakeApplication, Some(mockMessagesApi), mockConfig)
      lazy val doc = Jsoup.parse(result.body)

      "have a table row with a table row for the question with ID id-question" which {

        "has a question column with the question 'question'" in {
          doc.select("tr > td").first().text() shouldBe "question"
        }

        "has a data column for the answer" which {

          "should have the id 'id'" in {
            doc.select("tr > td").get(1).attr("id") shouldBe "id-answer"
          }

          "should have the answer 'answer'" in {
            doc.select("tr > td").get(1).text() shouldBe "answer"
          }
        }

        "has the hyper-link text 'Change'" in {
          doc.select("tr > td").last().text should include(messages.change)
        }

        "has a link to 'change-link'" in {
          doc.select("tr > td > a").attr("href") shouldBe "change-link"
        }
      }
    }

    "passing in a Int answer" should {
      lazy val model = Seq(QuestionAnswerModel[Int]("id", 200, "question", Some("change-link")))
      lazy val result = checkYourAnswers(model, "hello")(fakeRequest, mockMessage, lang, fakeApplication, Some(mockMessagesApi), mockConfig)
      lazy val doc = Jsoup.parse(result.body)
      lazy val dataColumnContents = doc.select("tr > td").get(1).text()

      s"generate a row with a data column with 200 as the data" in {
        dataColumnContents shouldBe "200"
      }
    }

    "passing in a BigDecimal answer" should {
      lazy val model = Seq(QuestionAnswerModel[BigDecimal]("id", BigDecimal(1000.01), "question", Some("change-link")))
      lazy val result = checkYourAnswers(model, "hello")(fakeRequest, mockMessage, lang, fakeApplication, Some(mockMessagesApi), mockConfig)
      lazy val doc = Jsoup.parse(result.body)
      lazy val dataColumnContents = doc.select("tr > td").get(1).text()

      s"generate a row with a data column with '£1,000.01' as the data" in {
        dataColumnContents shouldBe "£1,000.01"
      }
    }

    "passing in a Date answer" should {
      lazy val model = Seq(QuestionAnswerModel[LocalDate]("id", LocalDate.parse("2016-05-04"), "question", Some("change-link")))
      lazy val result = checkYourAnswers(model, "hello")(fakeRequest, mockMessage, lang, fakeApplication, Some(mockMessagesApi), mockConfig)
      lazy val doc = Jsoup.parse(result.body)
      lazy val dataColumnContents = doc.select("tr > td").get(1).text()

      s"generate a row with a data column with '4 May 2016' as the data" in {
        dataColumnContents shouldBe "4 May 2016"
      }
    }

    "passing in a Boolean answer" should {
      lazy val model = Seq(QuestionAnswerModel[Boolean]("id", true, "question", Some("change-link")))
      lazy val result = checkYourAnswers(model, "hello")(fakeRequest, mockMessage, lang, fakeApplication, Some(mockMessagesApi), mockConfig)
      lazy val doc = Jsoup.parse(result.body)
      lazy val dataColumnContents = doc.select("tr > td").get(1).text()

      s"generate a row with a data column with 'Yes' as the data'" in {
        dataColumnContents shouldBe "Yes"
      }
    }

    "passing in a non-matching type" should {
      lazy val model = Seq(QuestionAnswerModel[Double]("id", 50.2, "question", Some("change-link")))
      lazy val result = checkYourAnswers(model, "hello")(fakeRequest, mockMessage, lang, fakeApplication, Some(mockMessagesApi), mockConfig)
      lazy val doc = Jsoup.parse(result.body)

      "generate a data column with a blank answer" in {
        doc.select("id-data").text() shouldBe ""
      }
    }
  }

  "Creating a table of multiple rows" should {
    val idString = "stringQA"
    val idBoolean = "booleanQA"

    val model = Seq(QuestionAnswerModel[String]("stringQA", "answer", "question", Some("change-link")),
      QuestionAnswerModel[Boolean]("booleanQA", false, "question", Some("change-link-diff")))
    lazy val result = checkYourAnswers(model, "hello")(fakeRequest, mockMessage, lang, fakeApplication, Some(mockMessagesApi), mockConfig)
    lazy val doc = Jsoup.parse(result.body)

    s"have a table row with a table row for the question with ID $idString" which {
      lazy val row = doc.select("tr").get(1).select("td")
      "has a question column with the question 'question'" in {
        row.first().text() shouldBe "question"
      }

      "has a data column with the data 'answer'" in {
        row.get(1).text() shouldBe "answer"
      }

      "has the hyper-link text 'Change'" in {
        row.last().text() should include(messages.change)
      }

      "has a visually hidden tag" in {
        row.select("span").hasClass("visuallyhidden") shouldBe true
      }

      s"has a visually hidden message of ${messages.hiddenText}" in {
        row.select("span").text() should include(messages.hiddenText + " " + "question")
      }

      "has a link to 'change-link'" in {
        row.select("a").attr("href") shouldBe "change-link"
      }

    }

    s"have a table row with a table row for the question with ID $idBoolean" which {
      lazy val row = doc.select("tr").get(2).select("td")
      "has a question column with the question 'question'" in {
        row.first().text() shouldBe "question"
      }

      "has a data column with the data 'No'" in {
        row.get(1).text() shouldBe "No"
      }

      "has a visually hidden tag" in {
        row.select("span").hasClass("visuallyhidden") shouldBe true
      }

      s"has a visually hidden message of ${messages.hiddenText}" in {
        row.select("span").text() should include(messages.hiddenText + " " + "question")
      }

      "has the hyper-link text 'Change'" in {
        row.last().text() should include(messages.change)
      }

      "has a link to 'change-link-diff'" in {
        row.select("a").attr("href") shouldBe "change-link-diff"
      }
    }
  }
}
