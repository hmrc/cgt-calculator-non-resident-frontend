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
import forms.ClaimingReliefsForm.claimingReliefsForm
import models.ClaimingReliefsModel
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.claimingReliefs

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ClaimingReliefsController @Inject()(http: DefaultHttpClient,
                                          sessionCacheService: SessionCacheService,
                                          mcc: MessagesControllerComponents,
                                          claimingReliefsView: claimingReliefs)(implicit ec: ExecutionContext)
                                            extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val claimingReliefs: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheService.fetchAndGetFormData[ClaimingReliefsModel](KeystoreKeys.claimingReliefs).map {
      case Some(data) => Ok(claimingReliefsView(claimingReliefsForm.fill(data)))
      case None => Ok(claimingReliefsView(claimingReliefsForm))
    }
  }

  val submitClaimingReliefs: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(errors: Form[ClaimingReliefsModel]) =
      Future.successful(BadRequest(claimingReliefsView(errors)))

    def successAction(model: ClaimingReliefsModel) = {
      sessionCacheService.saveFormData(KeystoreKeys.claimingReliefs, model).map(_ =>
        Redirect(routes.CalculationElectionController.calculationElection))
    }

    claimingReliefsForm.bindFromRequest().fold(errorAction, successAction)
  }
}