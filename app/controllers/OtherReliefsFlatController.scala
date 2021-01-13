/*
 * Copyright 2021 HM Revenue & Customs
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
import controllers.utils.RecoverableFuture
import forms.OtherReliefsForm._
import javax.inject.Inject
import models._
import play.api.Application
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation

import scala.concurrent.ExecutionContext.Implicits.global

class OtherReliefsFlatController @Inject()(http: DefaultHttpClient,calcConnector: CalculatorConnector,
                                           answersConstructor: AnswersConstructor,
                                           mcc: MessagesControllerComponents)(implicit val applicationConfig: ApplicationConfig,
                                                                              implicit val application: Application)
                                            extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val otherReliefsFlat: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(model: Option[OtherReliefsModel],
                     totalGain: Option[TotalGainResultsModel],
                     chargeableGainResult: Option[CalculationResultsWithTaxOwedModel]) = {
      val hasExistingRelief = model.isDefined
      val gain = totalGain.fold(BigDecimal(0))(_.flatGain)
      val chargeableGain = chargeableGainResult.fold(BigDecimal(0))(_.flatResult.taxableGain)

      val result = model.fold(calculation.otherReliefsFlat(otherReliefsForm, hasExistingRelief, chargeableGain, gain)) { data =>
        calculation.otherReliefsFlat(otherReliefsForm.fill(data), hasExistingRelief, chargeableGain, gain)
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

  val submitOtherReliefsFlat: Action[AnyContent] = ValidateSession.async { implicit request =>

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
      calcConnector.saveFormData(KeystoreKeys.otherReliefsFlat, model).map(_ =>
        Redirect(routes.CalculationElectionController.calculationElection()))
    }

    def errorRoute(totalGain: Option[TotalGainResultsModel], chargeableGainResult: Option[CalculationResultsWithTaxOwedModel],
                   form: Form[OtherReliefsModel]) = {
      val gain = totalGain.fold(BigDecimal(0))(_.flatGain)
      val chargeableGain = chargeableGainResult.fold(BigDecimal(0))(_.flatResult.taxableGain)

      calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat).map {
        case Some(_) => BadRequest(calculation.otherReliefsFlat(form, hasExistingReliefAmount = true, chargeableGain, gain))
        case _ => BadRequest(calculation.otherReliefsFlat(form, hasExistingReliefAmount = false, chargeableGain, gain))
      }
    }

    otherReliefsForm.bindFromRequest.fold(errorAction, successAction)
  }
}
