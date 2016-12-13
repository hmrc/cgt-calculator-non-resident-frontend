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

package connectors

import java.util.UUID

import common.KeystoreKeys
import common.nonresident.CustomerTypeKeys
import models.nonresident._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet}
import uk.gov.hmrc.play.http.logging.SessionId
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class CalculatorConnectorSpec extends UnitSpec with MockitoSugar {

  val mockHttp = mock[HttpGet]
  val mockSessionCache = mock[SessionCache]
  val sessionId = UUID.randomUUID.toString

  object TargetCalculatorConnector extends CalculatorConnector {
    override val sessionCache = mockSessionCache
    override val http = mockHttp
    override val serviceUrl = "dummy"
  }

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionId.toString)))

  def mockFetchAndGetFormData (summary: SummaryModel, calculationElectionModel: Option[CalculationElectionModel], otherReliefsModel: Option[OtherReliefsModel]) = {
    when(mockSessionCache.fetchAndGetEntry[CustomerTypeModel](Matchers.eq(KeystoreKeys.customerType))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(summary.customerTypeModel)))

    when(mockSessionCache.fetchAndGetEntry[DisabledTrusteeModel](Matchers.eq(KeystoreKeys.disabledTrustee))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(summary.disabledTrusteeModel))

    when(mockSessionCache.fetchAndGetEntry[CurrentIncomeModel](Matchers.eq(KeystoreKeys.currentIncome))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(summary.currentIncomeModel))

    when(mockSessionCache.fetchAndGetEntry[PersonalAllowanceModel](Matchers.eq(KeystoreKeys.personalAllowance))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(summary.personalAllowanceModel))

    when(mockSessionCache.fetchAndGetEntry[OtherPropertiesModel](Matchers.eq(KeystoreKeys.otherProperties))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(summary.otherPropertiesModel)))

    when(mockSessionCache.fetchAndGetEntry[AnnualExemptAmountModel](Matchers.eq(KeystoreKeys.annualExemptAmount))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(summary.annualExemptAmountModel))

    when(mockSessionCache.fetchAndGetEntry[AcquisitionDateModel](Matchers.eq(KeystoreKeys.acquisitionDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(summary.acquisitionDateModel)))

    when(mockSessionCache.fetchAndGetEntry[AcquisitionValueModel](Matchers.eq(KeystoreKeys.acquisitionValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(summary.acquisitionValueModel)))

    when(mockSessionCache.fetchAndGetEntry[RebasedValueModel](Matchers.eq(KeystoreKeys.rebasedValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(summary.rebasedValueModel))

    when(mockSessionCache.fetchAndGetEntry[RebasedCostsModel](Matchers.eq(KeystoreKeys.rebasedCosts))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(summary.rebasedCostsModel))

    when(mockSessionCache.fetchAndGetEntry[ImprovementsModel](Matchers.eq(KeystoreKeys.improvements))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(summary.improvementsModel)))

    when(mockSessionCache.fetchAndGetEntry[DisposalDateModel](Matchers.eq(KeystoreKeys.disposalDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(summary.disposalDateModel)))

    when(mockSessionCache.fetchAndGetEntry[DisposalValueModel](Matchers.eq(KeystoreKeys.disposalValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(summary.disposalValueModel)))

    when(mockSessionCache.fetchAndGetEntry[AcquisitionCostsModel](Matchers.eq(KeystoreKeys.acquisitionCosts))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(summary.acquisitionCostsModel)))

    when(mockSessionCache.fetchAndGetEntry[DisposalCostsModel](Matchers.eq(KeystoreKeys.disposalCosts))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(summary.disposalCostsModel)))

    when(mockSessionCache.fetchAndGetEntry[AllowableLossesModel](Matchers.eq(KeystoreKeys.allowableLosses))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(summary.allowableLossesModel)))

    when(mockSessionCache.fetchAndGetEntry[CalculationElectionModel](Matchers.eq(KeystoreKeys.calculationElection))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(calculationElectionModel))

    when(mockSessionCache.fetchAndGetEntry[OtherReliefsModel](Matchers.eq(KeystoreKeys.otherReliefsFlat))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(otherReliefsModel))

    when(mockSessionCache.fetchAndGetEntry[OtherReliefsModel](Matchers.eq(KeystoreKeys.otherReliefsTA))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(otherReliefsModel))

    when(mockSessionCache.fetchAndGetEntry[OtherReliefsModel](Matchers.eq(KeystoreKeys.otherReliefsRebased))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(otherReliefsModel))

    when(mockSessionCache.fetchAndGetEntry[PrivateResidenceReliefModel](Matchers.eq(KeystoreKeys.privateResidenceRelief))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(summary.privateResidenceReliefModel))
  }

  def setupMockedConnector(totalGainAnswersModel: TotalGainAnswersModel, totalGainResultsModel: Option[TotalGainResultsModel] = None,
                           calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel] = None): CalculatorConnector = {

    val mockSessionCache = mock[SessionCache]
    val mockHttpGet = mock[HttpGet]

    when(mockSessionCache.fetchAndGetEntry[DisposalDateModel](Matchers.eq(KeystoreKeys.disposalDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.disposalDateModel)))

    when(mockSessionCache.fetchAndGetEntry[DisposalValueModel](Matchers.eq(KeystoreKeys.disposalValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.disposalValueModel)))

    when(mockSessionCache.fetchAndGetEntry[DisposalCostsModel](Matchers.eq(KeystoreKeys.disposalCosts))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.disposalCostsModel)))

    when(mockSessionCache.fetchAndGetEntry[AcquisitionValueModel](Matchers.eq(KeystoreKeys.acquisitionValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.acquisitionValueModel)))

    when(mockSessionCache.fetchAndGetEntry[AcquisitionCostsModel](Matchers.eq(KeystoreKeys.acquisitionCosts))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.acquisitionCostsModel)))

    when(mockSessionCache.fetchAndGetEntry[AcquisitionDateModel](Matchers.eq(KeystoreKeys.acquisitionDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.acquisitionDateModel)))

    when(mockSessionCache.fetchAndGetEntry[RebasedValueModel](Matchers.eq(KeystoreKeys.rebasedValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.rebasedValueModel))

    when(mockSessionCache.fetchAndGetEntry[RebasedCostsModel](Matchers.eq(KeystoreKeys.rebasedCosts))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.rebasedCostsModel))

    when(mockSessionCache.fetchAndGetEntry[ImprovementsModel](Matchers.eq(KeystoreKeys.improvements))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.improvementsModel)))

    if (totalGainResultsModel.isDefined) {
      when(mockHttpGet.GET[Option[TotalGainResultsModel]](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(totalGainResultsModel)
    }

    if (calculationResultsWithPRRModel.isDefined) {
      when(mockHttpGet.GET[Option[CalculationResultsWithPRRModel]](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(calculationResultsWithPRRModel)
    }

    new CalculatorConnector {
      override val sessionCache: SessionCache = mockSessionCache
      override val http: HttpGet = mockHttpGet
      override val serviceUrl: String = ""
    }
  }

  val sumModelFlat = SummaryModel(
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(11100)),
    OtherPropertiesModel("No"),
    None,
    AcquisitionDateModel("No", None, None, None),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(None)),
    None,
    ImprovementsModel("No", None),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(0),
    DisposalCostsModel(0),
    AllowableLossesModel("No", None),
    CalculationElectionModel("flat"),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    None
  )

  val sumModelTA = SummaryModel(
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(11100)),
    OtherPropertiesModel("No"),
    None,
    AcquisitionDateModel("Yes", Some(9), Some(9), Some(1999)),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(None)),
    None,
    ImprovementsModel("No", None),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(0),
    DisposalCostsModel(0),
    AllowableLossesModel("No", None),
    CalculationElectionModel("time-apportioned-calculation"),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val sumModelRebased = SummaryModel(
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(11100)),
    OtherPropertiesModel("No"),
    None,
    AcquisitionDateModel("Yes", Some(9), Some(9), Some(1999)),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(Some(1000))),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel("No", None),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(0),
    DisposalCostsModel(0),
    AllowableLossesModel("No", None),
    CalculationElectionModel("rebased"),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val sumModelFlatDefaulted = SummaryModel(
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(11100)),
    OtherPropertiesModel("No"),
    None,
    AcquisitionDateModel("No", None, None, None),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(None)),
    None,
    ImprovementsModel("No", None),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(0),
    DisposalCostsModel(0),
    AllowableLossesModel("No", None),
    CalculationElectionModel(""),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    None
  )

  "Calculator Connector" should {

    "fetch and get from keystore" in {
      val testModel = CustomerTypeModel(CustomerTypeKeys.trustee)
      when(mockSessionCache.fetchAndGetEntry[CustomerTypeModel](Matchers.anyString())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(testModel)))

      lazy val result = TargetCalculatorConnector.fetchAndGetFormData[CustomerTypeModel](KeystoreKeys.customerType)
      await(result) shouldBe Some(testModel)
    }

    "save data to keystore" in {
      val testModel = CustomerTypeModel(CustomerTypeKeys.trustee)
      val returnedCacheMap = CacheMap(KeystoreKeys.customerType, Map("data" -> Json.toJson(testModel)))
      when(mockSessionCache.cache[CustomerTypeModel](Matchers.anyString(), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnedCacheMap))

      lazy val result = TargetCalculatorConnector.saveFormData(KeystoreKeys.customerType, testModel)
      await(result) shouldBe returnedCacheMap
    }
  }

  "Calling calculateTotalGain" should {

    val validResponse = TotalGainResultsModel(1000, None, None)
    val model = TotalGainAnswersModel(DisposalDateModel(5, 10, 2016),
      SoldOrGivenAwayModel(true),
      Some(SoldForLessModel(false)),
      DisposalValueModel(1000),
      DisposalCostsModel(100),
      Some(HowBecameOwnerModel("Gifted")),
      Some(BoughtForLessModel(false)),
      AcquisitionValueModel(2000),
      AcquisitionCostsModel(200),
      AcquisitionDateModel("No", None, None, None),
      Some(RebasedValueModel(None)),
      Some(RebasedCostsModel("No", None)),
      ImprovementsModel("Yes", Some(10), Some(20)),
      None)
    val target = setupMockedConnector(model, Some(validResponse))

    "return a valid response" in {

      val result = target.calculateTotalGain(model)

      await(result) shouldBe Some(validResponse)
    }
  }

  "Calling calculateTaxableGainAfterPRR" should {

    val validResponse = mock[CalculationResultsWithPRRModel]
    val model = TotalGainAnswersModel(DisposalDateModel(5, 10, 2016),
      SoldOrGivenAwayModel(true),
      Some(SoldForLessModel(false)),
      DisposalValueModel(1000),
      DisposalCostsModel(100),
      Some(HowBecameOwnerModel("Gifted")),
      Some(BoughtForLessModel(false)),
      AcquisitionValueModel(2000),
      AcquisitionCostsModel(200),
      AcquisitionDateModel("No", None, None, None),
      Some(RebasedValueModel(None)),
      Some(RebasedCostsModel("No", None)),
      ImprovementsModel("Yes", Some(10), Some(20)),
      None)
    val target = setupMockedConnector(model, calculationResultsWithPRRModel = Some(validResponse))

    "return a valid response" in {
      val result = target.calculateTaxableGainAfterPRR(model, PrivateResidenceReliefModel("No", None, None))

      await(result) shouldBe Some(validResponse)
    }

  }

  "Calling calculateFlat" should {

    val validResponse = CalculationResultModel(8000, 40000, 32000, 18, 0, Some(8000), Some(28), None)
    when(mockHttp.GET[Option[CalculationResultModel]](Matchers.anyString())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(validResponse)))

    "return a valid response" in {
      val testModel: SummaryModel = sumModelFlat
      val result = TargetCalculatorConnector.calculateFlat(testModel)
      await(result) shouldBe Some(validResponse)
    }
  }

  "Calling calculateTA" should {
    val validResponse = CalculationResultModel(8000, 40000, 32000, 18, 0, Some(8000), Some(28), None)
    when(mockHttp.GET[Option[CalculationResultModel]](Matchers.anyString())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(validResponse)))

    "return a valid response" in {
      val testModel: SummaryModel = sumModelTA
      val result = TargetCalculatorConnector.calculateTA(testModel)
      await(result) shouldBe Some(validResponse)
    }
  }

  "Calling calculateRebased" should {
    val validResponse = CalculationResultModel(8000, 40000, 32000, 18, 0, Some(8000), Some(28), None)
    when(mockHttp.GET[Option[CalculationResultModel]](Matchers.anyString())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(validResponse)))

    "return a valid response" in {
      val testModel: SummaryModel = sumModelRebased
      val result = TargetCalculatorConnector.calculateRebased(testModel)
      await(result) shouldBe Some(validResponse)
    }
  }
}