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
import common.nonresident.CalculationType
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import views.html.helpers

class SummaryPartialWorkingOutSectionViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "The workingOutSummary partial" when {

    "supplied with a flat calculation" should {

      lazy val view = helpers.summaryPartialWorkingOutSection(
        CalculationType.flat,
        saleValue = 100000,
        acquisitionValue = 20000,
        allCosts = 4000
      )(fakeRequestWithSession, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)

      s"have the h3 heading ${messages.workingOutSectionHeading}" in {
        doc.select("h3").text shouldBe messages.workingOutSectionHeading
      }

      s"have the message for a flat calculation type ${messages.flatCalculationSummary}" in {
        doc.select("p.lede").text shouldBe messages.flatCalculationSummary
      }

      s"have the text ${messages.yourTotalGain}" in {
        doc.select("h4").text shouldBe messages.yourTotalGain
      }
    }

    "supplied with a rebased calculation" should {

      lazy val view = helpers.summaryPartialWorkingOutSection(
        CalculationType.rebased,
        saleValue = 100000,
        acquisitionValue = 20000,
        allCosts = 4000
      )(fakeRequestWithSession, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)

      s"have the h3 heading ${messages.workingOutSectionHeading}" in {
        doc.select("h3").text shouldBe messages.workingOutSectionHeading
      }

      s"have the message for a rebased calculation type ${messages.rebasedCalculationSummary} ${messages.rebasedCalculationSummaryDate}" in {
        doc.select("p.lede").text shouldBe s"${messages.rebasedCalculationSummary} ${messages.rebasedCalculationSummaryDate}"
      }

      s"have the text ${messages.yourTotalGain}" in {
        doc.select("h4").text shouldBe messages.yourTotalGain
      }
    }

    "supplied with a time-apportioned calculation" should {

      lazy val view = helpers.summaryPartialWorkingOutSection(
        CalculationType.timeApportioned,
        saleValue = 100000,
        acquisitionValue = 20000,
        allCosts = 4000
      )(fakeRequestWithSession, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)

      s"have the h3 heading ${messages.workingOutSectionHeading}" in {
        doc.select("h3").text shouldBe messages.workingOutSectionHeading
      }

      s"have the message for a time calculation type ${messages.timeCalculationSummary} ${messages.timeCalculationSummaryDate}" in {
        doc.select("p.lede").text shouldBe s"${messages.timeCalculationSummary} ${messages.timeCalculationSummaryDate}"
      }

      s"have the text ${messages.yourTotalGain}" in {
        doc.select("h4").text shouldBe messages.yourTotalGain
      }
    }
  }
}
