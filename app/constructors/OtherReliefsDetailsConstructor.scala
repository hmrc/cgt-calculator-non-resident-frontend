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

import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.nonresident.{CalculationType, Flat, Rebased, TimeApportioned}
import models.{OtherReliefsModel, QuestionAnswerModel}
import play.api.i18n.{Messages, MessagesProvider}

import javax.inject.Inject

class OtherReliefsDetailsConstructor @Inject()(implicit messagesProvider: MessagesProvider) {

  def getOtherReliefsSection(otherReliefs: Option[OtherReliefsModel],
                             calculationElection: CalculationType): Seq[QuestionAnswerModel[Any]] = {

    val flatReliefsRow = getOtherReliefsFlatRow(otherReliefs, calculationElection)
    val rebasedReliefsRow = getOtherReliefsRebasedRow(otherReliefs, calculationElection)
    val timeApportionedReliefsRow = getOtherReliefsTimeApportionedRow(otherReliefs, calculationElection)

    Seq(flatReliefsRow, rebasedReliefsRow, timeApportionedReliefsRow).flatten
  }

  def getOtherReliefsRebasedRow(rebasedReliefs: Option[OtherReliefsModel],
                                calculationElection: CalculationType): Option[QuestionAnswerModel[BigDecimal]] = {
    (calculationElection, rebasedReliefs) match {
      case (Rebased, Some(OtherReliefsModel(value))) if value > 0 =>
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
                                        calculationElection: CalculationType): Option[QuestionAnswerModel[BigDecimal]] = {
    (calculationElection, timeApportionedReliefs) match {
      case (TimeApportioned, Some(OtherReliefsModel(value))) if value > 0 =>
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
                             calculationElection: CalculationType): Option[QuestionAnswerModel[BigDecimal]] = {
    (calculationElection, flatReliefs) match {
      case (Flat, Some(OtherReliefsModel(value))) if value > 0 =>
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
