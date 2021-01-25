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

package views.whatNext

import assets.MessageLookup.{WhatNext => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import views.html.whatNext.whatNext

class WhatNextViewSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  "What next view should" when {
    implicit lazy val fakeApp: Application = fakeApplication

    lazy val view = whatNext(isDateAfter = false)(FakeRequest("GET", ""), mockMessage, fakeApplication, mockConfig)
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

    s"have a progressive disclosure heading of ${messages.saHeader}" in {
      doc.select("summary span").text() shouldBe messages.saHeader
    }

    s"have information for sa users of ${messages.saText}" in {
      doc.select("article p").get(2).text() shouldBe messages.saText
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

    "should produce the same output when render and f are called" in {
      whatNext.f(false)(FakeRequest("GET", ""), mockMessage, fakeApplication, mockConfig) shouldBe whatNext.render(false, FakeRequest("GET", ""), mockMessage, fakeApplication, mockConfig)
    }
  }

  "Disposal date is after 6 April 2020" which {
    lazy val view2 = whatNext(isDateAfter = true)(FakeRequest("GET", ""), mockMessage, fakeApplication, mockConfig)
    lazy val doc2 = Jsoup.parse(view2.body)

    "doesn't have information for sa users" in {
      doc2.select("article p").size shouldBe 2
    }

    "has a button for reporting" which {
      lazy val button = doc2.select("a.button")

      s"has the text ${messages.report}" in {
        button.text() shouldBe messages.report
      }

      "has an href linking to the nr report service" in {
        button.attr("href") shouldBe "/capital-gains-tax-uk-property/start/report-pay-capital-gains-tax-uk-property"
      }
    }
  }


}
