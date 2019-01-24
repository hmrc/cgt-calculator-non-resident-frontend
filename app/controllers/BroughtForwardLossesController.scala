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

import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import config.ApplicationConfig
import connectors.CalculatorConnector
import constructors.DefaultCalculationElectionConstructor
import controllers.predicates.ValidActiveSession
import forms.BroughtForwardLossesForm._
import views.html.calculation
import models._
import play.api.data.Form
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import controllers.utils.RecoverableFuture
import javax.inject.Inject
import play.api.Environment

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

class BroughtForwardLossesController @Inject()(environment: Environment,
                                               http: DefaultHttpClient,calcConnector: CalculatorConnector)(implicit val applicationConfig: ApplicationConfig)
                                                extends FrontendController with ValidActiveSession {


  def generateBackLink(implicit hc: HeaderCarrier): Future[String] = {
    val getOtherProperties = calcConnector.fetchAndGetFormData[OtherPropertiesModel](KeystoreKeys.otherProperties)
    val getGainOrLoss = calcConnector.fetchAndGetFormData[PreviousLossOrGainModel](KeystoreKeys.previousLossOrGain)
    val getGain = calcConnector.fetchAndGetFormData[HowMuchGainModel](KeystoreKeys.howMuchGain)
    val getLoss = calcConnector.fetchAndGetFormData[HowMuchLossModel](KeystoreKeys.howMuchLoss)

    for {
      otherPropertiesModel <- getOtherProperties
      gainOrLoss <- getGainOrLoss
      gain <- getGain
      loss <- getLoss
    } yield {
      (otherPropertiesModel, gainOrLoss) match {
        case (Some(OtherPropertiesModel("No")), _) => controllers.routes.OtherPropertiesController.otherProperties().url
        case (_, Some(PreviousLossOrGainModel("Gain"))) if gain.get.howMuchGain > 0 =>
          controllers.routes.HowMuchGainController.howMuchGain().url
        case (_, Some(PreviousLossOrGainModel("Loss"))) if loss.get.loss > 0 =>
          controllers.routes.HowMuchLossController.howMuchLoss().url
        case _ => controllers.routes.AnnualExemptAmountController.annualExemptAmount().url
      }
    }
  }

  val broughtForwardLosses = ValidateSession.async { implicit request =>

    val generateForm = calcConnector.fetchAndGetFormData[BroughtForwardLossesModel](KeystoreKeys.broughtForwardLosses).map {
      case Some(data) => broughtForwardLossesForm.fill(data)
      case _ => broughtForwardLossesForm
    }

    (for {
      backLink <- generateBackLink(hc)
      form <- generateForm
    } yield Ok(calculation.broughtForwardLosses(form, backLink))).recoverToStart
  }

  val submitBroughtForwardLosses = ValidateSession.async { implicit request =>

    def successAction(model: BroughtForwardLossesModel) = {
      calcConnector.saveFormData[BroughtForwardLossesModel](KeystoreKeys.broughtForwardLosses, model).map(_ =>
        Redirect(controllers.routes.CheckYourAnswersController.checkYourAnswers()))
    }

    def errorAction(form: Form[BroughtForwardLossesModel]) = {
      (for {
        backLink <- generateBackLink(hc)
      } yield BadRequest(calculation.broughtForwardLosses(form, backLink))).recoverToStart
    }

    broughtForwardLossesForm.bindFromRequest.fold(
      errorAction,
      successAction
    )
  }
}
