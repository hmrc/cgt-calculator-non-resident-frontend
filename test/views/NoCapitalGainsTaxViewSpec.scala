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

import assets.MessageLookup.{NonResident => messages}
import controllers.helpers.FakeRequestHelper
import models.DisposalDateModel
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.noCapitalGainsTax
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class NoCapitalGainsTaxViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  "No Capital Gains Tax view" when {

    "supplied with a date of 5-4-2014" should {
      lazy val view = noCapitalGainsTax(DisposalDateModel(5, 4, 2014))
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '${messages.NoCapitalGainsTax.title}'" in {
        document.title() shouldBe messages.NoCapitalGainsTax.title
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has the text" in {
          backLink.text shouldBe messages.back
        }

        s"has a route to 'disposal-costs'" in {
          backLink.attr("href") shouldBe controllers.routes.DisposalDateController.disposalDate().url
        }
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate().url
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        s"has the text '${messages.NoCapitalGainsTax.title}'" in {
          heading.text shouldBe messages.NoCapitalGainsTax.title
        }
      }

      "contain a paragraph with the first paragraph text" in {
        document.select("p").text() should include(messages.NoCapitalGainsTax.paragraphOne)
      }

      "contain a paragraph with the second paragraph text" in {
        document.select("p").text() should include(messages.NoCapitalGainsTax.paragraphTwo)
      }

      "contain a span within the second paragraph" which {
        lazy val span = document.select("div#content p span")

        "contains a date in a span" which {
          lazy val dateSpan = span.select("span > span")

          "has the date 05-04-2014" in {
            dateSpan.text() shouldBe "5 April 2014"
          }
        }
      }

      "contains a change link" which {
        lazy val changeLink = document.select("article div a").first()

        "has an href to disposal-date page" in {
          changeLink.attr("href") shouldBe controllers.routes.DisposalDateController.disposalDate().url
        }

        s"has the text ${messages.NoCapitalGainsTax.changeLink}" in {
          changeLink.text() shouldBe messages.NoCapitalGainsTax.changeLink
        }
      }

      "contains a return link" which {
        lazy val returnLink = document.select("article div a").get(1)

        "has an href to disposal-date page" in {
          returnLink.attr("href") shouldBe "http://www.gov.uk"
        }

        s"has the text ${messages.NoCapitalGainsTax.returnLink}" in {
          returnLink.text() shouldBe messages.NoCapitalGainsTax.returnLink
        }
      }
    }

    "supplied with a date of 12-11-2013" should {
      lazy val view = noCapitalGainsTax(DisposalDateModel(12, 11, 2013))
      lazy val document = Jsoup.parse(view.body)

      "has the date 12-11-2013" in {
        document.select("div#content p span > span").text() shouldBe "12 November 2013"
      }
    }
  }
}
