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

package routes

import org.scalatest.Matchers
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class RoutesSpec extends UnitSpec with WithFakeApplication with Matchers {

  /* Sold or Given Away routes */
  "The URL for the soldOrGivenAway Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/sold-or-given-away" in {
      val path = controllers.routes.SoldOrGivenAwayController.soldOrGivenAway().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/sold-or-given-away"
    }
  }

  "The URL for the submitSoldOrGivenAway Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/sold-or-given-away" in {
      val path = controllers.routes.SoldOrGivenAwayController.submitSoldOrGivenAway().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/sold-or-given-away"
    }
  }

  /* Sold for Less routes */
  "The URL for the soldForLess Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/sold-for-less" in {
      val path = controllers.routes.SoldForLessController.soldForLess().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/sold-for-less"
    }
  }

  "The URL for the submitSoldForLess Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/sold-for-less" in {
      val path = controllers.routes.SoldForLessController.submitSoldForLess().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/sold-for-less"
    }
  }

  /* How Became Owner routes */
  "The URL for the howBecameOwner Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/how-became-owner" in {
      val path = controllers.routes.HowBecameOwnerController.howBecameOwner().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/how-became-owner"
    }
  }

  "The URL for the submitHowBecameOwner Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/how-became-owner" in {
      val path = controllers.routes.HowBecameOwnerController.submitHowBecameOwner().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/how-became-owner"
    }
  }

  /* Bought for Less routes */
  "The URL for the boughtForLess Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/bought-for-less" in {
      val path = controllers.routes.BoughtForLessController.boughtForLess().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/bought-for-less"
    }
  }

  "The URL for the submitBoughtForLess Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/bought-for-less" in {
      val path = controllers.routes.BoughtForLessController.submitBoughtForLess().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/bought-for-less"
    }
  }

  /* Check Your Answers routes */
  "The URL for the checkYourAnswers Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/check-your-answers" in {
      val path = controllers.routes.CheckYourAnswersController.checkYourAnswers().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/check-your-answers"
    }
  }

  "The URL for the submitCheckYourAnswers Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/check-your-answers" in {
      val path = controllers.routes.CheckYourAnswersController.submitCheckYourAnswers().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/check-your-answers"
    }
  }

  /* Market Value When Sold routes */
  "The URL for the marketValueWhenSold Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/market-value-when-sold" in {
      val path = controllers.routes.MarketValueWhenSoldOrGaveAwayController.marketValueWhenSold().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/market-value-when-sold"
    }
  }

  "The URL for the submitMarketValueWhenSold Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/market-value-when-sold" in {
      val path = controllers.routes.MarketValueWhenSoldOrGaveAwayController.submitMarketValueWhenSold().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/market-value-when-sold"
    }
  }

  /* Worth When Bought For Less routes */
  "The URL for the worthWhenBoughtForLess Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/worth-when-bought-for-less" in {
      val path = controllers.routes.WorthWhenBoughtForLessController.worthWhenBoughtForLess().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/worth-when-bought-for-less"
    }
  }

  "The URL for the submitWorthWhenBoughtForLess Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/worth-when-bought-for-less" in {
      val path = controllers.routes.WorthWhenBoughtForLessController.submitWorthWhenBoughtForLess().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/worth-when-bought-for-less"
    }
  }

  /* Worth When Gifted To routes */
  "The URL for the worthWhenGiftedTo Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/worth-when-gifted-to" in {
      val path = controllers.routes.WorthWhenGiftedToController.worthWhenGiftedTo().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/worth-when-gifted-to"
    }
  }

  "The URL for the submitWorthWhenGiftedTo Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/worth-when-gifted-to" in {
      val path = controllers.routes.WorthWhenGiftedToController.submitWorthWhenGiftedTo().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/worth-when-gifted-to"
    }
  }

  /* Worth When Inherited routes */
  "The URL for the worthWhenInherited Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/worth-when-inherited" in {
      val path = controllers.routes.WorthWhenInheritedController.worthWhenInherited().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/worth-when-inherited"
    }
  }

  "The URL for the submitWorthWhenInherited Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/worth-when-inherited" in {
      val path = controllers.routes.WorthWhenInheritedController.submitWorthWhenInherited().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/worth-when-inherited"
    }
  }

  /* Worth Before Legislation Start routes */
  "The URL for the worthBeforeLegislationStart Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/worth-before-legislation-start" in {
      val path = controllers.routes.WorthBeforeLegislationStartController.worthBeforeLegislationStart().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/worth-before-legislation-start"
    }
  }

  "The URL for the submitWorthBeforeLegislationStart Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/worth-before-legislation-start" in {
      val path = controllers.routes.WorthBeforeLegislationStartController.submitWorthBeforeLegislationStart().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/worth-before-legislation-start"
    }
  }

  /* Current Income routes */
  "The URL for the currentIncome Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/current-income" in {
      val path = controllers.routes.CurrentIncomeController.currentIncome().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/current-income"
    }
  }

  "The URL for the submitCurrentIncome Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/current-income" in {
      val path = controllers.routes.CurrentIncomeController.submitCurrentIncome().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/current-income"
    }
  }

  /* Personal Allowance routes */
  "The URL for personalAllowance Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/personal-allowance" in {
      val path = controllers.routes.PersonalAllowanceController.personalAllowance().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/personal-allowance"
    }
  }

  "The URL for submitPersonalAllowance Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/personal-allowance" in {
      val path = controllers.routes.PersonalAllowanceController.submitPersonalAllowance().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/personal-allowance"
    }
  }

  /* Other Properties routes */
  "The URL for otherProperties Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/other-properties" in {
      val path = controllers.routes.OtherPropertiesController.otherProperties().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/other-properties"
    }
  }

  "The URL for submitOtherProperties Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/other-properties" in {
      val path = controllers.routes.OtherPropertiesController.submitOtherProperties().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/other-properties"
    }
  }

  /* Allowance routes */
  "The URL for the allowance Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/allowance" in {
      val path = controllers.routes.AnnualExemptAmountController.annualExemptAmount().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/allowance"
    }
  }

  "The URL for the submitAllowance Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/allowance" in {
      val path = controllers.routes.AnnualExemptAmountController.submitAnnualExemptAmount().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/allowance"
    }
  }

  /* Acquisition Date routes */
  "The URL for the acquisitionDate Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/acquisition-date" in {
      val path = controllers.routes.AcquisitionDateController.acquisitionDate().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/acquisition-date"
    }
  }

  "The URL for the submitAcquisitionDate Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/acquisition-date" in {
      val path = controllers.routes.AcquisitionDateController.submitAcquisitionDate().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/acquisition-date"
    }
  }

  /* Rebased Value routes */
  "The URL for the rebasedValue Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/rebased-value" in {
      val path = controllers.routes.RebasedValueController.rebasedValue().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/rebased-value"
    }
  }

  "The URL for the submitRebasedValue Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/rebased-value" in {
      val path = controllers.routes.RebasedValueController.submitRebasedValue().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/rebased-value"
    }
  }

  /* Rebased Costs routes */
  "The URL for the rebasedCosts Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/rebased-costs" in {
      val path = controllers.routes.RebasedCostsController.rebasedCosts().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/rebased-costs"
    }
  }

  "The URL for the submitRebasedCosts Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/rebased-costs" in {
      val path = controllers.routes.RebasedCostsController.submitRebasedCosts().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/rebased-costs"
    }
  }

  /* Improvements routes */
  "The URL for the improvements Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/improvements" in {
      val path = controllers.routes.ImprovementsController.improvements().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/improvements"
    }
  }

  "The URL for the submitImprovements Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/improvements" in {
      val path = controllers.routes.ImprovementsController.submitImprovements().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/improvements"
    }
  }

  /* Disposal Date routes */
  "The URL for the disposalDate Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/disposal-date" in {
      val path = controllers.routes.DisposalDateController.disposalDate().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/"
    }
  }

  "The URL for the submitDisposalDate Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/disposal-date" in {
      val path = controllers.routes.DisposalDateController.submitDisposalDate().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/disposal-date"
    }
  }

    /* Outside Tax Year routes */
  "The URL for the OutsideTaxYear Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/outside-tax-year" in {
      val path = controllers.routes.OutsideTaxYearController.outsideTaxYear().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/outside-tax-year"
    }
  }



  /* No Capital Gains Tax routes */
  "The URL for the no capital gains tax Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/no-capital-gains-tax" in {
      val path = controllers.routes.NoCapitalGainsTaxController.noCapitalGainsTax().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/no-capital-gains-tax"
    }
  }

  /* Disposal Value routes */
  "The URL for the disposalValue Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/disposal-value" in {
      val path = controllers.routes.DisposalValueController.disposalValue().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/disposal-value"
    }
  }

  "The URL for the submitDisposalValue Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/disposal-value" in {
      val path = controllers.routes.DisposalValueController.submitDisposalValue().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/disposal-value"
    }
  }

  /* Acquisition Costs routes */
  "The URL for the acquisitionCosts Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/acquisition-costs" in {
      val path = controllers.routes.AcquisitionCostsController.acquisitionCosts().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/acquisition-costs"
    }
  }

  "The URL for the submitAcquisitionCosts Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/acquisition-costs" in {
      val path = controllers.routes.AcquisitionCostsController.submitAcquisitionCosts().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/acquisition-costs"
    }
  }

  /* Disposal Costs routes */
  "The URL for the disposalCosts Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/disposal-costs" in {
      val path = controllers.routes.DisposalCostsController.disposalCosts().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/disposal-costs"
    }
  }

  "The URL for the submitDisposalCosts Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/disposal-costs" in {
      val path = controllers.routes.DisposalCostsController.submitDisposalCosts().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/disposal-costs"
    }
  }

  /* Private Residence Relief routes */
  "The URL for the privateResidenceRelief Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/private-residence-relief" in {
      val path = controllers.routes.PrivateResidenceReliefController.privateResidenceRelief().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/private-residence-relief"
    }
  }

  "The URL for the submitPrivateResidenceRelief Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/private-residence-relief" in {
      val path = controllers.routes.PrivateResidenceReliefController.submitPrivateResidenceRelief().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/private-residence-relief"
    }
  }

  /* Previous Gain Or Loss routes */
  "The URL for the previousGainOrLoss Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/previous-gain-or-loss" in {
      val path = controllers.routes.PreviousGainOrLossController.previousGainOrLoss().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/previous-gain-or-loss"
    }
  }

  "The URL for the submitPreviousGainOrLoss Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/previous-gain-or-loss" in {
      val path = controllers.routes.PreviousGainOrLossController.submitPreviousGainOrLoss().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/previous-gain-or-loss"
    }
  }

  /* How Much Loss routes */
  "The URL for the howMuchLoss Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/how-much-loss" in {
      val path = controllers.routes.HowMuchLossController.howMuchLoss().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/how-much-loss"
    }
  }

  "The URL for the submitHowMuchLoss Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/how-much-loss" in {
      val path = controllers.routes.HowMuchLossController.submitHowMuchLoss().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/how-much-loss"
    }
  }

  /* How Much Gain routes */
  "The URL for the howMuchGain Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/how-much-gain" in {
      val path = controllers.routes.HowMuchGainController.howMuchGain().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/how-much-gain"
    }
  }

  "The URL for the submitHowMuchGain Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/how-much-gain" in {
      val path = controllers.routes.HowMuchGainController.submitHowMuchGain().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/how-much-gain"
    }
  }

  /* Brought Forward Losses routes */
  "The URL for the broughtForwardLosses Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/brought-forward-losses" in {
      val path = controllers.routes.BroughtForwardLossesController.broughtForwardLosses().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/brought-forward-losses"
    }
  }

  "The URL for the submitBroughtForwardLosses Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/brought-forward-losses" in {
      val path = controllers.routes.BroughtForwardLossesController.submitBroughtForwardLosses().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/brought-forward-losses"
    }
  }

  /* Calculation Election routes */
  "The URL for the calculationElection Action" should {
    "be equal to /calculate/your-capital-gains/non-resident/calculation-election" in {
      val path = controllers.routes.CalculationElectionController.calculationElection().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/calculation-election"
    }
  }

  "The URL for the submitCalculationElection Action" should {
    "be equal to /calculate/your-capital-gains/non-resident/calculation-election" in {
      val path = controllers.routes.CalculationElectionController.submitCalculationElection().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/calculation-election"
    }
  }

  /* Other Reliefs routes */
  "The URL for the otherReliefs Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/other-reliefs" in {
      val path = controllers.routes.OtherReliefsController.otherReliefs().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/other-reliefs"
    }
  }

  "The URL for the submitOtherReliefs Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/other-reliefs" in {
      val path = controllers.routes.OtherReliefsController.submitOtherReliefs().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/other-reliefs"
    }
  }

  "The URL for the otherReliefsFlat Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/other-reliefs-flat" in {
      val path = controllers.routes.OtherReliefsFlatController.otherReliefsFlat().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/other-reliefs-flat"
    }
  }

  "The URL for the submitOtherReliefsFlat Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/other-reliefs-flat" in {
      val path = controllers.routes.OtherReliefsFlatController.submitOtherReliefsFlat().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/other-reliefs-flat"
    }
  }

  "The URL for the otherReliefsTimeApportioned Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/other-reliefs-time-apportioned" in {
      val path = controllers.routes.OtherReliefsTAController.otherReliefsTA().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/other-reliefs-time-apportioned"
    }
  }

  "The URL for the submitOtherReliefsTimeApportioned Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/other-reliefs-time-apportioned" in {
      val path = controllers.routes.OtherReliefsTAController.submitOtherReliefsTA().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/other-reliefs-time-apportioned"
    }
  }

  "The URL for the otherReliefsRebased Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/other-reliefs-rebased" in {
      val path = controllers.routes.OtherReliefsRebasedController.otherReliefsRebased().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/other-reliefs-rebased"
    }
  }

  "The URL for the submitOtherReliefsRebased Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/other-reliefs-rebased" in {
      val path = controllers.routes.OtherReliefsRebasedController.submitOtherReliefsRebased().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/other-reliefs-rebased"
    }
  }

  /* Summary routes */
  "The URL for the summary Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/summary" in {
      val path = controllers.routes.SummaryController.summary().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/summary"
    }
  }

  "The URL for the restart Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/restart" in {
      val path = controllers.routes.SummaryController.restart().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/restart"
    }
  }

  /* What Next routes */

  "The URL for the whatNext Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/what-next" in {
      val path = controllers.routes.WhatNextController.whatNext().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/what-next"
    }
  }

  /* Who Did You Give It To / No Tax To Pay routes*/

  "The URL for the whoDidYouGiveItTo Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/who-did-you-give-it-to" in {
      val path = controllers.routes.WhoDidYouGiveItToController.whoDidYouGiveItTo().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/who-did-you-give-it-to"
    }
  }

  "The URL for the submitWhoDidYouGiveItTo Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/who-did-you-give-it-to" in {
      val path = controllers.routes.WhoDidYouGiveItToController.submitWhoDidYouGiveItTo().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/who-did-you-give-it-to"
    }
  }

  "The URL for the noTaxToPay Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/no-tax-to-pay" in {
      val path = controllers.routes.WhoDidYouGiveItToController.noTaxToPay().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/no-tax-to-pay"
    }
  }

  "The URL for the propertyLivedIn Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/property-lived-in" in {
      val path = controllers.routes.PropertyLivedInController.propertyLivedIn().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/property-lived-in"
    }
  }

  "The URL for the submit propertyLivedIn Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/property-lived-in" in {
      val path = controllers.routes.PropertyLivedInController.submitPropertyLivedIn().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/property-lived-in"
    }
  }

  /* Report route*/
  "The URL for the save as pdf Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/summary-report" in {
      val path = controllers.routes.ReportController.summaryReport().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/summary-report"
    }
  }
}
