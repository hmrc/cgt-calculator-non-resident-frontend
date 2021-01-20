/*
 * Copyright 2021 HM Revenue & Customs
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

import java.time.LocalDate

import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.TaxDates
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.RebasedValueForm._
import javax.inject.Inject
import models.{DateModel, RebasedValueModel}
import play.api.Application
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation

import scala.concurrent.ExecutionContext.Implicits.global

class RebasedValueController @Inject()(http: DefaultHttpClient,calcConnector: CalculatorConnector,
                                       mcc: MessagesControllerComponents)(implicit val applicationConfig: ApplicationConfig,
                                                                          implicit val application: Application)
                                        extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  def backLink(acquisitionDate: Option[DateModel]): String = {

    val localDate: Option[LocalDate] = acquisitionDate.map{x => x.get}

    localDate match {
      case Some(x) => {
        if (TaxDates.dateBeforeLegislationStart(x)) controllers.routes.CostsAtLegislationStartController.costsAtLegislationStart().url
        else controllers.routes.AcquisitionCostsController.acquisitionCosts().url
      }
      case _ => controllers.routes.AcquisitionCostsController.acquisitionCosts().url
    }
  }

  val rebasedValue: Action[AnyContent] = ValidateSession.async { implicit request =>
    (for {
      rebasedValueModel <- calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue)
      acquisitionDate <- calcConnector.fetchAndGetFormData[DateModel](KeystoreKeys.acquisitionDate)
    } yield rebasedValueModel match {
      case Some(data) => Ok(calculation.rebasedValue(rebasedValueForm.fill(data), backLink(acquisitionDate)))
      case None => Ok(calculation.rebasedValue(rebasedValueForm, backLink(acquisitionDate)))
    }).recoverToStart
  }

  val submitRebasedValue: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(errors: Form[RebasedValueModel]) = {
      calcConnector.fetchAndGetFormData[DateModel](KeystoreKeys.acquisitionDate).map{
        x => BadRequest(calculation.rebasedValue(errors, backLink(x)))
      }
    }

    def successAction(model: RebasedValueModel) = {
      calcConnector.saveFormData(KeystoreKeys.rebasedValue, model).map(_ =>
        Redirect(routes.RebasedCostsController.rebasedCosts()))
    }

    rebasedValueForm.bindFromRequest.fold(errorAction, successAction)
  }
}
