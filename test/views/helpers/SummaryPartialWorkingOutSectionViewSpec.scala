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
        disposalValue = 100000,
        acquisitionValue = 20000,
        totalCosts = 4000
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

      "has a row for disposal value" which {
        s"has the text '${messages.valueWhenSold}'" in {
          doc.select("#disposalValue-text").text shouldBe messages.valueWhenSold
        }

        "has the value '£100,000'" in {
          doc.select("#disposalValue-amount").text shouldBe "£100,000"
        }
      }

      "has a row for the acquisition value" which {
        s"has the text '${messages.valueWhenAcquired}'" in {
          doc.select("#acquisitionValue-text").text shouldBe messages.valueWhenAcquired
        }

        "has the value '£20,000'" in {
          doc.select("#acquisitionValue-amount").text shouldBe "£20,000"
        }
      }

      "has a row for the total costs" which {
        s"has the text '${messages.totalCosts}'" in {
          doc.select("#totalCosts-text").text shouldBe messages.totalCosts
        }

        "has the value '£4,000'" in {
          doc.select("#totalCosts-amount").text shouldBe "£4,000"
        }
      }
    }

    "supplied with a rebased calculation" should {

      lazy val view = helpers.summaryPartialWorkingOutSection(
        CalculationType.rebased,
        disposalValue = 100000,
        acquisitionValue = 20000,
        totalCosts = 4000
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

      "has a row for disposal value" which {
        s"has the text '${messages.valueWhenSold}'" in {
          doc.select("#disposalValue-text").text shouldBe messages.valueWhenSold
        }

        "has the value '£100,000'" in {
          doc.select("#disposalValue-amount").text shouldBe "£100,000"
        }
      }

      "has a row for the acquisition value" which {
        s"has the text '${messages.valueAtTaxStart}'" in {
          doc.select("#acquisitionValue-text").text shouldBe messages.valueAtTaxStart
        }

        "has the value '£20,000'" in {
          doc.select("#acquisitionValue-amount").text shouldBe "£20,000"
        }
      }

      "has a row for the total costs" which {
        s"has the text '${messages.totalCosts}'" in {
          doc.select("#totalCosts-text").text shouldBe messages.totalCosts
        }

        "has the value '£4,000'" in {
          doc.select("#totalCosts-amount").text shouldBe "£4,000"
        }
      }
    }

    "supplied with a time-apportioned calculation" should {

      lazy val view = helpers.summaryPartialWorkingOutSection(
        CalculationType.timeApportioned,
        disposalValue = 100000,
        acquisitionValue = 20000,
        totalCosts = 4000
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

      "has a row for disposal value" which {
        s"has the text '${messages.valueWhenSold}'" in {
          doc.select("#disposalValue-text").text shouldBe messages.valueWhenSold
        }

        "has the value '£100,000'" in {
          doc.select("#disposalValue-amount").text shouldBe "£100,000"
        }
      }

      "has a row for the acquisition value" which {
        s"has the text '${messages.valueWhenAcquired}'" in {
          doc.select("#acquisitionValue-text").text shouldBe messages.valueWhenAcquired
        }

        "has the value '£20,000'" in {
          doc.select("#acquisitionValue-amount").text shouldBe "£20,000"
        }
      }

      "has a row for the total costs" which {
        s"has the text '${messages.totalCosts}'" in {
          doc.select("#totalCosts-text").text shouldBe messages.totalCosts
        }

        "has the value '£4,000'" in {
          doc.select("#totalCosts-amount").text shouldBe "£4,000"
        }
      }
    }
  }
}
