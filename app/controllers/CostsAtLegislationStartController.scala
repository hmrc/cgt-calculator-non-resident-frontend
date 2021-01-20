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

import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.CostsAtLegislationStartForm._
import javax.inject.Inject
import models.CostsAtLegislationStartModel
import play.api.Application
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.{calculation => views}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CostsAtLegislationStartController @Inject()(http: DefaultHttpClient,calcConnector: CalculatorConnector,
                                                  mcc: MessagesControllerComponents)
                                                 (implicit val applicationConfig: ApplicationConfig,
                                                  implicit val application: Application)
                                                    extends FrontendController(mcc) with ValidActiveSession with I18nSupport {


  val costsAtLegislationStart: Action[AnyContent] = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[CostsAtLegislationStartModel](KeystoreKeys.costAtLegislationStart).map {
      case Some(data) => Ok(views.costsAtLegislationStart(costsAtLegislationStartForm.fill(data)))
      case None => Ok(views.costsAtLegislationStart(costsAtLegislationStartForm))
    }
  }

  val submitCostsAtLegislationStart: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(form: Form[CostsAtLegislationStartModel]) = {
      Future.successful(BadRequest(views.costsAtLegislationStart(form)))
    }

    def successAction(model: CostsAtLegislationStartModel) = {
      calcConnector.saveFormData(KeystoreKeys.costAtLegislationStart, model).map { _ =>
        Redirect(routes.RebasedValueController.rebasedValue())
      }
    }

    costsAtLegislationStartForm.bindFromRequest.fold(errorAction, successAction)
  }
}
