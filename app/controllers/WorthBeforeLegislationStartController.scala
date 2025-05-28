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
import controllers.predicates.ValidActiveSession
import forms.WorthBeforeLegislationStartForm._
import models.WorthBeforeLegislationStartModel
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.worthBeforeLegislationStart

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WorthBeforeLegislationStartController @Inject()(val http: DefaultHttpClient,
                                                      sessionCacheService: SessionCacheService,
                                                      mcc: MessagesControllerComponents,
                                                      worthBeforeLegislationStartView: worthBeforeLegislationStart)
                                                     (implicit ec: ExecutionContext)
                                                        extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val worthBeforeLegislationStart: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheService.fetchAndGetFormData[WorthBeforeLegislationStartModel](KeystoreKeys.worthBeforeLegislationStart).map {
      case Some(data) => Ok(worthBeforeLegislationStartView(worthBeforeLegislationStartForm.fill(data)))
      case None => Ok(worthBeforeLegislationStartView(worthBeforeLegislationStartForm))
    }
  }

  val submitWorthBeforeLegislationStart: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(form: Form[WorthBeforeLegislationStartModel]) = Future.successful(BadRequest(worthBeforeLegislationStartView(form)))

    def successAction(model: WorthBeforeLegislationStartModel) = {
      sessionCacheService.saveFormData(KeystoreKeys.worthBeforeLegislationStart, model).map(_ =>
        Redirect(routes.CostsAtLegislationStartController.costsAtLegislationStart))
    }

    worthBeforeLegislationStartForm.bindFromRequest().fold(errorAction, successAction)
  }
}
