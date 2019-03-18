/*
 * Copyright 2019 HM Revenue & Customs
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
import config.ApplicationConfig
import connectors.CalculatorConnector
import constructors.AnswersConstructor
import controllers.predicates.ValidActiveSession
import forms.OtherReliefsForm._
import views.html.calculation
import models._
import play.api.data.Form
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import controllers.utils.RecoverableFuture
import javax.inject.Inject
import play.api.Environment
import play.api.i18n.I18nSupport
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class OtherReliefsController @Inject()(http: DefaultHttpClient,calcConnector: CalculatorConnector,
                                       answersConstructor: AnswersConstructor,
                                       mcc: MessagesControllerComponents)(implicit val applicationConfig: ApplicationConfig)
                                        extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val otherReliefs: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(model: Option[OtherReliefsModel],
                     totalGain: Option[TotalGainResultsModel],
                     chargeableGainResult: Option[CalculationResultsWithTaxOwedModel]) = {
      val gain = totalGain.fold(BigDecimal(0))(_.flatGain)
      val chargeableGain = chargeableGainResult.fold(BigDecimal(0))(_.flatResult.taxableGain)

      val result = model.fold(calculation.otherReliefs(otherReliefsForm, chargeableGain, gain)) { data =>
        calculation.otherReliefs(otherReliefsForm.fill(data), chargeableGain, gain)
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
      reliefs <- calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat)
    } yield routeRequest(reliefs, gain, chargeableGainResult)).recoverToStart

  }

  val submitOtherReliefs: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(form: Form[OtherReliefsModel]) = {

      def routeRequest(totalGain: Option[TotalGainResultsModel], chargeableGainResult: Option[CalculationResultsWithTaxOwedModel]) = {
        val gain = totalGain.fold(BigDecimal(0))(_.flatGain)
        val chargeableGain = chargeableGainResult.fold(BigDecimal(0))(_.flatResult.taxableGain)

        BadRequest(calculation.otherReliefs(form, chargeableGain, gain))
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
      } yield routeRequest(gain, chargeableGainResult)).recoverToStart
    }

    def successAction(model: OtherReliefsModel) = {
      calcConnector.saveFormData(KeystoreKeys.otherReliefsFlat, model).map(_ =>
        Redirect(routes.SummaryController.summary()))
    }

    otherReliefsForm.bindFromRequest.fold(
      errors => errorAction(errors),
      success => successAction(success))
  }
}
