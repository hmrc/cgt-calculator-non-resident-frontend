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

import assets.MessageLookup.{NonResident => commonMessages, OutsideTaxYears => messages}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import models.TaxYearModel
import org.jsoup.Jsoup
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.{calculation => views}

class OutsideTaxYearViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]

  "Outside tax years views" when {

    "using a disposal date of 2018/19 " should {
      lazy val taxYear = TaxYearModel("2018/19", false, "2017/18")
      lazy val view = views.outsideTaxYear(taxYear)(fakeRequestWithSession, applicationMessages, fakeApplication, mockConfig)
      lazy val doc = Jsoup.parse(view.body)

      "have charset UTF-8" in {
        doc.charset().toString shouldBe "UTF-8"
      }

      s"return a title of ${messages.title}" in {
        doc.title shouldBe messages.title
      }

      "have a home link to '/calculate-your-capital-gains/non-resident/'" in {
        doc.getElementById("homeNavHref").attr("href") shouldEqual "/calculate-your-capital-gains/non-resident/"
      }

      s"have a heading of ${messages.title}" in {
        doc.select("h1").text() shouldBe messages.title
      }

      s"have a message of ${messages.content("2017/18")}" in {
        doc.select("p.lede").text() shouldBe messages.content("2017/18")
      }

      "have a back link that" should {
        lazy val backLink = doc.select("a#back-link")

        "have the correct back link text" in {
          backLink.text shouldBe commonMessages.back
        }

        s"have a link to '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
          backLink.attr("href") shouldBe controllers.routes.DisposalDateController.disposalDate().url
        }

      }

      "have a continue button" should {
        lazy val continue = doc.select("a#continue-button")

        "have the text continue" in {
          continue.text shouldBe commonMessages.continue
        }

        s" have a link to ${controllers.routes.SoldOrGivenAwayController.soldOrGivenAway().url}" in {
          continue.attr("href") shouldBe controllers.routes.SoldOrGivenAwayController.soldOrGivenAway().url
        }
      }
    }
  }
}

