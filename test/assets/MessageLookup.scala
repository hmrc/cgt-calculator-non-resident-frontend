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

package assets

import uk.gov.hmrc.play.views.helpers.MoneyPounds
import common.Constants

object MessageLookup {

  // TO MOVE

  trait Common {

    val externalLink = "(opens in a new window)"
    val change = "change"
    val back = "Back"
    val continue = "Continue"
    val yes = "Yes"
    val no = "No"
    val day = "Day"
    val month = "Month"
    val year = "Year"

    val readMore = "Read more"

    val mandatoryAmount = "Enter an amount"
    val minimumAmount = "Enter an amount that's £0 or more"
    val maximumAmount = "Enter an amount that's £1,000,000,000 or less"
    val errorRequired = "This field is required"

    def maximumLimit(limit: String): String = s"Enter an amount that's £$limit or less"

    val invalidAmount = "Enter an amount in the correct format e.g. 10000.00"
    val invalidAmountNoDecimal = "Enter an amount in the correct format e.g. 10000"
    val numericPlayErrorOverride = "Enter a number without commas, for example 10000.00"
    val optionReqError = "Choose one of the options"

    val whatToDoNextTextTwo = "You need to tell HMRC about the property"
    val whatToDoNextFurtherDetails = "Further details on how to tell HMRC about this property can be found at"
  }

  object NonResident extends Common {

    val pageHeading = "Calculate your Non-Resident Capital Gains Tax"
    val errorInvalidDate = "Enter a real date"
    val errorRealNumber = "Enter a number without commas, for example 10000.00"

    object AcquisitionCosts {
      val question = "How much did you pay in costs when you became the property owner?"
      val helpText = "Costs include agent fees, legal fees and surveys"
      val errorNegative = "Enter a positive number for your costs"
      val errorDecimalPlaces = "There are too many numbers after the decimal point in your costs"
      val bulletTitle = "This is what you paid for:"
      val bulletOne = "estate agents or auctioneers"
      val bulletTwo = "solicitors or conveyancers, including Stamp Duty Land Tax"
      val bulletThree = "any professional help to value the property, eg surveyor or valuer (this includes any valuations at 31 March 1982)"
      val hint ="If you owned the property with someone else, only enter your share of the costs, as agreed with your co-owner."

      def errorMaximum(value: String): String = s"Enter an amount that's £$value or less"
    }

    object AcquisitionDate {
      val question = "What date did you sign the contract to become the owner?"
      val questionTwo = "What was the date you signed the contract that made you the owner?"
      val hintText = "If you inherited the property, it is the date that the previous owner died."
      val errorIncompleteDate = "Date is not valid"
      val errorFutureDate = "Date must be in the past"
      val errorFutureDateGuidance = "Enter a date from the past"
    }

    object AcquisitionValue {
      val question = "How much did you pay for the property?"
      val helpText = "If you owned the property with someone else, only enter your share of the purchase."
      val bulletTitle = "Put the market value of the property instead if you:"
      val bulletOne = "inherited it"
      val bulletTwo = "got it as a gift"
      val bulletThree = "bought it from a relative, business partner or someone else you're connected to"
      val bulletFour = "bought it for less than it's worth because the seller wanted to help you"
      val bulletFive = "became the owner before 1 April 1982"
      val bulletLink = "someone else you're connected to"
      val errorNegative = "Enter a positive number for the amount you paid"
      val errorDecimalPlaces = "The amount you paid has too many numbers after the decimal point"

      def errorMaximum(value: String): String = s"Enter an amount that's £$value or less"
    }

    object AnnualExemptAmount {
      val question = "How much of your Capital Gains Tax allowance have you got left?"
      val link = "Capital Gains Tax allowances"
      def hint(amount: String): String = s"Your Capital Gains Tax allowance is £$amount a year. It's the amount of capital gains you can make before you have to pay tax."
      val errorMaxStart = "Enter a Capital Gains Tax allowance that's £"
      val errorMaxEnd = "or less"
      val errorNegative = "Enter a positive number for your allowance"
      val errorDecimalPlaces = "Your allowance has too many numbers after the decimal point"
    }

    object BoughtForLess {
      val question = "Did you buy the property for less than it was worth because the seller wanted to help you?"
    }

    object CalculationElection {
      val heading = "Working out your total"
      val question = "Which method of calculation would you like?"
      val moreInformation = "How this affects your tax"
      val moreInfoFirstP = "We can work out your tax in a number of ways. Before we give you a final total you have a choice about what to base it on."
      val moreInfoSecondP = "Because the options take different things into account they'll usually give you different totals. " +
        "Some totals may be much larger than others."
      val moreInfoThirdP = "Unless you want to add more tax reliefs, the total you choose is the amount of tax you'll pay."
      val legend = "Tax you'll owe"
      val basedOn = "Based on"
      val linkOne = "Different ways of working out Capital Gains Tax"
      val otherTaxRelief = "Add other tax relief"
      val someOtherTaxRelief = "Other tax relief"
      val timeApportioned = "Working out your total gain, then taxing you on the percentage of it you've made since"
      val rebased = "How much you've gained on the property since"
      val taxStartDate = "5 April 2015"
      val flat = "How much you've gained on the property since you became the owner"
      val whyMore = "Why you might choose a higher total"
      val whyMoreDetailsOne = "You might pick a higher total if you're planning to add more tax reliefs."
      val whyMoreDetailsTwo = "Higher totals sometimes qualify for larger reliefs, which can reduce the amount you owe."
    }

    object CheckYourAnswers {
      val question = "Check your answers"
      val tableHeading = "You've told us"
      val change = "Change"
      val hiddenText = "your response to the question"
    }

    object CurrentIncome {
      val question = "What was your total UK income in the tax year when you stopped owning the property?"
      val linkOne = "Income Tax"
      val linkTwo = "Previous tax years"
      val helpText = "Give a realistic estimate if this was in the current tax year. Include your UK salary before tax, and anything else you pay UK income tax on. Do not include the money you made from selling the property."
      val errorNegative = "Enter a positive number for your current income"
      val errorDecimalPlace = "Your current income has too many numbers after the decimal point"

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

      val question = "How much did you pay in costs when you stopped owning the property?"

      val helpTitle = "This is what you paid for:"
      val helpBulletOne = "estate agents or auctioneers"
      val helpBulletTwo = "solicitors or conveyancers"
      val helpBulletThree = "any professional help to value your property, eg surveyor or valuer"
      val helpBulletFour = "advertising to find a buyer"

      val errorNegativeNumber = "Enter a positive number for your selling costs"
      val errorDecimalPlaces = "There are too many numbers after the decimal point in your selling costs"

      val jointOwnership = "If you owned the property with someone else, only enter your share of the costs, as agreed with your co-owner."
    }

    object DisposalDate {

      val question = "When did you sign the contract that made someone else the owner?"
      val errorDateAfter = "This can't be before the date you became the owner"

    }

    object SoldForLess {
      val question = "Did you sell the property for less than it was worth to help the buyer?"
    }

    object DisposalValue {

      val question = "How much did you sell the property for?"
      val errorNegativeNumber = "Enter a positive number for the amount you sold the property for"
      val errorDecimalPlaces = "The amount you sold the property for has too many numbers after the decimal point"
      val errorNegative = "Enter a positive number for the amount you sold the property for"
      val bulletIntro = "Put the market value of the property instead if you:"
      val bulletOne = "gave it away as a gift"
      val bulletTwo = "sold it to a relative, business partner or"
      val bulletTwoLink = "someone else you're connected to"
      val bulletThree = "sold it for less than it's worth to help the buyer"
      val jointOwnership = "If you owned the property with someone else, only enter your share of the sale value"
      def errorMaximum(value: String): String = s"Enter an amount that's £$value or less"
    }

    object HowMuchGain {

      val question = "What was your taxable gain?"
      val errorNegativeNumber = "Enter a positive number for the amount on your taxable gain"
      val errorDecimalPlaces = "Taxable Gain for has too many numbers after the decimal point"
      }

    object Improvements extends Common {
      val helpOne = "Improvements are permanent changes that increase the value of a property, like adding extensions or garages."
      val helpTwo = "Normal maintenance costs don't count."
      val exampleTitle = "Show me an example"
      val exampleOne = "Replacing a basic kitchen or bathroom with a luxury version is normally considered an improvement."
      val exampleTwo = "Replacing them with something of a similar standard is normally not an improvement."
      val question = "Did you make any improvements to the property?"
      val ownerBeforeLegislationStartQuestion = "Did you make an improvement to the property after 31 March 1982?"
      val questionTwo = "How much did the improvements cost?"
      val questionThree = "How much did you spend on improvements before 6 April 2015?"
      val questionFour = "How much have you spent on improvements since 6 April 2015?"
      val noValueSuppliedError = "Enter the value of your improvements"
      val negativeValueError = "Enter a positive number for the cost of your improvements"
      val excessDecimalPlacesError = "The cost of your improvements has too many numbers after the decimal point"
      val jointOwnership = "If you owned the property with someone else, only enter your share of the improvement costs, as agreed with your co-owner."

      def errorMaximum(value: String): String = s"Enter an amount that's £$value or less"

    }

    object NoCapitalGainsTax {

      val title = "You have no tax to pay"
      val paragraphOne = "Capital Gains Tax for non-UK residents only applies to UK residential properties that were sold or given away after 5 April 2015."
      val paragraphTwo = "You've told us that you sold or gave away the property on"
      val changeLink = "Change this date"
      val returnLink = "Return to GOV.UK"
    }

    object OtherProperties {
      val question = "Did you sell or give away other UK residential properties in the tax year when you stopped owning the property?"
      val questionTwo = "What was your taxable gain?"
      val questionTwoHelpTextStart = "How to"
      val questionTwoHelpTextLinkText = "work out your total taxable gains"
      val errorNegative = "Enter a positive number for your taxable gain"
      val errorDecimalPlaces = "Your taxable gain has too many numbers after the decimal point"
      val errorQuestion = "Enter a value for your taxable gain"
      val linkOne = "Capital Gains Tax"
      val linkTwo = "Previous tax years"
    }

    object PreviousLossOrGain {
      val question = "Did these disposals result in an overall Capital Gains Tax loss or gain?"
      val mandatoryCheck = "Please tell us whether you made a gain or loss"
      val loss = "Loss"
      val gain = "Gain"
      val neither = "Neither ('nil gain')"
      val CGTlink = "Capital Gains Tax"
      val previousTaxLink = "Previous tax years"
      val hintOne = "This is the combined loss or gain on these properties after accounting for costs, reliefs and any Capital Gains allowance (Annual Exempt Amount) you've used."
      val hintTwo = "You should already have reported these properties to HMRC. Work out if you've made a loss or gain by adding up the final figures from these reports."
    }

    object OtherReliefs {
      val help = "For example, lettings relief."
      val question = "How much extra tax relief are you claiming?"
      val totalGain = "Total gain"
      val taxableGain = "Taxable gain"
      val lossCarriedForward = "Loss carried forward"
      val addRelief = "Add relief"
      val updateRelief = "Update relief"
      val errorDecimal = "There are too many numbers after the decimal point in your other reliefs"
      val errorNegative = "Enter a positive number for your other reliefs"
      val helpTwo = "Don't include any Private Residence Relief."
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
      val disposalGaveAwayQuestion = "What was the property worth when you gave it away?"
      val disposalSoldQuestion = "What was the market value of the property when you sold it?"

      val disposalHelpText = "You can use a valuation from a surveyor. " +
        "If you don't know the exact value, you must provide a realistic estimate."
      val disposalHelpTextAdditional = "You might have to pay more if we think your estimate is unrealistic."

      val disposalErrorDecimalPlacesGaveAway = "There are too many numbers after the decimal point in your market value" +
        " at the point of giving away"
      val disposalErrorDecimalPlacesSold = "There are too many numbers after the decimal point in your market value" +
        " at the point of being sold"

      val errorNegativeGaveAway = "Enter a positive number for the market value at the point of being given away"
      val errorNegativeSold = "Enter a positive number for the market value at the point of being sold"

      val jointOwnership = "If you owned the property with someone else, only enter your share of the property value."
    }

    object PersonalAllowance {
      val question = "What was your UK Personal Allowance in the tax year when you stopped owning the property?"
      val link = "Personal Allowances"
      val help = "This the amount of your income that you don’t pay tax on. Find out more about"
      val errorNegative = "Enter a positive number for your Personal Allowance"
      val errorDecimalPlaces = "Enter a whole number for your Personal Allowance"
      val errorMaxLimit = "Enter a Personal Allowance that's £"
      val errorMaxLimitEnd = "or less"
    }

    object PrivateResidenceRelief {
      val question = "Are you claiming Private Residence Relief?"
      val helpLink = "Private Residence Relief"
      val questionBefore = "How many days before"
      val questionEnd = "are you claiming relief for?"
      val questionBetween = "How many days between 5 April 2015 and"
      val errorNoValue = "Enter the value for your days claimed"
      val errorNegative = "Enter a positive number for your days claimed"
      val errorDecimalPlaces = "There are too many numbers after the decimal point in your days claimed"

      def errorMaximum(value: String): String = s"Enter a value for your days claimed that's $value or less"
    }

    object RebasedCosts {
      val question = "Did you pay to have the property valued at 5 April 2015?"
      val inputQuestion = "How much did it cost to get the property valued?"
      val jointOwnership = "If you owned the property with someone else, only enter your share of the cost, as agreed with your co-owner."
      val errorNegative = "Enter a positive number for your costs"
      val errorNoValue = "Enter the value for your costs"
      val errorDecimalPlaces = "There are too many numbers after the decimal point in your costs"

      def errorMaximum(value: String): String = s"Enter an amount that's £$value or less"
    }

    object RebasedValue {
      val question = "What was the market value of the property on 5 April 2015?"

      val questionOptionalText = "Only tell us if you owned the property on that date"

      val inputHintText = "If you don't know the exact value, you must provide a realistic estimate. " +
        "You might have to pay more if we think your estimate is unrealistic."
      val jointOwnership = "If you owned the property with someone else, only enter your share of the property value."
      val additionalContentTitle = "Why we're asking for this"
      val helpHiddenContent = "If you're not a UK resident, you only have to report UK property you've sold or given away since 5 April 2015."

      val errorNoValue = "Enter a value for your property on 5 April 2015"
      val errorNegative = "Enter a positive value for your property on 5 April 2015"
      val errorDecimalPlaces = "The value for your property on 5 April 2015 has too many numbers after the decimal point"


      def errorMaximum(value: String): String = s"Enter an amount that's £$value or less"
    }

    object HowBecameOwner {
      val question = "How did you become the owner?"
      val errorMandatory = "Tell us how you became the owner"
      val bought = "Bought it"
      val gifted = "Got it as a gift"
      val inherited = "Inherited it"
    }

    object SoldOrGivenAway {
      val question = "Did you sell or give away the property?"
      val sold = "I sold it"
      val gave = "I gave it away"
    }

    //Acquisition Market Value messages
    object AcquisitionMarketValue {
      val errorNegativeNumber = "Enter a positive number for the market value of the property"
      val errorDecimalPlaces = "The market value of the property has too many numbers after the decimal point"
      val hintOne = "You can use a valuation from a surveyor."
      val hintTwo = "If you don't know the exact value, you must provide a realistic estimate. " +
        "You might have to pay more if we think your estimate is unrealistic."
    }

    object WorthBeforeLegislationStart {
      val question = "What was the market value of the property on 31 March 1982?"
      val information = "31 March 1982 is when the current Capital Gains Tax system came into effect."
      val hintText = "If you don't know the exact value, you must provide a realistic estimate. " +
        "You might have to pay more if we think your estimate is unrealistic."
      val jointOwnership = "If you owned the property with someone else, only enter your share of the property value."
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
      val question = "How much loss did you report?"
      val errorNegative = "Enter a positive number for your loss"
      val errorDecimalPlaces = "There are too many numbers after the decimal point in your loss"

      def errorMaximum(value: String): String = s"Enter an amount that's £$value or less"
    }

    object BroughtForwardLosses {
      val question = "Do you have losses you want to bring forward from previous tax years?"
      val inputQuestion = "How much would you like to bring forward?"
      val helpText = "These are unused losses that are covered by Capital Gains Tax and that you've already reported to HMRC."
      val linkOne = "Capital Gains Tax"
      val linkTwo = "Previous tax years"
      val errorDecimalPlaces = "There are too many numbers after the decimal point in your brought forward loss"
      val errorNegative = "Enter a positive number for your brought forward loss"

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
      val title = "Are you entitled to any other reliefs?"
      val helpText = "For example, Letting Relief (if you already claimed Private Residence Relief) or Rollover Relief."
      val errorMandatory = "Tell us if you are entitled to any other reliefs"
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
    val tooEarly = "You can use this calculator if you've sold a property since 5 April 2015."
    val sharesTooEarly = "You can use this calculator if you've sold shares since 5 April 2015."
    val changeDate = "Change your date"
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
    val title = "Did you buy the property for less than it was worth because the seller wanted to help you?"
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
    val title = "How much of your Capital Gains Tax allowance have you got left?"
    val question = "How much of your Capital Gains Tax allowance have you got left?"
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
    val title = "Have you ever lived in the property since you became the owner?"
    val errorNoSelect = "Tell us if you have ever lived in the property since you became the owner"
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
    val title = "Who did you give the property to?"
    val spouse = "Your spouse or a civil partner"
    val charity = "A charity"
    val other = "Someone else"
    val errormandatory = "Please tell us who you gave the property to"
  }

  object SummaryPartialMessages {
    val headingTwo: String => String = taxYear => s"Capital Gains Tax to pay for $taxYear tax year"
    val warningHidden: String = "Warning"
    val warningNoticeSummary: String = "Your result may be inaccurate because the calculator does not support the date of sale you entered. " +
      "Do not use these figures to report your Capital Gains Tax."

    val workingOutSectionHeading = "How we've worked this out"
    val flatCalculationSummary = "How much you've gained on the property since you became the owner"
    val timeCalculationSummary = "Working out your total gain, then taxing you on the percentage of it you've made since"
    val timeCalculationSummaryDate = "5 April 2015"
    val rebasedCalculationSummary = "How much you've gained on the property since"
    val rebasedCalculationSummaryDate = "5 April 2015"
    val yourTotalGain = "Your total gain"
    val yourTotalLoss = "Your total loss"
    val valueWhenSold = "Value when you sold the property"
    val valueAtTaxStart = "Minus the value of the property on 5 April 2015"
    val valueWhenAcquired = "Minus the value of the property when you acquired it"
    val totalCosts = "Minus all costs, including improvements"
    val totalGain = "Total gain"
    val totalLoss = "Total loss"
    val gainMadeOnProperty = "The gain you've made on the property"
    val percentageTotalGain = "The percentage gain you've made since 5 April 2015"

    val deductionsSectionHeading = "Your deductions"
    val reliefsUsed = "Reliefs used"
    val inYearLossesUsed = "In year losses used"
    val aeaUsed = "Capital Gains Tax Annual Exempt Amount used"
    val broughtForwardLossesUsed = "Brought forward losses used"
    val totalDeductions = "Total deductions"

    val yourTaxableGain = "Your taxable gain"
    val gain = "Total gain"
    val minusDeductions = "Minus deductions"
    val taxableGain = "Taxable gain"

    val yourTaxRate = "Your tax rate"
    val incomeBandInfo = "These tax rates are based on your Income Tax bands:"

    def taxRate(taxAmount: String, taxRate: Int): String = s"$taxAmount taxable gain multiplied by $taxRate% tax rate"

    val taxToPay = "Tax to pay"

    val remainingDeductions = "Your remaining deductions"

    def inYearLossesRemaining(taxYear: String): String = s"In year losses left for the $taxYear tax year"

    def aeaRemaining(taxYear: String): String = s"Annual Exempt Amount left for the $taxYear tax year"

    val broughtForwardLossesRemaining = "Losses to carry forward from previous tax years"
  }


  object WhatNext extends Common {
    val title = "Report your property now"
    val listTitle = "You have 30 days from the date you completed the transfer of your property (the 'conveyance date') to:"
    val listOne = "tell us about the property you've sold or gave away"
    val listTwo = "pay any Capital Gains Tax"
    val penaltyWarning = "Use our online form to report now. You'll have to pay a penalty if you miss the reporting deadline."
    val saHeader = "What if I'm registered for Self Assessment?"
    val saText = "You must report basic information now, but you can choose to report in full and pay any tax due when you do your Self Assessment return."
    val report = "Report now"
    val finish = "Return to GOV.UK"
  }
}
