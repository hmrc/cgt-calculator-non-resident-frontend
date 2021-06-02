/*
 * Copyright 2021 HM Revenue & Customs
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
import assets.MessageLookup.NonResident.{WorthWhenInherited => messages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import constructors.AnswersConstructor
import controllers.WorthWhenInheritedController
import controllers.helpers.FakeRequestHelper
import models.AcquisitionValueModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.worthWhenInherited

import scala.concurrent.{ExecutionContext, Future}

class WorthWhenInheritedActionSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))

  val materializer = mock[Materializer]
  val ec = fakeApplication.injector.instanceOf[ExecutionContext]
  val mockHttp =mock[DefaultHttpClient]
  val mockCalcConnector =mock[CalculatorConnector]
  val mockAnswerConstuctor = mock[AnswersConstructor]
  val defaultCache = mock[CacheMap]
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val worthWhenInheritedView = fakeApplication.injector.instanceOf[worthWhenInherited]

  class Setup {
    val controller = new WorthWhenInheritedController(
      mockHttp,
      mockCalcConnector,
      mockMessagesControllerComponents,
      worthWhenInheritedView
    )(ec)
  }

  def setupTarget(getData: Option[AcquisitionValueModel]): WorthWhenInheritedController = {


    when(mockCalcConnector.fetchAndGetFormData[AcquisitionValueModel](
      ArgumentMatchers.eq(KeystoreKeys.acquisitionMarketValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData[AcquisitionValueModel](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new WorthWhenInheritedController(mockHttp, mockCalcConnector, mockMessagesControllerComponents, worthWhenInheritedView)(ec)
  }

  "Calling .worthWhenInherited" when {

    "request has a valid session and no keystore value" should {

      lazy val target = setupTarget(None)
      lazy val result = target.worthWhenInherited(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.question}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(bodyOf(result)(materializer, ec)).title shouldEqual messages.question
      }
    }

    "request has a valid session and some keystore value" should {

      lazy val target = setupTarget(Some(AcquisitionValueModel(BigDecimal(1000.00))))
      lazy val result = target.worthWhenInherited(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.question}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(bodyOf(result)(materializer, ec)).title shouldEqual messages.question
      }
    }

    "request has an invalid session" should {

      lazy val target = setupTarget(None)
      lazy val result = target.worthWhenInherited(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling .submitWorthWhenInherited" should {

    "with valid form with the answer '1000.00'" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("acquisitionMarketValue", "1000.00"))
      lazy val result = target.submitWorthWhenInherited(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the acquisition costs page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/non-resident/acquisition-costs")
      }
    }

    "with an invalid form with the answer 'a'" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("acquisitionMarketValue", "a"))
      lazy val result = target.submitWorthWhenInherited(request)
      lazy val doc = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "return to the Worth When Inherited page" in {
        doc.title() shouldEqual messages.question
      }
    }
  }
}
