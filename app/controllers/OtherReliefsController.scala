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
import models._
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.calculation.otherReliefs

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class OtherReliefsController @Inject()(calcConnector: CalculatorConnector,
                                       sessionCacheService: SessionCacheService,
                                       answersConstructor: AnswersConstructor,
                                       mcc: MessagesControllerComponents,
                                       otherReliefsView: otherReliefs
                                      )(implicit ec: ExecutionContext)
                                        extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val otherReliefs: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(model: Option[OtherReliefsModel],
                     totalGain: Option[TotalGainResultsModel],
                     chargeableGainResult: Option[CalculationResultsWithTaxOwedModel]) = {
      val gain = totalGain.fold(BigDecimal(0))(_.flatGain)
      val chargeableGain = chargeableGainResult.fold(BigDecimal(0))(_.flatResult.taxableGain)

      val result = model.fold(otherReliefsView(otherReliefsForm, chargeableGain, gain)) { data =>
        otherReliefsView(otherReliefsForm.fill(data), chargeableGain, gain)
      }

      Ok(result)
    }

    (for {
      answers <- answersConstructor.getNRTotalGainAnswers
      gain <- calcConnector.calculateTotalGain(answers)(using hc)
      gainExists <- checkGainExists(gain.get)
      propertyLivedIn <- getPropertyLivedInResponse(gainExists, sessionCacheService)
      prrAnswers <- getPrrResponse(propertyLivedIn, sessionCacheService)
      totalGainWithPRR <- getPrrIfApplicable(answers, prrAnswers, propertyLivedIn, calcConnector)(using hc)
      allAnswers <- getFinalSectionsAnswers(gain.get, totalGainWithPRR, answersConstructor)
      taxYear <- getTaxYear(answers, calcConnector)(using hc)
      maxAEA <- getMaxAEA(taxYear, calcConnector)(using hc)
      chargeableGainResult <- getChargeableGain(answers, prrAnswers, propertyLivedIn, allAnswers, maxAEA.get, calcConnector)
      reliefs <- sessionCacheService.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat)
    } yield routeRequest(reliefs, gain, chargeableGainResult)).recoverToStart

  }

  val submitOtherReliefs: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(form: Form[OtherReliefsModel]) = {

      def routeRequest(totalGain: Option[TotalGainResultsModel], chargeableGainResult: Option[CalculationResultsWithTaxOwedModel]) = {
        val gain = totalGain.fold(BigDecimal(0))(_.flatGain)
        val chargeableGain = chargeableGainResult.fold(BigDecimal(0))(_.flatResult.taxableGain)

        BadRequest(otherReliefsView(form, chargeableGain, gain))
      }

      (for {
        answers <- answersConstructor.getNRTotalGainAnswers
        gain <- calcConnector.calculateTotalGain(answers)(using hc)
        gainExists <- checkGainExists(gain.get)
        propertyLivedIn <- getPropertyLivedInResponse(gainExists, sessionCacheService)
        prrAnswers <- getPrrResponse(propertyLivedIn, sessionCacheService)
        totalGainWithPRR <- getPrrIfApplicable(answers, prrAnswers, propertyLivedIn, calcConnector)(using hc)
        allAnswers <- getFinalSectionsAnswers(gain.get, totalGainWithPRR, answersConstructor)
        taxYear <- getTaxYear(answers, calcConnector)(using hc)
        maxAEA <- getMaxAEA(taxYear, calcConnector)(using hc)
        chargeableGainResult <- getChargeableGain(answers, prrAnswers, propertyLivedIn, allAnswers, maxAEA.get, calcConnector)
      } yield routeRequest(gain, chargeableGainResult)).recoverToStart
    }

    def successAction(model: OtherReliefsModel) = {
      sessionCacheService.saveFormData(KeystoreKeys.otherReliefsFlat, model).map(_ =>
        Redirect(routes.SummaryController.summary))
    }

    otherReliefsForm.bindFromRequest().fold(
      errors => errorAction(errors),
      success => successAction(success))
  }
}
