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

import assets.MessageLookup.NonResident.{DisposalDate => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.{DisposalDateController, routes}
import models.{DateModel, TaxYearModel}
import org.jsoup._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.disposalDate

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DisposalDateActionSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockHttp: DefaultHttpClient =mock[DefaultHttpClient]
  val mockCalcConnector: CalculatorConnector =mock[CalculatorConnector]
  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val disposalDateView: disposalDate = fakeApplication.injector.instanceOf[disposalDate]
  val pageTitle: String = s"""${messages.question} - ${commonMessages.serviceName} - GOV.UK"""
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]

  class Setup {
    val controller = new DisposalDateController(
      mockHttp,
      mockCalcConnector,
      mockSessionCacheService,
      mockMessagesControllerComponents,
      disposalDateView
    )
  }

  def setupTarget(getData: Option[DateModel], taxYearModel: Option[TaxYearModel] = None): DisposalDateController = {

    when(mockSessionCacheService.fetchAndGetFormData[DateModel](ArgumentMatchers.anyString())(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheService.saveFormData[DateModel](ArgumentMatchers.any(), ArgumentMatchers.any())(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(("", "")))

    when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(taxYearModel))

    new DisposalDateController(mockHttp, mockCalcConnector, mockSessionCacheService, mockMessagesControllerComponents, disposalDateView)
  }

  // GET Tests
  "Calling the CalculationController.disposalDate" when {

    "not supplied with a pre-existing stored model" should {

      lazy val target = setupTarget(None)
      lazy val result = target.disposalDate(fakeRequestWithSession)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "return to the disposal date page" in {
        document.title shouldEqual pageTitle
      }
    }

    "supplied with a pre-existing stored model" should {

      lazy val target = setupTarget(Some(DateModel(1, 3, 2016)))
      lazy val result = target.disposalDate(fakeRequestWithSession)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "return to the disposal date page" in {
        document.title shouldEqual pageTitle
      }
    }

    "supplied with a pre-existing stored model without a session" should {
      lazy val target = setupTarget(Some(DateModel(1, 3, 2016)))
      lazy val result = target.disposalDate(fakeRequest)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "return to the disposal date page" in {
        document.title shouldEqual pageTitle
      }
    }
  }

  // POST Tests
  "In CalculationController calling the .submitDisposalDate action" when {

    "submitting a valid date 31/01/2016" should {

      lazy val target = setupTarget(None, Some(TaxYearModel("2015/16", isValidYear = true, "2015/16")))
      lazy val request = fakeRequestToPOSTWithSession(("disposalDate.day", "31"), ("disposalDate.month", "1"), ("disposalDate.year", "2016")).withMethod("POST")
      lazy val result = target.submitDisposalDate(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.SoldOrGivenAwayController.soldOrGivenAway}" in {
        redirectLocation(result) shouldBe Some(s"${routes.SoldOrGivenAwayController.soldOrGivenAway}")
      }
    }

    "submitting a valid leap year date 29/02/2016" should {

      lazy val target = setupTarget(None, Some(TaxYearModel("2015/16", isValidYear = true, "2015/16")))
      lazy val request = fakeRequestToPOSTWithSession(("disposalDate.day", "29"), ("disposalDate.month", "2"), ("disposalDate.year", "2016")).withMethod("POST")
      lazy val result = target.submitDisposalDate(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.SoldOrGivenAwayController.soldOrGivenAway}" in {
        redirectLocation(result) shouldBe Some(s"${routes.SoldOrGivenAwayController.soldOrGivenAway}")
      }
    }

    "submitting a valid date of 20/02/2014 before the tax start date" should {

      lazy val target = setupTarget(None, Some(TaxYearModel("2014/15", isValidYear = false, "2015/16")))
      lazy val request = fakeRequestToPOSTWithSession(("disposalDate.day", "20"), ("disposalDate.month", "2"), ("disposalDate.year", "2014")).withMethod("POST")
      lazy val result = target.submitDisposalDate(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.NoCapitalGainsTaxController.noCapitalGainsTax}" in {
        redirectLocation(result) shouldBe Some(s"${routes.NoCapitalGainsTaxController.noCapitalGainsTax}")
      }
    }

    "submitting a valid date in the future" should {
      lazy val target = setupTarget(None, Some(TaxYearModel("2020/21", isValidYear = false, "2017/18")))
      lazy val request = fakeRequestToPOSTWithSession(("disposalDate.day", "20"), ("disposalDate.month", "2"), ("disposalDate.year", "2021")).withMethod("POST")
      lazy val result = target.submitDisposalDate(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.OutsideTaxYearController.outsideTaxYear}" in {
        redirectLocation(result) shouldBe Some(s"${routes.OutsideTaxYearController.outsideTaxYear}")
      }
    }
  }
}
