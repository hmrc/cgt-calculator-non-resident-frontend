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
import constructors.DefaultCalculationElectionConstructor
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.BoughtForLessForm._
import models.BoughtForLessModel
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.boughtForLess

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BoughtForLessController @Inject()(val http: DefaultHttpClient,
                                        sessionCacheService: SessionCacheService,
                                        val calcElectionConstructor: DefaultCalculationElectionConstructor,
                                        mcc: MessagesControllerComponents,
                                        boughtForLessView: boughtForLess)(implicit ec: ExecutionContext)
                                          extends FrontendController(mcc) with ValidActiveSession with I18nSupport {


  val boughtForLess: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheService.fetchAndGetFormData[BoughtForLessModel](KeystoreKeys.boughtForLess).map {
      case Some(data) => Ok(boughtForLessView(boughtForLessForm.fill(data)))
      case None => Ok(boughtForLessView(boughtForLessForm))
    }
  }

  val submitBoughtForLess: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(errors: Form[BoughtForLessModel]) = Future.successful(BadRequest(boughtForLessView(errors)))

    def routeRequest(model: BoughtForLessModel) = {
      if (model.boughtForLess) Future.successful(Redirect(routes.WorthWhenBoughtForLessController.worthWhenBoughtForLess))
      else Future.successful(Redirect(routes.AcquisitionValueController.acquisitionValue))
    }

    def successAction(model: BoughtForLessModel) = {
      (for {
        _ <- sessionCacheService.saveFormData(KeystoreKeys.boughtForLess, model)
        route <- routeRequest(model)
      } yield route).recoverToStart
    }


    boughtForLessForm.bindFromRequest().fold(errorAction, successAction)
  }
}
