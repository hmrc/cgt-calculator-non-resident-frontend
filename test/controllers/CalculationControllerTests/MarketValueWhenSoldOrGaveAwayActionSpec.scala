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
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.{marketValueGaveAway, marketValueSold}

import scala.concurrent.{ExecutionContext, Future}

class MarketValueWhenSoldOrGaveAwayActionSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val materializer = mock[Materializer]
  val ec = fakeApplication.injector.instanceOf[ExecutionContext]
  val mockEnvironment =mock[Environment]
  val mockHttp =mock[DefaultHttpClient]
  val mockCalcConnector =mock[CalculatorConnector]
  val defaultCache = mock[CacheMap]
  val mockMessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val marketValueSoldView = fakeApplication.injector.instanceOf[marketValueSold]
  val marketValueGaveAwayView = fakeApplication.injector.instanceOf[marketValueGaveAway]
  val pageTitle = s"""${marketValueMessages.disposalSoldQuestion} - ${commonMessages.pageHeading} - GOV.UK"""

  class Setup {
    val controller = new MarketValueWhenSoldOrGaveAwayController(
      mockEnvironment,
      mockHttp,
      mockCalcConnector,
      mockMessagesControllerComponents,
      marketValueSoldView,
      marketValueGaveAwayView
    )(ec)
  }

  def setupTarget(getData: Option[DisposalValueModel]): MarketValueWhenSoldOrGaveAwayController = {

    when(mockCalcConnector.fetchAndGetFormData[DisposalValueModel](ArgumentMatchers.anyString())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map.empty)))

    new MarketValueWhenSoldOrGaveAwayController(mockEnvironment, mockHttp, mockCalcConnector, mockMessagesControllerComponents, marketValueSoldView, marketValueGaveAwayView)(ec)
  }

  "The marketValueWhenGaveAway action" when {
    "not supplied with a pre-existing stored model" should {
      val target = setupTarget(None)
      lazy val result = target.marketValueWhenGaveAway(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the market value page" in {
        document.title shouldBe marketValueMessages.disposalGaveAwayQuestion
      }
    }

    "supplied with a pre-existing stored model" should {
      val target = setupTarget(Some(DisposalValueModel(1000)))
      lazy val result = target.marketValueWhenGaveAway(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the market value page" in {
        document.title shouldBe marketValueMessages.disposalGaveAwayQuestion
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
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

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
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

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
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

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
      lazy val request = fakeRequestToPOSTWithSession(("disposalValue", "1000"))
      lazy val result = target.submitMarketValueWhenSold(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the DisposalCosts page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.DisposalCostsController.disposalCosts().url)
      }
    }
  }

  "The submitMarketValueWhenGaveAway action" when {
    "submitting a valid form" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("disposalValue", "1000"))
      lazy val result = target.submitMarketValueWhenGaveAway(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the DisposalCosts page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.DisposalCostsController.disposalCosts().url)
      }
    }


    "supplied with an invalid form" should {
      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("disposalValue", "invalid text"))
      lazy val result = target.submitMarketValueWhenGaveAway(request)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      "generate a 400 error" in {
        status(result) shouldEqual 400
      }

      s"and lead to the current page reloading and return some HTML with title of ${marketValueMessages.disposalGaveAwayQuestion}" in {
        document.title shouldEqual marketValueMessages.disposalGaveAwayQuestion
      }
    }
  }
}
