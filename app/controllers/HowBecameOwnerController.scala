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
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.HowBecameOwnerForm._
import models.HowBecameOwnerModel
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.{calculation => views}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object HowBecameOwnerController extends HowBecameOwnerController {
  val calcConnector = CalculatorConnector
}

trait HowBecameOwnerController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector

  val howBecameOwner: Action[AnyContent] = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[HowBecameOwnerModel](KeystoreKeys.howBecameOwner).map {
      case Some(data) => Ok(views.howBecameOwner(howBecameOwnerForm.fill(data)))
      case None => Ok(views.howBecameOwner(howBecameOwnerForm))
    }
  }

  val submitHowBecameOwner: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(form: Form[HowBecameOwnerModel]) = Future.successful(BadRequest(views.howBecameOwner(form)))

    def successAction(model: HowBecameOwnerModel) = {
      for {
        save <- calcConnector.saveFormData[HowBecameOwnerModel](KeystoreKeys.howBecameOwner, model)
        route <- routeRequest(model)
      } yield route
    }

    def routeRequest(data: HowBecameOwnerModel): Future[Result] = {
      data.gainedBy match {
        case "Gifted" => Future.successful(Redirect(routes.WorthWhenGiftedToController.worthWhenGiftedTo()))
        case "Inherited" => Future.successful(Redirect(routes.WorthWhenInheritedController.worthWhenInherited()))
        case _ => Future.successful(Redirect(routes.BoughtForLessController.boughtForLess()))
      }
    }

    howBecameOwnerForm.bindFromRequest.fold(errorAction, successAction)
  }
}
