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

package connectors

import java.util.UUID

import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import config.WSHttp
import models._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet, HttpPost}
import uk.gov.hmrc.http.logging.SessionId

import scala.concurrent.ExecutionContext.Implicits.global

class CalculatorConnectorSpec extends UnitSpec with MockitoSugar {

  val mockHttp: HttpPost with HttpGet= mock[WSHttp]
  val mockSessionCache: SessionCache = mock[SessionCache]
  val sessionId: String = UUID.randomUUID.toString

  object TargetCalculatorConnector extends CalculatorConnector {
    override val sessionCache: SessionCache = mockSessionCache
    override val http: HttpPost with HttpGet = mockHttp
    override val serviceUrl = "dummy"
  }

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionId.toString)))

  def mockFetchAndGetFormData(summary: SummaryModel,
                              calculationElectionModel: Option[CalculationElectionModel],
                              otherReliefsModel: Option[OtherReliefsModel]): OngoingStubbing[Future[Option[PrivateResidenceReliefModel]]] = {

    when(mockSessionCache.fetchAndGetEntry[CurrentIncomeModel](ArgumentMatchers.eq(KeystoreKeys.currentIncome))(ArgumentMatchers.any(), ArgumentMatchers.any(),
      ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(summary.currentIncomeModel)))

    when(mockSessionCache.fetchAndGetEntry[PersonalAllowanceModel](ArgumentMatchers.eq(KeystoreKeys.personalAllowance))(ArgumentMatchers.any(),
      ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(summary.personalAllowanceModel))

    when(mockSessionCache.fetchAndGetEntry[OtherPropertiesModel](ArgumentMatchers.eq(KeystoreKeys.otherProperties))(ArgumentMatchers.any(),
      ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(Some(summary.otherPropertiesModel)))

    when(mockSessionCache.fetchAndGetEntry[AnnualExemptAmountModel](ArgumentMatchers.eq(KeystoreKeys.annualExemptAmount))(ArgumentMatchers.any(),
      ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(summary.annualExemptAmountModel))

    when(mockSessionCache.fetchAndGetEntry[DateModel](ArgumentMatchers.eq(KeystoreKeys.acquisitionDate))(ArgumentMatchers.any(),
      ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(Some(summary.acquisitionDateModel)))

    when(mockSessionCache.fetchAndGetEntry[AcquisitionValueModel](ArgumentMatchers.eq(KeystoreKeys.acquisitionValue))(ArgumentMatchers.any(),
      ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(Some(summary.acquisitionValueModel)))

    when(mockSessionCache.fetchAndGetEntry[RebasedValueModel](ArgumentMatchers.eq(KeystoreKeys.rebasedValue))(ArgumentMatchers.any(), ArgumentMatchers.any(),
      ArgumentMatchers.any()))
      .thenReturn(Future.successful(summary.rebasedValueModel))

    when(mockSessionCache.fetchAndGetEntry[RebasedCostsModel](ArgumentMatchers.eq(KeystoreKeys.rebasedCosts))(ArgumentMatchers.any(), ArgumentMatchers.any(),
      ArgumentMatchers.any()))
      .thenReturn(Future.successful(summary.rebasedCostsModel))

    when(mockSessionCache.fetchAndGetEntry[ImprovementsModel](ArgumentMatchers.eq(KeystoreKeys.improvements))(ArgumentMatchers.any(), ArgumentMatchers.any(),
      ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(summary.improvementsModel)))

    when(mockSessionCache.fetchAndGetEntry[DateModel](ArgumentMatchers.eq(KeystoreKeys.disposalDate))(ArgumentMatchers.any(), ArgumentMatchers.any(),
      ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(summary.disposalDateModel)))

    when(mockSessionCache.fetchAndGetEntry[DisposalValueModel](ArgumentMatchers.eq(KeystoreKeys.disposalValue))(ArgumentMatchers.any(), ArgumentMatchers.any(),
      ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(summary.disposalValueModel)))

    when(mockSessionCache.fetchAndGetEntry[AcquisitionCostsModel](ArgumentMatchers.eq(KeystoreKeys.acquisitionCosts))(ArgumentMatchers.any(),
      ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(Some(summary.acquisitionCostsModel)))

    when(mockSessionCache.fetchAndGetEntry[DisposalCostsModel](ArgumentMatchers.eq(KeystoreKeys.disposalCosts))(ArgumentMatchers.any(), ArgumentMatchers.any(),
      ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(summary.disposalCostsModel)))

    when(mockSessionCache.fetchAndGetEntry[CalculationElectionModel](ArgumentMatchers.eq(KeystoreKeys.calculationElection))(ArgumentMatchers.any(),
      ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(calculationElectionModel))

    when(mockSessionCache.fetchAndGetEntry[OtherReliefsModel](ArgumentMatchers.eq(KeystoreKeys.otherReliefsFlat))(ArgumentMatchers.any(),
      ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(otherReliefsModel))

    when(mockSessionCache.fetchAndGetEntry[OtherReliefsModel](ArgumentMatchers.eq(KeystoreKeys.otherReliefsTA))(ArgumentMatchers.any(), ArgumentMatchers.any(),
      ArgumentMatchers.any()))
      .thenReturn(Future.successful(otherReliefsModel))

    when(mockSessionCache.fetchAndGetEntry[OtherReliefsModel](ArgumentMatchers.eq(KeystoreKeys.otherReliefsRebased))(ArgumentMatchers.any(),
      ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(otherReliefsModel))

    when(mockSessionCache.fetchAndGetEntry[PrivateResidenceReliefModel](ArgumentMatchers.eq(KeystoreKeys.privateResidenceRelief))(ArgumentMatchers.any(),
      ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(summary.privateResidenceReliefModel))
  }

  def setupMockedConnector(totalGainAnswersModel: TotalGainAnswersModel, totalGainResultsModel: Option[TotalGainResultsModel] = None,
                           calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel] = None): CalculatorConnector = {

    val mockSessionCache = mock[SessionCache]

    when(mockSessionCache.fetchAndGetEntry[DateModel](ArgumentMatchers.eq(KeystoreKeys.disposalDate))(ArgumentMatchers.any(), ArgumentMatchers.any(),
      ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.disposalDateModel)))

    when(mockSessionCache.fetchAndGetEntry[DisposalValueModel](ArgumentMatchers.eq(KeystoreKeys.disposalValue))(ArgumentMatchers.any(), ArgumentMatchers.any(),
      ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.disposalValueModel)))

    when(mockSessionCache.fetchAndGetEntry[DisposalCostsModel](ArgumentMatchers.eq(KeystoreKeys.disposalCosts))(ArgumentMatchers.any(), ArgumentMatchers.any(),
      ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.disposalCostsModel)))

    when(mockSessionCache.fetchAndGetEntry[AcquisitionValueModel](ArgumentMatchers.eq(KeystoreKeys.acquisitionValue))(ArgumentMatchers.any(),
      ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(Some(totalGainAnswersModel.acquisitionValueModel)))

    when(mockSessionCache.fetchAndGetEntry[AcquisitionCostsModel](ArgumentMatchers.eq(KeystoreKeys.acquisitionCosts))(ArgumentMatchers.any(),
      ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(Some(totalGainAnswersModel.acquisitionCostsModel.get)))

    when(mockSessionCache.fetchAndGetEntry[DateModel](ArgumentMatchers.eq(KeystoreKeys.acquisitionDate))(ArgumentMatchers.any(),
      ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(Some(totalGainAnswersModel.acquisitionDateModel)))

    when(mockSessionCache.fetchAndGetEntry[RebasedValueModel](ArgumentMatchers.eq(KeystoreKeys.rebasedValue))(ArgumentMatchers.any(), ArgumentMatchers.any(),
      ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.rebasedValueModel))

    when(mockSessionCache.fetchAndGetEntry[RebasedCostsModel](ArgumentMatchers.eq(KeystoreKeys.rebasedCosts))(ArgumentMatchers.any(), ArgumentMatchers.any(),
      ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.rebasedCostsModel))

    when(mockSessionCache.fetchAndGetEntry[ImprovementsModel](ArgumentMatchers.eq(KeystoreKeys.improvements))(ArgumentMatchers.any(), ArgumentMatchers.any(),
      ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.improvementsModel)))

    if (totalGainResultsModel.isDefined) {
      when(mockHttp.POST[TotalGainAnswersModel, Option[TotalGainResultsModel]](ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(
        ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
      ).thenReturn(Future.successful(totalGainResultsModel))
    }

    if (calculationResultsWithPRRModel.isDefined) {
      when(mockHttp.GET[Option[CalculationResultsWithPRRModel]](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(),
        ArgumentMatchers.any()))
        .thenReturn(calculationResultsWithPRRModel)
    }

    new CalculatorConnector {
      override val sessionCache: SessionCache = mockSessionCache
      override val http: HttpPost with HttpGet = mockHttp
      override val serviceUrl: String = ""
    }
  }

  val sumModelFlat = SummaryModel(
    CurrentIncomeModel(1000),
    Some(PersonalAllowanceModel(11100)),
    OtherPropertiesModel("No"),
    None,
    DateModel(1, 1, 2016),
    AcquisitionValueModel(100000),
    None,
    None,
    ImprovementsModel("No", None),
    DateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(0),
    DisposalCostsModel(0),
    CalculationElectionModel("flat"),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    None
  )

  val sumModelTA = SummaryModel(
    CurrentIncomeModel(1000),
    Some(PersonalAllowanceModel(11100)),
    OtherPropertiesModel("No"),
    None,
    DateModel(9, 9, 1999),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(1000)),
    None,
    ImprovementsModel("No", None),
    DateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(0),
    DisposalCostsModel(0),
    CalculationElectionModel("time-apportioned-calculation"),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val sumModelRebased = SummaryModel(
    CurrentIncomeModel(1000),
    Some(PersonalAllowanceModel(11100)),
    OtherPropertiesModel("No"),
    None,
    DateModel(9, 9, 1999),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(1000)),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel("No", None),
    DateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(0),
    DisposalCostsModel(0),
    CalculationElectionModel("rebased"),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val sumModelFlatDefaulted = SummaryModel(
    CurrentIncomeModel(1000),
    Some(PersonalAllowanceModel(11100)),
    OtherPropertiesModel("No"),
    None,
    DateModel(1, 1, 2016),
    AcquisitionValueModel(100000),
    None,
    None,
    ImprovementsModel("No", None),
    DateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(0),
    DisposalCostsModel(0),
    CalculationElectionModel(""),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    None
  )

  "Calculator Connector" should {

    "fetch and get from keystore" in {
      val testModel = DisposalValueModel(1000)
      when(mockSessionCache.fetchAndGetEntry[DisposalValueModel](ArgumentMatchers.anyString())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Option(testModel)))

      lazy val result = TargetCalculatorConnector.fetchAndGetFormData[DisposalValueModel](KeystoreKeys.disposalValue)
      await(result) shouldBe Some(testModel)
    }

    "save data to keystore" in {
      val testModel = DisposalValueModel(1000)
      val returnedCacheMap = CacheMap(KeystoreKeys.disposalValue, Map("data" -> Json.toJson(testModel)))
      when(mockSessionCache.cache[DisposalValueModel](ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(returnedCacheMap))

      lazy val result = TargetCalculatorConnector.saveFormData(KeystoreKeys.disposalValue, testModel)
      await(result) shouldBe returnedCacheMap
    }
  }

  "Calling calculateTotalGain" should {

    val validResponse = TotalGainResultsModel(1000, None, None)
    val model = TotalGainAnswersModel(DateModel(5, 10, 2016),
      SoldOrGivenAwayModel(true),
      Some(SoldForLessModel(false)),
      DisposalValueModel(1000),
      DisposalCostsModel(100),
      Some(HowBecameOwnerModel("Gifted")),
      Some(BoughtForLessModel(false)),
      AcquisitionValueModel(2000),
      AcquisitionCostsModel(200),
      DateModel(1, 1, 2016),
      None,
      None,
      ImprovementsModel("Yes", Some(10), Some(20)),
      None)
    val target = setupMockedConnector(model, totalGainResultsModel = Some(validResponse))

    "return a valid response" in {

      val result = target.calculateTotalGain(model)

      await(result) shouldBe Some(validResponse)
    }
  }

  "Calling calculateTaxableGainAfterPRR" should {

    val validResponse = mock[CalculationResultsWithPRRModel]
    val model = TotalGainAnswersModel(DateModel(5, 10, 2016),
      SoldOrGivenAwayModel(true),
      Some(SoldForLessModel(false)),
      DisposalValueModel(1000),
      DisposalCostsModel(100),
      Some(HowBecameOwnerModel("Gifted")),
      Some(BoughtForLessModel(false)),
      AcquisitionValueModel(2000),
      AcquisitionCostsModel(200),
      DateModel(1, 1, 2016),
      None,
      None,
      ImprovementsModel("Yes", Some(10), Some(20)),
      None)
    val target = setupMockedConnector(model, calculationResultsWithPRRModel = Some(validResponse))

    "return a valid response" in {
      val result = target.calculateTaxableGainAfterPRR(model, PrivateResidenceReliefModel("No", None, None),
        PropertyLivedInModel(true))

      await(result) shouldBe Some(validResponse)
    }

  }
}
