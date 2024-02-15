/*
 * Copyright 2024 HM Revenue & Customs
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

object KeyLookup {

  trait Common {

    val externalLink = "calc.base.externalLink"
    val change = "calc.base.change"
    val back = "calc.base.back"
    val continue = "calc.base.continue"
    val yes = "calc.base.yes"
    val no = "calc.base.no"

    val mandatoryAmount = "calc.common.error.mandatoryAmount"

    val numericPlayErrorOverride = "error.number"
    val optionReqError = "calc.base.optionReqError"

    def maximumError(value: String): String = s"calc.common.error.maxNumericExceeded"
  }

  object NonResident extends Common {

    def errorRequired(formName: String): String = {
      s"calc.$formName.errors.required"
    }

    def fieldErrorRequired(field: String): String = {
      s"calc.$field.error.required"
    }

    val errorRealNumber = "error.real"

    object DisposalDate {
      val errorNotRealDate = "disposalDate.error.notReal"
      val errorNotRealDay = "disposalDate.error.notReal.day"
      val errorNotRealMonth = "disposalDate.error.notReal.month"
      val errorNotRealYear = "disposalDate.error.notReal.year"
      val errorInvalidDate = "disposalDate.error.invalid"
      val errorRequiredDay = "disposalDate.error.required.day"
      val errorRequiredMonth = "disposalDate.error.required.month"
      val errorRequiredYear = "disposalDate.error.required.year"
    }

    object AcquisitionCosts {
      val errorRequired = "calc.acquisitionCosts.error.required"
      val errorNegative = "calc.acquisitionCosts.errorNegative"
      val errorDecimalPlaces = "calc.acquisitionCosts.errorDecimalPlaces"
      val errorTooHigh = "calc.acquisitionCosts.error.tooHigh"
      val errorInvalid = "calc.acquisitionCosts.error.invalid"
    }

    object AcquisitionDate {
      val errorRequiredDay = "acquisitionDate.error.required.day"
      val errorRequiredMonth = "acquisitionDate.error.required.month"
      val errorRequiredYear = "acquisitionDate.error.required.year"
      val errorInvalidDate = "acquisitionDate.error.invalid"
      val errorNotRealDate = "acquisitionDate.error.notReal"
      val errorFutureDateGuidance = "acquisitionDate.error.range.max"
    }

    object AcquisitionValue {
      val errorRequired = "calc.acquisitionValue.error.required"
      val errorReal = "calc.acquisitionValue.errorReal"
      val errorMax = "calc.acquisitionValue.errorMax"
      val errorNegative = "calc.acquisitionValue.errorNegative"
      val errorDecimalPlaces = "calc.acquisitionValue.errorDecimalPlaces"
    }

    object AnnualExemptAmount {
      val errorRequired = "calc.annualExemptAmount.error.required"
      val errorReal = "calc.annualExemptAmount.errorReal"
      val errorMax = "calc.annualExemptAmount.errorMax"
      val errorNegative = "calc.annualExemptAmount.errorNegative"
      val errorDecimalPlaces = "calc.annualExemptAmount.errorDecimalPlaces"
    }

    object CurrentIncome {
      val errorRequired = "calc.currentIncome.error.required"
      val errorReal = "calc.currentIncome.errorReal"
      val errorMax = "calc.currentIncome.errorMax"
      val errorNegative = "calc.currentIncome.errorNegative"
      val errorDecimalPlace = "calc.currentIncome.errorDecimalPlaces"
    }

    object DisposalCosts {
      val errorRequired = "calc.disposalCosts.error.required"
      val errorReal = "calc.disposalCosts.errorReal"
      val errorNegativeNumber = "calc.disposalCosts.errorNegativeNumber"
      val errorDecimalPlaces = "calc.disposalCosts.errorDecimalPlaces"
    }

    object DisposalValue {
      val errorRequired = "calc.disposalValue.error.required"
      val errorReal = "calc.disposalValue.errorReal"
      val errorMax = "calc.disposalValue.errorMax"
      val errorDecimalPlaces = "calc.disposalValue.errorDecimalPlaces"
      val errorNegative = "calc.disposalValue.errorNegative"

    }

    object HowMuchGain {
      val errorRequired = "calc.howMuchGain.error.required"
      val errorReal = "calc.howMuchGain.errorReal"
      val errorNegativeNumber = "calc.howMuchGain.errorNegative"
      val errorDecimalPlaces = "calc.howMuchGain.errorDecimalPlaces"
      }

    object Improvements extends Common {
      val errorRequired = "calc.improvements.error.required"
      val errorReal = "calc.improvements.error.invalid"
      val negativeValueError = "calc.improvements.errorNegative"
      val excessDecimalPlacesError = "calc.improvements.errorDecimalPlaces"
      val tooHigh = "calc.improvements.error.tooHigh"
    }

    object ImprovementsBefore extends Common {
      val errorRequired = "calc.improvements.before.error.required"
      val errorReal = "calc.improvements.before.error.invalid"
      val negativeValueError = "calc.improvements.before.error.tooLow"
      val excessDecimalPlacesError = "calc.improvements.before.error.decimalPlaces"
      val tooHigh = "calc.improvements.before.error.tooHigh"
    }

    object ImprovementsAfter extends Common {
      val errorRequired = "calc.improvements.after.error.required"
      val errorReal = "calc.improvements.after.error.invalid"
      val negativeValueError = "calc.improvements.after.error.tooLow"
      val excessDecimalPlacesError = "calc.improvements.after.error.decimalPlaces"
      val tooHigh = "calc.improvements.after.error.tooHigh"
    }

    object OtherProperties {
      val errorNegative = "calc.howMuchGain.errorNegative"
      val errorDecimalPlaces = "calc.howMuchGain.errorDecimalPlaces"
      val errorQuestion = "Enter a value for your taxable gain"
    }

    object PreviousLossOrGain {
      val mandatoryCheck = "calc.previousLossOrGain.errors.required"
    }

    object OtherReliefs {
      val errorRequired = "calc.otherReliefs.error.required"
      val errorReal = "calc.otherReliefs.errorReal"
      val errorMax = "calc.otherReliefs.errorMax"
      val errorDecimal = "calc.otherReliefs.errorDecimal"
      val errorNegative = "calc.otherReliefs.errorNegative"
    }

    object MarketValue {
      val gaveAwayErrorRequired = "calc.marketValue.error.gaveItAway.required"
      val disposalErrorRealPlacesGaveAway = "calc.marketValue.error.gaveItAway.errorReal"
      val disposalErrorRealPlacesSold = "calc.marketValue.error.sold.errorReal"

      val disposalErrorMaxPlacesGaveAway = "calc.marketValue.error.gaveItAway.errorMax"
      val disposalErrorMaxPlacesSold = "calc.marketValue.error.sold.errorMax"

      val disposalErrorDecimalPlacesGaveAway = "calc.marketValue.error.gaveItAway.decimalPlaces"
      val disposalErrorDecimalPlacesSold = "calc.marketValue.error.sold.decimalPlaces"

      val errorNegativeGaveAway = "calc.marketValue.error.gaveItAway.negative"
      val errorNegativeSold = "calc.marketValue.error.sold.negative"
    }

    object PersonalAllowance {
      val errorRequired = "calc.personalAllowance.error.required"
      val errorReal = "calc.personalAllowance.errorReal"
      val errorNegative = "calc.personalAllowance.errorNegative"
      val errorDecimalPlaces = "calc.personalAllowance.errorDecimalPlaces"
      val errorMax = "calc.personalAllowance.errorMaxLimit"
    }

    object PrivateResidenceRelief {
      val errorRequired = "calc.privateResidenceRelief.error.required"
      val errorNoValue = "calc.privateResidenceRelief.error.noValueProvided"
      val errorNegative = "calc.privateResidenceRelief.error.errorNegative"
      val errorDecimalPlaces = "calc.privateResidenceRelief.error.errorDecimalPlaces"
    }

    object RebasedCosts {
      val errorRequired = "calc.rebasedCosts.error.required"
      val errorNegative = "calc.rebasedCosts.errorNegative"
      val errorTooHigh = "calc.rebasedCosts.error.tooHigh"
      val errorDecimalPlaces = "calc.rebasedCosts.errorDecimalPlaces"
      val errorInvalid = "calc.rebasedCosts.error.invalid"
    }

    object RebasedValue {
      val errorRequired = "calc.nonResident.rebasedValue.error.required"
      val errorInvalid = "calc.nonResident.rebasedValue.error.invalid"
      val errorMax = "calc.nonResident.rebasedValue.errorMax"
      val errorNegative = "calc.nonResident.rebasedValue.errorNegative"
      val errorDecimalPlaces = "calc.nonResident.rebasedValue.errorDecimalPlaces"
    }

    object HowBecameOwner {
      val errorMandatory = "calc.howBecameOwner.errors.required"
    }

    object AcquisitionMarketValue {
      val errorRequired = "calc.acquisitionMarketValue.error.required"
      val errorReal = "calc.acquisitionMarketValue.errorReal"
      val errorMax = "calc.acquisitionMarketValue.errorMax"
      val errorNegativeNumber = "calc.acquisitionMarketValue.errorNegative"
      val errorDecimalPlaces = "calc.acquisitionMarketValue.errorDecimalPlaces"
    }

    object WorthWhenBoughtForLess {
      val errorRequired = "calc.worthWhenBoughtForLess.error.required"
      val errorReal = "calc.worthWhenBoughtForLess.errorReal"
      val errorMax = "calc.worthWhenBoughtForLess.errorMax"
      val errorNegativeNumber = "calc.worthWhenBoughtForLess.errorNegative"
      val errorDecimalPlaces = "calc.worthWhenBoughtForLess.errorDecimalPlaces"
    }

    object WorthWhenInherited {
      val errorRequired = "calc.worthWhenInherited.error.required"
      val errorReal = "calc.worthWhenInherited.errorReal"
      val errorMax = "calc.worthWhenInherited.errorMax"
      val errorNegativeNumber = "calc.worthWhenInherited.errorNegative"
      val errorDecimalPlaces = "calc.worthWhenInherited.errorDecimalPlaces"
    }

    object WorthWhenGiftedTo {
      val errorRequired = "calc.worthWhenGiftedTo.error.required"
      val errorReal = "calc.worthWhenGiftedTo.errorReal"
      val errorMax = "calc.worthWhenGiftedTo.errorMax"
      val errorNegativeNumber = "calc.worthWhenGiftedTo.errorNegative"
      val errorDecimalPlaces = "calc.worthWhenGiftedTo.errorDecimalPlaces"
    }

    object WorthBeforeLegislationStart {
      val errorRequired = "calc.worthBeforeLegislationStart.error.required"
      val errorReal = "calc.worthBeforeLegislationStart.errorReal"
      val errorMax = "calc.worthBeforeLegislationStart.errorMax"
      val errorNegativeNumber = "calc.worthBeforeLegislationStart.errorNegative"
      val errorDecimalPlaces = "calc.worthBeforeLegislationStart.errorDecimalPlaces"
    }

    object HowMuchLoss {
      val errorRequired = "calc.howMuchLoss.error.required"
      val errorReal = "calc.howMuchLoss.errorReal"
      val errorMax = "calc.howMuchLoss.errorMax"
      val errorNegative = "calc.howMuchLoss.errorNegative"
      val errorDecimalPlaces = "calc.howMuchLoss.errorDecimal"
    }

    object BroughtForwardLosses {
      val errorRequired = "calc.broughtForwardLosses.error.required"
      val errorInvalid = "calc.broughtForwardLosses.error.invalid"
      val errorDecimalPlaces = "calc.broughtForwardLosses.errorDecimal"
      val errorNegative = "calc.broughtForwardLosses.errorNegative"
      val errorTooHigh = "calc.broughtForwardLosses.error.tooHigh"
    }

    object ClaimingReliefs {
      val errorMandatory = "calc.claimingReliefs.errorMandatory"
    }

    object CostsAtLegislationStart {
      val errorRequired = "calc.costsAtLegislationStart.error.required"
      val errorInvalid = "calc.costsAtLegislationStart.error.invalid"
      val errorNegative = "calc.costsAtLegislationStart.errorNegative"
      val errorDecimalPlaces = "calc.costsAtLegislationStart.errorDecimalPlaces"
      val errorTooHigh = "calc.costsAtLegislationStart.error.tooHigh"
    }

  }

  object Resident extends Common {
    val errorInvalidDate = "Enter a real date"
  }

  object WhoDidYouGiveItTo {
    val errormandatory = "calc.whoDidYouGiveThePropertyTo.errormandatory"
  }

}
