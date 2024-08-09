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

import assets.MessageLookup.NonResident.{NoCapitalGainsTax => messages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.NoCapitalGainsTaxController
import controllers.helpers.FakeRequestHelper
import models.DateModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.noCapitalGainsTax

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NoCapitalGainsTaxActionSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]

  val mockHttp: DefaultHttpClient =mock[DefaultHttpClient]
  val mockCalcConnector: CalculatorConnector =mock[CalculatorConnector]
  val mockMessagesControllerComponents: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val noCapitalGainsTaxView: noCapitalGainsTax = fakeApplication.injector.instanceOf[noCapitalGainsTax]
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  class Setup {
    val controller = new NoCapitalGainsTaxController(
      mockHttp,
      mockSessionCacheService,
      mockMessagesControllerComponents,
      noCapitalGainsTaxView
    )
  }

  def setupTarget(getData: Option[DateModel]): NoCapitalGainsTaxController = {

    when(mockSessionCacheService.fetchAndGetFormData[DateModel](
      ArgumentMatchers.eq(KeystoreKeys.disposalDate))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    new NoCapitalGainsTaxController(mockHttp, mockSessionCacheService, mockMessagesControllerComponents, noCapitalGainsTaxView)
  }

  //GET Tests
  "In CalculationController calling the .noCapitalGainsTax action " when {

    "called with a valid session" should {
      val target = setupTarget(Some(DateModel(1, 1, 2015)))
      lazy val result = target.noCapitalGainsTax(fakeRequestWithSession)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the No Capital Gains Tax page" in {
        document.title() shouldBe s"${messages.title} - Calculate your Non-Resident Capital Gains Tax - GOV.UK"
      }
    }

    "called with an invalid session" should {
      val target = setupTarget(Some(DateModel(2, 4, 2013)))
      lazy val result = target.noCapitalGainsTax(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }
}
