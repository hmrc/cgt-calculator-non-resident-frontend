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

import constructors.SessionExpiredException
import play.api.Logging
import play.api.mvc.Results.*
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.play.bootstrap.frontend.http.ApplicationException

import scala.concurrent.duration.Duration
import scala.concurrent.{CanAwait, ExecutionContext, Future}
import scala.util.Try

package object utils {
  implicit class RecoverableFuture(future: Future[Result]) extends Future[Result] with Logging {
    override def onComplete[U](f: Try[Result] => U)(implicit executor: ExecutionContext): Unit = future.onComplete(f)
    override def isCompleted: Boolean = future.isCompleted
    override def value: Option[Try[Result]] = future.value
    override def ready(atMost: Duration)(implicit permit: CanAwait): RecoverableFuture.this.type = ready(atMost)
    override def result(atMost: Duration)(implicit permit: CanAwait): Result = future.result(atMost)

    def recoverToStart(implicit request: Request[?], ec: ExecutionContext): Future[Result] =
      future.recover {
        case e: (NoSuchElementException | SessionExpiredException)  =>
          logger.warn(s"${request.uri} resulted in None.get, user redirected to start")
          throw ApplicationException(
            Redirect(controllers.utils.routes.TimeoutController.timeout()),
            e.getMessage
          )
      }

    override def transform[S](f: Try[Result] => Try[S])(implicit executor: ExecutionContext): Future[S] = future.transform(f)

    override def transformWith[S](f: Try[Result] => Future[S])(implicit executor: ExecutionContext): Future[S] = future.transformWith(f)
  }
}