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

import common.TaxDates
import models.nonresident._

object TotalGainRequestConstructor {

  def totalGainQuery(totalGainAnswersModel: TotalGainAnswersModel): String = {
    disposalValue(totalGainAnswersModel.disposalValueModel) +
    disposalCosts(totalGainAnswersModel.disposalCostsModel) +
    acquisitionValue(totalGainAnswersModel.acquisitionValueModel) +
    acquisitionCosts(totalGainAnswersModel.acquisitionCostsModel) +
    improvements(totalGainAnswersModel.improvementsModel) +
    rebasedValues(totalGainAnswersModel.rebasedValueModel, totalGainAnswersModel.rebasedCostsModel,
      totalGainAnswersModel.improvementsModel, totalGainAnswersModel.acquisitionDateModel) +
    timeApportionedValues(totalGainAnswersModel.disposalDateModel, totalGainAnswersModel.acquisitionDateModel)
  }

  def disposalValue(disposalValueModel: DisposalValueModel): String = {
    s"disposalValue=${disposalValueModel.disposalValue}"
  }

  def disposalCosts(disposalCostsModel: DisposalCostsModel): String = {
    s"&disposalCosts=${disposalCostsModel.disposalCosts}"
  }

  def acquisitionValue(acquisitionValueModel: AcquisitionValueModel): String = {
    s"&acquisitionValue=${acquisitionValueModel.acquisitionValueAmt}"
  }

  def acquisitionCosts(acquisitionCostsModel: AcquisitionCostsModel): String = {
    s"&acquisitionCosts=${acquisitionCostsModel.acquisitionCostsAmt}"
  }

  def improvements(improvementsModel: ImprovementsModel): String = {
    improvementsModel match {
      case ImprovementsModel("Yes", Some(value), _) =>
        s"&improvements=$value"
      case _ => ""
    }
  }

  def rebasedValues(rebasedValueModel: Option[RebasedValueModel],
                    rebasedCostsModel: Option[RebasedCostsModel],
                    improvementsModel: ImprovementsModel,
                    acquisitionDateModel: AcquisitionDateModel): String = {
    (rebasedValueModel, acquisitionDateModel) match {
      case (Some(RebasedValueModel(Some(value))), AcquisitionDateModel("Yes",_,_,_))
        if !TaxDates.dateAfterStart(acquisitionDateModel.get) =>
        s"&rebasedValue=$value${rebasedCosts(rebasedCostsModel.get)}${improvementsAfterTaxStarted(improvementsModel)}"
      case (Some(RebasedValueModel(Some(value))), AcquisitionDateModel("No",_,_,_)) =>
        s"&rebasedValue=$value${rebasedCosts(rebasedCostsModel.get)}${improvementsAfterTaxStarted(improvementsModel)}"
      case _ => ""
    }
  }

  def rebasedCosts(rebasedCostsModel: RebasedCostsModel): String = {
    rebasedCostsModel match {
      case RebasedCostsModel("Yes", Some(value)) => s"&rebasedCosts=$value"
      case _ => ""
    }
  }

  def improvementsAfterTaxStarted(improvementsModel: ImprovementsModel): String = {
    improvementsModel match {
      case ImprovementsModel("Yes", _, Some(value)) => s"&improvementsAfterTaxStarted=$value"
      case _ => ""
    }
  }

  def timeApportionedValues(disposalDateModel: DisposalDateModel, acquisitionDateModel: AcquisitionDateModel): String = {
    (disposalDateModel, acquisitionDateModel) match {
      case (DisposalDateModel(dDay, dMonth, dYear), AcquisitionDateModel("Yes", Some(aDay), Some(aMonth), Some(aYear)))
        if !TaxDates.dateAfterStart(acquisitionDateModel.get) =>
        s"&disposalDate=$dYear-$dMonth-$dDay&acquisitionDate=$aYear-$aMonth-$aDay"
      case (DisposalDateModel(dDay, dMonth, dYear), _) => s"&disposalDate=$dYear-$dMonth-$dDay"
    }
  }
}
