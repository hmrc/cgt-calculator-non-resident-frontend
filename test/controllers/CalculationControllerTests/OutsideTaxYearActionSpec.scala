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

import assets.MessageLookup.{NonResident => commonMessages, OutsideTaxYears => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.OutsideTaxYearController
import controllers.helpers.FakeRequestHelper
import models.{DateModel, TaxYearModel}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.outsideTaxYear

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class OutsideTaxYearActionSpec
  extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockHttp: DefaultHttpClient =mock[DefaultHttpClient]
  val mockCalcConnector: CalculatorConnector =mock[CalculatorConnector]
  val mockMessagesControllerComponents: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val outsideTaxYearView: outsideTaxYear = fakeApplication.injector.instanceOf[outsideTaxYear]
  lazy val pageTitle: String = s"""${messages.title} - ${commonMessages.serviceName} - GOV.UK"""
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]

  def setupTarget(disposalDateModel: Option[DateModel], taxYearModel: Option[TaxYearModel]): OutsideTaxYearController = {

    when(mockSessionCacheService.fetchAndGetFormData[DateModel](ArgumentMatchers.any())(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(disposalDateModel))

    when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(taxYearModel))

    new OutsideTaxYearController(mockHttp, mockCalcConnector, mockSessionCacheService, mockMessagesControllerComponents, outsideTaxYearView) {
    }
  }

  "Calling .outsideTaxYears from the outsideTaxYearController" when {

    "there is a valid session" should {
      lazy val target = setupTarget(Some(DateModel(10, 10, 2018)), Some(TaxYearModel("2018/19", isValidYear = false, "2018/19")))
      lazy val result = target.outsideTaxYear(fakeRequestWithSession)

      "return a 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a title of $pageTitle" in {
        Jsoup.parse(contentAsString(result)).title shouldBe pageTitle
      }

      s"have a back link to '${controllers.routes.DisposalDateController.disposalDate.url}'" in {
        Jsoup.parse(contentAsString(result)).getElementsByClass("govuk-back-link").attr("href") shouldBe "#"
      }
    }

    "there is no valid session" should {
      lazy val target = setupTarget(Some(DateModel(10, 10, 2018)), Some(TaxYearModel("2018/19", isValidYear = false, "2018/19")))
      lazy val result = target.outsideTaxYear(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }
}
