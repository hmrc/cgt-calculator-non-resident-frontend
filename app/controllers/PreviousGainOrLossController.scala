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

import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.PreviousLossOrGainForm._
import javax.inject.Inject
import models.PreviousLossOrGainModel
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.previousLossOrGain

import scala.concurrent.{ExecutionContext, Future}

class PreviousGainOrLossController @Inject()(http: DefaultHttpClient,calcConnector: CalculatorConnector,
                                             mcc: MessagesControllerComponents,
                                             previousLossOrGainView: previousLossOrGain)
                                            (implicit ec: ExecutionContext)
                                              extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val previousGainOrLoss = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[PreviousLossOrGainModel](KeystoreKeys.previousLossOrGain) map {
      case Some(data) => Ok(previousLossOrGainView(previousLossOrGainForm().fill(data)))
      case None => Ok(previousLossOrGainView(previousLossOrGainForm()))
    }
  }

  val submitPreviousGainOrLoss = ValidateSession.async {
    implicit request =>

      def errorAction(form: Form[PreviousLossOrGainModel]) = {
        Future.successful(BadRequest(previousLossOrGainView(form)))
      }
      def successAction(model: PreviousLossOrGainModel) = {
        (for {
          save <- calcConnector.saveFormData[PreviousLossOrGainModel](KeystoreKeys.previousLossOrGain, model)
          route <- routeRequest(model)
        } yield route).recoverToStart
      }

      def routeRequest(data: PreviousLossOrGainModel): Future[Result] = {
        data.previousLossOrGain match {
          case "Loss" => Future.successful(Redirect(routes.HowMuchLossController.howMuchLoss))
          case "Gain" => Future.successful(Redirect(routes.HowMuchGainController.howMuchGain))
          case "Neither" => Future.successful(Redirect(routes.AnnualExemptAmountController.annualExemptAmount))
        }
      }

      previousLossOrGainForm().bindFromRequest().fold(errorAction, successAction)
  }
}
