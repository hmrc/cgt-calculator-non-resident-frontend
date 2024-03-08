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
import common.nonresident.Flat
import common.nonresident.TaxableGainCalculation._
import connectors.CalculatorConnector
import constructors.{AnswersConstructor, YourAnswersConstructor}
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import models._
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.checkYourAnswers

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(http: DefaultHttpClient,
                                           calculatorConnector: CalculatorConnector,
                                           sessionCacheService: SessionCacheService,
                                           answersConstructor: AnswersConstructor,
                                           mcc: MessagesControllerComponents,
                                           checkYourAnswersView: checkYourAnswers)(implicit ec: ExecutionContext)
                                              extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  def getBackLink(totalGainResultsModel: TotalGainResultsModel,
                  acquisitionDateController: DateModel,
                  totalTaxOwedAnswers: Option[TotalPersonalDetailsCalculationModel]): Future[String] = totalTaxOwedAnswers match {

    case Some(_) =>
      Future.successful(controllers.routes.BroughtForwardLossesController.broughtForwardLosses.url)
    case _ =>
      checkGainExists(totalGainResultsModel).map { gain =>
        if(gain) controllers.routes.PrivateResidenceReliefController.privateResidenceRelief.url
        else controllers.routes.ImprovementsController.improvementsRebased.url
      }
  }

  def redirectRoute(calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel],
                    totalGainResultsModel: TotalGainResultsModel): Future[Result] = {
    (calculationResultsWithPRRModel, totalGainResultsModel) match {
      case (Some(CalculationResultsWithPRRModel(data, _, _)),_) if data.taxableGain > 0 =>
        Future.successful(Redirect(routes.OtherReliefsController.otherReliefs))
      case (None, TotalGainResultsModel(data, _, _)) if data > 0 =>
        Future.successful(Redirect(routes.OtherReliefsController.otherReliefs))
      case _ => Future.successful(Redirect(routes.SummaryController.summary))
    }
  }

  val checkYourAnswers: Action[AnyContent] = ValidateSession.async { implicit request =>
    implicit val lang: Lang = mcc.messagesApi.preferred(request).lang

    (for {
      model <- answersConstructor.getNRTotalGainAnswers
      totalGainResult <- calculatorConnector.calculateTotalGain(model)
      gainExists <- checkGainExists(totalGainResult.get)
      propertyLivedIn <- getPropertyLivedInResponse(gainExists, sessionCacheService)
      prrModel <- getPrrResponse(propertyLivedIn, sessionCacheService)
      totalGainWithPRRResult <- getPrrIfApplicable(model, prrModel, propertyLivedIn, calculatorConnector)
      finalAnswers <- getFinalSectionsAnswers(totalGainResult.get, totalGainWithPRRResult, answersConstructor)
      answers <- Future.successful(YourAnswersConstructor.fetchYourAnswers(model, prrModel, finalAnswers, propertyLivedIn))
      backLink <- getBackLink(totalGainResult.get, model.acquisitionDateModel, finalAnswers)
    } yield {
      Ok(checkYourAnswersView(answers, backLink))
    }).recoverToStart
  }

  val submitCheckYourAnswers: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(model: Option[TotalGainResultsModel], taxableGainModel: Option[CalculationResultsWithPRRModel]) = model match {
      case Some(data) if data.rebasedGain.isDefined || data.timeApportionedGain.isDefined =>
        val seq = Seq(Some(data.flatGain), data.rebasedGain, data.timeApportionedGain).flatten
        if (seq.forall(_ <= 0)) Future.successful(Redirect(routes.CalculationElectionController.calculationElection))
        else Future.successful(Redirect(routes.ClaimingReliefsController.claimingReliefs))
      case Some(_) =>
        sessionCacheService.saveFormData[CalculationElectionModel](KeystoreKeys.calculationElection, CalculationElectionModel(Flat))
        redirectRoute(taxableGainModel, model.get)
      case _ => sys.error("TotalGainResultModel is missing")
    }

    (for {
      allAnswersModel <- answersConstructor.getNRTotalGainAnswers
      totalGains <- calculatorConnector.calculateTotalGain(allAnswersModel)
      gainExists <- checkGainExists(totalGains.get)
      propertyLivedIn <- getPropertyLivedInResponse(gainExists, sessionCacheService)
      prrModel <- getPrrResponse(propertyLivedIn, sessionCacheService)
      taxableGainWithPrr <- getPrrIfApplicable(allAnswersModel, prrModel, propertyLivedIn, calculatorConnector)
      route <- routeRequest(totalGains, taxableGainWithPrr)
    } yield route).recoverToStart
  }
}
