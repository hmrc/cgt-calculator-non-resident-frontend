/*
 * Copyright 2020 HM Revenue & Customs
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

import akka.stream.Materializer
import assets.MessageLookup.{SummaryPage => messages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.TestModels
import common.nonresident.CalculationType
import config.ApplicationConfig
import connectors.CalculatorConnector
import constructors.AnswersConstructor
import controllers.ReportController
import controllers.helpers.FakeRequestHelper
import it.innove.play.pdf.PdfGenerator
import javax.inject.Inject
import models.{TaxYearModel, _}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.logging.SessionId
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class ReportActionSpec @Inject()(pdfGenerator: PdfGenerator) extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val materializer = mock[Materializer]
  val mockHttp =mock[DefaultHttpClient]
  val mockCalcConnector =mock[CalculatorConnector]
  val mockAnswersConstructor = mock[AnswersConstructor]
  val defaultCache = mock[CacheMap]
  val mockMessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]

  class Setup {
    val controller = new ReportController(
      mockHttp,
      mockCalcConnector,
      mockAnswersConstructor,
      mockMessagesControllerComponents,
      pdfGenerator
    )(mockConfig, fakeApplication)
  }

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


    when(mockAnswersConstructor.getNRTotalGainAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel))

    when(mockAnswersConstructor.getPersonalDetailsAndPreviousCapitalGainsAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(finalSummaryModel)))

    when(mockCalcConnector.fetchAndGetFormData[CalculationElectionModel](
      ArgumentMatchers.eq(KeystoreKeys.calculationElection))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Some(calculationElectionModel))

    when(mockCalcConnector.calculateTotalGain(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGainResultsModel))

    when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(taxYearModel))

    when(mockCalcConnector.calculateTaxableGainAfterPRR(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(calculationResultsWithPRRModel))

    when(mockCalcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](ArgumentMatchers.eq(KeystoreKeys.privateResidenceRelief))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(prrModel))

    when(mockCalcConnector.fetchAndGetFormData[PropertyLivedInModel](ArgumentMatchers.eq(KeystoreKeys.propertyLivedIn))
    (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(PropertyLivedInModel(true))))

    when(mockCalcConnector.getFullAEA(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(BigDecimal(11000))))

    when(mockCalcConnector.calculateNRCGTTotalTax(
      ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(taxOwedResult))

    when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(TaxYearModel("2015/16", isValidYear = true, "2015/16"))))

    when(mockCalcConnector.fetchAndGetFormData[OtherReliefsModel](
      ArgumentMatchers.eq(KeystoreKeys.otherReliefsFlat))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(None))

    when(mockCalcConnector.fetchAndGetFormData[OtherReliefsModel](
      ArgumentMatchers.eq(KeystoreKeys.otherReliefsRebased))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(None))

    when(mockCalcConnector.fetchAndGetFormData[OtherReliefsModel](
      ArgumentMatchers.eq(KeystoreKeys.otherReliefsTA))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(None))

    when(mockCalcConnector.calculateTotalCosts(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(BigDecimal(1000.00)))

    new ReportController(mockHttp, mockCalcConnector, mockAnswersConstructor, mockMessagesControllerComponents, pdfGenerator)(mockConfig, fakeApplication)
  }

  val model = TotalGainAnswersModel(DateModel(5, 10, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(false)),
    DisposalValueModel(1000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Gifted")),
    Some(BoughtForLessModel(false)),
    AcquisitionValueModel(2000),
    AcquisitionCostsModel(200),
    DateModel(4, 10, 2013),
    Some(RebasedValueModel(3000)),
    Some(RebasedCostsModel("Yes", Some(300))),
    ImprovementsModel("Yes", Some(10), Some(20)),
    Some(OtherReliefsModel(30)))

  val finalAnswersModel = TotalPersonalDetailsCalculationModel(
    CurrentIncomeModel(0),
    None,
    OtherPropertiesModel("No"),
    None,
    None,
    None,
    None,
    BroughtForwardLossesModel(isClaiming = false, None)
  )

//  "ReportController" should {
//
//
//
//    "use the correct calculator connector" in {
//      target.calcConnector shouldBe CalculatorConnector
//    }
//
//    "use the correct answers constructor" in {
//      ReportController.answersConstructor shouldBe AnswersConstructor
//    }
//  }


  "Calling .summaryReport from the ReportController" when {

    "the calculation chosen is flat" should {

      lazy val taxYear = TaxYearModel("2016-12-12", isValidYear = true, "16")

      lazy val target = setupTarget(
        model,
        Some(TotalGainResultsModel(1000, None, None)),
        Some(taxYear),
        CalculationElectionModel(CalculationType.flat),
        finalSummaryModel = finalAnswersModel,
        taxOwedResult = Some(TestModels.calculationResultsModelWithAll)
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
        header("Content-Disposition", result).get should include(s"""filename="${messages.title}.pdf""")
      }

    }

    "the calculation chosen is flat with some PRR" should {

      lazy val taxYear = TaxYearModel("2016-12-12", isValidYear = true, "16")

      lazy val target = setupTarget(
        model,
        Some(TotalGainResultsModel(1000, None, None)),
        Some(taxYear),
        CalculationElectionModel(CalculationType.flat),
        Some(PrivateResidenceReliefModel("Yes", Some(200), None)),
        Some(CalculationResultsWithPRRModel(GainsAfterPRRModel(10000, 2000, 1000), None, None)),
        finalSummaryModel = finalAnswersModel,
        taxOwedResult = Some(TestModels.calculationResultsModelWithAll)
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
        header("Content-Disposition", result).get should include(s"""filename="${messages.title}.pdf""")
      }
    }

    "the calculation chosen is rebased" should {

      lazy val taxYear = TaxYearModel("2016-12-12", isValidYear = true, "16")

      lazy val target = setupTarget(
        model,
        Some(TotalGainResultsModel(1000, Some(2000), None)),
        Some(taxYear),
        CalculationElectionModel(CalculationType.rebased),
        finalSummaryModel = finalAnswersModel,
        taxOwedResult = Some(TestModels.calculationResultsModelWithRebased)
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
        header("Content-Disposition", result).get should include(s"""filename="${messages.title}.pdf""")
      }

    }

    "the calculation chosen is time apportioned" should {

      lazy val taxYear = TaxYearModel("2016-12-12", isValidYear = true, "16")

      lazy val target = setupTarget(
        model,
        Some(TotalGainResultsModel(1000, None, Some(3000))),
        Some(taxYear),
        CalculationElectionModel(CalculationType.timeApportioned),
        finalSummaryModel = finalAnswersModel,
        taxOwedResult = Some(TestModels.calculationResultsModelWithTA)
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
        header("Content-Disposition", result).get should include(s"""filename="${messages.title}.pdf""")
      }
    }

    "supplied without a session" should {
      lazy val taxYear = TaxYearModel("2016-12-12", isValidYear = true, "16")

      lazy val target = setupTarget(
        model,
        Some(TotalGainResultsModel(1000, None, None)),
        Some(taxYear),
        CalculationElectionModel(CalculationType.flat),
        finalSummaryModel = finalAnswersModel,
        taxOwedResult = Some(TestModels.calculationResultsModelWithAll)
      )

      lazy val result = target.summaryReport(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }
}
