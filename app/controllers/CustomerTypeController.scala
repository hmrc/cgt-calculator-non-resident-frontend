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
import common.nonresident.CustomerTypeKeys
import connectors.CalculatorConnector
import constructors.CalculationElectionConstructor
import controllers.predicates.ValidActiveSession
import forms.CustomerTypeForm._
import models.{CustomerTypeModel, RebasedValueModel}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object CustomerTypeController extends CustomerTypeController {
  val calcConnector = CalculatorConnector
}

trait CustomerTypeController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  val calcElectionConstructor = CalculationElectionConstructor

  def customerTypeBackUrl(implicit hc: HeaderCarrier): Future[String] = {

    def skipPRR(rebasedValue: RebasedValueModel): Boolean = {
      rebasedValue.rebasedValueAmt.isEmpty
    }

    calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue).map {
      case Some(rebasedValue) if skipPRR(rebasedValue) => routes.ImprovementsController.improvements().url
      case _ => routes.PrivateResidenceReliefController.privateResidenceRelief().url

    }
  }

  val customerType: Action[AnyContent] = Action.async { implicit request =>

    def routeRequest(backUrl: String): Future[Result] =
      calcConnector.fetchAndGetFormData[CustomerTypeModel](KeystoreKeys.customerType).map {
        case Some(data) => Ok(calculation.customerType(customerTypeForm.fill(data), backUrl))
        case None => Ok(calculation.customerType(customerTypeForm, backUrl))
      }

    for {
      backUrl <- customerTypeBackUrl
      result <- routeRequest(backUrl)
    } yield result
  }

  val submitCustomerType: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(form: Form[CustomerTypeModel]) = {
      for {
        backUrl <- customerTypeBackUrl
        result <- Future.successful(BadRequest(calculation.customerType(form, backUrl)))
      } yield result
    }

    def successAction(model: CustomerTypeModel) = {
      for {
        _ <- calcConnector.saveFormData[CustomerTypeModel](KeystoreKeys.customerType, model)
        route <- routeRequest(model)
      } yield route
    }

    def routeRequest(data: CustomerTypeModel): Future[Result] = {
      val view  = data.customerType match {
        case CustomerTypeKeys.individual => routes.CurrentIncomeController.currentIncome()
        case CustomerTypeKeys.trustee => routes.DisabledTrusteeController.disabledTrustee()
        case CustomerTypeKeys.personalRep => routes.OtherPropertiesController.otherProperties()
      }

      Future.successful(Redirect(view))
    }

    customerTypeForm.bindFromRequest.fold(errorAction, successAction)
  }
}
