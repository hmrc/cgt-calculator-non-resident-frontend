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

package views.whatNext

import assets.MessageLookup
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.play.test.UnitSpec
import assets.MessageLookup.{WhatNext => messages}
import config.ApplicationConfig
import org.jsoup.Jsoup
import views.html.whatNext.whatNext
import play.api.i18n.Messages.Implicits._
import play.api.test.FakeRequest

class WhatNextViewSpec extends UnitSpec with GuiceOneAppPerSuite with MockitoSugar {

  "What next view" should {
    lazy val config = ApplicationConfig
    lazy val view = whatNext()(FakeRequest("GET", ""), applicationMessages, config)
    lazy val doc = Jsoup.parse(view.body)

    s"have a title of ${messages.title}" in {
      doc.title() shouldBe messages.title
    }

    "have a back link" which {
      lazy val backLink = doc.select("a.back-link")

      s"should have the text ${messages.back}" in {
        backLink.text() shouldBe messages.back
      }

      "should have a link to the summary page" in {
        backLink.attr("href") shouldBe controllers.routes.SummaryController.summary().url
      }
    }

    s"have a header of ${messages.title}" in {
      doc.select("h1").text() shouldBe messages.title
    }

    s"have a list title of ${messages.listTitle}" in {
      doc.select("article p").get(0).text() shouldBe messages.listTitle
    }

    s"have a first list entry of ${messages.listOne}" in {
      doc.select("article li").get(0).text() shouldBe messages.listOne
    }

    s"have a second list entry of ${messages.listTwo}" in {
      doc.select("article li").get(1).text() shouldBe messages.listTwo
    }

    s"have a warning regarding penalties of ${messages.penaltyWarning}" in {
      doc.select("article p").get(1).text() shouldBe messages.penaltyWarning
    }

    s"have a secondary heading of ${messages.saHeader}" in {
      doc.select("h2").text() shouldBe messages.saHeader
    }

    s"have information for sa users of ${messages.saText}" in {
      doc.select("article p").get(2).text() shouldBe messages.saText
    }

    s"have information about logging in of ${messages.loginInformation}" in {
      doc.select("article p").get(3).text() shouldBe messages.loginInformation
    }

    "have a button for reporting" which {
      lazy val button = doc.select("a.button")

      s"has the text ${messages.report}" in {
        button.text() shouldBe messages.report
      }

      "has an href linking to the iForm" in {
        button.attr("href") shouldBe "https://online.hmrc.gov.uk/shortforms/form/NRCGT_Return"
      }
    }

    "have a link to finish" which {
      lazy val link = doc.select("article a").get(2)

      s"has the text ${messages.finish}" in {
        link.text() shouldBe messages.finish
      }

      "has an href linking to gov uk" in {
        link.attr("href") shouldBe "http://www.gov.uk"
      }
    }
  }
}
