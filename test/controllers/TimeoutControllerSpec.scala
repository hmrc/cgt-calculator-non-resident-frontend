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

import assets.MessageLookup.{NonResident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import controllers.utils.TimeoutController
import org.apache.pekko.util.Timeout
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{Messages, MessagesProvider}
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, status}
import views.html.warnings.sessionTimeout

import scala.concurrent.Future

class TimeoutControllerSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {
  implicit val mockMessagesProvider: MessagesProvider = mock[MessagesProvider]
  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesComponent: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  lazy val timeout: Timeout = mock[Timeout]
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  val sessionTimeoutView: sessionTimeout = fakeApplication.injector.instanceOf[sessionTimeout]
  lazy val pageTitle = s"""${Messages("session.timeout.message")} - ${commonMessages.serviceName} - GOV.UK"""


    class fakeRequestTo(url : String, controllerAction : Action[AnyContent]) {
    val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/calculate-your-capital-gains/" + url)
    val result: Future[Result] = controllerAction(fakeRequest)
    val jsoupDoc: Document = Jsoup.parse(contentAsString(result))
  }

  val controller = new TimeoutController(mockMessagesComponent, sessionTimeoutView)

  "TimeoutController.timeout" should {

    "when called with no session" should {

      object timeoutTestDataItem extends fakeRequestTo("", controller.timeout())

      "return a 200" in {
        status(timeoutTestDataItem.result) shouldBe 200
      }

      "have the title" in {
        timeoutTestDataItem.jsoupDoc.getElementsByTag("title").text shouldEqual pageTitle
      }

      "contain the heading 'Your session has timed out." in {
        timeoutTestDataItem.jsoupDoc.select("h1").text shouldEqual Messages("session.timeout.message")
      }

      "have a restart link to href of 'test'" in {
        timeoutTestDataItem.jsoupDoc.getElementsByClass("govuk-button").first()
          .attr("href") shouldEqual common.DefaultRoutes.restartUrl
      }
    }
  }
}
