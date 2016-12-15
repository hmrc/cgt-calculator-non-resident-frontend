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
import forms.CurrentIncomeForm._
import views.html.calculation
import models.CurrentIncomeModel
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

object CurrentIncomeController extends CurrentIncomeController {
  val calcConnector = CalculatorConnector
}

trait CurrentIncomeController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl = controllers.routes.SummaryController.restart().url
  override val homeLink = controllers.routes.DisposalDateController.disposalDate().url
  val calcConnector: CalculatorConnector

  val currentIncome = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[CurrentIncomeModel](KeystoreKeys.currentIncome).map {
      case Some(data) => Ok(calculation.currentIncome(currentIncomeForm.fill(data)))
      case None => Ok(calculation.currentIncome(currentIncomeForm))
    }
  }

  val submitCurrentIncome = ValidateSession.async { implicit request =>

    def routeRequest(model: CurrentIncomeModel) = {
      if (model.currentIncome > 0) Future.successful(Redirect(routes.PersonalAllowanceController.personalAllowance()))
      else Future.successful(Redirect(routes.OtherPropertiesController.otherProperties()))
    }

    def successAction(model: CurrentIncomeModel) = {
      for {
        _ <- calcConnector.saveFormData(KeystoreKeys.currentIncome, model)
        route <- routeRequest(model)
      } yield route
    }

    currentIncomeForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(calculation.currentIncome(errors))),
      success => successAction(success)
    )
  }
}
