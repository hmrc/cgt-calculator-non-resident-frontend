/*
 * Copyright 2023 HM Revenue & Customs
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

package models

import play.api.libs.json.{Json, OFormat}

case class TotalTaxOwedModel (taxOwed: BigDecimal,
                         taxGain: Double,
                         taxRate: Int,
                         upperTaxGain: Option[BigDecimal] = None,
                         upperTaxRate: Option[Int] = None,
                         totalGain: BigDecimal,
                         taxableGain: BigDecimal,
                         prrUsed: Option[BigDecimal],
                         otherReliefsUsed: Option[BigDecimal],
                         allowableLossesUsed: Option[BigDecimal],
                         aeaUsed: Option[BigDecimal],
                         aeaRemaining: BigDecimal,
                         broughtForwardLossesUsed: Option[BigDecimal],
                         reliefsRemaining: Option[BigDecimal],
                         allowableLossesRemaining: Option[BigDecimal],
                         broughtForwardLossesRemaining: Option[BigDecimal],
                         totalDeductions: Option[BigDecimal],
                         taxOwedAtBaseRate: Option[BigDecimal],
                         taxOwedAtUpperRate: Option[BigDecimal])

object TotalTaxOwedModel {
  implicit val formats: OFormat[TotalTaxOwedModel] = Json.format[TotalTaxOwedModel]
}
