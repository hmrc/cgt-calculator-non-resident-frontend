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

import assets.MessageLookup.NonResident.{AnnualExemptAmount => messages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.nonresident.CustomerTypeKeys
import connectors.CalculatorConnector
import controllers.AnnualExemptAmountController
import controllers.helpers.FakeRequestHelper
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.jsoup._
import org.scalatest.mock.MockitoSugar

import scala.concurrent.Future
import controllers.routes
import models._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.HeaderCarrier

class AnnualExemptAmountActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(
                   getData: Option[AnnualExemptAmountModel],
                   disabledTrustee: String = "",
                   disposalDate: Option[DisposalDateModel] = Some(DisposalDateModel(12, 12, 2016)),
                   previousLossOrGain: Option[PreviousLossOrGainModel] = Some(PreviousLossOrGainModel("Neither")),
                   howMuchLoss: Option[HowMuchLossModel] = None,
                   howMuchGain: Option[HowMuchGainModel] = None
                 ): AnnualExemptAmountController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[DisposalDateModel](
      ArgumentMatchers.eq(KeystoreKeys.disposalDate))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(disposalDate))

    when(mockCalcConnector.fetchAndGetFormData[AnnualExemptAmountModel](
      ArgumentMatchers.eq(KeystoreKeys.annualExemptAmount))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[PreviousLossOrGainModel](ArgumentMatchers.eq(KeystoreKeys.previousLossOrGain))
      (ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(previousLossOrGain))

    when(mockCalcConnector.fetchAndGetFormData[HowMuchGainModel](ArgumentMatchers.eq(KeystoreKeys.howMuchGain))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(howMuchGain))

    when(mockCalcConnector.fetchAndGetFormData[HowMuchLossModel](ArgumentMatchers.eq(KeystoreKeys.howMuchLoss))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(howMuchLoss))

    when(mockCalcConnector.getFullAEA(ArgumentMatchers.anyInt())(ArgumentMatchers.any()))
      .thenReturn(Some(BigDecimal(11100)))

    when(mockCalcConnector.getTaxYear(ArgumentMatchers.anyString())(ArgumentMatchers.any()))
      .thenReturn(Some(TaxYearModel("2016-5-6", isValidYear = true, "2016/17")))

    when(mockCalcConnector.saveFormData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map.empty)))

    new AnnualExemptAmountController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  // GET Tests
  "Calling the .annualExemptAmount action" when {

    "not supplied with a pre-existing stored model" should {
      val target = setupTarget(None)
      lazy val result = target.annualExemptAmount(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"have the title '${messages.question}'" in {
        document.title shouldEqual messages.question
      }
    }

    "supplied with a 2016/17 tax year date" should {
      val target = setupTarget(None, "Yes", disposalDate = Some(DisposalDateModel(12, 12, 2016)))
      lazy val result = target.annualExemptAmount(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"have the help text '${messages.hint("11,100")}'" in {
        document.select("#input-hint").text() shouldBe messages.hint("11,100")
      }
    }

    "supplied with a 2015/16 tax year date" should {
      val target = setupTarget(None, disposalDate = Some(DisposalDateModel(12, 12, 2015)))
      lazy val result = target.annualExemptAmount(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"have the help text '${messages.hint("11,100")}'" in {
        document.select("#input-hint").text() shouldBe messages.hint("11,100")
      }
    }

    "supplied with a date outside 2015/16, 2016/17 tax years" should {
      val target = setupTarget(None, disposalDate = Some(DisposalDateModel(12, 12, 2013)))
      lazy val result = target.annualExemptAmount(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"have the help text '${messages.hint("11,100")}'" in {
        document.select("#input-hint").text() shouldBe messages.hint("11,100")
      }
    }

    "not supplied with a valid session" should {
      val target = setupTarget(None, disposalDate = Some(DisposalDateModel(12, 12, 2016)))
      lazy val result = target.annualExemptAmount(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }

    "when there was no previous gain or loss" should {
      val target = setupTarget(None, previousLossOrGain = Some(PreviousLossOrGainModel("Neither")))
      lazy val result = target.annualExemptAmount(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "have a back link to previous-gain-or-loss" in {
        document.select("#back-link").attr("href") shouldEqual routes.PreviousGainOrLossController.previousGainOrLoss().url
      }
    }

    "when there was a previous loss of 0" should {
      val target = setupTarget(None, previousLossOrGain = Some(PreviousLossOrGainModel("Loss")), howMuchLoss = Some(HowMuchLossModel(0)))
      lazy val result = target.annualExemptAmount(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "have a back link to previous-gain-or-loss" in {
        document.select("#back-link").attr("href") shouldEqual routes.HowMuchLossController.howMuchLoss().url
      }
    }

    "when there was a previous gain of 0" should {
      val target = setupTarget(None, previousLossOrGain = Some(PreviousLossOrGainModel("Gain")), howMuchGain = Some(HowMuchGainModel(0)))
      lazy val result = target.annualExemptAmount(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "have a back link to previous-gain-or-loss" in {
        document.select("#back-link").attr("href") shouldEqual routes.HowMuchGainController.howMuchGain().url
      }
    }
  }

  // POST Tests
  "Calling the .submitAnnualExemptAmount action" when {

    "submitting a valid form for a non-trustee" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("annualExemptAmount", "1000"))
      lazy val result = target.submitAnnualExemptAmount(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "should redirect to the Brought Forward Losses page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.BroughtForwardLossesController.broughtForwardLosses().url)
      }
    }

    "submitting an invalid form" should {
      val target = setupTarget(None, "Yes")
      lazy val request = fakeRequestToPOSTWithSession(("annualExemptAmount", "1000000"))
      lazy val result = target.submitAnnualExemptAmount(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "return to the Annual Exempt Amount page" in {
        document.title shouldBe messages.question
      }
    }
  }
}
