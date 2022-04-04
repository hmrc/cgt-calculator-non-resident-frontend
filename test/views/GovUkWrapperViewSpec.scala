/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.html.govuk_wrapper

class GovUkWrapperViewSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  implicit lazy val fakeApp: Application = fakeApplication
  lazy val govUkWrapperView = fakeApplication.injector.instanceOf[govuk_wrapper]

  "GovUK Wrapper" should {
    "produce the same output when render and f are called" in {
      govUkWrapperView.f("Title", None, None, None, Html(""), None, Html(""), Html(""), None, Html(""))(FakeRequest("GET", ""), mockMessage) shouldBe
        govUkWrapperView.render("Title", None, None, None, Html(""), None, Html(""), Html(""), None, Html(""), FakeRequest("GET", ""), mockMessage)
    }
  }

}
