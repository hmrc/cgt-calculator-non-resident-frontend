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

package assets

import common.Constants
import uk.gov.hmrc.play.views.helpers.MoneyPounds

object KeyLookup {

  // TO MOVE

  trait Common {

    val externalLink = "calc.base.externalLink"
    val change = "calc.base.change"
    val back = "calc.base.back"
    val continue = "calc.base.continue"
    val yes = "calc.base.yes"
    val no = "calc.base.no"
//    val day = "calc.common.date.fields.day"
//    val month = "calc.common.date.fields.month"
//    val year = "calc.common.date.fields.year"

//    val readMore = "calc.common.readMore"

    val mandatoryAmount = "calc.common.error.mandatoryAmount"
//    val minimumAmount = "calc.common.error.maxNumericExceeded"
//    val maximumAmount = "calc.common.error.maxAmountExceeded"
    val errorRequired = "calc.common.error.fieldRequired"

    def maximumLimit(limit: String): String = s"Enter an amount that's £$limit or less"

    def maximumError(value: String): String = s"calc.common.error.maxNumericExceeded" + s"$value " +
      "calc.common.error.maxNumericExceeded.OrLess"

//    val invalidAmount = "calc.common.error.mandatoryAmount"
//    val invalidAmountNoDecimal = "error.real"
    val numericPlayErrorOverride = "error.number"
    val optionReqError = "calc.base.optionReqError"

//    val whatToDoNextTextTwo = "You need to tell HMRC about the property"
//    val whatToDoNextFurtherDetails = "Further details on how to tell HMRC about this property can be found at"
  }

  object NonResident extends Common {

    val pageHeading = "calc.base.pageHeading"
    val errorInvalidDate = "calc.common.date.error.invalidDate"
    val errorRealNumber = "error.real"

    object AcquisitionCosts {
      val question = "calc.acquisitionCosts.question"
//      val helpText = "Costs include agent fees, legal fees and surveys"
      val errorNegative = "calc.acquisitionCosts.errorNegative"
      val errorDecimalPlaces = "calc.acquisitionCosts.errorDecimalPlaces"
      val bulletTitle = "calc.acquisitionCosts.bulletTitle"
      val bulletOne = "calc.acquisitionCosts.bulletOne"
      val bulletTwo = "calc.acquisitionCosts.bulletTwo"
      val bulletThree = "calc.acquisitionCosts.bulletThree"
      val hint ="calc.acquisitionCosts.hint"

//      def errorMaximum(value: String): String = s"Enter an amount that's £$value or less"
    }

    object AcquisitionDate {
//      val question = "calc.acquisitionDate.question"
//      val questionTwo = "calc.acquisitionDate.questionTwo"
//      val hintText = "calc.acquisitionDate.hintText"
//      val errorIncompleteDate = "calc.acquisitionDate.errorIncompleteDate"
//      val errorFutureDate = "calc.acquisitionDate.errorFutureDate"
      val errorFutureDateGuidance = "calc.acquisitionDate.errorFutureDateGuidance"
    }

    object AcquisitionValue {
//      val question = "calc.acquisitionValue.question"
//      val helpText = "calc.acquisitionValue.helpText"
//      val bulletTitle = "Put the market value of the property instead if you:"
//      val bulletOne = "inherited it"
//      val bulletTwo = "got it as a gift"
//      val bulletThree = "bought it from a relative, business partner or someone else you're connected to"
//      val bulletFour = "bought it for less than it's worth because the seller wanted to help you"
//      val bulletFive = "became the owner before 1 April 1982"
//      val bulletLink = "someone else you're connected to"
      val errorNegative = "calc.acquisitionValue.errorNegative"
      val errorDecimalPlaces = "calc.acquisitionValue.errorDecimalPlaces"

//      def errorMaximum(value: String): String = s"Enter an amount that's £$value or less"
    }

    object AnnualExemptAmount {
//      val question = "calc.annualExemptAmount.question"
//      val link = "Capital Gains Tax allowances"
//      def hint(amount: String): String = s"calc.annualExemptAmount.helpOne$amount calc.annualExemptAmount.helpTwo"
      val errorMaxStart = "calc.annualExemptAmount.errorMax"
      val errorMaxEnd = "calc.annualExemptAmount.errorMaxEnd"
      val errorNegative = "calc.annualExemptAmount.errorNegative"
      val errorDecimalPlaces = "calc.annualExemptAmount.errorDecimalPlaces"
    }

    object BoughtForLess {
      val question = "calc.boughtForLess.question"
    }

    object CalculationElection {
      val heading = "calc.calculationElection.pageHeading"
      val moreInfoFirstP = "calc.calculationElection.paragraph.one"
      val moreInfoSecondP = "calc.calculationElection.paragraph.two"
      val legend = "calc.calculationElection.legend"
      val otherTaxRelief = "calc.calculationElection.otherRelief"
      val someOtherTaxRelief = "calc.calculationElection.someOtherRelief"
      val timeApportioned = "Based on the percentage of your total gain made since"
      val timeApportionedDescription = "calc.calculationElection.description.time"
      val rebased = "calc.calculationElection.message.time"
      val rebasedDescription = "calc.calculationElection.description.rebased"
      val taxStartDate = "calc.calculationElection.message.rebasedDate"
      val flat = "calc.calculationElection.message.flat"
      val flatDescription = "calc.calculationElection.description.flat"
    }

    object CalculationElectionNoReliefs {
      val title = "calc.calculationElectionNoReliefs.title"
      val helpText = "calc.calculationElectionNoReliefs.helpText"
      def helpTextMethodType(method: String): String =
        s"calc.calculationElectionNoReliefs.helpTextMethodPartOne $method calc.calculationElectionNoReliefs.helpTextMethodPartTwo"
      val helpTextChooseMethod = "calc.calculationElectionNoReliefs.helpTextChooseMethod"
      val rebasing = "rebasing method"
      val flatGain = "gain over whole period of ownership method"
      val straightLine = "straight-line apportionment method"
    }

    object CheckYourAnswers {
      val question = "calc.checkYourAnswers.title"
      val tableHeading = "calc.checkYourAnswers.tableHeading"
      val change = "calc.checkYourAnswers.change"
      val hiddenText = "calc.checkYourAnswers.hidden.text"
    }

    object CurrentIncome {
      val question = "calc.currentIncome.question"
      val linkOne = "Income Tax"
      val linkTwo = "Previous tax years"
      val helpText = "calc.currentIncome.helpText.line1 calc.currentIncome.helpText.line2"
      val errorNegative = "calc.currentIncome.errorNegative"
      val errorDecimalPlace = "calc.currentIncome.errorDecimalPlaces"

      def errorMaximum(value: String): String = s"Enter an amount that's £$value or less"
    }

    object CustomerType {
      val question = "Who owned the property?"
      val individual = "I owned it"
      val trustee = "I was a trustee"
      val personalRep = "I was the executor of an estate"
      val errorInvalid = "Invalid customer type"
    }

    object DisabledTrustee {
      val question = "Are you a trustee for someone who's vulnerable?"
      val helpText = "A person's vulnerable if they're disabled, or if they're under 18 and their parents have died"
      val linkOne = "Trusts and Capital Gains Tax"
    }

    object DisposalCosts {

      val question = "calc.disposalCosts.question"

      val helpTitle = "calc.disposalCosts.helpTitle"
      val helpBulletOne = "calc.disposalCosts.helpBulletOne"
      val helpBulletTwo = "calc.disposalCosts.helpBulletTwo"
      val helpBulletThree = "calc.disposalCosts.helpBulletThree"
      val helpBulletFour = "calc.disposalCosts.helpBulletFour"

      val errorNegativeNumber = "calc.disposalCosts.errorNegativeNumber"
      val errorDecimalPlaces = "calc.disposalCosts.errorDecimalPlaces"

      val jointOwnership = "calc.disposalCosts.jointOwnership"
    }

    object DisposalDate {

      val question = "calc.disposalDate.question"
      val errorDateAfter = "This can't be before the date you became the owner"

    }

    object SoldForLess {
      val question = "calc.nonResident.soldForLess.question"
    }

    object DisposalValue {

      val question = "calc.disposalValue.question"
      val errorDecimalPlaces = "calc.disposalValue.errorDecimalPlaces"
      val errorNegative = "calc.disposalValue.errorNegative"
      val bulletIntro = "Put the market value of the property instead if you:"
      val bulletOne = "gave it away as a gift"
      val bulletTwo = "sold it to a relative, business partner or"
      val bulletTwoLink = "someone else you're connected to"
      val bulletThree = "sold it for less than it's worth to help the buyer"
      val jointOwnership = "calc.disposalValue.jointOwnership"
      //def errorMaximum(value: String): String = s"Enter an amount that's £$value or less"
      def errorMaximum(value: String): String = s"calc.common.error.maxNumericExceeded" + s"$value " +
        "calc.common.error.maxNumericExceeded.OrLess"
    }

    object HowMuchGain {

      val question = "calc.howMuchGain.question"
      val errorNegativeNumber = "calc.howMuchGain.errorNegative"
      val errorDecimalPlaces = "calc.howMuchGain.errorDecimalPlaces"
      }

    object Improvements extends Common {
      val helpOne = "calc.resident.properties.improvements.hint."
      val helpTwo = "calc.improvements.helpTwo"
      val exampleTitle = "calc.resident.properties.improvements.helpButton"
      val exampleOne = "calc.resident.properties.improvements.additionalContent.one"
      val exampleTwo = "calc.resident.properties.improvements.additionalContent.two"
      val question = "calc.improvements.question"
      val ownerBeforeLegislationStartQuestion = "calc.resident.properties.improvements.questionBefore"
      val questionTwo = "calc.improvements.questionTwo"
      val questionThree = "calc.improvements.questionThree"
      val questionFour = "calc.improvements.questionFour"
      val noValueSuppliedError = "calc.improvements.error.no.value.supplied"
      val negativeValueError = "calc.improvements.errorNegative"
      val excessDecimalPlacesError = "calc.improvements.errorDecimalPlaces"
      val jointOwnership = "calc.improvements.jointOwnership"

      def errorMaximum(value: String): String = s"Enter an amount that's £$value or less"

    }

    object NoCapitalGainsTax {

      val title = "nocgt.invaliddate.title"
      val paragraphOne = "nocgt.content.one"
      val paragraphTwo = "nocgt.content.two"
      val changeLink = "nocgt.content.change"
      val returnLink = "nocgt.content.return"
    }

    object OtherProperties {
      val question = "calc.otherProperties.question"
      val questionTwo = "calc.howMuchGain.question"
      val questionTwoHelpTextStart = "How to"
      val questionTwoHelpTextLinkText = "work out your total taxable gains"
      val errorNegative = "calc.howMuchGain.errorNegative"
      val errorDecimalPlaces = "calc.howMuchGain.errorDecimalPlaces"
      val errorQuestion = "Enter a value for your taxable gain"
      val linkOne = "Capital Gains Tax"
      val linkTwo = "Previous tax years"
    }

    object PreviousLossOrGain {
      val question = "calc.previousLossOrGain.question"
      val mandatoryCheck = "calc.previousLossOrGain.errors.required"
      val loss = "calc.previousLossOrGain.loss"
      val gain = "calc.previousLossOrGain.gain"
      val neither = "calc.previousLossOrGain.neither"
      val CGTlink = "Capital Gains Tax"
      val previousTaxLink = "Previous tax years"
      val hintOne = "calc.previousLossOrGain.hintOne"
      val hintTwo = "calc.previousLossOrGain.hintTwo"
    }

    object OtherReliefs {
      val help = "calc.otherReliefs.help"
      val question = "calc.otherReliefs.question"
      val totalGain = "calc.otherReliefs.totalGain"
      val taxableGain = "calc.otherReliefs.taxableGain"
      val lossCarriedForward = "calc.summary.calculation.details.lossCarriedForward"
      val addRelief = "calc.otherReliefs.button.addRelief"
      val updateRelief = "calc.otherReliefs.button.updateRelief"
      val errorDecimal = "calc.otherReliefs.errorDecimal"
      val errorNegative = "calc.otherReliefs.errorMinimum"
      val helpTwo = "calc.otherReliefs.helpTwo"
      def additionalHelp(gain: Int, chargeableGain: Int): String = {
        val locale = new java.util.Locale("en", "EN")
        val amount = java.text.NumberFormat.getIntegerInstance(locale)

        val gainText = if (gain < 0) "total loss" else "total gain"
        val chargeableGainText = if (chargeableGain < 0) "an allowable loss" else "a taxable gain"
        s"We've calculated that you've made $chargeableGainText of " +
          s"£${amount.format(chargeableGain.abs)} and a $gainText of £${amount.format(gain.abs)} on your property. " +
          "You'll need these figures to calculate other reliefs."
      }

      def totalLoss(value: String): String = s"Total loss $value"

      def errorMaximum(value: String): String = s"Enter an amount that's £$value or less"
    }

    object MarketValue {
      val disposalGaveAwayQuestion = "calc.marketValue.gaveItAway.question"
      val disposalSoldQuestion = "calc.marketValue.sold.question"

      val disposalHelpText = "calc.marketValue.helpText"
      val disposalHelpTextAdditional = "calc.marketValue.helpTextAdditional"

      val disposalErrorDecimalPlacesGaveAway = "calc.marketValue.error.gaveItAway.decimalPlaces"
      val disposalErrorDecimalPlacesSold = "calc.marketValue.error.sold.decimalPlaces"

      val errorNegativeGaveAway = "calc.marketValue.error.gaveItAway.negative"
      val errorNegativeSold = "calc.marketValue.error.sold.negative"

      val jointOwnership = "calc.marketValue.jointOwnership"
    }

    object PersonalAllowance {
      val question = "calc.personalAllowance.question"
      val link = "calc.personalAllowance.link"
      val help = "calc.personalAllowance.help"
      val errorNegative = "calc.personalAllowance.errorNegative"
      val errorDecimalPlaces = "calc.personalAllowance.errorDecimalPlaces"
      val errorMaxLimit = "calc.personalAllowance.errorMaxLimit"
      val errorMaxLimitEnd = "calc.personalAllowance.errorMaxLimitEnd"
    }

    object PrivateResidenceRelief {
      val question = "calc.privateResidenceRelief.question"
      val intro = "calc.privateResidenceRelief.intro"
      val findOut = "calc.privateResidenceRelief.helpText"
      val findOutAboutPRRLink = "calc.privateResidenceRelief.helpLink"
      val formHelp = "calc.privateResidenceRelief.formHintExplanation"
      val questionBefore = "calc.privateResidenceRelief.firstQuestion"
      val questionBetween = "calc.privateResidenceRelief.questionBetween.partOne"
      val questionBetweenEnd = "calc.privateResidenceRelief.questionBetween.partTwo"
      def questionAcquisitionDateAfterStartDate(message: String): String =
        "calc.privateResidenceRelief.questionFlat"
      val questionBetweenWhyThisDate: String = "calc.privateResidenceRelief.daysBetweenHelpText"
      val questionBeforeWhyThisDate = "calc.privateResidenceRelief.daysBeforeHelpText"
      val helpTextSubtitle = "calc.privateResidenceRelief.helpTextSubTitle"
      val helpTextBeforeAfter = "calc.privateResidenceRelief.helpTextBeforeAfter"
      val helpTextJustBefore = "calc.privateResidenceRelief.helpTextJustBefore"
      val errorNoValue = "calc.privateResidenceRelief.error.noValueProvided"
      val errorNegative = "calc.privateResidenceRelief.error.errorNegative"
      val errorDecimalPlaces = "calc.privateResidenceRelief.error.errorDecimalPlaces"

      def errorMaximum(value: String): String = "calc.privateResidenceRelief.error.maxNumericExceeded" + s" $value " +
      "calc.privateResidenceRelief.error.maxNumericExceeded.OrLess"
    }

    object RebasedCosts {
      val question = "calc.rebasedCosts.question"
      val inputQuestion = "calc.rebasedCosts.questionTwo"
      val jointOwnership = "calc.rebasedCosts.jointOwnership"
      val errorNegative = "calc.rebasedCosts.errorNegative"
      val errorNoValue = "calc.rebasedCosts.error.no.value.supplied"
      val errorDecimalPlaces = "calc.rebasedCosts.errorDecimalPlaces"

      def errorMaximum(value: String): String = s"Enter an amount that's £$value or less"
    }

    object RebasedValue {
      val question = "calc.nonResident.rebasedValue.questionAndDate"

      val questionOptionalText = "Only tell us if you owned the property on that date"

      val inputHintText = "calc.nonResident.rebasedValue.hintText"
      val jointOwnership = "calc.nonResident.rebasedValue.jointOwnership"
      val additionalContentTitle = "calc.nonResident.rebasedValue.helpHidden.title"
      val helpHiddenContent = "calc.nonResident.rebasedValue.helpText"

      val errorNoValue = "calc.nonResident.rebasedValue.error.no.value.supplied"
      val errorNegative = "calc.nonResident.rebasedValue.errorNegative"
      val errorDecimalPlaces = "calc.nonResident.rebasedValue.errorDecimalPlaces"


      def errorMaximum(value: String): String = s"Enter an amount that's £$value or less"
    }

    object HowBecameOwner {
      val question = "calc.howBecameOwner.question"
      val errorMandatory = "calc.howBecameOwner.errors.required"
      val bought = "calc.howBecameOwner.bought"
      val gifted = "calc.howBecameOwner.gifted"
      val inherited = "calc.howBecameOwner.inherited"
    }

    object SoldOrGivenAway {
      val question = "calc.soldOrGivenAway.question"
      val sold = "calc.soldOrGivenAway.sold"
      val gave = "calc.soldOrGivenAway.gave"
    }

    //Acquisition Market Value messages
    object AcquisitionMarketValue {
      val errorNegativeNumber = "calc.acquisitionMarketValue.errorNegative"
      val errorDecimalPlaces = "calc.acquisitionMarketValue.errorDecimalPlaces"
      val hintOne = "calc.worthWhenBoughtForLess.hint"
      val hintTwo = "calc.worthWhenBoughtForLess.hint"
    }

    object WorthBeforeLegislationStart {
      val question = "calc.worthBeforeLegislationStart.question"
      val information = "calc.worthBeforeLegislationStart.information"
      val hintText = "calc.worthBeforeLegislationStart.help"
      val jointOwnership = "calc.worthBeforeLegislationStart.jointOwnership"
    }

    //Worth When Inherited messages
    object WorthWhenInherited {
      val question = "What was the market value of the property when you inherited it?"
      val hint = "This is the value of the property on the date the previous owner died."
      val helpText = "If you owned the property with someone else, only enter your share of the property value."
    }

    //Worth When Gifted To messages
    object WorthWhenGiftedTo {
      val question = "What was the market value of the property when you got it as a gift?"
      val hintText = "If you don't know the exact value, you must provide a realistic estimate. " +
        "You might have to pay more if we think your estimate is unrealistic."
      val jointOwnership = "If you owned the property with someone else, only enter your share of the property value."
    }

    //Worth When Bought for Less messages
    object WorthWhenBoughtForLess {
      val question = "What was the market value of the property when you bought it?"
      val hintOne = "You can use a valuation from a surveyor. If you don't know the exact value, you must provide a realistic estimate. You might have to pay more if we think your estimate is unrealistic."
      val helpText = "If you owned the property with someone else, only enter your share of the property value."
    }

    object HowMuchLoss {
      val question = "calc.howMuchLoss.question"
      val errorNegative = "calc.howMuchLoss.errorMinimum"
      val errorDecimalPlaces = "calc.howMuchLoss.errorDecimal"

      def errorMaximum(value: String): String = s"Enter an amount that's £$value or less"
    }

    object BroughtForwardLosses {
      val question = "calc.broughtForwardLosses.question"
      val inputQuestion = "calc.broughtForwardLosses.inputQuestion"
      val helpText = "calc.broughtForwardLosses.helpText"
      val linkOne = "Capital Gains Tax"
      val linkTwo = "Previous tax years"
      val errorDecimalPlaces = "calc.broughtForwardLosses.errorDecimal"
      val errorNegative = "calc.broughtForwardLosses.errorNegative"

      def errorMaximum(value: String): String = s"Enter an amount that's £$value or less"
    }

    object Summary {

      val title = "Summary"
      val secondaryHeading = "You owe"
      val amountOwed = "Amount you owe"
      val calculationDetailsTitle = "Calculation details"
      val totalGain = "Your total gain"
      val totalLoss = "Loss"
      val usedAEA = "Capital Gains Tax allowance used"
      val remainingAEA = "Capital Gains Tax allowance remaining"
      def usedAllowableLosses(taxYear: String): String = s"Loss used from $taxYear tax year"
      def usedBroughtForwardLosses(taxYear: String): String = s"Loss used from tax years before $taxYear"
      val lossesRemaining = "Carried forward loss"
      val taxableGain = "Your taxable gain"
      val taxRate = "Tax rate"
      val prrUsed = "Private resident relief used"
      val otherReliefsUsed = "Other tax reliefs used"
      val personalDetailsTitle = "Personal details"
      val purchaseDetailsTitle = "Owning the property"
      val propertyDetailsTitle = "Property details"
      val salesDetailsTitle = "Selling or giving away the property"
      val deductionsTitle = "Deductions"
      val whatToDoNextText = "What to do next"
      val whatToDoNextContent = "Before you continue, save a copy of your calculation. You will need this when you report your Capital Gains Tax."
      val whatToDoNextLink = "tell HMRC about the property"
      val startAgain = "Start again"
      val calculationElection = "What would you like to base your tax on?"
      val timeCalculation = "The percentage of your total gain you've made since"
      val flatCalculation = "How much you've gained on the property since you became the owner"
      val rebasedCalculation = "How much you've gained on the property since 5 April 2015"
      val lossesCarriedForward = "Loss carried forward"
      val taxYearWarning = "Your total might be less accurate because you didn't sell or give away your property in this tax year"
      val saveAsPdf = "Download your Capital Gains Tax calculation (PDF, under 25kB)"
      val yourAnswers = "You've told us"
      val noticeSummary: String = "Your result may be inaccurate because the calculator does not support the date of sale you entered. " +
        "Do not use these figures to report your Capital Gains Tax."
      val whoOwnedIt = "Who owned the property?"


      def basedOnYear(year: String): String = s"These figures are based on the tax rates from the $year tax year"
    }

    object Report {
      val logoText = "HM Revenue & Customs"
      val title = "Calculate your Capital Gains Tax"
    }

    object ClaimingReliefs {
      val title = "calc.claimingReliefs.title"
      val helpText = "calc.claimingReliefs.helpText"
      val errorMandatory = "calc.claimingReliefs.errorMandatory"
    }

    object CostsAtLegislationStart {
      val title = "calc.costsAtLegislationStart.title"
      val howMuch = "calc.costsAtLegislationStart.howMuch"
      val helpText = "calc.costsAtLegislationStart.helpText"
      val errorNegative = "calc.costsAtLegislationStart.errorNegative"
      val errorNoValue = "calc.costsAtLegislationStart.error.no.value.supplied"
      val errorDecimalPlaces = "calc.costsAtLegislationStart.errorDecimalPlaces"

      def errorMaximum(value: String): String = "calc.common.error.maxAmountExceeded"
    }
  }

  object Resident extends Common {

    val homeText = "Calculate your Capital Gains Tax"
    val errorInvalidDate = "Enter a real date"

    object Properties {

      object WorthWhenSoldForLess {
        val question = "What was the property worth when you sold it?"
        val paragraphText = "You can use a valuation from a surveyor or a property website."
      }

      object OwnerBeforeLegislationStart {
        val title = "Did you become the property owner before 1 April 1982?"
        val errorSelectAnOption = "Tell us if you became the property owner before 1 April 1982"
      }

      object PropertiesWorthWhenGaveAway {
        val title = "What was the property worth when you gave it away?"
        val helpMessage = "You can use a valuation from a surveyor or a property website."
      }

      object ValueBeforeLegislationStart {
        val question = "What was the property worth on 31 March 1982?"
      }

      object WorthWhenInherited {
        val question = "What was the property worth when you inherited it?"
        val additionalContent = "You can use a valuation from a surveyor or a property website."
      }

      object WorthWhenGifted {
        val question = "What was the property worth when you got it as a gift?"
        val additionalContent = "You can use a valuation from a surveyor or a property website."
      }

      object WorthWhenBoughtForLess {
        val question = "What was the property worth when you bought it?"
        val additionalContent = "You can use a valuation from a surveyor or a property website."
      }

      object ImprovementsView {
        val question = "How much have you spent on improvements since you became the property owner?"
        val label = "How much have you spent on improvements since you became the property owner?"
        val questionBefore = "How much have you spent on improvements since 31 March 1982?"
        val hint = "Improvements are permanent changes that raise the value of a property, like adding " +
          "extensions or garages. Normal maintenance costs don't count."
        val improvementsHelpButton = "Show me an example"
        val improvementsAdditionalContentOne = "Replacing a basic kitchen or bathroom with a luxury version is normally considered an improvement."
        val improvementsAdditionalContentTwo = "But replacing them with something of a similar standard is normally not an improvement."
      }

      object SellForLess {
        val title = "Did you sell the property for less than it was worth to help the buyer?"
      }
    }

    object Shares {

      //This object will have some duplication of text from the properties summary as well as duplicating
      //some of the questions for the shares pages however it will still pull form the same messages location
      //this is to encourage making the changes in the tests first in both places and understanding what changing
      //the message will affect.
      object SharesSummaryMessages {

        val disposalDateQuestion = "When did you sell or give away the shares?"
        val disposalValueQuestion = "How much did you sell the shares for?"
        val disposalCostsQuestion = "How much did you pay in costs when you sold the shares?"
        val acquisitionValueQuestion = "How much did you pay for the shares?"
        val acquisitionCostsQuestion = "How much did you pay in costs when you got the shares?"

      }

      object ValueBeforeLegislationStart {
        val question = "What were the shares worth on 31 March 1982?"
      }

      object DisposalValue {
        val question = "How much did you sell the shares for?"
      }

      //############ Owner Before Legislation Start messages #################//
      object OwnerBeforeLegislationStart {
        val title = "Did you own the shares before 1 April 1982?"
        val errorNoSelect = "Tell us if you owned the shares before 1 April 1982"
      }

      object DidYouInheritThem {
        val question = "Did you inherit the shares?"
        val errorSelect = "Tell us if you inherited the shares"
      }

      //############ Sell For Less messages #################//
      object SellForLess {
        val title = "Did you sell the shares for less than they were worth to help the buyer?"
        val errorSelect = s"Tell us if you sold the shares for less than they were worth to help the buyer."
      }

      //############ Worth When Inherited messages #################//
      object WorthWhenInherited {
        val question = "What were the shares worth when you inherited them?"
      }

      //############ Worth When Sold For Less messages #################//
      object WorthWhenSoldForLess {
        val question = "What were the shares worth when you sold them?"
      }
    }
  }


  //########################################################################################

  object IntroductionView {
    val title = "Work out how much Capital Gains Tax you owe"
    val subheading = "Do you need to use this calculator?"
    val paragraph = "You probably don't need to pay Capital Gains Tax if the property you've sold is your own home. You'll be entitled to a tax relief called Private Residence Relief."
    val entitledLinkText = "Find out if you're entitled to Private Residence Relief (opens in a new window)."
    val continuationInstructions = "Continue to use this calculator if you've never lived at the property, or you're entitled to only some or no Private Residence Relief."
  }

  //Disposal Date messages
  object DisposalDate {
    val title = "When did you sell or give away the property?"
    val question = "When did you sell or give away the property?"
    val helpText = "For example, 4 9 2016"
    val day = "Day"
    val month = "Month"
    val year = "Year"
    val invalidDayError = "Enter a day"
    val invalidMonthError = "Enter a month"
    val invalidYearError = "Enter a year"
    val realDateError = "Enter a real date"
    val invalidYearRange = "Enter a date in the correct format e.g. 9 12 2015"
  }

  //Outside Tax Years messages
  object OutsideTaxYears {
    val title = "The date you've entered isn't supported by this calculator"
    def content(year: String): String = s"You can continue to use it, but we'll use the tax rates from the $year tax year."
  }

  //No Tax To Pay messages
  object NoTaxToPay {
    val title = "You have no tax to pay"
    val spouseText = "This is because Capital Gains Tax doesn't apply if you give a property to your spouse or civil partner."
    val charityText = "This is because Capital Gains Tax doesn't apply if you give a property to a charity."
    val returnToGov = "Return to GOV.UK"
  }

  //############ Sell For Less messages #################//
  object SellForLess {
    val title = "Did you sell the property for less than it was worth to help the buyer?"
  }

  //############ Worth When Inherited messages #################//
  object WorthWhenInherited {
    val title = "What was the property worth when you inherited it?"
    val additionalContent = "You can use a valuation from a surveyor or a property website."
  }

  //############ Worth When Gifted messages #################//
  object WorthWhenGifted {
    val question = "What was the property worth when you got it as a gift?"
    val additionalContent = "You can use a valuation from a surveyor or a property website."
  }

  //############ Worth When Bought messages #################//
  object WorthWhenBought {
    val question = "What was the property worth when you bought it?"
    val additionalContent = "You can use a valuation from a surveyor or a property website."
  }

  //Disposal Value messages
  object DisposalValue {
    val question = "How much did you sell the property for?"
  }

  //Disposal Costs messages
  object DisposalCosts {
    val title = "How much did you pay in costs when you stopped owning the property?"
    val pageHeading = "How much did you pay in costs when you stopped owning the property?"
    val helpText = "Costs include agent fees, legal fees and surveys"
  }

  //How Became Owner messages
  object HowBecameOwner {
    val title = "How did you become the property owner?"
    val errorMandatory = "Tell us how you became the property owner"
    val bought = "Bought it"
    val gifted = "Got it as a gift"
    val inherited = "Inherited it"
  }

  //############ Bought For Less Than Worth messages #################//
  object BoughtForLessThanWorth {
    val title = "calc.boughtForLess.question"
  }

  //Acquisition Value messages
  object AcquisitionValue {
    val title = "How much did you pay for the property?"
    val pageHeading = "How much did you pay for the property?"
  }

  //Acquisition Costs messages
  object AcquisitionCosts {
    val title = "How much did you pay in costs when you became the property owner?"
    val pageHeading = "How much did you pay in costs when you became the property owner?"
    val helpText = "Costs include stamp duty, agent fees, legal fees and surveys"
  }

  //Improvements messages


  //Summary messages
  object SummaryPage {
    val title = "Summary"
    val pageHeading = "Tax owed"
    val secondaryHeading = "You owe"
    val calcDetailsHeading = "Calculation details"
    def calcDetailsHeadingDate(input: String): String = s"Calculation details for $input tax year"
    val aeaHelp = "You can use this to reduce your tax if you sell something else that's covered by Capital Gains Tax in the same tax year."
    val yourAnswersHeading = "Your answers"
    val totalLoss = "Loss"
    val totalGain = "Total gain"
    val deductions = "Deductions"
    val chargeableLoss = "Carried forward loss"
    val chargeableGain = "Taxable gain"
    val taxRate = "Tax rate"
    def noticeWarning(input: String): String = s"These figures are based on the tax rates from the $input tax year"
    val warning = "Warning"
    val whatToDoNextTitle = "What to do next"
    val whatToDoNextText = "You can tell us about this loss so that you might need to pay less tax in the future."
    val whatNextYouCan = "You can "
    val whatNextLink = "tell us about this loss "
    val whatNextText = "so that you might need to pay less tax in the future."
    val whatToDoNextTextTwoShares = "You need to tell HMRC about the shares"
    val whatToDoNextNoLossText = "Find out whether you need to"
    val whatToDoNextNoLossLinkProperties = "tell HMRC about the property"
    val whatToDoNextNoLossLinkShares = "tell HMRC about the shares"
    val whatToDoNextLossRemaining = "so that you might need to pay less tax in the future"
    val whatToDoNextSharesLiabilityMessage = "You can tell HMRC about the shares and pay your tax using our online service"
    val whatToDoNextPropertiesLiabilityMessage = "You can tell HMRC about the property and pay your tax using our online service"
    val whatToDoNextLiabilityAdditionalMessage = "You can use the figures on this page to help you do this."
    def aeaRemaining(taxYear: String): String = s"Capital Gains Tax allowance left for $taxYear"
    val saveAsPdf = "Save as PDF"
    def remainingAllowableLoss(taxYear: String): String = s"Remaining loss from $taxYear tax year"
    def remainingBroughtForwardLoss(taxYear: String): String = s"Remaining loss from tax years before $taxYear"
    val remainingLossHelp = "You can"
    val remainingLossLink = "use this loss"
    val remainingAllowableLossHelp = "to reduce your Capital Gains Tax if you sell something in the same tax year"
    val remainingBroughtForwardLossHelp = "to reduce your Capital Gains Tax in the future"
    val lettingReliefsUsed = "Letting Relief used"
    def deductionsDetailsAllowableLosses(taxYear: String): String = s"Loss from $taxYear tax year"
    val deductionsDetailsCapitalGainsTax = "Capital Gains Tax allowance used"
    def deductionsDetailsLossBeforeYear(taxYear: String): String = s"Loss from tax years before $taxYear"
    def deductionsDetailsAllowableLossesUsed(taxYear: String): String = s"Loss used from $taxYear tax year"
    def deductionsDetailsLossBeforeYearUsed(taxYear: String): String = s"Loss used from tax years before $taxYear"
  }

  //Private Residence Relief Value messages
  object PrivateResidenceReliefValue {
    val title = "How much Private Residence Relief are you entitled to?"
    val question = title
    val link = "Find out how much you're entitled to"
    def help(value: String): String = s"We've calculated that you've made a gain of £$value on your property. " +
      s"You'll need this figure to calculate your Private Residence Relief."
    def error(value: String): String = s"Enter an amount that is less than your gain of £$value"
  }

  //Reliefs messages
  object Reliefs {
    val title = "Do you want to claim any other tax reliefs?"
    val questionSummary = "Do you want to claim any other tax reliefs?"
    val question = s"Do you want to claim any other tax reliefs?"
    val help = "For example, lettings relief"
    val helpOne = "Capital Gains Tax reliefs can lower the amount of tax you owe. For example, you might be able to claim"
    val helpLinkOne = "Private Residence Relief"
    val errorSelect = s"Tell us if you want to claim any other tax reliefs"
    def errorSelectNoPrr(value: String) = s"Tell us if you want to claim any Capital Gains Tax reliefs on your total gain of £$value"
    val titleNoPrr = "Do you want to claim any Capital Gains Tax reliefs on your total gain of £10,000?"
    val questionSummaryNoPrr = "Do you want to claim any Capital Gains Tax reliefs on your total gain of £50,000?"
    def questionNoPrr(input: String = "100") = s"Do you want to claim any Capital Gains Tax reliefs on your total gain of £$input?"
    val helpButton = "What are Capital Gains Tax reliefs?"
    val helpNoPrr = "For example, Private Residence Relief"
  }

  //Reliefs Value messages
  object ReliefsValue {
    def title(input: String): String = s"How much tax relief are you claiming on your total gain of £$input?"
    def question(input: String): String = s"How much tax relief are you claiming on your total gain of £$input?"
    val prrLink = "Private Residence Relief"
    val lettingsReliefLink = "Lettings Relief"
  }

  //Lettings Relief Value messages
  object LettingsReliefValue {
    val title = s"How much Letting Relief are you entitled to?"
    val question = s"How much Letting Relief are you entitled to?"
    def additionalContent(input: String): String = s"We've calculated that you've made a gain of £$input on your property. " +
      s"You'll need this figure to calculate your Letting Relief."
    val maxLettingsReliefExceeded = "The Letting Relief you've entered is more than the maximum amount of £" + MoneyPounds(Constants.maxLettingsRelief,0).quantity
    val lettingsReliefMoreThanPRR = "The Letting Relief amount you've entered is more than your Private Residence Relief"
    def lettingsReliefMoreThanRemainingGain(input: BigDecimal): String = s"The Letting Relief you've entered is more than your remaining gain of £" + MoneyPounds(input,0).quantity
    val reducYourLettingsRelief = "Reduce your Letting Relief amount"
  }

  //No Prr Reliefs Value messages
  object ReliefsValueNoPrr {
    val title = "How much Capital Gains Tax relief are you claiming?"
    val question = "How much Capital Gains Tax relief are you claiming?"
    val prrLink = "Private Residence Relief"
    val lettingsReliefLink = "Lettings Relief"
  }

  //Lettings Relief messages
  object LettingsRelief {
    val title = "Are you entitled to Letting Relief?"
    val help = "You may be able entitled to Letting Relief if you've rented out the property. Find out more about Letting Relief (opens in a new window)"
    val helpOne = "Letting Relief (opens in a new window)"
    val helpLink = "https://www.gov.uk/government/publications/private-residence-relief-hs283-self-assessment-helpsheet/hs283-private-residence-relief-2016#letting-relief"
    val errorSelect = "Tell us if you want to claim Letting Relief"
  }

  //Other Properties messages
  object OtherProperties {
    def title(input: String): String = s"In the $input tax year, did you sell or give away anything else that's covered by Capital Gains Tax?"
    def pageHeading(input: String): String = s"In the $input tax year, did you sell or give away anything else that's covered by Capital Gains Tax?"
    val help = "This includes things like:"
    val helpOne = "shares"
    val helpTwo = "antiques"
    val helpThree = "other UK residential properties"
    def errorSelect(input: String): String = s"Tell us if you sold or gave away anything else that's covered by Capital Gains Tax in the $input tax year"
  }

  //Allowable Losses Value messages
  object AllowableLossesValue {
    def title(input: String): String = s"What's the total value of your Capital Gains Tax losses from the $input tax year?"
    def question(input: String): String = s"What's the total value of your Capital Gains Tax losses from the $input tax year?"
  }

  //Losses Brought Forward messages
  object LossesBroughtForward {
    def title(input: String): String = s"Are you claiming any Capital Gains Tax losses from tax years before $input?"
    def question(input: String): String = s"Are you claiming any Capital Gains Tax losses from tax years before $input?"
    val helpInfoTitle = "What are Capital Gains Tax losses?"
    val helpInfoSubtitle = "They're losses you've made that:"
    val helpInfoPoint1 = "are covered by Capital Gains Tax"
    val helpInfoPoint2 = "you've declared within 4 years of making the loss"
    val helpInfoPoint3 = "you haven't already used to reduce the amount of Capital Gains Tax you had to pay"
    def errorSelect(input: String): String = s"Tell us if you're claiming any Capital Gains Tax losses from tax years before $input"
  }

  //Allowable Losses messages
  object AllowableLosses {
    def title(input: String): String = s"Are you claiming any Capital Gains Tax losses from the $input tax year?"
    val helpInfoTitle = "What are Capital Gains Tax losses?"
    val helpInfoSubtitle = "They're losses you've made that:"
    val helpInfoPoint1 = "are covered by Capital Gains Tax"
    val helpInfoPoint2 = "you've declared within 4 years of making the loss"
    val helpInfoPoint3 = "you haven't already used to reduce the amount of Capital Gains Tax you had to pay"
    def errorSelect(input: String): String = s"Tell us if you're claiming any Capital Gains Tax losses from the $input tax year"
  }

  //Losses Brought Forward Value messages
  object LossesBroughtForwardValue {
    def title(input: String): String = s"What's the total value of your Capital Gains Tax losses from tax years before $input?"
    def question(input: String): String = s"What's the total value of your Capital Gains Tax losses from tax years before $input?"
  }

  //Annual Exempt Amount messages
  object AnnualExemptAmount {
    val title = "calc.annualExemptAmount.question"
    val question = "calc.annualExemptAmount.question"
    val help = "This is the amount you can make in capital gains before you have to pay tax."
    val helpOne = "It's £11,100 a year."
    val helpLinkOne = "Tax-free allowances for Capital Gains Tax"
  }

  //Previous Taxable Gains messages
  object PreviousTaxableGains {
    def title(year: String): String = s"What was your taxable gain in the $year tax year?"
    def question(year: String): String = s"What was your taxable gain in the $year tax year?"
    val helpLinkOne = "How to work out your taxable gain"
  }

  //Current Income messages
  object CurrentIncome {
    def title(input: String): String = s"In the $input tax year, what was your income?"
    def question(input: String): String = s"In the $input tax year, what was your income?"
    val currentYearTitle = "How much do you expect your income to be in this tax year?"
    val currentYearQuestion = "How much do you expect your income to be in this tax year?"
    val helpText = "Include your salary before tax, and anything else you pay income tax on, but not the money you made from selling the property."
    val helpTextShares = "Include your salary before tax, and anything else you pay income tax on, but not the money you made from selling the shares."
    val linkText = "Income tax"
  }

  //Personal Allowance messages
  object PersonalAllowance {
    def question(input: String): String = s"In the $input tax year, what was your Personal Allowance?"
    val inYearQuestion = "How much is your Personal Allowance?"
    def help(input: String): String = s"This is the amount of your income you don't pay tax on. It was £$input unless you were claiming other allowances."
    def inYearHelp(input: String): String = s"This is the amount of your income you don't pay tax on. It's £$input unless you're claiming other allowances."
    val helpLinkOne = "Personal Allowance"
  }

  //############ Private Residence Relief messages #################//
  object PrivateResidenceRelief {
    val title = "Are you entitled to Private Residence Relief?"
    val helpTextOne = "You'll be entitled to Private Residence Relief if you've lived in the property as your main home " +
      "at some point while you owned it. Find out more about"
    val helpTextLink = "Private Residence Relief"
    val errorSelect = "Tell us if you want to claim Private Residence Relief"
  }

  //############ Property Lived In messages #################//
  object PropertyLivedIn {
    val title = "calc.propertyLivedIn.title"
    val errorNoSelect = "calc.propertyLivedIn.noSelectError"
  }

  //############ Shares messages ##############//
  object SharesDisposalDate {
    val title = "When did you sell or give away the shares?"
  }

  object SharesAcquisitionCosts {
    val title = "How much did you pay in costs when you got the shares?"
    val helpText = "Costs include stockbroker fees and Stamp Duty tax"
  }

  object SharesDisposalCosts {
    val title = "How much did you pay in costs when you sold the shares?"
    val helpText = "For example, stockbroker fees"
  }

  object SharesAcquisitionValue {
    val title = "How much did you pay for the shares?"
    val bulletListTitle = "Put the market value of the shares instead if you:"
    val bulletListOne = "inherited them"
    val bulletListTwo = "owned them before 1 April 1982"
  }

  object SharesOtherDisposals {
    val helpOne = "UK residential properties"
    val helpThree = "other shares"
  }

  object PropertiesSellOrGiveAway {
    val title = "Did you sell the property or give it away?"
    val errorMandatory = "Tell us if you sold the property or gave it away"
    val sold = "Sold it"
    val gift = "Gave it away"
  }

  object WhoDidYouGiveItTo {
    val title = "calc.whoDidYouGiveThePropertyTo.title"
    val spouse = "calc.whoDidYouGiveThePropertyTo.spouse"
    val charity = "calc.whoDidYouGiveThePropertyTo.charity"
    val other = "calc.whoDidYouGiveThePropertyTo.other"
    val errormandatory = "calc.whoDidYouGiveThePropertyTo.errormandatory"
  }

  object SummaryPartialMessages {
    val headingTwo: String => String = taxYear => "calc.summaryPartial.cgtToPay"
    val warningHidden: String = "calc.summaryPartial.warning"
    val warningNoticeSummary: String = "calc.summaryPartial.noticeSummary"

    val workingOutSectionHeading = "calc.summaryPartial.workingOutSectionHeading"
    val flatCalculationSummary = "Gain over whole period of ownership method: based on the amount you've gained on the property since you became the owner"
    val timeCalculationSummary = "Straight-line apportionment method: based on the percentage of your total gain made since"
    val timeCalculationSummaryDate = "5 April 2015"
    val rebasedCalculationSummary = "Rebasing method: based on the amount you've gained on the property since"
    val rebasedCalculationSummaryDate = "5 April 2015"
    val yourTotalGain = "calc.summaryPartial.yourTotalGain"
    val yourTotalLoss = "calc.summaryPartial.yourTotalLoss"
    val valueWhenSold = "calc.summaryPartial.valueWhenSold"
    val valueAtTaxStart = "calc.summaryPartial.valueAtTaxStart"
    val valueWhenAcquired = "calc.summaryPartial.valueWhenAcquired"
    val totalCosts = "calc.summaryPartial.totalCosts"
    val totalGain = "calc.summaryPartial.totalGain"
    val totalLoss = "calc.summaryPartial.totalLoss"
    val gainMadeOnProperty = "calc.summaryPartial.gainMadeOnProperty"
    val lossMadeOnProperty = "calc.summaryPartial.lossMadeOnProperty"
    val percentageTotalGain = "calc.summaryPartial.percentageOfGain"
    val percentageTotalLoss = "calc.summaryPartial.percentageOfLoss"

    val deductionsSectionHeading = "calc.summaryPartial.deductionsSectionHeading"
    val reliefsUsed = "calc.summaryPartial.reliefsUsed"
    val inYearLossesUsed = "calc.summaryPartial.inYearLossesUsed"
    val aeaUsed = "calc.summaryPartial.aeaUsed"
    val broughtForwardLossesUsed = "calc.summaryPartial.broughtForwardLossesUsed"
    val totalDeductions = "calc.summaryPartial.totalDeductions"

    val yourTaxableGain = "calc.summaryPartial.yourTaxableGain"
    val gain = "calc.summaryPartial.totalGain"
    val minusDeductions = "calc.summaryPartial.minusDeductions"
    val taxableGain = "calc.summaryPartial.taxableGain"

    val yourTaxRate = "calc.summaryPartial.yourTaxRate"
    val incomeBandInfo = "calc.summaryPartial.incomeBandInfo"

    def taxRate(taxAmount: String, taxRate: Int): String = "calc.summaryPartial.taxRate"

    val taxToPay = "calc.summaryPartial.taxToPay"

    val remainingDeductions = "calc.summaryPartial.remainingDeductions"

    def inYearLossesRemaining(taxYear: String): String = "calc.summaryPartial.inYearLossesRemaining"

    def aeaRemaining(taxYear: String): String = "calc.summaryPartial.aeaRemaining"

    val broughtForwardLossesRemaining = "calc.summaryPartial.broughtForwardLossesRemaining"
  }


  object WhatNext extends Common {
    val title = "calc.whatNext.title"
    val listTitle = "calc.whatNext.listTitle"
    val listOne = "calc.whatNext.listOne"
    val listTwo = "calc.whatNext.listTwo"
    val penaltyWarning = "calc.whatNext.penaltyWarning"
    val saHeader = "calc.whatNext.saHeader"
    val saText = "calc.whatNext.saText"
    val report = "calc.whatNext.report"
    val finish = "calc.whatNext.finish"
  }
}
