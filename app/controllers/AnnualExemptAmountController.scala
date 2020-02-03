/*
 * Copyright 2020 HM Revenue & Customs
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

import common.DefaultRoutes._
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.TaxDates
import config.ApplicationConfig
import connectors.CalculatorConnector
import constructors.DefaultCalculationElectionConstructor
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.AnnualExemptAmountForm._
import javax.inject.Inject
import models._
import play.api.Environment
import play.api.Play.current
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class AnnualExemptAmountController @Inject()(http: DefaultHttpClient,calcConnector: CalculatorConnector,
                                             calcElectionConstructor: DefaultCalculationElectionConstructor,
                                             mcc: MessagesControllerComponents)
                                            (implicit val applicationConfig: ApplicationConfig)
                                              extends FrontendController(mcc) with ValidActiveSession with I18nSupport {


  private def fetchAEA(taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    calcConnector.getFullAEA(taxYear)
  }

  private def fetchAnnualExemptAmount(implicit headerCarrier: HeaderCarrier): Future[Option[AnnualExemptAmountModel]] = {
    calcConnector.fetchAndGetFormData[AnnualExemptAmountModel](KeystoreKeys.annualExemptAmount)
  }

  private def fetchPreviousGainOrLoss(implicit hc: HeaderCarrier): Future[Option[PreviousLossOrGainModel]] = {
    calcConnector.fetchAndGetFormData[PreviousLossOrGainModel](KeystoreKeys.previousLossOrGain)
  }

  private def fetchPreviousLossAmount(gainOrLoss: String)(implicit hc: HeaderCarrier): Future[Option[HowMuchLossModel]] = {
    gainOrLoss match {
      case "Loss" => calcConnector.fetchAndGetFormData[HowMuchLossModel](KeystoreKeys.howMuchLoss)
      case _ => Future.successful(None)
    }
  }

  private def fetchPreviousGainAmount(gainOrLoss: String)(implicit hc: HeaderCarrier): Future[Option[HowMuchGainModel]] = {
    gainOrLoss match {
      case "Gain" => calcConnector.fetchAndGetFormData[HowMuchGainModel](KeystoreKeys.howMuchGain)
      case _ => Future.successful(None)
    }
  }

  private def fetchDisposalDate(implicit hc: HeaderCarrier): Future[Option[DateModel]] = {
    calcConnector.fetchAndGetFormData[DateModel](KeystoreKeys.disposalDate)
  }

  private def backUrl(lossOrGain: PreviousLossOrGainModel, gainAmount: Option[HowMuchGainModel], lossAmount: Option[HowMuchLossModel]): Future[String] = {
    (lossOrGain, gainAmount, lossAmount) match {
      case (PreviousLossOrGainModel("Neither"), None, None) =>
        Future.successful(routes.PreviousGainOrLossController.previousGainOrLoss().url)
      case (PreviousLossOrGainModel("Gain"), Some(data), None) if data.howMuchGain == 0 =>
        Future.successful(routes.HowMuchGainController.howMuchGain().url)
      case (PreviousLossOrGainModel("Loss"), None, Some(data)) if data.loss == 0 =>
        Future.successful(routes.HowMuchLossController.howMuchLoss().url)
      case _ => Future.successful(missingDataRoute)
    }
  }

  private def formatDisposalDate(disposalDateModel: Option[DateModel]): Future[String] = {
    val date = disposalDateModel.get
    Future.successful(s"${date.year}-${date.month}-${date.day}")
  }

  private def getTaxYear(details: Option[TaxYearModel]) = {
    val taxYear = details.get
    Future.successful(TaxDates.taxYearStringToInteger(taxYear.calculationTaxYear))
  }

  val annualExemptAmount: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(model: Option[AnnualExemptAmountModel], maxAEA: BigDecimal, backUrl: String): Future[Result] = {
      calcConnector.fetchAndGetFormData[AnnualExemptAmountModel](KeystoreKeys.annualExemptAmount).map {
        case Some(data) => Ok(calculation.annualExemptAmount(annualExemptAmountForm().fill(data), maxAEA, backUrl))
        case None => Ok(calculation.annualExemptAmount(annualExemptAmountForm(), maxAEA, backUrl))
      }
    }

    (for {
      disposalDate <- fetchDisposalDate(hc)
      date <- formatDisposalDate(disposalDate)
      closestTaxYear <- calcConnector.getTaxYear(date)
      taxYear <- getTaxYear(closestTaxYear)
      maxAEA <- fetchAEA(taxYear)
      annualExemptAmount <- fetchAnnualExemptAmount(hc)
      previousLossOrGain <- fetchPreviousGainOrLoss(hc)
      gainAmount <- fetchPreviousGainAmount(previousLossOrGain.get.previousLossOrGain)
      lossAmount <- fetchPreviousLossAmount(previousLossOrGain.get.previousLossOrGain)
      backUrl <- backUrl(previousLossOrGain.get, gainAmount, lossAmount)
      finalResult <- routeRequest(annualExemptAmount, maxAEA.get, backUrl)
    } yield finalResult).recoverToStart
  }

  val submitAnnualExemptAmount: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(form: Form[AnnualExemptAmountModel], maxAEA: BigDecimal, backUrl: String) = {
      Future.successful(BadRequest(calculation.annualExemptAmount(form, maxAEA, backUrl)))
    }

    def successAction(model: AnnualExemptAmountModel) = {
      calcConnector.saveFormData(KeystoreKeys.annualExemptAmount, model).map(_ =>
        Redirect(routes.BroughtForwardLossesController.broughtForwardLosses()))
    }

    def routeRequest(maxAEA: BigDecimal, backUrl: String): Future[Result] = {
      annualExemptAmountForm(maxAEA).bindFromRequest.fold(
        errors => errorAction(errors, maxAEA, backUrl),
        success => successAction(success))
    }

    (for {
      disposalDate <- fetchDisposalDate(hc)
      date <- formatDisposalDate(disposalDate)
      closestTaxYear <- calcConnector.getTaxYear(date)
      taxYear <- getTaxYear(closestTaxYear)
      maxAEA <- fetchAEA(taxYear)
      previousLossOrGain <- fetchPreviousGainOrLoss(hc)
      gainAmount <- fetchPreviousGainAmount(previousLossOrGain.get.previousLossOrGain)
      lossAmount <- fetchPreviousLossAmount(previousLossOrGain.get.previousLossOrGain)
      backUrl <- backUrl(previousLossOrGain.get, gainAmount, lossAmount)
      finalResult <- routeRequest(maxAEA.get, backUrl)
    } yield finalResult).recoverToStart
  }
}
