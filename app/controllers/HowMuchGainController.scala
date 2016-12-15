/*
 * Copyright 2016 HM Revenue & Customs
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
import forms.HowMuchGainForm._
import models.HowMuchGainModel
import play.api.data.Form
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.calculation
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object HowMuchGainController extends HowMuchGainController {
  val calcConnector = CalculatorConnector
}

trait HowMuchGainController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  override val sessionTimeoutUrl = controllers.routes.SummaryController.restart().url
  override val homeLink = controllers.routes.DisposalDateController.disposalDate().url

  val howMuchGain = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[HowMuchGainModel](KeystoreKeys.howMuchGain).map {
      case Some(data) => Ok(calculation.howMuchGain(howMuchGainForm.fill(data)))
      case None => Ok(calculation.howMuchGain(howMuchGainForm))
    }
  }

  val submitHowMuchGain = ValidateSession.async { implicit request =>

    def errorAction(form: Form[HowMuchGainModel]) = Future.successful(BadRequest(calculation.howMuchGain(form)))

    def successAction(model: HowMuchGainModel) = {
      calcConnector.saveFormData(KeystoreKeys.howMuchGain, model)
      if (model.howMuchGain > 0) Future.successful(Redirect(routes.BroughtForwardLossesController.broughtForwardLosses()))
      else Future.successful(Redirect(routes.AnnualExemptAmountController.annualExemptAmount()))
    }

    howMuchGainForm.bindFromRequest.fold(errorAction, successAction)
  }
}
