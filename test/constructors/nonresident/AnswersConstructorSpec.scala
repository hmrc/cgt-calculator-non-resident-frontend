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

package constructors.nonresident

import common.KeystoreKeys
import connectors.CalculatorConnector
import models.nonresident._
import org.mockito.Matchers
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

    when(mockConnector.fetchAndGetFormData[DisposalDateModel](Matchers.eq(KeystoreKeys.disposalDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.disposalDateModel)))
    when(mockConnector.fetchAndGetFormData[SoldOrGivenAwayModel](Matchers.eq(KeystoreKeys.soldOrGivenAway))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.soldOrGivenAwayModel)))
    when(mockConnector.fetchAndGetFormData[SoldForLessModel](Matchers.eq(KeystoreKeys.NonResidentKeys.soldForLess))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.soldForLessModel))
    when(mockConnector.fetchAndGetFormData[DisposalValueModel](Matchers.eq(KeystoreKeys.disposalValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.disposalValueModel)))
    when(mockConnector.fetchAndGetFormData[DisposalValueModel](Matchers.eq(KeystoreKeys.disposalMarketValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(marketDisposalValue))
    when(mockConnector.fetchAndGetFormData[DisposalCostsModel](Matchers.eq(KeystoreKeys.disposalCosts))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.disposalCostsModel)))
    when(mockConnector.fetchAndGetFormData[HowBecameOwnerModel](Matchers.eq(KeystoreKeys.howBecameOwner))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.howBecameOwnerModel))
    when(mockConnector.fetchAndGetFormData[BoughtForLessModel](Matchers.eq(KeystoreKeys.boughtForLess))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.boughtForLessModel))
    when(mockConnector.fetchAndGetFormData[WorthBeforeLegislationStartModel](Matchers.eq(KeystoreKeys.worthBeforeLegislationStart))
      (Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(worthBeforeLegislationStartModel))
    when(mockConnector.fetchAndGetFormData[AcquisitionValueModel](Matchers.eq(KeystoreKeys.acquisitionMarketValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(marketValueAcquisition))
    when(mockConnector.fetchAndGetFormData[AcquisitionValueModel](Matchers.eq(KeystoreKeys.acquisitionValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.acquisitionValueModel)))
    when(mockConnector.fetchAndGetFormData[AcquisitionCostsModel](Matchers.eq(KeystoreKeys.acquisitionCosts))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.acquisitionCostsModel)))
    when(mockConnector.fetchAndGetFormData[AcquisitionDateModel](Matchers.eq(KeystoreKeys.acquisitionDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.acquisitionDateModel)))
    when(mockConnector.fetchAndGetFormData[RebasedValueModel](Matchers.eq(KeystoreKeys.rebasedValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.rebasedValueModel))
    when(mockConnector.fetchAndGetFormData[RebasedCostsModel](Matchers.eq(KeystoreKeys.rebasedCosts))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.rebasedCostsModel))
    when(mockConnector.fetchAndGetFormData[ImprovementsModel](Matchers.eq(KeystoreKeys.improvements))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.improvementsModel)))
    when(mockConnector.fetchAndGetFormData[OtherReliefsModel](Matchers.eq(KeystoreKeys.otherReliefsFlat))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.otherReliefsFlat))

    new AnswersConstructor {
      override val calculatorConnector: CalculatorConnector = mockConnector
    }
  }

  def setupMockedFinalAnswersConstructor(totalPersonalDetailsCalculationModel: TotalPersonalDetailsCalculationModel): AnswersConstructor = {

    val mockConnector = mock[CalculatorConnector]

    when(mockConnector.fetchAndGetFormData[CustomerTypeModel](Matchers.eq(KeystoreKeys.customerType))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(totalPersonalDetailsCalculationModel.customerTypeModel)))

    when(mockConnector.fetchAndGetFormData[CurrentIncomeModel](Matchers.eq(KeystoreKeys.currentIncome))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(totalPersonalDetailsCalculationModel.currentIncomeModel))

    when(mockConnector.fetchAndGetFormData[PersonalAllowanceModel](Matchers.eq(KeystoreKeys.personalAllowance))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(totalPersonalDetailsCalculationModel.personalAllowanceModel))

    when(mockConnector.fetchAndGetFormData[DisabledTrusteeModel](Matchers.eq(KeystoreKeys.disabledTrustee))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(totalPersonalDetailsCalculationModel.trusteeModel))

    when(mockConnector.fetchAndGetFormData[OtherPropertiesModel](Matchers.eq(KeystoreKeys.otherProperties))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(totalPersonalDetailsCalculationModel.otherPropertiesModel)))

    when(mockConnector.fetchAndGetFormData[PreviousLossOrGainModel](Matchers.eq(KeystoreKeys.NonResidentKeys.previousLossOrGain))
      (Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(totalPersonalDetailsCalculationModel.previousGainOrLoss))

    when(mockConnector.fetchAndGetFormData[HowMuchLossModel](Matchers.eq(KeystoreKeys.howMuchLoss))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(totalPersonalDetailsCalculationModel.howMuchLossModel))

    when(mockConnector.fetchAndGetFormData[HowMuchGainModel](Matchers.eq(KeystoreKeys.howMuchGain))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(totalPersonalDetailsCalculationModel.howMuchGainModel))

    when(mockConnector.fetchAndGetFormData[AnnualExemptAmountModel](Matchers.eq(KeystoreKeys.annualExemptAmount))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(totalPersonalDetailsCalculationModel.annualExemptAmountModel))

    when(mockConnector.fetchAndGetFormData[BroughtForwardLossesModel](Matchers.eq(KeystoreKeys.broughtForwardLosses))(Matchers.any(), Matchers.any()))
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
    AcquisitionDateModel("No", None, None, None),
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
    AcquisitionDateModel("Yes", Some(1), Some(4), Some(2013)),
    Some(RebasedValueModel(Some(7500))),
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
    AcquisitionDateModel("Yes", Some(1), Some(4), Some(1967)),
    Some(RebasedValueModel(Some(7500))),
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
    AcquisitionDateModel("Yes", Some(1), Some(4), Some(2013)),
    Some(RebasedValueModel(Some(7500))),
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
    AcquisitionDateModel("Yes", Some(1), Some(4), Some(2013)),
    Some(RebasedValueModel(Some(7500))),
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
      CustomerTypeModel("Individual"),
      Some(CurrentIncomeModel(10000)),
      Some(PersonalAllowanceModel(100)),
      Some(DisabledTrusteeModel("Yes")),
      OtherPropertiesModel("Yes"),
      Some(PreviousLossOrGainModel("Loss")),
      Some(HowMuchLossModel(100)),
      Some(HowMuchGainModel(200)),
      Some(AnnualExemptAmountModel(10000)),
      BroughtForwardLossesModel(true, Some(1000))
    )

    val hc = mock[HeaderCarrier]
    val constructor = setupMockedFinalAnswersConstructor(model)

    "when called with the model with all options return all options" in {
      await(constructor.getPersonalDetailsAndPreviousCapitalGainsAnswers(hc)) shouldEqual Some(model)
    }
  }
}
