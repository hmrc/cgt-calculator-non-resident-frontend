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
import assets.MessageLookup.NonResident.{AcquisitionDate => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import constructors.DefaultCalculationElectionConstructor
import controllers.AcquisitionDateController
import controllers.helpers.FakeRequestHelper
import controllers.utils.TimeoutController
import models.DateModel
import org.jsoup._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.acquisitionDate
import views.html.warnings.sessionTimeout

import scala.concurrent.{ExecutionContext, Future}

class AcquisitionDateActionSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))
  val ec = fakeApplication.injector.instanceOf[ExecutionContext]
  val materializer = mock[Materializer]
  val mockHttp =mock[DefaultHttpClient]
  val mockCalcConnector =mock[CalculatorConnector]
  val mockDefaultCalcElecConstructor = mock[DefaultCalculationElectionConstructor]
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val acquisitionCostsView = fakeApplication.injector.instanceOf[acquisitionDate]
  val sessionTimeoutView = fakeApplication.injector.instanceOf[sessionTimeout]
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]

  class Setup {
    val controller = new AcquisitionDateController(
      mockHttp,
      mockSessionCacheService,
      mockDefaultCalcElecConstructor,
      mockMessagesControllerComponents,
      acquisitionCostsView
    )(ec)
  }

  def setupTarget(getData: Option[DateModel]
                 ): AcquisitionDateController = {

    when(mockSessionCacheService.fetchAndGetFormData[DateModel](ArgumentMatchers.anyString())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheService.saveFormData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(("", "")))

    new AcquisitionDateController(mockHttp, mockSessionCacheService, mockDefaultCalcElecConstructor, mockMessagesControllerComponents, acquisitionCostsView)(ec) {
    }
  }

  val controller = new TimeoutController(mockMessagesControllerComponents, sessionTimeoutView)


  "Calling the .acquisitionDate action " should {

    "no session is active" should {
      lazy val target = setupTarget(None)
      lazy val result = target.acquisitionDate(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${controller.timeout()}" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }

    "not supplied with a pre-existing model" should {

      val target = setupTarget(None)
      lazy val result = target.acquisitionDate(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "should render the acquisition date page" in {
        document.title shouldEqual s"${messages.question} - Calculate your Non-Resident Capital Gains Tax - GOV.UK"
      }
    }

    "supplied with a model already filled with data" should {

      val testAcquisitionDateModel = new DateModel(10, 12, 2016)
      val target = setupTarget(Some(testAcquisitionDateModel))
      lazy val result = target.acquisitionDate(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"have load the acquisition date page ${messages.question}" in {
        document.title() shouldBe s"${messages.question} - Calculate your Non-Resident Capital Gains Tax - GOV.UK"
      }
    }
  }

  "Calling the submitAcquisitionDate action" when {

    "supplied with a valid model" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("acquisitionDate.day", "31"),
        ("acquisitionDate.month", "03"), ("acquisitionDate.year", "2016")).withMethod("POST")
      lazy val result = target.submitAcquisitionDate(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${controllers.routes.HowBecameOwnerController.howBecameOwner.url}" in {
        redirectLocation(result).get shouldBe controllers.routes.HowBecameOwnerController.howBecameOwner.url
      }
    }

    "supplied with a valid model with date before the legislation start" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("acquisitionDate.day", "31"),
        ("acquisitionDate.month", "03"), ("acquisitionDate.year", "1982")).withMethod("POST")
      lazy val result = target.submitAcquisitionDate(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${controllers.routes.WorthBeforeLegislationStartController.worthBeforeLegislationStart.url}" in {
        redirectLocation(result).get shouldBe controllers.routes.WorthBeforeLegislationStartController.worthBeforeLegislationStart.url
      }
    }

    "supplied with a valid model with date on the legislation start" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("acquisitionDate.day", "01"),
        ("acquisitionDate.month", "04"), ("acquisitionDate.year", "1982")).withMethod("POST")
      lazy val result = target.submitAcquisitionDate(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${controllers.routes.HowBecameOwnerController.howBecameOwner.url}" in {
        redirectLocation(result).get shouldBe controllers.routes.HowBecameOwnerController.howBecameOwner.url
      }
    }
  }
}
