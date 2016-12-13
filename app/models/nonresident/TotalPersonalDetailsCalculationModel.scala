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

import constructors.nonresident.PersonalDetailsConstructor

case class TotalPersonalDetailsCalculationModel(customerTypeModel: CustomerTypeModel,
                                                currentIncomeModel: Option[CurrentIncomeModel],
                                                personalAllowanceModel: Option[PersonalAllowanceModel],
                                                trusteeModel: Option[DisabledTrusteeModel],
                                                otherPropertiesModel: OtherPropertiesModel,
                                                previousGainOrLoss: Option[PreviousLossOrGainModel],
                                                howMuchLossModel: Option[HowMuchLossModel],
                                                howMuchGainModel: Option[HowMuchGainModel],
                                                annualExemptAmountModel: Option[AnnualExemptAmountModel],
                                                broughtForwardLossesModel: BroughtForwardLossesModel
                                               ) {

  lazy val personalDetailsRows = PersonalDetailsConstructor.getPersonalDetailsSection(Some(this))
}