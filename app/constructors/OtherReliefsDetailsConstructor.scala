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

package constructors

import models.{OtherReliefsModel, QuestionAnswerModel}
import common.nonresident.CalculationType
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import play.api.i18n.Messages

object OtherReliefsDetailsConstructor {

  def getOtherReliefsSection(otherReliefs: Option[OtherReliefsModel],
                             calculationElection: String): Seq[QuestionAnswerModel[Any]] = {

    val flatReliefsRow = getOtherReliefsFlatRow(otherReliefs, calculationElection)
    val rebasedReliefsRow = getOtherReliefsRebasedRow(otherReliefs, calculationElection)
    val timeApportionedReliefsRow = getOtherReliefsTimeApportionedRow(otherReliefs, calculationElection)

    Seq(flatReliefsRow, rebasedReliefsRow, timeApportionedReliefsRow).flatten
  }

  def getOtherReliefsRebasedRow(rebasedReliefs: Option[OtherReliefsModel],
                                calculationElection: String): Option[QuestionAnswerModel[BigDecimal]] = {
    (calculationElection, rebasedReliefs) match {
      case (CalculationType.rebased, Some(OtherReliefsModel(value))) if value > 0 =>
        Some(QuestionAnswerModel(
          s"${KeystoreKeys.otherReliefsRebased}",
          value,
          Messages("calc.otherReliefs.question"),
          None
        ))
      case _ => None
    }
  }

  def getOtherReliefsTimeApportionedRow(timeApportionedReliefs: Option[OtherReliefsModel],
                                        calculationElection: String): Option[QuestionAnswerModel[BigDecimal]] = {
    (calculationElection, timeApportionedReliefs) match {
      case (CalculationType.timeApportioned, Some(OtherReliefsModel(value))) if value > 0 =>
        Some(QuestionAnswerModel(
          s"${KeystoreKeys.otherReliefsTA}",
          value,
          Messages("calc.otherReliefs.question"),
          None
        ))
      case _ => None
    }
  }

  def getOtherReliefsFlatRow(flatReliefs: Option[OtherReliefsModel],
                             calculationElection: String): Option[QuestionAnswerModel[BigDecimal]] = {
    (calculationElection, flatReliefs) match {
      case (CalculationType.flat, Some(OtherReliefsModel(value))) if value > 0 =>
        Some(QuestionAnswerModel(
          s"${KeystoreKeys.otherReliefsFlat}",
          value,
          Messages("calc.otherReliefs.question"),
          None
        ))
      case _ => None
    }
  }
}
