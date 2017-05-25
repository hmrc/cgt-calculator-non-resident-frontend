/*
 * Copyright 2017 HM Revenue & Customs
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

import assets.MessageLookup.{NonResident => commonMessages}
import assets.MessageLookup.NonResident.{CustomerType => messages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.nonresident.CustomerTypeKeys
import connectors.CalculatorConnector
import controllers.CustomerTypeController
import controllers.helpers.FakeRequestHelper
import uk.gov.hmrc.http.cache.client.CacheMap
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.jsoup._
import org.scalatest.mock.MockitoSugar

import scala.concurrent.Future
import models.{AcquisitionDateModel, CustomerTypeModel, RebasedValueModel}
import play.api.mvc.Result

class CustomerTypeActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[CustomerTypeModel], postData: Option[CustomerTypeModel],
                  acquisitionDateData: Option[AcquisitionDateModel] = None,
                  rebasedValueData: Option[RebasedValueModel] = None): CustomerTypeController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[CustomerTypeModel](ArgumentMatchers.anyString())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionDateModel]
      (ArgumentMatchers.eq(KeystoreKeys.acquisitionDate))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(acquisitionDateData))

    when(mockCalcConnector.fetchAndGetFormData[RebasedValueModel](
      ArgumentMatchers.eq(KeystoreKeys.rebasedValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(rebasedValueData))

    when(mockCalcConnector.saveFormData[CustomerTypeModel](
      ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new CustomerTypeController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  // GET Tests
  "Calling the CalculationController.customerType" when {

    lazy val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/non-resident/customer-type").withSession(SessionKeys.sessionId -> "12345")

    "not supplied with a pre-existing stored model" should {

      val target = setupTarget(None, None)
      lazy val result = target.customerType(fakeRequest)

      "return a 200" in {
        status(result) shouldBe 200
      }
    }

    "supplied with a pre-existing stored model" should {

      val target = setupTarget(Some(CustomerTypeModel(CustomerTypeKeys.individual)), None)
      lazy val result = target.customerType(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "have a back link with the correct back link text" in {
        document.body.getElementById("back-link").text shouldBe commonMessages.back
      }

      "have the radio option `individual` selected by default" in {
        document.body.getElementById("customerType-individual").parent.classNames().contains("selected") shouldBe true
      }
    }
  }

  "Have a dynamic back link generated by .customerType" when {
    lazy val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/non-resident/customer-type").withSession(SessionKeys.sessionId -> "12345")

    "there is no rebased value" should {
      val target = setupTarget(None, None, Some(AcquisitionDateModel(1, 1, 2016)), Some(RebasedValueModel(None)))
      lazy val result = target.customerType(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "have a back link that links to improvements" in {
        document.getElementById("back-link").attr("href") shouldBe controllers.routes.ImprovementsController.improvements().url
      }
    }

    "there is an acquisition date and a rebased value" should {
      val target = setupTarget(None, None, Some(AcquisitionDateModel(1, 1, 2016)), Some(RebasedValueModel(Some(1))))
      lazy val result = target.customerType(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "have a back link that links to PRR" in {
        document.getElementById("back-link").attr("href") shouldBe controllers.routes.
          PrivateResidenceReliefController.privateResidenceRelief().url
      }
    }
  }

  // POST Tests
  "In CalculationController calling the .submitCustomerType action" when {

    def buildRequest(body: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest("POST",
      "/calculate-your-capital-gains/non-resident/customer-type")
      .withSession(SessionKeys.sessionId -> "12345")
      .withFormUrlEncodedBody(body: _*)

    def executeTargetWithMockData(data: String): Future[Result] = {
      lazy val fakeRequest = buildRequest(("customerType", data))
      val mockData = new CustomerTypeModel(data)
      val target = setupTarget(None, Some(mockData))
      target.submitCustomerType(fakeRequest)
    }

    "submitting a valid form with 'individual'" should {

      lazy val result = executeTargetWithMockData(CustomerTypeKeys.individual)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the current income page" in {
        redirectLocation(result) shouldEqual Some("/calculate-your-capital-gains/non-resident/current-income")
      }
    }

    "submitting a valid form with 'trustee'" should {

      lazy val result = executeTargetWithMockData(CustomerTypeKeys.trustee)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the current income page" in {
        redirectLocation(result) shouldEqual Some("/calculate-your-capital-gains/non-resident/disabled-trustee")
      }
    }

    "submitting a valid form with 'personalRep'" should {

      lazy val result = executeTargetWithMockData(CustomerTypeKeys.personalRep)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the current income page" in {
        redirectLocation(result) shouldEqual Some("/calculate-your-capital-gains/non-resident/other-properties")
      }
    }

    "submitting an invalid form with no content" should {

      lazy val result = executeTargetWithMockData("")
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "raise an error on the page" in {
        document.body.select("#customerType-error-summary").size shouldBe 1
      }

      "render the customer type page" in {
        document.title shouldEqual messages.question
      }
    }

    "submitting an invalid form with incorrect content" should {

      lazy val result = executeTargetWithMockData("invalid-user")
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "raise an error on the page" in {
        document.body.select("#customerType-error-summary").size shouldBe 1
      }

      "render the customer type page" in {
        document.title shouldEqual messages.question
      }
    }
  }
}
