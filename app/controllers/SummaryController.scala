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

package controllers

import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.nonresident.TaxableGainCalculation._
import common.nonresident.{CalculationType, Flat, Rebased, TimeApportioned}
import connectors.CalculatorConnector
import constructors.{AnswersConstructor, YourAnswersConstructor}
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import models._
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.calculation.summary

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class SummaryController @Inject()(calcConnector: CalculatorConnector,
                                  sessionCacheService: SessionCacheService,
                                  answersConstructor: AnswersConstructor,
                                  mcc: MessagesControllerComponents,
                                  summaryView: summary)(implicit ec: ExecutionContext)
                                    extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val summary: Action[AnyContent] = ValidateSession.async { implicit request =>

    def getCalculationResult(calculationResultsWithTaxOwedModel: Option[CalculationResultsWithTaxOwedModel],
                             calculationElection: CalculationType): Future[TotalTaxOwedModel] = {
      (calculationResultsWithTaxOwedModel, calculationElection) match {
        case (Some(model), Flat) => Future.successful(model.flatResult)
        case (Some(model), Rebased) => Future.successful(model.rebasedResult.get)
        case (Some(model), TimeApportioned) => Future.successful(model.timeApportionedResult.get)
        case _ => throw new MatchError("Unexpected values for: (calculationResultsWithTaxOwedModel, calculationElection)")
      }
    }

    def summaryBackUrl(model: Option[TotalGainResultsModel]): Future[String] = model match {
      case Some(data) if data.rebasedGain.isDefined || data.timeApportionedGain.isDefined =>
        Future.successful(routes.CalculationElectionController.calculationElection.url)
      case Some(_) =>
        Future.successful(routes.CheckYourAnswersController.checkYourAnswers.url)
      case None => Future.successful(common.DefaultRoutes.missingDataRoute)
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

    def getAllOtherReliefs(totalPersonalDetailsCalculationModel: Option[TotalPersonalDetailsCalculationModel]): Future[Option[AllOtherReliefsModel]] = {
      totalPersonalDetailsCalculationModel match {
        case Some(_) =>
          val flat = sessionCacheService.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat)
          val rebased = sessionCacheService.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsRebased)
          val time = sessionCacheService.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsTA)

          for {
            flatReliefs <- flat
            rebasedReliefs <- rebased
            timeReliefs <- time
          } yield Some(AllOtherReliefsModel(flatReliefs, rebasedReliefs, timeReliefs))
        case _ => Future.successful(None)
      }
    }

    val showUserResearchPanel = setURPanelFlag

    (for {
      answers <- answersConstructor.getNRTotalGainAnswers(using request)
      totalGainResultsModel <- calcConnector.calculateTotalGain(answers)
      gainExists <- checkGainExists(totalGainResultsModel.get)
      propertyLivedIn <- getPropertyLivedInResponse(gainExists, sessionCacheService)
      prrModel <- getPrrResponse(propertyLivedIn, sessionCacheService)
      totalGainWithPRR <- getPrrIfApplicable(answers, prrModel, propertyLivedIn, calcConnector)
      finalAnswers <- getFinalSectionsAnswers(totalGainResultsModel.get, totalGainWithPRR, answersConstructor)
      otherReliefsModel <- getAllOtherReliefs(finalAnswers)
      taxYearModel <- getTaxYear(answers, calcConnector)
      maxAEA <- getMaxAEA(taxYearModel, calcConnector)
      finalResult <- calculateTaxOwed(answers, prrModel, propertyLivedIn, finalAnswers, maxAEA.get, otherReliefsModel)
      backUrl <- summaryBackUrl(totalGainResultsModel)
      calculationType <- sessionCacheService.fetchAndGetFormData[CalculationElectionModel](KeystoreKeys.calculationElection)
      totalCosts <- calcConnector.calculateTotalCosts(answers, calculationType)
      calculationResult <- getCalculationResult(finalResult, calculationType.get.calculationType)
      questionAnswerRows = YourAnswersConstructor.fetchYourAnswers(answers, prrModel, finalAnswers, propertyLivedIn)
    } yield {
      calculationType.get.calculationType match {
        case Flat =>
          Ok(summaryView(calculationResult, taxYearModel.get, calculationType.get.calculationType,
            answers.disposalValueModel.disposalValue,
            answers.acquisitionValueModel.acquisitionValueAmt,
            totalCosts,
            backUrl,
            reliefsUsed = calculationResult.prrUsed.getOrElse(BigDecimal(0)) + calculationResult.otherReliefsUsed.getOrElse(BigDecimal(0)),
            showUserResearchPanel = showUserResearchPanel,
            questionsForPrint = questionAnswerRows
          ))
        case TimeApportioned =>
          Ok(summaryView(calculationResult, taxYearModel.get, calculationType.get.calculationType,
            answers.disposalValueModel.disposalValue,
            answers.acquisitionValueModel.acquisitionValueAmt,
            totalCosts,
            backUrl, Some(finalResult.get.flatResult.totalGain),
            reliefsUsed = calculationResult.prrUsed.getOrElse(BigDecimal(0)) + calculationResult.otherReliefsUsed.getOrElse(BigDecimal(0)),
            showUserResearchPanel = showUserResearchPanel,
            questionsForPrint = questionAnswerRows
          ))
        case Rebased =>
          Ok(summaryView(calculationResult, taxYearModel.get, calculationType.get.calculationType,
            answers.disposalValueModel.disposalValue,
            answers.rebasedValueModel.get.rebasedValueAmt,
            totalCosts,
            backUrl, None,
            reliefsUsed = calculationResult.prrUsed.getOrElse(BigDecimal(0)) + calculationResult.otherReliefsUsed.getOrElse(BigDecimal(0)),
            showUserResearchPanel = showUserResearchPanel,
            questionsForPrint = questionAnswerRows
          ))
      }
    }).recoverToStart
  }

  private[controllers] def setURPanelFlag(implicit hc: HeaderCarrier): Boolean = {
    val random = new Random()
    val seed = getLongFromSessionID(hc)
    random.setSeed(seed)
    random.nextInt(3) == 0
  }

  private[controllers] def getLongFromSessionID(hc: HeaderCarrier): Long = {
    val session = hc.sessionId.map(_.value).getOrElse("0")
    val numericSessionValues = session.replaceAll("[^0-9]", "") match {
      case "" => "0"
      case num => num
    }
    numericSessionValues.takeRight(10).toLong
  }

  def restart(): Action[AnyContent] = Action.async { implicit request =>
    sessionCacheService.clearSession
    Future.successful(Redirect(common.DefaultRoutes.homeUrl))
  }
}
