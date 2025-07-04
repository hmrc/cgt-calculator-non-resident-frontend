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

import assets.MessageLookup.NonResident.{AcquisitionValue => messages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.{AcquisitionValueController, routes}
import models.AcquisitionValueModel
import org.jsoup._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.acquisitionValue

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AcquisitionValueActionSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockHttp: DefaultHttpClient =mock[DefaultHttpClient]
  val mockCalcConnector: CalculatorConnector =mock[CalculatorConnector]
  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val acquisitionValueView: acquisitionValue =  fakeApplication.injector.instanceOf[acquisitionValue]
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]

  class Setup {
    val controller = new AcquisitionValueController(
      mockHttp,
      mockSessionCacheService,
      mockMessagesControllerComponents,
      acquisitionValueView
    )
  }
  def setupTarget(getData: Option[AcquisitionValueModel]): AcquisitionValueController = {

    when(mockSessionCacheService.fetchAndGetFormData[AcquisitionValueModel](
      ArgumentMatchers.eq(KeystoreKeys.acquisitionValue))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheService.saveFormData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(("", "")))

    new AcquisitionValueController(mockHttp, mockSessionCacheService, mockMessagesControllerComponents, acquisitionValueView) {
    }
  }

  // GET Tests
  "Calling the CalculationController.acquisitionValue" when {

    "not supplied with a pre-existing stored model" should {

      val target = setupTarget(None)
      lazy val result = target.acquisitionValue(fakeRequestWithSession)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"have the title of ${messages.question}" in {
        document.title() shouldBe s"${messages.question} - Calculate your Non-Resident Capital Gains Tax - GOV.UK"
      }
    }

    "supplied with a pre-existing stored model" should {

      val target = setupTarget(Some(AcquisitionValueModel(1000)))
      lazy val result = target.acquisitionValue(fakeRequestWithSession)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"have the title of ${messages.question}" in {
        document.title() shouldBe s"${messages.question} - Calculate your Non-Resident Capital Gains Tax - GOV.UK"
      }
    }

    "not supplied with a valid session" should {
      val target = setupTarget(None)
      lazy val result = target.acquisitionValue(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  // POST Tests
  "In CalculationController calling the .submitAcquisitionValue action" when {

    "submit a valid form with a valid acquisitionValue" should {
      lazy val request = fakeRequestToPOSTWithSession(("acquisitionValue", "1000")).withMethod("POST")
      lazy val target = setupTarget(None)
      lazy val result = target.submitAcquisitionValue(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.AcquisitionCostsController.acquisitionCosts}" in {
        redirectLocation(result) shouldBe Some(routes.AcquisitionCostsController.acquisitionCosts.url)
      }
    }

    "submitting an invalid form with a negative value" should {
      lazy val request = fakeRequestToPOSTWithSession(("acquisitionValue", "-1000"))
      lazy val target = setupTarget(None)
      lazy val result = target.submitAcquisitionValue(request.withMethod("POST"))
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      s"fail with message ${messages.errorNegative}" in {
        document.getElementsByClass("govuk-error-summary").text should include (messages.errorNegative)
      }
    }
  }
}
