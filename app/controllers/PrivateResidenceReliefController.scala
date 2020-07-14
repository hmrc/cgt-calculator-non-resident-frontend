/*
 * Copyright 2020 HM Revenue & Customs
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
import common.nonresident.TaxableGainCalculation.{checkGainExists, getPropertyLivedInResponse}
import common.{Dates, TaxDates}
import config.ApplicationConfig
import connectors.CalculatorConnector
import constructors.AnswersConstructor
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.PrivateResidenceReliefForm._
import javax.inject.Inject
import models._
import play.api.Application
import play.api.data.Form
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PrivateResidenceReliefController @Inject()(http: DefaultHttpClient,calcConnector: CalculatorConnector,
                                                 answersConstructor: AnswersConstructor,
                                                 mcc: MessagesControllerComponents)(implicit val applicationConfig: ApplicationConfig,
                                                                                    implicit val application: Application)
                                                  extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  def getAcquisitionDate(implicit hc: HeaderCarrier): Future[Option[LocalDate]] =
    calcConnector.fetchAndGetFormData[DateModel](KeystoreKeys.acquisitionDate).map {
      case Some(DateModel(day, month, year)) => Some(Dates.constructDate(day, month, year))
      case _ => None
    }

  def getDisposalDate(implicit hc: HeaderCarrier): Future[Option[LocalDate]] =
    calcConnector.fetchAndGetFormData[DateModel](KeystoreKeys.disposalDate).map {
      case Some(data) => Some(Dates.constructDate(data.day, data.month, data.year))
      case _ => None
    }

  def displayAfterQuestion(disposalDate: Option[LocalDate], acquisitionDate: Option[LocalDate]): Boolean =
    (disposalDate, acquisitionDate) match {
      case (Some(dDate), Some(_)) if !TaxDates.dateAfterOctober(dDate) => false
      case (Some(dDate), Some(aDate)) if TaxDates.dateAfterOctober(dDate) && !TaxDates.dateAfterStart(aDate) => true
      case _ => false
    }

  def displayFirstQuestion(disposalDate: Option[LocalDate], acquisitionDate: Option[LocalDate]): Boolean = {

    val pRRDateDetails = TaxDates.privateResidenceReliefMonthDeductionApplicable(disposalDate)

    (disposalDate, acquisitionDate) match {
      case (Some(dDate), Some(aDate)) if !TaxDates.dateAfterOctober(dDate) || !dDate.minusMonths(pRRDateDetails.months).isAfter(aDate) => false
      case _ => true
    }
  }

  def displayOnlyFlatCalculationQuestion(disposalDate: Option[LocalDate], acquisitionDate: Option[LocalDate]): Boolean = {

    val pRRDateDetails = TaxDates.privateResidenceReliefMonthDeductionApplicable(disposalDate)

    (disposalDate, acquisitionDate) match {
      case (Some(dDate), Some(aDate)) if TaxDates.dateAfterStart(aDate) && dDate.minusMonths(pRRDateDetails.months).isAfter(aDate) => true
      case _ => false
    }
  }

  val privateResidenceRelief: Action[AnyContent] = ValidateSession.async { implicit request =>
    implicit val lang: Lang  = mcc.messagesApi.preferred(request).lang

    def action(disposalDate: Option[LocalDate], acquisitionDate: Option[LocalDate]) = {

      val pRRDateDetails = TaxDates.privateResidenceReliefMonthDeductionApplicable(disposalDate)

      val showOnlyFlatQuestion = displayOnlyFlatCalculationQuestion(disposalDate, acquisitionDate)
      val showFirstQuestion = displayFirstQuestion(disposalDate, acquisitionDate)
      val showBetweenQuestion = displayAfterQuestion(disposalDate, acquisitionDate)
      val disposalDateLessMonths = Dates.dateMinusMonths(disposalDate, pRRDateDetails.months)

      calcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](KeystoreKeys.privateResidenceRelief).map {
        case Some(data) => Ok(calculation.privateResidenceRelief(privateResidenceReliefForm(showFirstQuestion,
          showBetweenQuestion).fill(data), showBetweenQuestion, showFirstQuestion, disposalDateLessMonths, pRRDateDetails.months, showOnlyFlatQuestion))
        case None => Ok(calculation.privateResidenceRelief(privateResidenceReliefForm(showFirstQuestion, showBetweenQuestion),
          showBetweenQuestion, showFirstQuestion, disposalDateLessMonths, pRRDateDetails.months, showOnlyFlatQuestion))
      }
    }

    (for {
      disposalDate <- getDisposalDate
      acquisitionDate <- getAcquisitionDate
      finalResult <- action(disposalDate, acquisitionDate)
    } yield finalResult).recoverToStart
  }

  val submitPrivateResidenceRelief: Action[AnyContent] = ValidateSession.async { implicit request =>

    def action(disposalDate: Option[LocalDate], acquisitionDate: Option[LocalDate]): Future[Result] = {

      val showOnlyFlatQuestion = displayOnlyFlatCalculationQuestion(disposalDate, acquisitionDate)
      val showFirstQuestion = displayFirstQuestion(disposalDate, acquisitionDate)
      val showBetweenQuestion = displayAfterQuestion(disposalDate, acquisitionDate)

      val pRRDateDetails = TaxDates.privateResidenceReliefMonthDeductionApplicable(disposalDate)

      val disposalDateLessMonths = Dates.dateMinusMonths(disposalDate, pRRDateDetails.months)

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
        implicit val lang: Lang = mcc.messagesApi.preferred(request).lang
        Future.successful(BadRequest(calculation.privateResidenceRelief(form, showBetweenQuestion,
          showFirstQuestion, disposalDateLessMonths, 0, showOnlyFlatQuestion)))
      }

      def successAction(model: PrivateResidenceReliefModel) = {
        (for {
          _ <- calcConnector.saveFormData(KeystoreKeys.privateResidenceRelief, model)
          answers <- answersConstructor.getNRTotalGainAnswers
          totalGainResultsModel <- calcConnector.calculateTotalGain(answers)
          gainExists <- checkGainExists(totalGainResultsModel.get)
          propertyLivedIn <- getPropertyLivedInResponse(gainExists, calcConnector)
          results <- calcConnector.calculateTaxableGainAfterPRR(answers, model, propertyLivedIn.get)
          taxableGainsZeroOrLess <- checkTaxableGainsZeroOrLess(results.get)
          route <- routeDestination(taxableGainsZeroOrLess)
        } yield route).recoverToStart
      }

      privateResidenceReliefForm(showFirstQuestion, showBetweenQuestion).bindFromRequest.fold(errorAction, successAction)
    }

    (for {
      disposalDate <- getDisposalDate
      acquisitionDate <- getAcquisitionDate
      finalResult <- action(disposalDate, acquisitionDate)
    } yield finalResult).recoverToStart
  }
}
