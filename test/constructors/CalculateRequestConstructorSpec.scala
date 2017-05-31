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

import common.TestModels
import models._
import uk.gov.hmrc.play.test.UnitSpec

class CalculateRequestConstructorSpec extends UnitSpec {

  val sumModel = SummaryModel(
    CurrentIncomeModel(1000),
    Some(PersonalAllowanceModel(11100)),
    OtherPropertiesModel("No"),
    None,
    AcquisitionDateModel(9, 9, 1990),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(1000)),
    None,
    ImprovementsModel("No", None),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(0),
    DisposalCostsModel(0),
    CalculationElectionModel("flat"),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  "CalculateRequest Constructor" should {
    "return a string from the baseCalcUrl as an individual with no prior disposal" in {
      CalculateRequestConstructor.baseCalcUrl(sumModel) shouldEqual "priorDisposal=No&currentIncome=1000" +
        "&personalAllowanceAmt=11100&disposalValue=150000&disposalCosts=0&disposalDate=2010-10-10"
    }

    "return a string from the flatCalcUrlExtra with no improvements" in {
      CalculateRequestConstructor.flatCalcUrlExtra(sumModel) shouldEqual "&improvementsAmt=0&initialValueAmt=100000" +
        "&initialCostsAmt=0&reliefs=0&isClaimingPRR=No"
    }

    "return a string from the flatCalcUrlExtra with improvements and a rebased value model with no improvements after" in {
      CalculateRequestConstructor.flatCalcUrlExtra(TestModels.summaryFlatWithoutAEA) shouldEqual
        "&improvementsAmt=8000&initialValueAmt=100000&initialCostsAmt=300&reliefs=999&isClaimingPRR=No"
    }

    "return a string from the flatCalcUrlExtra with improvements and a rebased value model with improvements after" in {
      CalculateRequestConstructor.flatCalcUrlExtra(TestModels.summaryImprovementsWithRebasedModel) shouldEqual
        "&improvementsAmt=9000&initialValueAmt=100000&initialCostsAmt=300&reliefs=999&isClaimingPRR=No"
    }

    "return a string from the flatCalcUrlExtra with PRR claimed" in {
      CalculateRequestConstructor.flatCalcUrlExtra(TestModels.summaryWithAllOptions) shouldEqual
        "&improvementsAmt=8000&initialValueAmt=100000&initialCostsAmt=300" +
          "&reliefs=999&daysClaimed=100&isClaimingPRR=Yes&acquisitionDate=1999-9-9"
    }

    "return a string from the taCalcUrlExtra with no improvements" in {
      CalculateRequestConstructor.taCalcUrlExtra(sumModel) shouldEqual "&improvementsAmt=0" +
        "&acquisitionDate=1990-9-9&initialValueAmt=100000&initialCostsAmt=0&reliefs=0&isClaimingPRR=No"
    }

    "return a string from the taCalcUrlExtra with improvements and a rebased value model with improvements after" in {
      CalculateRequestConstructor.taCalcUrlExtra(TestModels.summaryImprovementsWithRebasedModel) shouldEqual "&improvementsAmt=9000" +
        "&acquisitionDate=1999-9-9&initialValueAmt=100000&initialCostsAmt=300&reliefs=888&isClaimingPRR=No"
    }

    "return a string from the taCalcUrlExtra with PRR" in {
      CalculateRequestConstructor.taCalcUrlExtra(TestModels.summaryWithAllOptions) shouldEqual "&improvementsAmt=8000" +
        "&acquisitionDate=1999-9-9&initialValueAmt=100000&initialCostsAmt=300&reliefs=888&daysClaimed=50&isClaimingPRR=Yes"
    }

    "return a string from the rebasedCalcUrlExtra with no improvements or rebased costs" in {
      CalculateRequestConstructor.rebasedCalcUrlExtra(TestModels.summaryRebasedNoImprovements) shouldEqual
        "&improvementsAmt=0&initialValueAmt=150000&initialCostsAmt=0&reliefs=0&isClaimingPRR=No"
    }

    "return a string from the rebasedCalcUrlExtra with improvements and rebased costs" in {
      CalculateRequestConstructor.rebasedCalcUrlExtra(TestModels.summaryRebased) shouldEqual
        "&improvementsAmt=3000&initialValueAmt=150000&initialCostsAmt=1000&reliefs=777&isClaimingPRR=No"
    }

    "return a string from the rebasedCalcUrlExtra with PRR" in {
      CalculateRequestConstructor.rebasedCalcUrlExtra(TestModels.summaryWithAllOptions) shouldEqual
        "&improvementsAmt=0&initialValueAmt=1000&initialCostsAmt=500&reliefs=777&daysClaimed=50&isClaimingPRR=Yes"
    }

    "return a string from the improvements with a rebased value and claiming improvements with an empty field" in {
      CalculateRequestConstructor.improvements(TestModels.summaryRebasedNoneImprovements) shouldEqual "&improvementsAmt=0"
    }

    "return a string from privateResidenceReliefFlat with an acquisition date after tax start date and disposal date after 18 month period" in {
      CalculateRequestConstructor.privateResidenceReliefFlat(TestModels.summaryPRRAcqDateAfterAndDisposalDateBefore) shouldEqual "&daysClaimed=100"
    }

    "return a string from privateResidenceReliefFlat with an acquisition date before tax start date and no rebased value" in {
      CalculateRequestConstructor.privateResidenceReliefFlat(TestModels.summaryPRRAcqDateAfterAndNoRebased) shouldEqual "&daysClaimed=100"
    }

    "return a string from privateResidenceReliefFlat with " +
      "an acquisition date before tax start date, a rebased value and disposal date after the 18 month period" in {
      CalculateRequestConstructor.privateResidenceReliefFlat(TestModels.summaryPRRAcqDateAfterAndDisposalDateAfter) shouldEqual "&daysClaimed=100"
    }

    "return an empty string from privateResidenceReliefFlat with an answer of no to PRR" in {
      CalculateRequestConstructor.privateResidenceReliefFlat(TestModels.summaryFlatWithAEA) shouldEqual ""
    }

    "return a string from privateResidenceReliefTA with " +
      "an acquisition date before tax start date, a rebased value and disposal date after the 18 month period" in {
      CalculateRequestConstructor.privateResidenceReliefTA(TestModels.summaryPRRAcqDateAfterAndDisposalDateAfterWithRebased) shouldEqual
        "&daysClaimed=50"
    }

    "return an empty string from privateResidenceReliefTA with an answer of no to PRR" in {
      CalculateRequestConstructor.privateResidenceReliefTA(TestModels.summaryFlatWithAEA) shouldEqual ""
    }

    "return a string from privateResidenceReliefRebased with a Rebased Value and a disposal date after 18 month period with an acqDate before the start" in {
      CalculateRequestConstructor.privateResidenceReliefRebased(TestModels.summaryPRRAcqDateBeforeAndDisposalDateAfterWithRebased) shouldEqual
        "&daysClaimed=50"
    }

    "return an empty string from privateResidenceReliefRebased with no Rebased PRR" in {
      CalculateRequestConstructor.privateResidenceReliefRebased(TestModels.summaryFlatWithAEA) shouldEqual ""
    }

  }

  "Calling rebasedImprovements" should {

    "return a value of 10000" in {
      val model = ImprovementsModel("Yes", None, Some(10000))
      val result = CalculateRequestConstructor.rebasedImprovements(model)
      result shouldBe "&improvementsAmt=10000"
    }

    "return a value of 2000" in {
      val model = ImprovementsModel("Yes", None, Some(2000))
      val result = CalculateRequestConstructor.rebasedImprovements(model)
      result shouldBe "&improvementsAmt=2000"
    }

    "return a value of 0 when supplied a none" in {
      val model = ImprovementsModel("Yes", Some(4000), None)
      val result = CalculateRequestConstructor.rebasedImprovements(model)
      result shouldBe "&improvementsAmt=0"
    }

    "return a value of 0 when improvements not supplied" in {
      val model = ImprovementsModel("No", None, Some(1000))
      val result = CalculateRequestConstructor.rebasedImprovements(model)
      result shouldBe "&improvementsAmt=0"
    }
  }

  "Calling rebasedValue" should {

    "return a value of 10000" in {
      val result = CalculateRequestConstructor.rebasedValue(10000)
      result shouldBe "&initialValueAmt=10000"
    }

    "return a value of 2000" in {
      val result = CalculateRequestConstructor.rebasedValue(2000)
      result shouldBe "&initialValueAmt=2000"
    }
  }

  "Calling revaluationCost" should {

    "return a value of 10000" in {
      val model = RebasedCostsModel("Yes", Some(10000))
      val result = CalculateRequestConstructor.revaluationCost(model)
      result shouldBe "&initialCostsAmt=10000"
    }

    "return a value of 2000" in {
      val model = RebasedCostsModel("Yes", Some(2000))
      val result = CalculateRequestConstructor.revaluationCost(model)
      result shouldBe "&initialCostsAmt=2000"
    }

    "return a value of 0" in {
      val model = RebasedCostsModel("No", None)
      val result = CalculateRequestConstructor.revaluationCost(model)
      result shouldBe "&initialCostsAmt=0"
    }
  }

  "Calling rebasedReliefs" should {

    "return a value of 10000" in {
      val result = CalculateRequestConstructor.rebasedReliefs(Some(10000))
      result shouldBe "&reliefs=10000"
    }

    "return a value of 2000" in {
      val result = CalculateRequestConstructor.rebasedReliefs(Some(2000))
      result shouldBe "&reliefs=2000"
    }

    "return a value of 0" in {
      val result = CalculateRequestConstructor.rebasedReliefs(None)
      result shouldBe "&reliefs=0"
    }
  }

  "Calling taAcquisitionDate" should {

    "return a value of 2015-10-9" in {
      val model = AcquisitionDateModel(9, 10, 2015)
      val result = CalculateRequestConstructor.taAcquisitionDate(model)
      result shouldBe "&acquisitionDate=2015-10-9"
    }

    "return a value of 2016-3-20" in {
      val model = AcquisitionDateModel(20, 3, 2016)
      val result = CalculateRequestConstructor.taAcquisitionDate(model)
      result shouldBe "&acquisitionDate=2016-3-20"
    }
  }

  "Calling taReliefs" should {

    "return a value of 10000" in {
      val result = CalculateRequestConstructor.taReliefs(Some(10000))
      result shouldBe "&reliefs=10000"
    }

    "return a value of 2000" in {
      val result = CalculateRequestConstructor.taReliefs(Some(2000))
      result shouldBe "&reliefs=2000"
    }

    "return a value of 0" in {
      val result = CalculateRequestConstructor.taReliefs(None)
      result shouldBe "&reliefs=0"
    }
  }

  "Calling priorDisposal" should {

    "return a value of yes" in {
      val result = CalculateRequestConstructor.priorDisposal("Yes")
      result shouldBe "priorDisposal=Yes"
    }

    "return a value of No" in {
      val result = CalculateRequestConstructor.priorDisposal("No")
      result shouldBe "priorDisposal=No"
    }
  }

  "Calling currentIncome" should {

    "return a value of 10000" in {
      val currentIncomeModel = CurrentIncomeModel(10000)
      val result = CalculateRequestConstructor.currentIncome(currentIncomeModel)
      result shouldBe "&currentIncome=10000"
    }

    "return a value of 2000" in {
      val currentIncomeModel = CurrentIncomeModel(2000)
      val result = CalculateRequestConstructor.currentIncome(currentIncomeModel)
      result shouldBe "&currentIncome=2000"
    }
  }

  "Calling personalAllowance" should {

    "return a value of 10000" in {
      val personalAllowanceModel = PersonalAllowanceModel(10000)
      val result = CalculateRequestConstructor.personalAllowanceAmount(Some(personalAllowanceModel))
      result shouldBe "&personalAllowanceAmt=10000"
    }

    "return a value of 2000" in {
      val personalAllowanceModel = PersonalAllowanceModel(2000)
      val result = CalculateRequestConstructor.personalAllowanceAmount(Some(personalAllowanceModel))
      result shouldBe "&personalAllowanceAmt=2000"
    }
  }

  "Calling disposalValue" should {

    "return a value of 10000" in {
      val result = CalculateRequestConstructor.disposalValue(10000)
      result shouldBe "&disposalValue=10000"
    }

    "return a value of 2000" in {
      val result = CalculateRequestConstructor.disposalValue(2000)
      result shouldBe "&disposalValue=2000"
    }
  }

  "Calling disposalCosts" should {

    "return a value of 10000" in {
      val result = CalculateRequestConstructor.disposalCosts(10000)
      result shouldBe "&disposalCosts=10000"
    }

    "return a value of 2000" in {
      val result = CalculateRequestConstructor.disposalCosts(2000)
      result shouldBe "&disposalCosts=2000"
    }
  }

  "Calling disposalDate" should {

    "return a date of 2014-5-10" in {
      val model = DisposalDateModel(10, 5, 2014)
      val result = CalculateRequestConstructor.disposalDate(model)
      result shouldBe "&disposalDate=2014-5-10"
    }

    "return a date of 2013-7-13" in {
      val model = DisposalDateModel(13, 7, 2013)
      val result = CalculateRequestConstructor.disposalDate(model)
      result shouldBe "&disposalDate=2013-7-13"
    }
  }

  "Calling flatReliefs" should {

    "return a value of 10000" in {
      val result = CalculateRequestConstructor.flatReliefs(Some(10000))
      result shouldBe "&reliefs=10000"
    }

    "return a value of 2000" in {
      val result = CalculateRequestConstructor.flatReliefs(Some(2000))
      result shouldBe "&reliefs=2000"
    }

    "return a value of 0" in {
      val result = CalculateRequestConstructor.flatReliefs(None)
      result shouldBe "&reliefs=0"
    }
  }

  "Calling isClaimingPrrRebased" should {

    "return a value of 'Yes'" in {
      val model = PrivateResidenceReliefModel("Yes", None, None)
      val result = CalculateRequestConstructor.isClaimingPrrRebased(Some(model))
      result shouldBe "&isClaimingPRR=Yes"
    }

    "return a value of 'No' when given by the user" in {
      val model = PrivateResidenceReliefModel("No", None, None)
      val result = CalculateRequestConstructor.isClaimingPrrRebased(Some(model))
      result shouldBe "&isClaimingPRR=No"
    }

    "return a value of 'No' when no PRR is available" in {
      val result = CalculateRequestConstructor.isClaimingPrrRebased(None)
      result shouldBe "&isClaimingPRR=No"
    }
  }


  "Calling flatAcquisitionDate" should {

    "return a value of 2015-10-5" in {
      val model = AcquisitionDateModel(5, 10, 2015)
      val answer = "&isClaimingPRR=Yes"
      val result = CalculateRequestConstructor.flatAcquisitionDate(answer, model)
      result shouldBe "&acquisitionDate=2015-10-5"
    }

    "return a value of 2017-8-9" in {
      val model = AcquisitionDateModel(9, 8, 2017)
      val answer = "&isClaimingPRR=Yes"
      val result = CalculateRequestConstructor.flatAcquisitionDate(answer, model)
      result shouldBe "&acquisitionDate=2017-8-9"
    }

    "return an empty string" in {
      val model = AcquisitionDateModel(5, 4, 2017)
      val answer = "&isClaimingPRR=No"
      val result = CalculateRequestConstructor.flatAcquisitionDate(answer, model)
      result shouldBe ""
    }
  }
}
