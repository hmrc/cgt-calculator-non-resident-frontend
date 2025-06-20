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

import assets.MessageLookup.NonResident.{CostsAtLegislationStart => messages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.CostsAtLegislationStartController
import controllers.helpers.FakeRequestHelper
import models.CostsAtLegislationStartModel
import org.jsoup._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.costsAtLegislationStart

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CostsAtLegislationStartActionSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockHttp: DefaultHttpClient =mock[DefaultHttpClient]
  val mockCalcConnector: CalculatorConnector =mock[CalculatorConnector]
  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val costsAtLegislationStartView: costsAtLegislationStart = fakeApplication.injector.instanceOf[costsAtLegislationStart]
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  class Setup {
    val controller = new CostsAtLegislationStartController(
      mockHttp,
      mockSessionCacheService,
      mockMessagesControllerComponents,
      costsAtLegislationStartView
    )
  }

  def setupTarget(getData: Option[CostsAtLegislationStartModel]): CostsAtLegislationStartController = {

    when(mockSessionCacheService.fetchAndGetFormData[CostsAtLegislationStartModel](
      ArgumentMatchers.eq(KeystoreKeys.costAtLegislationStart))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    val successfulSave = Future.successful(("", ""))
    when(mockSessionCacheService.saveFormData[CostsAtLegislationStartModel](
      ArgumentMatchers.eq(KeystoreKeys.costAtLegislationStart), ArgumentMatchers.any())(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(successfulSave)

    new CostsAtLegislationStartController(mockHttp, mockSessionCacheService, mockMessagesControllerComponents, costsAtLegislationStartView)
  }

  // GET Tests
  "calling the costsAtLegislationStart action" when {

    "a request is made without a session" should {
      val target = setupTarget(None)
      lazy val result = target.costsAtLegislationStart(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }

    "no data has already been saved" should {
      val target = setupTarget(None)
      lazy val result = target.costsAtLegislationStart(fakeRequestWithSession)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the expected page" in {
        document.title() shouldBe messages.title
      }
    }

    "some data has already been supplied" should {
      val target = setupTarget(Some(CostsAtLegislationStartModel("Yes", Some(1500))))
      lazy val result = target.costsAtLegislationStart(fakeRequestWithSession)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the expected page" in {
        document.title() shouldBe messages.title
      }
    }
  }

  // POST Tests
  "Calling the .submitCostsAtlLegislationStart action" when {

    "a valid form is submitted" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession("hasCosts" -> "Yes", "costs" -> "1000").withMethod("POST")
      lazy val result = target.submitCostsAtLegislationStart(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the rebased value page" in {
        redirectLocation(result).get shouldBe controllers.routes.RebasedValueController.rebasedValue.url
      }
    }

    "an invalid form is submitted" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession("hasCosts" -> "Yes", "costs" -> "")
      lazy val result = target.submitCostsAtLegislationStart(request)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "return to the 'costs at legislation start' page" in {
        document.title() shouldBe s"Error: ${messages.title}"
      }
    }
  }
}
