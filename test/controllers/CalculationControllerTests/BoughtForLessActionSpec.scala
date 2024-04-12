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

import org.apache.pekko.stream.Materializer
import assets.MessageLookup.{NonResident => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import constructors.{AnswersConstructor, DefaultCalculationElectionConstructor}
import controllers.BoughtForLessController
import controllers.helpers.FakeRequestHelper
import models.BoughtForLessModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.boughtForLess

import scala.concurrent.{ExecutionContext, Future}

class BoughtForLessActionSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {
  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))
  val ec = fakeApplication.injector.instanceOf[ExecutionContext]

  val materializer = mock[Materializer]
  val mockHttp =mock[DefaultHttpClient]
  val mockCalcConnector =mock[CalculatorConnector]
  val mockAnswersConstructor = mock[AnswersConstructor]
  val mockDefaultCalElecConstructor = mock[DefaultCalculationElectionConstructor]
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val boughtForLessView = fakeApplication.injector.instanceOf[boughtForLess]
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]

  lazy val pageTitle = s"""${messages.BoughtForLess.question} - ${messages.serviceName} - GOV.UK"""

  class Setup {
    val controller = new BoughtForLessController(
      mockHttp,
      mockSessionCacheService,
      mockDefaultCalElecConstructor,
      mockMessagesControllerComponents,
      boughtForLessView
    )(ec)
  }

  def setupTarget(getData: Option[BoughtForLessModel]): BoughtForLessController = {

    when(mockSessionCacheService.fetchAndGetFormData[BoughtForLessModel](ArgumentMatchers.anyString())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheService.saveFormData[BoughtForLessModel](
      ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(("", "")))

    new BoughtForLessController(mockHttp, mockSessionCacheService, mockDefaultCalElecConstructor, mockMessagesControllerComponents, boughtForLessView)(ec) {
    }
  }

  "Calling .boughtForLess" when {

    "provided with no previous data" should {
      val target = setupTarget(None)
      lazy val result = target.boughtForLess(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the bought for less page" in {
        document.title() shouldBe pageTitle
      }
    }

    "provided with some previous data" should {
      val target = setupTarget(Some(BoughtForLessModel(true)))
      lazy val result = target.boughtForLess(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the bought for less page" in {
        document.title() shouldBe pageTitle
      }
    }

    "provided without a valid session" should {
      val target = setupTarget(None)
      lazy val result = target.boughtForLess(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect the user to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling .submitBoughtForLess" when {

    "submitting a valid form with an answer of Yes" should {
      lazy val request = fakeRequestToPOSTWithSession(("boughtForLess", "Yes")).withMethod("POST")
      lazy val target = setupTarget(None)
      lazy val result = target.submitBoughtForLess(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the worthWhenBoughtForLess page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.WorthWhenBoughtForLessController.worthWhenBoughtForLess.url)
      }
    }

    "submitting a valid form with an answer of No" should {
      lazy val request = fakeRequestToPOSTWithSession(("boughtForLess", "No")).withMethod("POST")
      lazy val target = setupTarget(None)
      lazy val result = target.submitBoughtForLess(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the acquisitionValue page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.AcquisitionValueController.acquisitionValue.url)
      }
    }

    "submitting an invalid form" should {
      lazy val request = fakeRequestToPOSTWithSession(("boughtForLess", ""))
      lazy val target = setupTarget(None)
      lazy val result = target.submitBoughtForLess(request)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "return to the Bought For Less page" in {
        document.title() shouldBe s"Error: $pageTitle"
      }
    }
  }
}
