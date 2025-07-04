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

import assets.MessageLookup.NonResident.{WorthWhenBoughtForLess => messages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.WorthWhenBoughtForLessController
import controllers.helpers.FakeRequestHelper
import models.AcquisitionValueModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.worthWhenBoughtForLess

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class WorthWhenBoughtForLessActionSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {


  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockHttp: DefaultHttpClient =mock[DefaultHttpClient]
  val mockCalcConnector: CalculatorConnector =mock[CalculatorConnector]
  val mockMessagesControllerComponents: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val worthWhenBoughtForLessView: worthWhenBoughtForLess = fakeApplication.injector.instanceOf[worthWhenBoughtForLess]
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  lazy val pageTitle: String = s"""${messages.question} - ${messages.pageHeading} - GOV.UK"""

  class Setup {
    val controller = new WorthWhenBoughtForLessController(
      mockHttp,
      mockSessionCacheService,
      mockMessagesControllerComponents,
      worthWhenBoughtForLessView
    )
  }

  def setupTarget(getData: Option[AcquisitionValueModel]): WorthWhenBoughtForLessController = {

    when(mockSessionCacheService.fetchAndGetFormData[AcquisitionValueModel](
      ArgumentMatchers.eq(KeystoreKeys.acquisitionMarketValue))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheService.saveFormData[AcquisitionValueModel](ArgumentMatchers.any(), ArgumentMatchers.any())(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(("", "")))

    new WorthWhenBoughtForLessController(mockHttp, mockSessionCacheService, mockMessagesControllerComponents, worthWhenBoughtForLessView)
  }

  "Calling .worthWhenBoughtForLess" when {

    "request has a valid session and no keystore value" should {

      lazy val target = setupTarget(None)
      lazy val result = target.worthWhenBoughtForLess(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.question}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(contentAsString(result)).select("h1").text shouldEqual messages.question
      }
    }

    "request has a valid session and some keystore value" should {

      lazy val target = setupTarget(Some(AcquisitionValueModel(BigDecimal(1000.00))))
      lazy val result = target.worthWhenBoughtForLess(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.question}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(contentAsString(result)).select("h1").text shouldEqual messages.question
      }
    }

    "request has an invalid session" should {

      lazy val target = setupTarget(None)
      lazy val result = target.worthWhenBoughtForLess(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling .submitWorthWhenBoughtForLess" should {

    "with valid form with the answer '1000.00'" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("acquisitionMarketValue", "1000.00")).withMethod("POST")
      lazy val result = target.submitWorthWhenBoughtForLess(request)

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
      lazy val result = target.submitWorthWhenBoughtForLess(request)
      lazy val doc = Jsoup.parse(contentAsString(result))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "return to the Worth When Bought For Less page" in {
        doc.title() shouldBe s"Error: $pageTitle"
      }
    }
  }
}
