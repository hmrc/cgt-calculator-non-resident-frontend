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

import config.ApplicationConfig
import controllers.predicates.ValidActiveSession
import javax.inject.Inject
import play.api.Environment
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.i18n.I18nSupport
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.concurrent.Future

class WhatNextController @Inject()(http: DefaultHttpClient,
                                   implicit val appConfig : ApplicationConfig,
                                   mcc: MessagesControllerComponents) extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val whatNext = ValidateSession.async { implicit request =>
    Future.successful(Ok(views.html.whatNext.whatNext()))
  }

}