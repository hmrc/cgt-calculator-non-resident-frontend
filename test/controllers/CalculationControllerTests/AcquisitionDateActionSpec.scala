/*
 * Copyright 2020 HM Revenue & Customs
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
import assets.MessageLookup.NonResident.{AcquisitionDate => messages}
import config.ApplicationConfig
import connectors.CalculatorConnector
import constructors.{AnswersConstructor, DefaultCalculationElectionConstructor}
import controllers.{AcquisitionDateController, PersonalAllowanceController}
import controllers.helpers.FakeRequestHelper
import controllers.utils.TimeoutController
import models.DateModel
import org.jsoup._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.SessionId
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

class AcquisitionDateActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))

  val materializer = mock[Materializer]
  val mockHttp =mock[DefaultHttpClient]
  val mockCalcConnector =mock[CalculatorConnector]
  val defaultCache = mock[CacheMap]
  val mockDefaultCalcElecConstructor = mock[DefaultCalculationElectionConstructor]
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]


  class Setup {
    val controller = new AcquisitionDateController(
      mockHttp,
      mockCalcConnector,
      mockDefaultCalcElecConstructor,
      mockMessagesControllerComponents
    )(mockConfig, fakeApplication)
  }

  def setupTarget(getData: Option[DateModel]
                 ): AcquisitionDateController = {

    when(mockCalcConnector.fetchAndGetFormData[DateModel](ArgumentMatchers.anyString())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map.empty)))

    new AcquisitionDateController(mockHttp, mockCalcConnector, mockDefaultCalcElecConstructor, mockMessagesControllerComponents)(mockConfig, fakeApplication) {
      val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  val controller = new TimeoutController(mockMessagesControllerComponents)(mockConfig, fakeApplication)


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
      lazy val document = Jsoup.parse(bodyOf(result)(materializer))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "should render the acquisition date page" in {
        document.title shouldEqual messages.question
      }
    }

    "supplied with a model already filled with data" should {

      val testAcquisitionDateModel = new DateModel(10, 12, 2016)
      val target = setupTarget(Some(testAcquisitionDateModel))
      lazy val result = target.acquisitionDate(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"have load the acquisition date page ${messages.question}" in {
        document.title() shouldBe messages.question
      }
    }
  }

  "Calling the submitAcquisitionDate action" when {

    "supplied with a valid model" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("acquisitionDateDay", "31"),
        ("acquisitionDateMonth", "03"), ("acquisitionDateYear", "2016"))
      lazy val result = target.submitAcquisitionDate(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${controllers.routes.HowBecameOwnerController.howBecameOwner().url}" in {
        redirectLocation(result).get shouldBe controllers.routes.HowBecameOwnerController.howBecameOwner().url
      }
    }

    "supplied with a valid model with date before the legislation start" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("acquisitionDateDay", "31"),
        ("acquisitionDateMonth", "03"), ("acquisitionDateYear", "1982"))
      lazy val result = target.submitAcquisitionDate(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${controllers.routes.WorthBeforeLegislationStartController.worthBeforeLegislationStart().url}" in {
        redirectLocation(result).get shouldBe controllers.routes.WorthBeforeLegislationStartController.worthBeforeLegislationStart().url
      }
    }

    "supplied with a valid model with date on the legislation start" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("acquisitionDateDay", "01"),
        ("acquisitionDateMonth", "04"), ("acquisitionDateYear", "1982"))
      lazy val result = target.submitAcquisitionDate(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${controllers.routes.HowBecameOwnerController.howBecameOwner().url}" in {
        redirectLocation(result).get shouldBe controllers.routes.HowBecameOwnerController.howBecameOwner().url
      }
    }
  }
}
