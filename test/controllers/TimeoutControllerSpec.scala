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

import akka.stream.Materializer
import akka.util.Timeout
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import controllers.utils.TimeoutController
import org.jsoup.Jsoup
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.{Messages, MessagesProvider}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class TimeoutControllerSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {
  implicit val mockMessagesProvider = mock[MessagesProvider]
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesComponent = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  lazy val materializer = mock[Materializer]
  lazy val timeout = mock[Timeout]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)


    class fakeRequestTo(url : String, controllerAction : Action[AnyContent]) {
    val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/" + url)
    val result = controllerAction(fakeRequest)
    val jsoupDoc = Jsoup.parse(bodyOf(result)(materializer))
  }

  val controller = new TimeoutController(mockMessagesComponent)(mockConfig)

  "TimeoutController.timeout" should {

    "when called with no session" should {

      object timeoutTestDataItem extends fakeRequestTo("", controller.timeout())

      "return a 200" in {
        status(timeoutTestDataItem.result) shouldBe 200
      }

      "have the title" in {
        timeoutTestDataItem.jsoupDoc.getElementsByTag("title").text shouldEqual Messages("session.timeout.message")
      }

      "contain the heading 'Your session has timed out." in {
        timeoutTestDataItem.jsoupDoc.select("h1").text shouldEqual Messages("session.timeout.message")
      }

      "have a restart link to href of 'test'" in {
        timeoutTestDataItem.jsoupDoc.getElementById("startAgain").attr("href") shouldEqual common.DefaultRoutes.restartUrl
      }
    }
  }
}
