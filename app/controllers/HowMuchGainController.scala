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
import forms.HowMuchGainForm._
import javax.inject.Inject
import models.HowMuchGainModel
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.howMuchGain

import scala.concurrent.{ExecutionContext, Future}

class HowMuchGainController @Inject()(http: DefaultHttpClient,calcConnector: CalculatorConnector,
                                      mcc: MessagesControllerComponents,
                                      howMuchGainView: howMuchGain)(implicit ec: ExecutionContext)
                                        extends FrontendController(mcc) with ValidActiveSession with I18nSupport{

  val howMuchGain = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[HowMuchGainModel](KeystoreKeys.howMuchGain).map {
      case Some(data) => Ok(howMuchGainView(howMuchGainForm.fill(data)))
      case None => Ok(howMuchGainView(howMuchGainForm))
    }
  }

  val submitHowMuchGain = ValidateSession.async { implicit request =>

    def errorAction(form: Form[HowMuchGainModel]) = Future.successful(BadRequest(howMuchGainView(form)))

    def successAction(model: HowMuchGainModel) = {
      calcConnector.saveFormData(KeystoreKeys.howMuchGain, model).map(_ =>
        if (model.howMuchGain > 0) {
          Redirect(routes.BroughtForwardLossesController.broughtForwardLosses)
        } else {
          Redirect(routes.AnnualExemptAmountController.annualExemptAmount)
        }
      )
    }

    howMuchGainForm.bindFromRequest().fold(errorAction, successAction)
  }
}
