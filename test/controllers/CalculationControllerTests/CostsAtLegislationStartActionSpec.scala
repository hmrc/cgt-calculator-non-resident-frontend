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

import assets.MessageLookup.NonResident.{CostsAtLegislationStart => messages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import connectors.CalculatorConnector
import controllers.CostsAtLegislationStartController
import controllers.helpers.FakeRequestHelper
import models.CostsAtLegislationStartModel
import org.jsoup._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

class CostsAtLegislationStartActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[CostsAtLegislationStartModel]): CostsAtLegislationStartController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[CostsAtLegislationStartModel](
      ArgumentMatchers.eq(KeystoreKeys.costAtLegislationStart))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    val successfulSave = Future.successful(CacheMap("", Map()))
    when(mockCalcConnector.saveFormData[CostsAtLegislationStartModel](
      ArgumentMatchers.eq(KeystoreKeys.costAtLegislationStart), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(successfulSave)

    new CostsAtLegislationStartController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  // GET Tests
  "calling the costsAtLegislationStart action" when {

    "a request is made without a session" should {
      val target = setupTarget(None)
      lazy val result = target.costsAtLegislationStart(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }

    "no data has already been saved" should {
      val target = setupTarget(None)
      lazy val result = target.costsAtLegislationStart(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the expected page" in {
        document.title() shouldBe messages.title
      }
    }

    "some data has already been supplied" should {
      val target = setupTarget(Some(CostsAtLegislationStartModel("Yes", Some(1500))))
      lazy val result = target.costsAtLegislationStart(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the expected page" in {
        document.title() shouldBe messages.title
      }
    }
  }

  // POST Tests
  "Calling the .submitCostsAtlLegislationStart action" when {

    "a valid form is submitted" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession("hasCosts" -> "Yes", "costs" -> "1000")
      lazy val result = target.submitCostsAtLegislationStart(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the rebased value page" in {
        redirectLocation(result).get shouldBe controllers.routes.RebasedValueController.rebasedValue().url
      }
    }

    "an invalid form is submitted" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession("hasCosts" -> "Yes", "costs" -> "")
      lazy val result = target.submitCostsAtLegislationStart(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "return to the 'costs at legislation start' page" in {
        document.title() shouldBe messages.title
      }
    }
  }
}
