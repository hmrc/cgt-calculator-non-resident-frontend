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

import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.TaxDates
import config.ApplicationConfig
import connectors.CalculatorConnector
import constructors.DefaultCalculationElectionConstructor
import controllers.predicates.ValidActiveSession
import forms.AcquisitionDateForm._
import javax.inject.Inject
import models.DateModel
import play.api.Application
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class  AcquisitionDateController @Inject()(http: DefaultHttpClient,
                                           calcConnector: CalculatorConnector,
                                           calcElectionConstructor: DefaultCalculationElectionConstructor,
                                           mcc: MessagesControllerComponents)
                                          (implicit val applicationConfig: ApplicationConfig,
                                           implicit val application: Application)
                                              extends FrontendController(mcc) with ValidActiveSession with I18nSupport {


  val acquisitionDate: Action[AnyContent] = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[DateModel](KeystoreKeys.acquisitionDate).map {
      case Some(data) => Ok(calculation.acquisitionDate(acquisitionDateForm.fill(data)))
      case None => Ok(calculation.acquisitionDate(acquisitionDateForm))
    }
  }

  val submitAcquisitionDate: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(form: Form[DateModel]) = Future.successful(BadRequest(calculation.acquisitionDate(form)))

    def successAction(model: DateModel) = {
      calcConnector.saveFormData(KeystoreKeys.acquisitionDate, model).map(_ =>
        if(TaxDates.dateBeforeLegislationStart(model.day, model.month, model.year)) {
          Redirect(routes.WorthBeforeLegislationStartController.worthBeforeLegislationStart())
        } else {
          Redirect(routes.HowBecameOwnerController.howBecameOwner())
        }
      )
    }

    acquisitionDateForm.bindFromRequest.fold(errorAction, successAction)
  }
}
