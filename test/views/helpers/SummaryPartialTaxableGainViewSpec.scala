/*
 * Copyright 2022 HM Revenue & Customs
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
import views.html.helpers
import views.html.helpers.summaryPartialTaxableGain

class SummaryPartialTaxableGainViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  implicit val mockLang = mock[Lang]
  lazy val summaryPartialTaxableGainView = fakeApplication.injector.instanceOf[summaryPartialTaxableGain]

  "The workingOutSummary partial" when {

    "supplied with a flat calculation" should {

      lazy val view = summaryPartialTaxableGainView(
        gain = 3000,
        totalDeductions = 2000,
        taxableGain = 1000
      )(fakeRequestWithSession, mockMessage)
      lazy val doc = Jsoup.parse(view.body)

      s"have the text ${messages.yourTaxableGain}" in {
        doc.select("h3").text shouldBe messages.yourTaxableGain
      }

      "has a row for total gain" which {
        s"has the text '${messages.gain}'" in {
          doc.select("#gain-text").text shouldBe messages.gain
        }

        "has the value '£3,000'" in {
          doc.select("#gain-amount").text shouldBe "£3,000"
        }
      }

      "has a row for minus deductions" which {
        s"has the text '${messages.minusDeductions}'" in {
          doc.select("#minusDeductions-text").text shouldBe messages.minusDeductions
        }

        "has the value '£2,000'" in {
          doc.select("#minusDeductions-amount").text shouldBe "£2,000"
        }
      }

      "has a row for taxable gain" which {
        s"has the text '${messages.taxableGain}'" in {
          doc.select("#taxableGain-text").text shouldBe messages.taxableGain
        }

        "has the value '£1,000'" in {
          doc.select("#taxableGain-amount").text shouldBe "£1,000"
        }
      }
    }
  }
}
