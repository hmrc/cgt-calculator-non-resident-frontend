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
import models.TaxYearModel
import org.jsoup.Jsoup
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.helpers

class SummaryPartialViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "the summaryPartial" when {

    "the tax owed is zero and the date of disposal was in a valid tax year" should {

      lazy val view = helpers.summaryPartial(
        BigDecimal(0),
        TaxYearModel("2016/17", isValidYear = true, "2018/19")
      )(fakeRequestWithSession, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)

      "the header" should {

        "contain the title text £0" in {
          doc.select("h1").text shouldEqual "£0.00"
        }

        s"contain the second heading text ${messages.headingTwo("2016 to 2017")}" in {
          doc.select("h2").text shouldEqual messages.headingTwo("2016 to 2017")
        }
      }

      "not display the out of tax years message" in {
        doc.select("div#notice-summary").isEmpty shouldBe true
      }
    }

    "the tax owed is 400 and the date of disposal was in a valid tax year" should {

      lazy val view = helpers.summaryPartial(
        BigDecimal(400),
        TaxYearModel("2015/16", isValidYear = false, "2018/19")
      )(fakeRequestWithSession, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)

      "the header" should {

        "contain the title text £400.00" in {
          doc.select("h1").text shouldEqual "£400.00"
        }

        s"contain the second heading text ${messages.headingTwo("2015 to 2016")}" in {
          doc.select("h2").text shouldEqual messages.headingTwo("2015 to 2016")
        }
      }

      "display the out of tax years section" which {

        lazy val outsideTaxYearsSummary = doc.select("div#notice-summary")

        "is not empty" in {
          outsideTaxYearsSummary.isEmpty shouldBe false
        }

        "has an icon with the icon and icon-important classes" in {
          //Why is the icon in an italics tag?
          outsideTaxYearsSummary.select("i").hasClass("icon") shouldBe true
          outsideTaxYearsSummary.select("i").hasClass("icon-important") shouldBe true
        }

        s"has a visually hidden message of ${messages.warningHidden}" in {
          outsideTaxYearsSummary.select("span.visuallyhidden").text shouldBe messages.warningHidden
        }

        s"has a strong tag with the text ${messages.warningNoticeSummary}" in {
          outsideTaxYearsSummary.select("strong").text shouldBe messages.warningNoticeSummary
        }
      }
    }
  }
}
