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

import common.Dates
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.PersonalAllowanceForm
import models.{DisposalDateModel, PersonalAllowanceModel}
import play.api.data.Form
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

object PersonalAllowanceController extends PersonalAllowanceController {
  val calcConnector = CalculatorConnector
}

trait PersonalAllowanceController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.DisposalDateController.disposalDate().url
  val calcConnector: CalculatorConnector

  val personalAllowance = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[PersonalAllowanceModel](KeystoreKeys.personalAllowance).map {
      case Some(data) => Ok(calculation.nonresident.personalAllowance(personalAllowanceForm().fill(data)))
      case None => Ok(calculation.nonresident.personalAllowance(personalAllowanceForm()))
    }
  }

  val submitPersonalAllowance = ValidateSession.async { implicit request =>

    def getPersonalAllowanceForYear: Future[BigDecimal] = {
      for {
        disposalDate <- calcConnector.fetchAndGetFormData[DisposalDateModel](KeystoreKeys.disposalDate)
        disposalYear <- Future.successful(Dates.getDisposalYear(disposalDate.get.day, disposalDate.get.month, disposalDate.get.year))
        yearsAllowance <- calcConnector.getPA(disposalYear)
      } yield yearsAllowance.get
    }

    def errorAction(form: Form[PersonalAllowanceModel]) = {
      Future.successful(BadRequest(calculation.nonresident.personalAllowance(form)))
    }

    def successAction(model: PersonalAllowanceModel) = {
      calcConnector.saveFormData[PersonalAllowanceModel] (KeystoreKeys.personalAllowance, model)
      Future.successful(Redirect(routes.OtherPropertiesController.otherProperties()))
    }

    for {
      pA <- getPersonalAllowanceForYear
      action <- personalAllowanceForm(pA).bindFromRequest.fold(errorAction, successAction)
    } yield action
  }
}
