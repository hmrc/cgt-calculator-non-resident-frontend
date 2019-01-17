/*
 * Copyright 2019 HM Revenue & Customs
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
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import connectors.CalculatorConnector
import controllers.PersonalAllowanceController
import controllers.helpers.FakeRequestHelper
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.jsoup._
import org.scalatest.mock.MockitoSugar

import scala.concurrent.Future
import controllers.routes
import models.{DateModel, PersonalAllowanceModel, TaxYearModel}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.HeaderCarrier

class PersonalAllowanceActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[PersonalAllowanceModel]): PersonalAllowanceController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[PersonalAllowanceModel](
      ArgumentMatchers.eq(KeystoreKeys.personalAllowance))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[DateModel](
      ArgumentMatchers.eq(KeystoreKeys.disposalDate))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(DateModel(6, 5, 2016))))

    when(mockCalcConnector.getTaxYear(ArgumentMatchers.anyString())(ArgumentMatchers.any()))
      .thenReturn(Some(TaxYearModel("2016-5-6", isValidYear = true, "2016/17")))

    when(mockCalcConnector.getPA(ArgumentMatchers.anyInt(), ArgumentMatchers.anyBoolean())(ArgumentMatchers.any()))
      .thenReturn(Some(BigDecimal(11000)))

    when(mockCalcConnector.saveFormData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map.empty)))

    new PersonalAllowanceController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
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
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"have the title ${messages.question}" in {
        document.title shouldEqual messages.question
      }
    }

    "supplied with a pre-existing model with a session" should {

      lazy val target = setupTarget(Some(PersonalAllowanceModel(10000)))
      lazy val result = target.personalAllowance(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"have the title ${messages.question}" in {
        document.title shouldEqual messages.question
      }
    }
  }

  // POST Tests
  "In PersonalAllowanceController calling the .submitPersonalAllowance action" when {

    "submitting a valid form with '1000'" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("personalAllowance", "10000"))
      lazy val result = target.submitPersonalAllowance(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.OtherPropertiesController.otherProperties()}" in {
        redirectLocation(result) shouldBe Some(s"${routes.OtherPropertiesController.otherProperties()}")
      }
    }


    "submitting an invalid form with no value" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("personalAllowance", "-12139"))
      lazy val result = target.submitPersonalAllowance(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      s"return to the personal allowance page" in {
        document.title shouldEqual messages.question
      }
    }
  }
}
