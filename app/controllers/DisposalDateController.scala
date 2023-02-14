/*
 * Copyright 2023 HM Revenue & Customs
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

import java.util.UUID

import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.TaxDates
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.DisposalDateForm._
import javax.inject.Inject
import models.DateModel
import play.api.Logging
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.disposalDate

import scala.concurrent.{ExecutionContext, Future}


class DisposalDateController @Inject()(http: DefaultHttpClient,calcConnector: CalculatorConnector,
                                       mcc: MessagesControllerComponents,
                                       disposalDateView: disposalDate)
                                      (implicit ec: ExecutionContext)
                                        extends FrontendController(mcc) with ValidActiveSession with I18nSupport with Logging{

  val disposalDate = Action.async { implicit request =>
    implicit val lang = mcc.messagesApi.preferred(request).lang
    if (request.session.get(SessionKeys.sessionId).isEmpty) {
      val sessionId = UUID.randomUUID.toString
      Future.successful(Ok(disposalDateView(disposalDateForm)).withSession(request.session +
        (SessionKeys.sessionId -> s"session-$sessionId")))
    }
    else {
      calcConnector.fetchAndGetFormData[DateModel](KeystoreKeys.disposalDate).map {
        case Some(data) => Ok(disposalDateView(disposalDateForm.fill(data)))
        case None => Ok(disposalDateView(disposalDateForm))
      }
    }
  }

  val submitDisposalDate = ValidateSession.async { implicit request =>
    implicit val lang = mcc.messagesApi.preferred(request).lang

    def errorAction(form: Form[DateModel]) = Future.successful(BadRequest(disposalDateView(form)))

    def successAction(model: DateModel) = {
      logger.info("Saving disposalDate as : " + model)
      for {
        _ <- calcConnector.saveFormData(KeystoreKeys.disposalDate, model)
        taxYear <- calcConnector.getTaxYear(s"${model.year}-${model.month}-${model.day}")
      } yield {
        if (!TaxDates.dateAfterStart(model.day, model.month, model.year)) {
          Redirect(routes.NoCapitalGainsTaxController.noCapitalGainsTax)
        } else if (!taxYear.get.isValidYear) {
          Redirect(routes.OutsideTaxYearController.outsideTaxYear)
        } else {
          Redirect(routes.SoldOrGivenAwayController.soldOrGivenAway)
        }
      }
    }
    disposalDateForm.bindFromRequest.fold(errorAction, successAction)
  }
}
