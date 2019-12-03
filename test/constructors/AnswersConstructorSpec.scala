/*
 * Copyright 2019 HM Revenue & Customs
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

import akka.stream.Materializer
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import connectors.CalculatorConnector
import controllers.PrivateResidenceReliefController
import models._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.Environment
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.logging.SessionId
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

class AnswersConstructorSpec extends UnitSpec with MockitoSugar {

  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))

  val materializer = mock[Materializer]
  val mockCalcConnector =mock[CalculatorConnector]


  class Setup {
    val controller = new AnswersConstructor(
      mockCalcConnector
    )
  }

  def setupMockedAnswersConstructor(totalGainAnswersModel: TotalGainAnswersModel,
                                    worthBeforeLegislationStartModel: Option[WorthBeforeLegislationStartModel] = None,
                                    marketValueAcquisition: Option[AcquisitionValueModel] = None,
                                    marketDisposalValue: Option[DisposalValueModel] = None): AnswersConstructor = {


    when(mockCalcConnector.fetchAndGetFormData[DateModel](ArgumentMatchers.eq(KeystoreKeys.disposalDate))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.disposalDateModel)))

    when(mockCalcConnector.fetchAndGetFormData[SoldOrGivenAwayModel](
      ArgumentMatchers.eq(KeystoreKeys.soldOrGivenAway))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.soldOrGivenAwayModel)))

    when(mockCalcConnector.fetchAndGetFormData[SoldForLessModel](ArgumentMatchers.eq(KeystoreKeys.soldForLess))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.soldForLessModel))

    when(mockCalcConnector.fetchAndGetFormData[DisposalValueModel](ArgumentMatchers.eq(KeystoreKeys.disposalValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.disposalValueModel)))

    when(mockCalcConnector.fetchAndGetFormData[DisposalValueModel](
      ArgumentMatchers.eq(KeystoreKeys.disposalMarketValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(marketDisposalValue))

    when(mockCalcConnector.fetchAndGetFormData[DisposalCostsModel](ArgumentMatchers.eq(KeystoreKeys.disposalCosts))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.disposalCostsModel)))

    when(mockCalcConnector.fetchAndGetFormData[HowBecameOwnerModel](
      ArgumentMatchers.eq(KeystoreKeys.howBecameOwner))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.howBecameOwnerModel))

    when(mockCalcConnector.fetchAndGetFormData[BoughtForLessModel](ArgumentMatchers.eq(KeystoreKeys.boughtForLess))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.boughtForLessModel))

    when(mockCalcConnector.fetchAndGetFormData[BigDecimal]
      (ArgumentMatchers.eq(KeystoreKeys.costAtLegislationStart))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(None))

    when(mockCalcConnector.fetchAndGetFormData[WorthBeforeLegislationStartModel](ArgumentMatchers.eq(KeystoreKeys.worthBeforeLegislationStart))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(worthBeforeLegislationStartModel))
    when(mockCalcConnector.fetchAndGetFormData[AcquisitionValueModel](
      ArgumentMatchers.eq(KeystoreKeys.acquisitionMarketValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(marketValueAcquisition))

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionValueModel](
      ArgumentMatchers.eq(KeystoreKeys.acquisitionValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.acquisitionValueModel)))

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionCostsModel](
      ArgumentMatchers.eq(KeystoreKeys.acquisitionCosts))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.acquisitionCostsModel.get)))

    when(mockCalcConnector.fetchAndGetFormData[DateModel](
      ArgumentMatchers.eq(KeystoreKeys.acquisitionDate))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.acquisitionDateModel)))

    when(mockCalcConnector.fetchAndGetFormData[RebasedValueModel](ArgumentMatchers.eq(KeystoreKeys.rebasedValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.rebasedValueModel))

    when(mockCalcConnector.fetchAndGetFormData[RebasedCostsModel](ArgumentMatchers.eq(KeystoreKeys.rebasedCosts))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.rebasedCostsModel))

    when(mockCalcConnector.fetchAndGetFormData[ImprovementsModel](ArgumentMatchers.eq(KeystoreKeys.improvements))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.improvementsModel)))

    when(mockCalcConnector.fetchAndGetFormData[OtherReliefsModel](
      ArgumentMatchers.eq(KeystoreKeys.otherReliefsFlat))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.otherReliefsFlat))

    new AnswersConstructor(mockCalcConnector)
  }

  def setupMockedFinalAnswersConstructor(totalPersonalDetailsCalculationModel: TotalPersonalDetailsCalculationModel): AnswersConstructor = {

    when(mockCalcConnector.fetchAndGetFormData[CurrentIncomeModel](ArgumentMatchers.eq(KeystoreKeys.currentIncome))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalPersonalDetailsCalculationModel.currentIncomeModel)))

    when(mockCalcConnector.fetchAndGetFormData[PersonalAllowanceModel](
      ArgumentMatchers.eq(KeystoreKeys.personalAllowance))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalPersonalDetailsCalculationModel.personalAllowanceModel))

    when(mockCalcConnector.fetchAndGetFormData[OtherPropertiesModel](
      ArgumentMatchers.eq(KeystoreKeys.otherProperties))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalPersonalDetailsCalculationModel.otherPropertiesModel)))

    when(mockCalcConnector.fetchAndGetFormData[PreviousLossOrGainModel](ArgumentMatchers.eq(KeystoreKeys.previousLossOrGain))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalPersonalDetailsCalculationModel.previousGainOrLoss))

    when(mockCalcConnector.fetchAndGetFormData[HowMuchLossModel](ArgumentMatchers.eq(KeystoreKeys.howMuchLoss))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalPersonalDetailsCalculationModel.howMuchLossModel))

    when(mockCalcConnector.fetchAndGetFormData[HowMuchGainModel](ArgumentMatchers.eq(KeystoreKeys.howMuchGain))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalPersonalDetailsCalculationModel.howMuchGainModel))

    when(mockCalcConnector.fetchAndGetFormData[AnnualExemptAmountModel](
      ArgumentMatchers.eq(KeystoreKeys.annualExemptAmount))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalPersonalDetailsCalculationModel.annualExemptAmountModel))

    when(mockCalcConnector.fetchAndGetFormData[BroughtForwardLossesModel](
      ArgumentMatchers.eq(KeystoreKeys.broughtForwardLosses))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalPersonalDetailsCalculationModel.broughtForwardLossesModel)))

    new AnswersConstructor(mockCalcConnector)
  }

  val totalGainNoOptionalModel = TotalGainAnswersModel(
    DateModel(10, 10, 2016),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(10000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    DateModel(1, 1, 2016),
    None,
    None,
    ImprovementsModel("No", None, None),
    None
  )

  val totalGainAllOptionalModel = TotalGainAnswersModel(
    DateModel(10, 10, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(false)),
    DisposalValueModel(10000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Bought")),
    Some(BoughtForLessModel(false)),
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    DateModel(1, 4, 2013),
    Some(RebasedValueModel(7500)),
    Some(RebasedCostsModel("Yes", Some(150))),
    ImprovementsModel("Yes", Some(50), Some(25)),
    Some(OtherReliefsModel(1000))
  )

  val modelDateBeforeLegislationStart = TotalGainAnswersModel(
    DateModel(10, 10, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(false)),
    DisposalValueModel(10000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Bought")),
    Some(BoughtForLessModel(false)),
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    DateModel(1, 4, 1967),
    Some(RebasedValueModel(7500)),
    Some(RebasedCostsModel("Yes", Some(150))),
    ImprovementsModel("Yes", Some(50), Some(25)),
    Some(OtherReliefsModel(1000))
  )

  val totalGainBoughtForLess = TotalGainAnswersModel(
    DateModel(10, 10, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(false)),
    DisposalValueModel(10000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Bought")),
    Some(BoughtForLessModel(true)),
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    DateModel(1, 4, 2013),
    Some(RebasedValueModel(7500)),
    Some(RebasedCostsModel("Yes", Some(150))),
    ImprovementsModel("Yes", Some(50), Some(25)),
    Some(OtherReliefsModel(1000))
  )

  val totalGainSoldForLess = TotalGainAnswersModel(
    DateModel(10, 10, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(true)),
    DisposalValueModel(11000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Bought")),
    Some(BoughtForLessModel(true)),
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    DateModel(1, 4, 2013),
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
