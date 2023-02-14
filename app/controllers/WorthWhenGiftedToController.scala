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
import forms.WorthWhenGiftedTo.worthWhenGiftedToForm
import javax.inject.Inject
import models.AcquisitionValueModel
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.worthWhenGiftedTo

import scala.concurrent.{ExecutionContext, Future}

class WorthWhenGiftedToController @Inject()(http: DefaultHttpClient,calcConnector: CalculatorConnector,
                                            mcc: MessagesControllerComponents,
                                            worthWhenGiftedToView: worthWhenGiftedTo)(implicit ec: ExecutionContext)
                                              extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val worthWhenGiftedTo = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[AcquisitionValueModel](KeystoreKeys.acquisitionMarketValue).map {
      case Some(data) => Ok(worthWhenGiftedToView(worthWhenGiftedToForm.fill(data)))
      case None => Ok(worthWhenGiftedToView(worthWhenGiftedToForm))
    }
  }

  val submitWorthWhenGiftedTo = ValidateSession.async { implicit request =>

    def errorAction(form: Form[AcquisitionValueModel]) = Future.successful(BadRequest(worthWhenGiftedToView(form)))

    def successAction(model: AcquisitionValueModel) = {
      calcConnector.saveFormData(KeystoreKeys.acquisitionMarketValue, model).map(_ =>
        Redirect(routes.AcquisitionCostsController.acquisitionCosts))
    }

    worthWhenGiftedToForm.bindFromRequest.fold(errorAction, successAction)
  }
}
