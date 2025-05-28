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
import forms.MarketValueGaveAwayForm._
import forms.MarketValueWhenSoldForm._
import models.DisposalValueModel
import play.api.Environment
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.{marketValueGaveAway, marketValueSold}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MarketValueWhenSoldOrGaveAwayController @Inject()(val environment: Environment,
                                                        val http: DefaultHttpClient,
                                                        sessionCacheService: SessionCacheService,
                                                        mcc: MessagesControllerComponents,
                                                        marketValueSoldView: marketValueSold,
                                                        marketValueGaveAwayView: marketValueGaveAway)
                                                       (implicit ec: ExecutionContext)
                                                          extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val marketValueWhenSold: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheService.fetchAndGetFormData[DisposalValueModel](KeystoreKeys.disposalMarketValue).map {
      case Some(data) => Ok(marketValueSoldView(marketValueWhenSoldForm.fill(data)))
      case None => Ok(marketValueSoldView(marketValueWhenSoldForm))
    }
  }

  val marketValueWhenGaveAway: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheService.fetchAndGetFormData[DisposalValueModel](KeystoreKeys.disposalMarketValue).map {
      case Some(data) => Ok(marketValueGaveAwayView(marketValueWhenGaveAwayForm.fill(data)))
      case None => Ok(marketValueGaveAwayView(marketValueWhenGaveAwayForm))
    }
  }

  val submitMarketValueWhenSold: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(form: Form[DisposalValueModel]) = Future.successful(BadRequest(marketValueSoldView(form)))

    def successAction(model: DisposalValueModel) = {
      sessionCacheService.saveFormData(KeystoreKeys.disposalMarketValue, model)
      Future.successful(Redirect(routes.DisposalCostsController.disposalCosts))
    }

    marketValueWhenSoldForm.bindFromRequest().fold(errorAction, successAction)
  }

  val submitMarketValueWhenGaveAway: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(form: Form[DisposalValueModel]) = Future.successful(BadRequest(marketValueGaveAwayView(form)))

    def successAction(model: DisposalValueModel) = {
      sessionCacheService.saveFormData(KeystoreKeys.disposalMarketValue, model).map(_ =>
        Redirect(routes.DisposalCostsController.disposalCosts))
    }

    marketValueWhenGaveAwayForm.bindFromRequest().fold(errorAction, successAction)
  }
}
