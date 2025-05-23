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
import connectors.CalculatorConnector
import constructors.AnswersConstructor
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.OtherReliefsForm._
import models.{CalculationResultsWithTaxOwedModel, OtherReliefsModel, TotalGainResultsModel}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.calculation.otherReliefsTA

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class OtherReliefsTAController @Inject()(calcConnector: CalculatorConnector,
                                         sessionCacheService: SessionCacheService,
                                         answersConstructor: AnswersConstructor,
                                         mcc: MessagesControllerComponents,
                                         otherReliefsTAView: otherReliefsTA)
                                        (implicit ec: ExecutionContext)
                                          extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val otherReliefsTA: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(model: Option[OtherReliefsModel],
      totalGain: Option[TotalGainResultsModel],
      chargeableGainResult: Option[CalculationResultsWithTaxOwedModel]) = {
      val hasExistingRelief = model.isDefined
      val gain = totalGain.fold(BigDecimal(0))(_.timeApportionedGain.get)
      val chargeableGain = chargeableGainResult.fold(BigDecimal(0))(_.timeApportionedResult.get.taxableGain)

      val result = model.fold(otherReliefsTAView(otherReliefsForm, hasExistingRelief, chargeableGain, gain)) { data =>
        otherReliefsTAView(otherReliefsForm.fill(data), hasExistingRelief, chargeableGain, gain)
      }

      Ok(result)
    }
    (for {
        answers <- answersConstructor.getNRTotalGainAnswers(using request)
        gain <- calcConnector.calculateTotalGain(answers)(using hc)
        gainExists <- checkGainExists(gain.get)
        propertyLivedIn <- getPropertyLivedInResponse(gainExists, sessionCacheService)
        prrAnswers <- getPrrResponse(propertyLivedIn, sessionCacheService)
        totalGainWithPRR <- getPrrIfApplicable(answers, prrAnswers, propertyLivedIn, calcConnector)(using hc)
        allAnswers <- getFinalSectionsAnswers(gain.get, totalGainWithPRR, answersConstructor)
        taxYear <- getTaxYear(answers, calcConnector)(using hc)
        maxAEA <- getMaxAEA(taxYear, calcConnector)(using hc)
        chargeableGainResult <- getChargeableGain(answers, prrAnswers, propertyLivedIn, allAnswers, maxAEA.get, calcConnector)
        reliefs <- sessionCacheService.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsTA)
      } yield routeRequest(reliefs, gain, chargeableGainResult)).recoverToStart
  }

  val submitOtherReliefsTA: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(form: Form[OtherReliefsModel]) = {
      (for {
        answers <- answersConstructor.getNRTotalGainAnswers(using request)
        gain <- calcConnector.calculateTotalGain(answers)(using hc)
        gainExists <- checkGainExists(gain.get)
        propertyLivedIn <- getPropertyLivedInResponse(gainExists, sessionCacheService)
        prrAnswers <- getPrrResponse(propertyLivedIn, sessionCacheService)
        totalGainWithPRR <- getPrrIfApplicable(answers, prrAnswers, propertyLivedIn, calcConnector)(using hc)
        allAnswers <- getFinalSectionsAnswers(gain.get, totalGainWithPRR, answersConstructor)
        taxYear <- getTaxYear(answers, calcConnector)(using hc)
        maxAEA <- getMaxAEA(taxYear, calcConnector)(using hc)
        chargeableGainResult <- getChargeableGain(answers, prrAnswers, propertyLivedIn, allAnswers, maxAEA.get, calcConnector)
        route <- errorRoute(gain, chargeableGainResult, form)
      } yield route).recoverToStart
    }

    def successAction(model: OtherReliefsModel) = {
      sessionCacheService.saveFormData(KeystoreKeys.otherReliefsTA, model).map(_ =>
        Redirect(routes.CalculationElectionController.calculationElection))
    }

    def errorRoute(totalGain: Option[TotalGainResultsModel], chargeableGainResult: Option[CalculationResultsWithTaxOwedModel],
                   form: Form[OtherReliefsModel]) = {
      val gain = totalGain.fold(BigDecimal(0))(_.timeApportionedGain.get)
      val chargeableGain = chargeableGainResult.fold(BigDecimal(0))(_.timeApportionedResult.get.taxableGain)


      sessionCacheService.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsTA).map {
        case Some(_) => BadRequest(otherReliefsTAView(form, hasExistingReliefAmount = true, chargeableGain, gain))
        case _ => BadRequest(otherReliefsTAView(form, hasExistingReliefAmount = false, chargeableGain, gain))
      }
    }

    otherReliefsForm.bindFromRequest().fold(errorAction, successAction)
  }

}
