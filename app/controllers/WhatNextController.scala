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

import constructors.AnswersConstructor
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import models.DateModel
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.whatNext.whatNext

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class WhatNextController @Inject()(answersConstructor: AnswersConstructor,
                                   mcc: MessagesControllerComponents,
                                   whatNextView: whatNext)
                                  (implicit ec: ExecutionContext) extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val referenceDate: DateModel = DateModel(5, 4, 2020)
  val conveyanceDate: DateModel = DateModel(27, 10, 2021)

  def whatNext: Action[AnyContent] = ValidateSession.async { implicit request =>
    answersConstructor.getNRTotalGainAnswers.map { answerModel =>
      val isDateAfter: Boolean = answerModel.disposalDateModel.isDateAfter(referenceDate)
      val reportWindow = if (conveyanceDate.isDateAfter(answerModel.disposalDateModel)) 30 else 60
      Ok(whatNextView(isDateAfter, reportWindow))
    }.recoverToStart
  }

}
