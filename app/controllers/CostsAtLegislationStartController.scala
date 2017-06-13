/*
 * Copyright 2017 HM Revenue & Customs
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
import forms.CostsAtLegislationStartForm._
import views.html.{calculation => views}
import models.CostsAtLegislationStartModel
import play.api.data.Form
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.Future

object CostsAtLegislationStartController extends CostsAtLegislationStartController {
  val calcConnector = CalculatorConnector
}

trait CostsAtLegislationStartController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector

  val costsAtLegislationStart: Action[AnyContent] = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[CostsAtLegislationStartModel](KeystoreKeys.costAtLegislationStart).map {
      case Some(data) => Ok(views.costsAtLegislationStart(costsAtLegislationStartForm.fill(data)))
      case None => Ok(views.costsAtLegislationStart(costsAtLegislationStartForm))
    }
  }

  val submitCostsAtLegislationStart: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(form: Form[CostsAtLegislationStartModel]) = {
      Future.successful(BadRequest(views.costsAtLegislationStart(form)))
    }

    def successAction(model: CostsAtLegislationStartModel) = {
      calcConnector.saveFormData(KeystoreKeys.costAtLegislationStart, model).map { _ =>
        Redirect(routes.RebasedValueController.rebasedValue())
      }
    }

    costsAtLegislationStartForm.bindFromRequest.fold(errorAction, successAction)
  }
}
