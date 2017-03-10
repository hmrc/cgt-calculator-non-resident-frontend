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

import connectors.CalculatorConnector
import models.PreviousLossOrGainModel
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import config.AppConfig
import controllers.helpers.FakeRequestHelper
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers
import org.scalatest.mock.MockitoSugar
import common.KeystoreKeys.{NonResidentKeys => keystoreKeys}
import assets.MessageLookup.NonResident.{PreviousLossOrGain => messages}
import controllers.PreviousGainOrLossController
import org.jsoup.Jsoup

import scala.concurrent.Future

class PreviousGainOrLossActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {
  def setupTarget(getData: Option[PreviousLossOrGainModel]): PreviousGainOrLossController = {
    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[PreviousLossOrGainModel](
      ArgumentMatchers.eq(keystoreKeys.previousLossOrGain))(ArgumentMatchers.any(), ArgumentMatchers.any())).
      thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData[PreviousLossOrGainModel](
      ArgumentMatchers.eq(keystoreKeys.previousLossOrGain), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())).
      thenReturn(mock[CacheMap])

    new PreviousGainOrLossController {
      val calcConnector: CalculatorConnector = mockCalcConnector
      val config: AppConfig = mock[AppConfig]
    }
  }

  "Calling .previousGainOrLoss from the PreviousGainOrLossController" when {
    "there is no keystore data" should {
      lazy val target = setupTarget(None)
      lazy val result = target.previousGainOrLoss(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldEqual 200
      }

      s"return some HTML with title of ${messages.question}" in {
        Jsoup.parse(bodyOf(result)).select("h1").text shouldEqual messages.question
      }
    }

    "there is keystore data" should {
      lazy val target = setupTarget(Some(PreviousLossOrGainModel("Loss")))
      lazy val result = target.previousGainOrLoss(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldEqual 200
      }

      s"return some html with title of ${messages.question}" in {
        Jsoup.parse(bodyOf(result)).select("h1").text shouldEqual messages.question
      }
    }

    "there is no valid session" should {
      lazy val target = setupTarget(None)
      lazy val result = target.previousGainOrLoss(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling .submitPreviousGainOrLoss from the PreviousGainOrLossController" when {
    "a valid form is submitted with the value 'Loss'" should {
      lazy val target = setupTarget(None)
      lazy val result = target.submitPreviousGainOrLoss(fakeRequestToPOSTWithSession(("previousLossOrGain", "Loss")))

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the how much loss page" in {
        redirectLocation(result).get shouldBe controllers.routes.HowMuchLossController.howMuchLoss().url
      }
    }

    "a valid form is submitted with the value 'Gain'" should {
      lazy val target = setupTarget(None)
      lazy val result = target.submitPreviousGainOrLoss(fakeRequestToPOSTWithSession(("previousLossOrGain", "Gain")))

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the how much gained page" in {
        redirectLocation(result).get shouldBe controllers.routes.HowMuchGainController.howMuchGain().url
      }
    }

    "a valid form is submitted with the value 'Neither" should {
      lazy val target = setupTarget(None)
      lazy val result = target.submitPreviousGainOrLoss(fakeRequestToPOSTWithSession(("previousLossOrGain","Neither")))

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the AEA page" in {
        redirectLocation(result).get shouldBe controllers.routes.AnnualExemptAmountController.annualExemptAmount().url
      }
    }

    "an invalid form is submitted" should {
      lazy val target = setupTarget(None)
      lazy val result = target.submitPreviousGainOrLoss(fakeRequestToPOSTWithSession(("previousLossOrGain", "invalid text")))
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "return to the page" in {
        doc.title shouldEqual messages.question
      }
    }
  }
}
