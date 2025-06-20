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

import assets.MessageLookup.NonResident.{PersonalAllowance => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.{PersonalAllowanceController, routes}
import models.{DateModel, PersonalAllowanceModel, TaxYearModel}
import org.jsoup._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.personalAllowance

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PersonalAllowanceActionSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockHttp: DefaultHttpClient =mock[DefaultHttpClient]
  val mockCalcConnector: CalculatorConnector =mock[CalculatorConnector]
  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val personalAllowanceView: personalAllowance = fakeApplication.injector.instanceOf[personalAllowance]
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  lazy val pageTitle: String = s"""${messages.question} - ${commonMessages.serviceName} - GOV.UK"""

  class Setup {
    val controller = new PersonalAllowanceController(
      mockHttp,
      mockCalcConnector,
      mockSessionCacheService,
      mockMessagesControllerComponents,
      personalAllowanceView
    )
  }


  def setupTarget(getData: Option[PersonalAllowanceModel]): PersonalAllowanceController = {

    when(mockSessionCacheService.fetchAndGetFormData[PersonalAllowanceModel](
      ArgumentMatchers.eq(KeystoreKeys.personalAllowance))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheService.fetchAndGetFormData[DateModel](
      ArgumentMatchers.eq(KeystoreKeys.disposalDate))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(DateModel(6, 5, 2016))))

    when(mockCalcConnector.getTaxYear(ArgumentMatchers.anyString())(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(TaxYearModel("2016-5-6", isValidYear = true, "2016/17"))))

    when(mockCalcConnector.getPA(ArgumentMatchers.anyInt(), ArgumentMatchers.anyBoolean())(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(BigDecimal(11000))))

    when(mockSessionCacheService.saveFormData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(("", "")))

    new PersonalAllowanceController(mockHttp, mockCalcConnector, mockSessionCacheService, mockMessagesControllerComponents, personalAllowanceView)
  }

  // GET Tests
  "Calling the PersonalAllowanceController.personalAllowance" when {

    "called with no session" should {
      lazy val target = setupTarget(None)
      lazy val result = target.personalAllowance(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to /calculate-your-capital-gains/non-resident/session-timeout" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }

    "not supplied with a pre-existing stored model with a session" should {

      lazy val target = setupTarget(None)
      lazy val result = target.personalAllowance(fakeRequestWithSession)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"have the title $pageTitle" in {
        document.title shouldEqual pageTitle
      }
    }

    "supplied with a pre-existing model with a session" should {

      lazy val target = setupTarget(Some(PersonalAllowanceModel(10000)))
      lazy val result = target.personalAllowance(fakeRequestWithSession)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"have the title $pageTitle" in {
        document.title shouldEqual pageTitle
      }
    }
  }

  // POST Tests
  "In PersonalAllowanceController calling the .submitPersonalAllowance action" when {

    "submitting a valid form with '1000'" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("personalAllowance", "10000")).withMethod("POST")
      lazy val result = target.submitPersonalAllowance(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.OtherPropertiesController.otherProperties}" in {
        redirectLocation(result) shouldBe Some(s"${routes.OtherPropertiesController.otherProperties}")
      }
    }


    "submitting an invalid form with no value" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("personalAllowance", "-12139"))
      lazy val result = target.submitPersonalAllowance(request)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      s"return to the personal allowance page" in {
        document.title shouldEqual s"Error: $pageTitle"
      }
    }
  }
}
