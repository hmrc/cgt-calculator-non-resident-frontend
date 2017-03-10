/*
 * Copyright 2017 HM Revenue & Customs
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
import forms.BoughtForLessForm._
import models.BoughtForLessModel
import play.api.data.Form
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.calculation
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object BoughtForLessController extends BoughtForLessController {
  val calcConnector = CalculatorConnector
}

trait BoughtForLessController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector

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
      for {
        save <- calcConnector.saveFormData(KeystoreKeys.boughtForLess, model)
        route <- routeRequest(model)
      } yield route
    }


    boughtForLessForm.bindFromRequest().fold(errorAction, successAction)
  }
}
