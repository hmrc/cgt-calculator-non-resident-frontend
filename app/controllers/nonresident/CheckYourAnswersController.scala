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

package controllers.nonresident

import common.KeystoreKeys
import common.nonresident.CalculationType
import connectors.CalculatorConnector
import constructors.nonresident.{AnswersConstructor, CalculationElectionConstructor, YourAnswersConstructor}
import controllers.predicates.ValidActiveSession
import models.nonresident._
import play.api.mvc.Result
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation

import scala.concurrent.Future

object CheckYourAnswersController extends CheckYourAnswersController {
  val calcElectionConstructor = CalculationElectionConstructor
  val answersConstructor = AnswersConstructor
  val calculatorConnector = CalculatorConnector
}

trait CheckYourAnswersController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.DisposalDateController.disposalDate().url

  val answersConstructor: AnswersConstructor
  val calculatorConnector: CalculatorConnector

  def getBackLink(totalGainResultsModel: TotalGainResultsModel,
                  acquisitionDateController: AcquisitionDateModel,
                  totalTaxOwedAnswers: Option[TotalPersonalDetailsCalculationModel]): Future[String] = totalTaxOwedAnswers match {

    case Some(data) =>
      Future.successful(controllers.nonresident.routes.BroughtForwardLossesController.broughtForwardLosses().url)
    case _ =>
      val optionSeq = Seq(totalGainResultsModel.rebasedGain, totalGainResultsModel.timeApportionedGain).flatten
      val finalSeq = Seq(totalGainResultsModel.flatGain) ++ optionSeq
      if (!finalSeq.forall(_ <= 0))
        Future.successful(controllers.nonresident.routes.PrivateResidenceReliefController.privateResidenceRelief().url)
      else
        Future.successful(controllers.nonresident.routes.ImprovementsController.improvements().url)

  }

  def getPRRModel(totalGainResultsModel: Option[TotalGainResultsModel])(implicit hc: HeaderCarrier): Future[Option[PrivateResidenceReliefModel]] = {
    totalGainResultsModel match {
      case Some(model) =>
        val optionSeq = Seq(model.rebasedGain, model.timeApportionedGain).flatten
        val finalSeq = Seq(model.flatGain) ++ optionSeq

        if (!finalSeq.forall(_ <= 0)) {
          val prrModel = calculatorConnector.fetchAndGetFormData[PrivateResidenceReliefModel](KeystoreKeys.privateResidenceRelief)

          for {
            prrModel <- prrModel
          } yield prrModel
        } else Future(None)
      case _ => Future(None)
    }
  }

  def calculateTaxableGainWithPRR(privateResidenceReliefModel: Option[PrivateResidenceReliefModel],
                                  totalGainAnswersModel: TotalGainAnswersModel)
                                 (implicit hc: HeaderCarrier): Future[Option[CalculationResultsWithPRRModel]] = {
    (totalGainAnswersModel.acquisitionDateModel, totalGainAnswersModel.rebasedValueModel) match {
      case (AcquisitionDateModel("No", _, _, _), Some(rebasedValue)) if rebasedValue.rebasedValueAmt.isEmpty =>
        Future.successful(None)
      case (_, _) if privateResidenceReliefModel.isDefined =>
        calculatorConnector.calculateTaxableGainAfterPRR(totalGainAnswersModel, privateResidenceReliefModel.get)
      case _ => Future.successful(None)
    }
  }

  def redirectRoute(calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel],
                    totalGainResultsModel: TotalGainResultsModel): Future[Result] = {
    (calculationResultsWithPRRModel, totalGainResultsModel) match {
      case (Some(CalculationResultsWithPRRModel(data, _, _)),_) if data.taxableGain > 0 =>
        Future.successful(Redirect(routes.OtherReliefsController.otherReliefs()))
      case (None, TotalGainResultsModel(data, _, _)) if data > 0 =>
        Future.successful(Redirect(routes.OtherReliefsController.otherReliefs()))
      case _ => Future.successful(Redirect(routes.SummaryController.summary()))
    }
  }

  def calculatePRRIfApplicable(totalGainAnswersModel: TotalGainAnswersModel,
                               privateResidenceReliefModel: Option[PrivateResidenceReliefModel])(implicit hc: HeaderCarrier):
  Future[Option[CalculationResultsWithPRRModel]] = {

    privateResidenceReliefModel match {
      case Some(data) => calculatorConnector.calculateTaxableGainAfterPRR(totalGainAnswersModel, data)
      case None => Future.successful(None)
    }
  }

  def checkAndGetFinalSectionsAnswers(totalGainResultsModel: TotalGainResultsModel,
                                      calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel])(implicit hc: HeaderCarrier):
  Future[Option[TotalPersonalDetailsCalculationModel]] = {

    calculationResultsWithPRRModel match {

      case Some(data) =>
        val optionalSeq = Seq(data.rebasedResult, data.timeApportionedResult).flatten
        val finalSeq = Seq(data.flatResult) ++ optionalSeq

        if (!finalSeq.forall(_.taxableGain <= 0)) {
          val personalAndPreviousDetailsModel = answersConstructor.getPersonalDetailsAndPreviousCapitalGainsAnswers(hc)
          for {
            personalAndPreviousDetailsAnswers <- personalAndPreviousDetailsModel
          } yield personalAndPreviousDetailsAnswers
        } else Future(None)


      case None =>
        val optionalBaseSeq = Seq(totalGainResultsModel.rebasedGain, totalGainResultsModel.timeApportionedGain).flatten
        val finalBaseSeq = Seq(totalGainResultsModel.flatGain) ++ optionalBaseSeq

        if (!finalBaseSeq.forall(_ <= 0)) {
          val personalAndPreviousDetailsModel = answersConstructor.getPersonalDetailsAndPreviousCapitalGainsAnswers(hc)
          for {
            personalAndPreviousDetailsAnswers <- personalAndPreviousDetailsModel
          } yield personalAndPreviousDetailsAnswers
        } else Future(None)

    }
  }

  val checkYourAnswers = ValidateSession.async { implicit request =>

    for {
      model <- answersConstructor.getNRTotalGainAnswers
      totalGainResult <- calculatorConnector.calculateTotalGain(model)
      prrModel <- getPRRModel(totalGainResult)
      totalGainWithPRRResult <- calculatePRRIfApplicable(model, prrModel)
      finalAnswers <- checkAndGetFinalSectionsAnswers(totalGainResult.get, totalGainWithPRRResult)
      answers <- Future.successful(YourAnswersConstructor.fetchYourAnswers(model, prrModel, finalAnswers))
      backLink <- getBackLink(totalGainResult.get, model.acquisitionDateModel, finalAnswers)
    } yield {
      Ok(calculation.nonresident.checkYourAnswers(answers, backLink))
    }
  }

  val submitCheckYourAnswers = ValidateSession.async { implicit request =>

    def routeRequest(model: Option[TotalGainResultsModel], taxableGainModel: Option[CalculationResultsWithPRRModel]) = model match {
      case (Some(data)) if data.rebasedGain.isDefined || data.timeApportionedGain.isDefined =>
        Future.successful(Redirect(routes.CalculationElectionController.calculationElection()))
      case (Some(data)) =>
        calculatorConnector.saveFormData[CalculationElectionModel](KeystoreKeys.calculationElection, CalculationElectionModel(CalculationType.flat))
        redirectRoute(taxableGainModel, model.get)
      case (None) => Future.successful(Redirect(common.DefaultRoutes.missingDataRoute))
    }

    for {
      allAnswersModel <- answersConstructor.getNRTotalGainAnswers
      totalGains <- calculatorConnector.calculateTotalGain(allAnswersModel)
      prrModel <- getPRRModel(totalGains)
      taxableGainWithPrr <- calculateTaxableGainWithPRR(prrModel, allAnswersModel)
      route <- routeRequest(totalGains, taxableGainWithPrr)
    } yield route
  }
}
