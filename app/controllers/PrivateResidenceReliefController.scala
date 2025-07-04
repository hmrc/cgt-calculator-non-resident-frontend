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

package controllers

import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.nonresident.TaxableGainCalculation.{checkGainExists, getPropertyLivedInResponse}
import common.{Dates, TaxDates}
import connectors.CalculatorConnector
import constructors.AnswersConstructor
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.PrivateResidenceReliefForm._
import models._
import play.api.data.Form
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.calculation.{privateResidenceRelief, privateResidenceReliefValue}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.chaining.scalaUtilChainingOps

class PrivateResidenceReliefController @Inject()(calcConnector: CalculatorConnector,
                                                 sessionCacheService: SessionCacheService,
                                                 answersConstructor: AnswersConstructor,
                                                 mcc: MessagesControllerComponents,
                                                 privateResidenceReliefView: privateResidenceRelief,
                                                 privateResidenceReliefValueView: privateResidenceReliefValue)
                                                (implicit ec: ExecutionContext)
                                                  extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  private def modelToDate(model: DateModel) = Dates.constructDate(model.day, model.month, model.year)

  def getAcquisitionDate(implicit request: Request[?]): Future[Option[LocalDate]] =
    sessionCacheService.fetchAndGetFormData[DateModel](KeystoreKeys.acquisitionDate).map(_.map(modelToDate))

  def getDisposalDate(implicit request: Request[?]): Future[Option[LocalDate]] =
    sessionCacheService.fetchAndGetFormData[DateModel](KeystoreKeys.disposalDate).map(_.map(modelToDate))

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

  def privateResidenceRelief: Action[AnyContent] = ValidateSession.async { implicit request =>
      sessionCacheService.fetchAndGetFormData[ClaimingPrrModel](KeystoreKeys.privateResidenceRelief).map {
        case Some(data) => Ok(privateResidenceReliefView(isClaimingPrrForm.fill(data)))
        case None => Ok(privateResidenceReliefView(isClaimingPrrForm))
      }.recoverToStart
  }

  def privateResidenceReliefValue: Action[AnyContent] = ValidateSession.async { implicit request =>
    implicit val lang: Lang  = mcc.messagesApi.preferred(request).lang
      sessionCacheService.fetchAndGetFormData[PrivateResidenceReliefModel](KeystoreKeys.privateResidenceRelief).map {
        case Some(data) => Ok(privateResidenceReliefValueView(privateResidenceReliefForm.fill(data)))
        case None => Ok(privateResidenceReliefValueView(privateResidenceReliefForm))
      }.recoverToStart
  }

  private def checkTaxableGainsZeroOrLess(calculationResultsWithPRRModel: CalculationResultsWithPRRModel) = {
    val optionSeq = Seq(calculationResultsWithPRRModel.rebasedResult, calculationResultsWithPRRModel.timeApportionedResult).flatten
    val finalSeq = Seq(calculationResultsWithPRRModel.flatResult) ++ optionSeq

    Future.successful(finalSeq.forall(_.taxableGain <= 0))
  }

  private def saveAndRedirect(model: PrivateResidenceReliefModel)(implicit request: Request[?]) = {
    for {
      _ <- sessionCacheService.saveFormData(KeystoreKeys.privateResidenceRelief, model)
      answers <- answersConstructor.getNRTotalGainAnswers
      totalGainResultsModel <- calcConnector.calculateTotalGain(answers)
      gainExists <- checkGainExists(totalGainResultsModel.get)
      propertyLivedIn <- getPropertyLivedInResponse(gainExists, sessionCacheService)
      results <- calcConnector.calculateTaxableGainAfterPRR(answers, model, propertyLivedIn.get)
      taxableGainsZeroOrLess <- checkTaxableGainsZeroOrLess(results.get)
    } yield
      if (taxableGainsZeroOrLess) Redirect(controllers.routes.CheckYourAnswersController.checkYourAnswers.url)
      else Redirect(controllers.routes.CurrentIncomeController.currentIncome.url)
  }

  def submitPrivateResidenceRelief: Action[AnyContent] = ValidateSession.async { implicit request =>
      isClaimingPrrForm.bindFromRequest().fold(Left(_), Right(_)).pipe {
        case Left(form) =>
          Future.successful(BadRequest(privateResidenceReliefView(form)))
        case Right(model) =>
          if (model.isClaimingPRR == "Yes")
            Future.successful(Redirect(controllers.routes.PrivateResidenceReliefController.submitprivateResidenceReliefValue.url))
          else {
            val model = PrivateResidenceReliefModel("No", None)
            saveAndRedirect(model)
          }
      }.recoverToStart
  }

  def submitprivateResidenceReliefValue: Action[AnyContent] = ValidateSession.async { implicit request =>
      def errorAction(form: Form[PrivateResidenceReliefModel]) = {
        implicit val lang: Lang = mcc.messagesApi.preferred(request).lang
        Future.successful(BadRequest(privateResidenceReliefValueView(form)))
      }

      privateResidenceReliefForm.bindFromRequest().fold(errorAction, saveAndRedirect).recoverToStart
  }
}
