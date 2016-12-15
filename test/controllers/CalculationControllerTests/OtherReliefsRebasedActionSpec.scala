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

import assets.MessageLookup.NonResident.{OtherReliefs => messages}
import common.TestModels
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import connectors.CalculatorConnector
import constructors.AnswersConstructor
import controllers.OtherReliefsRebasedController
import controllers.helpers.FakeRequestHelper
import models.{TaxYearModel, _}
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class OtherReliefsRebasedActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[OtherReliefsModel],
                  gainAnswers: TotalGainAnswersModel,
                  calculationResultsModel: CalculationResultsWithTaxOwedModel,
                  personalDetailsModel: TotalPersonalDetailsCalculationModel,
                  totalGainResultModel: TotalGainResultsModel = TotalGainResultsModel(200, Some(500), None),
                  calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel] = None
                 ): OtherReliefsRebasedController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockAnswersConstructor = mock[AnswersConstructor]


    when(mockCalcConnector.fetchAndGetFormData[OtherReliefsModel](Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](Matchers.eq(KeystoreKeys.privateResidenceRelief))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(PrivateResidenceReliefModel("No", None))))

    when(mockAnswersConstructor.getNRTotalGainAnswers(Matchers.any()))
      .thenReturn(Future.successful(gainAnswers))

    when(mockCalcConnector.calculateTotalGain(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(totalGainResultModel)))

    when(mockAnswersConstructor.getPersonalDetailsAndPreviousCapitalGainsAnswers(Matchers.any()))
      .thenReturn(Future.successful(Some(personalDetailsModel)))

    when(mockCalcConnector.calculateTaxableGainAfterPRR(Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(calculationResultsWithPRRModel)

    when(mockCalcConnector.getFullAEA(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(BigDecimal(11000))))

    when(mockCalcConnector.getPartialAEA(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(BigDecimal(5500))))

    when(mockCalcConnector.calculateNRCGTTotalTax(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(TestModels.calculationResultsModelWithRebased)))

    when(mockCalcConnector.getTaxYear(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(TaxYearModel("2015/16", isValidYear = true, "2015/16"))))

    new OtherReliefsRebasedController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val answersConstructor: AnswersConstructor = mockAnswersConstructor
    }
  }

  "OtherReliefsRebasedController" should {
    s"have a session timeout home link of '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
      OtherReliefsRebasedController.homeLink shouldEqual controllers.routes.DisposalDateController.disposalDate().url
    }
  }

  "Calling the .otherReliefsRebased action " when {

    "not supplied with a pre-existing stored model and a chargeable gain of £500 and total gain of £500" should {

      val target = setupTarget(
        None,
        TestModels.totalGainAnswersModelWithRebasedTA,
        TestModels.calculationResultsModelWithRebased,
        TestModels.personalDetailsCalculationModelIndividual
      )
      lazy val result = target.otherReliefsRebased(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200 with a valid calculation result" in {
        status(result) shouldBe 200
      }

      "load the otherReliefs rebased page" in {
        document.title() shouldBe messages.question
      }

      s"have a total gain message with text '${messages.totalGain}' £500" in {
        document.getElementById("totalGain").text() shouldBe s"${messages.totalGain} £500"
      }

      s"have a taxable gain message with text '${messages.taxableGain}' £500" in {
        document.getElementById("taxableGain").text() shouldBe s"${messages.taxableGain} £500"
      }
    }

    "supplied with a pre-existing stored model" should {
      val testOtherReliefsModel = OtherReliefsModel(5000)
      val target = setupTarget(
        Some(testOtherReliefsModel),
        TestModels.totalGainAnswersModelWithRebasedTA,
        TestModels.calculationResultsModelWithRebased,
        TestModels.personalDetailsCalculationModelIndividual
      )
      lazy val result = target.otherReliefsRebased(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the otherReliefs rebased page" in {
        document.title() shouldBe messages.question
      }
    }

    "supplied with an invalid session" should {
      val target = setupTarget(
        None,
        TestModels.totalGainAnswersModelWithRebasedTA,
        TestModels.calculationResultsModelWithRebased,
        TestModels.personalDetailsCalculationModelIndividual
      )
      lazy val result = target.otherReliefsRebased(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "In CalculationController calling the .submitOtherReliefsRebased action" when {

    "submitting a valid form" should {
      val target = setupTarget(
        None,
        TestModels.totalGainAnswersModelWithRebasedTA,
        TestModels.calculationResultsModelWithRebased,
        TestModels.personalDetailsCalculationModelIndividual
      )
      lazy val request = fakeRequestToPOSTWithSession(("isClaimingOtherReliefs", "Yes"), ("otherReliefs", "1000"))
      lazy val result = target.submitOtherReliefsRebased(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the calculation election page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.CalculationElectionController.calculationElection().url)
      }
    }

    "submitting an invalid form" should {
      val target = setupTarget(
        None,
        TestModels.totalGainAnswersModelWithRebasedTA,
        TestModels.calculationResultsModelWithRebased,
        TestModels.personalDetailsCalculationModelIndividual
      )
      lazy val request = fakeRequestToPOSTWithSession(("isClaimingOtherReliefs", "Yes"), ("otherReliefs", "-1000"))
      lazy val result = target.submitOtherReliefsRebased(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "return to the other reliefs rebased page" in {
        document.title() shouldBe messages.question
      }
    }
  }
}
