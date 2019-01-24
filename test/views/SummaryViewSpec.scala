/*
 * Copyright 2019 HM Revenue & Customs
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

package views

import assets.MessageLookup.NonResident.Summary
import assets.MessageLookup.{NonResident => messages}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import models.{QuestionAnswerModel, TaxYearModel, TotalTaxOwedModel}
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.summary
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class SummaryViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]

  "Summary view" when {
    "supplied with a disposal date within the valid tax years" should {
      val totalTaxOwedModel = TotalTaxOwedModel(100000, 500, 20, None, None, 500, 500, None, None, None, None, 0, None, None, None, None, None, None, None)
      val taxYearModel: TaxYearModel = TaxYearModel("2016/17", isValidYear = true, "2016/17")
      lazy val view = summary(totalTaxOwedModel, taxYearModel, "flat", 1000.0, 100, 100, "back-link", showUserResearchPanel = false)(fakeRequest, applicationMessages,fakeApplication,mockConfig)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '${messages.Summary.title}'" in {
        document.title() shouldBe messages.Summary.title
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has the text" in {
          backLink.text shouldBe messages.back
        }

        s"has a route to 'back-link'" in {
          backLink.attr("href") shouldBe "back-link"
        }
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate().url
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        s"has a span with the text '£100,000.00'" in {
          heading.text() shouldBe "£100,000.00"
        }
      }

      "have a what to do next section" which {
        lazy val whatToDoNext = document.select("#whatToDoNext")

        "has the heading 'What to do next'" in {
          whatToDoNext.select("h2").text() shouldBe messages.Summary.whatToDoNextText
        }

        "should have the text describing what to do next" in {
          whatToDoNext.select("p").text() shouldBe s"${messages.Summary.whatToDoNextContent}"
        }
      }

      "have a save pdf button" which {
        lazy val savePDF = document.select("a.save-pdf-link")

        "which has the text 'Save as PDF'" in {
          savePDF.text() shouldBe messages.Summary.saveAsPdf
        }

        "which has the link to the summary report" in {
          savePDF.attr("href") shouldBe controllers.routes.ReportController.summaryReport().url
        }
      }

      "have a continue button" which {
        lazy val continue = document.select("a.button")

        "have the text 'Continue" in {
          continue.text() shouldBe messages.continue
        }

        "have a link to /calculate-your-capital-gains/non-resident/what-next" in {
          continue.attr("href") shouldBe controllers.routes.WhatNextController.whatNext().url
        }
      }

      "does not have ur panel" in {
        document.select("div#ur-panel").size() shouldBe 0
      }
    }

    "supplied with a disposal date not within the valid tax years" should {
      val totalTaxOwedModel = TotalTaxOwedModel(500, 500, 20, None, None, 500, 500, None, None, None, None, 0, None, None, None, None, None, None, None)
      val taxYearModel: TaxYearModel = TaxYearModel("2018/19", isValidYear = false, "2017/18")
      lazy val view = summary(totalTaxOwedModel, taxYearModel, "flat", 1000.0, 100, 100, "back-url", showUserResearchPanel = true)(fakeRequest, applicationMessages,fakeApplication,mockConfig)
      lazy val document = Jsoup.parse(view.body)

      "display a tax year warning" in {
        document.select("div.notice-wrapper").size() shouldBe 1
      }

      "does have ur panel" in {
        document.select("div#ur-panel").size() shouldBe 1

        document.select(".banner-panel__close").size() shouldBe 1
        document.select(".banner-panel__title").text() shouldBe messages.Summary.bannerPanelTitle

        document.select("section > a").first().attr("href") shouldBe messages.Summary.bannerPanelLinkURL
        document.select("section > a").first().text() shouldBe messages.Summary.bannerPanelLinkText

        document.select("a > span").first().text() shouldBe Summary.bannerPanelCloseVisibleText
        document.select("a > span").eq(1).text() shouldBe Summary.bannerPanelCloseHiddenText

      }

    }
  }
}
