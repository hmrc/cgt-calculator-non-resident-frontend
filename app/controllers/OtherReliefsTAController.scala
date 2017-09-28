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
import common.nonresident.TaxableGainCalculation._
import connectors.CalculatorConnector
import constructors.AnswersConstructor
import controllers.predicates.ValidActiveSession
import forms.OtherReliefsForm._
import views.html.calculation
import models.{CalculationResultsWithTaxOwedModel, OtherReliefsModel, TotalGainResultsModel}
import play.api.data.Form
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.{Action, AnyContent}
import controllers.utils.RecoverableFuture

import scala.concurrent.Future

object OtherReliefsTAController extends OtherReliefsTAController {
  val calcConnector = CalculatorConnector
  val answersConstructor = AnswersConstructor
}

trait OtherReliefsTAController extends FrontendController with ValidActiveSession {

  val answersConstructor: AnswersConstructor
  val calcConnector: CalculatorConnector

  val otherReliefsTA: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(model: Option[OtherReliefsModel],
      totalGain: Option[TotalGainResultsModel],
      chargeableGainResult: Option[CalculationResultsWithTaxOwedModel]) = {
      val hasExistingRelief = model.isDefined
      val gain = totalGain.fold(BigDecimal(0))(_.timeApportionedGain.get)
      val chargeableGain = chargeableGainResult.fold(BigDecimal(0))(_.timeApportionedResult.get.taxableGain)

      val result = model.fold(calculation.otherReliefsTA(otherReliefsForm, hasExistingRelief, chargeableGain, gain)) { data =>
        calculation.otherReliefsTA(otherReliefsForm.fill(data), hasExistingRelief, chargeableGain, gain)
      }

      Ok(result)
    }
    (for {
        answers <- answersConstructor.getNRTotalGainAnswers(hc)
        gain <- calcConnector.calculateTotalGain(answers)(hc)
        gainExists <- checkGainExists(gain.get)
        propertyLivedIn <- getPropertyLivedInResponse(gainExists, calcConnector)
        prrAnswers <- getPrrResponse(propertyLivedIn, calcConnector)
        totalGainWithPRR <- getPrrIfApplicable(answers, prrAnswers, propertyLivedIn, calcConnector)(hc)
        allAnswers <- getFinalSectionsAnswers(gain.get, totalGainWithPRR, calcConnector, answersConstructor)(hc)
        taxYear <- getTaxYear(answers, calcConnector)(hc)
        maxAEA <- getMaxAEA(taxYear, calcConnector)(hc)
        chargeableGainResult <- getChargeableGain(answers, prrAnswers, propertyLivedIn, allAnswers, maxAEA.get, calcConnector)(hc)
        reliefs <- calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsTA)
      } yield routeRequest(reliefs, gain, chargeableGainResult)).recoverToStart
  }

  val submitOtherReliefsTA: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(form: Form[OtherReliefsModel]) = {
      (for {
        answers <- answersConstructor.getNRTotalGainAnswers(hc)
        gain <- calcConnector.calculateTotalGain(answers)(hc)
        gainExists <- checkGainExists(gain.get)
        propertyLivedIn <- getPropertyLivedInResponse(gainExists, calcConnector)
        prrAnswers <- getPrrResponse(propertyLivedIn, calcConnector)
        totalGainWithPRR <- getPrrIfApplicable(answers, prrAnswers, propertyLivedIn, calcConnector)(hc)
        allAnswers <- getFinalSectionsAnswers(gain.get, totalGainWithPRR, calcConnector, answersConstructor)(hc)
        taxYear <- getTaxYear(answers, calcConnector)(hc)
        maxAEA <- getMaxAEA(taxYear, calcConnector)(hc)
        chargeableGainResult <- getChargeableGain(answers, prrAnswers, propertyLivedIn, allAnswers, maxAEA.get, calcConnector)(hc)
        route <- errorRoute(gain, chargeableGainResult, form)
      } yield route).recoverToStart
    }

    def successAction(model: OtherReliefsModel) = {
      calcConnector.saveFormData(KeystoreKeys.otherReliefsTA, model).map(_ =>
        Redirect(routes.CalculationElectionController.calculationElection()))
    }

    def errorRoute(totalGain: Option[TotalGainResultsModel], chargeableGainResult: Option[CalculationResultsWithTaxOwedModel],
                   form: Form[OtherReliefsModel]) = {
      val gain = totalGain.fold(BigDecimal(0))(_.timeApportionedGain.get)
      val chargeableGain = chargeableGainResult.fold(BigDecimal(0))(_.timeApportionedResult.get.taxableGain)


      calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsTA).map {
        case Some(_) => BadRequest(calculation.otherReliefsTA(form, hasExistingReliefAmount = true, chargeableGain, gain))
        case _ => BadRequest(calculation.otherReliefsTA(form, hasExistingReliefAmount = false, chargeableGain, gain))
      }
    }

    otherReliefsForm.bindFromRequest.fold(errorAction, successAction)
  }

}
