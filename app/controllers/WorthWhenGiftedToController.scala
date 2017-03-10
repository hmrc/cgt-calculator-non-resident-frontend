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
import forms.AcquisitionMarketValueForm._
import views.html.calculation
import models.AcquisitionValueModel
import play.api.data.Form
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object WorthWhenGiftedToController extends WorthWhenGiftedToController {
  val calcConnector = CalculatorConnector
}

trait WorthWhenGiftedToController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  override val sessionTimeoutUrl = controllers.routes.SummaryController.restart().url
  override val homeLink = controllers.routes.DisposalDateController.disposalDate().url

  val worthWhenGiftedTo = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[AcquisitionValueModel](KeystoreKeys.acquisitionMarketValue).map {
      case Some(data) => Ok(calculation.worthWhenGiftedTo(acquisitionMarketValueForm.fill(data)))
      case None => Ok(calculation.worthWhenGiftedTo(acquisitionMarketValueForm))
    }
  }

  val submitWorthWhenGiftedTo = ValidateSession.async { implicit request =>

    def errorAction(form: Form[AcquisitionValueModel]) = Future.successful(BadRequest(calculation.worthWhenGiftedTo(form)))

    def successAction(model: AcquisitionValueModel) = {
      calcConnector.saveFormData(KeystoreKeys.acquisitionMarketValue, model)
      Future.successful(Redirect(routes.AcquisitionCostsController.acquisitionCosts()))
    }

    acquisitionMarketValueForm.bindFromRequest.fold(errorAction, successAction)
  }
}
