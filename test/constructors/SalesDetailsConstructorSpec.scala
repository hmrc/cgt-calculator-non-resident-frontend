/*
 * Copyright 2018 HM Revenue & Customs
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

package constructors

import java.time.LocalDate

import assets.MessageLookup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.{NonResident => messages}
import helpers.AssertHelpers
import models._

class SalesDetailsConstructorSpec extends UnitSpec with WithFakeApplication with AssertHelpers {

  val totalGainGiven = TotalGainAnswersModel(
    DisposalDateModel(10, 10, 2010),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(150000),
    DisposalCostsModel(600),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    AcquisitionDateModel(1, 4, 2013),
    None,
    None,
    ImprovementsModel("No", None, None),
    None
  )

  val totalGainSold = TotalGainAnswersModel(
    DisposalDateModel(10, 10, 2018),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(false)),
    DisposalValueModel(90000),
    DisposalCostsModel(0),
    Some(HowBecameOwnerModel("Bought")),
    Some(BoughtForLessModel(false)),
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    AcquisitionDateModel(1, 4,2013),
    Some(RebasedValueModel(7500)),
    Some(RebasedCostsModel("Yes", Some(150))),
    ImprovementsModel("Yes", Some(50), Some(25)),
    None
  )

  val totalGainForLess = TotalGainAnswersModel(
    DisposalDateModel(10, 10, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(true)),
    DisposalValueModel(10000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Bought")),
    Some(BoughtForLessModel(true)),
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    AcquisitionDateModel(1, 4, 2013),
    Some(RebasedValueModel(7500)),
    Some(RebasedCostsModel("Yes", Some(150))),
    ImprovementsModel("Yes", Some(50), Some(25)),
    None
  )

  private def assertExpectedResult[T](option: Option[T])(test: T => Unit) = assertOption("expected option is None")(option)(test)

  "Calling salesDetailsRows" when {

    "using the totalGainForLess model" should {
      lazy val result = SalesDetailsConstructor.salesDetailsRows(totalGainForLess)

      "return a Sequence of size 3" in {
        result.size shouldBe 5
      }

      "return a sequence with a Disposal Date" in {
        result.contains(SalesDetailsConstructor.disposalDateRow(totalGainForLess).get)
      }

      "return a sequence with Sold Or Given Away" in {
        result.contains(SalesDetailsConstructor.soldOrGivenAwayRow(totalGainForLess).get)
      }

      "return a sequence with Sold For Less" in {
        result.contains(SalesDetailsConstructor.soldForLessRow(totalGainForLess).get)
      }

      "return a sequence with a Disposal Value" in {
        result.contains(SalesDetailsConstructor.disposalValueRow(totalGainForLess).get)
      }

      "return a sequence with Disposal Costs" in {
        result.contains(SalesDetailsConstructor.disposalCostsRow(totalGainForLess).get)
      }
    }
  }

  "Calling disposalDateRow" when {

    "supplied with a date of 10 October 2010" should {
      lazy val result = SalesDetailsConstructor.disposalDateRow(totalGainGiven).get

      "have an id of nr:disposalDate" in {
        result.id shouldBe "nr:disposalDate"
      }

      "have the data for 10 October 2010" in {
        result.data shouldBe LocalDate.parse("2010-10-10")
      }

      "have the question for disposal date" in {
        result.question shouldBe messages.DisposalDate.question
      }

      "have a link to the disposal date page" in {
        result.link shouldBe Some(controllers.routes.DisposalDateController.disposalDate().url)
      }
    }

    "supplied with a date of 10 October 2018" should {
      lazy val result = SalesDetailsConstructor.disposalDateRow(totalGainSold).get

      "have the data for 10 October 2018" in {
        result.data shouldBe LocalDate.parse("2018-10-10")
      }
    }
  }

  "Calling soldOrGivenAwayRow" when {

    "supplied with an answer of false" should {
      lazy val result = SalesDetailsConstructor.soldOrGivenAwayRow(totalGainGiven).get

      "have an id of nr:soldOrGivenAway" in {
        result.id shouldBe "nr:soldOrGivenAway"
      }

      s"have the data for '${messages.SoldOrGivenAway.gave}'" in {
        result.data shouldBe messages.SoldOrGivenAway.gave
      }

      "have the question for sold or given away" in {
        result.question shouldBe messages.SoldOrGivenAway.question
      }

      "have a link to the sold or given away page" in {
        result.link shouldBe Some(controllers.routes.SoldOrGivenAwayController.soldOrGivenAway().url)
      }
    }

    "supplied with an answer of true" should {
      lazy val result = SalesDetailsConstructor.soldOrGivenAwayRow(totalGainSold).get

      "have an id of nr:soldOrGivenAway" in {
        result.id shouldBe "nr:soldOrGivenAway"
      }

      s"have the data for '${messages.SoldOrGivenAway.sold}'" in {
        result.data shouldBe messages.SoldOrGivenAway.sold
      }

      "have the question for sold or given away" in {
        result.question shouldBe messages.SoldOrGivenAway.question
      }

      "have a link to the sold or given away page" in {
        result.link shouldBe Some(controllers.routes.SoldOrGivenAwayController.soldOrGivenAway().url)
      }
    }
  }

  "Calling soldForLessRow" when {

    "supplied with no value" should {
      lazy val result = SalesDetailsConstructor.soldForLessRow(totalGainGiven)

      "return a None" in {
        result shouldBe None
      }
    }

    "supplied with a value of false" should {
      lazy val result = SalesDetailsConstructor.soldForLessRow(totalGainSold)

      "should have a value for the row" in {
        result.isDefined shouldBe true
      }

      "have an id of nr:soldForLess" in {
        assertExpectedResult[QuestionAnswerModel[Boolean]](result)(_.id shouldBe "nr:soldForLess")
      }

      "have the data for the value 'false'" in {
        assertExpectedResult[QuestionAnswerModel[Boolean]](result)(_.data shouldBe false)
      }

      "have the question for sold for less page" in {
        assertExpectedResult[QuestionAnswerModel[Boolean]](result)(_.question shouldBe messages.SoldForLess.question)
      }

      "have a link to the sold for less page" in {
        assertExpectedResult[QuestionAnswerModel[Boolean]](result)(_.link shouldBe Some(controllers.routes.SoldForLessController.soldForLess().url))
      }
    }

    "supplied with a value of true" should {
      lazy val result = SalesDetailsConstructor.soldForLessRow(totalGainForLess)

      "should have a value for the row" in {
        result.isDefined shouldBe true
      }

      "have the data for the value 'false'" in {
        assertExpectedResult[QuestionAnswerModel[Boolean]](result)(_.data shouldBe true)
      }
    }
  }

  "Calling disposalValueRow" when {

    "supplied with soldOrGivenAway answer false" should {
      lazy val result = SalesDetailsConstructor.disposalValueRow(totalGainGiven).get

      "have an id of nr:disposalValue" in {
        result.id shouldBe "nr:disposalValue"
      }

      "have the data for the value of 150000" in {
        result.data shouldBe BigDecimal(150000)
      }

      "have the question for market value gave away" in {
        result.question shouldBe messages.MarketValue.disposalGaveAwayQuestion
      }

      "have a link to the market value when gave away page" in {
        result.link shouldBe Some(controllers.routes.MarketValueWhenSoldOrGaveAwayController.marketValueWhenGaveAway().url)
      }
    }

    "supplied with soldOrGivenAway answer true and soldForLess answer false'" should {
      lazy val result = SalesDetailsConstructor.disposalValueRow(totalGainSold).get

      "have the data for 90000" in {
        result.data shouldBe BigDecimal(90000)
      }

      "have the question for disposal value" in {
        result.question shouldBe messages.DisposalValue.question
      }

      "have a link to the disposal value page" in {
        result.link shouldBe Some(controllers.routes.DisposalValueController.disposalValue().url)
      }
    }

    "supplied with soldOrGivenAway answer true and soldForLess answer true'" should {
      lazy val result = SalesDetailsConstructor.disposalValueRow(totalGainForLess).get

      "have the data for 10000" in {
        result.data shouldBe BigDecimal(10000)
      }

      "have the question for market value sold" in {
        result.question shouldBe messages.MarketValue.disposalSoldQuestion
      }

      "have a link to the market value sold page" in {
        result.link shouldBe Some(controllers.routes.MarketValueWhenSoldOrGaveAwayController.marketValueWhenSold().url)
      }
    }
  }

  "Calling disposalCostsRow" when {

    "supplied with a value of 600" should {
      lazy val result = SalesDetailsConstructor.disposalCostsRow(totalGainGiven).get

      "have an id of nr:disposalCosts" in {
        result.id shouldBe "nr:disposalCosts"
      }

      "have the data for the value of 600" in {
        result.data shouldBe BigDecimal(600)
      }

      "have the question for disposal costs" in {
        result.question shouldBe messages.DisposalCosts.question
      }

      "have a link to the disposal costs page" in {
        result.link shouldBe Some(controllers.routes.DisposalCostsController.disposalCosts().url)
      }
    }

    "supplied with a value of 0" should {
      lazy val result = SalesDetailsConstructor.disposalCostsRow(totalGainSold).get

      "have the data for 0" in {
        result.data shouldBe BigDecimal(0)
      }
    }
  }

  "Calling whoDidYouGiveItToRow" when {

    "property was given away" should {
      lazy val result = SalesDetailsConstructor.whoDidYouGiveItToRow(totalGainGiven).get

      "have an id of nr:whoDidYouGiveItTo" in {
        result.id shouldBe "nr:whoDidYouGiveItTo"
      }

      "have the data 'Someone else'" in {
        result.data shouldBe "Someone else"
      }

      "have the question for who did you give it to" in {
        result.question shouldBe MessageLookup.WhoDidYouGiveItTo.title
      }

      "have a link to who did you give it to page" in {
        result.link shouldBe Some(controllers.routes.WhoDidYouGiveItToController.whoDidYouGiveItTo().url)
      }
    }

    "property was sold" should {
      lazy val result = SalesDetailsConstructor.whoDidYouGiveItToRow(totalGainSold)

      "return a None" in {
        result shouldBe None
      }
    }
  }
}
