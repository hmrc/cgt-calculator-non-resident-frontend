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
import config.ApplicationConfig
import connectors.CalculatorConnector
import constructors.DefaultCalculationElectionConstructor
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.BoughtForLessForm._
import javax.inject.Inject
import models.BoughtForLessModel
import play.api.{Application, Environment}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BoughtForLessController @Inject()(http: DefaultHttpClient,calcConnector: CalculatorConnector,
                                        calcElectionConstructor: DefaultCalculationElectionConstructor,
                                        mcc: MessagesControllerComponents)
                                       (implicit val applicationConfig: ApplicationConfig,
                                        implicit val application: Application)
                                          extends FrontendController(mcc) with ValidActiveSession with I18nSupport {


  val boughtForLess = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[BoughtForLessModel](KeystoreKeys.boughtForLess).map {
      case Some(data) => Ok(calculation.boughtForLess(boughtForLessForm.fill(data)))
      case None => Ok(calculation.boughtForLess(boughtForLessForm))
    }
  }

  val submitBoughtForLess = ValidateSession.async {implicit request =>

    def errorAction(errors: Form[BoughtForLessModel]) = Future.successful(BadRequest(calculation.boughtForLess(errors)))

    def routeRequest(model: BoughtForLessModel) = {
      if (model.boughtForLess) Future.successful(Redirect(routes.WorthWhenBoughtForLessController.worthWhenBoughtForLess()))
      else Future.successful(Redirect(routes.AcquisitionValueController.acquisitionValue()))
    }

    def successAction(model: BoughtForLessModel) = {
      (for {
        save <- calcConnector.saveFormData(KeystoreKeys.boughtForLess, model)
        route <- routeRequest(model)
      } yield route).recoverToStart
    }


    boughtForLessForm.bindFromRequest().fold(errorAction, successAction)
  }
}
