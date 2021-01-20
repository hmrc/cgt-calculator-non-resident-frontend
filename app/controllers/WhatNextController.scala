/*
 * Copyright 2021 HM Revenue & Customs
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

import config.ApplicationConfig
import constructors.AnswersConstructor
import controllers.predicates.ValidActiveSession
import javax.inject.Inject
import models.DateModel
import play.api.Application
import play.api.i18n.I18nSupport
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class WhatNextController @Inject()(http: DefaultHttpClient,
                                   answersConstructor: AnswersConstructor,
                                   implicit val appConfig: ApplicationConfig,
                                   implicit val application: Application,
                                   mcc: MessagesControllerComponents) extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val referenceDate: DateModel = DateModel(5,4,2020)

  val whatNext = ValidateSession.async { implicit request => {
    answersConstructor.getNRTotalGainAnswers.flatMap(answerModel => {
      val isDateAfter: Boolean = answerModel.disposalDateModel.isDateAfter(referenceDate)
      Future.successful(Ok(views.html.whatNext.whatNext(isDateAfter)))
    })
  }}

}