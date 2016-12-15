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
import forms.WorthBeforeLegislationStartForm._
import models.WorthBeforeLegislationStartModel
import play.api.data.Form
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.{calculation => views}

import scala.concurrent.Future

object WorthBeforeLegislationStartController extends WorthBeforeLegislationStartController {
  val calcConnector = CalculatorConnector
}

trait WorthBeforeLegislationStartController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  override val sessionTimeoutUrl = controllers.routes.SummaryController.restart().url
  override val homeLink = controllers.routes.DisposalDateController.disposalDate().url

  val worthBeforeLegislationStart = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[WorthBeforeLegislationStartModel](KeystoreKeys.worthBeforeLegislationStart).map {
      case Some(data) => Ok(views.worthBeforeLegislationStart(worthBeforeLegislationStartForm.fill(data)))
      case None => Ok(views.worthBeforeLegislationStart(worthBeforeLegislationStartForm))
    }
  }

  val submitWorthBeforeLegislationStart = ValidateSession.async { implicit request =>

    def errorAction(form: Form[WorthBeforeLegislationStartModel]) = Future.successful(BadRequest(views.worthBeforeLegislationStart(form)))

    def successAction(model: WorthBeforeLegislationStartModel) = {
      calcConnector.saveFormData(KeystoreKeys.worthBeforeLegislationStart, model)
      Future.successful(Redirect(routes.AcquisitionCostsController.acquisitionCosts()))
    }

    worthBeforeLegislationStartForm.bindFromRequest.fold(errorAction, successAction)
  }
}
