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

import common.KeystoreKeys
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import models.nonresident._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import forms.nonresident.BroughtForwardLossesForm._
import play.api.data.Form
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation

import scala.concurrent.Future

object BroughtForwardLossesController extends BroughtForwardLossesController {
  val calcConnector = CalculatorConnector
}

trait BroughtForwardLossesController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.DisposalDateController.disposalDate().url

  def generateBackLink(implicit hc: HeaderCarrier): Future[String] = {
    val getOtherProperties = calcConnector.fetchAndGetFormData[OtherPropertiesModel](KeystoreKeys.otherProperties)
    val getGainOrLoss = calcConnector.fetchAndGetFormData[PreviousLossOrGainModel](KeystoreKeys.NonResidentKeys.previousLossOrGain)
    val getGain = calcConnector.fetchAndGetFormData[HowMuchGainModel](KeystoreKeys.howMuchGain)
    val getLoss = calcConnector.fetchAndGetFormData[HowMuchLossModel](KeystoreKeys.howMuchLoss)

    for {
      otherPropertiesModel <- getOtherProperties
      gainOrLoss <- getGainOrLoss
      gain <- getGain
      loss <- getLoss
    } yield {
      (otherPropertiesModel, gainOrLoss) match {
        case (Some(OtherPropertiesModel("No")), _) => controllers.nonresident.routes.OtherPropertiesController.otherProperties().url
        case (_, Some(PreviousLossOrGainModel("Gain"))) if gain.get.howMuchGain > 0 =>
          controllers.nonresident.routes.HowMuchGainController.howMuchGain().url
        case (_, Some(PreviousLossOrGainModel("Loss"))) if loss.get.loss > 0 =>
          controllers.nonresident.routes.HowMuchLossController.howMuchLoss().url
        case _ => controllers.nonresident.routes.AnnualExemptAmountController.annualExemptAmount().url
      }
    }
  }

  val broughtForwardLosses = ValidateSession.async { implicit request =>

    val generateForm = calcConnector.fetchAndGetFormData[BroughtForwardLossesModel](KeystoreKeys.broughtForwardLosses).map {
      case Some(data) => broughtForwardLossesForm.fill(data)
      case _ => broughtForwardLossesForm
    }

    for {
      backLink <- generateBackLink(hc)
      form <- generateForm
    } yield Ok(calculation.nonresident.broughtForwardLosses(form, backLink))
  }

  val submitBroughtForwardLosses = ValidateSession.async { implicit request =>

    def successAction(model: BroughtForwardLossesModel) = {
      calcConnector.saveFormData[BroughtForwardLossesModel](KeystoreKeys.broughtForwardLosses, model)
      Future.successful(Redirect(controllers.nonresident.routes.CheckYourAnswersController.checkYourAnswers()))
    }

    def errorAction(form: Form[BroughtForwardLossesModel]) = {
      for {
        backLink <- generateBackLink(hc)
      } yield BadRequest(calculation.nonresident.broughtForwardLosses(form, backLink))
    }

    broughtForwardLossesForm.bindFromRequest.fold(
      errorAction,
      successAction
    )
  }
}