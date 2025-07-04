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

import assets.MessageLookup.NonResident.{CurrentIncome => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.{CurrentIncomeController, routes}
import models.{CurrentIncomeModel, PropertyLivedInModel}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.currentIncome

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CurrentIncomeActionSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockHttp: DefaultHttpClient =mock[DefaultHttpClient]
  val mockCalcConnector: CalculatorConnector =mock[CalculatorConnector]
  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val currentIncomeView: currentIncome = fakeApplication.injector.instanceOf[currentIncome]
  val pageTitle: String = s"${messages.question} - ${commonMessages.serviceName} - GOV.UK"
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  class Setup {
    val controller = new CurrentIncomeController(
      mockSessionCacheService,
      mockMessagesControllerComponents,
      currentIncomeView
    )
  }

  def setupTarget(getData: Option[CurrentIncomeModel], getPropertyLivedIn: Option[PropertyLivedInModel] = None): CurrentIncomeController = {

    when(mockSessionCacheService.fetchAndGetFormData[CurrentIncomeModel](ArgumentMatchers.anyString())(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheService.fetchAndGetFormData[PropertyLivedInModel](ArgumentMatchers.anyString())(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getPropertyLivedIn))

    when(mockSessionCacheService.saveFormData[CurrentIncomeModel](ArgumentMatchers.any(), ArgumentMatchers.any())(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful("", ""))

    new CurrentIncomeController(mockSessionCacheService, mockMessagesControllerComponents, currentIncomeView)
  }

  //GET Tests
  "Calling the .currentIncome action " when {

    "not supplied with a pre-existing stored model" should {

      val target = setupTarget(None)
      lazy val result = target.currentIncome(fakeRequestWithSession)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the current income page" in {
        document.title shouldBe pageTitle
      }
    }

    "supplied with a pre-existing stored model" should {

      val target = setupTarget(Some(CurrentIncomeModel(1000)))
      lazy val result = target.currentIncome(fakeRequestWithSession)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the current income page" in {
        document.title shouldBe pageTitle
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
      lazy val request = fakeRequestToPOSTWithSession(("currentIncome", "1000")).withMethod("POST")
      lazy val result = target.submitCurrentIncome(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.PersonalAllowanceController.personalAllowance}" in {
        redirectLocation(result) shouldBe Some(s"${routes.PersonalAllowanceController.personalAllowance}")
      }
    }

    "submitting a valid form with a £0 amount" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("currentIncome", "0")).withMethod("POST")
      lazy val result = target.submitCurrentIncome(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.OtherPropertiesController.otherProperties}" in {
        redirectLocation(result) shouldBe Some(s"${routes.OtherPropertiesController.otherProperties}")
      }
    }

    "submitting an invalid form with negative data" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("currentIncome", "-10"))
      lazy val result = target.submitCurrentIncome(request)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "return to the current income page" in {
        document.title shouldBe s"Error: $pageTitle"
      }
    }
  }
}
