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

package views.helpers

import assets.MessageLookup.{SummaryPartialMessages => messages}
import common.nonresident.Flat
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.helpers.FakeRequestHelper
import models.{TaxYearModel, TotalTaxOwedModel}
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{Lang, Messages}
import play.api.mvc.MessagesControllerComponents
import views.html.playComponents.summaryPartial

class SummaryPartialViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  implicit val mockLang: Lang = mock[Lang]
  lazy val summaryPartialView: summaryPartial = fakeApplication.injector.instanceOf[summaryPartial]

  "the summaryPartial" should {

    "the tax owed is zero and the date of disposal was in a valid tax year" should {

      val totalTaxOwedModel = TotalTaxOwedModel(
        taxOwed = 0,
        taxGain = 500,
        taxRate = 20,
        upperTaxGain = None,
        upperTaxRate = None,
        totalGain = 500,
        taxableGain = 500,
        prrUsed = None,
        otherReliefsUsed = None,
        allowableLossesUsed = None,
        aeaUsed = None,
        aeaRemaining = 2000.00,
        broughtForwardLossesUsed = None,
        reliefsRemaining = None,
        allowableLossesRemaining = Some(5000.00),
        broughtForwardLossesRemaining = Some(27000.00),
        totalDeductions = None,
        taxOwedAtBaseRate = None,
        taxOwedAtUpperRate = None
      )


      lazy val view = summaryPartialView(
        totalTaxOwedModel,
        TaxYearModel("2016/17", isValidYear = true, "2016/17"),
        Flat,
        100.00,
        100.00,
        0
      )(using fakeRequestWithSession, mockMessage)
      lazy val doc = Jsoup.parse(view.body)

      "the header" should {

        s"contain the title text ${messages.headingTwo("2016 to 2017")}" in {
          doc.select("h1").text shouldEqual messages.headingTwo("2016 to 2017")
        }

        "contain the second heading text £0" in {
          doc.select("#tax-owed-banner > div > strong").text shouldEqual "£0.00"
        }

      }

      "not display the out of tax years message" in {
        doc.select("div#notice-summary").isEmpty shouldBe true
      }

      "display the section for Working Out" in {
        doc.select("div#workedOutSection").isEmpty shouldBe false
      }

      "display the section for Deductions" in {
        doc.select("div#deductionsSection").isEmpty shouldBe false
      }

      "display the section for Taxable Gain"in {
        doc.select("div#yourTaxableGain").isEmpty shouldBe false
      }

      "have a section for the deductions remaining" should {

        s"have the heading ${messages.remainingDeductions}" in {
          doc.select("div#remainingDeductions h2")
        }

        "have a row for in Year Losses" which {
          s"has the text '${messages.inYearLossesRemaining("2016 to 2017")}'" in {
            doc.select("#inYearLossesRemaining-text").text shouldBe messages.inYearLossesRemaining("2016 to 2017")
          }

          "has the value '£5,000'" in {
            doc.select("#inYearLossesRemaining-amount").text shouldBe "£5,000"
          }
        }

        "has a row for aea remaining" which {
          s"has the text '${messages.aeaRemaining("2016 to 2017")}'" in {
            doc.select("#aeaRemaining-text").text shouldBe messages.aeaRemaining("2016 to 2017")
          }

          "has the value '£2,000'" in {
            doc.select("#aeaRemaining-amount").text shouldBe "£2,000"
          }
        }

        "has a row for brought forward losses" which {
          s"has the text '${messages.broughtForwardLossesRemaining}'" in {
            doc.select("#broughtForwardLossesRemaining-text").text shouldBe messages.broughtForwardLossesRemaining
          }

          "has the value '£27,000'" in {
            doc.select("#broughtForwardLossesRemaining-amount").text shouldBe "£27,000"
          }
        }
      }
    }

    "the tax owed is 400 and the date of disposal was in a valid tax year" should {

      val totalTaxOwedModel = TotalTaxOwedModel(
        taxOwed = 400,
        taxGain = 500,
        taxRate = 20,
        upperTaxGain = None,
        upperTaxRate = None,
        totalGain = 500,
        taxableGain = 500,
        prrUsed = None,
        otherReliefsUsed = None,
        allowableLossesUsed = None,
        aeaUsed = None,
        aeaRemaining = 0,
        broughtForwardLossesUsed = None,
        reliefsRemaining = None,
        allowableLossesRemaining = None,
        broughtForwardLossesRemaining = None,
        totalDeductions = None,
        taxOwedAtBaseRate = None,
        taxOwedAtUpperRate = None
      )

      lazy val view = summaryPartialView(
        totalTaxOwedModel,
        TaxYearModel("2018/19", isValidYear = false, "2016/17"),
        Flat,
        100.00,
        100.00,
        0
      )(using fakeRequestWithSession, mockMessage)
      lazy val doc = Jsoup.parse(view.body)

      "the header" should {

        s"contain the title text ${messages.headingTwo("2018 to 2019")}" in {
          doc.select("h1").text shouldEqual messages.headingTwo("2018 to 2019")
        }

        "contain the second heading text £400.00" in {
          doc.select("#tax-owed-banner > div > strong").text shouldEqual "£400.00"
        }
      }

      "display the out of tax years section" which {

        lazy val outsideTaxYearsSummary = doc.select(".govuk-warning-text")

        "is not empty" in {
          outsideTaxYearsSummary.isEmpty shouldBe false
        }

        s"has a visually hidden message of ${messages.warningHidden}" in {
          outsideTaxYearsSummary.select(".govuk-warning-text .govuk-visually-hidden").text shouldBe messages.warningHidden
        }

        s"has a strong tag with the text ${messages.warningNoticeSummary}" in {
          outsideTaxYearsSummary.select("strong").text shouldBe messages.warningNoticeSummary
        }
      }

      "have a section for the deductions remaining" should {

        s"have the heading ${messages.remainingDeductions}" in {
          doc.select("div#remainingDeductions h2").text shouldBe messages.remainingDeductions
        }

        "not display a row for in Year Losses" in {
          doc.select("#inYearLossesRemaining-text").isEmpty shouldBe true
        }

        "has a row for aea remaining" which {
          s"has the text '${messages.aeaRemaining("2018 to 2019")}'" in {
            doc.select("#aeaRemaining-text").text shouldBe messages.aeaRemaining("2018 to 2019")
          }

          "has the value '£0'" in {
            doc.select("#aeaRemaining-amount").text shouldBe "£0"
          }
        }

        "does not have a row for brought forward losses" in {
          doc.select("#broughtForwardLossesRemaining-text").isEmpty shouldBe true
        }
      }
    }
  }
}
