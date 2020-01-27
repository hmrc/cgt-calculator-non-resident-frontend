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
    val errorRequired = "calc.common.error.fieldRequired"

    val numericPlayErrorOverride = "error.number"
    val optionReqError = "calc.base.optionReqError"

    def maximumError(value: String): String = s"calc.common.error.maxNumericExceeded"
  }

  object NonResident extends Common {

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
      val errorNegative = "calc.acquisitionValue.errorNegative"
      val errorDecimalPlaces = "calc.acquisitionValue.errorDecimalPlaces"
    }

    object AnnualExemptAmount {
      val errorMaxStart = "calc.annualExemptAmount.errorMax"
      val errorMaxEnd = "calc.annualExemptAmount.errorMaxEnd"
      val errorNegative = "calc.annualExemptAmount.errorNegative"
      val errorDecimalPlaces = "calc.annualExemptAmount.errorDecimalPlaces"
    }

    object CurrentIncome {
      val errorNegative = "calc.currentIncome.errorNegative"
      val errorDecimalPlace = "calc.currentIncome.errorDecimalPlaces"
    }

    object DisposalCosts {
      val errorNegativeNumber = "calc.disposalCosts.errorNegativeNumber"
      val errorDecimalPlaces = "calc.disposalCosts.errorDecimalPlaces"
    }

    object DisposalValue {
      val errorDecimalPlaces = "calc.disposalValue.errorDecimalPlaces"
      val errorNegative = "calc.disposalValue.errorNegative"

    }

    object HowMuchGain {
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
      val errorDecimal = "calc.otherReliefs.errorDecimal"
      val errorNegative = "calc.otherReliefs.errorMinimum"
    }

    object MarketValue {
      val disposalErrorDecimalPlacesGaveAway = "calc.marketValue.error.gaveItAway.decimalPlaces"
      val disposalErrorDecimalPlacesSold = "calc.marketValue.error.sold.decimalPlaces"

      val errorNegativeGaveAway = "calc.marketValue.error.gaveItAway.negative"
      val errorNegativeSold = "calc.marketValue.error.sold.negative"
    }

    object PersonalAllowance {
      val errorNegative = "calc.personalAllowance.errorNegative"
      val errorDecimalPlaces = "calc.personalAllowance.errorDecimalPlaces"
      val errorMaxLimit = "calc.personalAllowance.errorMaxLimit"
      val errorMaxLimitEnd = "calc.personalAllowance.errorMaxLimitEnd"
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
      val errorNegative = "calc.nonResident.rebasedValue.errorNegative"
      val errorDecimalPlaces = "calc.nonResident.rebasedValue.errorDecimalPlaces"
    }

    object HowBecameOwner {
      val errorMandatory = "calc.howBecameOwner.errors.required"
    }

    object AcquisitionMarketValue {
      val errorNegativeNumber = "calc.acquisitionMarketValue.errorNegative"
      val errorDecimalPlaces = "calc.acquisitionMarketValue.errorDecimalPlaces"
    }

    object HowMuchLoss {
      val errorNegative = "calc.howMuchLoss.errorMinimum"
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

  object PropertyLivedIn {
    val errorNoSelect = "calc.propertyLivedIn.noSelectError"
  }

  object WhoDidYouGiveItTo {
    val errormandatory = "calc.whoDidYouGiveThePropertyTo.errormandatory"
  }
}
