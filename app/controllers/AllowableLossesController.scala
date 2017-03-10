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
import common.TaxDates
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.AllowableLossesForm._
import views.html.calculation
import models.{AcquisitionDateModel, AllowableLossesModel, CalculationElectionModel, RebasedValueModel}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future


object AllowableLossesController extends AllowableLossesController {
  val calcConnector = CalculatorConnector
}

trait AllowableLossesController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl = controllers.routes.SummaryController.restart().url
  override val homeLink = controllers.routes.DisposalDateController.disposalDate().url
  val calcConnector: CalculatorConnector

  def allowableLossesBackLink(implicit hc: HeaderCarrier): Future[String] = {
    calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).flatMap {
      case Some(acquisitionData) if acquisitionData.hasAcquisitionDate == "Yes" =>
        Future.successful(routes.PrivateResidenceReliefController.privateResidenceRelief().url)
      case _ => calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue).flatMap {
        case Some(rebasedData) if rebasedData.rebasedValueAmt.isDefined =>
          Future.successful(routes.PrivateResidenceReliefController.privateResidenceRelief().url)
        case _ => Future.successful(routes.DisposalCostsController.disposalCosts().url)
      }
    }
  }

  val allowableLosses = ValidateSession.async { implicit request =>
    def routeRequest(backUrl: String) = {
      calcConnector.fetchAndGetFormData[AllowableLossesModel](KeystoreKeys.allowableLosses).map {
        case Some(data) => Ok(calculation.allowableLosses(allowableLossesForm.fill(data), backUrl))
        case None => Ok(calculation.allowableLosses(allowableLossesForm, backUrl))
      }
    }

    for {
      backUrl <- allowableLossesBackLink
      route <- routeRequest(backUrl)
    } yield route
  }

  val submitAllowableLosses = ValidateSession.async { implicit request =>
    def routeRequest(backUrl: String) = {
      allowableLossesForm.bindFromRequest.fold(
        errors => Future.successful(BadRequest(calculation.allowableLosses(errors, backUrl))),
        success => {
          calcConnector.saveFormData(KeystoreKeys.allowableLosses, success)
          calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).flatMap {
            case Some(data) if data.hasAcquisitionDate == "Yes" && !TaxDates.dateAfterStart(data.day.get, data.month.get, data.year.get) =>
              Future.successful(Redirect(routes.CalculationElectionController.calculationElection()))
            case Some(data) if data.hasAcquisitionDate == "Yes" =>
              calcConnector.saveFormData(KeystoreKeys.calculationElection, CalculationElectionModel("flat"))
              Future.successful(Redirect(routes.OtherReliefsController.otherReliefs()))
            case _ =>
              calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue).flatMap {
                case Some(rebasedData) if rebasedData.rebasedValueAmt.isDefined =>
                  Future.successful(Redirect(routes.CalculationElectionController.calculationElection()))
                case _ =>
                  calcConnector.saveFormData(KeystoreKeys.calculationElection, CalculationElectionModel("flat"))
                  Future.successful(Redirect(routes.OtherReliefsController.otherReliefs()))
              }
          }
        }
      )
    }

    for {
      backUrl <- allowableLossesBackLink
      route <- routeRequest(backUrl)
    } yield route
  }
}
