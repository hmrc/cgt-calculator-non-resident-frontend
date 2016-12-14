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
import forms.PreviousLossOrGainForm
import models.PreviousLossOrGainModel
import play.api.data.Form
import play.api.mvc.Result
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.calculation.{nonresident => views}

import scala.concurrent.Future
object PreviousGainOrLossController extends PreviousGainOrLossController {
  val calcConnector = CalculatorConnector
}

trait PreviousGainOrLossController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.DisposalDateController.disposalDate().url
  val calcConnector: CalculatorConnector

  val previousGainOrLoss = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[PreviousLossOrGainModel](KeystoreKeys.previousLossOrGain) map {
      case Some(data) => Ok(views.previousLossOrGain(previousLossOrGainForm.fill(data)))
      case None => Ok(views.previousLossOrGain(previousLossOrGainForm))
    }
  }

  val submitPreviousGainOrLoss = ValidateSession.async {
    implicit request =>

      def errorAction(form: Form[PreviousLossOrGainModel]) = {
        Future.successful(BadRequest(views.previousLossOrGain(form)))
      }
      def successAction(model: PreviousLossOrGainModel) = {
        for {
          save <- calcConnector.saveFormData[PreviousLossOrGainModel](KeystoreKeys.previousLossOrGain, model)
          route <- routeRequest(model)
        } yield route
      }

      def routeRequest(data: PreviousLossOrGainModel): Future[Result] = {
        data.previousLossOrGain match {
          case "Loss" => Future.successful(Redirect(routes.HowMuchLossController.howMuchLoss()))
          case "Gain" => Future.successful(Redirect(routes.HowMuchGainController.howMuchGain()))
          case "Neither" => Future.successful(Redirect(routes.AnnualExemptAmountController.annualExemptAmount()))
        }
      }

      previousLossOrGainForm.bindFromRequest.fold(errorAction, successAction)
  }
}