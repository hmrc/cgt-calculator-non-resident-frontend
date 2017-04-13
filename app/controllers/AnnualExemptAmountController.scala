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

import common.TaxDates
import common.DefaultRoutes._
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.nonresident.CustomerTypeKeys
import connectors.CalculatorConnector
import constructors.CalculationElectionConstructor
import controllers.predicates.ValidActiveSession
import forms.AnnualExemptAmountForm._
import models._
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object AnnualExemptAmountController extends AnnualExemptAmountController{
  val calcConnector = CalculatorConnector
}

trait AnnualExemptAmountController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  val calcElectionConstructor = CalculationElectionConstructor

  private def fetchMaxAEA(isFullAEA: Boolean, taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    if (isFullAEA) {
      calcConnector.getFullAEA(taxYear)
    }
    else {
      calcConnector.getPartialAEA(taxYear)
    }
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

  private def fetchDisposalDate(implicit hc: HeaderCarrier): Future[Option[DisposalDateModel]] = {
    calcConnector.fetchAndGetFormData[DisposalDateModel](KeystoreKeys.disposalDate)
  }

  private def customerType(implicit hc: HeaderCarrier): Future[Option[CustomerTypeModel]] = {
    calcConnector.fetchAndGetFormData[CustomerTypeModel](KeystoreKeys.customerType)
  }

  private def trusteeAEA(customerTypeVal: String)(implicit hc: HeaderCarrier): Future[Boolean] = {
    customerTypeVal match {
      case CustomerTypeKeys.trustee =>
        calcConnector.fetchAndGetFormData[DisabledTrusteeModel](KeystoreKeys.disabledTrustee).map {
          disabledTrusteeModel => if (disabledTrusteeModel.get.isVulnerable == "No") false else true
        }
      case _ => Future.successful(true)
    }
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

  private def formatDisposalDate(disposalDateModel: Option[DisposalDateModel]): Future[String] = {
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

    for {
      disposalDate <- fetchDisposalDate(hc)
      date <- formatDisposalDate(disposalDate)
      customerTypeVal <- customerType(hc)
      isDisabledTrustee <- trusteeAEA(customerTypeVal.get.customerType)
      closestTaxYear <- calcConnector.getTaxYear(date)
      taxYear <- getTaxYear(closestTaxYear)
      maxAEA <- fetchMaxAEA(isDisabledTrustee, taxYear)
      annualExemptAmount <- fetchAnnualExemptAmount(hc)
      previousLossOrGain <- fetchPreviousGainOrLoss(hc)
      gainAmount <- fetchPreviousGainAmount(previousLossOrGain.get.previousLossOrGain)
      lossAmount <- fetchPreviousLossAmount(previousLossOrGain.get.previousLossOrGain)
      backUrl <- backUrl(previousLossOrGain.get, gainAmount, lossAmount)
      finalResult <- routeRequest(annualExemptAmount, maxAEA.get, backUrl)
    } yield finalResult
  }

  val submitAnnualExemptAmount: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(form: Form[AnnualExemptAmountModel], maxAEA: BigDecimal, backUrl: String) = {
      Future.successful(BadRequest(calculation.annualExemptAmount(form, maxAEA, backUrl)))
    }

    def successAction(model: AnnualExemptAmountModel) = {
      calcConnector.saveFormData(KeystoreKeys.annualExemptAmount, model)
      Future.successful(Redirect(routes.BroughtForwardLossesController.broughtForwardLosses()))
    }

    def routeRequest(maxAEA: BigDecimal, backUrl: String): Future[Result] = {
      annualExemptAmountForm(maxAEA).bindFromRequest.fold(
        errors => errorAction(errors, maxAEA, backUrl),
        success => successAction(success))
    }

    for {
      disposalDate <- fetchDisposalDate(hc)
      date <- formatDisposalDate(disposalDate)
      customerTypeVal <- customerType(hc)
      isDisabledTrustee <- trusteeAEA(customerTypeVal.get.customerType)
      closestTaxYear <- calcConnector.getTaxYear(date)
      taxYear <- getTaxYear(closestTaxYear)
      maxAEA <- fetchMaxAEA(isDisabledTrustee, taxYear)
      previousLossOrGain <- fetchPreviousGainOrLoss(hc)
      gainAmount <- fetchPreviousGainAmount(previousLossOrGain.get.previousLossOrGain)
      lossAmount <- fetchPreviousLossAmount(previousLossOrGain.get.previousLossOrGain)
      backUrl <- backUrl(previousLossOrGain.get, gainAmount, lossAmount)
      finalResult <- routeRequest(maxAEA.get, backUrl)
    } yield finalResult
  }
}
