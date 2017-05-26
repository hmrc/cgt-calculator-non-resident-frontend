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

package constructors

import assets.MessageLookup.NonResident.{Improvements => messages}
import helpers.AssertHelpers
import models._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class PropertyDetailsConstructorSpec extends UnitSpec with WithFakeApplication with AssertHelpers {

  val noImprovements = TotalGainAnswersModel(
    DisposalDateModel(10, 10, 2010),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(150000),
    DisposalCostsModel(600),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    AcquisitionDateModel(1, 1, 2015),
    None,
    None,
    ImprovementsModel("No", None, None),
    None
  )

  val noRebasedImprovements = TotalGainAnswersModel(
    DisposalDateModel(10, 10, 2018),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(false)),
    DisposalValueModel(90000),
    DisposalCostsModel(0),
    Some(HowBecameOwnerModel("Bought")),
    Some(BoughtForLessModel(false)),
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    AcquisitionDateModel(1, 4, 2013),
    Some(RebasedValueModel(None)),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel("Yes", Some(50), None),
    None
  )

  val allImprovements = TotalGainAnswersModel(
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
    Some(RebasedValueModel(Some(7500))),
    Some(RebasedCostsModel("Yes", Some(150))),
    ImprovementsModel("Yes", Some(50), Some(25)),
    None
  )

  private def assertExpectedResult[T](option: Option[T])(test: T => Unit) = assertOption("expected option is None")(option)(test)

  "Calling propertyDetailsRow" when {

    "supplied with all improvements" should {
      lazy val result = PropertyDetailsConstructor.propertyDetailsRows(allImprovements)

      "return a sequence with an improvements question" in {
        result.contains(PropertyDetailsConstructor.constructClaimingImprovementsRow(allImprovements).get)
      }

      "return a sequence with an improvements value" in {
        result.contains(PropertyDetailsConstructor.constructTotalImprovementsRow(allImprovements, display = true, displayRebased = true).get)
      }

      "return a sequence with an improvements after value" in {
        result.contains(PropertyDetailsConstructor.constructImprovementsAfterRow(allImprovements, display = true, displayRebased = true).get)
      }
    }
  }

  "Calling constructClaimingImprovementsRow" when {

    "supplied with a value of Yes" should {
      lazy val result = PropertyDetailsConstructor.constructClaimingImprovementsRow(allImprovements).get

      "have an id of nr:improvements-isClaiming" in {
        result.id shouldBe "nr:improvements-isClaiming"
      }

      "have the data for Yes" in {
        result.data shouldBe "Yes"
      }

      "have the question for improvements is claiming" in {
        result.question shouldBe messages.question
      }

      "have a link to the improvements page" in {
        result.link shouldBe Some(controllers.routes.ImprovementsController.improvements().url)
      }
    }

    "supplied with a value of No" should {
      lazy val result = PropertyDetailsConstructor.constructClaimingImprovementsRow(noImprovements).get

      "have the data for No" in {
        result.data shouldBe "No"
      }
    }
  }

  "Calling constructTotalImprovementsRow" when {

    "supplied with a value of 50 and no rebased value is used" should {
      lazy val result = PropertyDetailsConstructor.constructTotalImprovementsRow(noRebasedImprovements, display = true, displayRebased = false)

      "return some value" in {
        result.isDefined shouldBe true
      }

      "have an id of nr:improvements-total" in {
        assertExpectedResult(result)(_.id shouldBe "nr:improvements-total")
      }

      "have the data for the value of 50" in {
        assertExpectedResult(result)(_.data shouldBe BigDecimal(50))
      }

      "have the question for improvements values" in {
        assertExpectedResult(result)(_.question shouldBe messages.questionTwo)
      }

      "have a link to the improvements page" in {
        assertExpectedResult(result)(_.link shouldBe Some(controllers.routes.ImprovementsController.improvements().url))
      }
    }

    "supplied with no improvements value" should {
      lazy val result = PropertyDetailsConstructor.constructTotalImprovementsRow(noImprovements, display = false, displayRebased = false)

      "return a None" in {
        result shouldBe None
      }
    }

    "supplied with a value of 50 alongside a rebased improvements value" should {
      lazy val result = PropertyDetailsConstructor.constructTotalImprovementsRow(allImprovements, display = true, displayRebased = true)

      "return some value" in {
        result.isDefined shouldBe true
      }

      "have the data for the value of 50" in {
        assertExpectedResult(result)(_.data shouldBe BigDecimal(50))
      }

      "have the question for improvements values" in {
        assertExpectedResult(result)(_.question shouldBe messages.questionThree)
      }
    }
  }

  "Calling constructImprovementsAfterRow" when {

    "supplied with a value of 25 and should be displayed it true" should {
      lazy val result = PropertyDetailsConstructor.constructImprovementsAfterRow(allImprovements, display = true, displayRebased = true)

      "return some value" in {
        result.isDefined shouldBe true
      }

      "have an id of nr:improvements-after" in {
        assertExpectedResult(result)(_.id shouldBe "nr:improvements-after")
      }

      "have the data for the value of 25" in {
        assertExpectedResult(result)(_.data shouldBe BigDecimal(25))
      }

      "have the question for improvements value" in {
        assertExpectedResult(result)(_.question shouldBe messages.questionFour)
      }

      "have a link to the improvements page" in {
        assertExpectedResult(result)(_.link shouldBe Some(controllers.routes.ImprovementsController.improvements().url))
      }
    }

    "supplied with a no value for improvements after" should {
      lazy val result = PropertyDetailsConstructor.constructImprovementsAfterRow(noRebasedImprovements, display = false, displayRebased = true)

      "return a None" in {
        result shouldBe None
      }
    }
  }
}
