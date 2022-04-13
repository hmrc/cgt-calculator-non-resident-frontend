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
  val whatNextView = fakeApplication.injector.instanceOf[whatNext]

  "What next view should" when {
    implicit lazy val fakeApp: Application = fakeApplication

    lazy val view = whatNextView(isDateAfter = false)(FakeRequest("GET", ""), mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    s"have a title of ${messages.title}" in {
      doc.title() shouldBe messages.title
    }

    "have a back link" which {
      lazy val backLink = doc.select("#back-link")

      s"should have the text ${messages.back}" in {
        backLink.text() shouldBe messages.back
      }

      "should have a link to the summary page" in {
        backLink.attr("href") shouldBe "javascript:history.back()"
      }
    }

    s"have a header of ${messages.title}" in {
      doc.select("h1").text() shouldBe messages.heading
    }

    s"have a list title of ${messages.listTitle}" in {
      doc.select("#list-title").text() shouldBe messages.listTitle
    }

    s"have a first list entry of ${messages.listOne}" in {
      doc.select("#item1").text() shouldBe messages.listOne
    }

    s"have a second list entry of ${messages.listTwo}" in {
      doc.select("#item2").text() shouldBe messages.listTwo
    }

    s"have a warning regarding penalties of ${messages.penaltyWarning}" in {
      doc.select("#penalty-warning").text() shouldBe messages.penaltyWarning
    }

    s"have a progressive disclosure heading of ${messages.saHeader}" in {
      doc.select(".govuk-details__summary-text").text() shouldBe messages.saHeader
    }

    s"have information for sa users of ${messages.saText}" in {
      doc.select(".govuk-details__text").text() shouldBe messages.saText
    }

    "have a button for reporting" which {
      lazy val button = doc.select(".govuk-button")

      s"has the text ${messages.report}" in {
        button.text() shouldBe messages.report
      }

      "has an href linking to the iForm" in {
        button.attr("href") shouldBe "https://online.hmrc.gov.uk/shortforms/form/NRCGT_Return"
      }
    }

    "have a link to finish" which {
      lazy val link = doc.select("#return-link")

      s"has the text ${messages.finish}" in {
        link.text() shouldBe messages.finish
      }

      "has an href linking to gov uk" in {
        link.attr("href") shouldBe "http://www.gov.uk"
      }
    }

    "should produce the same output when render and f are called" in {
      whatNextView.f(false)(FakeRequest("GET", ""), mockMessage) shouldBe whatNextView.render(false, FakeRequest("GET", ""), mockMessage)
    }
  }

  "Disposal date is after 6 April 2020" which {
    lazy val view2 = whatNextView(isDateAfter = true)(FakeRequest("GET", ""), mockMessage)
    lazy val doc2 = Jsoup.parse(view2.body)

    "doesn't have information for sa users" in {
      doc2.select(".govuk-details").size shouldBe 0
    }

    "has a button for reporting" which {
      lazy val button = doc2.select(".govuk-button")

      s"has the text ${messages.report}" in {
        button.text() shouldBe messages.report
      }

      "has an href linking to the nr report service" in {
        button.attr("href") shouldBe "/capital-gains-tax-uk-property/start/report-pay-capital-gains-tax-uk-property"
      }
    }
  }


}
