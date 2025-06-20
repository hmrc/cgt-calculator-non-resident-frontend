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
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.SoldForLessForm._
import models.SoldForLessModel
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.soldForLess

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SoldForLessController @Inject()(val http: DefaultHttpClient,
                                      sessionCacheService: SessionCacheService,
                                      mcc: MessagesControllerComponents,
                                      soldForLessView: soldForLess)
                                     (implicit ec: ExecutionContext)
                                        extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val soldForLess: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheService.fetchAndGetFormData[SoldForLessModel](KeystoreKeys.soldForLess).map {
      case Some(data) => Ok(soldForLessView(soldForLessForm.fill(data)))
      case None => Ok(soldForLessView(soldForLessForm))
    }
  }

  val submitSoldForLess: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(errors: Form[SoldForLessModel]) = Future.successful(BadRequest(soldForLessView(errors)))

    def routeRequest(model: SoldForLessModel) = {
      if (model.soldForLess) Future.successful(Redirect(routes.MarketValueWhenSoldOrGaveAwayController.marketValueWhenSold))
      else Future.successful(Redirect(routes.DisposalValueController.disposalValue))
    }

    def successAction(model: SoldForLessModel) = {
      (for {
        _ <- sessionCacheService.saveFormData(KeystoreKeys.soldForLess, model)
        route <- routeRequest(model)
      } yield route).recoverToStart
    }

    soldForLessForm.bindFromRequest().fold(errorAction, successAction)
  }
}
