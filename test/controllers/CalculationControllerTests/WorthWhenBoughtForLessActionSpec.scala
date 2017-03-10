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

import assets.MessageLookup.NonResident.{WorthWhenBoughtForLess => messages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import connectors.CalculatorConnector
import controllers.WorthWhenBoughtForLessController
import controllers.helpers.FakeRequestHelper
import models.AcquisitionValueModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class WorthWhenBoughtForLessActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  def setupTarget(getData: Option[AcquisitionValueModel]): WorthWhenBoughtForLessController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionValueModel](
      ArgumentMatchers.eq(KeystoreKeys.acquisitionMarketValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData[AcquisitionValueModel](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new WorthWhenBoughtForLessController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "WorthWhenBoughtForLessController" should {
    s"have a session timeout home link of '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
      WorthWhenBoughtForLessController.homeLink shouldEqual controllers.routes.DisposalDateController.disposalDate().url
    }
  }

  "Calling .worthWhenBoughtForLess" when {

    "request has a valid session and no keystore value" should {

      lazy val target = setupTarget(None)
      lazy val result = target.worthWhenBoughtForLess(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.question}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.question
      }
    }

    "request has a valid session and some keystore value" should {

      lazy val target = setupTarget(Some(AcquisitionValueModel(BigDecimal(1000.00))))
      lazy val result = target.worthWhenBoughtForLess(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.question}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.question
      }
    }

    "request has an invalid session" should {

      lazy val target = setupTarget(None)
      lazy val result = target.worthWhenBoughtForLess(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling .submitWorthWhenBoughtForLess" should {

    "with valid form with the answer '1000.00'" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("acquisitionMarketValue", "1000.00"))
      lazy val result = target.submitWorthWhenBoughtForLess(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the acquisition costs page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/non-resident/acquisition-costs")
      }
    }

    "with an invalid form with the answer 'a'" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("acquisitionMarketValue", "a"))
      lazy val result = target.submitWorthWhenBoughtForLess(request)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "return to the Worth When Bought For Less page" in {
        doc.title() shouldEqual messages.question
      }
    }
  }
}
