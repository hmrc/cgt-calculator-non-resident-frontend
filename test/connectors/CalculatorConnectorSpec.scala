/*
 * Copyright 2021 HM Revenue & Customs
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
import config.ApplicationConfig
import models._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}
import uk.gov.hmrc.http.logging.SessionId
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class CalculatorConnectorSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  val mockHttp         = mock[DefaultHttpClient]
  val mockSessionCache = mock[SessionCache]
  val mockServiceConf  = mock[ServicesConfig]
  val mockConfig       = fakeApplication.injector.instanceOf[ApplicationConfig]
  val sessionId        = UUID.randomUUID.toString

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionId.toString)))

  class Setup {
    val connector = new CalculatorConnector(mockHttp, mockConfig, mockServiceConf)
  }

  "Calculator Connector" should {

    "fetch and get from keystore" in new Setup {
      val validCacheMap = CacheMap("id", Map(KeystoreKeys.disposalValue -> Json.toJson(DisposalValueModel(1000))))

      when(mockHttp.GET[CacheMap](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(validCacheMap))

      val result = connector.fetchAndGetFormData[DisposalValueModel](KeystoreKeys.disposalValue)
      await(result) shouldBe Some(DisposalValueModel(1000))
    }

    "save data to keystore" in new Setup {
      val testModel = DisposalValueModel(1000)
      val returnedCacheMap = CacheMap(KeystoreKeys.disposalValue, Map("data" -> Json.toJson(testModel)))

      when(mockHttp.PUT[DisposalValueModel, CacheMap](
        ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
        (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(returnedCacheMap))

      lazy val result = connector.saveFormData(KeystoreKeys.disposalValue, testModel)
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

    "return a valid response" in new Setup {
      when(mockHttp.POST[TotalGainAnswersModel, Option[TotalGainResultsModel]](ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(
        ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
      ).thenReturn(Future.successful(Option(validResponse)))

      val result = connector.calculateTotalGain(model)

      await(result) shouldBe Some(validResponse)
    }
  }

  "Calling calculateTaxableGainAfterPRR" should {

    val validResponse = CalculationResultsWithPRRModel(
      GainsAfterPRRModel(
        BigDecimal(10.0),
        BigDecimal(20.0),
        BigDecimal(30.0)
      ),
      None,
      None
    )
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


    "return a valid response" in new Setup {
      when(mockHttp.GET[Option[CalculationResultsWithPRRModel]](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Option(validResponse)))

      val result = connector.calculateTaxableGainAfterPRR(model, PrivateResidenceReliefModel("No", None, None),
        PropertyLivedInModel(true))

      await(result) shouldBe Some(validResponse)
    }

  }
}