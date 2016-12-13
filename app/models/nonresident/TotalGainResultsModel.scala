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

case class TotalGainResultsModel(flatGain: BigDecimal, rebasedGain: Option[BigDecimal], timeApportionedGain: Option[BigDecimal]) {

  def calculationDetailsRows(calculationType: String): Seq[QuestionAnswerModel[Any]] = CalculationDetailsConstructor.buildSection(this, calculationType)
}

object TotalGainResultsModel {
  implicit val formats = Json.format[TotalGainResultsModel]
}
