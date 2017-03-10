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
import constructors.AnswersConstructor
import controllers.predicates.ValidActiveSession
import forms.ImprovementsForm._
import views.html.calculation
import models._
import play.api.data.Form
import play.api.mvc.Result
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object ImprovementsController extends ImprovementsController {
  val calcConnector = CalculatorConnector
  val answersConstructor = AnswersConstructor
}

trait ImprovementsController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  val answersConstructor: AnswersConstructor

  private def improvementsBackUrl(rebasedValue: Option[RebasedValueModel], acquisitionDate: Option[AcquisitionDateModel])
                                 (implicit hc: HeaderCarrier): Future[String] = (rebasedValue, acquisitionDate) match {
    case (_, Some(AcquisitionDateModel("Yes", Some(day), Some(month), Some(year)))) if TaxDates.dateAfterStart(day, month, year) =>
      Future.successful(routes.AcquisitionCostsController.acquisitionCosts().url)
    case (Some(RebasedValueModel(None)), Some(AcquisitionDateModel("No", _, _, _))) =>
      Future.successful(routes.RebasedValueController.rebasedValue().url)
    case (Some(RebasedValueModel(Some(data))), Some(AcquisitionDateModel("No", _, _, _))) =>
      Future.successful(routes.RebasedCostsController.rebasedCosts().url)
    case (Some(RebasedValueModel(Some(data))), Some(AcquisitionDateModel("Yes", Some(day), Some(month), Some(year))))
      if !TaxDates.dateAfterStart(day, month, year) =>
      Future.successful(routes.RebasedCostsController.rebasedCosts().url)
    case (_, _) => Future.successful(common.DefaultRoutes.missingDataRoute)
  }

  private def fetchAcquisitionDate(implicit headerCarrier: HeaderCarrier): Future[Option[AcquisitionDateModel]] = {
    calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate)
  }

  private def fetchImprovements(implicit headerCarrier: HeaderCarrier): Future[Option[ImprovementsModel]] = {
    calcConnector.fetchAndGetFormData[ImprovementsModel](KeystoreKeys.improvements)
  }

  private def fetchRebasedValue(implicit headerCarrier: HeaderCarrier): Future[Option[RebasedValueModel]] = {
    calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue)
  }

  private def displayImprovementsSectionCheck(rebasedValueModel: Option[RebasedValueModel],
                                              acquisitionDateModel: Option[AcquisitionDateModel]): Future[Boolean] = {
    (rebasedValueModel, acquisitionDateModel) match {
      case (Some(value), Some(data)) if data.hasAcquisitionDate == "Yes" &&
        !TaxDates.dateAfterStart(data.day.get, data.month.get, data.year.get) &&
        value.rebasedValueAmt.isDefined =>
        Future.successful(true)
      case (Some(value), Some(data)) if data.hasAcquisitionDate == "No" &&
        value.rebasedValueAmt.isDefined =>
        Future.successful(true)
      case (_, _) =>
        Future.successful(false)
    }
  }

  val improvements = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String, improvementsModel: Option[ImprovementsModel], improvementsOptions: Boolean): Future[Result] = {
      improvementsModel match {
        case Some(data) =>
          Future.successful(Ok(calculation.improvements(improvementsForm(improvementsOptions).fill(data),
            improvementsOptions, backUrl)))
        case None =>
          Future.successful(Ok(calculation.improvements(improvementsForm(improvementsOptions),
            improvementsOptions, backUrl)))
      }
    }

    for {
      rebasedValue <- fetchRebasedValue(hc)
      acquisitionDate <- fetchAcquisitionDate(hc)
      improvements <- fetchImprovements(hc)
      improvementsOptions <- displayImprovementsSectionCheck(rebasedValue, acquisitionDate)
      backUrl <- improvementsBackUrl(rebasedValue, acquisitionDate)
      route <- routeRequest(backUrl, improvements, improvementsOptions)
    } yield route
  }

  val submitImprovements = ValidateSession.async { implicit request =>

    def skipPRR(acquisitionDateModel: Option[AcquisitionDateModel], rebasedValueModel: Option[RebasedValueModel]): Boolean =
      (acquisitionDateModel, rebasedValueModel) match {
        case (Some(AcquisitionDateModel("No", _, _, _)), Some(rebasedValue)) if rebasedValue.rebasedValueAmt.isEmpty => true
        case (_, _) => false
      }

    def successRouteRequest(model: Option[TotalGainResultsModel], skipPRR: Boolean): Result = {

      if (model.isEmpty) Redirect(common.DefaultRoutes.missingDataRoute)
      else {
        val optionSeq = Seq(model.get.rebasedGain, model.get.timeApportionedGain).flatten
        val finalSeq = Seq(model.get.flatGain) ++ optionSeq

        (!finalSeq.forall(_ <= 0), skipPRR) match {
          case (true, false) => Redirect(routes.PrivateResidenceReliefController.privateResidenceRelief())
          case (true, true) => Redirect(controllers.routes.CustomerTypeController.customerType())
          case (_, _) => Redirect(routes.CheckYourAnswersController.checkYourAnswers())
        }
      }
    }

    def errorAction(errors: Form[ImprovementsModel], backUrl: String, improvementsOptions: Boolean) = {
      Future.successful(BadRequest(calculation.improvements(errors, improvementsOptions, backUrl)))
    }

    def successAction(rebasedValue: Option[RebasedValueModel],
                      acquisitionDate: Option[AcquisitionDateModel],
                      improvements: ImprovementsModel
                     ): Future[Result] = {

      val skipPrivateResidence = skipPRR(acquisitionDate, rebasedValue)

      for {
        save <- calcConnector.saveFormData(KeystoreKeys.improvements, improvements)
        allAnswersModel <- answersConstructor.getNRTotalGainAnswers
        gains <- calcConnector.calculateTotalGain(allAnswersModel)
      } yield successRouteRequest(gains, skipPrivateResidence)
    }

    def routeRequest(rebasedValue: Option[RebasedValueModel],
                     acquisitionDate: Option[AcquisitionDateModel],
                     backUrl: String,
                     improvementsOptions: Boolean): Future[Result] = {
      improvementsForm(improvementsOptions).bindFromRequest.fold(
        errors => errorAction(errors, backUrl, improvementsOptions),
        success => successAction(rebasedValue, acquisitionDate, success)
      )
    }

    for {
      rebasedValue <- fetchRebasedValue(hc)
      acquisitionDate <- fetchAcquisitionDate(hc)
      improvementsOptions <- displayImprovementsSectionCheck(rebasedValue, acquisitionDate)
      backUrl <- improvementsBackUrl(rebasedValue, acquisitionDate)
      route <- routeRequest(rebasedValue, acquisitionDate, backUrl, improvementsOptions)
    } yield route
  }
}
