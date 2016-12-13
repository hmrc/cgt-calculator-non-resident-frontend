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

package routes.nrcgt

import org.scalatest.Matchers
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class RoutesSpec extends UnitSpec with WithFakeApplication with Matchers {

  /* Sold or Given Away routes */
  "The URL for the soldOrGivenAway Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/sold-or-given-away" in {
      val path = controllers.nonresident.routes.SoldOrGivenAwayController.soldOrGivenAway().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/sold-or-given-away"
    }
  }

  "The URL for the submitSoldOrGivenAway Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/sold-or-given-away" in {
      val path = controllers.nonresident.routes.SoldOrGivenAwayController.submitSoldOrGivenAway().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/sold-or-given-away"
    }
  }

  /* Sold for Less routes */
  "The URL for the soldForLess Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/sold-for-less" in {
      val path = controllers.nonresident.routes.SoldForLessController.soldForLess().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/sold-for-less"
    }
  }

  "The URL for the submitSoldForLess Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/sold-for-less" in {
      val path = controllers.nonresident.routes.SoldForLessController.submitSoldForLess().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/sold-for-less"
    }
  }

  /* How Became Owner routes */
  "The URL for the howBecameOwner Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/how-became-owner" in {
      val path = controllers.nonresident.routes.HowBecameOwnerController.howBecameOwner().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/how-became-owner"
    }
  }

  "The URL for the submitHowBecameOwner Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/how-became-owner" in {
      val path = controllers.nonresident.routes.HowBecameOwnerController.submitHowBecameOwner().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/how-became-owner"
    }
  }

  /* Bought for Less routes */
  "The URL for the boughtForLess Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/bought-for-less" in {
      val path = controllers.nonresident.routes.BoughtForLessController.boughtForLess().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/bought-for-less"
    }
  }

  "The URL for the submitBoughtForLess Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/bought-for-less" in {
      val path = controllers.nonresident.routes.BoughtForLessController.submitBoughtForLess().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/bought-for-less"
    }
  }

  /* Check Your Answers routes */
  "The URL for the checkYourAnswers Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/check-your-answers" in {
      val path = controllers.nonresident.routes.CheckYourAnswersController.checkYourAnswers().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/check-your-answers"
    }
  }

  "The URL for the submitCheckYourAnswers Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/check-your-answers" in {
      val path = controllers.nonresident.routes.CheckYourAnswersController.submitCheckYourAnswers().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/check-your-answers"
    }
  }

  /* Market Value When Sold routes */
  "The URL for the marketValueWhenSold Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/market-value-when-sold" in {
      val path = controllers.nonresident.routes.MarketValueWhenSoldOrGaveAwayController.marketValueWhenSold().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/market-value-when-sold"
    }
  }

  "The URL for the submitMarketValueWhenSold Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/market-value-when-sold" in {
      val path = controllers.nonresident.routes.MarketValueWhenSoldOrGaveAwayController.submitMarketValueWhenSold().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/market-value-when-sold"
    }
  }

  /* Worth When Bought For Less routes */
  "The URL for the worthWhenBoughtForLess Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/worth-when-bought-for-less" in {
      val path = controllers.nonresident.routes.WorthWhenBoughtForLessController.worthWhenBoughtForLess().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/worth-when-bought-for-less"
    }
  }

  "The URL for the submitWorthWhenBoughtForLess Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/worth-when-bought-for-less" in {
      val path = controllers.nonresident.routes.WorthWhenBoughtForLessController.submitWorthWhenBoughtForLess().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/worth-when-bought-for-less"
    }
  }

  /* Worth When Gifted To routes */
  "The URL for the worthWhenGiftedTo Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/worth-when-gifted-to" in {
      val path = controllers.nonresident.routes.WorthWhenGiftedToController.worthWhenGiftedTo().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/worth-when-gifted-to"
    }
  }

  "The URL for the submitWorthWhenGiftedTo Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/worth-when-gifted-to" in {
      val path = controllers.nonresident.routes.WorthWhenGiftedToController.submitWorthWhenGiftedTo().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/worth-when-gifted-to"
    }
  }

  /* Worth When Inherited routes */
  "The URL for the worthWhenInherited Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/worth-when-inherited" in {
      val path = controllers.nonresident.routes.WorthWhenInheritedController.worthWhenInherited().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/worth-when-inherited"
    }
  }

  "The URL for the submitWorthWhenInherited Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/worth-when-inherited" in {
      val path = controllers.nonresident.routes.WorthWhenInheritedController.submitWorthWhenInherited().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/worth-when-inherited"
    }
  }

  /* Worth Before Legislation Start routes */
  "The URL for the worthBeforeLegislationStart Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/worth-before-legislation-start" in {
      val path = controllers.nonresident.routes.WorthBeforeLegislationStartController.worthBeforeLegislationStart().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/worth-before-legislation-start"
    }
  }

  "The URL for the submitWorthBeforeLegislationStart Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/worth-before-legislation-start" in {
      val path = controllers.nonresident.routes.WorthBeforeLegislationStartController.submitWorthBeforeLegislationStart().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/worth-before-legislation-start"
    }
  }

  /* Rebased Valuation Cost routes */
  "The URL for the rebasedValuationCost Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/rebased-valuation-cost" in {
      val path = controllers.nonresident.routes.RebasedValuationCostController.rebasedValuationCost().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/rebased-valuation-cost"
    }
  }

  "The URL for the submitRebasedValuationCost Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/rebased-valuation-cost" in {
      val path = controllers.nonresident.routes.RebasedValuationCostController.submitRebasedValuationCost().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/rebased-valuation-cost"
    }
  }

  /* Customer Type routes */
  "The URL for the customerType Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/" in {
      val path = controllers.nonresident.routes.CustomerTypeController.customerType().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/customer-type"
    }
  }

  "The URL for the submit customerType Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/" in {
      val path = controllers.nonresident.routes.CustomerTypeController.submitCustomerType().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/customer-type"
    }
  }

  /* Disabled Trustee routes */
  "The URL for the disabledTrustee Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/disabled-trustee" in {
      val path = controllers.nonresident.routes.DisabledTrusteeController.disabledTrustee().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/disabled-trustee"
    }
  }

  "The URL for the submitDisabledTrustee Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/disabled-trustee" in {
      val path = controllers.nonresident.routes.DisabledTrusteeController.submitDisabledTrustee().url

      path shouldEqual "/calculate-your-capital-gains/non-resident/disabled-trustee"
    }
  }

  /* Current Income routes */
  "The URL for the currentIncome Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/current-income" in {
      val path = controllers.nonresident.routes.CurrentIncomeController.currentIncome().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/current-income"
    }
  }

  "The URL for the submitCurrentIncome Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/current-income" in {
      val path = controllers.nonresident.routes.CurrentIncomeController.submitCurrentIncome().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/current-income"
    }
  }

  /* Personal Allowance routes */
  "The URL for personalAllowance Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/personal-allowance" in {
      val path = controllers.nonresident.routes.PersonalAllowanceController.personalAllowance().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/personal-allowance"
    }
  }

  "The URL for submitPersonalAllowance Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/personal-allowance" in {
      val path = controllers.nonresident.routes.PersonalAllowanceController.submitPersonalAllowance().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/personal-allowance"
    }
  }

  /* Other Properties routes */
  "The URL for otherProperties Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/other-properties" in {
      val path = controllers.nonresident.routes.OtherPropertiesController.otherProperties().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/other-properties"
    }
  }

  "The URL for submitOtherProperties Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/other-properties" in {
      val path = controllers.nonresident.routes.OtherPropertiesController.submitOtherProperties().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/other-properties"
    }
  }

  /* Allowance routes */
  "The URL for the allowance Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/allowance" in {
      val path = controllers.nonresident.routes.AnnualExemptAmountController.annualExemptAmount().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/allowance"
    }
  }

  "The URL for the submitAllowance Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/allowance" in {
      val path = controllers.nonresident.routes.AnnualExemptAmountController.submitAnnualExemptAmount().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/allowance"
    }
  }

  /* Acquisition Date routes */
  "The URL for the acquisitionDate Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/acquisition-date" in {
      val path = controllers.nonresident.routes.AcquisitionDateController.acquisitionDate().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/acquisition-date"
    }
  }

  "The URL for the submitAcquisitionDate Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/acquisition-date" in {
      val path = controllers.nonresident.routes.AcquisitionDateController.submitAcquisitionDate().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/acquisition-date"
    }
  }

  /* Rebased Value routes */
  "The URL for the rebasedValue Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/rebased-value" in {
      val path = controllers.nonresident.routes.RebasedValueController.rebasedValue().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/rebased-value"
    }
  }

  "The URL for the submitRebasedValue Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/rebased-value" in {
      val path = controllers.nonresident.routes.RebasedValueController.submitRebasedValue().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/rebased-value"
    }
  }

  /* Rebased Costs routes */
  "The URL for the rebasedCosts Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/rebased-costs" in {
      val path = controllers.nonresident.routes.RebasedCostsController.rebasedCosts().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/rebased-costs"
    }
  }

  "The URL for the submitRebasedCosts Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/rebased-costs" in {
      val path = controllers.nonresident.routes.RebasedCostsController.submitRebasedCosts().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/rebased-costs"
    }
  }

  /* Improvements routes */
  "The URL for the improvements Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/improvements" in {
      val path = controllers.nonresident.routes.ImprovementsController.improvements().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/improvements"
    }
  }

  "The URL for the submitImprovements Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/improvements" in {
      val path = controllers.nonresident.routes.ImprovementsController.submitImprovements().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/improvements"
    }
  }

  /* Disposal Date routes */
  "The URL for the disposalDate Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/disposal-date" in {
      val path = controllers.nonresident.routes.DisposalDateController.disposalDate().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/"
    }
  }

  "The URL for the submitDisposalDate Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/disposal-date" in {
      val path = controllers.nonresident.routes.DisposalDateController.submitDisposalDate().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/disposal-date"
    }
  }

  /* No Capital Gains Tax routes */
  "The URL for the no capital gains tax Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/no-capital-gains-tax" in {
      val path = controllers.nonresident.routes.NoCapitalGainsTaxController.noCapitalGainsTax().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/no-capital-gains-tax"
    }
  }

  /* Disposal Value routes */
  "The URL for the disposalValue Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/disposal-value" in {
      val path = controllers.nonresident.routes.DisposalValueController.disposalValue().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/disposal-value"
    }
  }

  "The URL for the submitDisposalValue Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/disposal-value" in {
      val path = controllers.nonresident.routes.DisposalValueController.submitDisposalValue().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/disposal-value"
    }
  }

  /* Acquisition Costs routes */
  "The URL for the acquisitionCosts Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/acquisition-costs" in {
      val path = controllers.nonresident.routes.AcquisitionCostsController.acquisitionCosts().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/acquisition-costs"
    }
  }

  "The URL for the submitAcquisitionCosts Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/acquisition-costs" in {
      val path = controllers.nonresident.routes.AcquisitionCostsController.submitAcquisitionCosts().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/acquisition-costs"
    }
  }

  /* Disposal Costs routes */
  "The URL for the disposalCosts Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/disposal-costs" in {
      val path = controllers.nonresident.routes.DisposalCostsController.disposalCosts().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/disposal-costs"
    }
  }

  "The URL for the submitDisposalCosts Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/disposal-costs" in {
      val path = controllers.nonresident.routes.DisposalCostsController.submitDisposalCosts().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/disposal-costs"
    }
  }

  /* Private Residence Relief routes */
  "The URL for the privateResidenceRelief Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/private-residence-relief" in {
      val path = controllers.nonresident.routes.PrivateResidenceReliefController.privateResidenceRelief().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/private-residence-relief"
    }
  }

  "The URL for the submitPrivateResidenceRelief Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/private-residence-relief" in {
      val path = controllers.nonresident.routes.PrivateResidenceReliefController.submitPrivateResidenceRelief().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/private-residence-relief"
    }
  }

  /* Allowable Losses routes */
  "The URL for the allowableLosses Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/allowable-losses" in {
      val path = controllers.nonresident.routes.AllowableLossesController.allowableLosses().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/allowable-losses"
    }
  }

  "The URL for the submitAllowableLosses Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/allowable-losses" in {
      val path = controllers.nonresident.routes.AllowableLossesController.submitAllowableLosses().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/allowable-losses"
    }
  }

  /* Previous Gain Or Loss routes */
  "The URL for the previousGainOrLoss Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/previous-gain-or-loss" in {
      val path = controllers.nonresident.routes.PreviousGainOrLossController.previousGainOrLoss().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/previous-gain-or-loss"
    }
  }

  "The URL for the submitPreviousGainOrLoss Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/previous-gain-or-loss" in {
      val path = controllers.nonresident.routes.PreviousGainOrLossController.submitPreviousGainOrLoss().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/previous-gain-or-loss"
    }
  }

  /* How Much Loss routes */
  "The URL for the howMuchLoss Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/how-much-loss" in {
      val path = controllers.nonresident.routes.HowMuchLossController.howMuchLoss().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/how-much-loss"
    }
  }

  "The URL for the submitHowMuchLoss Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/how-much-loss" in {
      val path = controllers.nonresident.routes.HowMuchLossController.submitHowMuchLoss().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/how-much-loss"
    }
  }

  /* How Much Gain routes */
  "The URL for the howMuchGain Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/how-much-gain" in {
      val path = controllers.nonresident.routes.HowMuchGainController.howMuchGain().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/how-much-gain"
    }
  }

  "The URL for the submitHowMuchGain Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/how-much-gain" in {
      val path = controllers.nonresident.routes.HowMuchGainController.submitHowMuchGain().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/how-much-gain"
    }
  }

  /* Brought Forward Losses routes */
  "The URL for the broughtForwardLosses Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/brought-forward-losses" in {
      val path = controllers.nonresident.routes.BroughtForwardLossesController.broughtForwardLosses().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/brought-forward-losses"
    }
  }

  "The URL for the submitBroughtForwardLosses Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/brought-forward-losses" in {
      val path = controllers.nonresident.routes.BroughtForwardLossesController.submitBroughtForwardLosses().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/brought-forward-losses"
    }
  }

  /* Calculation Election routes */
  "The URL for the calculationElection Action" should {
    "be equal to /calculate/your-capital-gains/non-resident/calculation-election" in {
      val path = controllers.nonresident.routes.CalculationElectionController.calculationElection().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/calculation-election"
    }
  }

  "The URL for the submitCalculationElection Action" should {
    "be equal to /calculate/your-capital-gains/non-resident/calculation-election" in {
      val path = controllers.nonresident.routes.CalculationElectionController.submitCalculationElection().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/calculation-election"
    }
  }

  /* Other Reliefs routes */
  "The URL for the otherReliefs Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/other-reliefs" in {
      val path = controllers.nonresident.routes.OtherReliefsController.otherReliefs().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/other-reliefs"
    }
  }

  "The URL for the submitOtherReliefs Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/other-reliefs" in {
      val path = controllers.nonresident.routes.OtherReliefsController.submitOtherReliefs().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/other-reliefs"
    }
  }

  "The URL for the otherReliefsFlat Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/other-reliefs-flat" in {
      val path = controllers.nonresident.routes.OtherReliefsFlatController.otherReliefsFlat().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/other-reliefs-flat"
    }
  }

  "The URL for the submitOtherReliefsFlat Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/other-reliefs-flat" in {
      val path = controllers.nonresident.routes.OtherReliefsFlatController.submitOtherReliefsFlat().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/other-reliefs-flat"
    }
  }

  "The URL for the otherReliefsTimeApportioned Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/other-reliefs-time-apportioned" in {
      val path = controllers.nonresident.routes.OtherReliefsTAController.otherReliefsTA().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/other-reliefs-time-apportioned"
    }
  }

  "The URL for the submitOtherReliefsTimeApportioned Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/other-reliefs-time-apportioned" in {
      val path = controllers.nonresident.routes.OtherReliefsTAController.submitOtherReliefsTA().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/other-reliefs-time-apportioned"
    }
  }

  "The URL for the otherReliefsRebased Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/other-reliefs-rebased" in {
      val path = controllers.nonresident.routes.OtherReliefsRebasedController.otherReliefsRebased().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/other-reliefs-rebased"
    }
  }

  "The URL for the submitOtherReliefsRebased Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/other-reliefs-rebased" in {
      val path = controllers.nonresident.routes.OtherReliefsRebasedController.submitOtherReliefsRebased().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/other-reliefs-rebased"
    }
  }

  /* Summary routes */
  "The URL for the summary Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/summary" in {
      val path = controllers.nonresident.routes.SummaryController.summary().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/summary"
    }
  }

  "The URL for the restart Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/restart" in {
      val path = controllers.nonresident.routes.SummaryController.restart().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/restart"
    }
  }

  /* Report route*/
  "The URL for the save as pdf Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/summary-report" in {
      val path = controllers.nonresident.routes.ReportController.summaryReport().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/summary-report"
    }
  }
}