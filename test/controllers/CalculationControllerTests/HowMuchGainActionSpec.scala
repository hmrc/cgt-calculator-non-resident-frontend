/*
 * Copyright 2022 HM Revenue & Customs
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
import assets.MessageLookup.{NonResident => commonMessages}
import assets.MessageLookup.NonResident.{HowMuchGain => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.HowMuchGainController
import controllers.helpers.FakeRequestHelper
import models.HowMuchGainModel
import org.jsoup._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.howMuchGain

import scala.concurrent.{ExecutionContext, Future}

class HowMuchGainActionSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val materializer = mock[Materializer]
  val ec = fakeApplication.injector.instanceOf[ExecutionContext]
  val mockHttp =mock[DefaultHttpClient]
  val mockCalcConnector =mock[CalculatorConnector]
  val defaultCache = mock[CacheMap]
  val mockMessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val howMuchGainView = fakeApplication.injector.instanceOf[howMuchGain]
  val pageTitle = s"""${messages.question} - ${commonMessages.pageHeading} - GOV.UK"""

  class Setup {
    val controller = new HowMuchGainController(
      mockHttp,
      mockCalcConnector,
      mockMessagesControllerComponents,
      howMuchGainView
    )(ec)
  }

  def setupTarget(getData: Option[HowMuchGainModel]): HowMuchGainController = {

    when(mockCalcConnector.fetchAndGetFormData[HowMuchGainModel](ArgumentMatchers.anyString())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map.empty)))

    new HowMuchGainController(mockHttp, mockCalcConnector, mockMessagesControllerComponents, howMuchGainView)(ec)
  }

  //GET Tests
  "In CalculationController calling the .howMuchGain action" when {

    "not supplied with a pre-existing stored model" should {
      val target = setupTarget(None)
      lazy val result = target.howMuchGain(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the How Much Gain page" in {
        document.title shouldBe pageTitle
      }
    }

    "supplied with a pre-existing stored model" should {
      val target = setupTarget(Some(HowMuchGainModel(1000)))
      lazy val result = target.howMuchGain(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the How Much Gain page" in {
        document.title shouldBe pageTitle
      }
    }

    "without a valid session" should {
      val target = setupTarget(None)
      lazy val result = target.howMuchGain(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "In CalculationController calling the .submitHowMuchGain action" when {

    "submitting a valid form" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("howMuchGain", "1000"))
      lazy val result = target.submitHowMuchGain(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the Brought Forward Losses page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.BroughtForwardLossesController.broughtForwardLosses().url)
      }
    }

    "a valid form is submitted with a zero value" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("howMuchGain", "0"))
      lazy val result = target.submitHowMuchGain(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the Annual Exempt Amount page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.AnnualExemptAmountController.annualExemptAmount().url)
      }
    }

    "submitting an invalid form" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("howMuchGain", "-100"))
      lazy val result = target.submitHowMuchGain(request)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "load the How Much Gain page" in {
        document.title shouldBe s"""Error: ${pageTitle}"""
      }
    }

    "an invalid session is submitted" should {
      val target = setupTarget(None)
      lazy val result = target.howMuchGain(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }
}
