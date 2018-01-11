/*
 * Copyright 2018 HM Revenue & Customs
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

import assets.MessageLookup.{NonResident => nrMessages}
import common.TestModels._
import controllers.helpers.FakeRequestHelper
import models._
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.summaryReport
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class SummaryReportViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {



  "The report summary view" when {

    "provided with a valid tax year" should {

      lazy val taxYear = TaxYearModel("2016/17", isValidYear = true, "2016/17")

      val totalTaxOwedModel = TotalTaxOwedModel(
        taxOwed = BigDecimal(1000.00),
        taxGain = 100.00,
        taxRate = 18,
        upperTaxGain = Some(100.00),
        upperTaxRate = Some(100),
        totalGain = 150.00,
        taxableGain = 100.00,
        prrUsed = None,
        otherReliefsUsed = None,
        allowableLossesUsed = None,
        aeaUsed = Some(11000.00),
        aeaRemaining = 0.00,
        broughtForwardLossesUsed = None,
        reliefsRemaining = None,
        allowableLossesRemaining = None,
        broughtForwardLossesRemaining = None,
        totalDeductions = None,
        taxOwedAtBaseRate = None,
        taxOwedAtUpperRate = None
      )

      val questionAnswer = QuestionAnswerModel[String]("text", "1000", "test-question", None)
      val seqQuestionAnswers = Seq(questionAnswer, questionAnswer)

      lazy val view = summaryReport(seqQuestionAnswers, totalTaxOwedModel, taxYear, sumModelFlat.calculationElectionModel.calculationType,
        disposalValue = 1000.00,
        acquisitionValue = 1000.00,
        totalCosts = 1000,
        flatGain = Some(1000.00))

      lazy val document = Jsoup.parse(view.body)

      "have the HMRC logo with the HMRC name" in {
        document.select("div.logo span").text should include(nrMessages.Report.logoText)
      }

      s"have the title ${nrMessages.Report.title}" in {
        document.select("span.calculate-your-cgt").text() shouldBe nrMessages.Report.title
      }

      "not have a notice summary" in {
        document.select("div.notice-wrapper").isEmpty shouldBe true
      }

      "have a 'You've told us' section that" in {
        document.select("div.check-your-answers-report h2").text should include(nrMessages.Summary.yourAnswers)
      }
    }

    "provided with an invalid tax year" should {
      lazy val taxYear = TaxYearModel("2018/19", isValidYear = false, "2016/17")

      val totalTaxOwedModel = TotalTaxOwedModel(
        taxOwed = BigDecimal(1000.00),
        taxGain = 100.00,
        taxRate = 18,
        upperTaxGain = Some(100.00),
        upperTaxRate = Some(100),
        totalGain = 150.00,
        taxableGain = 100.00,
        prrUsed = None,
        otherReliefsUsed = None,
        allowableLossesUsed = None,
        aeaUsed = Some(11000.00),
        aeaRemaining = 0.00,
        broughtForwardLossesUsed = None,
        reliefsRemaining = None,
        allowableLossesRemaining = None,
        broughtForwardLossesRemaining = None,
        totalDeductions = None,
        taxOwedAtBaseRate = None,
        taxOwedAtUpperRate = None
      )

      val questionAnswer = QuestionAnswerModel[String]("text", "1000", "test-question", None)
      val seqQuestionAnswers = Seq(questionAnswer, questionAnswer)

      lazy val view = summaryReport(seqQuestionAnswers, totalTaxOwedModel, taxYear, sumModelFlat.calculationElectionModel.calculationType,
        disposalValue = 1000.00,
        acquisitionValue = 1000.00,
        totalCosts = 1000,
        flatGain = Some(1000.00))

      lazy val document = Jsoup.parse(view.body)

      "have a notice summary" which {
        lazy val notice = document.body().select("div.notice-wrapper")

        "has a div with the class 'notice'" in {
          notice.select("div.notice-wrapper > div").attr("class") shouldBe "notice"
        }

        "has a message with class of 'bold-small'" in {
          notice.select("strong").attr("class") shouldBe "bold-small"
        }

        "has the correct message text" in {
          notice.select("strong").text() shouldBe nrMessages.Summary.noticeSummary
        }
      }
    }
  }
}
