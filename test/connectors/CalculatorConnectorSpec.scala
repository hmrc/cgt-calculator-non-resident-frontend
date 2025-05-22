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

package connectors

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import common.nonresident.{Flat, Rebased}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import models._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

class CalculatorConnectorSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with BeforeAndAfterEach {
  val Port = 11119
  val Host = "localhost"
  val wireMockServer = new WireMockServer(wireMockConfig().port(Port))

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val model = TotalGainAnswersModel(
    disposalDateModel = DateModel(1, 1, 2024),
    soldOrGivenAwayModel = SoldOrGivenAwayModel(soldIt = true),
    soldForLessModel = Some(SoldForLessModel(soldForLess = false)),
    disposalValueModel = DisposalValueModel(300000),
    disposalCostsModel = DisposalCostsModel(0),
    howBecameOwnerModel = Some(HowBecameOwnerModel("Bought It")),
    boughtForLessModel = Some(BoughtForLessModel(boughtForLess = false)),
    acquisitionValueModel = AcquisitionValueModel(100000),
    acquisitionCostsModel = Some(AcquisitionCostsModel(0)),
    acquisitionDateModel = DateModel(1, 1, 2000),
    rebasedValueModel = Some(RebasedValueModel(200000)),
    rebasedCostsModel = Some(RebasedCostsModel(hasRebasedCosts = "No", rebasedCosts = None)),
    isClaimingImprovementsModel = IsClaimingImprovementsModel(isClaimingImprovements = true),
    improvementsModel = Some(ImprovementsModel(351)),
    otherReliefsFlat = Some(OtherReliefsModel(0)),
    costsAtLegislationStart = Some(CostsAtLegislationStartModel(hasCosts = "No", costs = None)),
  )

  private val con = fakeApplication.injector.instanceOf[CalculatorConnector]

  override def beforeEach(): Unit = {
    wireMockServer.start()
    wireMockServer.resetAll()
    WireMock.configureFor(Host, Port)
  }

  override def afterEach(): Unit = {
    wireMockServer.stop()
  }

  private def equalToNumber(number: Int) = {
    matching(s"$number(\\.0)?")
  }

  "calculateTotalGain" should {
    val url = "/capital-gains-calculator/non-resident/calculate-total-gain"
    "return some parsed JSON on success" in {
      val expected = TotalGainResultsModel(200000, Some(100000), Some(72841))
      stubFor(post(urlMatching(".*")).willReturn(aResponse().withStatus(200).withBody(Json.toJson(expected).toString())))

      val response = await(con.calculateTotalGain(model))

      response shouldBe Some(expected)
      verify(
        postRequestedFor(urlEqualTo(url))
          .withHeader("Content-Type", equalTo("application/json"))
          .withRequestBody(equalTo(Json.toJson(model).toString()))
       )
    }

    "return None on 404" in {
      stubFor(post(urlMatching(".*")).willReturn(aResponse().withStatus(404)))

      val response = await(con.calculateTotalGain(model))

      response shouldBe None
      verify(
        postRequestedFor(urlEqualTo(url))
          .withHeader("Content-Type", equalTo("application/json"))
          .withRequestBody(equalTo(Json.toJson(model).toString()))
      )
    }

    "Throw UpstreamErrorResponse on 500" in {
      stubFor(post(urlMatching(".*")).willReturn(aResponse().withStatus(500)))

      assertThrows[UpstreamErrorResponse] {
        await(con.calculateTotalGain(model))
      }

      verify(
        postRequestedFor(urlEqualTo(url))
          .withHeader("Content-Type", equalTo("application/json"))
          .withRequestBody(equalTo(Json.toJson(model).toString()))
      )
    }
  }

  "calculateNRCGTTotalTax" should {
    val prrModel = PrivateResidenceReliefModel(isClaimingPRR = "Yes", prrClaimed = Some(9000))
    val propertyLivedInModel = PropertyLivedInModel(true)
    val total = TotalPersonalDetailsCalculationModel(
      CurrentIncomeModel(20000),
      Some(PersonalAllowanceModel(0)),
      OtherPropertiesModel("Yes"),
      Some(PreviousLossOrGainModel("Neither")),
      None,
      None,
      Some(AnnualExemptAmountModel(0)),
      BroughtForwardLossesModel(isClaiming = false, None)
    )
    val maxAnnualExemptAmount = BigDecimal(9000)
    val otherReliefs = AllOtherReliefsModel(Some(OtherReliefsModel(1000)), Some(OtherReliefsModel(1000)), Some(OtherReliefsModel(1000)))

    val expected = CalculationResultsWithTaxOwedModel(
      TotalTaxOwedModel(100, 100, 20, None, None, 200, 100, None, None, None, None, 0, None, None, None, None, None, None, None),
      None,
      None
    )

    "call the correct endpoint" in {
      stubFor(get(urlPathMatching(".*")).willReturn(aResponse().withStatus(200).withBody(Json.toJson(expected).toString())))

      await(con.calculateNRCGTTotalTax(model, Some(prrModel), Some(propertyLivedInModel), Some(total), maxAnnualExemptAmount, Some(otherReliefs)))

      verify(
        getRequestedFor(urlPathEqualTo("/capital-gains-calculator/non-resident/calculate-tax-owed"))
          .withQueryParam("disposalValue", equalToNumber(300000))
          .withQueryParam("disposalCosts", equalToNumber(0))
          .withQueryParam("acquisitionValue", equalToNumber(100000))
          .withQueryParam("acquisitionCosts", equalToNumber(0))
          .withQueryParam("improvements", equalToNumber(351))
          .withQueryParam("rebasedValue", equalToNumber(200000))
          .withQueryParam("disposalDate", equalTo("2024-1-1"))
          .withQueryParam("acquisitionDate", equalTo("2000-1-1"))
          .withQueryParam("prrClaimed", equalToNumber(9000))
          .withQueryParam("currentIncome", equalToNumber(20000))
          .withQueryParam("personalAllowanceAmt", equalToNumber(0))
          .withQueryParam("annualExemptAmount", equalToNumber(0))
          .withQueryParam("otherReliefsFlat", equalToNumber(1000))
          .withQueryParam("otherReliefsRebased", equalToNumber(1000))
          .withQueryParam("otherReliefsTimeApportioned", equalToNumber(1000)))
    }
    "return some parsed JSON on success" in {
      stubFor(get(urlPathMatching(".*")).willReturn(aResponse().withStatus(200).withBody(Json.toJson(expected).toString())))

      val response = await(con.calculateNRCGTTotalTax(model, Some(prrModel), Some(propertyLivedInModel), Some(total), maxAnnualExemptAmount, Some(otherReliefs)))

      response shouldBe Some(expected)
    }

    "return None on 404" in {
      stubFor(get(urlPathMatching(".*")).willReturn(aResponse().withStatus(404)))

      val response = await(con.calculateNRCGTTotalTax(model, Some(prrModel), Some(propertyLivedInModel), Some(total), maxAnnualExemptAmount, Some(otherReliefs)))

      response shouldBe None
    }

    "Throw UpstreamErrorResponse on 500" in {
      stubFor(get(urlPathMatching(".*")).willReturn(aResponse().withStatus(500)))

      assertThrows[UpstreamErrorResponse] {
        await(con.calculateNRCGTTotalTax(model, Some(prrModel), Some(propertyLivedInModel), Some(total), maxAnnualExemptAmount, Some(otherReliefs)))
      }
    }
  }

  "calculateTaxableGainAfterPRR" should {
    val prrModel = PrivateResidenceReliefModel(isClaimingPRR = "Yes", prrClaimed = Some(9000))
    val propertyLivedInModel = PropertyLivedInModel(true)

    "call the correct endpoint" in {
      val expected = CalculationResultsWithPRRModel(
        GainsAfterPRRModel(
          BigDecimal(10.0),
          BigDecimal(20.0),
          BigDecimal(30.0)
        ),
        None,
        None
      )
      stubFor(get(urlPathMatching(".*")).willReturn(aResponse().withStatus(200).withBody(Json.toJson(expected).toString())))

      await(con.calculateTaxableGainAfterPRR(model, prrModel, propertyLivedInModel))

      verify(getRequestedFor(urlPathEqualTo("/capital-gains-calculator/non-resident/calculate-gain-after-prr"))
        .withQueryParam("disposalValue", equalToNumber(300000))
        .withQueryParam("disposalCosts", equalToNumber(0))
        .withQueryParam("acquisitionValue", equalToNumber(100000))
        .withQueryParam("acquisitionCosts", equalToNumber(0))
        .withQueryParam("improvements", equalToNumber(351))
        .withQueryParam("rebasedValue", equalToNumber(200000))
        .withQueryParam("disposalDate", equalTo("2024-1-1"))
        .withQueryParam("acquisitionDate", equalTo("2000-1-1"))
        .withQueryParam("prrClaimed", equalToNumber(9000))
      )
    }

    "return some parsed JSON on success" in {
      val expected = CalculationResultsWithPRRModel(
        GainsAfterPRRModel(
          BigDecimal(10.0),
          BigDecimal(20.0),
          BigDecimal(30.0)
        ),
        None,
        None
      )
      stubFor(get(urlPathMatching(".*")).willReturn(aResponse().withStatus(200).withBody(Json.toJson(expected).toString())))

      val response = await(con.calculateTaxableGainAfterPRR(model, prrModel, propertyLivedInModel))

      response shouldBe Some(expected)
    }

    "return None on 404" in {
      stubFor(get(urlPathMatching(".*")).willReturn(aResponse().withStatus(404)))

      val response = await(con.calculateTaxableGainAfterPRR(model, prrModel, propertyLivedInModel))

      response shouldBe None
    }

    "Throw UpstreamErrorResponse on 500" in {
      stubFor(get(urlPathMatching(".*")).willReturn(aResponse().withStatus(500)))

      assertThrows[UpstreamErrorResponse] {
        await(con.calculateTaxableGainAfterPRR(model, prrModel, propertyLivedInModel))
      }
    }
  }

  "getFullAEA" should {
    val url = "/capital-gains-calculator/tax-rates-and-bands/max-full-aea?taxYear=2024"
    "return some parsed JSON on success" in {
      val expected = BigDecimal(49000)
      stubFor(get(urlPathMatching(".*")).willReturn(aResponse().withStatus(200).withBody(Json.toJson(expected).toString())))

      val response = await(con.getFullAEA(2024))

      response shouldBe Some(expected)
      verify(getRequestedFor(urlEqualTo(url)))
    }

    "return None on 404" in {
      stubFor(get(urlPathMatching(".*")).willReturn(aResponse().withStatus(404)))

      val response = await(con.getFullAEA(2024))

      response shouldBe None
      verify(getRequestedFor(urlEqualTo(url)))
    }

    "Throw UpstreamErrorResponse on 500" in {
      stubFor(get(urlPathMatching(".*")).willReturn(aResponse().withStatus(500)))

      assertThrows[UpstreamErrorResponse] {
        await(con.getFullAEA(2024))
      }

      verify(getRequestedFor(urlEqualTo(url)))
    }
  }

  "getPA" should {
    val url = "/capital-gains-calculator/tax-rates-and-bands/max-pa?taxYear=2024"
    "return some number on success" in {
      val expected = BigDecimal(49000)
      stubFor(get(urlPathMatching(".*")).willReturn(aResponse().withStatus(200).withBody(Json.toJson(expected).toString())))

      val response = await(con.getPA(2024))

      response shouldBe Some(expected)
      verify(getRequestedFor(urlEqualTo(url)))
    }

    "Add extra parameter when isEligibleBlindPersonsAllowance is true" in {
      val expected = BigDecimal(49000)
      stubFor(get(urlPathMatching(".*")).willReturn(aResponse().withStatus(200).withBody(Json.toJson(expected).toString())))

      val response = await(con.getPA(2024, isEligibleBlindPersonsAllowance = true))

      response shouldBe Some(expected)
      verify(getRequestedFor(urlEqualTo(s"$url&isEligibleBlindPersonsAllowance=true")))
    }

    "return None on 404" in {
      stubFor(get(urlPathMatching(".*")).willReturn(aResponse().withStatus(404)))

      val response = await(con.getPA(2024))

      response shouldBe None
      verify(getRequestedFor(urlEqualTo(url)))
    }

    "Throw UpstreamErrorResponse on 500" in {
      stubFor(get(urlPathMatching(".*")).willReturn(aResponse().withStatus(500)))

      assertThrows[UpstreamErrorResponse] {
        await(con.getPA(2024))
      }

      verify(getRequestedFor(urlEqualTo(url)))
    }
  }

  "calculateTotalCosts" should {
    "return some parsed JSON on success + flat" in {
      val url = "/capital-gains-calculator/non-resident/calculate-total-costs?disposalCosts=0.0&acquisitionCosts=0.0&improvements=351.0"
      val expected = BigDecimal("4096")
      stubFor(get(urlPathMatching(".*")).willReturn(aResponse().withStatus(200).withBody(Json.toJson(expected).toString())))

      val response = await(con.calculateTotalCosts(model, Some(CalculationElectionModel(Flat))))

      response shouldBe expected
      verify(getRequestedFor(urlEqualTo(url)))
    }

    "return some parsed JSON on success + rebased. Also call endpoint with improvements=0" in {
      val url = "/capital-gains-calculator/non-resident/calculate-total-costs?disposalCosts=0.0&acquisitionCosts=0.0&improvements=0.0"
      val expected = BigDecimal("4096")
      stubFor(get(urlPathMatching(".*")).willReturn(aResponse().withStatus(200).withBody(Json.toJson(expected).toString())))

      val response = await(con.calculateTotalCosts(model, Some(CalculationElectionModel(Rebased))))

      response shouldBe expected
      verify(getRequestedFor(urlEqualTo(url)))
    }

    "Throw Exception when no calculationType is supplied" in {
      stubFor(get(urlPathMatching(".*")).willReturn(aResponse().withStatus(404)))

      assertThrows[Exception] {
        await(con.calculateTotalCosts(model, None))
      }
    }

    "Throw UpstreamErrorResponse on 404" in {
      stubFor(get(urlPathMatching(".*")).willReturn(aResponse().withStatus(404)))

      assertThrows[UpstreamErrorResponse] {
        await(con.calculateTotalCosts(model, Some(CalculationElectionModel(Flat))))
      }
    }

    "Throw UpstreamErrorResponse on 500" in {
      stubFor(get(urlPathMatching(".*")).willReturn(aResponse().withStatus(500)))

      assertThrows[UpstreamErrorResponse] {
        await(con.calculateTotalCosts(model, Some(CalculationElectionModel(Flat))))
      }
    }
  }

  "getTaxYear" should {
    val url = "/capital-gains-calculator/tax-year?date=2024"
    "return some parsed JSON on success" in {
      val expected = TaxYearModel(taxYearSupplied = "2024", isValidYear = true, calculationTaxYear = "2025")
      stubFor(get(urlPathMatching(".*")).willReturn(aResponse().withStatus(200).withBody(Json.toJson(expected).toString())))

      val response = await(con.getTaxYear("2024"))

      response shouldBe Some(expected)
      verify(getRequestedFor(urlEqualTo(url)))
    }

    "return None on 404" in {
      stubFor(get(urlPathMatching(".*")).willReturn(aResponse().withStatus(404)))

      val response = await(con.getTaxYear("2024"))

      response shouldBe None
      verify(getRequestedFor(urlEqualTo(url)))
    }

    "Throw UpstreamErrorResponse on 500" in {
      stubFor(get(urlPathMatching(".*")).willReturn(aResponse().withStatus(500)))

      assertThrows[UpstreamErrorResponse] {
        await(con.getTaxYear("2024"))
      }

      verify(getRequestedFor(urlEqualTo(url)))
    }
  }
}
