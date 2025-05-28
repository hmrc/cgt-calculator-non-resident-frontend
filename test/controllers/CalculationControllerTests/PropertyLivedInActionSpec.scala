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

import assets.MessageLookup.{PropertyLivedIn => messages}
import common.KeystoreKeys.{NonResidentKeys => keyStoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.PropertyLivedInController
import controllers.helpers.FakeRequestHelper
import models.PropertyLivedInModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers.{contentType, _}
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.propertyLivedIn

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PropertyLivedInActionSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  val mockHttp: DefaultHttpClient =mock[DefaultHttpClient]
  val mockCalcConnector: CalculatorConnector =mock[CalculatorConnector]
  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val propertyLivedInView: propertyLivedIn = fakeApplication.injector.instanceOf[propertyLivedIn]
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]

  class Setup {
    val controller = new PropertyLivedInController(
      mockHttp,
      mockSessionCacheService,
      mockMessagesControllerComponents,
      propertyLivedInView
    )
  }


  def setupTarget(getData: Option[PropertyLivedInModel]): PropertyLivedInController= {

    when(mockSessionCacheService.fetchAndGetFormData[PropertyLivedInModel](ArgumentMatchers.eq(keyStoreKeys.propertyLivedIn))
      (using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheService.saveFormData[PropertyLivedInModel](ArgumentMatchers.any(), ArgumentMatchers.any())(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(("", "")))

    new PropertyLivedInController(mockHttp, mockSessionCacheService, mockMessagesControllerComponents, propertyLivedInView)
  }

  "Calling .propertyLivedIn from the resident PropertyLivedInController" when {

    "request has a valid session and no keystore value" should {

      lazy val target = setupTarget(None)
      lazy val result = target.propertyLivedIn(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(contentAsString(result)).title shouldEqual s"${messages.title} - Calculate your Non-Resident Capital Gains Tax - GOV.UK"
      }
    }

    "request has a valid session and some keystore value" should {

      lazy val target = setupTarget(Some(PropertyLivedInModel(true)))
      lazy val result = target.propertyLivedIn(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(contentAsString(result)).title shouldEqual s"${messages.title} - Calculate your Non-Resident Capital Gains Tax - GOV.UK"
      }
    }

    "request has an invalid session" should {

      lazy val target = setupTarget(None)
      lazy val result = target.propertyLivedIn(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling .submitPropertyLivedIn " when {

    "a valid form with the answer 'Yes' is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("propertyLivedIn", "Yes")).withMethod("POST")
      lazy val result = target.submitPropertyLivedIn(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the private residence relief page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/non-resident/private-residence-relief")
      }
    }

    "a valid form with the answer 'No' is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("propertyLivedIn", "No")).withMethod("POST")
      lazy val result = target.submitPropertyLivedIn(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the currentIncome page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/non-resident/current-income")
      }
    }

    "an invalid form with the answer '' is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("propertyLivedIn", ""))
      lazy val result = target.submitPropertyLivedIn(request)
      lazy val doc = Jsoup.parse(contentAsString(result))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "render the Property Lived In page" in {
        doc.title() shouldEqual s"Error: ${messages.title} - Calculate your Non-Resident Capital Gains Tax - GOV.UK"
      }
    }
  }
}
