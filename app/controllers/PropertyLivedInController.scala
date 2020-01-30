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

import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.PropertyLivedInForm._
import models.PropertyLivedInModel
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.data.Form
import common.KeystoreKeys.{NonResidentKeys => keystoreKeys}
import config.ApplicationConfig
import controllers.utils.RecoverableFuture
import javax.inject.Inject
import play.api.Environment
import play.api.i18n.I18nSupport
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PropertyLivedInController @Inject()(http: DefaultHttpClient,calcConnector: CalculatorConnector,
                                          mcc: MessagesControllerComponents)(implicit val applicationConfig: ApplicationConfig)
                                            extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val propertyLivedIn: Action[AnyContent] = ValidateSession.async { implicit request =>

    calcConnector.fetchAndGetFormData[PropertyLivedInModel](keystoreKeys.propertyLivedIn).map {
      case Some(data) => Ok(views.html.calculation.propertyLivedIn(propertyLivedInForm.fill(data)))
      case _ => Ok(views.html.calculation.propertyLivedIn(propertyLivedInForm))
    }
  }

  val submitPropertyLivedIn: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(errors: Form[PropertyLivedInModel]) = Future.successful(BadRequest(views.html.calculation.propertyLivedIn(
      errors
    )))

    def routeRequest(model: PropertyLivedInModel) = {
      if (model.propertyLivedIn) Future.successful(Redirect(routes.PrivateResidenceReliefController.privateResidenceRelief()))
      else Future.successful(Redirect(routes.CurrentIncomeController.currentIncome()))
    }

    def successAction(model: PropertyLivedInModel) = {
      (for {
        save <- calcConnector.saveFormData(keystoreKeys.propertyLivedIn, model)
        route <- routeRequest(model)
      } yield route).recoverToStart
    }

    propertyLivedInForm.bindFromRequest().fold(errorAction, successAction)
  }
}

