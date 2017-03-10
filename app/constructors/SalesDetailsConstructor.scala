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

import models.{QuestionAnswerModel, SoldForLessModel, TotalGainAnswersModel}
import common.Dates
import common.KeystoreKeys.{NonResidentKeys => keys}
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object SalesDetailsConstructor {

  def salesDetailsRows(answers: TotalGainAnswersModel): Seq[QuestionAnswerModel[Any]] = {
    val disposalDate = disposalDateRow(answers)
    val disposalValue = disposalValueRow(answers)
    val disposalCosts = disposalCostsRow(answers)
    val soldOrGivenAway = soldOrGivenAwayRow(answers)
    val soldForLess = soldForLessRow(answers)

    val items = Seq(disposalDate, soldOrGivenAway, soldForLess, disposalValue, disposalCosts)

    items.flatten
  }

  def disposalDateRow(answers: TotalGainAnswersModel): Option[QuestionAnswerModel[LocalDate]] = {
    val dateModel = answers.disposalDateModel
    val date = Dates.constructDate(dateModel.day, dateModel.month, dateModel.year)

    Some(QuestionAnswerModel[LocalDate](keys.disposalDate,
      date,
      Messages("calc.disposalDate.question"),
      Some(controllers.routes.DisposalDateController.disposalDate().url)))
  }

  def soldOrGivenAwayRow(answers: TotalGainAnswersModel): Option[QuestionAnswerModel[String]] = {
    if (answers.soldOrGivenAwayModel.soldIt) {
      Some(QuestionAnswerModel[String](keys.soldOrGivenAway,
        Messages("calc.soldOrGivenAway.sold"),
        Messages("calc.soldOrGivenAway.question"),
        Some(controllers.routes.SoldOrGivenAwayController.soldOrGivenAway().url)
      ))
    }
    else {
      Some(QuestionAnswerModel[String](keys.soldOrGivenAway,
        Messages("calc.soldOrGivenAway.gave"),
        Messages("calc.soldOrGivenAway.question"),
        Some(controllers.routes.SoldOrGivenAwayController.soldOrGivenAway().url)
      ))
    }
  }

  def soldForLessRow(answers: TotalGainAnswersModel): Option[QuestionAnswerModel[Boolean]] = {
    if (answers.soldOrGivenAwayModel.soldIt) {
      Some(QuestionAnswerModel[Boolean](keys.soldForLess,
        answers.soldForLessModel.get.soldForLess,
        Messages("calc.nonResident.soldForLess.question"),
        Some(controllers.routes.SoldForLessController.soldForLess().url)
      ))
    }
    else None
  }

  def disposalValueRow(answers: TotalGainAnswersModel): Option[QuestionAnswerModel[BigDecimal]] = {

    val question = (answers.soldOrGivenAwayModel.soldIt, answers.soldForLessModel) match {
      case (true, Some(SoldForLessModel(true))) => Messages("calc.marketValue.sold.question")
      case (true, Some(_)) => Messages("calc.disposalValue.question")
      case _ => Messages("calc.marketValue.gaveItAway.question")
    }

    val route = (answers.soldOrGivenAwayModel.soldIt, answers.soldForLessModel) match {
      case (true, Some(SoldForLessModel(true))) =>
        controllers.routes.MarketValueWhenSoldOrGaveAwayController.marketValueWhenSold().url
      case (true, Some(_)) => controllers.routes.DisposalValueController.disposalValue().url
      case _ => controllers.routes.MarketValueWhenSoldOrGaveAwayController.marketValueWhenGaveAway().url
    }

    Some(QuestionAnswerModel[BigDecimal](keys.disposalValue,
      answers.disposalValueModel.disposalValue,
      question,
      Some(route)))
  }

  def disposalCostsRow(answers: TotalGainAnswersModel): Option[QuestionAnswerModel[BigDecimal]] = {
    Some(QuestionAnswerModel[BigDecimal](keys.disposalCosts,
      answers.disposalCostsModel.disposalCosts,
      Messages("calc.disposalCosts.question"),
      Some(controllers.routes.DisposalCostsController.disposalCosts().url)))
  }
}
