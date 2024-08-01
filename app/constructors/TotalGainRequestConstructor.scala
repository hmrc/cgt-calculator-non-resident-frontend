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

import common.TaxDates
import models._

object TotalGainRequestConstructor {

  def totalGainQuery(model: TotalGainAnswersModel): Map[String, Option[String]] = {
    val acquisitionCosts = {
      (model.acquisitionCostsModel, model.costsAtLegislationStart) match {
        case (_, Some(CostsAtLegislationStartModel("Yes", costs))) if TaxDates.dateBeforeLegislationStart(model.acquisitionDateModel.get) =>
          costs.get
        case (Some(acquisition), _) if !TaxDates.dateBeforeLegislationStart(model.acquisitionDateModel.get) =>
          acquisition.acquisitionCostsAmt
        case _ => BigDecimal(0)
      }
    }
    val improvements = model.improvementsModel.map(_.improvementsAmt).filter(_ > 0)
    val rebasedProps = model.rebasedValueModel.flatMap {
      case RebasedValueModel(value) if !TaxDates.dateAfterStart(model.acquisitionDateModel.get) =>
        val rebasedCosts = model.rebasedCostsModel.filter(_.hasRebasedCosts == "Yes").flatMap(_.rebasedCosts)
        val improvementsAfterTaxStarted = model.improvementsModel.flatMap(_.improvementsAmtAfter)
        Some(Map(
          "rebasedValue" -> Some(value.toString),
          "rebasedCosts" -> rebasedCosts.map(_.toString),
          "improvementsAfterTaxStarted" -> improvementsAfterTaxStarted.map(_.toString)
        ))
      case _ => None
    }
    Map(
      "disposalValue" -> Some(model.disposalValueModel.disposalValue.toString()),
      "disposalCosts" -> Some(model.disposalCostsModel.disposalCosts.toString()),
      "acquisitionValue" -> Some(model.acquisitionValueModel.acquisitionValueAmt.toString),
      "acquisitionCosts" -> Some(acquisitionCosts.toString()),
      "improvements" -> improvements.map(_.toString()),
      "disposalDate" -> Some(dateToString(model.disposalDateModel)),
      "acquisitionDate" -> Some(dateToString(model.acquisitionDateModel))
    ) ++ rebasedProps.getOrElse(Map.empty)
  }

  def includeLegislationCosts(costsAtLegislationStartModel: CostsAtLegislationStartModel, acquisitionDateModel: DateModel): Boolean = {
    costsAtLegislationStartModel.hasCosts == "Yes" && TaxDates.dateBeforeLegislationStart(acquisitionDateModel.get)
  }

  def afterLegislation(acquisitionDateModel: DateModel): Boolean = {
    !TaxDates.dateBeforeLegislationStart(acquisitionDateModel.get)
  }

  def includeRebasedValuesInCalculation(oRebasedValueModel: Option[RebasedValueModel], acquisitionDateModel: DateModel): Boolean = {
    oRebasedValueModel.isDefined && !TaxDates.dateAfterStart(Some(acquisitionDateModel))
  }

  private def dateToString(date: DateModel) = s"${date.year}-${date.month}-${date.day}"
}
