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

import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.TaxDates
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.RebasedValueForm._
import models.{DateModel, RebasedValueModel}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.rebasedValue

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class RebasedValueController @Inject()(val http: DefaultHttpClient,
                                       sessionCacheService: SessionCacheService,
                                       mcc: MessagesControllerComponents,
                                       rebasedValueView: rebasedValue
                                      )(implicit ec: ExecutionContext)
                                        extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  def backLink(acquisitionDate: Option[DateModel]): String = {

    val localDate: Option[LocalDate] = acquisitionDate.map{x => x.get}

    localDate match {
      case Some(x) =>
        if (TaxDates.dateBeforeLegislationStart(x)) controllers.routes.CostsAtLegislationStartController.costsAtLegislationStart.url
        else controllers.routes.AcquisitionCostsController.acquisitionCosts.url
      case _ => controllers.routes.AcquisitionCostsController.acquisitionCosts.url
    }
  }

  val rebasedValue: Action[AnyContent] = ValidateSession.async { implicit request =>
    (for {
      rebasedValueModel <- sessionCacheService.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue)
      acquisitionDate <- sessionCacheService.fetchAndGetFormData[DateModel](KeystoreKeys.acquisitionDate)
    } yield rebasedValueModel match {
      case Some(data) => Ok(rebasedValueView(rebasedValueForm.fill(data), backLink(acquisitionDate)))
      case None => Ok(rebasedValueView(rebasedValueForm, backLink(acquisitionDate)))
    }).recoverToStart
  }

  val submitRebasedValue: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(errors: Form[RebasedValueModel]) = {
      sessionCacheService.fetchAndGetFormData[DateModel](KeystoreKeys.acquisitionDate).map{
        x => BadRequest(rebasedValueView(errors, backLink(x)))
      }
    }

    def successAction(model: RebasedValueModel) = {
      sessionCacheService.saveFormData(KeystoreKeys.rebasedValue, model).map(_ =>
        Redirect(routes.RebasedCostsController.rebasedCosts))
    }

    rebasedValueForm.bindFromRequest().fold(errorAction, successAction)
  }
}
