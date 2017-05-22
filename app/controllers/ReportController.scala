/*
 * Copyright 2017 HM Revenue & Customs
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

package controllers

import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.TaxDates
import common.nonresident.{CalculationType, CustomerTypeKeys}
import connectors.CalculatorConnector
import constructors.{AnswersConstructor, YourAnswersConstructor}
import controllers.predicates.ValidActiveSession
import it.innove.play.pdf.PdfGenerator
import models.{TaxYearModel, _}
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, RequestHeader}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future


object ReportController extends ReportController {
  val calcConnector = CalculatorConnector
  val answersConstructor = AnswersConstructor
}

trait ReportController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  val answersConstructor: AnswersConstructor
  val pdfGenerator = new PdfGenerator

  def host(implicit request: RequestHeader): String = {
    s"http://${request.host}/"
  }

  val summaryReport: Action[AnyContent] = ValidateSession.async { implicit request =>

    def getPRRModel(implicit hc: HeaderCarrier, totalGainResultsModel: TotalGainResultsModel): Future[Option[PrivateResidenceReliefModel]] = {
      val optionSeq = Seq(totalGainResultsModel.rebasedGain, totalGainResultsModel.timeApportionedGain).flatten
      val finalSeq = Seq(totalGainResultsModel.flatGain) ++ optionSeq

      if (!finalSeq.forall(_ <= 0)) {
        calcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](KeystoreKeys.privateResidenceRelief)
      } else Future(None)
    }

    def getMaxAEA(totalPersonalDetailsCalculationModel: Option[TotalPersonalDetailsCalculationModel],
                  taxYear: Option[TaxYearModel]): Future[Option[BigDecimal]] = {
      totalPersonalDetailsCalculationModel match {
        case Some(data) if data.customerTypeModel.customerType.equals(CustomerTypeKeys.trustee) && data.trusteeModel.get.isVulnerable.equals("No") =>
          calcConnector.getPartialAEA(TaxDates.taxYearStringToInteger(taxYear.get.calculationTaxYear))
        case _ => calcConnector.getFullAEA(TaxDates.taxYearStringToInteger(taxYear.get.calculationTaxYear))
      }
    }

    def getTaxYear(totalGainAnswersModel: TotalGainAnswersModel): Future[Option[TaxYearModel]] = {
      val date = totalGainAnswersModel.disposalDateModel
      calcConnector.getTaxYear(s"${date.year}-${date.month}-${date.day}")
    }

    def getCalculationResult(calculationResultsWithTaxOwedModel: Option[CalculationResultsWithTaxOwedModel],
                             calculationElection: String): Future[TotalTaxOwedModel] = {
      (calculationResultsWithTaxOwedModel, calculationElection) match {
        case (Some(model), CalculationType.flat) => Future.successful(model.flatResult)
        case (Some(model), CalculationType.rebased) => Future.successful(model.rebasedResult.get)
        case (Some(model), CalculationType.timeApportioned) => Future.successful(model.timeApportionedResult.get)
      }
    }

    def summaryBackUrl(model: Option[TotalGainResultsModel])(implicit hc: HeaderCarrier): Future[String] = model match {
      case (Some(data)) if data.rebasedGain.isDefined || data.timeApportionedGain.isDefined =>
        Future.successful(routes.CalculationElectionController.calculationElection().url)
      case (Some(data)) =>
        Future.successful(routes.CheckYourAnswersController.checkYourAnswers().url)
      case (None) => Future.successful(common.DefaultRoutes.missingDataRoute)
    }

    def calculateTaxOwed(totalGainAnswersModel: TotalGainAnswersModel,
                         privateResidenceReliefModel: Option[PrivateResidenceReliefModel],
                         totalPersonalDetailsCalculationModel: Option[TotalPersonalDetailsCalculationModel],
                         maxAEA: BigDecimal,
                         otherReliefs: Option[AllOtherReliefsModel]): Future[Option[CalculationResultsWithTaxOwedModel]] = {
      totalPersonalDetailsCalculationModel match {
        case Some(data) => calcConnector.calculateNRCGTTotalTax(totalGainAnswersModel,
          privateResidenceReliefModel, data, maxAEA, otherReliefs)
        case _ => Future.successful(None)
      }
    }

    def getAllOtherReliefs(totalPersonalDetailsCalculationModel: Option[TotalPersonalDetailsCalculationModel])
                          (implicit hc: HeaderCarrier): Future[Option[AllOtherReliefsModel]] = {
      totalPersonalDetailsCalculationModel match {
        case Some(data) => {
          val flat = calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat)
          val rebased = calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsRebased)
          val time = calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsTA)

          for {
            flatReliefs <- flat
            rebasedReliefs <- rebased
            timeReliefs <- time
          } yield Some(AllOtherReliefsModel(flatReliefs, rebasedReliefs, timeReliefs))
        }
        case _ => Future.successful(None)
      }
    }

    def qaRows(totalGainAnswersModel: TotalGainAnswersModel,
               totalGainResultsModel: TotalGainResultsModel,
               privateResidenceReliefModel: Option[PrivateResidenceReliefModel] = None,
               personalAndPreviousDetailsModel: Option[TotalPersonalDetailsCalculationModel] = None
              )(implicit hc: HeaderCarrier): Future[Seq[QuestionAnswerModel[Any]]] = {

      val optionSeq = Seq(totalGainResultsModel.rebasedGain, totalGainResultsModel.timeApportionedGain).flatten
      val finalSeq = Seq(totalGainResultsModel.flatGain) ++ optionSeq

      if (!finalSeq.forall(_ <= 0)) {
        Future.successful(YourAnswersConstructor.fetchYourAnswers(totalGainAnswersModel, privateResidenceReliefModel, None))
      }
      else Future.successful(YourAnswersConstructor.fetchYourAnswers(totalGainAnswersModel, privateResidenceReliefModel, personalAndPreviousDetailsModel))
    }

    for {
      answers <- answersConstructor.getNRTotalGainAnswers(hc)
      totalGainResultsModel <- calcConnector.calculateTotalGain(answers)

      privateResidentReliefModel <- getPRRModel(hc, totalGainResultsModel.get)
      finalAnswers <- answersConstructor.getPersonalDetailsAndPreviousCapitalGainsAnswers
      otherReliefsModel <- getAllOtherReliefs(finalAnswers)
      taxYearModel <- getTaxYear(answers)
      maxAEA <- getMaxAEA(finalAnswers, taxYearModel)
      finalResult <- calculateTaxOwed(answers, privateResidentReliefModel, finalAnswers, maxAEA.get, otherReliefsModel)
      backUrl <- summaryBackUrl(totalGainResultsModel)

      questionAnswerRows <- qaRows(answers, totalGainResultsModel.get, privateResidentReliefModel, finalAnswers)

      calculationType <- calcConnector.fetchAndGetFormData[CalculationElectionModel](KeystoreKeys.calculationElection)
      totalCosts <- calcConnector.calculateTotalCosts(answers, calculationType)

      calculationResult <- getCalculationResult(finalResult, calculationType.get.calculationType)
    } yield {
      calculationType.get.calculationType match {
        case CalculationType.flat =>
          pdfGenerator.ok(calculation.summaryReport(questionAnswerRows, calculationResult, taxYearModel.get, calculationType.get.calculationType,
            answers.disposalValueModel.disposalValue,
            answers.acquisitionValueModel.acquisitionValueAmt,
            totalCosts,
            reliefsUsed = calculationResult.prrUsed.getOrElse(BigDecimal(0)) + calculationResult.otherReliefsUsed.getOrElse(BigDecimal(0))), host)
            .asScala()
            .withHeaders("Content-Disposition" ->s"""attachment; filename="${Messages("calc.summary.title")}.pdf"""")

        case CalculationType.timeApportioned =>
          pdfGenerator.ok(calculation.summaryReport(questionAnswerRows, calculationResult, taxYearModel.get, calculationType.get.calculationType,
            answers.disposalValueModel.disposalValue,
            answers.acquisitionValueModel.acquisitionValueAmt,
            totalCosts,
            Some(finalResult.get.flatResult.totalGain),
            reliefsUsed = calculationResult.prrUsed.getOrElse(BigDecimal(0)) + calculationResult.otherReliefsUsed.getOrElse(BigDecimal(0))), host)
            .asScala()
            .withHeaders("Content-Disposition" ->s"""attachment; filename="${Messages("calc.summary.title")}.pdf"""")

        case CalculationType.rebased =>
          pdfGenerator.ok(calculation.summaryReport(questionAnswerRows, calculationResult, taxYearModel.get, calculationType.get.calculationType,
            answers.disposalValueModel.disposalValue,
            answers.rebasedValueModel.get.rebasedValueAmt.get,
            totalCosts,
            None,
            reliefsUsed = calculationResult.prrUsed.getOrElse(BigDecimal(0)) + calculationResult.otherReliefsUsed.getOrElse(BigDecimal(0))), host)
            .asScala()
            .withHeaders("Content-Disposition" ->s"""attachment; filename="${Messages("calc.summary.title")}.pdf"""")
      }
    }
  }
}
