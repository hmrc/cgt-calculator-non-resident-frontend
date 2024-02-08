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
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import models.DateModel
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.outsideTaxYear

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class OutsideTaxYearController @Inject()(http: DefaultHttpClient,
                                         calcConnector: CalculatorConnector,
                                         sessionCacheService: SessionCacheService,
                                         mcc: MessagesControllerComponents,
                                         outsideTaxYearView: outsideTaxYear)(implicit ec: ExecutionContext)
  extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val outsideTaxYear: Action[AnyContent] = ValidateSession.async { implicit request =>
    (for {
      disposalDate <- sessionCacheService.fetchAndGetFormData[DateModel](keystoreKeys.disposalDate)
      taxYear <- calcConnector.getTaxYear(s"${disposalDate.get.year}-${disposalDate.get.month}-${disposalDate.get.day}")
    } yield {
      Ok(outsideTaxYearView(
        taxYear = taxYear.get))
    }).recoverToStart
  }
}