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
import views.html.calculation
import forms.nonresident.SoldOrGivenAwayForm._
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import models.nonresident.SoldOrGivenAwayModel
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

object SoldOrGivenAwayController extends SoldOrGivenAwayController {
  val calcConnector = CalculatorConnector
}

trait SoldOrGivenAwayController extends FrontendController with ValidActiveSession  {

  val calcConnector: CalculatorConnector
  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.DisposalDateController.disposalDate().url

  val soldOrGivenAway = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[SoldOrGivenAwayModel](KeystoreKeys.soldOrGivenAway).map {
      case Some(data) => Ok(calculation.nonresident.soldOrGivenAway(soldOrGivenAwayForm.fill(data)))
      case None => Ok(calculation.nonresident.soldOrGivenAway(soldOrGivenAwayForm))
    }
  }

  val submitSoldOrGivenAway = ValidateSession.async { implicit request =>

    soldOrGivenAwayForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(calculation.nonresident.soldOrGivenAway(errors))),
      success => {
        calcConnector.saveFormData[SoldOrGivenAwayModel](KeystoreKeys.soldOrGivenAway, success)
        success match {
          case SoldOrGivenAwayModel(true) => Future.successful(Redirect(routes.SoldForLessController.soldForLess()))
          case SoldOrGivenAwayModel(false) => Future.successful(Redirect(routes.MarketValueWhenSoldOrGaveAwayController.marketValueWhenGaveAway()))
        }
      }
    )
  }
}
