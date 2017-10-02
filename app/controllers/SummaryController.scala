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
import common.nonresident.CalculationType
import common.nonresident.TaxableGainCalculation._
import connectors.CalculatorConnector
import constructors.AnswersConstructor
import controllers.predicates.ValidActiveSession
import models._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.calculation
import controllers.utils.RecoverableFuture

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

object SummaryController extends SummaryController {
  val calcConnector = CalculatorConnector
  val answersConstructor = AnswersConstructor
}

trait SummaryController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  val answersConstructor: AnswersConstructor

  val summary: Action[AnyContent] = ValidateSession.async { implicit request =>

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
                         propertyLivedInModel: Option[PropertyLivedInModel],
                         totalPersonalDetailsCalculationModel: Option[TotalPersonalDetailsCalculationModel],
                         maxAEA: BigDecimal,
                         otherReliefs: Option[AllOtherReliefsModel]): Future[Option[CalculationResultsWithTaxOwedModel]] = {
      calcConnector.calculateNRCGTTotalTax(totalGainAnswersModel,
        privateResidenceReliefModel,
        propertyLivedInModel,
        totalPersonalDetailsCalculationModel,
        maxAEA,
        otherReliefs)
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

    (for {
      answers <- answersConstructor.getNRTotalGainAnswers(hc)
      totalGainResultsModel <- calcConnector.calculateTotalGain(answers)
      gainExists <- checkGainExists(totalGainResultsModel.get)
      propertyLivedIn <- getPropertyLivedInResponse(gainExists, calcConnector)
      prrModel <- getPrrResponse(propertyLivedIn, calcConnector)
      totalGainWithPRR <- getPrrIfApplicable(answers, prrModel, propertyLivedIn, calcConnector)
      finalAnswers <- getFinalSectionsAnswers(totalGainResultsModel.get, totalGainWithPRR, calcConnector, answersConstructor)
      otherReliefsModel <- getAllOtherReliefs(finalAnswers)
      taxYearModel <- getTaxYear(answers, calcConnector)
      maxAEA <- getMaxAEA(taxYearModel, calcConnector)
      finalResult <- calculateTaxOwed(answers, prrModel, propertyLivedIn, finalAnswers, maxAEA.get, otherReliefsModel)
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
    }).recoverToStart
  }

  def restart(): Action[AnyContent] = Action.async { implicit request =>
    calcConnector.clearKeystore(hc)
    Future.successful(Redirect(common.DefaultRoutes.homeUrl))
  }
}
