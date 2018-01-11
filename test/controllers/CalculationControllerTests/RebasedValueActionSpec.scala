/*
 * Copyright 2018 HM Revenue & Customs
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

import assets.MessageLookup.NonResident.{RebasedValue => messages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import connectors.CalculatorConnector
import controllers.RebasedValueController
import controllers.helpers.FakeRequestHelper
import controllers.routes
import models.{AcquisitionDateModel, RebasedValueModel}
import org.jsoup._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

class RebasedValueActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[RebasedValueModel],
                  acquisitionDateModel: Option[AcquisitionDateModel] = Some(AcquisitionDateModel(10, 10, 2015))): RebasedValueController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[RebasedValueModel](
      ArgumentMatchers.eq(KeystoreKeys.rebasedValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionDateModel](
      ArgumentMatchers.eq(KeystoreKeys.acquisitionDate))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(acquisitionDateModel))

    when(mockCalcConnector.saveFormData[RebasedValueModel](
      ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new RebasedValueController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  //GET tests
  "In CalculationController calling the .rebasedValue action " when {

    "no session is active" should {

      val target = setupTarget(None)
      lazy val result = target.rebasedValue(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }

    "not supplied with a pre-existing stored model" should {

      val target = setupTarget(None)
      lazy val result = target.rebasedValue(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"route to the rebased value view with the question ${messages.question}" in {
        document.title shouldEqual messages.question
      }
    }

    "supplied with a pre-existing stored model" should {
      val target = setupTarget(Some(RebasedValueModel(1000)))
      lazy val result = target.rebasedValue(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"route to the rebased value view with the question ${messages.question}" in {
        document.title shouldEqual messages.question
      }
    }
  }
  //POST Tests
  "In CalculationController calling the .submitRebasedValue action" when {

    lazy val target = setupTarget(None)

    "submitting a valid form with amount of 100" should {

      lazy val request = fakeRequestToPOSTWithSession(("rebasedValueAmt", "100"))
      lazy val result = target.submitRebasedValue(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the rebased costs page" in {
        redirectLocation(result).get shouldEqual routes.RebasedCostsController.rebasedCosts().url
      }
    }

    "submitting an invalid form" should {

      lazy val request = fakeRequestToPOSTWithSession(("rebasedValueAmt", ""))
      lazy val result = target.submitRebasedValue(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "return to the rebased value page" in {
        document.title shouldEqual messages.question
      }
    }
  }

  "Calling .backLink in the CalculationController" should {
    s"return the URL ${controllers.routes.AcquisitionCostsController.acquisitionCosts().url}" when {
      "supplied with an acquisitionDate post-legislation start" in {
        lazy val result = RebasedValueController.backLink(Some(AcquisitionDateModel(10, 10, 2015)))
        result shouldEqual controllers.routes.AcquisitionCostsController.acquisitionCosts().url
      }

      s"return the URL ${controllers.routes.CostsAtLegislationStartController.costsAtLegislationStart().url}" when {
        "supplied with an acquisitionDate pre-legislation start" in {
          lazy val result = RebasedValueController.backLink(Some(AcquisitionDateModel(10, 10, 1970)))
          result shouldEqual controllers.routes.CostsAtLegislationStartController.costsAtLegislationStart().url
        }
      }
    }
  }
}
