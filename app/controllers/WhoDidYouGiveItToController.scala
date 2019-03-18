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

import common.KeystoreKeys.{NonResidentKeys => keystoreKeys}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.WhoDidYouGiveItToForm._
import javax.inject.Inject
import models.WhoDidYouGiveItToModel
import play.api.Environment
import play.api.Play.current
import play.api.i18n.I18nSupport
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.{calculation => views}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class WhoDidYouGiveItToController @Inject()(http: DefaultHttpClient,
                                            calcConnector: CalculatorConnector,
                                            mcc: MessagesControllerComponents,
                                            implicit val appConfig: ApplicationConfig) extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val whoDidYouGiveItTo = ValidateSession.async { implicit request =>

    calcConnector.fetchAndGetFormData[WhoDidYouGiveItToModel](keystoreKeys.whoDidYouGiveItTo).map {
      case Some(data) => Ok(views.whoDidYouGiveItTo(whoDidYouGiveItToForm.fill(data)))
      case _ => Ok(views.whoDidYouGiveItTo(whoDidYouGiveItToForm))
    }
  }

  val submitWhoDidYouGiveItTo: Action[AnyContent] = ValidateSession.async { implicit request =>
    whoDidYouGiveItToForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.whoDidYouGiveItTo(errors))),
      success => {
        calcConnector.saveFormData[WhoDidYouGiveItToModel](keystoreKeys.whoDidYouGiveItTo, success).map(_ =>
        success match {
          case WhoDidYouGiveItToModel("Spouse") => Redirect(routes.WhoDidYouGiveItToController.noTaxToPay())
          case WhoDidYouGiveItToModel("Charity") => Redirect(routes.WhoDidYouGiveItToController.noTaxToPay())
          case WhoDidYouGiveItToModel("Other") => Redirect(routes.MarketValueWhenSoldOrGaveAwayController.marketValueWhenGaveAway())
        })
      })
  }

  val noTaxToPay = ValidateSession.async { implicit request =>

    def isGivenToCharity: Future[Boolean] = {
      calcConnector.fetchAndGetFormData[WhoDidYouGiveItToModel](keystoreKeys.whoDidYouGiveItTo).map {
        case Some(WhoDidYouGiveItToModel("Charity")) => true
        case _ => false
      }
    }

    def result(input: Boolean): Future[Result] = {
      Future.successful(Ok(views.noTaxToPay(input)))
    }

    (for {
      givenToCharity <- isGivenToCharity
      result <- result(givenToCharity)
    } yield result).recoverToStart
  }

}
