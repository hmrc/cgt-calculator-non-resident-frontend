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

package controllers

import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.TaxDates
import common.nonresident.{CalculationType, CustomerTypeKeys}
import connectors.CalculatorConnector
import constructors.AnswersConstructor
import controllers.predicates.ValidActiveSession
import it.innove.play.pdf.PdfGenerator
import models.{TaxYearModel, _}
import play.api.i18n.Messages
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation.{summaryReport => summaryView}

import scala.concurrent.Future


object ReportController extends ReportController {
  val calcConnector = CalculatorConnector
  val answersConstructor = AnswersConstructor
}

trait ReportController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  val answersConstructor: AnswersConstructor

  def host(implicit request: RequestHeader): String = {
    s"http://${request.host}/"
  }

  def getFinalTaxAnswers(totalGainResultsModel: TotalGainResultsModel,
                         calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel])
                        (implicit hc: HeaderCarrier): Future[Option[TotalPersonalDetailsCalculationModel]] = {
    val optionSeq = Seq(totalGainResultsModel.rebasedGain, totalGainResultsModel.timeApportionedGain).flatten
    val finalSeq = Seq(totalGainResultsModel.flatGain) ++ optionSeq
    lazy val finalAnswers = answersConstructor.getPersonalDetailsAndPreviousCapitalGainsAnswers
    if (!finalSeq.forall(_ <= 0)) {
      calculationResultsWithPRRModel match {
        case Some(model)
          if (Seq(model.flatResult) ++ Seq(model.rebasedResult, model.timeApportionedResult).flatten).forall(_.taxableGain <= 0) =>
          Future.successful(None)
        case _ => for {
          answers <- finalAnswers
        } yield answers
      }
    } else Future.successful(None)
  }

  def getMaxAEA(totalPersonalDetailsCalculationModel: Option[TotalPersonalDetailsCalculationModel],
                taxYear: Option[TaxYearModel])(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    totalPersonalDetailsCalculationModel match {
      case Some(data) if data.customerTypeModel.customerType.equals(CustomerTypeKeys.trustee) && data.trusteeModel.get.isVulnerable.equals("No") =>
        calcConnector.getPartialAEA(TaxDates.taxYearStringToInteger(taxYear.get.calculationTaxYear))
      case _ => calcConnector.getFullAEA(TaxDates.taxYearStringToInteger(taxYear.get.calculationTaxYear))
    }
  }

  def getTaxYear(totalGainAnswersModel: TotalGainAnswersModel)(implicit hc: HeaderCarrier): Future[Option[TaxYearModel]] = {
    val date = totalGainAnswersModel.disposalDateModel
    calcConnector.getTaxYear(s"${date.year}-${date.month}-${date.day}")
  }

  def getTaxOwed(calculationResultsWithTaxOwedModel: Option[CalculationResultsWithTaxOwedModel],
                 calculationElection: String)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    (calculationResultsWithTaxOwedModel, calculationElection) match {
      case (Some(model), CalculationType.flat) => Future.successful(Some(model.flatResult.taxOwed))
      case (Some(model), CalculationType.rebased) => Future.successful(model.rebasedResult.map(_.taxOwed))
      case (Some(model), CalculationType.timeApportioned) => Future.successful(model.timeApportionedResult.map(_.taxOwed))
      case _ => Future.successful(None)
    }
  }

  def calculateTaxOwed(totalGainAnswersModel: TotalGainAnswersModel,
                       privateResidenceReliefModel: Option[PrivateResidenceReliefModel],
                       totalPersonalDetailsCalculationModel: Option[TotalPersonalDetailsCalculationModel],
                       maxAEA: BigDecimal,
                       otherReliefs: Option[AllOtherReliefsModel])(implicit hc: HeaderCarrier): Future[Option[CalculationResultsWithTaxOwedModel]] = {
    totalPersonalDetailsCalculationModel match {
      case Some(data) => calcConnector.calculateNRCGTTotalTax(totalGainAnswersModel,
        privateResidenceReliefModel, totalPersonalDetailsCalculationModel.get, maxAEA, otherReliefs)
      case _ => Future.successful(None)
    }
  }

  def resultModel(totalGainAnswersModel: TotalGainAnswersModel)(implicit hc: HeaderCarrier): Future[Option[TotalGainResultsModel]] = {
    calcConnector.calculateTotalGain(totalGainAnswersModel)
  }

  def noPRR(acquisitionDateModel: AcquisitionDateModel, rebasedValueModel: Option[RebasedValueModel]): Future[Boolean] =
    (acquisitionDateModel, rebasedValueModel) match {
      case (AcquisitionDateModel("No", _, _, _), Some(rebasedValue)) if rebasedValue.rebasedValueAmt.isEmpty => Future.successful(true)
      case (_, _) => Future.successful(false)
    }

  def getPRRModel(model: Option[TotalGainResultsModel], noPRR: Boolean)(implicit hc: HeaderCarrier): Future[Option[PrivateResidenceReliefModel]] = {

    val optionSeq = Seq(model.get.rebasedGain, model.get.timeApportionedGain).flatten
    val finalSeq = Seq(model.get.flatGain) ++ optionSeq

    (!finalSeq.forall(_ <= 0), noPRR) match {
      case (true, false) => calcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](KeystoreKeys.privateResidenceRelief)
      case (_, _) => Future.successful(None)
    }
  }

  def calculatePRR(answers: TotalGainAnswersModel, privateResidenceReliefModel: Option[PrivateResidenceReliefModel])(implicit hc: HeaderCarrier):
  Future[Option[CalculationResultsWithPRRModel]] = {
    privateResidenceReliefModel match {
      case Some(model) => calcConnector.calculateTaxableGainAfterPRR(answers, model)
      case None => Future.successful(None)
    }
  }

  def getSection(calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel],
                 privateResidenceReliefModel: Option[PrivateResidenceReliefModel],
                 totalGainResultsModel: TotalGainResultsModel,
                 calculationType: String,
                 calculationResultsWithTaxOwedModel: Option[CalculationResultsWithTaxOwedModel],
                 taxYear: Option[TaxYearModel])(implicit hc: HeaderCarrier): Future[Seq[QuestionAnswerModel[Any]]] = {
    (calculationResultsWithTaxOwedModel, privateResidenceReliefModel) match {
      case (Some(model), _) => Future.successful(model.calculationDetailsRows(calculationType, taxYear.get.taxYearSupplied))
      case (_, Some(model)) if model.isClaimingPRR == "Yes" => Future.successful(calculationResultsWithPRRModel.get.calculationDetailsRows(calculationType))
      case _ => Future.successful(totalGainResultsModel.calculationDetailsRows(calculationType))
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

  def getOtherReliefs(calculationResultsWithTaxOwedModel: Option[CalculationResultsWithTaxOwedModel],
                      calculationElectionModel: Option[CalculationElectionModel])
                     (implicit hc: HeaderCarrier): Future[Option[OtherReliefsModel]] = {
    (calculationResultsWithTaxOwedModel, calculationElectionModel.map(_.calculationType)) match {
      case (Some(data), Some(CalculationType.flat)) => calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat)
      case (Some(data), Some(CalculationType.rebased)) => calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsRebased)
      case (Some(data), Some(CalculationType.timeApportioned)) => calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsTA)
      case _ => Future.successful(None)
    }
  }

  val summaryReport = ValidateSession.async { implicit request =>
    for {
      answers <- answersConstructor.getNRTotalGainAnswers
      calculationType <- calcConnector.fetchAndGetFormData[CalculationElectionModel](KeystoreKeys.calculationElection)
      totalGains <- resultModel(answers)(hc)
      taxYear <- getTaxYear(answers)
      noPRR <- noPRR(answers.acquisitionDateModel, answers.rebasedValueModel)
      prrModel <- getPRRModel(totalGains, noPRR)
      totalGainsWithPRR <- calculatePRR(answers, prrModel)
      finalAnswers <- getFinalTaxAnswers(totalGains.get, totalGainsWithPRR)
      otherReliefsModel <- getAllOtherReliefs(finalAnswers)
      taxYear <- getTaxYear(answers)
      maxAEA <- getMaxAEA(finalAnswers, taxYear)
      finalResult <- calculateTaxOwed(answers, prrModel, finalAnswers, maxAEA.get, otherReliefsModel)
      calculationType <- calcConnector.fetchAndGetFormData[CalculationElectionModel](KeystoreKeys.calculationElection)
      otherReliefs <- getOtherReliefs(finalResult, calculationType)
      results <- getSection(totalGainsWithPRR, prrModel,
        totalGains.get, calculationType.get.calculationType, finalResult, taxYear)
      taxOwed <- getTaxOwed(finalResult, calculationType.get.calculationType)
    } yield {
      PdfGenerator.ok(summaryView(answers, results, taxYear.get, calculationType.get.calculationType, prrModel,
        finalAnswers, taxOwed.getOrElse(0), otherReliefs), host).toScala
        .withHeaders("Content-Disposition" ->s"""attachment; filename="${Messages("calc.summary.title")}.pdf"""")
    }
  }
}
