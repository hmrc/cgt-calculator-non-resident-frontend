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

package controllers

import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.MarketValueGaveAwayForm._
import forms.MarketValueWhenSoldForm._
import views.html.calculation
import models.DisposalValueModel
import play.api.data.Form
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object MarketValueWhenSoldOrGaveAwayController extends MarketValueWhenSoldOrGaveAwayController {
  val calcConnector = CalculatorConnector
}

trait MarketValueWhenSoldOrGaveAwayController extends FrontendController with ValidActiveSession {
  val calcConnector: CalculatorConnector
  override val sessionTimeoutUrl = controllers.routes.SummaryController.restart().url
  override val homeLink = controllers.routes.DisposalDateController.disposalDate().url

  val marketValueWhenSold = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[DisposalValueModel](KeystoreKeys.disposalMarketValue).map {
      case Some(data) => Ok(calculation.marketValueSold(marketValueWhenSoldForm.fill(data)))
      case None => Ok(calculation.marketValueSold(marketValueWhenSoldForm))
    }
  }

  val marketValueWhenGaveAway = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[DisposalValueModel](KeystoreKeys.disposalMarketValue).map {
      case Some(data) => Ok(calculation.marketValueGaveAway(marketValueWhenGaveAwayForm.fill(data)))
      case None => Ok(calculation.marketValueGaveAway(marketValueWhenGaveAwayForm))
    }
  }

  val submitMarketValueWhenSold = ValidateSession.async { implicit request =>

    def errorAction(form: Form[DisposalValueModel]) = Future.successful(BadRequest(calculation.marketValueSold(form)))

    def successAction(model: DisposalValueModel) = {
      calcConnector.saveFormData(KeystoreKeys.disposalMarketValue, model)
      Future.successful(Redirect(routes.DisposalCostsController.disposalCosts()))
    }

    marketValueWhenSoldForm.bindFromRequest.fold(errorAction, successAction)
  }

  val submitMarketValueWhenGaveAway = ValidateSession.async { implicit request =>

    def errorAction(form: Form[DisposalValueModel]) = Future.successful(BadRequest(calculation.marketValueGaveAway(form)))

    def successAction(model: DisposalValueModel) = {
      calcConnector.saveFormData(KeystoreKeys.disposalMarketValue, model)
      Future.successful(Redirect(routes.DisposalCostsController.disposalCosts()))
    }

    marketValueWhenGaveAwayForm.bindFromRequest.fold(errorAction, successAction)
  }
}