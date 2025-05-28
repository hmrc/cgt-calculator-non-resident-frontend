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
import forms.WorthWhenInherited.worthWhenInheritedForm
import models.AcquisitionValueModel
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.worthWhenInherited

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WorthWhenInheritedController @Inject()(val http: DefaultHttpClient,
                                             sessionCacheService: SessionCacheService,
                                             mcc: MessagesControllerComponents,
                                             worthWhenInheritedView: worthWhenInherited
                                            )(implicit ec: ExecutionContext)
  extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val worthWhenInherited: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheService.fetchAndGetFormData[AcquisitionValueModel](KeystoreKeys.acquisitionMarketValue).map {
      case Some(data) => Ok(worthWhenInheritedView(worthWhenInheritedForm.fill(data)))
      case None => Ok(worthWhenInheritedView(worthWhenInheritedForm))
    }
  }

  val submitWorthWhenInherited: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(form: Form[AcquisitionValueModel]) = Future.successful(BadRequest(worthWhenInheritedView(form)))

    def successAction(model: AcquisitionValueModel) = {
      sessionCacheService.saveFormData(KeystoreKeys.acquisitionMarketValue, model).map(_ =>
        Redirect(routes.AcquisitionCostsController.acquisitionCosts))
    }

    worthWhenInheritedForm.bindFromRequest().fold(errorAction, successAction)
  }
}
