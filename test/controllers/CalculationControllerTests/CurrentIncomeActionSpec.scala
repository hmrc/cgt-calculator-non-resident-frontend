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
import assets.MessageLookup.NonResident.{CurrentIncome => messages}
import config.ApplicationConfig
import connectors.CalculatorConnector
import constructors.DefaultCalculationElectionConstructor
import controllers.{AcquisitionCostsController, CurrentIncomeController, routes}
import controllers.helpers.FakeRequestHelper
import models.{CurrentIncomeModel, PropertyLivedInModel}
import org.jsoup.Jsoup
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

class CurrentIncomeActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))

  val materializer = mock[Materializer]
  val mockHttp =mock[DefaultHttpClient]
  val mockCalcConnector =mock[CalculatorConnector]
  val defaultCache = mock[CacheMap]
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]

  class Setup {
    val controller = new CurrentIncomeController(
      mockHttp,
      mockCalcConnector,
      mockMessagesControllerComponents
    )(mockConfig, fakeApplication)
  }

  def setupTarget(getData: Option[CurrentIncomeModel], getPropertyLivedIn: Option[PropertyLivedInModel] = None): CurrentIncomeController = {

    when(mockCalcConnector.fetchAndGetFormData[CurrentIncomeModel](ArgumentMatchers.anyString())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[PropertyLivedInModel](ArgumentMatchers.anyString())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getPropertyLivedIn))

    when(mockCalcConnector.saveFormData[CurrentIncomeModel](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(mock[CacheMap])

    new CurrentIncomeController(mockHttp, mockCalcConnector, mockMessagesControllerComponents)(mockConfig, fakeApplication) {
      val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  //GET Tests
  "Calling the .currentIncome action " when {

    "not supplied with a pre-existing stored model" should {

      val target = setupTarget(None)
      lazy val result = target.currentIncome(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the current income page" in {
        document.title shouldBe messages.question
      }
    }

    "supplied with a pre-existing stored model" should {

      val target = setupTarget(Some(CurrentIncomeModel(1000)))
      lazy val result = target.currentIncome(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the current income page" in {
        document.title shouldBe messages.question
      }
    }

    "without a valid session" should {
      val target = setupTarget(None)
      lazy val result = target.currentIncome(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  //POST Tests
  "In CalculationController calling the .submitCurrentIncome action " when {

    "submitting a valid form" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("currentIncome", "1000"))
      lazy val result = target.submitCurrentIncome(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.PersonalAllowanceController.personalAllowance()}" in {
        redirectLocation(result) shouldBe Some(s"${routes.PersonalAllowanceController.personalAllowance()}")
      }
    }

    "submitting a valid form with a £0 amount" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("currentIncome", "0"))
      lazy val result = target.submitCurrentIncome(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.OtherPropertiesController.otherProperties()}" in {
        redirectLocation(result) shouldBe Some(s"${routes.OtherPropertiesController.otherProperties()}")
      }
    }

    "submitting an invalid form with negative data" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("currentIncome", "-10"))
      lazy val result = target.submitCurrentIncome(request)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "return to the current income page" in {
        document.title shouldBe messages.question
      }
    }
  }
}
