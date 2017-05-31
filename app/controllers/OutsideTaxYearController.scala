/*
 * Copyright 2017 HM Revenue & Customs
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
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import models.DisposalDateModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.calculation
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.{Action, AnyContent}

object OutsideTaxYearController extends OutsideTaxYearController {
  val calcConnector = CalculatorConnector
}


trait OutsideTaxYearController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector

  val outsideTaxYear: Action[AnyContent] = ValidateSession.async { implicit request =>
    for {
      model <- calcConnector.fetchAndGetFormData[DisposalDateModel](KeystoreKeys.disposalDate)
      taxYear <- calcConnector.getTaxYear(s"${model.get.year}-${model.get.month}-${model.get.day}")
    } yield {
      if (!TaxDates.dateAfterStart(model.get.day, model.get.month, model.get.year)) {
        Redirect(routes.NoCapitalGainsTaxController.noCapitalGainsTax())
      } else if (!taxYear.get.isValidYear) {
        Redirect(routes.OutsideTaxYearController.outsideTaxYear())
      } else {
        Redirect(routes.SoldOrGivenAwayController.soldOrGivenAway())
      }
    }
  }
}