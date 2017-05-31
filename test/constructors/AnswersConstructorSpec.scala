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

package constructors

import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import connectors.CalculatorConnector
import models._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class AnswersConstructorSpec extends UnitSpec with MockitoSugar {

  def setupMockedAnswersConstructor(totalGainAnswersModel: TotalGainAnswersModel,
                                    worthBeforeLegislationStartModel: Option[WorthBeforeLegislationStartModel] = None,
                                    marketValueAcquisition: Option[AcquisitionValueModel] = None,
                                    marketDisposalValue: Option[DisposalValueModel] = None): AnswersConstructor = {

    val mockConnector = mock[CalculatorConnector]

    when(mockConnector.fetchAndGetFormData[DisposalDateModel](ArgumentMatchers.eq(KeystoreKeys.disposalDate))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.disposalDateModel)))

    when(mockConnector.fetchAndGetFormData[SoldOrGivenAwayModel](
      ArgumentMatchers.eq(KeystoreKeys.soldOrGivenAway))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.soldOrGivenAwayModel)))

    when(mockConnector.fetchAndGetFormData[SoldForLessModel](ArgumentMatchers.eq(KeystoreKeys.soldForLess))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.soldForLessModel))

    when(mockConnector.fetchAndGetFormData[DisposalValueModel](ArgumentMatchers.eq(KeystoreKeys.disposalValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.disposalValueModel)))

    when(mockConnector.fetchAndGetFormData[DisposalValueModel](
      ArgumentMatchers.eq(KeystoreKeys.disposalMarketValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(marketDisposalValue))

    when(mockConnector.fetchAndGetFormData[DisposalCostsModel](ArgumentMatchers.eq(KeystoreKeys.disposalCosts))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.disposalCostsModel)))

    when(mockConnector.fetchAndGetFormData[HowBecameOwnerModel](
      ArgumentMatchers.eq(KeystoreKeys.howBecameOwner))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.howBecameOwnerModel))

    when(mockConnector.fetchAndGetFormData[BoughtForLessModel](ArgumentMatchers.eq(KeystoreKeys.boughtForLess))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.boughtForLessModel))

    when(mockConnector.fetchAndGetFormData[WorthBeforeLegislationStartModel](ArgumentMatchers.eq(KeystoreKeys.worthBeforeLegislationStart))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(worthBeforeLegislationStartModel))
    when(mockConnector.fetchAndGetFormData[AcquisitionValueModel](
      ArgumentMatchers.eq(KeystoreKeys.acquisitionMarketValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(marketValueAcquisition))

    when(mockConnector.fetchAndGetFormData[AcquisitionValueModel](
      ArgumentMatchers.eq(KeystoreKeys.acquisitionValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.acquisitionValueModel)))

    when(mockConnector.fetchAndGetFormData[AcquisitionCostsModel](
      ArgumentMatchers.eq(KeystoreKeys.acquisitionCosts))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.acquisitionCostsModel)))

    when(mockConnector.fetchAndGetFormData[AcquisitionDateModel](
      ArgumentMatchers.eq(KeystoreKeys.acquisitionDate))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.acquisitionDateModel)))

    when(mockConnector.fetchAndGetFormData[RebasedValueModel](ArgumentMatchers.eq(KeystoreKeys.rebasedValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.rebasedValueModel))

    when(mockConnector.fetchAndGetFormData[RebasedCostsModel](ArgumentMatchers.eq(KeystoreKeys.rebasedCosts))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.rebasedCostsModel))

    when(mockConnector.fetchAndGetFormData[ImprovementsModel](ArgumentMatchers.eq(KeystoreKeys.improvements))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.improvementsModel)))

    when(mockConnector.fetchAndGetFormData[OtherReliefsModel](
      ArgumentMatchers.eq(KeystoreKeys.otherReliefsFlat))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.otherReliefsFlat))

    new AnswersConstructor {
      override val calculatorConnector: CalculatorConnector = mockConnector
    }
  }

  def setupMockedFinalAnswersConstructor(totalPersonalDetailsCalculationModel: TotalPersonalDetailsCalculationModel): AnswersConstructor = {

    val mockConnector = mock[CalculatorConnector]

    when(mockConnector.fetchAndGetFormData[CurrentIncomeModel](ArgumentMatchers.eq(KeystoreKeys.currentIncome))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalPersonalDetailsCalculationModel.currentIncomeModel)))

    when(mockConnector.fetchAndGetFormData[PersonalAllowanceModel](
      ArgumentMatchers.eq(KeystoreKeys.personalAllowance))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalPersonalDetailsCalculationModel.personalAllowanceModel))

    when(mockConnector.fetchAndGetFormData[OtherPropertiesModel](
      ArgumentMatchers.eq(KeystoreKeys.otherProperties))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalPersonalDetailsCalculationModel.otherPropertiesModel)))

    when(mockConnector.fetchAndGetFormData[PreviousLossOrGainModel](ArgumentMatchers.eq(KeystoreKeys.previousLossOrGain))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalPersonalDetailsCalculationModel.previousGainOrLoss))

    when(mockConnector.fetchAndGetFormData[HowMuchLossModel](ArgumentMatchers.eq(KeystoreKeys.howMuchLoss))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalPersonalDetailsCalculationModel.howMuchLossModel))

    when(mockConnector.fetchAndGetFormData[HowMuchGainModel](ArgumentMatchers.eq(KeystoreKeys.howMuchGain))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalPersonalDetailsCalculationModel.howMuchGainModel))

    when(mockConnector.fetchAndGetFormData[AnnualExemptAmountModel](
      ArgumentMatchers.eq(KeystoreKeys.annualExemptAmount))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalPersonalDetailsCalculationModel.annualExemptAmountModel))

    when(mockConnector.fetchAndGetFormData[BroughtForwardLossesModel](
      ArgumentMatchers.eq(KeystoreKeys.broughtForwardLosses))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalPersonalDetailsCalculationModel.broughtForwardLossesModel)))

    new AnswersConstructor {
      override val calculatorConnector: CalculatorConnector = mockConnector
    }
  }

  val totalGainNoOptionalModel = TotalGainAnswersModel(
    DisposalDateModel(10, 10, 2016),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(10000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    AcquisitionDateModel(1, 1, 2016),
    None,
    None,
    ImprovementsModel("No", None, None),
    None
  )

  val totalGainAllOptionalModel = TotalGainAnswersModel(
    DisposalDateModel(10, 10, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(false)),
    DisposalValueModel(10000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Bought")),
    Some(BoughtForLessModel(false)),
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    AcquisitionDateModel(1, 4, 2013),
    Some(RebasedValueModel(7500)),
    Some(RebasedCostsModel("Yes", Some(150))),
    ImprovementsModel("Yes", Some(50), Some(25)),
    Some(OtherReliefsModel(1000))
  )

  val modelDateBeforeLegislationStart = TotalGainAnswersModel(
    DisposalDateModel(10, 10, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(false)),
    DisposalValueModel(10000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Bought")),
    Some(BoughtForLessModel(false)),
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    AcquisitionDateModel(1, 4, 1967),
    Some(RebasedValueModel(7500)),
    Some(RebasedCostsModel("Yes", Some(150))),
    ImprovementsModel("Yes", Some(50), Some(25)),
    Some(OtherReliefsModel(1000))
  )

  val totalGainBoughtForLess = TotalGainAnswersModel(
    DisposalDateModel(10, 10, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(false)),
    DisposalValueModel(10000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Bought")),
    Some(BoughtForLessModel(true)),
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    AcquisitionDateModel(1, 4, 2013),
    Some(RebasedValueModel(7500)),
    Some(RebasedCostsModel("Yes", Some(150))),
    ImprovementsModel("Yes", Some(50), Some(25)),
    Some(OtherReliefsModel(1000))
  )

  val totalGainSoldForLess = TotalGainAnswersModel(
    DisposalDateModel(10, 10, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(true)),
    DisposalValueModel(11000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Bought")),
    Some(BoughtForLessModel(true)),
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    AcquisitionDateModel(1, 4, 2013),
    Some(RebasedValueModel(7500)),
    Some(RebasedCostsModel("Yes", Some(150))),
    ImprovementsModel("Yes", Some(50), Some(25)),
    Some(OtherReliefsModel(1000))
  )

  "Calling getNRTotalGainAnswers" should {

    "return a valid TotalGainAnswersModel with no optional values" in {
      val hc = mock[HeaderCarrier]
      val constructor = setupMockedAnswersConstructor(totalGainNoOptionalModel, marketValueAcquisition = Some(AcquisitionValueModel(5000)),
        marketDisposalValue = Some(DisposalValueModel(10000)))
      val result = constructor.getNRTotalGainAnswers(hc)

      await(result) shouldBe totalGainNoOptionalModel
    }

    "return a valid TotalGainAnswersModel with all optional values" in {
      val hc = mock[HeaderCarrier]
      val constructor = setupMockedAnswersConstructor(totalGainAllOptionalModel)
      val result = constructor.getNRTotalGainAnswers(hc)

      await(result) shouldBe totalGainAllOptionalModel
    }

    "return a valid acquisition value of 4000 with an acquisition date before legislation start" in {
      val hc = mock[HeaderCarrier]
      val constructor = setupMockedAnswersConstructor(modelDateBeforeLegislationStart,
        worthBeforeLegislationStartModel = Some(WorthBeforeLegislationStartModel(4000)))
      val result = constructor.getNRTotalGainAnswers(hc)

      await(result).acquisitionValueModel.acquisitionValueAmt shouldBe 4000
    }

    "return a valid acquisition value of 3000 with an property acquired without purchasing" in {
      val hc = mock[HeaderCarrier]
      val constructor = setupMockedAnswersConstructor(totalGainNoOptionalModel, marketValueAcquisition = Some(AcquisitionValueModel(3000)),
        marketDisposalValue = Some(DisposalValueModel(10000)))
      val result = constructor.getNRTotalGainAnswers(hc)

      await(result).acquisitionValueModel.acquisitionValueAmt shouldBe 3000
    }

    "return a valid acquisition value of 2000 with a property bought for less" in {
      val hc = mock[HeaderCarrier]
      val constructor = setupMockedAnswersConstructor(totalGainBoughtForLess, marketValueAcquisition = Some(AcquisitionValueModel(2000)))
      val result = constructor.getNRTotalGainAnswers(hc)

      await(result).acquisitionValueModel.acquisitionValueAmt shouldBe 2000
    }

    "return a valid disposal value of 10000 when sold and not sold for less" in {
      val hc = mock[HeaderCarrier]
      val constructor = setupMockedAnswersConstructor(totalGainAllOptionalModel)
      val result = constructor.getNRTotalGainAnswers(hc)

      await(result).disposalValueModel.disposalValue shouldBe 10000
    }

    "return a valid disposal value of 11000 when sold and sold for less" in {
      val hc = mock[HeaderCarrier]
      val constructor = setupMockedAnswersConstructor(totalGainSoldForLess, marketValueAcquisition = Some(AcquisitionValueModel(5000)),
        marketDisposalValue = Some(DisposalValueModel(11000)))
      val result = constructor.getNRTotalGainAnswers(hc)

      await(result).disposalValueModel.disposalValue shouldBe 11000
    }

    "return a valid disposal value of 10000 when given away" in {
      val hc = mock[HeaderCarrier]
      val constructor = setupMockedAnswersConstructor(totalGainNoOptionalModel, marketValueAcquisition = Some(AcquisitionValueModel(5000)),
        marketDisposalValue = Some(DisposalValueModel(10000)))
      val result = constructor.getNRTotalGainAnswers(hc)

      await(result).disposalValueModel.disposalValue shouldBe 10000
    }
  }

  "Calling .getPersonalDetailsAndPreviousCapitalGainsAnswers" should {

    val model = TotalPersonalDetailsCalculationModel(
      CurrentIncomeModel(10000),
      Some(PersonalAllowanceModel(100)),
      OtherPropertiesModel("Yes"),
      Some(PreviousLossOrGainModel("Loss")),
      Some(HowMuchLossModel(100)),
      Some(HowMuchGainModel(200)),
      Some(AnnualExemptAmountModel(10000)),
      BroughtForwardLossesModel(isClaiming = true, Some(1000))
    )

    val hc = mock[HeaderCarrier]
    val constructor = setupMockedFinalAnswersConstructor(model)

    "when called with the model with all options return all options" in {
      await(constructor.getPersonalDetailsAndPreviousCapitalGainsAnswers(hc)) shouldEqual Some(model)
    }
  }
}
