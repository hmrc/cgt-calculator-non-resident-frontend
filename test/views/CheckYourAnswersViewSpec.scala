/*
 * Copyright 2024 HM Revenue & Customs
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
import assets.MessageLookup.NonResident.{CheckYourAnswers => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import models.QuestionAnswerModel
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.checkYourAnswers

import java.time.LocalDate

class CheckYourAnswersViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  lazy val lang: Lang = Lang("cy")
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val checkYourAnswersView = fakeApplication.injector.instanceOf[checkYourAnswers]
  lazy val pageTitle = s"""${messages.question} - ${commonMessages.pageHeading} - GOV.UK"""
  val mockMessagesApi = mock[MessagesApi]


  "The check your answers view" when {

    "provided with a valid sequence of question answers" should {

      val answersSequence = Seq(QuestionAnswerModel("dummyId", 200, "dummyQuestion", Some("google.com")))
      lazy val view = checkYourAnswersView(answersSequence, "some-back-link")(fakeRequest, mockMessage, lang, Some(mockMessagesApi))

      lazy val document = Jsoup.parse(view.body)

      s"has the title text $pageTitle" in {
        document.title shouldBe pageTitle
      }

      "have a back link" which {

        "should have the text" in {
          document.body.getElementsByClass("govuk-back-link").text shouldEqual MessageLookup.NonResident.back
        }

        "has the govuk-back-link class" in {
          document.select("a.govuk-back-link").hasClass("govuk-back-link") shouldBe true
        }

        s"should have a route too 'back-link'" in {
          document.body.getElementsByClass("govuk-back-link").attr("href") shouldEqual "#"
        }
      }

      "have a heading" which {
        lazy val heading = document.select("h1")
        s"has the title text ${messages.question}" in {
          heading.text() shouldBe messages.question
        }

        "has a class of 'govuk-heading-xl'" in {
          heading.attr("class") shouldBe "govuk-heading-xl"
        }
      }

      "has a continue button" which {
        lazy val continueButton = document.select("#submit")

        s"have the text ${MessageLookup.NonResident.continue}" in {
          continueButton.text() shouldBe MessageLookup.NonResident.continue
        }
      }
    }

    "should produce the same output when render and f are called" in {
      val answersSequence = Seq(QuestionAnswerModel("dummyId", 200, "dummyQuestion", Some("google.com")))

      checkYourAnswersView.f(answersSequence, "back-link")(fakeRequest, mockMessage, lang, Some(mockMessagesApi)) shouldBe checkYourAnswersView.render(answersSequence, "back-link", fakeRequest, mockMessage, lang, Some(mockMessagesApi))
    }
  }

  "Creating a single table of one row" when {

    "passing in a String answer" should {
      lazy val model = Seq(QuestionAnswerModel[String]("id", "answer", "question", Some("change-link")))
      lazy val result = checkYourAnswersView(model, "hello")(fakeRequest, mockMessage, lang, Some(mockMessagesApi))
      lazy val doc = Jsoup.parse(result.body)
      lazy val questionCell = doc.select("dl dt").first()
      lazy val answerCell = doc.select("dl dd").get(0)
      lazy val changeCell = doc.select("dl dd").get(1)

      "have a table row with a table row for the question with ID id-question" which {

        "has a question column with the question 'question'" in {
          questionCell.text() shouldBe "question"
        }

        "has a data column for the answer" which {
          "should have the class 'govuk-summary-list__value'" in {
            answerCell.attr("class") shouldBe "govuk-summary-list__value"
          }

          "should have the answer 'answer'" in {
            answerCell.text() shouldBe "answer"
          }
        }

        "has the hyper-link text 'Change'" in {
          changeCell.text should include(messages.change)
        }

        "has a link to 'change-link'" in {
          changeCell.select("a").attr("href") shouldBe "change-link"
        }
      }
    }

    "passing in a Int answer" should {
      lazy val model = Seq(QuestionAnswerModel[Int]("id", 200, "question", Some("change-link")))
      lazy val result = checkYourAnswersView(model, "hello")(fakeRequest, mockMessage, lang, Some(mockMessagesApi))
      lazy val doc = Jsoup.parse(result.body)
      lazy val dataColumnContents = doc.select("dl dd").get(0).text()

      s"generate a row with a data column with 200 as the data" in {
        dataColumnContents shouldBe "200"
      }
    }

    "passing in a BigDecimal answer" should {
      lazy val model = Seq(QuestionAnswerModel[BigDecimal]("id", BigDecimal(1000.01), "question", Some("change-link")))
      lazy val result = checkYourAnswersView(model, "hello")(fakeRequest, mockMessage, lang, Some(mockMessagesApi))
      lazy val doc = Jsoup.parse(result.body)
      lazy val dataColumnContents = doc.select("dl dd").get(0).text()

      s"generate a row with a data column with '£1,000.01' as the data" in {
        dataColumnContents shouldBe "£1,000.01"
      }
    }

    "passing in a Date answer" should {
      lazy val model = Seq(QuestionAnswerModel[LocalDate]("id", LocalDate.parse("2016-05-04"), "question", Some("change-link")))
      lazy val result = checkYourAnswersView(model, "hello")(fakeRequest, mockMessage, lang, Some(mockMessagesApi))
      lazy val doc = Jsoup.parse(result.body)
      lazy val dataColumnContents = doc.select("dl dd").get(0).text()

      s"generate a row with a data column with '4 May 2016' as the data" in {
        dataColumnContents shouldBe "4 May 2016"
      }
    }

    "passing in a Boolean answer" should {
      lazy val model = Seq(QuestionAnswerModel[Boolean]("id", true, "question", Some("change-link")))
      lazy val result = checkYourAnswersView(model, "hello")(fakeRequest, mockMessage, lang, Some(mockMessagesApi))
      lazy val doc = Jsoup.parse(result.body)
      lazy val dataColumnContents = doc.select("dl dd").get(0).text()

      s"generate a row with a data column with 'Yes' as the data'" in {
        dataColumnContents shouldBe "Yes"
      }
    }

    "passing in a non-matching type" should {
      lazy val model = Seq(QuestionAnswerModel[Double]("id", 50.2, "question", Some("change-link")))
      lazy val result = checkYourAnswersView(model, "hello")(fakeRequest, mockMessage, lang, Some(mockMessagesApi))
      lazy val doc = Jsoup.parse(result.body)

      "generate a data column with a blank answer" in {
        doc.select("dl dd").get(0).text() shouldBe ""
      }
    }
  }

  "Creating a table of multiple rows" should {
    val idString = "stringQA"
    val idBoolean = "booleanQA"

    val model = Seq(QuestionAnswerModel[String]("stringQA", "answer", "question", Some("change-link")),
      QuestionAnswerModel[Boolean]("booleanQA", false, "question", Some("change-link-diff")))
    lazy val result = checkYourAnswersView(model, "hello")(fakeRequest, mockMessage, lang, Some(mockMessagesApi))
    lazy val doc = Jsoup.parse(result.body)

    s"have a table row with a table row for the question with ID $idString" which {
      lazy val row = doc.select("div.govuk-summary-list__row").first()
      lazy val questionCell = row.select("dt")
      lazy val answerCell = row.select("dd").first()
      lazy val changeCell = row.select("dd").last()
      "has a question column with the question 'question'" in {
        questionCell.text() shouldBe "question"
      }

      "has a data column with the data 'answer'" in {
        answerCell.text() shouldBe "answer"
      }

      "has the hyper-link text 'Change'" in {
        changeCell.text() should include(messages.change)
      }

      "has a visually hidden tag" in {
        row.select("span").hasClass("govuk-visually-hidden") shouldBe true
      }

      s"has a visually hidden message of ${messages.hiddenText}" in {
        row.select("span").text() should include(messages.hiddenText + " " + "question")
      }

      "has a link to 'change-link'" in {
        row.select("a").attr("href") shouldBe "change-link"
      }

    }

    s"have a table row with a table row for the question with ID $idBoolean" which {
      lazy val row = doc.select("div.govuk-summary-list__row").get(1)
      lazy val questionCell = row.select("dt")
      lazy val answerCell = row.select("dd").first()
      lazy val changeCell = row.select("dd").last()
      "has a question column with the question 'question'" in {
        questionCell.text() shouldBe "question"
      }

      "has a data column with the data 'No'" in {
        answerCell.text() shouldBe "No"
      }

      "has a visually hidden tag" in {
        row.select("span").hasClass("govuk-visually-hidden") shouldBe true
      }

      s"has a visually hidden message of ${messages.hiddenText}" in {
        row.select("span").text() should include(messages.hiddenText + " " + "question")
      }

      "has the hyper-link text 'Change'" in {
        changeCell.text() should include(messages.change)
      }

      "has a link to 'change-link-diff'" in {
        row.select("a").attr("href") shouldBe "change-link-diff"
      }
    }
  }
}
