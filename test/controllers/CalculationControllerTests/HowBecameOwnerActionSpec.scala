/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.CalculationControllerTests

import akka.stream.Materializer
import assets.MessageLookup.NonResident.{HowBecameOwner => messages}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.HowBecameOwnerController
import controllers.helpers.FakeRequestHelper
import models.HowBecameOwnerModel
import org.jsoup._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.logging.SessionId
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class HowBecameOwnerActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))

  val materializer = mock[Materializer]
  val mockHttp =mock[DefaultHttpClient]
  val mockCalcConnector =mock[CalculatorConnector]
  val defaultCache = mock[CacheMap]
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]


  class Setup {
    val controller = new HowBecameOwnerController(
      mockHttp,
      mockCalcConnector,
      mockMessagesControllerComponents
    )(mockConfig, fakeApplication)
  }

  def setupTarget(getData: Option[HowBecameOwnerModel]): HowBecameOwnerController = {

    when(mockCalcConnector.fetchAndGetFormData[HowBecameOwnerModel](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(getData)

    when(mockCalcConnector.saveFormData[HowBecameOwnerModel](
      ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(mock[CacheMap])

    new HowBecameOwnerController(mockHttp, mockCalcConnector, mockMessagesControllerComponents)(mockConfig, fakeApplication)
  }

  "Calling .howBecameOwner action" when {

    "provided with a valid session with no stored data" should {
      lazy val target = setupTarget(None)
      lazy val result = target.howBecameOwner(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result)(materializer))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.question}" in {
        doc.title shouldEqual messages.question
      }
    }

    "provided with a valid session with stored data" should {
      lazy val target = setupTarget(Some(HowBecameOwnerModel("Bought")))
      lazy val result = target.howBecameOwner(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result)(materializer))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.question}" in {
        doc.title shouldEqual messages.question
      }
    }

    "provided with an invalid session" should {
      lazy val target = setupTarget(None)
      lazy val result = target.howBecameOwner(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling .submitHowBecameOwner action" when {
    "a valid form with the answer 'Bought' is submitted" should {
      lazy val target = setupTarget(None)
      lazy val result = target.submitHowBecameOwner(fakeRequestToPOSTWithSession(("gainedBy", "Bought")))

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the bought-for-less-than-worth page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/non-resident/bought-for-less")
      }
    }
  }

  "a valid form with the answer 'Inherited' is submitted" should {
    lazy val target = setupTarget(None)
    lazy val result = target.submitHowBecameOwner(fakeRequestToPOSTWithSession(("gainedBy", "Inherited")))

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "redirect to the worth-when-inherited page" in {
      redirectLocation(result).get should include ("/calculate-your-capital-gains/non-resident/worth-when-inherited")
    }
  }


  "a valid form with the answer 'Gifted' is submitted" should {
    lazy val target = setupTarget(None)
    lazy val result = target.submitHowBecameOwner(fakeRequestToPOSTWithSession(("gainedBy", "Gifted")))

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "redirect to the worth-when-gifted page" in {
      redirectLocation(result).get should include ("/calculate-your-capital-gains/non-resident/worth-when-gifted")
    }
  }


  "an invalid form with no answer is submitted" should {
    lazy val target = setupTarget(None)
    lazy val result = target.submitHowBecameOwner(fakeRequestToPOSTWithSession(("gainedBy", "")))
    lazy val doc = Jsoup.parse(bodyOf(result)(materializer))

    "return a status of 400" in {
      status(result) shouldBe 400
    }

    "return to the page" in {
      doc.title shouldEqual messages.question
    }

    "raise an error on the page" in {
      doc.body.select("#gainedBy-error-summary").size shouldBe 1
    }
  }
}
