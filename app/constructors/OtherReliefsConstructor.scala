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

import common.KeystoreKeys.{NonResidentKeys => keys}
import models.{OtherReliefsModel, QuestionAnswerModel, TotalGainAnswersModel}

object OtherReliefsConstructor {

  def getOtherReliefsSection(totalGainAnswersModel: Option[TotalGainAnswersModel]): Seq[QuestionAnswerModel[Any]] = {

    val allOtherReliefs = totalGainAnswersModel.get.allOtherReliefsModel
    if (allOtherReliefs.nonEmpty) {
      val flatReliefsRow = getOtherReliefsFlatRow(allOtherReliefs.get.otherReliefsFlat)
      val rebasedReliefsRow = getOtherReliefsRebasedRow(allOtherReliefs.get.otherReliefsRebased)
      val timeApportionedReliefsRow = getOtherReliefsTimeApportionedRow(allOtherReliefs.get.otherReliefsTime)

      Seq(flatReliefsRow, rebasedReliefsRow, timeApportionedReliefsRow).flatten
    }
    else {
      Seq()
    }
  }

  def getOtherReliefsFlatRow(flatReliefs: Option[OtherReliefsModel]): Option[QuestionAnswerModel[BigDecimal]] = {
    flatReliefs match {
      case Some(OtherReliefsModel(value)) if value > 0 =>
        Some(QuestionAnswerModel(
          s"${keys.otherReliefsFlat}",
          value,
          "calc.otherReliefs.question",
          Some(controllers.routes.OtherReliefsController.otherReliefs().url)
        ))
      case _ => None
    }
  }


  def getOtherReliefsRebasedRow(rebasedReliefs: Option[OtherReliefsModel]): Option[QuestionAnswerModel[BigDecimal]] = {
    rebasedReliefs match {
      case Some(OtherReliefsModel(value)) if value > 0 =>
        Some(QuestionAnswerModel(
          s"${keys.otherReliefsRebased}",
          value,
          "calc.otherReliefs.question",
          None
        ))
      case _ => None
    }
  }

  def getOtherReliefsTimeApportionedRow(timeApportionedReliefs: Option[OtherReliefsModel]): Option[QuestionAnswerModel[BigDecimal]] = {
    timeApportionedReliefs match {
      case Some(OtherReliefsModel(value)) if value > 0 =>
        Some(QuestionAnswerModel(
          s"${keys.otherReliefsTA}",
          value,
          "calc.otherReliefs.question",
          None
        ))
      case _ => None
    }
  }

}
