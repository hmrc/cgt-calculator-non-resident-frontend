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

import common.TaxDates
import models.{AcquisitionDateModel, PrivateResidenceReliefModel, RebasedValueModel, TotalGainAnswersModel}

object PrivateResidenceReliefRequestConstructor {

  def privateResidenceReliefQuery(totalGainAnswersModel: TotalGainAnswersModel,
                                  privateResidenceReliefModel: Option[PrivateResidenceReliefModel]): String = {
    eligibleForPrivateResidenceRelief(privateResidenceReliefModel) +
    daysClaimed(totalGainAnswersModel, privateResidenceReliefModel) +
    daysClaimedAfter(totalGainAnswersModel, privateResidenceReliefModel)
  }

  def eligibleForPrivateResidenceRelief(privateResidenceReliefModel: Option[PrivateResidenceReliefModel]): String = {
    privateResidenceReliefModel match {
      case Some(PrivateResidenceReliefModel("Yes", _, _)) => "&claimingPRR=true"
      case _ => "&claimingPRR=false"
    }
  }

  def daysClaimed(totalGainAnswersModel: TotalGainAnswersModel,
                  privateResidenceReliefModel: Option[PrivateResidenceReliefModel]): String = {

    (privateResidenceReliefModel, totalGainAnswersModel.acquisitionDateModel) match {
      case (Some(PrivateResidenceReliefModel("Yes", Some(value), _)), AcquisitionDateModel("Yes",_,_,_))
        if totalGainAnswersModel.acquisitionDateModel.get.plusMonths(18).isBefore(totalGainAnswersModel.disposalDateModel.get) =>
        s"&daysClaimed=$value"
      case _ => ""
    }
  }

  def daysClaimedAfter(totalGainAnswersModel: TotalGainAnswersModel,
                       privateResidenceReliefModel: Option[PrivateResidenceReliefModel]): String = {
    (privateResidenceReliefModel, totalGainAnswersModel.acquisitionDateModel, totalGainAnswersModel.rebasedValueModel) match {
      case (Some(PrivateResidenceReliefModel("Yes", _, Some(value))), AcquisitionDateModel("Yes",_,_,_), _)
        if totalGainAnswersModel.acquisitionDateModel.get.plusMonths(18).isBefore(totalGainAnswersModel.disposalDateModel.get) &&
        !TaxDates.dateAfterStart(totalGainAnswersModel.acquisitionDateModel.get) =>
        s"&daysClaimedAfter=$value"
      case (Some(PrivateResidenceReliefModel("Yes", _, Some(value))), AcquisitionDateModel("No",_,_,_), Some(RebasedValueModel(Some(_)))) =>
        s"&daysClaimedAfter=$value"
      case _ => ""
    }
  }
}
