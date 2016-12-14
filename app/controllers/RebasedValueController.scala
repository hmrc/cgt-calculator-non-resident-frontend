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
import common.TaxDates
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.RebasedValueForm
import models.{AcquisitionDateModel, RebasedValueModel}
import play.api.data.Form
import play.api.mvc.Result
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

object RebasedValueController extends RebasedValueController {
  val calcConnector = CalculatorConnector
}

trait RebasedValueController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.DisposalDateController.disposalDate().url
  val calcConnector: CalculatorConnector

  private def routeToMandatoryFuture(data: AcquisitionDateModel): Future[Boolean] = Future.successful(routeToMandatory(data))

  private def routeToMandatory(data: AcquisitionDateModel): Boolean = data.hasAcquisitionDate == "Yes" &&
    !TaxDates.dateAfterStart(data.day.getOrElse(0), data.month.getOrElse(0), data.year.getOrElse(0))

  private def fetchAcquisitionDate(implicit headerCarrier: HeaderCarrier): Future[Option[AcquisitionDateModel]] = {
    calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate)
  }

  private def fetchRebasedValue(implicit headerCarrier: HeaderCarrier): Future[Option[RebasedValueModel]] = {
    calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue)
  }

  val rebasedValue = ValidateSession.async { implicit request =>
    def routeRequest(routeToMandatory: Boolean, rebasedValue: Option[RebasedValueModel]): Future[Result] =
      (routeToMandatory, rebasedValue) match {
        case (true, Some(data)) => Future.successful(Ok(calculation.nonresident.mandatoryRebasedValue(rebasedValueForm(routeToMandatory).fill(data))))
        case (false, Some(data)) => Future.successful(Ok(calculation.nonresident.rebasedValue(rebasedValueForm(routeToMandatory).fill(data))))
        case (true, None) => Future.successful(Ok(calculation.nonresident.mandatoryRebasedValue(rebasedValueForm(routeToMandatory))))
        case (false, None) => Future.successful(Ok(calculation.nonresident.rebasedValue(rebasedValueForm(routeToMandatory))))
    }

    for {
      acquisitionDate <- fetchAcquisitionDate(hc)
      rebasedVal <- fetchRebasedValue(hc)
      routeToMandatory <- routeToMandatoryFuture(acquisitionDate.get)
      route <- routeRequest(routeToMandatory, rebasedVal)
    } yield route
  }

  val submitRebasedValue = ValidateSession.async { implicit request =>

    def errorAction(errors: Form[RebasedValueModel], routeToMandatory: Boolean) = {
      if(routeToMandatory) Future.successful(BadRequest(calculation.nonresident.mandatoryRebasedValue(errors)))
      else Future.successful(BadRequest(calculation.nonresident.rebasedValue(errors)))
    }

    def successAction(model: RebasedValueModel) = {
      calcConnector.saveFormData(KeystoreKeys.rebasedValue, model)
      model.rebasedValueAmt.isDefined match {
        case true => Future.successful(Redirect(routes.RebasedCostsController.rebasedCosts()))
        case false => Future.successful(Redirect(routes.ImprovementsController.improvements()))
      }
    }

    def routeRequest(routeToMandatory: Boolean): Future[Result] = {
      rebasedValueForm(routeToMandatory).bindFromRequest.fold(
        errors => errorAction(errors, routeToMandatory),
        success => successAction(success)
      )
    }
    for {
      acquisitionDate <- fetchAcquisitionDate(hc)
      routeToMandatory <- routeToMandatoryFuture(acquisitionDate.get)
      route <- routeRequest(routeToMandatory)
    } yield route
  }
}
