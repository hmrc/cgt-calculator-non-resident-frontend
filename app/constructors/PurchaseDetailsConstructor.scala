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

import java.time.LocalDate

import common.TaxDates
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import models._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object PurchaseDetailsConstructor {


  def getPurchaseDetailsSection(totalGainAnswersModel: TotalGainAnswersModel): Seq[QuestionAnswerModel[Any]] = {

    val useRebasedValues = !TaxDates.dateAfterStart(totalGainAnswersModel.acquisitionDateModel.get)
    val useWorthBeforeLegislationStart = TaxDates.dateBeforeLegislationStart(totalGainAnswersModel.acquisitionDateModel.get)

    val acquisitionDateData = acquisitionDateRow(totalGainAnswersModel)
    val howBecameOwnerData = howBecameOwnerRow(totalGainAnswersModel)
    val boughtForLessData = boughtForLessRow(totalGainAnswersModel)
    val payValuationLegislationStart = didYouPayValuationLegislationStart(totalGainAnswersModel)
    val costsAtLegislationStart = costsAtLegislationStartRow(totalGainAnswersModel)
    val acquisitionValueData = acquisitionValueRow(totalGainAnswersModel, useWorthBeforeLegislationStart)
    val acquisitionCostsData = acquisitionCostsRow(totalGainAnswersModel)
    val rebasedValueData = rebasedValueRow(totalGainAnswersModel.rebasedValueModel, useRebasedValues)
    val rebasedCostsQuestionData = rebasedCostsQuestionRow(totalGainAnswersModel.rebasedCostsModel, useRebasedValues)
    val rebasedCostsData = rebasedCostsRow(totalGainAnswersModel.rebasedCostsModel, useRebasedValues)

    val items = Seq(
      acquisitionDateData,
      howBecameOwnerData,
      boughtForLessData,
      acquisitionValueData,
      payValuationLegislationStart,
      costsAtLegislationStart,
      acquisitionCostsData,
      rebasedValueData,
      rebasedCostsQuestionData,
      rebasedCostsData
    )
    items.flatten
  }

  def acquisitionDateRow(totalGainAnswersModel: TotalGainAnswersModel): Option[QuestionAnswerModel[LocalDate]] = {
    Some(QuestionAnswerModel(
      s"${KeystoreKeys.acquisitionDate}",
      totalGainAnswersModel.acquisitionDateModel.get,
      Messages("calc.acquisitionDate.questionTwo"),
      Some(controllers.routes.AcquisitionDateController.acquisitionDate().url)
    ))
  }

  def howBecameOwnerRow(totalGainAnswersModel: TotalGainAnswersModel): Option[QuestionAnswerModel[String]] = {

    lazy val answer = totalGainAnswersModel.howBecameOwnerModel.get.gainedBy match {
      case "Bought" => Messages("calc.howBecameOwner.bought")
      case "Inherited" => Messages("calc.howBecameOwner.inherited")
      case _ => Messages("calc.howBecameOwner.gifted")
    }

    totalGainAnswersModel.howBecameOwnerModel match {
      case Some(_) => Some(QuestionAnswerModel(
        s"${KeystoreKeys.howBecameOwner}",
        answer,
        Messages("calc.howBecameOwner.question"),
        Some(controllers.routes.HowBecameOwnerController.howBecameOwner().url)
      ))
      case _ => None
    }
  }

  def boughtForLessRow(totalGainAnswersModel: TotalGainAnswersModel): Option[QuestionAnswerModel[Boolean]] = {
    totalGainAnswersModel.howBecameOwnerModel match {
      case Some(HowBecameOwnerModel("Bought")) => Some(QuestionAnswerModel(
        s"${KeystoreKeys.boughtForLess}",
        totalGainAnswersModel.boughtForLessModel.get.boughtForLess,
        Messages("calc.boughtForLess.question"),
        Some(controllers.routes.BoughtForLessController.boughtForLess().url)
      ))
      case _ => None
    }
  }

  def didYouPayValuationLegislationStart(totalGainAnswersModel: TotalGainAnswersModel): Option[QuestionAnswerModel[String]] = {

    val beforeLegislationStart = TaxDates.dateBeforeLegislationStart(totalGainAnswersModel.acquisitionDateModel.get)

    (totalGainAnswersModel.costsAtLegislationStart, beforeLegislationStart) match {
      case (Some(CostsAtLegislationStartModel(answer, _)), true) =>  Some(QuestionAnswerModel(
        KeystoreKeys.costAtLegislationStart,
        answer,
        Messages("calc.costsAtLegislationStart.title"),
        Some(controllers.routes.CostsAtLegislationStartController.costsAtLegislationStart().url)
      ))
      case _ => None
    }
  }

  def costsAtLegislationStartRow(totalGainAnswersModel: TotalGainAnswersModel): Option[QuestionAnswerModel[BigDecimal]] = {

    val beforeLegislationStart = TaxDates.dateBeforeLegislationStart(totalGainAnswersModel.acquisitionDateModel.get)

    (totalGainAnswersModel.costsAtLegislationStart, beforeLegislationStart) match {
      case (Some(CostsAtLegislationStartModel("Yes", Some(value))), true) => Some(QuestionAnswerModel(
        KeystoreKeys.costAtLegislationStartValue,
        value,
        Messages("calc.costsAtLegislationStart.howMuch"),
        Some(controllers.routes.CostsAtLegislationStartController.costsAtLegislationStart().url)
      ))
      case _ => None
    }
  }

  def acquisitionValueRow(totalGainAnswersModel: TotalGainAnswersModel, useWorthBeforeLegislationStart: Boolean): Option[QuestionAnswerModel[BigDecimal]] = {
    val question = (useWorthBeforeLegislationStart, totalGainAnswersModel.howBecameOwnerModel, totalGainAnswersModel.boughtForLessModel) match {
      case (true, _, _) => Messages("calc.worthBeforeLegislationStart.question")
      case (_, Some(HowBecameOwnerModel("Gifted")), _) => Messages("calc.worthWhenGiftedTo.question")
      case (_, Some(HowBecameOwnerModel("Inherited")), _) => Messages("calc.worthWhenInherited.question")
      case (_, _, Some(BoughtForLessModel(true))) => Messages("calc.worthWhenBoughtForLess.question")
      case _ => Messages("calc.acquisitionValue.question")
    }

    Some(QuestionAnswerModel(
      KeystoreKeys.acquisitionValue,
      totalGainAnswersModel.acquisitionValueModel.acquisitionValueAmt,
      question,
      Some(controllers.routes.AcquisitionValueController.acquisitionValue().url)
    ))
  }

  def acquisitionCostsRow(totalGainAnswersModel: TotalGainAnswersModel): Option[QuestionAnswerModel[BigDecimal]] = {

    val afterLegislationStart = !TaxDates.dateBeforeLegislationStart(totalGainAnswersModel.acquisitionDateModel.get)

    (totalGainAnswersModel.acquisitionCostsModel, afterLegislationStart) match{
    case (Some(AcquisitionCostsModel(_)), true) =>
    Some(QuestionAnswerModel(
      KeystoreKeys.acquisitionCosts,
      totalGainAnswersModel.acquisitionCostsModel.get.acquisitionCostsAmt,
      Messages("calc.acquisitionCosts.question"),
      Some(controllers.routes.AcquisitionCostsController.acquisitionCosts().url)
    ))
    case _ => None
  }
  }


  def rebasedValueRow(rebasedValueModel: Option[RebasedValueModel], useRebasedValues: Boolean): Option[QuestionAnswerModel[BigDecimal]] = {
    if (useRebasedValues) {
      Some(QuestionAnswerModel(
        KeystoreKeys.rebasedValue,
        rebasedValueModel.get.rebasedValueAmt,
        s"${Messages("calc.nonResident.rebasedValue.question")} ${Messages("calc.nonResident.rebasedValue.date")}",
        Some(controllers.routes.RebasedValueController.rebasedValue().url)
      ))
    }
    else None
  }

  def rebasedCostsQuestionRow(rebasedCostsModel: Option[RebasedCostsModel], useRebasedValues: Boolean): Option[QuestionAnswerModel[String]] = {
    if (useRebasedValues) {
      Some(QuestionAnswerModel(
        s"${KeystoreKeys.rebasedCosts}-question",
        rebasedCostsModel.get.hasRebasedCosts,
        Messages("calc.rebasedCosts.question"),
        Some(controllers.routes.RebasedCostsController.rebasedCosts().url)
      ))
    } else None
  }

  def rebasedCostsRow(rebasedCostsModel: Option[RebasedCostsModel], useRebasedValues: Boolean): Option[QuestionAnswerModel[BigDecimal]] = {
    (useRebasedValues, rebasedCostsModel) match {
      case (true, Some(RebasedCostsModel("Yes", Some(value)))) => Some(QuestionAnswerModel(
        KeystoreKeys.rebasedCosts,
        value,
        Messages("calc.rebasedCosts.questionTwo"),
        Some(controllers.routes.RebasedCostsController.rebasedCosts().url)
      ))
      case _ => None
    }
  }
}
