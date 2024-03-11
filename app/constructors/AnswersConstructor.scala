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

package constructors

import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.{TaxDates, YesNoKeys}
import models._
import play.api.Logging
import play.api.mvc.Request
import services.SessionCacheService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AnswersConstructor @Inject()(sessionCacheService: SessionCacheService)(implicit ec: ExecutionContext) extends Logging {

  def getNRTotalGainAnswers(implicit request: Request[_]): Future[TotalGainAnswersModel] = {
    val disposalDate = sessionCacheService.fetchAndGetFormData[DateModel](KeystoreKeys.disposalDate).map(data => {
      logger.info("Getting disposalDate as : " + data)
      data.get
    })
    val soldOrGivenAway = sessionCacheService.fetchAndGetFormData[SoldOrGivenAwayModel](KeystoreKeys.soldOrGivenAway).map(data => data.get)
    val soldForLess = sessionCacheService.fetchAndGetFormData[SoldForLessModel](KeystoreKeys.soldForLess)
    val disposalCosts = sessionCacheService.fetchAndGetFormData[DisposalCostsModel](KeystoreKeys.disposalCosts).map(data => data.get)
    val howBecameOwner = sessionCacheService.fetchAndGetFormData[HowBecameOwnerModel](KeystoreKeys.howBecameOwner)
    val boughtForLess = sessionCacheService.fetchAndGetFormData[BoughtForLessModel](KeystoreKeys.boughtForLess)
    val acquisitionCosts = sessionCacheService.fetchAndGetFormData[AcquisitionCostsModel](KeystoreKeys.acquisitionCosts)
    val acquisitionDate = sessionCacheService.fetchAndGetFormData[DateModel](KeystoreKeys.acquisitionDate).map(data => data.get)
    val rebasedValue = sessionCacheService.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue)
    val rebasedCosts = sessionCacheService.fetchAndGetFormData[RebasedCostsModel](KeystoreKeys.rebasedCosts)
    val isClaimingImprovements = sessionCacheService.fetchAndGetFormData[IsClaimingImprovementsModel](KeystoreKeys.isClaimingImprovements)
    val improvements = sessionCacheService.fetchAndGetFormData[ImprovementsModel](KeystoreKeys.improvements)
    val otherReliefsFlat = sessionCacheService.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat)
    val costsBeforeLegislationStart = sessionCacheService.fetchAndGetFormData[CostsAtLegislationStartModel](KeystoreKeys.costAtLegislationStart)

    def disposalValue(soldOrGivenAwayModel: SoldOrGivenAwayModel,
                      soldForLessModel: Option[SoldForLessModel]): Future[DisposalValueModel] = (soldOrGivenAwayModel, soldForLessModel) match {
      case (SoldOrGivenAwayModel(true), Some(SoldForLessModel(true))) =>
        sessionCacheService.fetchAndGetFormData[DisposalValueModel](KeystoreKeys.disposalMarketValue).map(data => data.get)
      case (SoldOrGivenAwayModel(true), Some(_)) =>
        sessionCacheService.fetchAndGetFormData[DisposalValueModel](KeystoreKeys.disposalValue).map(data => data.get)
      case _ =>
        sessionCacheService.fetchAndGetFormData[DisposalValueModel](KeystoreKeys.disposalMarketValue).map(data => data.get)
    }

    def acquisitionValue(acquisitionDateModel: DateModel,
                         howBecameOwnerModel: Option[HowBecameOwnerModel],
                         boughtForLessModel: Option[BoughtForLessModel]): Future[AcquisitionValueModel] =
      (acquisitionDateModel, howBecameOwnerModel, boughtForLessModel) match {
        case _ if TaxDates.dateBeforeLegislationStart(acquisitionDateModel.get) =>
          sessionCacheService.fetchAndGetFormData[WorthBeforeLegislationStartModel](KeystoreKeys.worthBeforeLegislationStart).map(data =>
            AcquisitionValueModel(data.get.worthBeforeLegislationStart)
          )
        case (_, Some(HowBecameOwnerModel(value)), _) if !value.equals("Bought") =>
          sessionCacheService.fetchAndGetFormData[AcquisitionValueModel](KeystoreKeys.acquisitionMarketValue)
            .map(data => data.get)
        case (_, _, Some(BoughtForLessModel(true))) =>
          sessionCacheService.fetchAndGetFormData[AcquisitionValueModel](KeystoreKeys.acquisitionMarketValue)
            .map(data => data.get)
        case _ => sessionCacheService.fetchAndGetFormData[AcquisitionValueModel](KeystoreKeys.acquisitionValue).map(data => data.get)
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
      isClaimingImprovements <- isClaimingImprovements
      improvements <- improvements
      otherReliefsFlat <- otherReliefsFlat
      costsBeforeLegislationStart <- costsBeforeLegislationStart
    } yield TotalGainAnswersModel(disposalDate, soldOrGivenAway, soldForLess, disposalValue, disposalCosts,
      howBecameOwner, boughtForLess, acquisitionValue, acquisitionCosts, acquisitionDate,
      rebasedValue, rebasedCosts, isClaimingImprovements.getOrElse(IsClaimingImprovementsModel(false)), improvements, otherReliefsFlat, costsBeforeLegislationStart)
  }

  def getPersonalDetailsAndPreviousCapitalGainsAnswers(implicit request: Request[_]): Future[Option[TotalPersonalDetailsCalculationModel]] = {
    val currentIncome = sessionCacheService.fetchAndGetFormData[CurrentIncomeModel](KeystoreKeys.currentIncome)
      .map(_.getOrElse(throw new Exception("No value for current income found")))
    val personalAllowance = sessionCacheService.fetchAndGetFormData[PersonalAllowanceModel](KeystoreKeys.personalAllowance)
    val otherProperties = sessionCacheService.fetchAndGetFormData[OtherPropertiesModel](KeystoreKeys.otherProperties)
      .map(data => data.getOrElse(OtherPropertiesModel(YesNoKeys.no)))
    val previousLossOrGain = sessionCacheService.fetchAndGetFormData[PreviousLossOrGainModel](KeystoreKeys.previousLossOrGain)
    val howMuchLoss = sessionCacheService.fetchAndGetFormData[HowMuchLossModel](KeystoreKeys.howMuchLoss)
    val howMuchGain = sessionCacheService.fetchAndGetFormData[HowMuchGainModel](KeystoreKeys.howMuchGain)
    val annualExemptAmount = sessionCacheService.fetchAndGetFormData[AnnualExemptAmountModel](KeystoreKeys.annualExemptAmount)
    val broughtForwardLosses = sessionCacheService.fetchAndGetFormData[BroughtForwardLossesModel](KeystoreKeys.broughtForwardLosses)
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
