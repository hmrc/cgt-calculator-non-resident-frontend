/*
 * Copyright 2019 HM Revenue & Customs
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

import java.util.UUID

import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.TaxDates
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.DisposalDateForm._
import views.html.calculation
import models.DateModel
import play.api.Logger
import play.api.data.Form
import play.api.mvc.Action
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future
import uk.gov.hmrc.http.SessionKeys

object DisposalDateController extends DisposalDateController {
  val calcConnector = CalculatorConnector
}

trait DisposalDateController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector

  val disposalDate = Action.async { implicit request =>
    if (request.session.get(SessionKeys.sessionId).isEmpty) {
      val sessionId = UUID.randomUUID.toString
      Future.successful(Ok(views.html.calculation.
        disposalDate(disposalDateForm)).withSession(request.session +
        (SessionKeys.sessionId -> s"session-$sessionId")))
    }
    else {
      calcConnector.fetchAndGetFormData[DateModel](KeystoreKeys.disposalDate).map {
        case Some(data) => Ok(calculation.disposalDate(disposalDateForm.fill(data)))
        case None => Ok(calculation.disposalDate(disposalDateForm))
      }
    }
  }

  val submitDisposalDate = ValidateSession.async { implicit request =>

    def errorAction(form: Form[DateModel]) = Future.successful(BadRequest(calculation.disposalDate(form)))

    def successAction(model: DateModel) = {
      Logger.info("Saving disposalDate as : " + model)
      for {
        _ <- calcConnector.saveFormData(KeystoreKeys.disposalDate, model)
        taxYear <- calcConnector.getTaxYear(s"${model.year}-${model.month}-${model.day}")
      } yield {
        if (!TaxDates.dateAfterStart(model.day, model.month, model.year)) {
          Redirect(routes.NoCapitalGainsTaxController.noCapitalGainsTax())
        } else if (!taxYear.get.isValidYear) {
          Redirect(routes.OutsideTaxYearController.outsideTaxYear())
        } else {
          Redirect(routes.SoldOrGivenAwayController.soldOrGivenAway())
        }
      }
    }
    disposalDateForm.bindFromRequest.fold(errorAction, successAction)
  }
}
