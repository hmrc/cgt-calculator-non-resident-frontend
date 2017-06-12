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

import java.time.LocalDate

import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.TaxDates
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.RebasedValueForm._
import views.html.calculation
import models.{AcquisitionDateModel, RebasedValueModel}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object RebasedValueController extends RebasedValueController {
  val calcConnector = CalculatorConnector
}

trait RebasedValueController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector

  def backLink(acquisitionDate: Option[AcquisitionDateModel]): String = {

    val localDate: Option[LocalDate] = acquisitionDate.map{x => x.get}

    localDate match {
      case Some(x) => {
        if (TaxDates.dateBeforeLegislationStart(x)) controllers.routes.CostsAtLegislationStartController.costsAtLegislationStart().url
        else controllers.routes.AcquisitionCostsController.acquisitionCosts().url
      }
      case _ => controllers.routes.AcquisitionCostsController.acquisitionCosts().url
    }
  }

  val rebasedValue: Action[AnyContent] = ValidateSession.async { implicit request =>
    for {
      rebasedValueModel <- calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue)
      acquisitionDate <- calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate)
    } yield (rebasedValueModel, acquisitionDate) match {
      case (Some(data), _) => Ok(calculation.rebasedValue(rebasedValueForm.fill(data), backLink(acquisitionDate)))
      case (None, _) => Ok(calculation.rebasedValue(rebasedValueForm, backLink(acquisitionDate)))
    }
  }

  val submitRebasedValue: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(errors: Form[RebasedValueModel]) = {
      calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).map{
        x => BadRequest(calculation.rebasedValue(errors, backLink(x)))
      }
    }

    def successAction(model: RebasedValueModel) = {
      calcConnector.saveFormData(KeystoreKeys.rebasedValue, model)
      Future.successful(Redirect(routes.RebasedCostsController.rebasedCosts()))
    }

    rebasedValueForm.bindFromRequest.fold(errorAction, successAction)
  }
}
