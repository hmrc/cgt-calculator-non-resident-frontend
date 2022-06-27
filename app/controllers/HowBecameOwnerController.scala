/*
 * Copyright 2022 HM Revenue & Customs
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
import javax.inject.Inject
import models.HowBecameOwnerModel
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.howBecameOwner

import scala.concurrent.{ExecutionContext, Future}

class HowBecameOwnerController @Inject()(http: DefaultHttpClient,calcConnector: CalculatorConnector,
                                         mcc: MessagesControllerComponents,
                                         howBecameOwnerView: howBecameOwner)(implicit ec: ExecutionContext)
                                          extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val howBecameOwner: Action[AnyContent] = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[HowBecameOwnerModel](KeystoreKeys.howBecameOwner).map {
      case Some(data) => Ok(howBecameOwnerView(howBecameOwnerForm.fill(data)))
      case None => Ok(howBecameOwnerView(howBecameOwnerForm))
    }
  }

  val submitHowBecameOwner: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(form: Form[HowBecameOwnerModel]) = Future.successful(BadRequest(howBecameOwnerView(form)))

    def successAction(model: HowBecameOwnerModel) = {
      for {
        save <- calcConnector.saveFormData[HowBecameOwnerModel](KeystoreKeys.howBecameOwner, model)
        route <- routeRequest(model)
      } yield route
    }

    def routeRequest(data: HowBecameOwnerModel): Future[Result] = {
      data.gainedBy match {
        case "Gifted" => Future.successful(Redirect(routes.WorthWhenGiftedToController.worthWhenGiftedTo))
        case "Inherited" => Future.successful(Redirect(routes.WorthWhenInheritedController.worthWhenInherited))
        case _ => Future.successful(Redirect(routes.BoughtForLessController.boughtForLess))
      }
    }

    howBecameOwnerForm.bindFromRequest.fold(errorAction, successAction)
  }
}
