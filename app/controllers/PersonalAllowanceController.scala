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
import controllers.predicates.ValidActiveSession
import forms.PersonalAllowanceForm._
import models.{DisposalDateModel, PersonalAllowanceModel, TaxYearModel}
import play.api.Play.current
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.calculation

import scala.concurrent.Future

object PersonalAllowanceController extends PersonalAllowanceController {
  val calcConnector = CalculatorConnector
}

trait PersonalAllowanceController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector

  val personalAllowance: Action[AnyContent] = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[PersonalAllowanceModel](KeystoreKeys.personalAllowance).map {
      case Some(data) => Ok(calculation.personalAllowance(personalAllowanceForm().fill(data)))
      case None => Ok(calculation.personalAllowance(personalAllowanceForm()))
    }
  }

  val submitPersonalAllowance: Action[AnyContent] = ValidateSession.async { implicit request =>

    def formatDisposalDate(disposalDateModel: Option[DisposalDateModel]): Future[String] = {
      val date = disposalDateModel.get
      Future.successful(s"${date.year}-${date.month}-${date.day}")
    }

    def getTaxYear(details: Option[TaxYearModel]) = {
      val taxYear = details.get
      Future.successful(TaxDates.taxYearStringToInteger(taxYear.calculationTaxYear))
    }

    def getPersonalAllowanceForYear: Future[BigDecimal] = {
      for {
        disposalDate <- calcConnector.fetchAndGetFormData[DisposalDateModel](KeystoreKeys.disposalDate)
        date <- formatDisposalDate(disposalDate)
        taxYearDetails <- calcConnector.getTaxYear(date)
        taxYear <- getTaxYear(taxYearDetails)
        yearsAllowance <- calcConnector.getPA(taxYear)
      } yield yearsAllowance.get
    }

    def errorAction(form: Form[PersonalAllowanceModel]) = {
      Future.successful(BadRequest(calculation.personalAllowance(form)))
    }

    def successAction(model: PersonalAllowanceModel) = {
      calcConnector.saveFormData[PersonalAllowanceModel](KeystoreKeys.personalAllowance, model)
      Future.successful(Redirect(routes.OtherPropertiesController.otherProperties()))
    }

    for {
      allowance <- getPersonalAllowanceForYear
      action <- personalAllowanceForm(allowance).bindFromRequest.fold(errorAction, successAction)
    } yield action
  }
}
