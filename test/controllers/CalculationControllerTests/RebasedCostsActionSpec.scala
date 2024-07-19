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
import assets.MessageLookup.NonResident.{RebasedCosts => messages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.RebasedCostsController
import controllers.helpers.FakeRequestHelper
import models.RebasedCostsModel
import org.jsoup._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.rebasedCosts

import scala.concurrent.{ExecutionContext, Future}

class RebasedCostsActionSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc: HeaderCarrier = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))

  val materializer: Materializer = mock[Materializer]
  val ec: ExecutionContext = fakeApplication.injector.instanceOf[ExecutionContext]
  val mockHttp: DefaultHttpClient =mock[DefaultHttpClient]
  val mockCalcConnector: CalculatorConnector =mock[CalculatorConnector]
  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val rebasedCostsView: rebasedCosts = fakeApplication.injector.instanceOf[rebasedCosts]
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  class Setup {
    val controller = new RebasedCostsController(
      mockHttp,
      mockSessionCacheService,
      mockMessagesControllerComponents,
      rebasedCostsView
    )(ec)
  }

  def setupTarget(getData: Option[RebasedCostsModel]): RebasedCostsController = {

    when(mockSessionCacheService.fetchAndGetFormData[RebasedCostsModel](
      ArgumentMatchers.eq(KeystoreKeys.rebasedCosts))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheService.saveFormData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(("", "")))

    new RebasedCostsController(mockHttp, mockSessionCacheService, mockMessagesControllerComponents, rebasedCostsView)(ec)
  }

  // GET Tests
  "Calling the CalculationController.rebasedCosts" when {

    "not supplied with a pre-existing stored model" should {
      val target = setupTarget(None)
      lazy val result = target.rebasedCosts(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the rebased costs page" in {
        document.title() shouldBe messages.title
      }

      "a previous value is supplied with a pre-existing stored model" should {
        val target = setupTarget(Some(RebasedCostsModel("Yes", Some(1500))))
        lazy val result = target.rebasedCosts(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

        "return a 200" in {
          status(result) shouldBe 200
        }

        "load the rebased costs page" in {
          document.title() shouldBe messages.title
        }
      }

      "a request is made without a session" should {
        val target = setupTarget(Some(RebasedCostsModel("Yes", Some(1500))))
        lazy val result = target.rebasedCosts(fakeRequest)

        "return a 303" in {
          status(result) shouldBe 303
        }

        "redirect to the session timeout page" in {
          redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
        }
      }
    }
  }

  // POST Tests
  "Calling the .submitRebasedCosts action" when {

    "a valid form is submitted" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession("hasRebasedCosts" -> "Yes", "rebasedCosts" -> "1000").withMethod("POST")
      lazy val result = target.submitRebasedCosts(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the isClaimingImprovements page" in {
        redirectLocation(result).get shouldBe controllers.routes.ImprovementsController.getIsClaimingImprovements.url
      }
    }

    "an invalid form is submitted" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession("hasRebasedCosts" -> "Yes", "rebasedCosts" -> "")
      lazy val result = target.submitRebasedCosts(request)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "return to the rebased costs page" in {
        document.title() shouldBe messages.errorTitle
      }
    }
  }
}
