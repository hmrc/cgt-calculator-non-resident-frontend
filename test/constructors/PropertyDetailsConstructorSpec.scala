/*
 * Copyright 2020 HM Revenue & Customs
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
    DateModel(10, 10, 2010),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(150000),
    DisposalCostsModel(600),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    DateModel(1, 1, 2015),
    None,
    None,
    ImprovementsModel("No", None, None),
    None
  )

  val flatOnlyImprovements = TotalGainAnswersModel(
    DateModel(10, 10, 2018),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(false)),
    DisposalValueModel(90000),
    DisposalCostsModel(0),
    Some(HowBecameOwnerModel("Bought")),
    Some(BoughtForLessModel(false)),
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    DateModel(6, 4, 2015),
    None,
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel("Yes", Some(50), None),
    None
  )

  val claimingRebasedImprovements = TotalGainAnswersModel(
    DateModel(10, 10, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(true)),
    DisposalValueModel(10000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Bought")),
    Some(BoughtForLessModel(true)),
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    DateModel(1, 4, 2013),
    Some(RebasedValueModel(7500)),
    Some(RebasedCostsModel("Yes", Some(150))),
    ImprovementsModel("Yes", Some(50), Some(25)),
    None
  )


  "Calling propertyDetailsRow" when {

    "claiming improvements is selected and acquisition date is before 5/4/2015" should {
      lazy val result = PropertyDetailsConstructor.propertyDetailsRows(claimingRebasedImprovements)

      "return a sequence with an improvements question" in {
        result.contains(PropertyDetailsConstructor.constructClaimingImprovementsRow(claimingRebasedImprovements).get)
      }

      "return a sequence with an improvements value" in {
        result.contains(PropertyDetailsConstructor.constructTotalImprovementsRow(claimingRebasedImprovements, display = true, displayRebased = true).get)
      }

      "return a sequence with an improvements after value" in {
        result.contains(PropertyDetailsConstructor.constructImprovementsAfterRow(claimingRebasedImprovements, display = true, displayRebased = true).get)
      }
    }
  }

  "Calling constructClaimingImprovementsRow" when {

    "supplied with a value of Yes" should {
      lazy val result = PropertyDetailsConstructor.constructClaimingImprovementsRow(claimingRebasedImprovements).get

      "have an id of nr:improvements-isClaiming" in {
        result.id shouldBe "nr:improvements-isClaiming"
      }

      s"have the data for ${messages.yes}" in {
        result.data shouldBe messages.yes
      }

      "have the question for improvements is claiming" in {
        result.question shouldBe "calc.improvements.question"
      }

      "have a link to the improvements page" in {
        result.link shouldBe Some(controllers.routes.ImprovementsController.improvements().url)
      }
    }

    "supplied with a value of No" should {
      lazy val result = PropertyDetailsConstructor.constructClaimingImprovementsRow(noImprovements).get

      s"have the value ${messages.no}" in {
        result.data shouldBe messages.no
      }
    }
  }

  "Calling constructTotalImprovementsRow" when {

    "claiming improvements is selected and acquisition date is after the 5/4/2015" should {
      lazy val result = PropertyDetailsConstructor.constructTotalImprovementsRow(flatOnlyImprovements, display = true, displayRebased = false)

      "return some value" in {
        result.isDefined shouldBe true
      }

      "have an id of nr:improvements-total" in {
        result.get.id shouldBe "nr:improvements-total"
      }

      "have the data for the value of 50" in {
        result.get.data shouldBe BigDecimal(50)
      }

      "have the question for improvements values" in {
        result.get.question shouldBe "calc.improvements.questionTwo"
      }

      "have a link to the improvements page" in {
        result.get.link shouldBe Some(controllers.routes.ImprovementsController.improvements().url)
      }
    }

    "not claiming improvements" should {
      lazy val result = PropertyDetailsConstructor.constructTotalImprovementsRow(noImprovements, display = false, displayRebased = false)

      "return a None" in {
        result shouldBe None
      }
    }

    "claiming improvements is selected and acquisition date is before 6/4/2015" should {
      lazy val result = PropertyDetailsConstructor.constructTotalImprovementsRow(claimingRebasedImprovements, display = true, displayRebased = true)

      "return some value" in {
        result.isDefined shouldBe true
      }

      "have an id of nr:improvements-total" in {
        result.get.id shouldBe "nr:improvements-total"
      }

      "have the data for the value of 50" in {
        result.get.data shouldBe BigDecimal(50)
      }

      "have the question for improvements values" in {
        result.get.question shouldBe "calc.improvements.questionThree"
      }

      "have a link to the improvements page" in {
        result.get.link shouldBe Some(controllers.routes.ImprovementsController.improvements().url)
      }
    }
  }

  "Calling constructImprovementsAfterRow" when {

    "claiming improvements is selected and acquisition date is before 6/4/2015" should {
      lazy val result = PropertyDetailsConstructor.constructImprovementsAfterRow(claimingRebasedImprovements, display = true, displayRebased = true)

      "return some value" in {
        result.isDefined shouldBe true
      }

      "have an id of nr:improvements-after" in {
        result.get.id shouldBe "nr:improvements-after"
      }

      "have the data for the value of 25" in {
        result.get.data shouldBe BigDecimal(25)
      }

      "have the question for improvements value" in {
        result.get.question shouldBe "calc.improvements.questionFour"
      }

      "have a link to the improvements page" in {
        result.get.link shouldBe Some(controllers.routes.ImprovementsController.improvements().url)
      }
    }

    "claiming improvements is selected and acquisition date is after 5/4/2015" should {
      lazy val result = PropertyDetailsConstructor.constructImprovementsAfterRow(flatOnlyImprovements, display = true, displayRebased = false)

      "return a None" in {
        result shouldBe None
      }
    }

    "is not claiming improvements" should {
      lazy val result = PropertyDetailsConstructor.constructImprovementsAfterRow(noImprovements, display = false, displayRebased = false)

      "return a None" in {
        result shouldBe None
      }
    }
  }
}
