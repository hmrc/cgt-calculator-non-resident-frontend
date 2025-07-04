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

import assets.MessageLookup.NonResident.{MarketValue => marketValueMessages}
import assets.MessageLookup.{NonResident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.MarketValueWhenSoldOrGaveAwayController
import controllers.helpers.FakeRequestHelper
import models.DisposalValueModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.{marketValueGaveAway, marketValueSold}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MarketValueWhenSoldOrGaveAwayActionSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockEnvironment: Environment =mock[Environment]
  val mockHttp: DefaultHttpClient =mock[DefaultHttpClient]
  val mockCalcConnector: CalculatorConnector =mock[CalculatorConnector]
  val mockMessagesControllerComponents: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val marketValueSoldView: marketValueSold = fakeApplication.injector.instanceOf[marketValueSold]
  val marketValueGaveAwayView: marketValueGaveAway = fakeApplication.injector.instanceOf[marketValueGaveAway]
  val pageTitle: String = s"""${marketValueMessages.disposalSoldQuestion} - ${commonMessages.serviceName} - GOV.UK"""
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]

  class Setup {
    val controller = new MarketValueWhenSoldOrGaveAwayController(
      mockEnvironment,
      mockHttp,
      mockSessionCacheService,
      mockMessagesControllerComponents,
      marketValueSoldView,
      marketValueGaveAwayView
    )
  }

  def setupTarget(getData: Option[DisposalValueModel]): MarketValueWhenSoldOrGaveAwayController = {

    when(mockSessionCacheService.fetchAndGetFormData[DisposalValueModel](ArgumentMatchers.anyString())(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheService.saveFormData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(("", "")))

    new MarketValueWhenSoldOrGaveAwayController(mockEnvironment, mockHttp, mockSessionCacheService, mockMessagesControllerComponents, marketValueSoldView, marketValueGaveAwayView)
  }

  "The marketValueWhenGaveAway action" when {
    "not supplied with a pre-existing stored model" should {
      val target = setupTarget(None)
      lazy val result = target.marketValueWhenGaveAway(fakeRequestWithSession)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the market value page" in {
        document.title shouldBe s"${marketValueMessages.disposalGaveAwayQuestion} - Calculate your Non-Resident Capital Gains Tax - GOV.UK"
      }
    }

    "supplied with a pre-existing stored model" should {
      val target = setupTarget(Some(DisposalValueModel(1000)))
      lazy val result = target.marketValueWhenGaveAway(fakeRequestWithSession)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the market value page" in {
        document.title shouldBe s"${marketValueMessages.disposalGaveAwayQuestion} - Calculate your Non-Resident Capital Gains Tax - GOV.UK"
      }
    }

    "without a valid session" should {
      val target = setupTarget(None)
      lazy val result = target.marketValueWhenGaveAway(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "The marketValueWhenSold action" when {
    "not supplied with a pre-existing stored model" should {
      val target = setupTarget(None)
      lazy val result = target.marketValueWhenSold(fakeRequestWithSession)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the market value page" in {
        document.title shouldBe pageTitle
      }
    }

    "supplied with a pre-existing stored model" should {
      val target = setupTarget(Some(DisposalValueModel(1000)))
      lazy val result = target.marketValueWhenSold(fakeRequestWithSession)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the market value page" in {
        document.title shouldBe pageTitle
      }
    }

    "without a valid session" should {
      val target = setupTarget(None)
      lazy val result = target.marketValueWhenSold(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }

    "supplied with an invalid form" should {
      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("disposalValue", "invalid text"))
      lazy val result = target.submitMarketValueWhenSold(request)
      lazy val document = Jsoup.parse(contentAsString(result))

      "generate a 400 error" in {
        status(result) shouldEqual 400
      }

      s"and lead to the current page reloading and return some HTML with title of $pageTitle" in {
        document.title shouldEqual s"Error: $pageTitle"
      }
    }
  }

  "The submitMarketValueWhenSold action" when {
    "submitting a valid form" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("disposalValue", "1000")).withMethod("POST")
      lazy val result = target.submitMarketValueWhenSold(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the DisposalCosts page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.DisposalCostsController.disposalCosts.url)
      }
    }
  }

  "The submitMarketValueWhenGaveAway action" when {
    "submitting a valid form" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("disposalValue", "1000")).withMethod("POST")
      lazy val result = target.submitMarketValueWhenGaveAway(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the DisposalCosts page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.DisposalCostsController.disposalCosts.url)
      }
    }


    "supplied with an invalid form" should {
      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("disposalValue", "invalid text")).withMethod("POST")
      lazy val result = target.submitMarketValueWhenGaveAway(request)
      lazy val document = Jsoup.parse(contentAsString(result))

      "generate a 400 error" in {
        status(result) shouldEqual 400
      }

      s"and lead to the current page reloading and return some HTML with title of ${marketValueMessages.disposalGaveAwayQuestion}" in {
        document.title shouldEqual s"Error: ${marketValueMessages.disposalGaveAwayQuestion} - Calculate your Non-Resident Capital Gains Tax - GOV.UK"
      }
    }
  }
}
