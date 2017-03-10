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
import forms.RebasedCostsForm._
import views.html.calculation
import models.RebasedCostsModel
import play.api.data.Form
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object RebasedCostsController extends RebasedCostsController {
  val calcConnector = CalculatorConnector
}

trait RebasedCostsController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl = controllers.routes.SummaryController.restart().url
  override val homeLink = controllers.routes.DisposalDateController.disposalDate().url
  val calcConnector: CalculatorConnector

  val rebasedCosts = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[RebasedCostsModel](KeystoreKeys.rebasedCosts).map {
      case Some(data) => Ok(calculation.rebasedCosts(rebasedCostsForm.fill(data)))
      case None => Ok(calculation.rebasedCosts(rebasedCostsForm))
    }
  }

  val submitRebasedCosts = ValidateSession.async { implicit request =>

    def errorAction(form: Form[RebasedCostsModel]) = {
      Future.successful(BadRequest(calculation.rebasedCosts(form)))
    }

    def successAction(model: RebasedCostsModel) = {
      calcConnector.saveFormData(KeystoreKeys.rebasedCosts, model)
      Future.successful(Redirect(routes.ImprovementsController.improvements()))
    }

    rebasedCostsForm.bindFromRequest.fold(errorAction, successAction)
  }
}
