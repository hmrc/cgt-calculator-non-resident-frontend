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

import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import connectors.CalculatorConnector
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.jsoup._
import org.scalatest.mock.MockitoSugar
import assets.MessageLookup.NonResident.{AcquisitionValue => messages}
import controllers.AcquisitionValueController
import controllers.helpers.FakeRequestHelper

import scala.concurrent.Future
import controllers.routes
import models.AcquisitionValueModel
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}
import uk.gov.hmrc.http.HeaderCarrier

class AcquisitionValueActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  val mockCalcConnector = mock[CalculatorConnector]

  def setupTarget(getData: Option[AcquisitionValueModel]): AcquisitionValueController = {

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionValueModel](
      ArgumentMatchers.eq(KeystoreKeys.acquisitionValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map.empty)))

    new AcquisitionValueController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  // GET Tests
  "Calling the CalculationController.acquisitionValue" when {

    "not supplied with a pre-existing stored model" should {

      val target = setupTarget(None)
      lazy val result = target.acquisitionValue(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"have the title of ${messages.question}" in {
        document.title shouldEqual messages.question
      }
    }

    "supplied with a pre-existing stored model" should {

      val target = setupTarget(Some(AcquisitionValueModel(1000)))
      lazy val result = target.acquisitionValue(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"have the title of ${messages.question}" in {
        document.title shouldEqual messages.question
      }
    }

    "not supplied with a valid session" should {
      val target = setupTarget(None)
      lazy val result = target.acquisitionValue(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  // POST Tests
  "In CalculationController calling the .submitAcquisitionValue action" when {

    "submit a valid form with a valid acquisitionValue" should {
      lazy val request = fakeRequestToPOSTWithSession(("acquisitionValue", "1000"))
      lazy val target = setupTarget(None)
      lazy val result = target.submitAcquisitionValue(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.AcquisitionCostsController.acquisitionCosts()}" in {
        redirectLocation(result) shouldBe Some(routes.AcquisitionCostsController.acquisitionCosts().url)
      }
    }

    "submitting an invalid form with a negative value" should {
      lazy val request = fakeRequestToPOSTWithSession(("acquisitionValue", "-1000"))
      lazy val target = setupTarget(None)
      lazy val result = target.submitAcquisitionValue(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      s"fail with message ${messages.errorNegative}" in {
        document.getElementsByClass("error-notification").text should include (messages.errorNegative)
      }
    }
  }
}