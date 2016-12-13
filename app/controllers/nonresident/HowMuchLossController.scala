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

package controllers.nonresident

import common.KeystoreKeys
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.calculation
import forms.nonresident.HowMuchLossForm._
import models.nonresident.HowMuchLossModel
import play.api.data.Form

import scala.concurrent.Future

object HowMuchLossController extends HowMuchLossController {
  val calcConnector = CalculatorConnector
}

trait HowMuchLossController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector

  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.DisposalDateController.disposalDate().url

  val howMuchLoss = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[HowMuchLossModel](KeystoreKeys.howMuchLoss).map {
      case Some(data) => Ok(calculation.nonresident.howMuchLoss(howMuchLossForm.fill(data)))
      case _ => Ok(calculation.nonresident.howMuchLoss(howMuchLossForm))
    }
  }

  val submitHowMuchLoss = ValidateSession.async { implicit request =>

    def errorAction(form: Form[HowMuchLossModel]) = {
      Future.successful(BadRequest(calculation.nonresident.howMuchLoss(form)))
    }

    def successAction(model: HowMuchLossModel) = {
      calcConnector.saveFormData[HowMuchLossModel](KeystoreKeys.howMuchLoss, model)
      if (model.loss > 0) Future.successful(Redirect(routes.BroughtForwardLossesController.broughtForwardLosses()))
      else Future.successful(Redirect(routes.AnnualExemptAmountController.annualExemptAmount()))
    }

    howMuchLossForm.bindFromRequest().fold(
      errorAction,
      successAction
    )
  }
}