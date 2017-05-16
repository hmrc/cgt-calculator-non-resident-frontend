/*
 * Copyright 2017 HM Revenue & Customs
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
import controllers.helpers.FakeRequestHelper
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import views.html.helpers
import org.jsoup.Jsoup

class SummaryPartialTaxableGainViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "The workingOutSummary partial" when {

    "supplied with a flat calculation" should {

      lazy val view = helpers.summaryPartialTaxableGain(
        gain = 3000,
        totalDeductions = 2000,
        taxableGain = 1000
      )(fakeRequestWithSession, applicationMessages)
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
