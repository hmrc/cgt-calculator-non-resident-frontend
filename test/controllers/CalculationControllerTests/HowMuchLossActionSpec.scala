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

package controllers.CalculationControllerTests

import akka.stream.Materializer
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import models.HowMuchLossModel
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import assets.MessageLookup.{NonResident => messages}
import config.ApplicationConfig
import controllers.{CostsAtLegislationStartController, HowMuchLossController}
import play.api.Environment
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.SessionId
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

class HowMuchLossActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val materializer = mock[Materializer]
  val mockEnvironment =mock[Environment]
  val mockHttp =mock[DefaultHttpClient]
  val mockCalcConnector =mock[CalculatorConnector]
  val defaultCache = mock[CacheMap]

  class Setup {
    val controller = new HowMuchLossController(
      mockEnvironment,
      mockHttp,
      mockCalcConnector
    )(mockConfig)
  }

  def setupTarget(getData: Option[HowMuchLossModel]): HowMuchLossController = {

    when(mockCalcConnector.fetchAndGetFormData[HowMuchLossModel](ArgumentMatchers.eq(KeystoreKeys.howMuchLoss))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(getData)

    when(mockCalcConnector.saveFormData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map.empty)))

    new HowMuchLossController(mockEnvironment, mockHttp, mockCalcConnector)(mockConfig) {
      val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "Calling the .howMuchLoss method" when {

    "not provided with any data" should {
      val target = setupTarget(None)
      lazy val result = target.howMuchLoss(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the How Much Loss Page" in {
        document.title() shouldBe messages.HowMuchLoss.question
      }
    }

    "provided with some data" should {
      val target = setupTarget(Some(HowMuchLossModel(100)))
      lazy val result = target.howMuchLoss(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the How Much Loss Page" in {
        document.title() shouldBe messages.HowMuchLoss.question
      }
    }

    "provided with an invalid session" should {
      val target = setupTarget(None)
      lazy val result = target.howMuchLoss(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling the .submitHowMuchLoss method" when {

    "a valid form is submitted with an non-zero value" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("loss", "100"))
      lazy val result = target.submitHowMuchLoss(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the Brought Forward Losses page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.BroughtForwardLossesController.broughtForwardLosses().url)
      }
    }

    "a valid form is submitted with a zero value" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("loss", "0"))
      lazy val result = target.submitHowMuchLoss(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the Annual Exempt Amount page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.AnnualExemptAmountController.annualExemptAmount().url)
      }
    }

    "an invalid form is submitted" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("loss", ""))
      lazy val result = target.submitHowMuchLoss(request)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "return to the How Much Loss page" in {
        document.title shouldBe messages.HowMuchLoss.question
      }
    }

    "an invalid session is submitted" should {
      val target = setupTarget(None)
      lazy val result = target.howMuchLoss(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }
}
