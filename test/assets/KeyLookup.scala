/*
 * Copyright 2022 HM Revenue & Customs
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

    val errorInvalidDate = "calc.common.date.error.invalidDate"
    val errorInvalidDay = "calc.common.date.invalidDayError"
    val errorInvalidMonth = "calc.common.date.invalidMonthError"
    val errorInvalidYear = "calc.common.date.invalidYearError"
    val errorRealNumber = "error.real"

    object AcquisitionCosts {
      val errorNegative = "calc.acquisitionCosts.errorNegative"
      val errorDecimalPlaces = "calc.acquisitionCosts.errorDecimalPlaces"
    }

    object AcquisitionDate {
      val errorFutureDateGuidance = "calc.acquisitionDate.errorFutureDateGuidance"
    }

    object AcquisitionValue {
      val errorReal = "calc.acquisitionValue.errorReal"
      val errorMax = "calc.acquisitionValue.errorMax"
      val errorNegative = "calc.acquisitionValue.errorNegative"
      val errorDecimalPlaces = "calc.acquisitionValue.errorDecimalPlaces"
    }

    object AnnualExemptAmount {
      val errorReal = "calc.annualExemptAmount.errorReal"
      val errorMax = "calc.annualExemptAmount.errorMax"
      val errorNegative = "calc.annualExemptAmount.errorNegative"
      val errorDecimalPlaces = "calc.annualExemptAmount.errorDecimalPlaces"
    }

    object CurrentIncome {
      val errorReal = "calc.currentIncome.errorReal"
      val errorMax = "calc.currentIncome.errorMax"
      val errorNegative = "calc.currentIncome.errorNegative"
      val errorDecimalPlace = "calc.currentIncome.errorDecimalPlaces"
    }

    object DisposalCosts {
      val errorReal = "calc.disposalCosts.errorReal"
      val errorNegativeNumber = "calc.disposalCosts.errorNegativeNumber"
      val errorDecimalPlaces = "calc.disposalCosts.errorDecimalPlaces"
    }

    object DisposalValue {
      val errorReal = "calc.disposalValue.errorReal"
      val errorMax = "calc.disposalValue.errorMax"
      val errorDecimalPlaces = "calc.disposalValue.errorDecimalPlaces"
      val errorNegative = "calc.disposalValue.errorNegative"

    }

    object HowMuchGain {
      val errorReal = "calc.howMuchGain.errorReal"
      val errorNegativeNumber = "calc.howMuchGain.errorNegative"
      val errorDecimalPlaces = "calc.howMuchGain.errorDecimalPlaces"
      }

    object Improvements extends Common {
      val noValueSuppliedError = "calc.improvements.error.no.value.supplied"
      val negativeValueError = "calc.improvements.errorNegative"
      val excessDecimalPlacesError = "calc.improvements.errorDecimalPlaces"
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
      val errorReal = "calc.otherReliefs.errorReal"
      val errorMax = "calc.otherReliefs.errorMax"
      val errorDecimal = "calc.otherReliefs.errorDecimal"
      val errorNegative = "calc.otherReliefs.errorNegative"
    }

    object MarketValue {
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
      val errorReal = "calc.personalAllowance.errorReal"
      val errorNegative = "calc.personalAllowance.errorNegative"
      val errorDecimalPlaces = "calc.personalAllowance.errorDecimalPlaces"
      val errorMax = "calc.personalAllowance.errorMaxLimit"
    }

    object PrivateResidenceRelief {
      val errorNoValue = "calc.privateResidenceRelief.error.noValueProvided"
      val errorNegative = "calc.privateResidenceRelief.error.errorNegative"
      val errorDecimalPlaces = "calc.privateResidenceRelief.error.errorDecimalPlaces"
    }

    object RebasedCosts {
      val errorNegative = "calc.rebasedCosts.errorNegative"
      val errorNoValue = "calc.rebasedCosts.error.no.value.supplied"
      val errorDecimalPlaces = "calc.rebasedCosts.errorDecimalPlaces"
    }

    object RebasedValue {
      val errorNoValue = "calc.nonResident.rebasedValue.error.no.value.supplied"
      val errorMax = "calc.nonResident.rebasedValue.errorMax"
      val errorNegative = "calc.nonResident.rebasedValue.errorNegative"
      val errorDecimalPlaces = "calc.nonResident.rebasedValue.errorDecimalPlaces"
    }

    object HowBecameOwner {
      val errorMandatory = "calc.howBecameOwner.errors.required"
    }

    object AcquisitionMarketValue {
      val errorReal = "calc.acquisitionMarketValue.errorReal"
      val errorMax = "calc.acquisitionMarketValue.errorMax"
      val errorNegativeNumber = "calc.acquisitionMarketValue.errorNegative"
      val errorDecimalPlaces = "calc.acquisitionMarketValue.errorDecimalPlaces"
    }

    object WorthWhenBoughtForLess {
      val errorReal = "calc.worthWhenBoughtForLess.errorReal"
      val errorMax = "calc.worthWhenBoughtForLess.errorMax"
      val errorNegativeNumber = "calc.worthWhenBoughtForLess.errorNegative"
      val errorDecimalPlaces = "calc.worthWhenBoughtForLess.errorDecimalPlaces"
    }

    object WorthWhenInherited {
      val errorReal = "calc.worthWhenInherited.errorReal"
      val errorMax = "calc.worthWhenInherited.errorMax"
      val errorNegativeNumber = "calc.worthWhenInherited.errorNegative"
      val errorDecimalPlaces = "calc.worthWhenInherited.errorDecimalPlaces"
    }

    object WorthWhenGiftedTo {
      val errorReal = "calc.worthWhenGiftedTo.errorReal"
      val errorMax = "calc.worthWhenGiftedTo.errorMax"
      val errorNegativeNumber = "calc.worthWhenGiftedTo.errorNegative"
      val errorDecimalPlaces = "calc.worthWhenGiftedTo.errorDecimalPlaces"
    }

    object WorthBeforeLegislationStart {
      val errorReal = "calc.worthBeforeLegislationStart.errorReal"
      val errorMax = "calc.worthBeforeLegislationStart.errorMax"
      val errorNegativeNumber = "calc.worthBeforeLegislationStart.errorNegative"
      val errorDecimalPlaces = "calc.worthBeforeLegislationStart.errorDecimalPlaces"
    }

    object HowMuchLoss {
      val errorReal = "calc.howMuchLoss.errorReal"
      val errorMax = "calc.howMuchLoss.errorMax"
      val errorNegative = "calc.howMuchLoss.errorNegative"
      val errorDecimalPlaces = "calc.howMuchLoss.errorDecimal"
    }

    object BroughtForwardLosses {
      val errorDecimalPlaces = "calc.broughtForwardLosses.errorDecimal"
      val errorNegative = "calc.broughtForwardLosses.errorNegative"
    }

    object ClaimingReliefs {
      val errorMandatory = "calc.claimingReliefs.errorMandatory"
    }

    object CostsAtLegislationStart {
      val errorNegative = "calc.costsAtLegislationStart.errorNegative"
      val errorNoValue = "calc.costsAtLegislationStart.error.no.value.supplied"
      val errorDecimalPlaces = "calc.costsAtLegislationStart.errorDecimalPlaces"
    }

  }

  object Resident extends Common {
    val errorInvalidDate = "Enter a real date"
  }

  object WhoDidYouGiveItTo {
    val errormandatory = "calc.whoDidYouGiveThePropertyTo.errormandatory"
  }

}
