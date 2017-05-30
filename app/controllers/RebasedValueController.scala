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
import common.TaxDates
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.RebasedValueForm._
import views.html.calculation
import models.{AcquisitionDateModel, RebasedValueModel}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object RebasedValueController extends RebasedValueController {
  val calcConnector = CalculatorConnector
}

trait RebasedValueController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector

  val rebasedValue: Action[AnyContent] = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue).map {
      case Some(data) => Ok(calculation.mandatoryRebasedValue(rebasedValueForm.fill(data)))
      case None => Ok(calculation.mandatoryRebasedValue(rebasedValueForm))
    }
  }

  val submitRebasedValue: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(errors: Form[RebasedValueModel]) = {
      Future.successful(BadRequest(calculation.mandatoryRebasedValue(errors)))
    }

    def successAction(model: RebasedValueModel) = {
      calcConnector.saveFormData(KeystoreKeys.rebasedValue, model)
      Future.successful(Redirect(routes.RebasedCostsController.rebasedCosts()))
    }

    rebasedValueForm.bindFromRequest.fold(errorAction, successAction)
  }
}
