/*
 * Copyright 2024 HM Revenue & Customs
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

import common.KeystoreKeys.NonResidentKeys as KeystoreKeys
import common.{CommonPlaySpec, WithCommonFakeApplication}
import connectors.CalculatorConnector
import models.*
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import services.SessionCacheService
import constructors.SessionExpiredException
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AnswersConstructorSpec extends CommonPlaySpec with MockitoSugar with WithCommonFakeApplication {
  val mockCalcConnector: CalculatorConnector =mock[CalculatorConnector]
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]

  class Setup {
    val controller = new AnswersConstructor(
      mockSessionCacheService
    )
  }

  def setupMockedAnswersConstructor(totalGainAnswersModel: TotalGainAnswersModel,
                                    worthBeforeLegislationStartModel: Option[WorthBeforeLegislationStartModel] = None,
                                    marketValueAcquisition: Option[AcquisitionValueModel] = None,
                                    marketDisposalValue: Option[DisposalValueModel] = None): AnswersConstructor = {


    when(mockSessionCacheService.fetchAndGetFormData[DateModel](ArgumentMatchers.eq(KeystoreKeys.disposalDate))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.disposalDateModel)))

    when(mockSessionCacheService.fetchAndGetFormData[SoldOrGivenAwayModel](
      ArgumentMatchers.eq(KeystoreKeys.soldOrGivenAway))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.soldOrGivenAwayModel)))

    when(mockSessionCacheService.fetchAndGetFormData[SoldForLessModel](ArgumentMatchers.eq(KeystoreKeys.soldForLess))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.soldForLessModel))

    when(mockSessionCacheService.fetchAndGetFormData[DisposalValueModel](ArgumentMatchers.eq(KeystoreKeys.disposalValue))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.disposalValueModel)))

    when(mockSessionCacheService.fetchAndGetFormData[DisposalValueModel](
      ArgumentMatchers.eq(KeystoreKeys.disposalMarketValue))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(marketDisposalValue))

    when(mockSessionCacheService.fetchAndGetFormData[DisposalCostsModel](ArgumentMatchers.eq(KeystoreKeys.disposalCosts))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.disposalCostsModel)))

    when(mockSessionCacheService.fetchAndGetFormData[HowBecameOwnerModel](
      ArgumentMatchers.eq(KeystoreKeys.howBecameOwner))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.howBecameOwnerModel))

    when(mockSessionCacheService.fetchAndGetFormData[BoughtForLessModel](ArgumentMatchers.eq(KeystoreKeys.boughtForLess))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.boughtForLessModel))

    when(mockSessionCacheService.fetchAndGetFormData[BigDecimal]
      (ArgumentMatchers.eq(KeystoreKeys.costAtLegislationStart))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(None))

    when(mockSessionCacheService.fetchAndGetFormData[WorthBeforeLegislationStartModel](ArgumentMatchers.eq(KeystoreKeys.worthBeforeLegislationStart))
      (using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(worthBeforeLegislationStartModel))
    when(mockSessionCacheService.fetchAndGetFormData[AcquisitionValueModel](
      ArgumentMatchers.eq(KeystoreKeys.acquisitionMarketValue))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(marketValueAcquisition))

    when(mockSessionCacheService.fetchAndGetFormData[AcquisitionValueModel](
      ArgumentMatchers.eq(KeystoreKeys.acquisitionValue))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.acquisitionValueModel)))

    when(mockSessionCacheService.fetchAndGetFormData[AcquisitionCostsModel](
      ArgumentMatchers.eq(KeystoreKeys.acquisitionCosts))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.acquisitionCostsModel.get)))

    when(mockSessionCacheService.fetchAndGetFormData[DateModel](
      ArgumentMatchers.eq(KeystoreKeys.acquisitionDate))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.acquisitionDateModel)))

    when(mockSessionCacheService.fetchAndGetFormData[RebasedValueModel](ArgumentMatchers.eq(KeystoreKeys.rebasedValue))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.rebasedValueModel))

    when(mockSessionCacheService.fetchAndGetFormData[RebasedCostsModel](ArgumentMatchers.eq(KeystoreKeys.rebasedCosts))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.rebasedCostsModel))

    when(mockSessionCacheService.fetchAndGetFormData[IsClaimingImprovementsModel](
      ArgumentMatchers.eq(KeystoreKeys.isClaimingImprovements))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.isClaimingImprovementsModel)))

    when(mockSessionCacheService.fetchAndGetFormData[ImprovementsModel](ArgumentMatchers.eq(KeystoreKeys.improvements))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.improvementsModel))

    when(mockSessionCacheService.fetchAndGetFormData[OtherReliefsModel](
      ArgumentMatchers.eq(KeystoreKeys.otherReliefsFlat))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.otherReliefsFlat))

    new AnswersConstructor(mockSessionCacheService)
  }

  def setupMockedFinalAnswersConstructor(totalPersonalDetailsCalculationModel: TotalPersonalDetailsCalculationModel): AnswersConstructor = {

    if(totalPersonalDetailsCalculationModel.currentIncomeModel.currentIncome != null){
    when(mockSessionCacheService.fetchAndGetFormData[CurrentIncomeModel](ArgumentMatchers.eq(KeystoreKeys.currentIncome))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalPersonalDetailsCalculationModel.currentIncomeModel)))
    }
    else{
      when(mockSessionCacheService.fetchAndGetFormData[CurrentIncomeModel](ArgumentMatchers.eq(KeystoreKeys.currentIncome))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Option.empty))
    }
    when(mockSessionCacheService.fetchAndGetFormData[PersonalAllowanceModel](
      ArgumentMatchers.eq(KeystoreKeys.personalAllowance))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalPersonalDetailsCalculationModel.personalAllowanceModel))

    when(mockSessionCacheService.fetchAndGetFormData[OtherPropertiesModel](
      ArgumentMatchers.eq(KeystoreKeys.otherProperties))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalPersonalDetailsCalculationModel.otherPropertiesModel)))

    when(mockSessionCacheService.fetchAndGetFormData[PreviousLossOrGainModel](ArgumentMatchers.eq(KeystoreKeys.previousLossOrGain))
      (using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalPersonalDetailsCalculationModel.previousGainOrLoss))

    when(mockSessionCacheService.fetchAndGetFormData[HowMuchLossModel](ArgumentMatchers.eq(KeystoreKeys.howMuchLoss))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalPersonalDetailsCalculationModel.howMuchLossModel))

    when(mockSessionCacheService.fetchAndGetFormData[HowMuchGainModel](ArgumentMatchers.eq(KeystoreKeys.howMuchGain))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalPersonalDetailsCalculationModel.howMuchGainModel))

    when(mockSessionCacheService.fetchAndGetFormData[AnnualExemptAmountModel](
      ArgumentMatchers.eq(KeystoreKeys.annualExemptAmount))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalPersonalDetailsCalculationModel.annualExemptAmountModel))

    when(mockSessionCacheService.fetchAndGetFormData[BroughtForwardLossesModel](
      ArgumentMatchers.eq(KeystoreKeys.broughtForwardLosses))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalPersonalDetailsCalculationModel.broughtForwardLossesModel)))

    new AnswersConstructor(mockSessionCacheService)
  }

  val totalGainNoOptionalModel: TotalGainAnswersModel = TotalGainAnswersModel(
    DateModel(10, 10, 2016),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(10000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    Some(AcquisitionCostsModel(200)),
    DateModel(1, 1, 2016),
    None,
    None,
    IsClaimingImprovementsModel(false),
    None,
    None
  )

  val totalGainAllOptionalModel: TotalGainAnswersModel = TotalGainAnswersModel(
    DateModel(10, 10, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(false)),
    DisposalValueModel(10000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Bought")),
    Some(BoughtForLessModel(false)),
    AcquisitionValueModel(5000),
    Some(AcquisitionCostsModel(200)),
    DateModel(1, 4, 2013),
    Some(RebasedValueModel(7500)),
    Some(RebasedCostsModel("Yes", Some(150))),
    IsClaimingImprovementsModel(true),
    Some(ImprovementsModel(50, Some(25))),
    Some(OtherReliefsModel(1000))
  )

  val modelDateBeforeLegislationStart: TotalGainAnswersModel = TotalGainAnswersModel(
    DateModel(10, 10, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(false)),
    DisposalValueModel(10000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Bought")),
    Some(BoughtForLessModel(false)),
    AcquisitionValueModel(5000),
    Some(AcquisitionCostsModel(200)),
    DateModel(1, 4, 1967),
    Some(RebasedValueModel(7500)),
    Some(RebasedCostsModel("Yes", Some(150))),
    IsClaimingImprovementsModel(true),
    Some(ImprovementsModel(50, Some(25))),
    Some(OtherReliefsModel(1000))
  )

  val totalGainBoughtForLess: TotalGainAnswersModel = TotalGainAnswersModel(
    DateModel(10, 10, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(false)),
    DisposalValueModel(10000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Bought")),
    Some(BoughtForLessModel(true)),
    AcquisitionValueModel(5000),
    Some(AcquisitionCostsModel(200)),
    DateModel(1, 4, 2013),
    Some(RebasedValueModel(7500)),
    Some(RebasedCostsModel("Yes", Some(150))),
    IsClaimingImprovementsModel(true),
    Some(ImprovementsModel(50, Some(25))),
    Some(OtherReliefsModel(1000))
  )

  val totalGainSoldForLess: TotalGainAnswersModel = TotalGainAnswersModel(
    DateModel(10, 10, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(true)),
    DisposalValueModel(11000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Bought")),
    Some(BoughtForLessModel(true)),
    AcquisitionValueModel(5000),
    Some(AcquisitionCostsModel(200)),
    DateModel(1, 4, 2013),
    Some(RebasedValueModel(7500)),
    Some(RebasedCostsModel("Yes", Some(150))),
    IsClaimingImprovementsModel(true),
    Some(ImprovementsModel(50, Some(25))),
    Some(OtherReliefsModel(1000))
  )

  "Calling getNRTotalGainAnswers" should {

    "return a valid TotalGainAnswersModel with no optional values" in {
      val constructor = setupMockedAnswersConstructor(totalGainNoOptionalModel, marketValueAcquisition = Some(AcquisitionValueModel(5000)),
        marketDisposalValue = Some(DisposalValueModel(10000)))
      val result = constructor.getNRTotalGainAnswers(using FakeRequest())

      await(result) shouldBe totalGainNoOptionalModel
    }

    "return a valid TotalGainAnswersModel with all optional values" in {
      val constructor = setupMockedAnswersConstructor(totalGainAllOptionalModel)
      val result = constructor.getNRTotalGainAnswers(using FakeRequest())

      await(result) shouldBe totalGainAllOptionalModel
    }

    "return a valid acquisition value of 4000 with an acquisition date before legislation start" in {
      val constructor = setupMockedAnswersConstructor(modelDateBeforeLegislationStart,
        worthBeforeLegislationStartModel = Some(WorthBeforeLegislationStartModel(4000)))
      val result = constructor.getNRTotalGainAnswers(using FakeRequest())

      await(result).acquisitionValueModel.acquisitionValueAmt shouldBe 4000
    }

    "return a valid acquisition value of 3000 with an property acquired without purchasing" in {
      val constructor = setupMockedAnswersConstructor(totalGainNoOptionalModel, marketValueAcquisition = Some(AcquisitionValueModel(3000)),
        marketDisposalValue = Some(DisposalValueModel(10000)))
      val result = constructor.getNRTotalGainAnswers(using FakeRequest())

      await(result).acquisitionValueModel.acquisitionValueAmt shouldBe 3000
    }

    "return a valid acquisition value of 2000 with a property bought for less" in {
      val constructor = setupMockedAnswersConstructor(totalGainBoughtForLess, marketValueAcquisition = Some(AcquisitionValueModel(2000)))
      val result = constructor.getNRTotalGainAnswers(using FakeRequest())

      await(result).acquisitionValueModel.acquisitionValueAmt shouldBe 2000
    }

    "return a valid disposal value of 10000 when sold and not sold for less" in {
      val constructor = setupMockedAnswersConstructor(totalGainAllOptionalModel)
      val result = constructor.getNRTotalGainAnswers(using FakeRequest())

      await(result).disposalValueModel.disposalValue shouldBe 10000
    }

    "return a valid disposal value of 11000 when sold and sold for less" in {
      val constructor = setupMockedAnswersConstructor(totalGainSoldForLess, marketValueAcquisition = Some(AcquisitionValueModel(5000)),
        marketDisposalValue = Some(DisposalValueModel(11000)))
      val result = constructor.getNRTotalGainAnswers(using FakeRequest())

      await(result).disposalValueModel.disposalValue shouldBe 11000
    }

    "return a valid disposal value of 10000 when given away" in {
      val constructor = setupMockedAnswersConstructor(totalGainNoOptionalModel, marketValueAcquisition = Some(AcquisitionValueModel(5000)),
        marketDisposalValue = Some(DisposalValueModel(10000)))
      val result = constructor.getNRTotalGainAnswers(using FakeRequest())

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

    val modelWithError = model.copy(
      CurrentIncomeModel(null)
    )

    val constructor = setupMockedFinalAnswersConstructor(model)

    "when called with the model with all options return all options" in {
      await(constructor.getPersonalDetailsAndPreviousCapitalGainsAnswers(using FakeRequest())) shouldEqual Some(model)
    }

    "when called model with null current income it throws exception" in {
      intercept[SessionExpiredException](
        await(setupMockedFinalAnswersConstructor(modelWithError).
          getPersonalDetailsAndPreviousCapitalGainsAnswers(using FakeRequest())
        )
      )
    }
  }
}
