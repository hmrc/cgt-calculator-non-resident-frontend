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

package models.nonresident

import constructors.nonresident.CalculationDetailsConstructor
import play.api.libs.json.Json

case class CalculationResultModel(taxOwed: BigDecimal,
                                  totalGain: BigDecimal,
                                  baseTaxGain: BigDecimal,
                                  baseTaxRate: Int,
                                  usedAnnualExemptAmount: BigDecimal,
                                  upperTaxGain: Option[BigDecimal],
                                  upperTaxRate: Option[Int],
                                  simplePRR: Option[BigDecimal]) {

  val taxableGain: BigDecimal = baseTaxGain + upperTaxGain.getOrElse(0)

  def calculationDetailsRows(calculationType: String): Seq[QuestionAnswerModel[Any]] =
    Seq(QuestionAnswerModel[String]("", "", "", None))
}

object CalculationResultModel {
  implicit val formats = Json.format[CalculationResultModel]
}
