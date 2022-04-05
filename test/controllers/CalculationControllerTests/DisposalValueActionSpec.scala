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
import assets.MessageLookup.NonResident.{DisposalValue => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.DisposalValueController
import controllers.helpers.FakeRequestHelper
import models.DisposalValueModel
import org.jsoup._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.disposalValue

import scala.concurrent.{ExecutionContext, Future}

class DisposalValueActionSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {


  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))

  val materializer = mock[Materializer]
  val ec = fakeApplication.injector.instanceOf[ExecutionContext]
  val mockHttp =mock[DefaultHttpClient]
  val mockCalcConnector =mock[CalculatorConnector]
  val defaultCache = mock[CacheMap]
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val disposalValueView = fakeApplication.injector.instanceOf[disposalValue]

  class Setup {
    val controller = new DisposalValueController(
      mockHttp,
      mockCalcConnector,
      mockMessagesControllerComponents,
      disposalValueView
    )(ec)
  }


  def setupTarget(getData: Option[DisposalValueModel]): DisposalValueController = {


    when(mockCalcConnector.fetchAndGetFormData[DisposalValueModel](ArgumentMatchers.anyString())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map.empty)))

    new DisposalValueController(mockHttp,mockCalcConnector, mockMessagesControllerComponents, disposalValueView)(ec)
  }

  //GET Tests
  "In CalculationController calling the .disposalValue action" when {

    "not supplied with a pre-existing stored model" should {
      val target = setupTarget(None)
      lazy val result = target.disposalValue(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the disposal value page" in {
        document.title shouldBe messages.question
      }
    }

    "supplied with a pre-existing stored model" should {
      val target = setupTarget(Some(DisposalValueModel(1000)))
      lazy val result = target.disposalValue(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the disposal value page" in {
        document.title shouldBe messages.question
      }
    }

    "without a valid session" should {
      val target = setupTarget(None)
      lazy val result = target.disposalValue(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "In CalculationController calling the .submitDisposalValue action" when {

    "submitting a valid form" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("disposalValue", "1000"))
      lazy val result = target.submitDisposalValue(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the Disposal Costs page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.DisposalCostsController.disposalCosts().url)
      }
    }

    "submitting an invalid form" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("disposalValue", "-100"))
      lazy val result = target.submitDisposalValue(request)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "load the Disposal Value page" in {
        document.title shouldBe messages.question
      }
    }
  }
}
