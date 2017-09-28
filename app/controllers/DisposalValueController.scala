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
import forms.DisposalValueForm._
import views.html.calculation
import models.DisposalValueModel
import play.api.data.Form
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object DisposalValueController extends DisposalValueController {
  val calcConnector = CalculatorConnector
}

trait DisposalValueController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector

  val disposalValue = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[DisposalValueModel](KeystoreKeys.disposalValue).map {
      case Some(data) => Ok(calculation.disposalValue(disposalValueForm.fill(data)))
      case None => Ok(calculation.disposalValue(disposalValueForm))
    }
  }

  val submitDisposalValue = ValidateSession.async { implicit request =>

    def errorAction(form: Form[DisposalValueModel]) = Future.successful(BadRequest(calculation.disposalValue(form)))

    def successAction(model: DisposalValueModel) = {
      calcConnector.saveFormData(KeystoreKeys.disposalValue, model).map(_ =>
        Redirect(routes.DisposalCostsController.disposalCosts()))
    }

    disposalValueForm.bindFromRequest.fold(errorAction, successAction)
  }
}
