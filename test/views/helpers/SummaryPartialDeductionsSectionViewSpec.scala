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

import assets.MessageLookup.{SummaryPartialMessages => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Lang
import play.api.mvc.MessagesControllerComponents
import views.html.helpers.summaryPartialDeductionsSection


class SummaryPartialDeductionsSectionViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {
  implicit val mockLang = mock[Lang]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val summaryPartialDeductionsSectionView = fakeApplication.injector.instanceOf[summaryPartialDeductionsSection]
  "The deductions section with all options supplied" should {

    lazy val view = summaryPartialDeductionsSectionView(
      reliefsUsed = 10000,
      aeaUsed = 11000,
      inYearLossesUsed = 10,
      broughtForwardLossesUsed = 20,
      totalDeductions = 21030
    )(fakeRequestWithSession, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    s"have the h3 heading ${messages.deductionsSectionHeading}" in {
      doc.select("h3").text shouldBe messages.deductionsSectionHeading
    }

    "has a row for reliefs used" which {
      s"has the text '${messages.reliefsUsed}'" in {
        doc.select("#reliefsUsed-text").text shouldBe messages.reliefsUsed
      }

      "has the value '£10,000'" in {
        doc.select("#reliefsUsed-amount").text shouldBe "£10,000"
      }
    }

    "has a row for in year losses used" which {
      s"has the text '${messages.inYearLossesUsed}'" in {
        doc.select("#inYearLossesUsed-text").text shouldBe messages.inYearLossesUsed
      }

      "has the value '£10'" in {
        doc.select("#inYearLossesUsed-amount").text shouldBe "£10"
      }
    }

    "has a row for annual exempt amount used" which {
      s"has the text '${messages.aeaUsed}'" in {
        doc.select("#aeaUsed-text").text shouldBe messages.aeaUsed
      }

      "has the value '£11,000'" in {
        doc.select("#aeaUsed-amount").text shouldBe "£11,000"
      }
    }

    "has a row for brought forward losses used" which {
      s"has the text '${messages.broughtForwardLossesUsed}'" in {
        doc.select("#broughtForwardLossesUsed-text").text shouldBe messages.broughtForwardLossesUsed
      }

      "has the value '£20'" in {
        doc.select("#broughtForwardLossesUsed-amount").text shouldBe "£20"
      }
    }

    "has a row for total deductions used" which {
      s"has the text '${messages.totalDeductions}'" in {
        doc.select("#totalDeductions-text").text shouldBe messages.totalDeductions
      }

      "has the value '£21,030'" in {
        doc.select("#totalDeductions-amount").text shouldBe "£21,030"
      }
    }
  }

  "The deductions section with no options supplied" should {

    lazy val view = summaryPartialDeductionsSectionView(
      reliefsUsed = 0,
      aeaUsed = 11000,
      inYearLossesUsed = 0,
      broughtForwardLossesUsed = 0,
      totalDeductions = 21030
    )(fakeRequestWithSession, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    s"have the h3 heading ${messages.deductionsSectionHeading}" in {
      doc.select("h3").text shouldBe messages.deductionsSectionHeading
    }

    "does not have a row for reliefs used" in {
      doc.select("#reliefsUsed-text").isEmpty shouldBe true
    }

    "does not have a row for in year losses used" in {
      doc.select("#inYearLossesUsed-text").isEmpty shouldBe true
    }

    "does not have a row for brought forward losses used" in {
      doc.select("#broughtForwardLossesUsed-text").isEmpty shouldBe true
    }
  }
}
