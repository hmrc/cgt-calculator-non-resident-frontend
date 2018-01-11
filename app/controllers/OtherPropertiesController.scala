/*
 * Copyright 2018 HM Revenue & Customs
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
import models.OtherPropertiesModel
import play.api.Play.current
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Result
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.calculation

import scala.concurrent.Future

object OtherPropertiesController extends OtherPropertiesController {
  val calcConnector = CalculatorConnector
}

trait OtherPropertiesController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector

  val otherProperties = ValidateSession.async { implicit request =>

    calcConnector.fetchAndGetFormData[OtherPropertiesModel](KeystoreKeys.otherProperties).map {
      case Some(data) => Ok(calculation.otherProperties(otherPropertiesForm.fill(data)))
      case _ => Ok(calculation.otherProperties(otherPropertiesForm))
    }
  }

  val submitOtherProperties = ValidateSession.async { implicit request =>
    def errorAction(form: Form[OtherPropertiesModel]) = {
      Future.successful(BadRequest(calculation.otherProperties(form)))
    }

    def successAction(model: OtherPropertiesModel) = {
      for {
        save <- calcConnector.saveFormData[OtherPropertiesModel](KeystoreKeys.otherProperties, model)
        route <- routeRequest(model)
      } yield route
    }

    def routeRequest(data: OtherPropertiesModel): Future[Result] = {
      data.otherProperties match {
        case "Yes" => Future.successful(Redirect(routes.PreviousGainOrLossController.previousGainOrLoss()))
        case "No" => Future.successful(Redirect(routes.BroughtForwardLossesController.broughtForwardLosses()))
      }
    }

    otherPropertiesForm.bindFromRequest.fold(errorAction, successAction)
  }
}
