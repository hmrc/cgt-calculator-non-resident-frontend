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
import common.TaxDates
import connectors.CalculatorConnector
import constructors.AnswersConstructor
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.ImprovementsForm._
import forms.IsClaimingImprovementsForm._
import models._
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.calculation.{isClaimingImprovements, improvements, improvementsRebased}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ImprovementsController @Inject()(calcConnector: CalculatorConnector,
                                       sessionCacheService: SessionCacheService,
                                       answersConstructor: AnswersConstructor,
                                       mcc: MessagesControllerComponents,
                                       improvementsView: improvements,
                                       improvementsRebasedView: improvementsRebased,
                                       isClaimingImprovementsView: isClaimingImprovements
                                      )(implicit ec: ExecutionContext)
                                        extends FrontendController(mcc) with ValidActiveSession with I18nSupport{

  private def fetchAcquisitionDate(implicit request: Request[?]): Future[Option[DateModel]] = {
    sessionCacheService.fetchAndGetFormData[DateModel](KeystoreKeys.acquisitionDate)
  }

  private def fetchIsClaimingImprovements(implicit request: Request[?]): Future[Option[IsClaimingImprovementsModel]] = {
    sessionCacheService.fetchAndGetFormData[IsClaimingImprovementsModel](KeystoreKeys.isClaimingImprovements)
  }

  private def fetchImprovements(implicit request: Request[?]): Future[Option[ImprovementsModel]] = {
    sessionCacheService.fetchAndGetFormData[ImprovementsModel](KeystoreKeys.improvements)
  }

  private def ownerBeforeLegislationStartCheck(model: Option[DateModel]): Future[Boolean] = {
    if(TaxDates.dateBeforeLegislationStart(model.get.day, model.get.month, model.get.year)) Future.successful(true)
    else Future.successful(false)
  }

  private def routeBasedOnGains(totalGainResultsModel: Option[TotalGainResultsModel]): Result =
    totalGainResultsModel match {
      case Some(model) =>
        val totalGainResults: Seq[BigDecimal] = Seq(model.flatGain) ++ Seq(model.rebasedGain, model.timeApportionedGain).flatten
        if (!totalGainResults.forall(_ <= 0)) Redirect(routes.PropertyLivedInController.propertyLivedIn)
        else Redirect(routes.CheckYourAnswersController.checkYourAnswers)
      case None => Redirect(common.DefaultRoutes.missingDataRoute)
    }

  val getIsClaimingImprovements: Action[AnyContent] = ValidateSession.async { implicit request =>
    def routeRequest(isClaimingImprovementsModel: Option[IsClaimingImprovementsModel], ownerBeforeLegislationStart: Boolean): Future[Result] = {
      val form = isClaimingImprovementsModel match {
        case Some(data) => isClaimingImprovementsForm.fill(data)
        case _ => isClaimingImprovementsForm
      }
      Future.successful(Ok(isClaimingImprovementsView(form, ownerBeforeLegislationStart)))
    }

    (for {
      acquisitionDate <- fetchAcquisitionDate(using request)
      isClaimingImprovements <- fetchIsClaimingImprovements(using request)
      ownerBeforeLegislationStart <- ownerBeforeLegislationStartCheck(acquisitionDate)
      route <- routeRequest(isClaimingImprovements, ownerBeforeLegislationStart)
    } yield route).recoverToStart
  }

  val submitIsClaimingImprovements: Action[AnyContent] = ValidateSession.async { implicit request =>

    def unsetImprovementsValues(oldValues: Option[ImprovementsModel]): Future[Unit] = oldValues match {
      case Some(_) => sessionCacheService.unsetData(KeystoreKeys.improvements)
      case _ => Future.unit
    }

    def errorAction(errors: Form[IsClaimingImprovementsModel], ownerBeforeLegislationStart: Boolean): Future[Result] =
      Future.successful(BadRequest(isClaimingImprovementsView(errors, ownerBeforeLegislationStart)))

    def routeRequest(model: IsClaimingImprovementsModel, isAfterTaxStart: Boolean, gains: Option[TotalGainResultsModel]): Result = {
      if (model.isClaimingImprovements && !isAfterTaxStart) Redirect(routes.ImprovementsController.improvementsRebased)
      else if (model.isClaimingImprovements) Redirect(routes.ImprovementsController.improvements)
      else routeBasedOnGains(gains)
    }

    def successAction(model: IsClaimingImprovementsModel, isAfterTaxStart: Boolean): Future[Result] = {
      for {
        _ <- sessionCacheService.saveFormData(KeystoreKeys.isClaimingImprovements, model)
        oldValues <- sessionCacheService.fetchAndGetFormData[ImprovementsModel](KeystoreKeys.improvements)
        _ <- unsetImprovementsValues(oldValues)
        allAnswersModel <- answersConstructor.getNRTotalGainAnswers
        gains <- calcConnector.calculateTotalGain(allAnswersModel)
      } yield routeRequest(model, isAfterTaxStart, gains)
    }

    for {
      acquisitionDate <- fetchAcquisitionDate(using request)
      ownerBeforeLegislationStart <- ownerBeforeLegislationStartCheck(acquisitionDate)
      route <- isClaimingImprovementsForm.bindFromRequest().fold(
        errors => errorAction(errors, ownerBeforeLegislationStart),
        success => successAction(success, TaxDates.dateAfterStart(acquisitionDate))
      )
    } yield route
  }

  val improvements: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(improvementsModel: Option[ImprovementsModel]): Future[Result] = {
      improvementsModel match {
        case Some(data) =>
          Future.successful(Ok(improvementsView(improvementsForm(false).fill(data))))
        case None =>
          Future.successful(Ok(improvementsView(improvementsForm(false))))
      }
    }

    (for {
      improvements <- fetchImprovements(using request)
      route <- routeRequest(improvements)
    } yield route).recoverToStart
  }

  val submitImprovements: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(errors: Form[ImprovementsModel]) = {
      Future.successful(BadRequest(improvementsView(errors)))
    }

    def successAction(improvements: ImprovementsModel): Future[Result] = {
      (for {
        _ <- sessionCacheService.saveFormData(KeystoreKeys.improvements, improvements)
        allAnswersModel <- answersConstructor.getNRTotalGainAnswers
        gains <- calcConnector.calculateTotalGain(allAnswersModel)
      } yield routeBasedOnGains(gains)).recoverToStart
    }

    improvementsForm(false).bindFromRequest().fold(
      errors => errorAction(errors),
      success => successAction(success)
    )
  }

  val improvementsRebased: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(improvementsRebasedModel: Option[ImprovementsModel]): Future[Result] = {
      val form = improvementsRebasedModel match {
        case Some(data) => improvementsForm(true).fill(data)
        case None => improvementsForm(true)
      }
      Future.successful(Ok(improvementsRebasedView(form)))
    }

    (for {
      improvements <- fetchImprovements(using request)
      route <- routeRequest(improvements)
    } yield route).recoverToStart
  }

  val submitImprovementsRebased: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(errors: Form[ImprovementsModel]): Future[Result] = {
      Future.successful(BadRequest(improvementsRebasedView(errors)))
    }

    def successAction(improvementsRebased: ImprovementsModel): Future[Result] = {
      (for {
        _ <- sessionCacheService.saveFormData[ImprovementsModel](KeystoreKeys.improvements, improvementsRebased)
        allAnswersModel <- answersConstructor.getNRTotalGainAnswers
        gains <- calcConnector.calculateTotalGain(allAnswersModel)
      } yield routeBasedOnGains(gains)).recoverToStart
    }

    improvementsForm(true).bindFromRequest().fold(
      errors => errorAction(errors),
      success => successAction(success)
    )
  }

}
