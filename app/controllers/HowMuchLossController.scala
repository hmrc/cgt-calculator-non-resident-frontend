/*
 * Copyright 2019 HM Revenue & Customs
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
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.HowMuchLossForm._
import javax.inject.Inject
import views.html.calculation
import models.HowMuchLossModel
import play.api.Environment
import play.api.data.Form
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.i18n.I18nSupport
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HowMuchLossController @Inject()(http: DefaultHttpClient,calcConnector: CalculatorConnector,
                                      mcc: MessagesControllerComponents)(implicit val applicationConfig: ApplicationConfig)
                                        extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val howMuchLoss = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[HowMuchLossModel](KeystoreKeys.howMuchLoss).map {
      case Some(data) => Ok(calculation.howMuchLoss(howMuchLossForm.fill(data)))
      case _ => Ok(calculation.howMuchLoss(howMuchLossForm))
    }
  }

  val submitHowMuchLoss = ValidateSession.async { implicit request =>

    def errorAction(form: Form[HowMuchLossModel]) = {
      Future.successful(BadRequest(calculation.howMuchLoss(form)))
    }

    def successAction(model: HowMuchLossModel) = {
      calcConnector.saveFormData[HowMuchLossModel](KeystoreKeys.howMuchLoss, model).map(_ =>
        if (model.loss > 0) {
          Redirect(routes.BroughtForwardLossesController.broughtForwardLosses())
        } else {
          Redirect(routes.AnnualExemptAmountController.annualExemptAmount())
        }
      )
    }

    howMuchLossForm.bindFromRequest().fold(
      errorAction,
      successAction
    )
  }
}
