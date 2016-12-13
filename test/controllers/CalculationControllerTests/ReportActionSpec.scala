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

package controllers.CalculationControllerTests

import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import play.api.mvc.RequestHeader
import models.resident.TaxYearModel
import connectors.CalculatorConnector
import org.scalatest.mock.MockitoSugar
import controllers.helpers.FakeRequestHelper
import controllers.nonresident.ReportController
import assets.MessageLookup.{SummaryPage => messages}
import common.KeystoreKeys
import constructors.nonresident.AnswersConstructor
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import models.nonresident._
import common.nonresident.CalculationType
import common.nonresident.CustomerTypeKeys
import org.apache.xpath.functions.FuncRound

import scala.concurrent.Future

class ReportActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget
  (
    totalGainAnswersModel: TotalGainAnswersModel,
    totalGainResultsModel: Option[TotalGainResultsModel],
    taxYearModel: Option[TaxYearModel],
    calculationElectionModel: CalculationElectionModel,
    prrModel: Option[PrivateResidenceReliefModel] = None,
    calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel] = None,
    finalSummaryModel: TotalPersonalDetailsCalculationModel,
    taxOwedResult: Option[CalculationResultsWithTaxOwedModel] = None
  ): ReportController = {

    lazy val mockCalculatorConnector = mock[CalculatorConnector]
    lazy val mockAnswersConstructor = mock[AnswersConstructor]

    when(mockAnswersConstructor.getNRTotalGainAnswers(Matchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel))

    when(mockAnswersConstructor.getPersonalDetailsAndPreviousCapitalGainsAnswers(Matchers.any()))
      .thenReturn(Future.successful(Some(finalSummaryModel)))

    when(mockCalculatorConnector.fetchAndGetFormData[CalculationElectionModel](Matchers.eq(KeystoreKeys.calculationElection))(Matchers.any(), Matchers.any()))
      .thenReturn(Some(calculationElectionModel))

    when(mockCalculatorConnector.calculateTotalGain(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(totalGainResultsModel))

    when(mockCalculatorConnector.getTaxYear(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(taxYearModel))

    when(mockCalculatorConnector.calculateTaxableGainAfterPRR(Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(calculationResultsWithPRRModel))

    when(mockCalculatorConnector.fetchAndGetFormData[PrivateResidenceReliefModel](Matchers.eq(KeystoreKeys.privateResidenceRelief))
      (Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(prrModel))

    when(mockCalculatorConnector.getFullAEA(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(BigDecimal(11000))))

    when(mockCalculatorConnector.getPartialAEA(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(BigDecimal(5500))))

    when(mockCalculatorConnector.calculateNRCGTTotalTax(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(taxOwedResult))

    when(mockCalculatorConnector.getTaxYear(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(TaxYearModel("2015/16", true, "2015/16"))))

    when(mockCalculatorConnector.fetchAndGetFormData[OtherReliefsModel](Matchers.eq(KeystoreKeys.otherReliefsFlat))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))

    when(mockCalculatorConnector.fetchAndGetFormData[OtherReliefsModel](Matchers.eq(KeystoreKeys.otherReliefsRebased))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))

    when(mockCalculatorConnector.fetchAndGetFormData[OtherReliefsModel](Matchers.eq(KeystoreKeys.otherReliefsTA))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))

    new ReportController {
      override val calcConnector: CalculatorConnector = mockCalculatorConnector
      override val answersConstructor = mockAnswersConstructor
      override def host(implicit request: RequestHeader): String = "http://localhost:9977"
    }
  }

  val model = TotalGainAnswersModel(DisposalDateModel(5, 10, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(false)),
    DisposalValueModel(1000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Gifted")),
    Some(BoughtForLessModel(false)),
    AcquisitionValueModel(2000),
    AcquisitionCostsModel(200),
    AcquisitionDateModel("Yes", Some(4), Some(10), Some(2013)),
    Some(RebasedValueModel(Some(3000))),
    Some(RebasedCostsModel("Yes", Some(300))),
    ImprovementsModel("Yes", Some(10), Some(20)),
    Some(OtherReliefsModel(30)))

  val finalAnswersModel = TotalPersonalDetailsCalculationModel(
    CustomerTypeModel(CustomerTypeKeys.personalRep),
    None,
    None,
    None,
    OtherPropertiesModel("No"),
    None,
    None,
    None,
    None,
    BroughtForwardLossesModel(false, None)
  )

  "ReportController" should {
    "use the correct calculator connector" in {
      ReportController.calcConnector shouldBe CalculatorConnector
    }

    "use the correct answers constructor" in {
      ReportController.answersConstructor shouldBe AnswersConstructor
    }
  }


  "Calling .summaryReport from the ReportController" when {

    "the calculation chosen is flat" should {

      lazy val taxYear = TaxYearModel("2016-12-12", true, "16")

      lazy val target = setupTarget(
        model,
        Some(TotalGainResultsModel(1000, None, None)),
        Some(taxYear),
        CalculationElectionModel(CalculationType.flat),
        finalSummaryModel = finalAnswersModel
      )

      lazy val result = target.summaryReport(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return a pdf" in {
        contentType(result) shouldBe Some("application/pdf")
      }

      "return a pdf as an attachment" in {
        header("Content-Disposition", result).get should include("attachment")
      }

      "return the pdf with a filename of 'Summary'" in {
        header("Content-Disposition", result).get should include(s"""filename="${messages.title}.pdf"""")
      }

    }

    "the calculation chosen is flat with some PRR" should {

      lazy val taxYear = TaxYearModel("2016-12-12", true, "16")

      lazy val target = setupTarget(
        model,
        Some(TotalGainResultsModel(1000, None, None)),
        Some(taxYear),
        CalculationElectionModel(CalculationType.flat),
        Some(PrivateResidenceReliefModel("Yes", Some(200), None)),
        Some(CalculationResultsWithPRRModel(GainsAfterPRRModel(10000, 2000, 1000), None, None)),
        finalSummaryModel = finalAnswersModel
      )

      lazy val result = target.summaryReport(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return a pdf" in {
        contentType(result) shouldBe Some("application/pdf")
      }

      "return a pdf as an attachment" in {
        header("Content-Disposition", result).get should include("attachment")
      }

      "return the pdf with a filename of 'Summary'" in {
        header("Content-Disposition", result).get should include(s"""filename="${messages.title}.pdf"""")
      }
    }

      "the calculation chosen is rebased" should {

      lazy val taxYear = TaxYearModel("2016-12-12", true, "16")

      lazy val target = setupTarget(
        model,
        Some(TotalGainResultsModel(1000, Some(2000), None)),
        Some(taxYear),
        CalculationElectionModel(CalculationType.rebased),
        finalSummaryModel = finalAnswersModel
      )

      lazy val result = target.summaryReport(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return a pdf" in {
        contentType(result) shouldBe Some("application/pdf")
      }

      "return a pdf as an attachment" in {
        header("Content-Disposition", result).get should include("attachment")
      }

      "return the pdf with a filename of 'Summary'" in {
        header("Content-Disposition", result).get should include(s"""filename="${messages.title}.pdf"""")
      }

    }

    "the calculation chosen is time apportioned" should {

      lazy val taxYear = TaxYearModel("2016-12-12", true, "16")

      lazy val target = setupTarget(
        model,
        Some(TotalGainResultsModel(1000, None, Some(3000))),
        Some(taxYear),
        CalculationElectionModel(CalculationType.timeApportioned),
        finalSummaryModel = finalAnswersModel
      )

      lazy val result = target.summaryReport(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return a pdf" in {
        contentType(result) shouldBe Some("application/pdf")
      }

      "return a pdf as an attachment" in {
        header("Content-Disposition", result).get should include("attachment")
      }

      "return the pdf with a filename of 'Summary'" in {
        header("Content-Disposition", result).get should include(s"""filename="${messages.title}.pdf"""")
      }
    }

    "supplied without a session" should {
      lazy val taxYear = TaxYearModel("2016-12-12", true, "16")

      lazy val target = setupTarget(
        model,
        Some(TotalGainResultsModel(1000, None, None)),
        Some(taxYear),
        CalculationElectionModel(CalculationType.flat),
        finalSummaryModel = finalAnswersModel
      )

      lazy val result = target.summaryReport(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }
}
