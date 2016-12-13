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

import common.nonresident.CustomerTypeKeys
import common.{KeystoreKeys, TaxDates}
import connectors.CalculatorConnector
import constructors.nonresident.{AnswersConstructor, CalculationElectionConstructor}
import controllers.predicates.ValidActiveSession
import forms.nonresident.CalculationElectionForm._
import models.nonresident.{TotalGainAnswersModel, _}
import models.resident.TaxYearModel
import play.api.data.Form
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation

import scala.concurrent.Future

object CalculationElectionController extends CalculationElectionController {
  val calcConnector = CalculatorConnector
  val calcAnswersConstructor = AnswersConstructor
  val calcElectionConstructor = CalculationElectionConstructor
}

trait CalculationElectionController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl: String = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink: String = controllers.nonresident.routes.DisposalDateController.disposalDate().url
  val calcConnector: CalculatorConnector
  val calcElectionConstructor: CalculationElectionConstructor
  val calcAnswersConstructor: AnswersConstructor

  private def getPRRResponse(totalGainResultsModel: TotalGainResultsModel)(implicit hc: HeaderCarrier): Future[Option[PrivateResidenceReliefModel]] = {
    val optionSeq = Seq(totalGainResultsModel.rebasedGain, totalGainResultsModel.timeApportionedGain).flatten
    val finalSeq = Seq(totalGainResultsModel.flatGain) ++ optionSeq

    if (finalSeq.exists(_ > 0)) {
      calcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](KeystoreKeys.privateResidenceRelief)
    } else Future(None)
  }

  private def getPRRIfApplicable(totalGainAnswersModel: TotalGainAnswersModel,
                                 privateResidenceReliefModel: Option[PrivateResidenceReliefModel])(implicit hc: HeaderCarrier):
  Future[Option[CalculationResultsWithPRRModel]] = {

    privateResidenceReliefModel match {
      case Some(data) => calcConnector.calculateTaxableGainAfterPRR(totalGainAnswersModel, data)
      case None => Future.successful(None)
    }
  }

  private def getFinalSectionsAnswers(totalGainResultsModel: TotalGainResultsModel,
                                      calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel])(implicit hc: HeaderCarrier):
  Future[Option[TotalPersonalDetailsCalculationModel]] = {

    calculationResultsWithPRRModel match {

      case Some(data) =>
        val results = data.flatResult :: List(data.rebasedResult, data.timeApportionedResult).flatten

        if (results.exists(_.taxableGain > 0)) {
          calcAnswersConstructor.getPersonalDetailsAndPreviousCapitalGainsAnswers(hc)
        } else Future(None)

      case None =>
        val gains = totalGainResultsModel.flatGain :: List(totalGainResultsModel.rebasedGain, totalGainResultsModel.timeApportionedGain).flatten

        if (gains.exists(_ > 0)) {
          calcAnswersConstructor.getPersonalDetailsAndPreviousCapitalGainsAnswers(hc)
        } else Future(None)
    }
  }

  private def getMaxAEA(totalPersonalDetailsCalculationModel: Option[TotalPersonalDetailsCalculationModel],
                        taxYear: Option[TaxYearModel])(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    totalPersonalDetailsCalculationModel match {
      case Some(data) if data.customerTypeModel.customerType.equals(CustomerTypeKeys.trustee) && data.trusteeModel.get.isVulnerable.equals("No") =>
        calcConnector.getPartialAEA(TaxDates.taxYearStringToInteger(taxYear.get.calculationTaxYear))
      case _ => calcConnector.getFullAEA(TaxDates.taxYearStringToInteger(taxYear.get.calculationTaxYear))
    }
  }

  private def getTaxYear(totalGainAnswersModel: TotalGainAnswersModel)(implicit hc: HeaderCarrier): Future[Option[TaxYearModel]] = {
    val date = totalGainAnswersModel.disposalDateModel
    calcConnector.getTaxYear(s"${date.year}-${date.month}-${date.day}")
  }

  private def getTaxOwedIfApplicable(totalGainAnswersModel: TotalGainAnswersModel,
                                     prrModel: Option[PrivateResidenceReliefModel],
                                     totalTaxOwedModel: Option[TotalPersonalDetailsCalculationModel],
                                     maxAEA: BigDecimal,
                                     otherReliefs: Option[AllOtherReliefsModel])
                                    (implicit hc: HeaderCarrier): Future[Option[CalculationResultsWithTaxOwedModel]] = {

    totalTaxOwedModel match {
      case Some(data) => calcConnector.calculateNRCGTTotalTax(totalGainAnswersModel, prrModel, data, maxAEA, otherReliefs)
      case None => Future(None)
    }
  }

  private def getAllOtherReliefs(totalPersonalDetailsCalculationModel: Option[TotalPersonalDetailsCalculationModel])
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

  val calculationElection = ValidateSession.async { implicit request =>

    def action(content: Seq[(String, String, String, Option[String], Option[BigDecimal])]) =
      calcConnector.fetchAndGetFormData[CalculationElectionModel](KeystoreKeys.calculationElection).map {
        case Some(data) =>
          Ok(calculation.nonresident.calculationElection(
            calculationElectionForm.fill(data),
            content)
          )
        case None =>
          Ok(calculation.nonresident.calculationElection(
            calculationElectionForm,
            content)
          )
      }

    for {
      totalGainAnswers <- calcAnswersConstructor.getNRTotalGainAnswers(hc)
      totalGain <- calcConnector.calculateTotalGain(totalGainAnswers)(hc)
      prrAnswers <- getPRRResponse(totalGain.get)(hc)
      totalGainWithPRR <- getPRRIfApplicable(totalGainAnswers, prrAnswers)
      allAnswers <- getFinalSectionsAnswers(totalGain.get, totalGainWithPRR)
      otherReliefs <- getAllOtherReliefs(allAnswers)
      taxYear <- getTaxYear(totalGainAnswers)
      maxAEA <- getMaxAEA(allAnswers, taxYear)
      taxOwed <- getTaxOwedIfApplicable(totalGainAnswers, prrAnswers, allAnswers, maxAEA.get, otherReliefs)
      content <- calcElectionConstructor.generateElection(totalGain.get, totalGainWithPRR, taxOwed, otherReliefs)
      finalResult <- action(content)
    } yield finalResult
  }

  val submitCalculationElection = ValidateSession.async { implicit request =>

    def successAction(model: CalculationElectionModel) = {
      calcConnector.saveFormData(KeystoreKeys.calculationElection, model)
      request.body.asFormUrlEncoded.flatMap(_.get("action").map(_.head)) match {
        case Some("flat") => Future.successful(Redirect(routes.OtherReliefsFlatController.otherReliefsFlat()))
        case Some("time") => Future.successful(Redirect(routes.OtherReliefsTAController.otherReliefsTA()))
        case Some("rebased") => Future.successful(Redirect(routes.OtherReliefsRebasedController.otherReliefsRebased()))
        case _ => Future.successful(Redirect(routes.SummaryController.summary()))
      }
    }

    def errorAction(form: Form[CalculationElectionModel]) = {
      for {
        totalGainAnswers <- calcAnswersConstructor.getNRTotalGainAnswers(hc)
        totalGain <- calcConnector.calculateTotalGain(totalGainAnswers)(hc)
        prrAnswers <- getPRRResponse(totalGain.get)(hc)
        totalGainWithPRR <- getPRRIfApplicable(totalGainAnswers, prrAnswers)
        allAnswers <- getFinalSectionsAnswers(totalGain.get, totalGainWithPRR)
        otherReliefs <- getAllOtherReliefs(allAnswers)
        taxYear <- getTaxYear(totalGainAnswers)
        maxAEA <- getMaxAEA(allAnswers, taxYear)
        taxOwed <- getTaxOwedIfApplicable(totalGainAnswers, prrAnswers, allAnswers, maxAEA.get, otherReliefs)
        content <- calcElectionConstructor.generateElection(totalGain.get, totalGainWithPRR, taxOwed, otherReliefs)
      } yield {
        BadRequest(calculation.nonresident.calculationElection(
          form,
          content
        ))
      }
    }

    calculationElectionForm.bindFromRequest.fold(errorAction, successAction)
  }

}
