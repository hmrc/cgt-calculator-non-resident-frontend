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
import common.nonresident.CalculationType
import connectors.CalculatorConnector
import constructors.AnswersConstructor
import controllers.predicates.ValidActiveSession
import models._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation

import scala.concurrent.Future

object SummaryController extends SummaryController {
  val calcConnector = CalculatorConnector
  val answersConstructor = AnswersConstructor
}

trait SummaryController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  val answersConstructor: AnswersConstructor

  val summary: Action[AnyContent] = ValidateSession.async { implicit request =>

    def getPRRModel(implicit hc: HeaderCarrier, totalGainResultsModel: TotalGainResultsModel): Future[Option[PrivateResidenceReliefModel]] = {
      val optionSeq = Seq(totalGainResultsModel.rebasedGain, totalGainResultsModel.timeApportionedGain).flatten
      val finalSeq = Seq(totalGainResultsModel.flatGain) ++ optionSeq

      if (!finalSeq.forall(_ <= 0)) {
        calcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](KeystoreKeys.privateResidenceRelief)
      } else Future(None)
    }

    def getMaxAEA(taxYear: Option[TaxYearModel]): Future[Option[BigDecimal]] = {
      calcConnector.getFullAEA(TaxDates.taxYearStringToInteger(taxYear.get.calculationTaxYear))
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
      case (Some(_)) =>
        Future.successful(routes.CheckYourAnswersController.checkYourAnswers().url)
      case (None) => Future.successful(common.DefaultRoutes.missingDataRoute)
    }

    def calculateTaxOwed(totalGainAnswersModel: TotalGainAnswersModel,
                         privateResidenceReliefModel: Option[PrivateResidenceReliefModel],
                         totalPersonalDetailsCalculationModel: Option[TotalPersonalDetailsCalculationModel],
                         maxAEA: BigDecimal,
                         otherReliefs: Option[AllOtherReliefsModel]): Future[Option[CalculationResultsWithTaxOwedModel]] = {
      calcConnector.calculateNRCGTTotalTax(totalGainAnswersModel,
          privateResidenceReliefModel, totalPersonalDetailsCalculationModel, maxAEA, otherReliefs)
    }

    def getAllOtherReliefs(totalPersonalDetailsCalculationModel: Option[TotalPersonalDetailsCalculationModel])
                          (implicit hc: HeaderCarrier): Future[Option[AllOtherReliefsModel]] = {
      totalPersonalDetailsCalculationModel match {
        case Some(_) =>
          val flat = calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat)
          val rebased = calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsRebased)
          val time = calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsTA)

          for {
            flatReliefs <- flat
            rebasedReliefs <- rebased
            timeReliefs <- time
          } yield Some(AllOtherReliefsModel(flatReliefs, rebasedReliefs, timeReliefs))
        case _ => Future.successful(None)
      }
    }

    def getFinalSectionsAnswers(totalGainResultsModel: TotalGainResultsModel,
                                calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel])(implicit hc: HeaderCarrier):
    Future[Option[TotalPersonalDetailsCalculationModel]] = {
      calculationResultsWithPRRModel match {

        case Some(data) =>
          val results = data.flatResult :: List(data.rebasedResult, data.timeApportionedResult).flatten

          if (results.exists(_.taxableGain > 0)) {
            answersConstructor.getPersonalDetailsAndPreviousCapitalGainsAnswers(hc)
          } else Future(None)

        case None =>
          val gains = totalGainResultsModel.flatGain :: List(totalGainResultsModel.rebasedGain, totalGainResultsModel.timeApportionedGain).flatten

          if (gains.exists(_ > 0)) {
            answersConstructor.getPersonalDetailsAndPreviousCapitalGainsAnswers(hc)
          } else Future(None)
      }
    }

    def getPRRIfApplicable(totalGainAnswersModel: TotalGainAnswersModel,
                           privateResidenceReliefModel: Option[PrivateResidenceReliefModel])(implicit hc: HeaderCarrier):
    Future[Option[CalculationResultsWithPRRModel]] = {

      privateResidenceReliefModel match {
        case Some(data) => calcConnector.calculateTaxableGainAfterPRR(totalGainAnswersModel, data)
        case None => Future.successful(None)
      }
    }

    for {
      answers <- answersConstructor.getNRTotalGainAnswers(hc)
      totalGainResultsModel <- calcConnector.calculateTotalGain(answers)
      privateResidentReliefModel <- getPRRModel(hc, totalGainResultsModel.get)
      totalGainWithPRR <- getPRRIfApplicable(answers, privateResidentReliefModel)
      finalAnswers <- getFinalSectionsAnswers(totalGainResultsModel.get, totalGainWithPRR)
      otherReliefsModel <- getAllOtherReliefs(finalAnswers)
      taxYearModel <- getTaxYear(answers)
      maxAEA <- getMaxAEA(taxYearModel)
      finalResult <- calculateTaxOwed(answers, privateResidentReliefModel, finalAnswers, maxAEA.get, otherReliefsModel)
      backUrl <- summaryBackUrl(totalGainResultsModel)
      calculationType <- calcConnector.fetchAndGetFormData[CalculationElectionModel](KeystoreKeys.calculationElection)
      totalCosts <- calcConnector.calculateTotalCosts(answers, calculationType)
      calculationResult <- getCalculationResult(finalResult, calculationType.get.calculationType)
    } yield {
      calculationType.get.calculationType match {
        case CalculationType.flat =>
          Ok(calculation.summary(calculationResult, taxYearModel.get, calculationType.get.calculationType,
            answers.disposalValueModel.disposalValue,
            answers.acquisitionValueModel.acquisitionValueAmt,
            totalCosts,
            backUrl,
            reliefsUsed = calculationResult.prrUsed.getOrElse(BigDecimal(0)) + calculationResult.otherReliefsUsed.getOrElse(BigDecimal(0)))
          )
        case CalculationType.timeApportioned =>
          Ok(calculation.summary(calculationResult, taxYearModel.get, calculationType.get.calculationType,
            answers.disposalValueModel.disposalValue,
            answers.acquisitionValueModel.acquisitionValueAmt,
            totalCosts,
            backUrl, Some(finalResult.get.flatResult.totalGain),
            reliefsUsed = calculationResult.prrUsed.getOrElse(BigDecimal(0)) + calculationResult.otherReliefsUsed.getOrElse(BigDecimal(0)))
          )
        case CalculationType.rebased =>
          Ok(calculation.summary(calculationResult, taxYearModel.get, calculationType.get.calculationType,
            answers.disposalValueModel.disposalValue,
            answers.rebasedValueModel.get.rebasedValueAmt,
            totalCosts,
            backUrl, None,
            reliefsUsed = calculationResult.prrUsed.getOrElse(BigDecimal(0)) + calculationResult.otherReliefsUsed.getOrElse(BigDecimal(0)))
          )
      }
    }
  }

  def restart(): Action[AnyContent] = Action.async { implicit request =>
    calcConnector.clearKeystore(hc)
    Future.successful(Redirect(common.DefaultRoutes.homeUrl))
  }
}
