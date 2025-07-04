/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.predicates.ValidActiveSession
import forms.HowMuchLossForm._
import models.HowMuchLossModel
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.howMuchLoss

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HowMuchLossController @Inject()(val http: DefaultHttpClient,
                                      sessionCacheService: SessionCacheService,
                                      mcc: MessagesControllerComponents,
                                      howMuchLossView: howMuchLoss)(implicit ec: ExecutionContext)
                                        extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val howMuchLoss: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheService.fetchAndGetFormData[HowMuchLossModel](KeystoreKeys.howMuchLoss).map {
      case Some(data) => Ok(howMuchLossView(howMuchLossForm.fill(data)))
      case _ => Ok(howMuchLossView(howMuchLossForm))
    }
  }

  val submitHowMuchLoss: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(form: Form[HowMuchLossModel]) = {
      Future.successful(BadRequest(howMuchLossView(form)))
    }

    def successAction(model: HowMuchLossModel) = {
      sessionCacheService.saveFormData[HowMuchLossModel](KeystoreKeys.howMuchLoss, model).map(_ =>
        if (model.loss > 0) {
          Redirect(routes.BroughtForwardLossesController.broughtForwardLosses)
        } else {
          Redirect(routes.AnnualExemptAmountController.annualExemptAmount)
        }
      )
    }

    howMuchLossForm.bindFromRequest().fold(
      errorAction,
      successAction
    )
  }
}
