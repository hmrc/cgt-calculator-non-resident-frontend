/*
 * Copyright 2022 HM Revenue & Customs
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
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.OtherPropertiesForm._
import javax.inject.Inject
import models.OtherPropertiesModel
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.otherProperties

import scala.concurrent.{ExecutionContext, Future}

class OtherPropertiesController @Inject()(http: DefaultHttpClient,calcConnector: CalculatorConnector,
                                          mcc: MessagesControllerComponents,
                                          otherPropertiesView: otherProperties)
                                         (implicit ec: ExecutionContext)
                                            extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val otherProperties = ValidateSession.async { implicit request =>

    calcConnector.fetchAndGetFormData[OtherPropertiesModel](KeystoreKeys.otherProperties).map {
      case Some(data) => Ok(otherPropertiesView(otherPropertiesForm.fill(data)))
      case _ => Ok(otherPropertiesView(otherPropertiesForm))
    }
  }

  val submitOtherProperties = ValidateSession.async { implicit request =>
    def errorAction(form: Form[OtherPropertiesModel]) = {
      Future.successful(BadRequest(otherPropertiesView(form)))
    }

    def successAction(model: OtherPropertiesModel) = {
      for {
        save <- calcConnector.saveFormData[OtherPropertiesModel](KeystoreKeys.otherProperties, model)
        route <- routeRequest(model)
      } yield route
    }

    def routeRequest(data: OtherPropertiesModel): Future[Result] = {
      data.otherProperties match {
        case "Yes" => Future.successful(Redirect(routes.PreviousGainOrLossController.previousGainOrLoss))
        case "No" => Future.successful(Redirect(routes.BroughtForwardLossesController.broughtForwardLosses))
      }
    }

    otherPropertiesForm.bindFromRequest.fold(errorAction, successAction)
  }
}
