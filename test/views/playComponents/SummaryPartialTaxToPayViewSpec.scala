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
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Lang
import play.api.mvc.MessagesControllerComponents
import views.html.playComponents.summaryPartialTaxToPay

class SummaryPartialTaxToPayViewSpec
    extends CommonPlaySpec
    with WithCommonFakeApplication
    with FakeRequestHelper
    with MockitoSugar {
  implicit val mockLang = mock[Lang]
  implicit lazy val mockMessage = fakeApplication.injector
    .instanceOf[MessagesControllerComponents]
    .messagesApi
    .preferred(fakeRequest)
  lazy val summaryPartialTaxToPayView =
    fakeApplication.injector.instanceOf[summaryPartialTaxToPay]

  "The workingOutSummary partial" when {

    "supplied with a flat calculation with a taxable gain and both tax rates" should {

      lazy val view = summaryPartialTaxToPayView(
        taxToPay = 10000,
        gainAtBandOne = 1000,
        gainAtBandTwo = 500,
        taxAtBandOne = 100,
        taxAtBandTwo = 50,
        taxRateOne = 18,
        taxRateTwo = 28
      )(fakeRequestWithSession, mockMessage)
      lazy val doc = Jsoup.parse(view.body)

      s"have the text ${messages.yourTaxRate}" in {
        doc.select("h3").text shouldBe messages.yourTaxRate
      }

      "has a information line" which {
        s"has the text '${messages.incomeBandInfo}'" in {
          doc.select("div#yourTaxRate p").text shouldBe messages.incomeBandInfo
        }
      }

      "has a row for tax band one" which {
        s"has the text '${messages.taxRate("£1,000", 18)}'" in {
          doc.select("#taxBandOne-text").text shouldBe messages.taxRate(
            "£1,000",
            18
          )
        }

        "has the value '£100'" in {
          doc.select("#taxBandOne-amount").text shouldBe "£100"
        }
      }

      "has a row for tax band two" which {
        s"has the text '${messages.taxRate("£500", 28)}'" in {
          doc.select("#taxBandTwo-text").text shouldBe messages.taxRate(
            "£500",
            28
          )
        }

        "has the value '£50'" in {
          doc.select("#taxBandTwo-amount").text shouldBe "£50"
        }
      }

      "has a row for tax to pay" which {
        s"has the text '${messages.taxToPay}'" in {
          doc.select("#taxToPay-text").text shouldBe messages.taxToPay
        }

        "has the value '£10,000'" in {
          doc.select("#taxToPay-amount").text shouldBe "£10,000"
        }
      }
    }

    "supplied with a flat calculation with a taxable gain and only the lower tax rate" should {

      lazy val view = summaryPartialTaxToPayView(
        taxToPay = 10000,
        gainAtBandOne = 1000,
        gainAtBandTwo = 0,
        taxAtBandOne = 100,
        taxAtBandTwo = 0,
        taxRateOne = 10,
        taxRateTwo = 0
      )(fakeRequestWithSession, mockMessage)
      lazy val doc = Jsoup.parse(view.body)

      s"have the text ${messages.yourTaxRate}" in {
        doc.select("h3").text shouldBe messages.yourTaxRate
      }

      "does not have a row for tax band two" in {
        doc.select("#taxBandTwo-text").isEmpty shouldBe true
      }
    }

    "supplied with a flat calculation with a taxable gain and only the higher tax rate" should {

      lazy val view = summaryPartialTaxToPayView(
        taxToPay = 10000,
        gainAtBandOne = 0,
        gainAtBandTwo = 1000,
        taxAtBandOne = 0,
        taxAtBandTwo = 200,
        taxRateOne = 0,
        taxRateTwo = 20
      )(fakeRequestWithSession, mockMessage)
      lazy val doc = Jsoup.parse(view.body)

      s"have the text ${messages.yourTaxRate}" in {
        doc.select("h3").text shouldBe messages.yourTaxRate
      }

      "does not have a row for tax band one" in {
        doc.select("#taxBandOne-text").isEmpty shouldBe true
      }
    }

    "supplied with a flat calculation with a loss" should {

      lazy val view = summaryPartialTaxToPayView(
        taxToPay = 0,
        gainAtBandOne = 10,
        gainAtBandTwo = 10,
        taxAtBandOne = 10,
        taxAtBandTwo = 10,
        taxRateOne = 10,
        taxRateTwo = 10
      )(fakeRequestWithSession, mockMessage)
      lazy val doc = Jsoup.parse(view.body)

      s"does not have the text ${messages.yourTaxRate}" in {
        doc.select("h3").isEmpty shouldBe true
      }

      "does not have a information line" in {
        doc.select("#incomeBandInfo").isEmpty shouldBe true
      }

      "does not have a row for tax band one" in {
        doc.select("#taxBandOne-text").isEmpty shouldBe true
      }

      "does not have a row for tax band two" in {
        doc.select("#taxBandTwo-text").isEmpty shouldBe true
      }

      "has a row for tax to pay" which {
        s"has the text '${messages.taxToPay}'" in {
          doc.select("#taxToPay-text").text shouldBe messages.taxToPay
        }

        "has the value '£0'" in {
          doc.select("#taxToPay-amount").text shouldBe "£0"
        }
      }
    }
  }
}
