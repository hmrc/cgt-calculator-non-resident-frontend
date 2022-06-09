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

package constructors

import common.TaxDates
import models.{PrivateResidenceReliefModel, PropertyLivedInModel, TotalGainAnswersModel}

object PrivateResidenceReliefRequestConstructor {

  def privateResidenceReliefQuery(totalGainAnswersModel: TotalGainAnswersModel,
                                  privateResidenceReliefModel: Option[PrivateResidenceReliefModel],
                                  propertyLivedInModel: Option[PropertyLivedInModel]): String = {
    if(checkLivedInProperty(propertyLivedInModel)) {
      eligibleForPrivateResidenceRelief(privateResidenceReliefModel) +
        daysClaimed(totalGainAnswersModel, privateResidenceReliefModel) +
        daysClaimedAfter(totalGainAnswersModel, privateResidenceReliefModel)
    } else "&claimingPRR=false"
  }

  def eligibleForPrivateResidenceRelief(privateResidenceReliefModel: Option[PrivateResidenceReliefModel]): String = {
    privateResidenceReliefModel match {
      case Some(PrivateResidenceReliefModel("Yes", _, _)) => "&claimingPRR=true"
      case _ => "&claimingPRR=false"
    }
  }

  def daysClaimed(totalGainAnswersModel: TotalGainAnswersModel,
                  privateResidenceReliefModel: Option[PrivateResidenceReliefModel]): String = {

    val pRRDateDetails = TaxDates.privateResidenceReliefMonthDeductionApplicable(totalGainAnswersModel.disposalDateModel)

    privateResidenceReliefModel match {
      case (Some(PrivateResidenceReliefModel("Yes", Some(value), daysAfter)))
        if totalGainAnswersModel.acquisitionDateModel.get.plusMonths(pRRDateDetails.months).isBefore(totalGainAnswersModel.disposalDateModel.get) =>
        s"&daysClaimed=${value + daysAfter.getOrElse(0)}"
      case _ => ""
    }
  }

  def daysClaimedAfter(totalGainAnswersModel: TotalGainAnswersModel,
                       privateResidenceReliefModel: Option[PrivateResidenceReliefModel]): String = {

    val pRRDateDetails = TaxDates.privateResidenceReliefMonthDeductionApplicable(totalGainAnswersModel.disposalDateModel)

    privateResidenceReliefModel match {
      case (Some(PrivateResidenceReliefModel("Yes", _, Some(value))))
        if totalGainAnswersModel.acquisitionDateModel.get.plusMonths(pRRDateDetails.months).isBefore(totalGainAnswersModel.disposalDateModel.get) &&
        !TaxDates.dateAfterStart(totalGainAnswersModel.acquisitionDateModel.get) =>
          s"&daysClaimedAfter=$value"
      case _ => ""
    }
  }

  def checkLivedInProperty(propertyLivedInModel: Option[PropertyLivedInModel]): Boolean = {
    propertyLivedInModel match {
      case Some(data) if data.propertyLivedIn => true
      case _ => false
    }
  }
}
