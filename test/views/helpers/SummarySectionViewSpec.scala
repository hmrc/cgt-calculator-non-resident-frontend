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

package views.helpers

import models.QuestionAnswerModel
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.UnitSpec
import views.html.helpers.summarySection

class SummarySectionViewSpec extends UnitSpec {

  "Creating a summary section" when {
    val firstItem = QuestionAnswerModel[String]("firstID", "firstData", "firstQuestion", Some("first-link"))
    val secondItem = QuestionAnswerModel[BigDecimal]("secondID", BigDecimal(2), "secondQuestion", Some("second-link"))
    val thirdItem = QuestionAnswerModel[Int]("thirdID", 2, "thirdQuestion", Some("third-link"))

    "passing in a sequence of one item" should {
      val sequence = Seq(firstItem)
      val result = summarySection("sectionID", "sectionTitle", sequence)
      val doc = Jsoup.parse(result.body)

      "contain a section with the ID 'sectionID' and the class 'summary-section'" in {
        doc.select("section#sectionID").attr("class") shouldBe "summary-section"
      }

      "contain a single div for the one summaryRow" in {
        doc.select("div.form-group").size() shouldBe 1
      }

      "have summary row divs with a class of 'grid-layout grid-layout--stacked form-group'" in {
        doc.select("div.form-group").attr("class") shouldBe "grid-layout grid-layout--stacked form-group"
      }

      "have a summary row" which {

        "has a sectionTitle" in {
          doc.select("div.form-group span.heading-large").text() shouldBe "sectionTitle"
        }

        "contains the question and answer for 'firstID'" in {
          doc.select("div.form-group > div").size shouldBe 3
        }

        "should have internal divs for title, question and answer with class 'grid-layout__column grid-layout__column--1-3'" in {
          doc.select("div.form-group > div").attr("class") shouldBe "grid-layout__column grid-layout__column--1-3"
        }
      }
    }

    "passing in a sequence of two items" should {
      val sequence = Seq(firstItem, secondItem)
      val result = summarySection("sectionID-two", "sectionTitle-two", sequence)
      val doc = Jsoup.parse(result.body)

      "contain a section with the ID 'sectionID-two' and the class 'summary-section'" in {
        doc.select("section#sectionID-two").attr("class") shouldBe "summary-section"
      }

      "contain two divs for the two summaryRow" in {
        doc.select("div.form-group").size() shouldBe 2
      }

      "have summary row divs with a class of 'grid-layout grid-layout--stacked form-group'" in {
        doc.select("div.form-group").attr("class") shouldBe "grid-layout grid-layout--stacked form-group"
      }

      "have summary rows" which {

        "have a sectionTitle" in {
          doc.select("div.form-group span.heading-large").text() shouldBe "sectionTitle-two"
        }

        "have only one sectionTitle" in {
          doc.select("div.form-group span.heading-large").size() shouldBe 1
        }

        "contains the question and answer for two rows" in {
          doc.select("div.form-group > div").size shouldBe 6
        }

        "should have internal divs for title, question and answer with class 'grid-layout__column grid-layout__column--1-3'" in {
          doc.select("div.form-group > div").attr("class") shouldBe "grid-layout__column grid-layout__column--1-3"
        }
      }
    }

    "passing in a sequence of three items" should {
      val sequence = Seq(firstItem, secondItem, thirdItem)
      val result = summarySection("sectionID", "sectionTitle", sequence)
      val doc = Jsoup.parse(result.body)

      "contain three divs for the three summaryRow" in {
        doc.select("div.form-group").size() shouldBe 3
      }

      "have only one sectionTitle" in {
        doc.select("div.form-group span.heading-large").size() shouldBe 1
      }

      "contains the question and answer for three rows" in {
        doc.select("div.form-group > div").size shouldBe 9
      }
    }

    "passing in an empty sequence" should {
      val sequence = Seq()
      val result = summarySection("sectionID", "sectionTitle", sequence)
      val doc = Jsoup.parse(result.body)

      "not have any content" in {
        doc.body().children().isEmpty() shouldBe true
      }
    }
  }
}
