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

package views

import assets.MessageLookup.{NonResident => nrMessages}
import common.TestModels._
import controllers.helpers.FakeRequestHelper
import models._
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.summaryReport
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class SummaryReportViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  implicit val fr = fakeRequest

  "The report summary view" when {

    "provided with a valid tax year" should {

      lazy val taxYear = TaxYearModel("2016/17", true, "2016/17")

      val answersModel = TotalGainAnswersModel(DisposalDateModel(5, 10, 2016),
        SoldOrGivenAwayModel(true),
        Some(SoldForLessModel(false)),
        DisposalValueModel(1000),
        DisposalCostsModel(100),
        Some(HowBecameOwnerModel("Gifted")),
        Some(BoughtForLessModel(false)),
        AcquisitionValueModel(2000),
        AcquisitionCostsModel(200),
        AcquisitionDateModel("Yes", Some(4), Some(10), Some(2013)),
        Some(RebasedValueModel(Some(3000))),
        Some(RebasedCostsModel("Yes", Some(300))),
        ImprovementsModel("Yes", Some(10), Some(20)),
        Some(OtherReliefsModel(1000)))

      val questionAnswer = QuestionAnswerModel[String]("text", "1000", "test-question", None)
      val seqQuestionAnswers = Seq(questionAnswer, questionAnswer)

      lazy val view = summaryReport(answersModel, seqQuestionAnswers, taxYear,
        sumModelFlat.calculationElectionModel.calculationType, None, taxOwed = BigDecimal(1000), otherReliefs = Some(OtherReliefsModel(1000)))
      lazy val document = Jsoup.parse(view.body)

      "have a heading" which {
        lazy val heading = document.select("h1")

        "has a class of 'heading-xlarge'" in {
          heading.attr("class") shouldBe "heading-xlarge"
        }

        "has a span with the class 'pre-heading'" in {
          heading.select("span").attr("class") shouldBe "pre-heading"
        }

        "has a span with the text 'You owe'" in {
          heading.select("span").text shouldEqual nrMessages.Summary.secondaryHeading
        }

        "have a result amount currently set to £1,000.00" in {
          heading.select("b").text shouldEqual "£1,000.00"
        }
      }

      "have the HMRC logo with the HMRC name" in {
        document.select("div.logo span").text shouldBe "HM Revenue & Customs"
      }

      "not have a notice summary" in {
        document.select("div.notice-wrapper").isEmpty shouldBe true
      }

      "have a 'Calculation details' section that" in {
        document.select("#calculationDetails span.heading-large").text should include(nrMessages.Summary.calculationDetailsTitle)
      }

      "have a 'Purchase details' section that" in {
        document.select("#purchaseDetails span.heading-large").text should include(nrMessages.Summary.purchaseDetailsTitle)
      }

      "have a 'Property details' section that" in {
        document.select("#propertyDetails span.heading-large").text should include(nrMessages.Summary.propertyDetailsTitle)
      }

      "have a 'Sale details' section that" in {
        document.select("#salesDetails span.heading-large").text should include(nrMessages.Summary.salesDetailsTitle)
      }

      "have a 'Deductions details' section that" in {
        document.select("#deductions span.heading-large").text should include(nrMessages.Summary.deductionsTitle)
      }

      "have a what to do next section that " should {

        lazy val whatToDoNext = document.select("#whatToDoNext")

        "have a heading with the class 'heading-medium'" in {
          whatToDoNext.select("h2").attr("class") shouldBe "heading-medium"
        }

        "have the text 'You need to tell HMRC about the property'" in {
          whatToDoNext.select("h2").text shouldBe nrMessages.whatToDoNextTextTwo
        }

        "have the text 'Further details on how to tell HMRC about this property can be found at'" in {
          whatToDoNext.select("p").text should include (nrMessages.whatToDoNextFurtherDetails)
        }

        "have a link with the class 'external-link'" in {
          whatToDoNext.select("a").attr("class") shouldBe "external-link"
        }

        "have a link with a rel of 'external'" in {
          whatToDoNext.select("a").attr("rel") shouldBe "external"
        }

        "have a link with a target of '_blank'" in {
          whatToDoNext.select("a").attr("target") shouldBe "_blank"
        }

        "have the correct link" in {
          whatToDoNext.select("a").text shouldBe "https://www.gov.uk/guidance/capital-gains-tax-for-non-residents-uk-residential-property"
        }
      }
    }

    "provided with an invalid tax year" should {
      lazy val taxYear = TaxYearModel("2018/19", false, "2016/17")
      val answersModel = TotalGainAnswersModel(DisposalDateModel(5, 10, 2016),
        SoldOrGivenAwayModel(true),
        Some(SoldForLessModel(false)),
        DisposalValueModel(1000),
        DisposalCostsModel(100),
        Some(HowBecameOwnerModel("Gifted")),
        Some(BoughtForLessModel(false)),
        AcquisitionValueModel(2000),
        AcquisitionCostsModel(200),
        AcquisitionDateModel("Yes", Some(4), Some(10), Some(2013)),
        Some(RebasedValueModel(Some(3000))),
        Some(RebasedCostsModel("Yes", Some(300))),
        ImprovementsModel("Yes", Some(10), Some(20)),
        Some(OtherReliefsModel(1000)))

      val questionAnswer = QuestionAnswerModel[String]("text", "1000", "test-question", None)
      val seqQuestionAnswers = Seq(questionAnswer, questionAnswer)

      lazy val view = summaryReport(answersModel, seqQuestionAnswers, taxYear,
        sumModelFlat.calculationElectionModel.calculationType,None, taxOwed = BigDecimal(1000), otherReliefs = None)
      lazy val document = Jsoup.parse(view.body)

      "have a notice summary" which {
        lazy val notice = document.body().select("div.notice-wrapper")

        "has a div with the class 'notice'" in {
          notice.select("div.notice-wrapper > div").attr("class") shouldBe "notice"
        }

        "has a message with class of 'bold-small'" in {
          notice.select("strong").attr("class") shouldBe "bold-small"
        }

        "has the correct message text" in {
          notice.select("strong").text() shouldBe nrMessages.Summary.basedOnYear("2016/17")
        }
      }
    }

    "provided with a flat loss calculation" should {
      lazy val taxYear = TaxYearModel("2016/17", true, "2016/17")

      val answersModel = TotalGainAnswersModel(DisposalDateModel(5, 10, 2016),
        SoldOrGivenAwayModel(true),
        Some(SoldForLessModel(false)),
        DisposalValueModel(1000),
        DisposalCostsModel(100),
        Some(HowBecameOwnerModel("Gifted")),
        Some(BoughtForLessModel(false)),
        AcquisitionValueModel(2000),
        AcquisitionCostsModel(200),
        AcquisitionDateModel("Yes", Some(4), Some(10), Some(2013)),
        Some(RebasedValueModel(Some(3000))),
        Some(RebasedCostsModel("Yes", Some(300))),
        ImprovementsModel("Yes", Some(10), Some(20)),
        Some(OtherReliefsModel(1000)))

      val questionAnswer = QuestionAnswerModel[String]("text", "1000", "test-question", None)
      val seqQuestionAnswers = Seq(questionAnswer, questionAnswer)

      lazy val view = summaryReport(answersModel, seqQuestionAnswers, taxYear,
        sumModelFlat.calculationElectionModel.calculationType, None, taxOwed = BigDecimal(1000), otherReliefs = Some(OtherReliefsModel(1000)))
      lazy val document = Jsoup.parse(view.body)

      "have a heading" which {
        lazy val heading = document.select("h1")

        "has a class of 'heading-xlarge'" in {
          heading.attr("class") shouldBe "heading-xlarge"
        }

        "has a span with the class 'pre-heading'" in {
          heading.select("span").attr("class") shouldBe "pre-heading"
        }

        "has a span with the text 'You owe'" in {
          heading.select("span").text shouldEqual nrMessages.Summary.secondaryHeading
        }

        "have a result amount currently set to £1,000.00" in {
          heading.select("b").text shouldEqual "£1,000.00"
        }
      }

      "have the HMRC logo with the HMRC name" in {
        document.select("div.logo span").text shouldBe "HM Revenue & Customs"
      }

      "not have a notice summary" in {
        document.select("div.notice-wrapper").isEmpty shouldBe true
      }

      "have a 'Calculation details' section that" in {
        document.select("#calculationDetails span.heading-large").text should include(nrMessages.Summary.calculationDetailsTitle)
      }

      "have an 'Owning the property' section that" in {
        document.select("#purchaseDetails span.heading-large").text should include(nrMessages.Summary.purchaseDetailsTitle)
      }

      "have a 'Property details' section that" in {
        document.select("#propertyDetails span.heading-large").text should include(nrMessages.Summary.propertyDetailsTitle)
      }

      "have a 'Sale details' section that" in {
        document.select("#salesDetails span.heading-large").text should include(nrMessages.Summary.salesDetailsTitle)
      }

      "have a 'Deductions details' section that" in {
        document.select("#deductions span.heading-large").text should include(nrMessages.Summary.deductionsTitle)
      }

      "not have a 'Personal details' section that" in {
        document.select("#personalDetails span.heading-large").text shouldBe ""
      }
    }
  }
}
