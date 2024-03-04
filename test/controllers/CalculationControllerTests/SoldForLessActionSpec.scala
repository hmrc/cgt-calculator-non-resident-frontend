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

package controllers.CalculationControllerTests

import akka.stream.Materializer
import assets.MessageLookup.NonResident.{SoldForLess => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import common.KeystoreKeys.{NonResidentKeys => keyStoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.SoldForLessController
import controllers.helpers.FakeRequestHelper
import models.SoldForLessModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.soldForLess

import scala.concurrent.{ExecutionContext, Future}

class SoldForLessActionSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val materializer = mock[Materializer]
  val ec = fakeApplication.injector.instanceOf[ExecutionContext]
  val mockHttp =mock[DefaultHttpClient]
  val mockCalcConnector =mock[CalculatorConnector]
  val mockMessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val soldForLessView = fakeApplication.injector.instanceOf[soldForLess]
  lazy val pageTitle = s"""${messages.question} - ${commonMessages.serviceName} - GOV.UK"""
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]

  class Setup {
    val controller = new SoldForLessController(
      mockHttp,
      mockSessionCacheService,
      mockMessagesControllerComponents, soldForLessView)(ec)
  }

  def setupTarget(getData: Option[SoldForLessModel]): SoldForLessController = {

    when(mockSessionCacheService.fetchAndGetFormData[SoldForLessModel](ArgumentMatchers.eq(keyStoreKeys.soldForLess))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheService.saveFormData[SoldForLessModel](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(("", "")))

    new SoldForLessController(mockHttp, mockSessionCacheService, mockMessagesControllerComponents, soldForLessView)(ec)
  }

  "Calling .soldForLess from the nonresident SoldForLess" when {

    "request has a valid session and no keystore value" should {

      lazy val target = setupTarget(None)
      lazy val result = target.soldForLess(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.question}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(bodyOf(result)(materializer, ec)).title shouldEqual pageTitle
      }
    }

    "request has a valid session and some keystore value" should {

      lazy val target = setupTarget(Some(SoldForLessModel(true)))
      lazy val result = target.soldForLess(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.question}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(bodyOf(result)(materializer, ec)).title shouldEqual pageTitle
      }
    }

    "request has an invalid session" should {

      lazy val target = setupTarget(None)
      lazy val result = target.soldForLess(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling .submitSoldForLess from the nonresident SoldForLessController" should {

    "with valid form with the answer 'Yes'" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("soldForLess", "Yes")).withMethod("POST")
      lazy val result = target.submitSoldForLess(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the market value when sold page" in {
        redirectLocation(result).get shouldBe controllers.routes.MarketValueWhenSoldOrGaveAwayController.marketValueWhenSold.url

      }
    }

    "with valid form with the answer 'No'" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("soldForLess", "No")).withMethod("POST")
      lazy val result = target.submitSoldForLess(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the disposal value page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/non-resident/disposal-value")
      }
    }

    "with an invalid form with the answer ''" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("soldForLess", ""))
      lazy val result = target.submitSoldForLess(request)
      lazy val doc = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "return to the Sold for Less page" in {
        doc.title() shouldEqual s"""Error: ${pageTitle}"""
      }
    }
  }
}
