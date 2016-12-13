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

import common.DefaultRoutes._
import common.KeystoreKeys
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.nonresident.OtherPropertiesForm._
import models.nonresident.{CurrentIncomeModel, CustomerTypeModel, OtherPropertiesModel}
import play.api.data.Form
import play.api.mvc.Result
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation

import scala.concurrent.Future

object OtherPropertiesController extends OtherPropertiesController {
  val calcConnector = CalculatorConnector
}

trait OtherPropertiesController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.DisposalDateController.disposalDate().url

  private def otherPropertiesBackUrl(implicit hc: HeaderCarrier): Future[String] =
    calcConnector.fetchAndGetFormData[CustomerTypeModel](KeystoreKeys.customerType).flatMap {
      case Some(CustomerTypeModel("individual")) =>
        calcConnector.fetchAndGetFormData[CurrentIncomeModel](KeystoreKeys.currentIncome).flatMap {
          case Some(data) if data.currentIncome == 0 => Future.successful(routes.CurrentIncomeController.currentIncome().url)
          case _ => Future.successful(routes.PersonalAllowanceController.personalAllowance().url)
        }
      case Some(CustomerTypeModel("trustee")) => Future.successful(routes.DisabledTrusteeController.disabledTrustee().url)
      case Some(_) => Future.successful(routes.CustomerTypeController.customerType().url)
      case _ => Future.successful(missingDataRoute)
    }

  val otherProperties = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String): Future[Result] = {
      calcConnector.fetchAndGetFormData[OtherPropertiesModel](KeystoreKeys.otherProperties).map {
        case Some(data) => Ok(calculation.nonresident.otherProperties(otherPropertiesForm.fill(data), backUrl))
        case _ => Ok(calculation.nonresident.otherProperties(otherPropertiesForm, backUrl))
      }
    }

    for {
      backUrl <- otherPropertiesBackUrl
      finalResult <- routeRequest(backUrl)
    } yield finalResult
  }

  val submitOtherProperties = ValidateSession.async { implicit request =>
    def errorAction(form: Form[OtherPropertiesModel]) = {
      for {
        url <- otherPropertiesBackUrl
        result <- Future.successful(BadRequest(calculation.nonresident.otherProperties(form, url)))
      } yield result
    }

    def successAction(model: OtherPropertiesModel) = {
      for {
        save <- calcConnector.saveFormData[OtherPropertiesModel](KeystoreKeys.otherProperties, model)
        route <- routeRequest(model)
      } yield route
    }

    def routeRequest(data: OtherPropertiesModel): Future[Result] = {
      data.otherProperties match {
        case "Yes" => Future.successful(Redirect(routes.PreviousGainOrLossController.previousGainOrLoss()))
        case "No" => Future.successful(Redirect(routes.BroughtForwardLossesController.broughtForwardLosses()))
      }
    }

    otherPropertiesForm.bindFromRequest.fold(errorAction, successAction)
  }
}
