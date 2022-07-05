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
import assets.MessageLookup.NonResident.{PreviousLossOrGain => messages}
import common.KeystoreKeys.{NonResidentKeys => keystoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.PreviousGainOrLossController
import controllers.helpers.FakeRequestHelper
import models.PreviousLossOrGainModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.previousLossOrGain

import scala.concurrent.{ExecutionContext, Future}

class PreviousGainOrLossActionSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val materializer = mock[Materializer]
  val ec = fakeApplication.injector.instanceOf[ExecutionContext]
  val mockHttp =mock[DefaultHttpClient]
  val mockCalcConnector =mock[CalculatorConnector]
  val defaultCache = mock[CacheMap]
  val mockMessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val previousLossOrGainView = fakeApplication.injector.instanceOf[previousLossOrGain]

  class Setup {
    val controller = new PreviousGainOrLossController(
      mockHttp,
      mockCalcConnector,
      mockMessagesControllerComponents,
      previousLossOrGainView
    )(ec)
  }

  def setupTarget(getData: Option[PreviousLossOrGainModel]): PreviousGainOrLossController = {
    when(mockCalcConnector.fetchAndGetFormData[PreviousLossOrGainModel](
      ArgumentMatchers.eq(keystoreKeys.previousLossOrGain))(ArgumentMatchers.any(), ArgumentMatchers.any())).
      thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData[PreviousLossOrGainModel](
      ArgumentMatchers.eq(keystoreKeys.previousLossOrGain), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())).
      thenReturn(mock[CacheMap])

    new PreviousGainOrLossController(mockHttp, mockCalcConnector, mockMessagesControllerComponents, previousLossOrGainView)(ec)
  }

  "Calling .previousGainOrLoss from the PreviousGainOrLossController" when {
    "there is no keystore data" should {
      lazy val target = setupTarget(None)
      lazy val result = target.previousGainOrLoss(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldEqual 200
      }

      s"return some HTML with title of ${messages.question}" in {
        Jsoup.parse(bodyOf(result)(materializer, ec)).select("h1").text shouldEqual messages.question
      }
    }

    "there is keystore data" should {
      lazy val target = setupTarget(Some(PreviousLossOrGainModel("Loss")))
      lazy val result = target.previousGainOrLoss(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldEqual 200
      }

      s"return some html with title of ${messages.question}" in {
        Jsoup.parse(bodyOf(result)(materializer, ec)).select("h1").text shouldEqual messages.question
      }
    }

    "there is no valid session" should {
      lazy val target = setupTarget(None)
      lazy val result = target.previousGainOrLoss(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling .submitPreviousGainOrLoss from the PreviousGainOrLossController" when {
    "a valid form is submitted with the value 'Loss'" should {
      lazy val target = setupTarget(None)
      lazy val result = target.submitPreviousGainOrLoss(fakeRequestToPOSTWithSession(("previousLossOrGain", "Loss")).withMethod("POST"))

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the how much loss page" in {
        redirectLocation(result).get shouldBe controllers.routes.HowMuchLossController.howMuchLoss.url
      }
    }

    "a valid form is submitted with the value 'Gain'" should {
      lazy val target = setupTarget(None)
      lazy val result = target.submitPreviousGainOrLoss(fakeRequestToPOSTWithSession(("previousLossOrGain", "Gain")).withMethod("POST"))

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the how much gained page" in {
        redirectLocation(result).get shouldBe controllers.routes.HowMuchGainController.howMuchGain.url
      }
    }

    "a valid form is submitted with the value 'Neither" should {
      lazy val target = setupTarget(None)
      lazy val result = target.submitPreviousGainOrLoss(fakeRequestToPOSTWithSession(("previousLossOrGain","Neither")).withMethod("POST"))

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the AEA page" in {
        redirectLocation(result).get shouldBe controllers.routes.AnnualExemptAmountController.annualExemptAmount.url
      }
    }

    "an invalid form is submitted" should {
      lazy val target = setupTarget(None)
      lazy val result = target.submitPreviousGainOrLoss(fakeRequestToPOSTWithSession(("previousLossOrGain", "invalid text")))
      lazy val doc = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "return to the page" in {
        doc.title shouldEqual s"Error: ${messages.title}"
      }
    }
  }
}
