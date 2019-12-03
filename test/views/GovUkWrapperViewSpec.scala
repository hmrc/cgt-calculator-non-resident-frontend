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

package views

import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import org.scalatest.mockito.MockitoSugar
import play.api.Application
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.twirl.api.Html
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.govuk_wrapper

class GovUkWrapperViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  implicit lazy val fakeApp: Application = fakeApplication

  "GovUK Wrapper" should {
    "produce the same output when render and f are called" in {
      govuk_wrapper.f(mockConfig, "Title", None, None, None, Html(""), None, Html(""), Html(""), None, Html(""))(FakeRequest("GET", ""), mockMessage) shouldBe govuk_wrapper.render(mockConfig, "Title", None, None, None, Html(""), None, Html(""), Html(""), None, Html(""), FakeRequest("GET", ""), mockMessage)
    }
  }

}
