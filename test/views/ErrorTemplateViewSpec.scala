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

package views

import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import views.html.error_template

class ErrorTemplateViewSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {
  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  implicit lazy val fakeApp: Application = fakeApplication
  lazy val errorTemplateView: error_template = fakeApplication.injector.instanceOf[error_template]

  "Error Template" should {
    "produce the same output when render and f are called" in {
      errorTemplateView.f("title", "heading", "message")(FakeRequest("GET", ""), mockMessage) shouldBe
        errorTemplateView.render("title", "heading", "message", FakeRequest("GET", ""), mockMessage)
    }
  }
}
