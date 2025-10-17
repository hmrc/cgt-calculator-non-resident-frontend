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

import _root_.views.html.calculation.noTaxToPay
import assets.MessageLookup.{NoTaxToPay => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents

class NoTaxToPayViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val noTaxToPayView: noTaxToPay = fakeApplication.injector.instanceOf[noTaxToPay]

  "No Tax to Pay View when gifted to spouse" should {
    lazy val view = noTaxToPayView(forCharity = false)(using fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title of ${messages.title}" in {
      doc.title() shouldBe s"${messages.title} - Calculate your Non-Resident Capital Gains Tax - GOV.UK"
    }

    "have a back link to back-link" in {
      doc.body().select("a.govuk-back-link").attr("href") shouldBe "#"
    }

    s"have a header of ${messages.title}" in {
      doc.body().getElementsByClass("govuk-heading-l").text() shouldBe messages.title
    }

    "have text explaining why tax is not owed" in {
      doc.body().getElementById("reasonText").text() shouldBe messages.spouseText
    }

    "have a link to the Gov.Uk page" which {

      "has the href to https://www.gov.uk/" in {
        doc.body().getElementById("exit-calculator").attr("href") shouldBe "https://www.gov.uk/"
      }

      s"has the text ${messages.returnToGov}" in {
        doc.body().select("a#exit-calculator").text shouldBe messages.returnToGov
      }
    }

    "should produce the same output when render and f are called" in {
      noTaxToPayView.f(false)(fakeRequest, mockMessage) shouldBe noTaxToPayView.render(forCharity = false, fakeRequest, mockMessage)
    }
  }

  "No Tax to Pay View when gifted to charity" should {
    lazy val view = noTaxToPayView(forCharity = true)(using fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "have text explaining why tax is not owed" in {
      doc.body().getElementById("reasonText").text() shouldBe messages.charityText
    }
  }
}