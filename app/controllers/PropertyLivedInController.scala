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

import common.KeystoreKeys.{NonResidentKeys => keystoreKeys}
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.PropertyLivedInForm._
import javax.inject.Inject
import models.PropertyLivedInModel
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.propertyLivedIn

import scala.concurrent.{ExecutionContext, Future}

class PropertyLivedInController @Inject()(http: DefaultHttpClient,calcConnector: CalculatorConnector,
                                          mcc: MessagesControllerComponents,
                                          propertyLivedInView: propertyLivedIn)
                                         (implicit ec: ExecutionContext)
                                            extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val propertyLivedIn: Action[AnyContent] = ValidateSession.async { implicit request =>

    calcConnector.fetchAndGetFormData[PropertyLivedInModel](keystoreKeys.propertyLivedIn).map {
      case Some(data) => Ok(propertyLivedInView(propertyLivedInForm.fill(data)))
      case _ => Ok(propertyLivedInView(propertyLivedInForm))
    }
  }

  val submitPropertyLivedIn: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(errors: Form[PropertyLivedInModel]) = Future.successful(BadRequest(propertyLivedInView(
      errors
    )))

    def routeRequest(model: PropertyLivedInModel) = {
      if (model.propertyLivedIn) Future.successful(Redirect(routes.PrivateResidenceReliefController.privateResidenceRelief))
      else Future.successful(Redirect(routes.CurrentIncomeController.currentIncome))
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

