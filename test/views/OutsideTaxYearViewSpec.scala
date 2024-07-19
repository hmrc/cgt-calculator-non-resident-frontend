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

package views

import _root_.views.html.calculation.outsideTaxYear
import assets.MessageLookup.{NonResident => commonMessages, OutsideTaxYears => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import models.TaxYearModel
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents

class OutsideTaxYearViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {
  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val outsideTaxYearView: outsideTaxYear = fakeApplication.injector.instanceOf[outsideTaxYear]
  lazy val pageTitle = s"""${messages.title} - ${commonMessages.serviceName} - GOV.UK"""


  "Outside tax years views" when {

    "using a disposal date of 2018/19 " should {
      lazy val taxYear = TaxYearModel("2018/19", isValidYear = false, "2017/18")
      lazy val view = outsideTaxYearView(taxYear)(fakeRequestWithSession, mockMessage)
      lazy val doc = Jsoup.parse(view.body)

      "have charset UTF-8" in {
        doc.charset().toString shouldBe "UTF-8"
      }

      s"return a title of $pageTitle" in {
        doc.title shouldBe pageTitle
      }

      s"have a heading of ${messages.title}" in {
        doc.select("h1").text() shouldBe messages.title
      }

      s"have a message of ${messages.content("2017/18")}" in {
        doc.select("p.govuk-body").text() shouldBe messages.content("2017/18")
      }

      "have a back link that" should {
        lazy val backLink = doc.select("a.govuk-back-link")

        "have the correct back link text" in {
          backLink.text shouldBe commonMessages.back
        }

        s"have a link to '${controllers.routes.DisposalDateController.disposalDate.url}'" in {
          backLink.attr("href") shouldBe "#"
        }

      }

      "have a continue button" should {
        lazy val continue = doc.select("#submit")
        "have the text continue" in {
          continue.text shouldBe commonMessages.continue
        }

        s" have a link to ${controllers.routes.SoldOrGivenAwayController.soldOrGivenAway.url}" in {
          continue.attr("href") shouldBe controllers.routes.SoldOrGivenAwayController.soldOrGivenAway.url
        }
      }

      "should produce the same output when render and f are called" in {
        outsideTaxYearView.f(taxYear)(fakeRequestWithSession, mockMessage) shouldBe outsideTaxYearView.render(taxYear, fakeRequestWithSession, mockMessage)
      }
    }
  }
}

