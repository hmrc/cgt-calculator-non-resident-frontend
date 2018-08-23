/*
 * Copyright 2018 HM Revenue & Customs
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
import common.TaxDates
import connectors.CalculatorConnector
import constructors.CalculationElectionConstructor
import controllers.predicates.ValidActiveSession
import forms.AcquisitionDateForm._
import models.DateModel
import play.api.data.Form
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.calculation
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.Future

object AcquisitionDateController extends AcquisitionDateController {
  val calcConnector = CalculatorConnector
}

trait AcquisitionDateController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  val calcElectionConstructor = CalculationElectionConstructor

  val acquisitionDate: Action[AnyContent] = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[DateModel](KeystoreKeys.acquisitionDate).map {
      case Some(data) => Ok(calculation.acquisitionDate(acquisitionDateForm.fill(data)))
      case None => Ok(calculation.acquisitionDate(acquisitionDateForm))
    }
  }

  val submitAcquisitionDate: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(form: Form[DateModel]) = Future.successful(BadRequest(calculation.acquisitionDate(form)))

    def successAction(model: DateModel) = {
      calcConnector.saveFormData(KeystoreKeys.acquisitionDate, model).map(_ =>
        if(TaxDates.dateBeforeLegislationStart(model.day, model.month, model.year)) {
          Redirect(routes.WorthBeforeLegislationStartController.worthBeforeLegislationStart())
        } else {
          Redirect(routes.HowBecameOwnerController.howBecameOwner())
        }
      )
    }

    acquisitionDateForm.bindFromRequest.fold(errorAction, successAction)
  }
}
