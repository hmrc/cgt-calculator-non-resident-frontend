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
import forms.nonresident.DisposalValueForm._
import models.nonresident.DisposalValueModel
import play.api.data.Form
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.calculation

import scala.concurrent.Future

object DisposalValueController extends DisposalValueController {
  val calcConnector = CalculatorConnector
}

trait DisposalValueController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.DisposalDateController.disposalDate().url

  val disposalValue = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[DisposalValueModel](KeystoreKeys.disposalValue).map {
      case Some(data) => Ok(calculation.nonresident.disposalValue(disposalValueForm.fill(data)))
      case None => Ok(calculation.nonresident.disposalValue(disposalValueForm))
    }
  }

  val submitDisposalValue = ValidateSession.async { implicit request =>

    def errorAction(form: Form[DisposalValueModel]) = Future.successful(BadRequest(calculation.nonresident.disposalValue(form)))

    def successAction(model: DisposalValueModel) = {
      calcConnector.saveFormData(KeystoreKeys.disposalValue, model)
      Future.successful(Redirect(routes.DisposalCostsController.disposalCosts()))
    }

    disposalValueForm.bindFromRequest.fold(errorAction, successAction)
  }
}
