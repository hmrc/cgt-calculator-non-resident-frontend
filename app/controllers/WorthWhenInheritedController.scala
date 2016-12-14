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
import forms.AcquisitionMarketValueForm
import models.AcquisitionValueModel
import play.api.data.Form
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.calculation.{nonresident => views}

import scala.concurrent.Future

object WorthWhenInheritedController extends WorthWhenInheritedController {
  val calcConnector = CalculatorConnector
}

trait WorthWhenInheritedController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.DisposalDateController.disposalDate().url

  val worthWhenInherited = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[AcquisitionValueModel](KeystoreKeys.acquisitionMarketValue).map {
      case Some(data) => Ok(views.worthWhenInherited(acquisitionMarketValueForm.fill(data)))
      case None => Ok(views.worthWhenInherited(acquisitionMarketValueForm))
    }
  }

  val submitWorthWhenInherited = ValidateSession.async { implicit request =>

    def errorAction(form: Form[AcquisitionValueModel]) = Future.successful(BadRequest(views.worthWhenInherited(form)))

    def successAction(model: AcquisitionValueModel) = {
      calcConnector.saveFormData(KeystoreKeys.acquisitionMarketValue, model)
      Future.successful(Redirect(routes.AcquisitionCostsController.acquisitionCosts()))
    }

    acquisitionMarketValueForm.bindFromRequest.fold(errorAction, successAction)
  }
}
