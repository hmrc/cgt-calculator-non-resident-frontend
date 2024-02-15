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
import assets.MessageLookup.NonResident.{OtherProperties => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import com.codahale.metrics.SharedMetricRegistries
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.utils.TimeoutController
import controllers.{OtherPropertiesController, routes}
import models.OtherPropertiesModel
import org.jsoup._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.otherProperties
import views.html.warnings.sessionTimeout

import scala.concurrent.{ExecutionContext, Future}

class OtherPropertiesActionSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))

  val materializer = mock[Materializer]
  val ec = fakeApplication.injector.instanceOf[ExecutionContext]
  val mockHttp =mock[DefaultHttpClient]
  val mockCalcConnector =mock[CalculatorConnector]
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val otherPropertiesView = fakeApplication.injector.instanceOf[otherProperties]
  val sessionTimeoutView = fakeApplication.injector.instanceOf[sessionTimeout]
  lazy val pageTitle = s"""${messages.question} - ${commonMessages.pageHeading} - GOV.UK"""
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]

  class Setup {
    val controller = new OtherPropertiesController(
      mockHttp,
      mockSessionCacheService,
      mockMessagesControllerComponents,
      otherPropertiesView
    )(ec)
  }

  def setupTarget(getData: Option[OtherPropertiesModel]): OtherPropertiesController = {
    SharedMetricRegistries.clear()

    when(mockSessionCacheService.fetchAndGetFormData[OtherPropertiesModel](ArgumentMatchers.anyString())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheService.saveFormData[OtherPropertiesModel](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(("", ""))

    new OtherPropertiesController(mockHttp, mockSessionCacheService, mockMessagesControllerComponents, otherPropertiesView)(ec)
  }

  val controller = new TimeoutController(mockMessagesControllerComponents, sessionTimeoutView)

  // GET Tests
  "Calling the CalculationController.otherProperties" when {

    "no session is active" should {
      lazy val target = setupTarget(None)
      lazy val result = target.otherProperties(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${controller.timeout()}" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }

    "not supplied with a pre-existing stored model" should {

      "for a customer type of Individual" should {

        val target = setupTarget(None)
        lazy val result = target.otherProperties(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

        "return a 200" in {
          status(result) shouldBe 200
        }

        "display the other properties page" in {
          document.title shouldEqual pageTitle
        }
      }
    }
  }

  // POST Tests
  "In CalculationController calling the .submitOtherProperties action" when {

    "submitting a valid form with 'Yes'" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("otherProperties", "Yes")).withMethod("POST")
      lazy val result = target.submitOtherProperties(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "should redirect to the previous gain or loss page" in {
        redirectLocation(result) shouldBe Some(s"${routes.PreviousGainOrLossController.previousGainOrLoss}")
      }
    }


    "submitting a valid form with 'No'" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("otherProperties", "No")).withMethod("POST")
      lazy val result = target.submitOtherProperties(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "should redirect to the broughtForwardLosses page" in {
        redirectLocation(result) shouldBe Some(s"${routes.BroughtForwardLossesController.broughtForwardLosses}")
      }
    }

    "submitting an form with no data" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("otherProperties", "")).withMethod("POST")
      lazy val result = target.submitOtherProperties(request)

      "return a 400" in {
        status(result) shouldBe 400
      }

      "return to the other properties page" in {
        Jsoup.parse(bodyOf(result)(materializer, ec)).select("title").text shouldEqual s"""Error: ${pageTitle}"""
      }
    }
  }
}
