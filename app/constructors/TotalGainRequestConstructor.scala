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
import models._

object TotalGainRequestConstructor {

  def totalGainQuery(totalGainAnswersModel: TotalGainAnswersModel): String = {
    val costs:BigDecimal = if(totalGainAnswersModel.acquisitionCostsModel.isDefined) totalGainAnswersModel.acquisitionCostsModel.get.acquisitionCostsAmt
    else totalGainAnswersModel.costsBeforeLegislationStart.get.costs.getOrElse(0.00)

    disposalValue(totalGainAnswersModel.disposalValueModel) +
    disposalCosts(totalGainAnswersModel.disposalCostsModel) +
    acquisitionValue(totalGainAnswersModel.acquisitionValueModel) +
    acquisitionCosts(totalGainAnswersModel.acquisitionCostsModel, totalGainAnswersModel.costsBeforeLegislationStart, totalGainAnswersModel.acquisitionDateModel) +
    improvements(totalGainAnswersModel.improvementsModel) +
    rebasedValues(totalGainAnswersModel.rebasedValueModel, totalGainAnswersModel.rebasedCostsModel,
      totalGainAnswersModel.improvementsModel, totalGainAnswersModel.acquisitionDateModel) +
    disposalDate(totalGainAnswersModel.disposalDateModel) +
    acquisitionDate(totalGainAnswersModel.acquisitionDateModel)
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

  def acquisitionCosts(acquisitionCostsModel: Option[AcquisitionCostsModel],
                       costsAtLegislationStartModel: Option[CostsAtLegislationStartModel],
                       acquisitionDateModel: AcquisitionDateModel): String = {
    s"&acquisitionCosts=${
      (acquisitionCostsModel, costsAtLegislationStartModel) match {
        case (_, Some(model)) if TaxDates.dateBeforeLegislationStart(acquisitionDateModel.get) && model.hasCosts == "Yes" =>
          model.costs.get
        case (Some(model), _) if !TaxDates.dateBeforeLegislationStart(acquisitionDateModel.get) =>
          model.acquisitionCostsAmt
        case _ => 0
      }
    }"
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
    rebasedValueModel match {
      case (Some(RebasedValueModel(value)))
        if !TaxDates.dateAfterStart(acquisitionDateModel.get) =>
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

  def disposalDate(disposalDateModel: DisposalDateModel): String = {
    s"&disposalDate=${disposalDateModel.year}-${disposalDateModel.month}-${disposalDateModel.day}"
  }

  def acquisitionDate(acquisitionDateModel: AcquisitionDateModel): String = {
    s"&acquisitionDate=${acquisitionDateModel.year}-${acquisitionDateModel.month}-${acquisitionDateModel.day}"
  }
}
