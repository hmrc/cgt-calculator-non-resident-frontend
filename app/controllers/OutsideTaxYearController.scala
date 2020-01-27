/*
 * Copyright 2020 HM Revenue & Customs
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
import views.html.{calculation => views}
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import models.DateModel
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import controllers.utils.RecoverableFuture
import javax.inject.Inject
import play.api.Environment
import play.api.i18n.I18nSupport
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import scala.concurrent.ExecutionContext.Implicits.global

class OutsideTaxYearController @Inject()(http: DefaultHttpClient,calcConnector: CalculatorConnector,
                                         mcc: MessagesControllerComponents)(implicit val applicationConfig: ApplicationConfig)
  extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val outsideTaxYear: Action[AnyContent] = ValidateSession.async { implicit request =>
    (for {
      disposalDate <- calcConnector.fetchAndGetFormData[DateModel](keystoreKeys.disposalDate)
      taxYear <- calcConnector.getTaxYear(s"${disposalDate.get.year}-${disposalDate.get.month}-${disposalDate.get.day}")
    } yield {
      Ok(views.outsideTaxYear(
        taxYear = taxYear.get))
    }).recoverToStart
  }
}