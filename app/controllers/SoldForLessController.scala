/*
 * Copyright 2019 HM Revenue & Customs
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
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.SoldForLessForm._
import models.SoldForLessModel
import play.api.data.Form
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.{calculation => views}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import controllers.utils.RecoverableFuture
import javax.inject.Inject
import play.api.Environment
import play.api.i18n.I18nSupport
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SoldForLessController @Inject()(http: DefaultHttpClient,calcConnector: CalculatorConnector,
                                      mcc: MessagesControllerComponents)(implicit val applicationConfig: ApplicationConfig)
                                        extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val soldForLess = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[SoldForLessModel](KeystoreKeys.soldForLess).map {
      case Some(data) => Ok(views.soldForLess(soldForLessForm.fill(data)))
      case None => Ok(views.soldForLess(soldForLessForm))
    }
  }

  val submitSoldForLess = ValidateSession.async { implicit request =>

    def errorAction(errors: Form[SoldForLessModel]) = Future.successful(BadRequest(views.soldForLess(errors)))

    def routeRequest(model: SoldForLessModel) = {
      if (model.soldForLess) Future.successful(Redirect(routes.MarketValueWhenSoldOrGaveAwayController.marketValueWhenSold()))
      else Future.successful(Redirect(routes.DisposalValueController.disposalValue()))
    }

    def successAction(model: SoldForLessModel) = {
      (for {
        save <- calcConnector.saveFormData(KeystoreKeys.soldForLess, model)
        route <- routeRequest(model)
      } yield route).recoverToStart
    }

    soldForLessForm.bindFromRequest().fold(errorAction, successAction)
  }
}
