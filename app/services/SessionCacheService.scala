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

package services

import play.api.libs.json.Format
import play.api.mvc.Request
import repositories.SessionRepository
import uk.gov.hmrc.mongo.cache.DataKey
import uk.gov.hmrc.play.http.logging.Mdc.preservingMdc

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SessionCacheService @Inject()(
  sessionRepository: SessionRepository
)(implicit ec: ExecutionContext) {
  def saveFormData[T](key: String, data: T)(implicit request: Request[_], formats: Format[T]): Future[(String, String)] = {
    preservingMdc {
      sessionRepository.putSession(DataKey(key), data)
    }
  }

  def unsetData[T](key: String)(implicit request: Request[_]): Future[Unit] = {
      preservingMdc {
        sessionRepository.deleteFromSession(DataKey(key))
      }
  }

  def fetchAndGetFormData[T](key: String)(implicit request: Request[_], formats: Format[T]): Future[Option[T]] = {
    preservingMdc {
      sessionRepository.getFromSession[T](DataKey(key))
    }
  }

  def clearSession(implicit request: Request[_]): Future[Unit] = {
      sessionRepository.clear
  }
}
