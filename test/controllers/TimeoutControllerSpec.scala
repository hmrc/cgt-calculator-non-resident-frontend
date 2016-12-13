/*
 * Copyright 2016 HM Revenue & Customs
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

import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, Action}
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class TimeoutControllerSpec extends UnitSpec with WithFakeApplication {

  class fakeRequestTo(url : String, controllerAction : Action[AnyContent]) {
    val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/" + url)
    val result = controllerAction(fakeRequest)
    val jsoupDoc = Jsoup.parse(bodyOf(result))
  }

  "TimeoutController.timeout" should {

    "when called with no session" should {

      object timeoutTestDataItem extends fakeRequestTo("", TimeoutController.timeout("test", "test2"))

      "return a 200" in {
        status(timeoutTestDataItem.result) shouldBe 200
      }

      s"have the home link to /calculate-your-capital-gains/non-resident/" in {
        timeoutTestDataItem.jsoupDoc.select("#homeNavHref").attr("href") shouldEqual "/calculate-your-capital-gains/non-resident/"
      }

      "have the title" in {
        timeoutTestDataItem.jsoupDoc.getElementsByTag("title").text shouldEqual Messages("session.timeout.message")
      }

      "contain the heading 'Your session has timed out." in {
        timeoutTestDataItem.jsoupDoc.select("h1").text shouldEqual Messages("session.timeout.message")
      }

      "have a restart link to href of 'test'" in {
        timeoutTestDataItem.jsoupDoc.getElementById("startAgain").attr("href") shouldEqual "test"
      }
    }
  }
}