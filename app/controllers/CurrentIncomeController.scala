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
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.CurrentIncomeForm._
import views.html.calculation
import models.{CurrentIncomeModel, PropertyLivedInModel}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.data.Form
import uk.gov.hmrc.play.http.HeaderCarrier
import controllers.utils.RecoverableFuture

import scala.concurrent.Future

object CurrentIncomeController extends CurrentIncomeController {
  val calcConnector = CalculatorConnector
}

trait CurrentIncomeController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector

  def getBackLink(implicit hc: HeaderCarrier): Future[String] = {
    calcConnector.fetchAndGetFormData[PropertyLivedInModel](KeystoreKeys.propertyLivedIn).map {
      case Some(PropertyLivedInModel(true)) => routes.PrivateResidenceReliefController.privateResidenceRelief.url
      case _ => routes.PropertyLivedInController.propertyLivedIn().url
    }
  }

  val currentIncome = ValidateSession.async { implicit request =>

    def getForm: Future[Form[CurrentIncomeModel]] = {
      calcConnector.fetchAndGetFormData[CurrentIncomeModel](KeystoreKeys.currentIncome).map {
        case(Some(data)) => currentIncomeForm.fill(data)
        case _ => currentIncomeForm
      }
    }

    (for {
      backLink <- getBackLink
      form <- getForm
    } yield Ok(calculation.currentIncome(form, backLink))).recoverToStart
  }

  val submitCurrentIncome = ValidateSession.async { implicit request =>

    def routeRequest(model: CurrentIncomeModel) = {
      if (model.currentIncome > 0) Future.successful(Redirect(routes.PersonalAllowanceController.personalAllowance()))
      else Future.successful(Redirect(routes.OtherPropertiesController.otherProperties()))
    }

    def successAction(model: CurrentIncomeModel) = {
      (for {
        _ <- calcConnector.saveFormData(KeystoreKeys.currentIncome, model)
        route <- routeRequest(model)
      } yield route).recoverToStart
    }

    currentIncomeForm.bindFromRequest.fold(
      errors => getBackLink.map{backLink => BadRequest(calculation.currentIncome(errors, backLink))},
      success => successAction(success)
    )
  }
}
