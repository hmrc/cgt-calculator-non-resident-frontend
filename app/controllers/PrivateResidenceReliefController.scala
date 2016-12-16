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
import play.api.mvc.Result
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

  override val sessionTimeoutUrl = controllers.routes.SummaryController.restart().url
  override val homeLink = controllers.routes.DisposalDateController.disposalDate().url
  val calcConnector: CalculatorConnector
  val answersConstructor: AnswersConstructor

  def getAcquisitionDate(implicit hc: HeaderCarrier): Future[Option[LocalDate]] =
    calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).map {
      case Some(AcquisitionDateModel("Yes", Some(day), Some(month), Some(year))) => Some(Dates.constructDate(day, month, year))
      case _ => None
    }

  def getDisposalDate(implicit hc: HeaderCarrier): Future[Option[LocalDate]] =
    calcConnector.fetchAndGetFormData[DisposalDateModel](KeystoreKeys.disposalDate).map {
      case Some(data) => Some(Dates.constructDate(data.day, data.month, data.year))
      case _ => None
    }

  def getRebasedAmount(implicit hc: HeaderCarrier): Future[Boolean] =
    calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue).map {
      case Some(data) if data.rebasedValueAmt.isDefined => true
      case _ => false
    }

  def displayBetweenQuestion(disposalDate: Option[LocalDate], acquisitionDate: Option[LocalDate], hasRebasedValue: Boolean): Boolean =
    (disposalDate, acquisitionDate) match {
      case (Some(dDate), Some(aDate)) if TaxDates.dateAfterOctober(dDate) && !TaxDates.dateAfterStart(aDate) => true
      case (Some(dDate), None) if TaxDates.dateAfterOctober(dDate) && hasRebasedValue => true
      case _ => false
    }

  def displayBeforeQuestion(disposalDate: Option[LocalDate], acquisitionDate: Option[LocalDate]): Boolean =
    (disposalDate, acquisitionDate) match {
      case (Some(dDate), Some(aDate)) if TaxDates.dateAfterOctober(dDate) => true
      case (Some(dDate), Some(aDate)) if !TaxDates.dateAfterStart(aDate) => true
      case _ => false
    }

  val privateResidenceRelief = ValidateSession.async { implicit request =>

    def action(disposalDate: Option[LocalDate], acquisitionDate: Option[LocalDate], hasRebasedValue: Boolean) = {

      val showBetweenQuestion = displayBetweenQuestion(disposalDate, acquisitionDate, hasRebasedValue)
      val showBeforeQuestion = displayBeforeQuestion(disposalDate, acquisitionDate)
      val disposalDateLess18Months = Dates.dateMinusMonths(disposalDate, 18)

      calcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](KeystoreKeys.privateResidenceRelief).map {
        case Some(data) => Ok(calculation.privateResidenceRelief(privateResidenceReliefForm(showBeforeQuestion,
          showBetweenQuestion).fill(data), showBetweenQuestion, showBeforeQuestion, disposalDateLess18Months))
        case None => Ok(calculation.privateResidenceRelief(privateResidenceReliefForm(showBeforeQuestion, showBetweenQuestion),
          showBetweenQuestion, showBeforeQuestion, disposalDateLess18Months))
      }
    }

    for {
      disposalDate <- getDisposalDate
      acquisitionDate <- getAcquisitionDate
      hasRebasedValue <- getRebasedAmount
      finalResult <- action(disposalDate, acquisitionDate, hasRebasedValue)
    } yield finalResult
  }

  val submitPrivateResidenceRelief = ValidateSession.async { implicit request =>

    def action(disposalDate: Option[LocalDate], acquisitionDate: Option[LocalDate], hasRebasedValue: Boolean): Future[Result] = {
      val showBetweenQuestion = displayBetweenQuestion(disposalDate, acquisitionDate, hasRebasedValue)
      val showBeforeQuestion = displayBeforeQuestion(disposalDate, acquisitionDate)
      val disposalDateLess18Months = Dates.dateMinusMonths(disposalDate, 18)

      def checkTaxableGainsZeroOrLess(calculationResultsWithPRRModel: CalculationResultsWithPRRModel) = {
        val optionSeq = Seq(calculationResultsWithPRRModel.rebasedResult, calculationResultsWithPRRModel.timeApportionedResult).flatten
        val finalSeq = Seq(calculationResultsWithPRRModel.flatResult) ++ optionSeq

        Future.successful(finalSeq.forall(_.taxableGain <= 0))
      }

      def routeDestination(taxableGainsZeroOrLess: Boolean) = {
        if (taxableGainsZeroOrLess) Future.successful(Redirect(controllers.routes.CheckYourAnswersController.checkYourAnswers().url))
        else Future.successful(Redirect(controllers.routes.CustomerTypeController.customerType().url))
      }

      def errorAction(form: Form[PrivateResidenceReliefModel]) = {
        Future.successful(BadRequest(calculation.privateResidenceRelief(form, showBetweenQuestion,
          showBeforeQuestion, disposalDateLess18Months)))
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
      hasRebasedValue <- getRebasedAmount
      finalResult <- action(disposalDate, acquisitionDate, hasRebasedValue)
    } yield finalResult
  }
}
