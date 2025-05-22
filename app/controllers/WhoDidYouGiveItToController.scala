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

import common.KeystoreKeys.{NonResidentKeys => keystoreKeys}
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.WhoDidYouGiveItToForm._
import models.WhoDidYouGiveItToModel
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.{noTaxToPay, whoDidYouGiveItTo}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class WhoDidYouGiveItToController @Inject()(val http: DefaultHttpClient,
                                            sessionCacheService: SessionCacheService,
                                            mcc: MessagesControllerComponents,
                                            noTaxToPayView: noTaxToPay,
                                            whoDidYouGiveItToView: whoDidYouGiveItTo)
                                           (implicit ec: ExecutionContext) extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val whoDidYouGiveItTo: Action[AnyContent] = ValidateSession.async { implicit request =>

    sessionCacheService.fetchAndGetFormData[WhoDidYouGiveItToModel](keystoreKeys.whoDidYouGiveItTo).map {
      case Some(data) => Ok(whoDidYouGiveItToView(whoDidYouGiveItToForm.fill(data)))
      case _ => Ok(whoDidYouGiveItToView(whoDidYouGiveItToForm))
    }
  }

  val submitWhoDidYouGiveItTo: Action[AnyContent] = ValidateSession.async { implicit request =>
    whoDidYouGiveItToForm.bindFromRequest().fold(
      errors => Future.successful(BadRequest(whoDidYouGiveItToView(errors))),
      success => {
        sessionCacheService.saveFormData[WhoDidYouGiveItToModel](keystoreKeys.whoDidYouGiveItTo, success).map(_ =>
        success match {
          case WhoDidYouGiveItToModel("Spouse") => Redirect(routes.WhoDidYouGiveItToController.noTaxToPay)
          case WhoDidYouGiveItToModel("Charity") => Redirect(routes.WhoDidYouGiveItToController.noTaxToPay)
          case _ => Redirect(routes.MarketValueWhenSoldOrGaveAwayController.marketValueWhenGaveAway)
        })
      })
  }

  val noTaxToPay: Action[AnyContent] = ValidateSession.async { implicit request =>

    def isGivenToCharity: Future[Boolean] = {
      sessionCacheService.fetchAndGetFormData[WhoDidYouGiveItToModel](keystoreKeys.whoDidYouGiveItTo).map {
        case Some(WhoDidYouGiveItToModel("Charity")) => true
        case _ => false
      }
    }

    def result(input: Boolean): Future[Result] = {
      Future.successful(Ok(noTaxToPayView(input)))
    }

    (for {
      givenToCharity <- isGivenToCharity
      result <- result(givenToCharity)
    } yield result).recoverToStart
  }

}
