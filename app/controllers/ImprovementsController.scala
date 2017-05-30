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
import play.api.mvc.{Action, AnyContent, Result}
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
    case (_, Some(AcquisitionDateModel(day, month, year)))
      if TaxDates.dateAfterStart(day, month, year) =>
      Future.successful(routes.AcquisitionCostsController.acquisitionCosts().url)
    case (Some(RebasedValueModel(None)), _) =>
      Future.successful(routes.RebasedValueController.rebasedValue().url)
    case (Some(RebasedValueModel(Some(_))), Some(AcquisitionDateModel(day, month, year)))
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
      case (Some(value), Some(data))
        if !TaxDates.dateAfterStart(data.day, data.month, data.year) && value.rebasedValueAmt.isDefined =>
          Future.successful(true)
      case (_, _) =>
        Future.successful(false)
    }
  }

  private def ownerBeforeLegislationStartCheck(model: Option[AcquisitionDateModel]): Future[Boolean] = {
    if(TaxDates.dateBeforeLegislationStart(model.get.day, model.get.month, model.get.year)) Future.successful(true)
    else Future.successful(false)
  }

  val improvements: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String, improvementsModel: Option[ImprovementsModel], improvementsOptions: Boolean,
                     ownerBeforeLegislationStart: Boolean): Future[Result] = {
      improvementsModel match {
        case Some(data) =>
          Future.successful(Ok(calculation.improvements(improvementsForm(improvementsOptions).fill(data),
            improvementsOptions, backUrl, ownerBeforeLegislationStart)))
        case None =>
          Future.successful(Ok(calculation.improvements(improvementsForm(improvementsOptions),
            improvementsOptions, backUrl, ownerBeforeLegislationStart)))
      }
    }

    for {
      rebasedValue <- fetchRebasedValue(hc)
      acquisitionDate <- fetchAcquisitionDate(hc)
      improvements <- fetchImprovements(hc)
      improvementsOptions <- displayImprovementsSectionCheck(rebasedValue, acquisitionDate)
      backUrl <- improvementsBackUrl(rebasedValue, acquisitionDate)
      ownerBeforeLegislationStart <- ownerBeforeLegislationStartCheck(acquisitionDate)
      route <- routeRequest(backUrl, improvements, improvementsOptions, ownerBeforeLegislationStart)
    } yield route
  }

  val submitImprovements: Action[AnyContent] = ValidateSession.async { implicit request =>

    def skipPRR(rebasedValueModel: Option[RebasedValueModel]): Boolean =
      rebasedValueModel match {
        case (Some(rebasedValue)) => rebasedValue.rebasedValueAmt.isEmpty
        case _ => false
      }

    def successRouteRequest(model: Option[TotalGainResultsModel], skipPRR: Boolean): Result = {

      if (model.isEmpty) Redirect(common.DefaultRoutes.missingDataRoute)
      else {
        val optionSeq = Seq(model.get.rebasedGain, model.get.timeApportionedGain).flatten
        val finalSeq = Seq(model.get.flatGain) ++ optionSeq

        (!finalSeq.forall(_ <= 0), skipPRR) match {
          case (true, false) => Redirect(routes.PropertyLivedInController.propertyLivedIn())
          case (true, true) => Redirect(controllers.routes.CurrentIncomeController.currentIncome())
          case (_, _) => Redirect(routes.CheckYourAnswersController.checkYourAnswers())
        }
      }
    }

    def errorAction(errors: Form[ImprovementsModel], backUrl: String, improvementsOptions: Boolean,
                    ownerBeforeLegislationStart: Boolean) = {
      Future.successful(BadRequest(calculation.improvements(errors, improvementsOptions, backUrl, ownerBeforeLegislationStart)))
    }

    def successAction(rebasedValue: Option[RebasedValueModel],
                      acquisitionDate: Option[AcquisitionDateModel],
                      improvements: ImprovementsModel
                     ): Future[Result] = {

      val skipPrivateResidence = skipPRR(rebasedValue)

      for {
        save <- calcConnector.saveFormData(KeystoreKeys.improvements, improvements)
        allAnswersModel <- answersConstructor.getNRTotalGainAnswers
        gains <- calcConnector.calculateTotalGain(allAnswersModel)
      } yield successRouteRequest(gains, skipPrivateResidence)
    }

    def routeRequest(rebasedValue: Option[RebasedValueModel],
                     acquisitionDate: Option[AcquisitionDateModel],
                     backUrl: String,
                     improvementsOptions: Boolean,
                     ownerBeforeLegislationStart: Boolean): Future[Result] = {
      improvementsForm(improvementsOptions).bindFromRequest.fold(
        errors => errorAction(errors, backUrl, improvementsOptions, ownerBeforeLegislationStart),
        success => successAction(rebasedValue, acquisitionDate, success)
      )
    }

    for {
      rebasedValue <- fetchRebasedValue(hc)
      acquisitionDate <- fetchAcquisitionDate(hc)
      improvementsOptions <- displayImprovementsSectionCheck(rebasedValue, acquisitionDate)
      backUrl <- improvementsBackUrl(rebasedValue, acquisitionDate)
      ownerBeforeLegislationStart <- ownerBeforeLegislationStartCheck(acquisitionDate)
      route <- routeRequest(rebasedValue, acquisitionDate, backUrl, improvementsOptions, ownerBeforeLegislationStart)
    } yield route
  }
}
