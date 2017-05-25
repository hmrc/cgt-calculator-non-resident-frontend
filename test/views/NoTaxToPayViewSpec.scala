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

package views

import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import assets.MessageLookup.{NoTaxToPay => messages}
import assets.MessageLookup.{Resident => commonMessages}
import views.html.{calculation => views}

class NoTaxToPayViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "No Tax to Pay View when gifted to spouse" should {
    lazy val view = views.noTaxToPay(false)(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title of ${messages.title}" in {
      doc.title() shouldBe messages.title
    }

    "have a back link to back-link" in {
      doc.body().select("a#back-link").attr("href") shouldBe "/calculate-your-capital-gains/resident/properties/who-did-you-give-it-to"
    }

    "have a home link to home-link" in {
      doc.body().select("a#homeNavHref").attr("href") shouldBe "/calculate-your-capital-gains/resident/properties/"
    }

    "have a navTitle for resident properties" in {
      doc.body().select("span.header__menu__proposition-name").text() shouldBe commonMessages.homeText
    }

    s"have a header of ${messages.title}" in {
      doc.body().select("h1.heading-large").text() shouldBe messages.title
    }

    "have text explaining why tax is not owed" in {
      doc.body().select("div#content p").text() shouldBe messages.spouseText
    }
  }

  "No Tax to Pay View when gifted to charity" should {
    lazy val view = views.noTaxToPay(true)(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have text explaining why tax is not owed" in {
      doc.body().select("div#content p").text() shouldBe messages.charityText
    }
  }
}