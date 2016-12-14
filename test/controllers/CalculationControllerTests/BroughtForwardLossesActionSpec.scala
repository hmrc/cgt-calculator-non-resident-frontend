/*
 * Copyright 2016 HM Revenue & Customs
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
import controllers.helpers.FakeRequestHelper
import models._
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.mockito.Matchers
import org.mockito.Mockito._
import assets.MessageLookup.{NonResident => messages}
import controllers.BroughtForwardLossesController
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class BroughtForwardLossesActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = mock[HeaderCarrier]

  def setupTarget(getData: Option[BroughtForwardLossesModel],
                  otherPropertiesModel: Option[OtherPropertiesModel] = None,
                  previousLossOrGainModel: Option[PreviousLossOrGainModel] = None,
                  howMuchGainModel: Option[HowMuchGainModel] = None,
                  howMuchLossModel: Option[HowMuchLossModel] = None): BroughtForwardLossesController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[BroughtForwardLossesModel](Matchers.eq(KeystoreKeys.broughtForwardLosses))(Matchers.any(), Matchers.any()))
      .thenReturn(getData)

    when(mockCalcConnector.fetchAndGetFormData[OtherPropertiesModel](Matchers.eq(KeystoreKeys.otherProperties))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(otherPropertiesModel))

    when(mockCalcConnector.fetchAndGetFormData[PreviousLossOrGainModel]
      (Matchers.eq(KeystoreKeys.previousLossOrGain))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(previousLossOrGainModel))

    when(mockCalcConnector.fetchAndGetFormData[HowMuchGainModel](Matchers.eq(KeystoreKeys.howMuchGain))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(howMuchGainModel))

    when(mockCalcConnector.fetchAndGetFormData[HowMuchLossModel](Matchers.eq(KeystoreKeys.howMuchLoss))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(howMuchLossModel))

    new BroughtForwardLossesController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "Calling .broughtForwardLosses" when {

    "provided with no previous data" should {
      val target = setupTarget(None)
      lazy val result = target.broughtForwardLosses(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the Brought Forward Losses page" in {
        document.title() shouldBe messages.BroughtForwardLosses.question
      }

      "have a back link to Annual Exempt Amount page" in {
        document.select("#back-link").attr("href") shouldBe controllers.routes.AnnualExemptAmountController.annualExemptAmount().url
      }
    }

    "provided with previous data" should {
      val target = setupTarget(Some(BroughtForwardLossesModel(false, None)))
      lazy val result = target.broughtForwardLosses(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the Brought Forward Losses page" in {
        document.title() shouldBe messages.BroughtForwardLosses.question
      }
    }

    "provided with no valid session" should {
      val target = setupTarget(None)
      lazy val result = target.broughtForwardLosses(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect the user to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling .submitBroughtForwardLosses" when {

    "provided with a valid form" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("isClaiming", "No"), ("broughtForwardLoss", ""))
      lazy val result = target.submitBroughtForwardLosses(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the Check Your Answers page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.CheckYourAnswersController.checkYourAnswers().url)
      }
    }

    "provided with an invalid form" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("isClaiming", "Yes"), ("broughtForwardLoss", ""))
      lazy val result = target.submitBroughtForwardLosses(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "load the Brought Forward Losses page" in {
        document.title() shouldBe messages.BroughtForwardLosses.question
      }
    }

    "provided with an invalid session" should {
      val target = setupTarget(None)
      lazy val result = target.submitBroughtForwardLosses(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect the user to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling generateBackLink" should {

    "return a back link to the OtherProperties page when there is an answer of no to Other Properties" in {
      val target = setupTarget(None, Some(OtherPropertiesModel("No")))
      lazy val result = target.broughtForwardLosses(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      document.select("#back-link").attr("href") shouldBe controllers.routes.OtherPropertiesController.otherProperties().url
    }

    "return a back link to the HowMuchGain page when there is a previous positive gain" in {
      val target = setupTarget(None, Some(OtherPropertiesModel("Yes")), Some(PreviousLossOrGainModel("Gain")), Some(HowMuchGainModel(1)))
      lazy val result = target.broughtForwardLosses(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      document.select("#back-link").attr("href") shouldBe controllers.routes.HowMuchGainController.howMuchGain().url
    }

    "return a back link to the AnnualExemptAmount page when there is a previous gain of 0" in {
      val target = setupTarget(None, Some(OtherPropertiesModel("Yes")), Some(PreviousLossOrGainModel("Gain")), Some(HowMuchGainModel(0)))
      lazy val result = target.broughtForwardLosses(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      document.select("#back-link").attr("href") shouldBe controllers.routes.AnnualExemptAmountController.annualExemptAmount().url
    }

    "return a back link to the HowMuchLoss page when there is a previous positive loss" in {
      val target = setupTarget(None, Some(OtherPropertiesModel("Yes")), Some(PreviousLossOrGainModel("Loss")), howMuchLossModel = Some(HowMuchLossModel(1)))
      lazy val result = target.broughtForwardLosses(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      document.select("#back-link").attr("href") shouldBe controllers.routes.HowMuchLossController.howMuchLoss().url
    }

    "return a back link to the AnnualExemptAmount page when there is a previous loss of 0" in {
      val target = setupTarget(None, Some(OtherPropertiesModel("Yes")), Some(PreviousLossOrGainModel("Loss")), howMuchLossModel = Some(HowMuchLossModel(0)))
      lazy val result = target.broughtForwardLosses(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      document.select("#back-link").attr("href") shouldBe controllers.routes.AnnualExemptAmountController.annualExemptAmount().url
    }

    "return a back link to the AnnualExemptAmount page when there is a previous disposal that breaks even" in {
      val target = setupTarget(None, Some(OtherPropertiesModel("Yes")), Some(PreviousLossOrGainModel("Neither")))
      lazy val result = target.broughtForwardLosses(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      document.select("#back-link").attr("href") shouldBe controllers.routes.AnnualExemptAmountController.annualExemptAmount().url
    }
  }
}
