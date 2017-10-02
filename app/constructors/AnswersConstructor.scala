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

import common.{TaxDates, YesNoKeys}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import connectors.CalculatorConnector
import models._
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

object AnswersConstructor extends AnswersConstructor {
  val calculatorConnector = CalculatorConnector
}

trait AnswersConstructor {
  val calculatorConnector: CalculatorConnector

  def getNRTotalGainAnswers(implicit hc: HeaderCarrier): Future[TotalGainAnswersModel] = {
    val disposalDate = calculatorConnector.fetchAndGetFormData[DisposalDateModel](KeystoreKeys.disposalDate).map(data => {
      Logger.info("Getting disposalDate as : " + data)
      data.get
    })
    val soldOrGivenAway = calculatorConnector.fetchAndGetFormData[SoldOrGivenAwayModel](KeystoreKeys.soldOrGivenAway).map(data => data.get)
    val soldForLess = calculatorConnector.fetchAndGetFormData[SoldForLessModel](KeystoreKeys.soldForLess)
    val disposalCosts = calculatorConnector.fetchAndGetFormData[DisposalCostsModel](KeystoreKeys.disposalCosts).map(data => data.get)
    val howBecameOwner = calculatorConnector.fetchAndGetFormData[HowBecameOwnerModel](KeystoreKeys.howBecameOwner)
    val boughtForLess = calculatorConnector.fetchAndGetFormData[BoughtForLessModel](KeystoreKeys.boughtForLess)
    val acquisitionCosts = calculatorConnector.fetchAndGetFormData[AcquisitionCostsModel](KeystoreKeys.acquisitionCosts)
    val acquisitionDate = calculatorConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).map(data => data.get)
    val rebasedValue = calculatorConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue)
    val rebasedCosts = calculatorConnector.fetchAndGetFormData[RebasedCostsModel](KeystoreKeys.rebasedCosts)
    val improvements = calculatorConnector.fetchAndGetFormData[ImprovementsModel](KeystoreKeys.improvements).map(data => data.get)
    val otherReliefsFlat = calculatorConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat)
    val costsBeforeLegislationStart = calculatorConnector.fetchAndGetFormData[CostsAtLegislationStartModel](KeystoreKeys.costAtLegislationStart)

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
        case _ if TaxDates.dateBeforeLegislationStart(acquisitionDateModel.get) =>
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
      costsBeforeLegislationStart <- costsBeforeLegislationStart
    } yield TotalGainAnswersModel(disposalDate, soldOrGivenAway, soldForLess, disposalValue, disposalCosts,
      howBecameOwner, boughtForLess, acquisitionValue, acquisitionCosts, acquisitionDate,
      rebasedValue, rebasedCosts, improvements, otherReliefsFlat, costsBeforeLegislationStart)
  }

  def getPersonalDetailsAndPreviousCapitalGainsAnswers(implicit hc: HeaderCarrier): Future[Option[TotalPersonalDetailsCalculationModel]] = {
    val currentIncome = calculatorConnector.fetchAndGetFormData[CurrentIncomeModel](KeystoreKeys.currentIncome)
      .map(_.getOrElse(throw new Exception("No value for current income found")))
    val personalAllowance = calculatorConnector.fetchAndGetFormData[PersonalAllowanceModel](KeystoreKeys.personalAllowance)
    val otherProperties = calculatorConnector.fetchAndGetFormData[OtherPropertiesModel](KeystoreKeys.otherProperties)
      .map(data => data.getOrElse(OtherPropertiesModel(YesNoKeys.no)))
    val previousLossOrGain = calculatorConnector.fetchAndGetFormData[PreviousLossOrGainModel](KeystoreKeys.previousLossOrGain)
    val howMuchLoss = calculatorConnector.fetchAndGetFormData[HowMuchLossModel](KeystoreKeys.howMuchLoss)
    val howMuchGain = calculatorConnector.fetchAndGetFormData[HowMuchGainModel](KeystoreKeys.howMuchGain)
    val annualExemptAmount = calculatorConnector.fetchAndGetFormData[AnnualExemptAmountModel](KeystoreKeys.annualExemptAmount)
    val broughtForwardLosses = calculatorConnector.fetchAndGetFormData[BroughtForwardLossesModel](KeystoreKeys.broughtForwardLosses)
      .map(data => data.getOrElse(BroughtForwardLossesModel(isClaiming = false, None)))

    for {
      currentIncome <- currentIncome
      personalAllowance <- personalAllowance
      otherProperties <- otherProperties
      previousLossOrGain <- previousLossOrGain
      howMuchLoss <- howMuchLoss
      howMuchGain <- howMuchGain
      annualExemptAmount <- annualExemptAmount
      broughtForwardLosses <- broughtForwardLosses
    } yield Some(TotalPersonalDetailsCalculationModel(currentIncome, personalAllowance, otherProperties,
      previousLossOrGain, howMuchLoss, howMuchGain, annualExemptAmount, broughtForwardLosses))
  }
}
