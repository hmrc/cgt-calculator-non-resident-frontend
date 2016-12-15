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
import forms.DisabledTrusteeForm._
import views.html.calculation
import models.DisabledTrusteeModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object DisabledTrusteeController extends DisabledTrusteeController {
  val calcConnector = CalculatorConnector
}

trait DisabledTrusteeController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  override val sessionTimeoutUrl = controllers.routes.SummaryController.restart().url
  override val homeLink = controllers.routes.DisposalDateController.disposalDate().url

  val disabledTrustee = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[DisabledTrusteeModel](KeystoreKeys.disabledTrustee).map {
      case Some(data) => Ok(calculation.disabledTrustee(disabledTrusteeForm.fill(data)))
      case None => Ok(calculation.disabledTrustee(disabledTrusteeForm))
    }
  }

  val submitDisabledTrustee = ValidateSession.async { implicit request =>
    disabledTrusteeForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(calculation.disabledTrustee(errors))),
      success => {
        calcConnector.saveFormData(KeystoreKeys.disabledTrustee, success)
        Future.successful(Redirect(routes.OtherPropertiesController.otherProperties()))
      }
    )
  }
}
