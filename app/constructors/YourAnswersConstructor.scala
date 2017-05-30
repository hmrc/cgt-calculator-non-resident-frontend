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

import models._

object YourAnswersConstructor {

  def fetchYourAnswers(totalGainAnswersModel: TotalGainAnswersModel,
                       privateResidenceReliefModel: Option[PrivateResidenceReliefModel] = None,
                       personalAndPreviousDetailsModel: Option[TotalPersonalDetailsCalculationModel] = None,
                       propertyLivedInModel: Option[PropertyLivedInModel] = None): Seq[QuestionAnswerModel[Any]] = {
    val salesDetailsRows = SalesDetailsConstructor.salesDetailsRows(totalGainAnswersModel)
    val purchaseDetailsRows = PurchaseDetailsConstructor.getPurchaseDetailsSection(totalGainAnswersModel)
    val propertyLivedInRow = DeductionDetailsConstructor.propertyLivedInQuestionRow(propertyLivedInModel)
    val propertyDetailsRows = PropertyDetailsConstructor.propertyDetailsRows(totalGainAnswersModel)
    val deductionDetailsRows = DeductionDetailsConstructor.deductionDetailsRows(totalGainAnswersModel, privateResidenceReliefModel)
    val personalAndPreviousDetailsRows = PersonalAndPreviousDetailsConstructor.personalAndPreviousDetailsRows(personalAndPreviousDetailsModel)

    if(!propertyLivedInRow.isEmpty) {
      salesDetailsRows ++ purchaseDetailsRows ++ propertyDetailsRows ++ propertyLivedInRow ++  deductionDetailsRows ++ personalAndPreviousDetailsRows
    }

    else {
      salesDetailsRows ++ purchaseDetailsRows ++ propertyDetailsRows ++ personalAndPreviousDetailsRows
    }
  }
}
