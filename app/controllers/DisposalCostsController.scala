/*
 * Copyright 2018 HM Revenue & Customs
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

import common.DefaultRoutes._
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.DisposalCostsForm._
import views.html.calculation
import models.{DisposalCostsModel, SoldForLessModel, SoldOrGivenAwayModel}
import play.api.data.Form
import play.api.mvc.Result
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import controllers.utils.RecoverableFuture

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

object DisposalCostsController extends DisposalCostsController {
  val calcConnector = CalculatorConnector
}

trait DisposalCostsController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector

  private def backUrl(soldOrGivenAwayModel: Option[SoldOrGivenAwayModel], soldForLessModel: Option[SoldForLessModel]): Future[String] =
    (soldOrGivenAwayModel, soldForLessModel) match {
    case (Some(SoldOrGivenAwayModel(soldIt)), _) if !soldIt => Future.successful(routes.MarketValueWhenSoldOrGaveAwayController.marketValueWhenGaveAway().url)
    case (Some(SoldOrGivenAwayModel(soldIt)), Some(SoldForLessModel(soldForLess))) if soldIt && soldForLess =>
      Future.successful(routes.MarketValueWhenSoldOrGaveAwayController.marketValueWhenSold().url)
    case (Some(SoldOrGivenAwayModel(soldIt)), Some(SoldForLessModel(soldForLess))) if soldIt && !soldForLess =>
      Future.successful(routes.DisposalValueController.disposalValue().url)
    case (_, _) => Future.successful(missingDataRoute)
  }

  private def fetchSoldOrGivenAway(implicit headerCarrier: HeaderCarrier): Future[Option[SoldOrGivenAwayModel]] = {
    calcConnector.fetchAndGetFormData[SoldOrGivenAwayModel](KeystoreKeys.soldOrGivenAway)
  }

  private def fetchSoldForLess(implicit headerCarrier: HeaderCarrier): Future[Option[SoldForLessModel]] = {
    calcConnector.fetchAndGetFormData[SoldForLessModel](KeystoreKeys.soldForLess)
  }

  val disposalCosts = ValidateSession.async { implicit request =>

    def routeRequest(backLink: String): Future[Result] = {
      calcConnector.fetchAndGetFormData[DisposalCostsModel](KeystoreKeys.disposalCosts).map {
        case Some(data) => Ok(calculation.disposalCosts(disposalCostsForm.fill(data), backLink))
        case None => Ok(calculation.disposalCosts(disposalCostsForm, backLink))
      }
    }

    (for {
      soldOrGivenAway <- fetchSoldOrGivenAway
      soldForLess <- fetchSoldForLess
      backLink <- backUrl(soldOrGivenAway, soldForLess)
      route <- routeRequest(backLink)
    } yield route).recoverToStart
  }

  val submitDisposalCosts = ValidateSession.async { implicit request =>

    def errorAction(errors: Form[DisposalCostsModel], backLink: String) = {
      Future.successful(BadRequest(calculation.disposalCosts(errors, backLink)))
    }

    def successAction(model: DisposalCostsModel) = {
      calcConnector.saveFormData(KeystoreKeys.disposalCosts, model).map(_ =>
        Redirect(routes.AcquisitionDateController.acquisitionDate()))
    }

    def routeRequest(backLink: String) = {
      disposalCostsForm.bindFromRequest.fold(
        errors => errorAction(errors, backLink),
        success => successAction(success)
      )
    }

    (for {
      soldOrGivenAway <- fetchSoldOrGivenAway
      soldForLess <- fetchSoldForLess
      backLink <- backUrl(soldOrGivenAway, soldForLess)
      route <- routeRequest(backLink)
    } yield route).recoverToStart
  }
}
