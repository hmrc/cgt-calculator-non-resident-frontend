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
import views.html.calculation.{improvements, isClaimingImprovements}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ImprovementsController @Inject()(calcConnector: CalculatorConnector,
                                       sessionCacheService: SessionCacheService,
                                       answersConstructor: AnswersConstructor,
                                       mcc: MessagesControllerComponents,
                                       improvementsView: improvements,
                                       isClaimingImprovementsView: isClaimingImprovements)(implicit ec: ExecutionContext)
                                        extends FrontendController(mcc) with ValidActiveSession with I18nSupport{

  private def improvementsBackUrl(acquisitionDate: Option[DateModel]): Future[String] = {
    acquisitionDate match {
      case Some(data) if TaxDates.dateAfterStart(data.get) =>
        Future.successful(routes.AcquisitionCostsController.acquisitionCosts.url)
      case Some(_) => Future.successful(routes.RebasedCostsController.rebasedCosts.url)
      case _ => Future.successful(common.DefaultRoutes.missingDataRoute)
    }
  }

  private def fetchAcquisitionDate(implicit request: Request[_]): Future[Option[DateModel]] = {
    sessionCacheService.fetchAndGetFormData[DateModel](KeystoreKeys.acquisitionDate)
  }

  private def fetchIsClaimingImprovements(implicit request: Request[_]): Future[Option[IsClaimingImprovementsModel]] = {
    sessionCacheService.fetchAndGetFormData[IsClaimingImprovementsModel](KeystoreKeys.isClaimingImprovements)
  }

  private def fetchImprovements(implicit request: Request[_]): Future[Option[ImprovementsModel]] = {
    sessionCacheService.fetchAndGetFormData[ImprovementsModel](KeystoreKeys.improvements)
  }

  private def displayImprovementsSectionCheck(acquisitionDateModel: Option[DateModel]): Future[Boolean] = {
    acquisitionDateModel match {
      case Some(data) if !TaxDates.dateAfterStart(data.get) =>
        Future.successful(true)
      case _ => Future.successful(false)
    }
  }

  private def ownerBeforeLegislationStartCheck(model: Option[DateModel]): Future[Boolean] = {
    if(TaxDates.dateBeforeLegislationStart(model.get.day, model.get.month, model.get.year)) Future.successful(true)
    else Future.successful(false)
  }

  val getIsClaimingImprovements: Action[AnyContent] = ValidateSession.async { implicit request =>
    def routeRequest(isClaimingImprovementsModel: Option[IsClaimingImprovementsModel], ownerBeforeLegislationStart: Boolean): Future[Result] = {
      isClaimingImprovementsModel match {
        case Some(data) =>
          Future.successful(Ok(isClaimingImprovementsView(isClaimingImprovementsForm.fill(data), ownerBeforeLegislationStart)))
        case _ =>
          Future.successful(Ok(isClaimingImprovementsView(isClaimingImprovementsForm, ownerBeforeLegislationStart)))
      }
    }

    (for {
      acquisitionDate <- fetchAcquisitionDate(request)
      isClaimingImprovements <- fetchIsClaimingImprovements(request)
      ownerBeforeLegislationStart <- ownerBeforeLegislationStartCheck(acquisitionDate)
      route <- routeRequest(isClaimingImprovements, ownerBeforeLegislationStart)
    } yield route).recoverToStart
  }

  val improvements: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String, improvementsModel: Option[ImprovementsModel], improvementsOptions: Boolean,
                     ownerBeforeLegislationStart: Boolean): Future[Result] = {
      improvementsModel match {
        case Some(data) =>
          Future.successful(Ok(improvementsView(improvementsForm(improvementsOptions).fill(data),
            improvementsOptions, backUrl, ownerBeforeLegislationStart)))
        case None =>
          Future.successful(Ok(improvementsView(improvementsForm(improvementsOptions),
            improvementsOptions, backUrl, ownerBeforeLegislationStart)))
      }
    }

    (for {
      acquisitionDate <- fetchAcquisitionDate(request)
      improvements <- fetchImprovements(request)
      improvementsOptions <- displayImprovementsSectionCheck(acquisitionDate)
      backUrl <- improvementsBackUrl(acquisitionDate)
      ownerBeforeLegislationStart <- ownerBeforeLegislationStartCheck(acquisitionDate)
      route <- routeRequest(backUrl, improvements, improvementsOptions, ownerBeforeLegislationStart)
    } yield route).recoverToStart
  }

  val submitIsClaimingImprovements: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(errors: Form[IsClaimingImprovementsModel], ownerBeforeLegislationStart: Boolean): Future[Result] =
      Future.successful(BadRequest(isClaimingImprovementsView(errors, ownerBeforeLegislationStart)))

    def routeRequest(model: IsClaimingImprovementsModel): Future[Result] = {
      if (model.isClaimingImprovements) Future.successful(Redirect(routes.ImprovementsController.improvements))
      else Future.successful(Redirect(routes.DisposalValueController.disposalValue))
    }

    def successAction(model: IsClaimingImprovementsModel) = {
      (for {
        _ <- sessionCacheService.saveFormData(KeystoreKeys.isClaimingImprovements, model)
        route <- routeRequest(model)
      } yield route).recoverToStart
    }

    (for {
      acquisitionDate <- fetchAcquisitionDate(request)
      ownerBeforeLegislationStart <- ownerBeforeLegislationStartCheck(acquisitionDate)
      route <- isClaimingImprovementsForm.bindFromRequest().fold(
        errors => errorAction(errors, ownerBeforeLegislationStart),
        success => successAction(success)
      )
    } yield route).recoverToStart
  }

  val submitImprovements: Action[AnyContent] = ValidateSession.async { implicit request =>

    def successRouteRequest(totalGainResultsModel: Option[TotalGainResultsModel]): Result = {
      totalGainResultsModel match {
        case Some(model) =>
          val totalGainResults: Seq[BigDecimal] = Seq(model.flatGain) ++ Seq(model.rebasedGain, model.timeApportionedGain).flatten
          if(!totalGainResults.forall(_ <= 0)) Redirect(routes.PropertyLivedInController.propertyLivedIn)
          else Redirect(routes.CheckYourAnswersController.checkYourAnswers)
        case None => Redirect(common.DefaultRoutes.missingDataRoute)
      }
    }

    def errorAction(errors: Form[ImprovementsModel], backUrl: String, improvementsOptions: Boolean,
                    ownerBeforeLegislationStart: Boolean) = {
      Future.successful(BadRequest(improvementsView(errors, improvementsOptions, backUrl, ownerBeforeLegislationStart)))
    }

    def successAction(improvements: ImprovementsModel): Future[Result] = {
      (for {
        _ <- sessionCacheService.saveFormData(KeystoreKeys.improvements, improvements)
        allAnswersModel <- answersConstructor.getNRTotalGainAnswers
        gains <- calcConnector.calculateTotalGain(allAnswersModel)
      } yield successRouteRequest(gains)).recoverToStart
    }

    def routeRequest(backUrl: String,
                     improvementsOptions: Boolean,
                     ownerBeforeLegislationStart: Boolean): Future[Result] = {
      improvementsForm(improvementsOptions).bindFromRequest().fold(
        errors => errorAction(errors, backUrl, improvementsOptions, ownerBeforeLegislationStart),
        success => successAction(success)
      )
    }

    (for {
      acquisitionDate <- fetchAcquisitionDate(request)
      improvementsOptions <- displayImprovementsSectionCheck(acquisitionDate)
      backUrl <- improvementsBackUrl(acquisitionDate)
      ownerBeforeLegislationStart <- ownerBeforeLegislationStartCheck(acquisitionDate)
      route <- routeRequest(backUrl, improvementsOptions, ownerBeforeLegislationStart)
    } yield route).recoverToStart
  }
}
