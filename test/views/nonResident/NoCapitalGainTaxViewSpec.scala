/*
 * Copyright 2016 HM Revenue & Customs
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

package views.nonResident

import assets.MessageLookup.{NonResident => messages}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import models.nonresident.DisposalDateModel
import views.html.calculation.nonresident.noCapitalGainsTax

class NoCapitalGainTaxViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  "No Capital Gains Tax view" when {

    "supplied with a date of 5-4-2014" should {
      lazy val view = noCapitalGainsTax(DisposalDateModel(5, 4, 2014))(fakeRequest)
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
          backLink.attr("href") shouldBe controllers.nonresident.routes.DisposalDateController.disposalDate().url
        }
      }

      s"have a home link to '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-xlarge" in {
          heading.attr("class") shouldBe "heading-xlarge"
        }

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

        "has a class of 'no-wrap'" in {
          span.attr("class") shouldBe "no-wrap"
        }

        "contains a date in a span" which {
          lazy val dateSpan = span.select("span > span")

          "has a class of 'bold-small'" in {
            dateSpan.attr("class") shouldBe "bold-small"
          }

          "has the date 05-04-2014" in {
            dateSpan.text() shouldBe "5 April 2014"
          }
        }

        "contains a link" which {
          lazy val changeLink = span.select("a")

          "has an href to disposal-date page" in {
            changeLink.attr("href") shouldBe controllers.nonresident.routes.DisposalDateController.disposalDate().url
          }

          s"has the text ${messages.NoCapitalGainsTax.change}" in {
            changeLink.text() shouldBe messages.NoCapitalGainsTax.change
          }
        }
      }

      "contains a sidebar with a link" which {
        lazy val sidebar = document.select("aside.sidebar ul > li > a")

        "has a target of _blank" in {
          sidebar.attr("target") shouldBe "_blank"
        }

        "has a rel of external" in {
          sidebar.attr("rel") shouldBe "external"
        }

        "has a link to https://www.gov.uk/guidance/capital-gains-tax-for-non-residents-uk-residential-property" in {
          sidebar.attr("href") shouldBe "https://www.gov.uk/guidance/capital-gains-tax-for-non-residents-uk-residential-property"
        }

        "has the correct link text" in {
          sidebar.text() shouldBe s"${messages.NoCapitalGainsTax.link} ${messages.externalLink}"
        }
      }
    }

    "supplied with a date of 12-11-2013" should {
      lazy val view = noCapitalGainsTax(DisposalDateModel(12, 11, 2013))(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "has the date 12-11-2013" in {
        document.select("div#content p span > span").text() shouldBe "12 November 2013"
      }
    }
  }
}
