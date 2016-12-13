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

package constructors.nonresident

import common.{KeystoreKeys, TaxDates}
import connectors.CalculatorConnector
import models.nonresident._
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AnswersConstructor extends AnswersConstructor {
  val calculatorConnector = CalculatorConnector
}

trait AnswersConstructor {
  val calculatorConnector: CalculatorConnector

  def getNRTotalGainAnswers(implicit hc: HeaderCarrier): Future[TotalGainAnswersModel] = {
    val disposalDate = calculatorConnector.fetchAndGetFormData[DisposalDateModel](KeystoreKeys.disposalDate).map(data => data.get)
    val soldOrGivenAway = calculatorConnector.fetchAndGetFormData[SoldOrGivenAwayModel](KeystoreKeys.soldOrGivenAway).map(data => data.get)
    val soldForLess = calculatorConnector.fetchAndGetFormData[SoldForLessModel](KeystoreKeys.NonResidentKeys.soldForLess)
    val disposalCosts = calculatorConnector.fetchAndGetFormData[DisposalCostsModel](KeystoreKeys.disposalCosts).map(data => data.get)
    val howBecameOwner = calculatorConnector.fetchAndGetFormData[HowBecameOwnerModel](KeystoreKeys.howBecameOwner)
    val boughtForLess = calculatorConnector.fetchAndGetFormData[BoughtForLessModel](KeystoreKeys.boughtForLess)
    val acquisitionCosts = calculatorConnector.fetchAndGetFormData[AcquisitionCostsModel](KeystoreKeys.acquisitionCosts).map(data => data.get)
    val acquisitionDate = calculatorConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).map(data => data.get)
    val rebasedValue = calculatorConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue)
    val rebasedCosts = calculatorConnector.fetchAndGetFormData[RebasedCostsModel](KeystoreKeys.rebasedCosts)
    val improvements = calculatorConnector.fetchAndGetFormData[ImprovementsModel](KeystoreKeys.improvements).map(data => data.get)
    val otherReliefsFlat = calculatorConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat)

    def disposalValue(soldOrGivenAwayModel: SoldOrGivenAwayModel,
                      soldForLessModel: Option[SoldForLessModel]): Future[DisposalValueModel] = (soldOrGivenAwayModel, soldForLessModel) match {
      case (SoldOrGivenAwayModel(true), Some(SoldForLessModel(true))) =>
        calculatorConnector.fetchAndGetFormData[DisposalValueModel](KeystoreKeys.disposalMarketValue).map(data => data.get)
      case (SoldOrGivenAwayModel(true), Some(_)) =>
        calculatorConnector.fetchAndGetFormData[DisposalValueModel](KeystoreKeys.disposalValue).map(data => data.get)
      case _ =>
        calculatorConnector.fetchAndGetFormData[DisposalValueModel](KeystoreKeys.disposalMarketValue).map(data => data.get)
    }

    def acquisitionValue(acquisitionDateModel: AcquisitionDateModel,
                         howBecameOwnerModel: Option[HowBecameOwnerModel],
                         boughtForLessModel: Option[BoughtForLessModel]): Future[AcquisitionValueModel] =
      (acquisitionDateModel, howBecameOwnerModel, boughtForLessModel) match {
        case (AcquisitionDateModel("Yes",_,_,_),_, _) if TaxDates.dateBeforeLegislationStart(acquisitionDateModel.get) =>
          calculatorConnector.fetchAndGetFormData[WorthBeforeLegislationStartModel](KeystoreKeys.worthBeforeLegislationStart).map(data =>
            AcquisitionValueModel(data.get.worthBeforeLegislationStart)
          )
        case (_, Some(HowBecameOwnerModel(value)), _) if !value.equals("Bought") =>
          calculatorConnector.fetchAndGetFormData[AcquisitionValueModel](KeystoreKeys.acquisitionMarketValue)
            .map(data => data.get)
        case (_, _, Some(BoughtForLessModel(true))) =>
          calculatorConnector.fetchAndGetFormData[AcquisitionValueModel](KeystoreKeys.acquisitionMarketValue)
            .map(data => data.get)
        case _ => calculatorConnector.fetchAndGetFormData[AcquisitionValueModel](KeystoreKeys.acquisitionValue).map(data => data.get)
      }

    for {
      disposalDate <- disposalDate
      soldOrGivenAway <- soldOrGivenAway
      soldForLess <- soldForLess
      disposalValue <- disposalValue(soldOrGivenAway, soldForLess)
      disposalCosts <- disposalCosts
      howBecameOwner <- howBecameOwner
      boughtForLess <- boughtForLess
      acquisitionDate <- acquisitionDate
      acquisitionValue <- acquisitionValue(acquisitionDate, howBecameOwner, boughtForLess)
      acquisitionCosts <- acquisitionCosts
      rebasedValue <- rebasedValue
      rebasedCosts <- rebasedCosts
      improvements <- improvements
      otherReliefsFlat <- otherReliefsFlat
    } yield TotalGainAnswersModel(disposalDate, soldOrGivenAway, soldForLess, disposalValue, disposalCosts,
      howBecameOwner, boughtForLess, acquisitionValue, acquisitionCosts, acquisitionDate,
      rebasedValue, rebasedCosts, improvements, otherReliefsFlat)
  }

  def getPersonalDetailsAndPreviousCapitalGainsAnswers(implicit hc: HeaderCarrier): Future[Option[TotalPersonalDetailsCalculationModel]] = {
    val customerType = calculatorConnector.fetchAndGetFormData[CustomerTypeModel](KeystoreKeys.customerType).map(data => data.get)
    val currentIncome = calculatorConnector.fetchAndGetFormData[CurrentIncomeModel](KeystoreKeys.currentIncome)
    val personalAllowance = calculatorConnector.fetchAndGetFormData[PersonalAllowanceModel](KeystoreKeys.personalAllowance)
    val isVulnerableTrustee = calculatorConnector.fetchAndGetFormData[DisabledTrusteeModel](KeystoreKeys.disabledTrustee)
    val otherProperties = calculatorConnector.fetchAndGetFormData[OtherPropertiesModel](KeystoreKeys.otherProperties).map(data => data.get)
    val previousLossOrGain = calculatorConnector.fetchAndGetFormData[PreviousLossOrGainModel](KeystoreKeys.NonResidentKeys.previousLossOrGain)
    val howMuchLoss = calculatorConnector.fetchAndGetFormData[HowMuchLossModel](KeystoreKeys.howMuchLoss)
    val howMuchGain = calculatorConnector.fetchAndGetFormData[HowMuchGainModel](KeystoreKeys.howMuchGain)
    val annualExemptAmount = calculatorConnector.fetchAndGetFormData[AnnualExemptAmountModel](KeystoreKeys.annualExemptAmount)
    val broughtForwardLosses = calculatorConnector.fetchAndGetFormData[BroughtForwardLossesModel](KeystoreKeys.broughtForwardLosses).map(data => data.get)

    for {
      customerType <- customerType
      currentIncome <- currentIncome
      personalAllowance <- personalAllowance
      isVulnerableTrustee <- isVulnerableTrustee
      otherProperties <- otherProperties
      previousLossOrGain <- previousLossOrGain
      howMuchLoss <- howMuchLoss
      howMuchGain <- howMuchGain
      annualExemptAmount <- annualExemptAmount
      broughtForwardLosses <- broughtForwardLosses
    } yield Some(TotalPersonalDetailsCalculationModel(customerType, currentIncome, personalAllowance, isVulnerableTrustee, otherProperties,
      previousLossOrGain, howMuchLoss, howMuchGain, annualExemptAmount, broughtForwardLosses))
  }
}
