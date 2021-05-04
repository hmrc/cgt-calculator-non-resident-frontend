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
import assets.MessageLookup.NonResident.{WorthBeforeLegislationStart => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.WorthBeforeLegislationStartController
import controllers.helpers.FakeRequestHelper
import models.WorthBeforeLegislationStartModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.concurrent.Future

class WorthBeforeLegislationStartActionSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper{

  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val materializer = mock[Materializer]
  val mockEnvironment =mock[Environment]
  val mockHttp =mock[DefaultHttpClient]
  val mockCalcConnector =mock[CalculatorConnector]
  val defaultCache = mock[CacheMap]
  val mockMessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]


  class Setup {
    val controller = new WorthBeforeLegislationStartController(
      mockHttp,
      mockCalcConnector,
      mockMessagesControllerComponents
    )(mockConfig, fakeApplication)
  }


  def setUpTarget(getData: Option[WorthBeforeLegislationStartModel]): WorthBeforeLegislationStartController = {

    when(mockCalcConnector.fetchAndGetFormData[WorthBeforeLegislationStartModel](ArgumentMatchers.anyString())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map.empty)))

    new WorthBeforeLegislationStartController(mockHttp, mockCalcConnector, mockMessagesControllerComponents)(mockConfig, fakeApplication)
  }

  "WorthBeforeLegislationStartController" when{

    "calling .worthBeforeLegislationStart" should {


      "request has a valid session and no keystore value" should {

        lazy val target = setUpTarget(None)
        lazy val result = target.worthBeforeLegislationStart(fakeRequestWithSession)

        "return a status of 200" in {
          status(result) shouldBe 200
        }

        s"return some html with title of ${messages.question}" in {
          contentType(result) shouldBe Some("text/html")
          Jsoup.parse(bodyOf(result)(materializer)).title shouldEqual messages.question
        }
      }

      "request has a valid session and some keystore value" should {

        lazy val target = setUpTarget(Some(WorthBeforeLegislationStartModel(BigDecimal(1000.00))))
        lazy val result = target.worthBeforeLegislationStart(fakeRequestWithSession)

        "return a status of 200" in {
          status(result) shouldBe 200
        }

        s"return some html with title of ${messages.question}" in {
          contentType(result) shouldBe Some("text/html")
          Jsoup.parse(bodyOf(result)(materializer)).title shouldEqual messages.question
        }
      }

      "request has an invalid session" should {

        lazy val target = setUpTarget(None)
        lazy val result = target.worthBeforeLegislationStart(fakeRequest)

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        "return you to the session timeout page" in {
          redirectLocation(result).get should include ("/calculate-your-capital-gains/non-resident/session-timeout")
        }
      }
    }

    "Calling .submitWorthBeforeLegislationStart" should {

      "with valid form with the answer '1000.00'" should {

        lazy val target = setUpTarget(None)
        lazy val request = fakeRequestToPOSTWithSession(("worthBeforeLegislationStart", "1000.00"))
        lazy val result = target.submitWorthBeforeLegislationStart(request)

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        "redirect to the costs at legislation start page" in {
          redirectLocation(result) shouldBe Some(controllers.routes.CostsAtLegislationStartController.costsAtLegislationStart().url)
        }
      }

      "with an invalid form with the answer 'a'" should {

        lazy val target = setUpTarget(None)
        lazy val request = fakeRequestToPOSTWithSession(("worthBeforeLegislationStart", "a"))
        lazy val result = target.submitWorthBeforeLegislationStart(request)
        lazy val doc = Jsoup.parse(bodyOf(result)(materializer))

        "return a status of 400" in {
          status(result) shouldBe 400
        }

        "return to the Worth Before Legislation Start page" in {
          doc.title() shouldEqual messages.question
        }
      }
    }
  }

}
