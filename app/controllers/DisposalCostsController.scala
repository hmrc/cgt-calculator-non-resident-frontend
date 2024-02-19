/*
 * Copyright 2023 HM Revenue & Customs
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

import common.DefaultRoutes._
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.DisposalCostsForm._
import models.{DisposalCostsModel, SoldForLessModel, SoldOrGivenAwayModel}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.calculation.disposalCosts

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DisposalCostsController @Inject()(sessionCacheService: SessionCacheService,
                                        mcc: MessagesControllerComponents,
                                        disposalCostsView: disposalCosts)
                                       (implicit ec: ExecutionContext)
                                          extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  private def backUrl(soldOrGivenAwayModel: Option[SoldOrGivenAwayModel], soldForLessModel: Option[SoldForLessModel]): Future[String] =
    (soldOrGivenAwayModel, soldForLessModel) match {
    case (Some(SoldOrGivenAwayModel(soldIt)), _) if !soldIt => Future.successful(routes.MarketValueWhenSoldOrGaveAwayController.marketValueWhenGaveAway.url)
    case (Some(SoldOrGivenAwayModel(soldIt)), Some(SoldForLessModel(soldForLess))) if soldIt && soldForLess =>
      Future.successful(routes.MarketValueWhenSoldOrGaveAwayController.marketValueWhenSold.url)
    case (Some(SoldOrGivenAwayModel(soldIt)), Some(SoldForLessModel(soldForLess))) if soldIt && !soldForLess =>
      Future.successful(routes.DisposalValueController.disposalValue.url)
    case (_, _) => Future.successful(missingDataRoute)
  }

  private def fetchSoldOrGivenAway(implicit request: Request[_]): Future[Option[SoldOrGivenAwayModel]] = {
    sessionCacheService.fetchAndGetFormData[SoldOrGivenAwayModel](KeystoreKeys.soldOrGivenAway)
  }

  private def fetchSoldForLess(implicit request: Request[_]): Future[Option[SoldForLessModel]] = {
    sessionCacheService.fetchAndGetFormData[SoldForLessModel](KeystoreKeys.soldForLess)
  }

  val disposalCosts: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(backLink: String): Future[Result] = {
      sessionCacheService.fetchAndGetFormData[DisposalCostsModel](KeystoreKeys.disposalCosts).map {
        case Some(data) => Ok(disposalCostsView(disposalCostsForm.fill(data), backLink))
        case None => Ok(disposalCostsView(disposalCostsForm, backLink))
      }
    }

    (for {
      soldOrGivenAway <- fetchSoldOrGivenAway
      soldForLess <- fetchSoldForLess
      backLink <- backUrl(soldOrGivenAway, soldForLess)
      route <- routeRequest(backLink)
    } yield route).recoverToStart
  }

  val submitDisposalCosts: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(errors: Form[DisposalCostsModel], backLink: String) = {
      Future.successful(BadRequest(disposalCostsView(errors, backLink)))
    }

    def successAction(model: DisposalCostsModel) = {
      sessionCacheService.saveFormData(KeystoreKeys.disposalCosts, model).map(_ =>
        Redirect(routes.AcquisitionDateController.acquisitionDate))
    }

    def routeRequest(backLink: String) = {
      disposalCostsForm.bindFromRequest().fold(
        errors => errorAction(errors, backLink),
        success => successAction(success)
      )
    }

    (for {
      soldOrGivenAway <- fetchSoldOrGivenAway
      soldForLess <- fetchSoldForLess
      backLink <- backUrl(soldOrGivenAway, soldForLess)
      route <- routeRequest(backLink)
    } yield route).recoverToStart
  }
}
