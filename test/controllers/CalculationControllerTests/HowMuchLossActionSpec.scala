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
import assets.MessageLookup.{NonResident => messages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.HowMuchLossController
import controllers.helpers.FakeRequestHelper
import models.HowMuchLossModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.howMuchLoss

import scala.concurrent.{ExecutionContext, Future}

class HowMuchLossActionSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val materializer = mock[Materializer]
  val ec = fakeApplication.injector.instanceOf[ExecutionContext]
  val mockHttp =mock[DefaultHttpClient]
  val mockCalcConnector =mock[CalculatorConnector]
  val mockMessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val howMuchLossView = fakeApplication.injector.instanceOf[howMuchLoss]
  val pageTitle = s"""${messages.HowMuchLoss.question} - ${messages.pageHeading} - GOV.UK"""
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]

  class Setup {
    val controller = new HowMuchLossController(
      mockHttp,
      mockSessionCacheService,
      mockMessagesControllerComponents,
      howMuchLossView
    )(ec)
  }

  def setupTarget(getData: Option[HowMuchLossModel]): HowMuchLossController = {

    when(mockSessionCacheService.fetchAndGetFormData[HowMuchLossModel](ArgumentMatchers.eq(KeystoreKeys.howMuchLoss))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(getData)

    when(mockSessionCacheService.saveFormData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(("", "")))

    new HowMuchLossController(mockHttp, mockSessionCacheService, mockMessagesControllerComponents, howMuchLossView)(ec)
  }

  "Calling the .howMuchLoss method" when {

    "not provided with any data" should {
      val target = setupTarget(None)
      lazy val result = target.howMuchLoss(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the How Much Loss Page" in {
        document.title() shouldBe pageTitle
      }
    }

    "provided with some data" should {
      val target = setupTarget(Some(HowMuchLossModel(100)))
      lazy val result = target.howMuchLoss(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the How Much Loss Page" in {
        document.title() shouldBe pageTitle
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
      lazy val request = fakeRequestToPOSTWithSession(("loss", "100")).withMethod("POST")
      lazy val result = target.submitHowMuchLoss(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the Brought Forward Losses page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.BroughtForwardLossesController.broughtForwardLosses.url)
      }
    }

    "a valid form is submitted with a zero value" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("loss", "0")).withMethod("POST")
      lazy val result = target.submitHowMuchLoss(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the Annual Exempt Amount page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.AnnualExemptAmountController.annualExemptAmount.url)
      }
    }

    "an invalid form is submitted" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("loss", ""))
      lazy val result = target.submitHowMuchLoss(request)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "return to the How Much Loss page" in {
        document.title shouldBe s"""Error: ${pageTitle}"""
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
