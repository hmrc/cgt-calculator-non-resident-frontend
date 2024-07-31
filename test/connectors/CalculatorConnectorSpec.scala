package connectors

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import common.nonresident.{Flat, Rebased}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.helpers.FakeRequestHelper
import models._
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

class CalculatorConnectorSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with BeforeAndAfterEach {

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

  override def beforeEach: Unit = {
    wireMockServer.start()
    wireMockServer.resetAll()
    WireMock.configureFor(Host, Port)
  }

  override def afterEach: Unit = {
    wireMockServer.stop()
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

  "calculateTaxableGainAfterPRR" should {
    val prrModel = PrivateResidenceReliefModel(isClaimingPRR = "Yes", prrClaimed = Some(9000))
    val propertyLivedInModel = PropertyLivedInModel(true)

    val url = "/capital-gains-calculator/non-resident/calculate-gain-after-prr?disposalValue=300000.0&disposalCosts=0.0&acquisitionValue=100000.0&acquisitionCosts=0.0&improvements=351.0&rebasedValue=200000.0&disposalDate=2024-1-1&acquisitionDate=2000-1-1&prrClaimed=9000"
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
      verify(getRequestedFor(urlEqualTo(url)))
    }

    "return None on 404" in {
      stubFor(get(urlPathMatching(".*")).willReturn(aResponse().withStatus(404)))

      val response = await(con.calculateTaxableGainAfterPRR(model, prrModel, propertyLivedInModel))

      response shouldBe None
      verify(getRequestedFor(urlEqualTo(url)))
    }

    "Throw UpstreamErrorResponse on 500" in {
      stubFor(get(urlPathMatching(".*")).willReturn(aResponse().withStatus(500)))

      assertThrows[UpstreamErrorResponse] {
        await(con.calculateTaxableGainAfterPRR(model, prrModel, propertyLivedInModel))
      }

      verify(getRequestedFor(urlEqualTo(url)))
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

