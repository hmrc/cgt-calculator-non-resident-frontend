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

import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import models.DateModel
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc.MessagesControllerComponents
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.noCapitalGainsTax

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class NoCapitalGainsTaxController @Inject()(http: DefaultHttpClient,
                                            sessionCacheService: SessionCacheService,
                                            mcc: MessagesControllerComponents,
                                            noCapitalGainsTaxView: noCapitalGainsTax)
                                           (implicit ec: ExecutionContext)
  extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val noCapitalGainsTax = ValidateSession.async { implicit request =>
    implicit val lang: Lang = mcc.messagesApi.preferred(request).lang
    sessionCacheService.fetchAndGetFormData[DateModel](KeystoreKeys.disposalDate).map {
      result => Ok(noCapitalGainsTaxView(result.get))
    }.recoverToStart
  }
}
