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

import java.util.UUID

import common.{KeystoreKeys, TaxDates}
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.nonresident.DisposalDateForm._
import models.nonresident.DisposalDateModel
import play.api.data.Form
import play.api.mvc.Action
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.SessionKeys
import views.html.calculation

import scala.concurrent.Future

object DisposalDateController extends DisposalDateController {
  val calcConnector = CalculatorConnector
}

trait DisposalDateController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.DisposalDateController.disposalDate().url

  val disposalDate = Action.async { implicit request =>
    if (request.session.get(SessionKeys.sessionId).isEmpty) {
      val sessionId = UUID.randomUUID.toString
      Future.successful(Ok(views.html.calculation.nonresident.
        disposalDate(disposalDateForm)).withSession(request.session +
        (SessionKeys.sessionId -> s"session-$sessionId")))
    }
    else {
      calcConnector.fetchAndGetFormData[DisposalDateModel](KeystoreKeys.disposalDate).map {
        case Some(data) => Ok(calculation.nonresident.disposalDate(disposalDateForm.fill(data)))
        case None => Ok(calculation.nonresident.disposalDate(disposalDateForm))
      }
    }
  }

  val submitDisposalDate = ValidateSession.async { implicit request =>

    def errorAction(form: Form[DisposalDateModel]) = Future.successful(BadRequest(calculation.nonresident.disposalDate(form)))

    def successAction(model: DisposalDateModel) = {
      calcConnector.saveFormData(KeystoreKeys.disposalDate, model)
      if (!TaxDates.dateAfterStart(model.day, model.month, model.year)) {
        Future.successful(Redirect(routes.NoCapitalGainsTaxController.noCapitalGainsTax()))
      } else {
        Future.successful(Redirect(routes.SoldOrGivenAwayController.soldOrGivenAway()))
      }
    }
    disposalDateForm.bindFromRequest.fold(errorAction, successAction)
  }
}
