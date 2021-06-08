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
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.SoldOrGivenAwayForm._
import javax.inject.Inject
import models.SoldOrGivenAwayModel
import play.api.i18n.I18nSupport
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.soldOrGivenAway

import scala.concurrent.{ExecutionContext, Future}

class SoldOrGivenAwayController @Inject()(http: DefaultHttpClient,calcConnector: CalculatorConnector,
                                          mcc: MessagesControllerComponents,
                                          soldOrGivenAwayView: soldOrGivenAway)
                                         (implicit ec: ExecutionContext)
                                            extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val soldOrGivenAway = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[SoldOrGivenAwayModel](KeystoreKeys.soldOrGivenAway).map {
      case Some(data) => Ok(soldOrGivenAwayView(soldOrGivenAwayForm.fill(data)))
      case None => Ok(soldOrGivenAwayView(soldOrGivenAwayForm))
    }
  }

  val submitSoldOrGivenAway = ValidateSession.async { implicit request =>

    soldOrGivenAwayForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(soldOrGivenAwayView(errors))),
      success => {
        calcConnector.saveFormData[SoldOrGivenAwayModel](KeystoreKeys.soldOrGivenAway, success).map(_ =>
        success match {
          case SoldOrGivenAwayModel(true) => Redirect(routes.SoldForLessController.soldForLess())
          case SoldOrGivenAwayModel(false) => Redirect(routes.WhoDidYouGiveItToController.whoDidYouGiveItTo())
        })
      }
    )
  }
}
