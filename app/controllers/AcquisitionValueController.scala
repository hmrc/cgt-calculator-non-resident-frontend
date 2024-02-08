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
import forms.AcquisitionValueForm._

import javax.inject.Inject
import models.AcquisitionValueModel
import play.api.i18n.I18nSupport
import play.api.mvc.MessagesControllerComponents
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.acquisitionValue

import scala.concurrent.{ExecutionContext, Future}

class AcquisitionValueController @Inject()(http: DefaultHttpClient,
                                           sessionCacheService: SessionCacheService,
                                           mcc: MessagesControllerComponents,
                                           acquisitionValueView: acquisitionValue
                                          )(implicit ec: ExecutionContext) extends FrontendController(mcc) with ValidActiveSession with I18nSupport {


  val acquisitionValue = ValidateSession.async { implicit request =>
    sessionCacheService.fetchAndGetFormData[AcquisitionValueModel](KeystoreKeys.acquisitionValue).map {
      case Some(data) => Ok(acquisitionValueView(acquisitionValueForm.fill(data)))
      case None => Ok(acquisitionValueView(acquisitionValueForm))
    }
  }

  val submitAcquisitionValue = ValidateSession.async { implicit request =>
    acquisitionValueForm.bindFromRequest().fold(
      errors => Future.successful(BadRequest(acquisitionValueView(errors))),
      success => {
        sessionCacheService.saveFormData(KeystoreKeys.acquisitionValue, success).map(_ => Redirect(routes.AcquisitionCostsController.acquisitionCosts))
      }
    )
  }
}
