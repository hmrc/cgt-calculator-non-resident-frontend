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
import controllers.predicates.ValidActiveSession
import forms.AcquisitionMarketValueForm._
import javax.inject.Inject
import models.AcquisitionValueModel
import play.api.Environment
import play.api.data.Form
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.{calculation => views}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.i18n.I18nSupport
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class WorthWhenInheritedController @Inject()(http: DefaultHttpClient,calcConnector: CalculatorConnector,
                                             mcc: MessagesControllerComponents)(implicit val applicationConfig: ApplicationConfig)
  extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val worthWhenInherited = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[AcquisitionValueModel](KeystoreKeys.acquisitionMarketValue).map {
      case Some(data) => Ok(views.worthWhenInherited(acquisitionMarketValueForm.fill(data)))
      case None => Ok(views.worthWhenInherited(acquisitionMarketValueForm))
    }
  }

  val submitWorthWhenInherited = ValidateSession.async { implicit request =>

    def errorAction(form: Form[AcquisitionValueModel]) = Future.successful(BadRequest(views.worthWhenInherited(form)))

    def successAction(model: AcquisitionValueModel) = {
      calcConnector.saveFormData(KeystoreKeys.acquisitionMarketValue, model).map(_ =>
        Redirect(routes.AcquisitionCostsController.acquisitionCosts()))
    }

    acquisitionMarketValueForm.bindFromRequest.fold(errorAction, successAction)
  }
}
