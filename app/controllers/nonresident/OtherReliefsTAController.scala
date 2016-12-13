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
import common.nonresident.TaxableGainCalculation._
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.nonresident.OtherReliefsForm._
import models.nonresident.{CalculationResultsWithTaxOwedModel, OtherReliefsModel, TotalGainResultsModel}
import play.api.data.Form
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.calculation
import constructors.nonresident.AnswersConstructor


import scala.concurrent.Future

object OtherReliefsTAController extends OtherReliefsTAController {
  val calcConnector = CalculatorConnector
  val answersConstructor = AnswersConstructor
}

trait OtherReliefsTAController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.DisposalDateController.disposalDate().url
  val answersConstructor: AnswersConstructor
  val calcConnector: CalculatorConnector

  val otherReliefsTA = ValidateSession.async { implicit request =>

    def routeRequest(model: Option[OtherReliefsModel],
      totalGain: Option[TotalGainResultsModel],
      chargeableGainResult: Option[CalculationResultsWithTaxOwedModel]) = {
      val hasExistingRelief = model.isDefined
      val gain = totalGain.fold(BigDecimal(0))(_.timeApportionedGain.get)
      val chargeableGain = chargeableGainResult.fold(BigDecimal(0))(_.timeApportionedResult.get.taxableGain)

      val result = model.fold(calculation.nonresident.otherReliefsTA(otherReliefsForm, hasExistingRelief, chargeableGain, gain)) { data =>
        calculation.nonresident.otherReliefsTA(otherReliefsForm.fill(data), hasExistingRelief, chargeableGain, gain)
      }

      Ok(result)
    }
      for {
        answers <- answersConstructor.getNRTotalGainAnswers(hc)
        gain <- calcConnector.calculateTotalGain(answers)(hc)
        prrAnswers <- getPRRResponse(gain.get, calcConnector)
        totalGainWithPRR <- getPRRIfApplicable(answers, prrAnswers, calcConnector)(hc)
        allAnswers <- getFinalSectionsAnswers(gain.get, totalGainWithPRR, calcConnector, answersConstructor)(hc)
        taxYear <- getTaxYear(answers, calcConnector)(hc)
        maxAEA <- getMaxAEA(allAnswers, taxYear, calcConnector)(hc)
        chargeableGainResult <- getChargeableGain(answers, prrAnswers, allAnswers, maxAEA.get, calcConnector)(hc)
        reliefs <- calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsTA)
      } yield routeRequest(reliefs, gain, chargeableGainResult)
  }

  val submitOtherReliefsTA = ValidateSession.async { implicit request =>


    def errorAction(form: Form[OtherReliefsModel]) = {
      for {
        answers <- answersConstructor.getNRTotalGainAnswers(hc)
        gain <- calcConnector.calculateTotalGain(answers)(hc)
        prrAnswers <- getPRRResponse(gain.get, calcConnector)
        totalGainWithPRR <- getPRRIfApplicable(answers, prrAnswers, calcConnector)(hc)
        allAnswers <- getFinalSectionsAnswers(gain.get, totalGainWithPRR, calcConnector, answersConstructor)(hc)
        taxYear <- getTaxYear(answers, calcConnector)(hc)
        maxAEA <- getMaxAEA(allAnswers, taxYear, calcConnector)(hc)
        chargeableGainResult <- getChargeableGain(answers, prrAnswers, allAnswers, maxAEA.get, calcConnector)(hc)
        route <- errorRoute(gain, chargeableGainResult, form)
      } yield route
    }

    def successAction(model: OtherReliefsModel) = {
      calcConnector.saveFormData(KeystoreKeys.otherReliefsTA, model)
      Future.successful(Redirect(routes.CalculationElectionController.calculationElection()))
    }

    def errorRoute(totalGain: Option[TotalGainResultsModel], chargeableGainResult: Option[CalculationResultsWithTaxOwedModel],
                   form: Form[OtherReliefsModel]) = {
      val gain = totalGain.fold(BigDecimal(0))(_.timeApportionedGain.get)
      val chargeableGain = chargeableGainResult.fold(BigDecimal(0))(_.timeApportionedResult.get.taxableGain)


      calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsTA).map {
        case Some(data) => BadRequest(calculation.nonresident.otherReliefsTA(form, hasExistingReliefAmount = true, chargeableGain, gain))
        case _ => BadRequest(calculation.nonresident.otherReliefsTA(form, hasExistingReliefAmount = false, chargeableGain, gain))
      }
    }

    otherReliefsForm.bindFromRequest.fold(errorAction, successAction)
  }

}
