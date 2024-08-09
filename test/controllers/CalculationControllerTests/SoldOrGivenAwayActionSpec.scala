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

import assets.MessageLookup.NonResident.{SoldOrGivenAway => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import constructors.AnswersConstructor
import controllers.SoldOrGivenAwayController
import controllers.helpers.FakeRequestHelper
import models.SoldOrGivenAwayModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.soldOrGivenAway

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SoldOrGivenAwayActionSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockHttp: DefaultHttpClient =mock[DefaultHttpClient]
  val mockCalcConnector: CalculatorConnector =mock[CalculatorConnector]
  val mockAnswersConstructor: AnswersConstructor = mock[AnswersConstructor]
  val mockMessagesControllerComponents: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val soldOrGivenAwayView: soldOrGivenAway = fakeApplication.injector.instanceOf[soldOrGivenAway]
  lazy val pageTitle = s"""${messages.question} - ${commonMessages.serviceName} - GOV.UK"""
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]

  class Setup {
    val controller = new SoldOrGivenAwayController(
      mockHttp,
      mockSessionCacheService,
      mockMessagesControllerComponents,
      soldOrGivenAwayView
    )
  }

  def setUpTarget(getData: Option[SoldOrGivenAwayModel]): SoldOrGivenAwayController = {

    when(mockSessionCacheService.fetchAndGetFormData[SoldOrGivenAwayModel](
      ArgumentMatchers.eq(KeystoreKeys.soldOrGivenAway))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheService.saveFormData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(("", "")))

    new SoldOrGivenAwayController(mockHttp, mockSessionCacheService, mockMessagesControllerComponents, soldOrGivenAwayView)
  }

  //GET Tests
  "Calling the SellOrGiveAway .sellOrGiveAway" when {

    "not supplied with a pre-existing model" should {
      val target = setUpTarget(None)
      lazy val result = target.soldOrGivenAway(fakeRequestWithSession)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a 200 response" in {
        status(result) shouldBe 200
      }

      s"have the title of $pageTitle" in {
        document.title() shouldBe pageTitle
      }
    }

    "supplied with a pre-existing model" should {
      val target = setUpTarget(Some(SoldOrGivenAwayModel(true)))
      lazy val result = target.soldOrGivenAway(fakeRequestWithSession)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a 200 response" in {
        status(result) shouldBe 200
      }

      s"have the title of $pageTitle" in {
        document.title() shouldBe pageTitle
      }
    }

    "not supplied with a valid session" should {
      val target = setUpTarget(Some(SoldOrGivenAwayModel(true)))
      lazy val result = target.soldOrGivenAway(fakeRequest)

      "return a 303 response" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  //POST Tests
  "Calling the SoldOrGivenAway .submitSoldOrGivenAway" when {

    "a valid form is submitted with Yes" should {
      val target = setUpTarget(None)
      lazy val request = fakeRequestToPOSTWithSession("soldIt" -> "Yes").withMethod("POST")
      lazy val result = target.submitSoldOrGivenAway(request)

      "return a 303 response" in {
        status(result) shouldBe 303
      }

      "redirect to the Sold For Less Page" in{
        redirectLocation(result).get shouldBe controllers.routes.SoldForLessController.soldForLess.url
      }
    }

    "a valid form is submitted with No" should {
      val target = setUpTarget(None)
      lazy val request = fakeRequestToPOSTWithSession("soldIt" -> "No").withMethod("POST")
      lazy val result = target.submitSoldOrGivenAway(request)

      "return a 303 response" in {
        status(result) shouldBe 303
      }

      "redirect to the Who Did You Give This To Page" in{
        redirectLocation(result).get shouldBe controllers.routes.WhoDidYouGiveItToController.whoDidYouGiveItTo.url
      }
    }

    "an invalid form is submitted with no data" should {
      val target = setUpTarget(None)
      lazy val request = fakeRequestToPOSTWithSession("soldIt" -> "")
      lazy val result = target.submitSoldOrGivenAway(request)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a 400 response" in {
        status(result) shouldBe 400
      }

      "stay on the SoldOrGivenAway page" in {
        document.title shouldBe s"Error: $pageTitle"
      }
    }
  }
}
