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
import models.{CalculationResultsWithPRRModel, TaxYearModel, _}
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation

import scala.concurrent.Future

object SummaryController extends SummaryController {
  val calcConnector = CalculatorConnector
  val answersConstructor = AnswersConstructor
}

trait SummaryController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl = controllers.routes.SummaryController.restart().url
  override val homeLink = controllers.routes.DisposalDateController.disposalDate().url
  val calcConnector: CalculatorConnector
  val answersConstructor: AnswersConstructor

  val summary = ValidateSession.async { implicit request =>

    def getPRRModel(implicit hc: HeaderCarrier, totalGainResultsModel: TotalGainResultsModel): Future[Option[PrivateResidenceReliefModel]] = {
      val optionSeq = Seq(totalGainResultsModel.rebasedGain, totalGainResultsModel.timeApportionedGain).flatten
      val finalSeq = Seq(totalGainResultsModel.flatGain) ++ optionSeq

      if (!finalSeq.forall(_ <= 0)) {
        val prrModel = calcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](KeystoreKeys.privateResidenceRelief)

        for {
          prrModel <- prrModel
        } yield prrModel
      } else Future(None)
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

    def getTaxOwed(calculationResultsWithTaxOwedModel: Option[CalculationResultsWithTaxOwedModel],
                   calculationElection: String): Future[Option[BigDecimal]] = {
      (calculationResultsWithTaxOwedModel, calculationElection) match {
        case (Some(model), CalculationType.flat) => Future.successful(Some(model.flatResult.taxOwed))
        case (Some(model), CalculationType.rebased) => Future.successful(model.rebasedResult.map(_.taxOwed))
        case (Some(model), CalculationType.timeApportioned) => Future.successful(model.timeApportionedResult.map(_.taxOwed))
        case _ => Future.successful(None)
      }
    }

    def getSection(calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel],
                   privateResidenceReliefModel: Option[PrivateResidenceReliefModel],
                   totalGainResultsModel: TotalGainResultsModel,
                   calculationType: String,
                   calculationResultsWithTaxOwedModel: Option[CalculationResultsWithTaxOwedModel],
                   taxYear: Option[TaxYearModel]): Future[Seq[QuestionAnswerModel[Any]]] = {
      (calculationResultsWithTaxOwedModel, privateResidenceReliefModel) match {
        case (Some(model), _) => Future.successful(model.calculationDetailsRows(calculationType, taxYear.get.taxYearSupplied))
        case (_, Some(model)) if model.isClaimingPRR == "Yes" => Future.successful(calculationResultsWithPRRModel.get.calculationDetailsRows(calculationType))
        case _ => Future.successful(totalGainResultsModel.calculationDetailsRows(calculationType))
      }
    }

    def summaryBackUrl(model: Option[TotalGainResultsModel])(implicit hc: HeaderCarrier): Future[String] = model match {
      case (Some(data)) if data.rebasedGain.isDefined || data.timeApportionedGain.isDefined =>
        Future.successful(routes.CalculationElectionController.calculationElection().url)
      case (Some(data)) =>
        Future.successful(routes.CheckYourAnswersController.checkYourAnswers().url)
      case (None) => Future.successful(common.DefaultRoutes.missingDataRoute)
    }

    def displayDateWarning(disposalDate: DisposalDateModel): Future[Boolean] = {
      Future.successful(!TaxDates.dateInsideTaxYear(disposalDate.day, disposalDate.month, disposalDate.year))
    }

    def calculateDetails(summaryData: TotalGainAnswersModel): Future[Option[TotalGainResultsModel]] = {
      calcConnector.calculateTotalGain(summaryData)
    }

    def calculatePRR(answers: TotalGainAnswersModel, privateResidenceReliefModel: Option[PrivateResidenceReliefModel])
      : Future[Option[CalculationResultsWithPRRModel]] = {
        privateResidenceReliefModel match {
          case Some(model) => calcConnector.calculateTaxableGainAfterPRR(answers, model)
          case None => Future.successful(None)
        }
    }

    def calculateTaxOwed(totalGainAnswersModel: TotalGainAnswersModel,
                         privateResidenceReliefModel: Option[PrivateResidenceReliefModel],
                         totalPersonalDetailsCalculationModel: Option[TotalPersonalDetailsCalculationModel],
                         maxAEA: BigDecimal,
                         otherReliefs: Option[AllOtherReliefsModel]): Future[Option[CalculationResultsWithTaxOwedModel]] = {
      totalPersonalDetailsCalculationModel match {
        case Some(data) => calcConnector.calculateNRCGTTotalTax(totalGainAnswersModel,
          privateResidenceReliefModel, totalPersonalDetailsCalculationModel.get, maxAEA, otherReliefs)
        case _ => Future.successful(None)
      }
    }

    def routeRequest(result: Seq[QuestionAnswerModel[Any]],
                     backUrl: String, displayDateWarning: Boolean,
                     calculationType: String,
                     taxOwed: Option[BigDecimal]): Future[Result] = {
      Future.successful(Ok(calculation.summary(result,
        backUrl, displayDateWarning, calculationType, taxOwed)))
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

    for {
      answers <- answersConstructor.getNRTotalGainAnswers(hc)
      displayWarning <- displayDateWarning(answers.disposalDateModel)
      totalGainResultsModel <- calculateDetails(answers)
      privateResidentReliefModel <- getPRRModel(hc, totalGainResultsModel.get)
      calculationResultsWithPRR <- calculatePRR(answers, privateResidentReliefModel)
      finalAnswers <- getFinalTaxAnswers(totalGainResultsModel.get, calculationResultsWithPRR)
      otherReliefsModel <- getAllOtherReliefs(finalAnswers)
      taxYear <- getTaxYear(answers)
      maxAEA <- getMaxAEA(finalAnswers, taxYear)
      finalResult <- calculateTaxOwed(answers, privateResidentReliefModel, finalAnswers, maxAEA.get, otherReliefsModel)
      backUrl <- summaryBackUrl(totalGainResultsModel)
      calculationType <- calcConnector.fetchAndGetFormData[CalculationElectionModel](KeystoreKeys.calculationElection)
      results <- getSection(calculationResultsWithPRR, privateResidentReliefModel,
        totalGainResultsModel.get, calculationType.get.calculationType, finalResult, taxYear)
      taxOwed <- getTaxOwed(finalResult, calculationType.get.calculationType)
      route <- routeRequest(results, backUrl, displayWarning,
        calculationType.get.calculationType, taxOwed)
    } yield route
  }

  def restart(): Action[AnyContent] = Action.async { implicit request =>
    calcConnector.clearKeystore(hc)
    Future.successful(Redirect(routes.DisposalDateController.disposalDate()))
  }
}
