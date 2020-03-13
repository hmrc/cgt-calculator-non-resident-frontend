/*
 * Copyright 2020 HM Revenue & Customs
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
import com.codahale.metrics.SharedMetricRegistries
import config.ApplicationConfig
import connectors.CalculatorConnector
import constructors.{AnswersConstructor, DefaultCalculationElectionConstructor}
import controllers.helpers.FakeRequestHelper
import controllers.utils.TimeoutController
import controllers.{CalculationElectionController, OtherPropertiesController, routes}
import models.OtherPropertiesModel
import org.jsoup._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.SessionId
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

class OtherPropertiesActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))

  val materializer = mock[Materializer]
  val mockHttp =mock[DefaultHttpClient]
  val mockCalcConnector =mock[CalculatorConnector]
  val defaultCache = mock[CacheMap]
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]

  class Setup {
    val controller = new OtherPropertiesController(
      mockHttp,
      mockCalcConnector,
      mockMessagesControllerComponents
    )(mockConfig, fakeApplication)
  }

  def setupTarget(getData: Option[OtherPropertiesModel]): OtherPropertiesController = {
    SharedMetricRegistries.clear()

    when(mockCalcConnector.fetchAndGetFormData[OtherPropertiesModel](ArgumentMatchers.anyString())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData[OtherPropertiesModel](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(mock[CacheMap])

    new OtherPropertiesController(mockHttp, mockCalcConnector, mockMessagesControllerComponents)(mockConfig, fakeApplication) {
      val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  val controller = new TimeoutController(mockMessagesControllerComponents)(mockConfig, fakeApplication)

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
        lazy val document = Jsoup.parse(bodyOf(result)(materializer))

        "return a 200" in {
          status(result) shouldBe 200
        }

        "display the other properties page" in {
          document.title shouldEqual messages.question
        }
      }
    }
  }

  // POST Tests
  "In CalculationController calling the .submitOtherProperties action" when {

    "submitting a valid form with 'Yes'" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("otherProperties", "Yes"))
      lazy val result = target.submitOtherProperties(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "should redirect to the previous gain or loss page" in {
        redirectLocation(result) shouldBe Some(s"${routes.PreviousGainOrLossController.previousGainOrLoss()}")
      }
    }


    "submitting a valid form with 'No'" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("otherProperties", "No"))
      lazy val result = target.submitOtherProperties(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "should redirect to the broughtForwardLosses page" in {
        redirectLocation(result) shouldBe Some(s"${routes.BroughtForwardLossesController.broughtForwardLosses()}")
      }
    }

    "submitting an form with no data" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("otherProperties", ""))
      lazy val result = target.submitOtherProperties(request)

      "return a 400" in {
        status(result) shouldBe 400
      }

      "return to the other properties page" in {
        Jsoup.parse(bodyOf(result)(materializer)).select("title").text shouldEqual messages.question
      }
    }
  }
}
