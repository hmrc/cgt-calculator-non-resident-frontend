/*
 * Copyright 2024 HM Revenue & Customs
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
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.BroughtForwardLossesForm._
import models._
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.calculation.broughtForwardLosses

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BroughtForwardLossesController @Inject()(val calcConnector: CalculatorConnector,
                                               sessionCacheService: SessionCacheService,
                                               mcc: MessagesControllerComponents,
                                               broughtForwardLossesView: broughtForwardLosses)(implicit ec: ExecutionContext)
                                              extends FrontendController(mcc) with ValidActiveSession with I18nSupport {


  private def generateBackLink(implicit request: Request[?]): Future[String] = {
    val getOtherProperties = sessionCacheService.fetchAndGetFormData[OtherPropertiesModel](KeystoreKeys.otherProperties)
    val getGainOrLoss = sessionCacheService.fetchAndGetFormData[PreviousLossOrGainModel](KeystoreKeys.previousLossOrGain)
    val getGain = sessionCacheService.fetchAndGetFormData[HowMuchGainModel](KeystoreKeys.howMuchGain)
    val getLoss = sessionCacheService.fetchAndGetFormData[HowMuchLossModel](KeystoreKeys.howMuchLoss)

    for {
      otherPropertiesModel <- getOtherProperties
      gainOrLoss <- getGainOrLoss
      gain <- getGain
      loss <- getLoss
    } yield {
      (otherPropertiesModel, gainOrLoss) match {
        case (Some(OtherPropertiesModel("No")), _) => controllers.routes.OtherPropertiesController.otherProperties.url
        case (_, Some(PreviousLossOrGainModel("Gain"))) if gain.get.howMuchGain > 0 =>
          controllers.routes.HowMuchGainController.howMuchGain.url
        case (_, Some(PreviousLossOrGainModel("Loss"))) if loss.get.loss > 0 =>
          controllers.routes.HowMuchLossController.howMuchLoss.url
        case _ => controllers.routes.AnnualExemptAmountController.annualExemptAmount.url
      }
    }
  }

  val broughtForwardLosses: Action[AnyContent] = ValidateSession.async { implicit request =>

    val generateForm = sessionCacheService.fetchAndGetFormData[BroughtForwardLossesModel](KeystoreKeys.broughtForwardLosses).map {
      case Some(data) => broughtForwardLossesForm.fill(data)
      case _ => broughtForwardLossesForm
    }

    (for {
      backLink <- generateBackLink(using request)
      form <- generateForm
    } yield Ok(broughtForwardLossesView(form, backLink))).recoverToStart
  }

  val submitBroughtForwardLosses: Action[AnyContent] = ValidateSession.async { implicit request =>

    def successAction(model: BroughtForwardLossesModel) = {
      sessionCacheService.saveFormData[BroughtForwardLossesModel](KeystoreKeys.broughtForwardLosses, model).map(_ =>
        Redirect(controllers.routes.CheckYourAnswersController.checkYourAnswers))
    }

    def errorAction(form: Form[BroughtForwardLossesModel]) = {
      (for {
        backLink <- generateBackLink(using request)
      } yield BadRequest(broughtForwardLossesView(form, backLink))).recoverToStart
    }

    broughtForwardLossesForm.bindFromRequest().fold(
      errorAction,
      successAction
    )
  }
}
