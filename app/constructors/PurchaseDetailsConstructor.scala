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
import common.TaxDates
import models._

import java.time.LocalDate

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
      "calc.acquisitionDate.questionTwo",
      Some(controllers.routes.AcquisitionDateController.acquisitionDate.url)
    ))
  }

  def howBecameOwnerRow(totalGainAnswersModel: TotalGainAnswersModel): Option[QuestionAnswerModel[String]] = {

    lazy val answer = totalGainAnswersModel.howBecameOwnerModel.get.gainedBy match {
      case "Bought" => "calc.howBecameOwner.bought"
      case "Inherited" => "calc.howBecameOwner.inherited"
      case _ => "calc.howBecameOwner.gifted"
    }

    totalGainAnswersModel.howBecameOwnerModel match {
      case Some(_) => Some(QuestionAnswerModel(
        s"${KeystoreKeys.howBecameOwner}",
        answer,
        "calc.howBecameOwner.question",
        Some(controllers.routes.HowBecameOwnerController.howBecameOwner.url)
      ))
      case _ => None
    }
  }

  def boughtForLessRow(totalGainAnswersModel: TotalGainAnswersModel): Option[QuestionAnswerModel[Boolean]] = {
    totalGainAnswersModel.howBecameOwnerModel match {
      case Some(HowBecameOwnerModel("Bought")) => Some(QuestionAnswerModel(
        s"${KeystoreKeys.boughtForLess}",
        totalGainAnswersModel.boughtForLessModel.get.boughtForLess,
        "calc.boughtForLess.question",
        Some(controllers.routes.BoughtForLessController.boughtForLess.url)
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
        "calc.costsAtLegislationStart.title",
        Some(controllers.routes.CostsAtLegislationStartController.costsAtLegislationStart.url)
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
        "calc.costsAtLegislationStart.howMuch",
        Some(controllers.routes.CostsAtLegislationStartController.costsAtLegislationStart.url)
      ))
      case _ => None
    }
  }

  def acquisitionValueRow(totalGainAnswersModel: TotalGainAnswersModel, useWorthBeforeLegislationStart: Boolean): Option[QuestionAnswerModel[BigDecimal]] = {
    val question = (useWorthBeforeLegislationStart, totalGainAnswersModel.howBecameOwnerModel, totalGainAnswersModel.boughtForLessModel) match {
      case (true, _, _) => "calc.worthBeforeLegislationStart.question"
      case (_, Some(HowBecameOwnerModel("Gifted")), _) => "calc.worthWhenGiftedTo.question"
      case (_, Some(HowBecameOwnerModel("Inherited")), _) => "calc.worthWhenInherited.question"
      case (_, _, Some(BoughtForLessModel(true))) => "calc.worthWhenBoughtForLess.question"
      case _ => "calc.acquisitionValue.question"
    }

    Some(QuestionAnswerModel(
      KeystoreKeys.acquisitionValue,
      totalGainAnswersModel.acquisitionValueModel.acquisitionValueAmt,
      question,
      Some(controllers.routes.AcquisitionValueController.acquisitionValue.url)
    ))
  }

  def acquisitionCostsRow(totalGainAnswersModel: TotalGainAnswersModel): Option[QuestionAnswerModel[BigDecimal]] = {

    val afterLegislationStart = !TaxDates.dateBeforeLegislationStart(totalGainAnswersModel.acquisitionDateModel.get)

    (totalGainAnswersModel.acquisitionCostsModel, afterLegislationStart) match{
    case (Some(AcquisitionCostsModel(_)), true) =>
    Some(QuestionAnswerModel(
      KeystoreKeys.acquisitionCosts,
      totalGainAnswersModel.acquisitionCostsModel.get.acquisitionCostsAmt,
      "calc.acquisitionCosts.question",
      Some(controllers.routes.AcquisitionCostsController.acquisitionCosts.url)
    ))
    case _ => None
  }
  }


  def rebasedValueRow(rebasedValueModel: Option[RebasedValueModel], useRebasedValues: Boolean): Option[QuestionAnswerModel[BigDecimal]] = {
    if (useRebasedValues) {
      Some(QuestionAnswerModel(
        KeystoreKeys.rebasedValue,
        rebasedValueModel.get.rebasedValueAmt,
        s"${"calc.nonResident.rebasedValue.questionAndDate"}",
        Some(controllers.routes.RebasedValueController.rebasedValue.url)
      ))
    }
    else None
  }

  def rebasedCostsQuestionRow(rebasedCostsModel: Option[RebasedCostsModel], useRebasedValues: Boolean): Option[QuestionAnswerModel[String]] = {
    if (useRebasedValues) {
      Some(QuestionAnswerModel(
        s"${KeystoreKeys.rebasedCosts}-question",
        rebasedCostsModel.get.hasRebasedCosts,
        "calc.rebasedCosts.question",
        Some(controllers.routes.RebasedCostsController.rebasedCosts.url)
      ))
    } else None
  }

  def rebasedCostsRow(rebasedCostsModel: Option[RebasedCostsModel], useRebasedValues: Boolean): Option[QuestionAnswerModel[BigDecimal]] = {
    (useRebasedValues, rebasedCostsModel) match {
      case (true, Some(RebasedCostsModel("Yes", Some(value)))) => Some(QuestionAnswerModel(
        KeystoreKeys.rebasedCosts,
        value,
        "calc.rebasedCosts.questionTwo",
        Some(controllers.routes.RebasedCostsController.rebasedCosts.url)
      ))
      case _ => None
    }
  }
}
