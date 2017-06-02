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

package controllers

import java.time.LocalDate

import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.{Dates, TaxDates}
import connectors.CalculatorConnector
import constructors.AnswersConstructor
import controllers.predicates.ValidActiveSession
import forms.PrivateResidenceReliefForm._
import views.html.calculation
import models._
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object PrivateResidenceReliefController extends PrivateResidenceReliefController {
  val calcConnector = CalculatorConnector
  val answersConstructor = AnswersConstructor
}

trait PrivateResidenceReliefController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  val answersConstructor: AnswersConstructor

  def getAcquisitionDate(implicit hc: HeaderCarrier): Future[Option[LocalDate]] =
    calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).map {
      case Some(AcquisitionDateModel(day, month, year)) => Some(Dates.constructDate(day, month, year))
      case _ => None
    }

  def getDisposalDate(implicit hc: HeaderCarrier): Future[Option[LocalDate]] =
    calcConnector.fetchAndGetFormData[DisposalDateModel](KeystoreKeys.disposalDate).map {
      case Some(data) => Some(Dates.constructDate(data.day, data.month, data.year))
      case _ => None
    }

  def displayAfterQuestion(disposalDate: Option[LocalDate], acquisitionDate: Option[LocalDate]): Boolean =
    (disposalDate, acquisitionDate) match {
      case (Some(dDate), Some(_)) if !TaxDates.dateAfterOctober(dDate) => false
      case (Some(dDate), Some(aDate)) if TaxDates.dateAfterOctober(dDate) && !TaxDates.dateAfterStart(aDate) => true
      case _ => false
    }

  def displayFirstQuestion(disposalDate: Option[LocalDate], acquisitionDate: Option[LocalDate]): Boolean =
    (disposalDate, acquisitionDate) match {
      case (Some(dDate), Some(aDate)) if !TaxDates.dateAfterOctober(dDate) || !dDate.minusMonths(18L).isAfter(aDate) => false
      case _ => true
    }

  def displayOnlyFlatCalculationQuestion(disposalDate: Option[LocalDate], acquisitionDate: Option[LocalDate]): Boolean =
    (disposalDate, acquisitionDate) match {
      case (Some(dDate), Some(aDate)) if TaxDates.dateAfterStart(aDate) && dDate.minusMonths(18L).isAfter(aDate) => true
      case _ => false
    }

  val privateResidenceRelief: Action[AnyContent] = ValidateSession.async { implicit request =>

    def action(disposalDate: Option[LocalDate], acquisitionDate: Option[LocalDate]) = {

      val showOnlyFlatQuestion = displayOnlyFlatCalculationQuestion(disposalDate, acquisitionDate)
      val showBeforeQuestion = displayFirstQuestion(disposalDate, acquisitionDate)
      val showBetweenQuestion = displayAfterQuestion(disposalDate, acquisitionDate)
      val disposalDateLess18Months = Dates.dateMinusMonths(disposalDate, 18)

      calcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](KeystoreKeys.privateResidenceRelief).map {
        case Some(data) => Ok(calculation.privateResidenceRelief(privateResidenceReliefForm(showBeforeQuestion,
          showBetweenQuestion).fill(data), showBetweenQuestion, showBeforeQuestion, disposalDateLess18Months, showOnlyFlatQuestion))
        case None => Ok(calculation.privateResidenceRelief(privateResidenceReliefForm(showBeforeQuestion, showBetweenQuestion),
          showBetweenQuestion, showBeforeQuestion, disposalDateLess18Months, showOnlyFlatQuestion))
      }
    }

    for {
      disposalDate <- getDisposalDate
      acquisitionDate <- getAcquisitionDate
      finalResult <- action(disposalDate, acquisitionDate)
    } yield finalResult
  }

  val submitPrivateResidenceRelief: Action[AnyContent] = ValidateSession.async { implicit request =>

    def action(disposalDate: Option[LocalDate], acquisitionDate: Option[LocalDate]): Future[Result] = {

      val showOnlyFlatQuestion = displayOnlyFlatCalculationQuestion(disposalDate, acquisitionDate)
      val showBeforeQuestion = displayFirstQuestion(disposalDate, acquisitionDate)
      val showBetweenQuestion = displayAfterQuestion(disposalDate, acquisitionDate)
      val disposalDateLess18Months = Dates.dateMinusMonths(disposalDate, 18)

      def checkTaxableGainsZeroOrLess(calculationResultsWithPRRModel: CalculationResultsWithPRRModel) = {
        val optionSeq = Seq(calculationResultsWithPRRModel.rebasedResult, calculationResultsWithPRRModel.timeApportionedResult).flatten
        val finalSeq = Seq(calculationResultsWithPRRModel.flatResult) ++ optionSeq

        Future.successful(finalSeq.forall(_.taxableGain <= 0))
      }

      def routeDestination(taxableGainsZeroOrLess: Boolean) = {
        if (taxableGainsZeroOrLess) Future.successful(Redirect(controllers.routes.CheckYourAnswersController.checkYourAnswers().url))
        else Future.successful(Redirect(controllers.routes.CurrentIncomeController.currentIncome().url))
      }

      def errorAction(form: Form[PrivateResidenceReliefModel]) = {
        Future.successful(BadRequest(calculation.privateResidenceRelief(form, showBetweenQuestion,
          showBeforeQuestion, disposalDateLess18Months, showOnlyFlatQuestion)))
      }

      def successAction(model: PrivateResidenceReliefModel) = {
        for {
          _ <- calcConnector.saveFormData(KeystoreKeys.privateResidenceRelief, model)
          answers <- answersConstructor.getNRTotalGainAnswers
          results <- calcConnector.calculateTaxableGainAfterPRR(answers, model)
          taxableGainsZeroOrLess <- checkTaxableGainsZeroOrLess(results.get)
          route <- routeDestination(taxableGainsZeroOrLess)
        } yield route
      }

      privateResidenceReliefForm(showBeforeQuestion, showBetweenQuestion).bindFromRequest.fold(errorAction, successAction)
    }

    for {
      disposalDate <- getDisposalDate
      acquisitionDate <- getAcquisitionDate
      finalResult <- action(disposalDate, acquisitionDate)
    } yield finalResult
  }
}
