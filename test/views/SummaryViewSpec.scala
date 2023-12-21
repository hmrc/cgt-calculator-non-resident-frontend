/*
 * Copyright 2023 HM Revenue & Customs
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

import assets.MessageLookup.NonResident.Summary
import assets.MessageLookup.{NonResident => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import models.{QuestionAnswerModel, TaxYearModel, TotalTaxOwedModel}
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Lang
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.summary

class SummaryViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  implicit val mockLang = mock[Lang]
  lazy val summaryView = fakeApplication.injector.instanceOf[summary]
  val questionAnswer = QuestionAnswerModel[String]("text", "1000", "test-question", None)
  val seqQuestionAnswers = Seq(questionAnswer, questionAnswer)

  "Summary view" when {
    "supplied with a disposal date within the valid tax years" should {
      val totalTaxOwedModel = TotalTaxOwedModel(100000, 500, 20, None, None, 500, 500, None, None, None, None, 0, None, None, None, None, None, None, None)
      val taxYearModel: TaxYearModel = TaxYearModel("2016/17", isValidYear = true, "2016/17")
      lazy val view = summaryView(totalTaxOwedModel, taxYearModel, "flat", 1000.0, 100, 100, "back-link", showUserResearchPanel = false, questionsForPrint = seqQuestionAnswers)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '${messages.Summary.title("2016 to 2017")}'" in {
        document.title() shouldBe messages.Summary.title("2016 to 2017")
      }

      "have a back link" which {
        lazy val backLink = document.body().select(".govuk-back-link")

        "has the text" in {
          backLink.text shouldBe messages.back
        }

        s"has a route to 'back-link'" in {
          backLink.attr("href") shouldBe "#"
        }
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate.url}'" in {
        document.select("body > header > div.govuk-header.hmrc-header > div > div.govuk-header__content > a").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate.url
      }

      "have a heading" which {
        lazy val heading = document.body().select("#tax-owed-banner > div > strong")

        s"has a span with the text '£100,000.00'" in {
          heading.text() shouldBe "£100,000.00"
        }
      }

      "have a what to do next section" which {
        lazy val whatToDoNext = document.select("#whatToDoNext")

        "has the heading 'What to do next'" in {
          whatToDoNext.select("h2").text() shouldBe messages.Summary.whatToDoNextText
        }

        "should have the text describing what to do next" in {
          whatToDoNext.select("p").text() shouldBe s"${messages.Summary.whatToDoNextContent}"
        }
      }

      "has a print Button" which {
        lazy val printSection = document.select("#print")
        lazy val link = printSection.select("a")

        "has the class bold-small" in {
          link.hasClass("govuk-link") shouldBe true
        }

        s"links to #" in {
          link.attr("href") shouldBe "#"
        }

        s"has the text ${messages.Summary.print}" in {
          link.text shouldBe messages.Summary.print
        }
      }

      "have a continue button" which {
        lazy val continue = document.select(".govuk-button")

        "have the text 'Continue" in {
          continue.text() shouldBe messages.continue
        }

        "have a link to /calculate-your-capital-gains/non-resident/what-next" in {
          continue.attr("href") shouldBe controllers.routes.WhatNextController.whatNext.url
        }
      }

      "does not have ur panel" in {
        document.select("div#ur-panel").size() shouldBe 0
      }

      "have a 'You've told us' section that" in {
        document.select("caption").text should include(messages.Summary.yourAnswers)
      }

      "should produce the same output when render and apply are called" in {
        summaryView(totalTaxOwedModel, taxYearModel, "flat", 1000.0, 100, 100, "back-link", Some(BigDecimal(100.1)), BigDecimal(10000.0), false, questionsForPrint = seqQuestionAnswers)(fakeRequest, mockMessage) shouldBe
          summaryView.render(totalTaxOwedModel, taxYearModel, "flat", 1000.0, 100, 100, "back-link", Some(BigDecimal(100.1)), BigDecimal(10000.0), false, questionsForPrint = seqQuestionAnswers, fakeRequest, mockMessage)
      }
    }

    "supplied with a disposal date not within the valid tax years" should {
      val totalTaxOwedModel = TotalTaxOwedModel(500, 500, 20, None, None, 500, 500, None, None, None, None, 0, None, None, None, None, None, None, None)
      val taxYearModel: TaxYearModel = TaxYearModel("2018/19", isValidYear = false, "2017/18")
      lazy val view = summaryView(totalTaxOwedModel, taxYearModel, "flat", 1000.0, 100, 100, "back-url", showUserResearchPanel = true, questionsForPrint = seqQuestionAnswers)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "display a tax year warning" in {
        document.select(".govuk-warning-text__text").size() shouldBe 1
        document.select(".govuk-warning-text__text").text() shouldBe messages.Summary.newNoticeSummary
      }

      "have ur panel" in {
        document.toString.contains("Help improve HMRC services")
      }

    }
  }
}
