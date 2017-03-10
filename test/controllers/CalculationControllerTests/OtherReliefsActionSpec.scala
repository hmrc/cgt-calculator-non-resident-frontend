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

import assets.MessageLookup.NonResident.{OtherReliefs => messages}
import common.TestModels
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import connectors.CalculatorConnector
import constructors.AnswersConstructor
import controllers.OtherReliefsController
import controllers.helpers.FakeRequestHelper
import models.{TaxYearModel, _}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class OtherReliefsActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[OtherReliefsModel],
                  calculationResultsModel: CalculationResultsWithTaxOwedModel,
                  personalDetailsModel: TotalPersonalDetailsCalculationModel,
                  totalGainResultModel: TotalGainResultsModel = TotalGainResultsModel(200, None, None),
                  calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel] = None
                 ): OtherReliefsController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockAnswersConstructor = mock[AnswersConstructor]

    when(mockCalcConnector.fetchAndGetFormData[OtherReliefsModel](
      ArgumentMatchers.eq(KeystoreKeys.otherReliefsFlat))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](
      ArgumentMatchers.eq(KeystoreKeys.privateResidenceRelief))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(PrivateResidenceReliefModel("No", None))))

    when(mockAnswersConstructor.getNRTotalGainAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(TestModels.businessScenarioFiveModel))

    when(mockCalcConnector.calculateTotalGain(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainResultModel)))

    when(mockAnswersConstructor.getPersonalDetailsAndPreviousCapitalGainsAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(personalDetailsModel)))

    when(mockCalcConnector.calculateTaxableGainAfterPRR(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(calculationResultsWithPRRModel)

    when(mockCalcConnector.getFullAEA(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(BigDecimal(11000))))

    when(mockCalcConnector.getPartialAEA(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(BigDecimal(5500))))

    when(mockCalcConnector.calculateNRCGTTotalTax(
      ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(calculationResultsModel)))

    when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(TaxYearModel("2015/16", isValidYear = true, "2015/16"))))

    new OtherReliefsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val answersConstructor: AnswersConstructor = mockAnswersConstructor
    }
  }

  "OtherReliefsController" should {
    s"have a session timeout home link of '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
      OtherReliefsController.homeLink shouldEqual controllers.routes.DisposalDateController.disposalDate().url
    }
  }

  val personalDetailsModel = TotalPersonalDetailsCalculationModel(
    CustomerTypeModel("individual"),
    Some(CurrentIncomeModel(20000)),
    Some(PersonalAllowanceModel(0)),
    None,
    OtherPropertiesModel("Yes"),
    Some(PreviousLossOrGainModel("Neither")),
    None,
    None,
    Some(AnnualExemptAmountModel(0)),
    BroughtForwardLossesModel(false, None)
  )

  val calculationResultsModel = CalculationResultsWithTaxOwedModel(
    TotalTaxOwedModel(100, 100, 20, None, None, 200, 100, None, None, None, None, 0, None),
    None,
    None
  )

  "Calling the .otherReliefs action " when {

    "not supplied with a pre-existing stored model and a chargeable gain of £100 and total gain of £200" should {
      val target = setupTarget(None,
        calculationResultsModel,
        personalDetailsModel)
      lazy val result = target.otherReliefs(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the other reliefs page" in {
        document.title() shouldBe messages.question
      }

      s"show help text with text '${messages.additionalHelp(200, 100)}'" in {
        document.body().select("#otherReliefHelpTwo").select("p").text() shouldBe messages.additionalHelp(200, 100)
      }
    }

    "supplied with a pre-existing stored model" should {
      val testOtherReliefsModel = OtherReliefsModel(5000)
      val target = setupTarget(Some(testOtherReliefsModel),
        calculationResultsModel,
        personalDetailsModel)
      lazy val result = target.otherReliefs(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the other reliefs page" in {
        document.title() shouldBe messages.question
      }
    }

    "supplied without a valid session" should {
      val target = setupTarget(None,
        calculationResultsModel,
        personalDetailsModel)
      lazy val result = target.otherReliefs(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling the .submitOtherReliefs action" when {

    "submitting a valid form" should {
      val target = setupTarget(None,
        calculationResultsModel,
        personalDetailsModel)
      lazy val request = fakeRequestToPOSTWithSession("otherReliefs" -> "1000")
      lazy val result = target.submitOtherReliefs(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the summary page" in {
        redirectLocation(result).get shouldBe controllers.routes.SummaryController.summary().url
      }
    }

    "submitting an invalid form" should {
      val target = setupTarget(None,
        calculationResultsModel,
        personalDetailsModel)
      lazy val request = fakeRequestToPOSTWithSession("otherReliefs" -> "")
      lazy val result = target.submitOtherReliefs(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "return to the other reliefs page" in {
        document.title() shouldBe messages.question
      }
    }
  }
}
