/*
 * Copyright 2016 HM Revenue & Customs
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

package controllers.nonresident

import common.KeystoreKeys
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import models.nonresident.SoldForLessModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.calculation.{nonresident => views}
import forms.nonresident.SoldForLessForm._
import play.api.data.Form
import common.KeystoreKeys.NonResidentKeys

import scala.concurrent.Future

object SoldForLessController extends SoldForLessController {
  val calcConnector = CalculatorConnector
}

trait SoldForLessController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.DisposalDateController.disposalDate().url

  val soldForLess = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[SoldForLessModel](KeystoreKeys.NonResidentKeys.soldForLess).map {
      case Some(data) => Ok(views.soldForLess(soldForLessForm.fill(data)))
      case None => Ok(views.soldForLess(soldForLessForm))
    }
  }

  val submitSoldForLess = ValidateSession.async { implicit request =>

    def errorAction(errors: Form[SoldForLessModel]) = Future.successful(BadRequest(views.soldForLess(errors)))

    def routeRequest(model: SoldForLessModel) = {
      //This has been written as such to make update to the routing in the second story much easier
      //It should require only a change to the tests and change to the else case
      if (model.soldForLess) Future.successful(Redirect(routes.MarketValueWhenSoldOrGaveAwayController.marketValueWhenSold()))
      else Future.successful(Redirect(routes.DisposalValueController.disposalValue()))
    }

    def successAction(model: SoldForLessModel) = {
      for {
        save <- calcConnector.saveFormData(NonResidentKeys.soldForLess, model)
        route <- routeRequest(model)
      } yield route
    }

    soldForLessForm.bindFromRequest().fold(errorAction, successAction)
  }
}
