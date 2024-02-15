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
import assets.MessageLookup.NonResident.{ClaimingReliefs => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import common.KeystoreKeys.NonResidentKeys
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.ClaimingReliefsController
import controllers.helpers.FakeRequestHelper
import models._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.claimingReliefs

import scala.concurrent.{ExecutionContext, Future}

class ClaimingReliefsActionSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))

  val materializer = mock[Materializer]
  val ec = fakeApplication.injector.instanceOf[ExecutionContext]
  val mockHttp =mock[DefaultHttpClient]
  val mockCalcConnector =mock[CalculatorConnector]
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val claimingReliefsView = fakeApplication.injector.instanceOf[claimingReliefs]
  lazy val pageTitle = s"""${messages.title} - ${commonMessages.pageHeading} - GOV.UK"""
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  class Setup {
    val controller = new ClaimingReliefsController(
      mockHttp,
      mockSessionCacheService,
      mockMessagesControllerComponents,
      claimingReliefsView
    )(ec)
  }
  def setupTarget(model: Option[ClaimingReliefsModel]): ClaimingReliefsController = {

    when(mockSessionCacheService.fetchAndGetFormData[ClaimingReliefsModel](ArgumentMatchers.eq(NonResidentKeys.claimingReliefs))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(model))

    when(mockSessionCacheService.saveFormData[ClaimingReliefsModel](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(("", ""))

    when(mockSessionCacheService.saveFormData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(("", "")))

    new ClaimingReliefsController(mockHttp, mockSessionCacheService, mockMessagesControllerComponents, claimingReliefsView)(ec) {
    }
  }

  "Calling claimingReliefs" when {

    "request has valid session and no keystore value" should {
      lazy val target = setupTarget(None)
      lazy val result = target.claimingReliefs(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return the claiming-reliefs view" in {
        Jsoup.parse(bodyOf(result)(materializer, ec)).title shouldBe pageTitle
      }
    }

    "request has valid session and some keystore value" should {
      lazy val target = setupTarget(Some(ClaimingReliefsModel(true)))
      lazy val result = target.claimingReliefs(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return the claiming-reliefs view" in {
        Jsoup.parse(bodyOf(result)(materializer, ec)).title shouldBe pageTitle
      }
    }

    "request has no session" should {
      lazy val target = setupTarget(None)
      lazy val result = target.claimingReliefs(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return to the session timeout page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling submitClaimingReliefs" when {

    "a valid form with 'Yes' is selected" should {
      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("isClaimingReliefs", "Yes")).withMethod("POST")
      lazy val result = target.submitClaimingReliefs(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the calculation-election page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/non-resident/calculation-election")
      }
    }

    "a valid form with 'No' is selected" should {
      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("isClaimingReliefs", "No")).withMethod("POST")
      lazy val result = target.submitClaimingReliefs(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the calculation-election page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/non-resident/calculation-election")
      }
    }

    "an invalid form is submitted" should {
      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("isClaimingReliefs", "abc"))
      lazy val result = target.submitClaimingReliefs(request)

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "return to the claiming-reliefs page" in {
        Jsoup.parse(bodyOf(result)(materializer, ec)).title shouldBe "Error: " + pageTitle
      }
    }
  }
}
