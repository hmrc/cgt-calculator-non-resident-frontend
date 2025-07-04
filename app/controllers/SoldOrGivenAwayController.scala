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
import forms.SoldOrGivenAwayForm._
import models.SoldOrGivenAwayModel
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.soldOrGivenAway

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SoldOrGivenAwayController @Inject()(val http: DefaultHttpClient,
                                          sessionCacheService: SessionCacheService,
                                          mcc: MessagesControllerComponents,
                                          soldOrGivenAwayView: soldOrGivenAway)
                                         (implicit ec: ExecutionContext)
                                            extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val soldOrGivenAway: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheService.fetchAndGetFormData[SoldOrGivenAwayModel](KeystoreKeys.soldOrGivenAway).map {
      case Some(data) => Ok(soldOrGivenAwayView(soldOrGivenAwayForm.fill(data)))
      case None => Ok(soldOrGivenAwayView(soldOrGivenAwayForm))
    }
  }

  val submitSoldOrGivenAway: Action[AnyContent] = ValidateSession.async { implicit request =>

    soldOrGivenAwayForm.bindFromRequest().fold(
      errors => Future.successful(BadRequest(soldOrGivenAwayView(errors))),
      success => {
        sessionCacheService.saveFormData[SoldOrGivenAwayModel](KeystoreKeys.soldOrGivenAway, success).map(_ =>
        success match {
          case SoldOrGivenAwayModel(true) => Redirect(routes.SoldForLessController.soldForLess)
          case SoldOrGivenAwayModel(false) => Redirect(routes.WhoDidYouGiveItToController.whoDidYouGiveItTo)
        })
      }
    )
  }
}
