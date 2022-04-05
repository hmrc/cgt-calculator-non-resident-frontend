/*
 * Copyright 2022 HM Revenue & Customs
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
import constructors.{AnswersConstructor, DefaultCalculationElectionConstructor}
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.CalculationElectionForm._
import javax.inject.Inject
import models._
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.{calculationElection, calculationElectionNoReliefs}

import scala.concurrent.{ExecutionContext, Future}


class CalculationElectionController @Inject()(http: DefaultHttpClient,calcConnector: CalculatorConnector,
                                              calcAnswersConstructor: AnswersConstructor,
                                              calcElectionConstructor: DefaultCalculationElectionConstructor,
                                              mcc: MessagesControllerComponents,
                                              calculationElectionView: calculationElection,
                                              calculationElectionNoReliefsView: calculationElectionNoReliefs)(implicit ec: ExecutionContext)
                                             extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  def orderElements(content: Seq[(String, String, String, String, Option[String], Option[BigDecimal])],
                    claimingReliefs: Boolean): Seq[(String, String, String, String, Option[String], Option[BigDecimal])] = {
    if (claimingReliefs) {
      val seq = Seq("rebased", "time", "flat")

      def sort(s1: (String, String, String, String, Option[String], Option[BigDecimal]),
               s2: (String, String, String, String, Option[String], Option[BigDecimal])) = {
        seq.indexOf(s1._1) < seq.indexOf(s2._1)
      }

      content.sortWith(sort)
    }
    else content.map { element =>
      (element._1, element._2, element._3, element._4, element._5, None)
    }
  }

  def determineClaimingReliefs(totalGainResultsModel: TotalGainResultsModel)(implicit hc: HeaderCarrier): Future[Boolean] = {
    val optionSeq = Seq(totalGainResultsModel.rebasedGain, totalGainResultsModel.timeApportionedGain).flatten
    val finalSeq = Seq(totalGainResultsModel.flatGain) ++ optionSeq
    if (finalSeq.exists(_ > 0)) calcConnector.fetchAndGetFormData[ClaimingReliefsModel](KeystoreKeys.claimingReliefs).map {
      case Some(model) => model.isClaimingReliefs
      case _ => throw new Exception("Claiming reliefs model not found.")
    }
    else Future.successful(false)
  }

  private def checkGainExists(totalGainResultsModel: TotalGainResultsModel): Future[Boolean] = {
    val optionSeq = Seq(totalGainResultsModel.rebasedGain, totalGainResultsModel.timeApportionedGain).flatten
    val finalSeq = Seq(totalGainResultsModel.flatGain) ++ optionSeq

    Future.successful(finalSeq.exists(_ > 0))
  }

  private def getBackLink(totalGainResultsModel: TotalGainResultsModel): Future[String] = {
    val results = Seq(totalGainResultsModel.flatGain) ++ Seq(totalGainResultsModel.rebasedGain, totalGainResultsModel.timeApportionedGain).flatten

    if (results.exists(_ > 0)) {
      Future.successful(routes.ClaimingReliefsController.claimingReliefs().url)
    } else Future.successful(routes.CheckYourAnswersController.checkYourAnswers().url)
  }

  private def getTaxOwedIfApplicable(totalGainAnswersModel: TotalGainAnswersModel,
                                     prrModel: Option[PrivateResidenceReliefModel],
                                     totalTaxOwedModel: Option[TotalPersonalDetailsCalculationModel],
                                     maxAEA: BigDecimal,
                                     otherReliefs: Option[AllOtherReliefsModel],
                                     propertyLivedInModel: Option[PropertyLivedInModel])
                                    (implicit hc: HeaderCarrier): Future[Option[CalculationResultsWithTaxOwedModel]] = {

    totalTaxOwedModel match {
      case Some(_) => calcConnector.calculateNRCGTTotalTax(totalGainAnswersModel,
        prrModel,
        propertyLivedInModel,
        totalTaxOwedModel,
        maxAEA,
        otherReliefs)
      case None => Future(None)
    }
  }

  private def getAllOtherReliefs(totalGainResultsModel: TotalGainResultsModel)
                                (implicit hc: HeaderCarrier): Future[Option[AllOtherReliefsModel]] = {
    val results = Seq(totalGainResultsModel.flatGain) ++ Seq(totalGainResultsModel.rebasedGain, totalGainResultsModel.timeApportionedGain).flatten

    if (results.exists(_ > 0)) {
      val flat = calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat)
      val rebased = calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsRebased)
      val time = calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsTA)

      for {
        flatReliefs <- flat
        rebasedReliefs <- rebased
        timeReliefs <- time
      } yield Some(AllOtherReliefsModel(flatReliefs, rebasedReliefs, timeReliefs))
    } else Future.successful(None)
  }

  val calculationElection: Action[AnyContent] = ValidateSession.async { implicit request =>

    def action(content: Seq[(String, String, String, String, Option[String], Option[BigDecimal])], isClaimingReliefs: Boolean, backLink: String) =
      calcConnector.fetchAndGetFormData[CalculationElectionModel](KeystoreKeys.calculationElection).map { result =>
        val form = result match {
          case Some(data) => calculationElectionForm.fill(data)
          case _ => calculationElectionForm
        }

        if (isClaimingReliefs) Ok(calculationElectionView(form, content))
        else Ok(calculationElectionNoReliefsView(form, content, backLink))
      }

    (for {
      totalGainAnswers <- calcAnswersConstructor.getNRTotalGainAnswers
      totalGain <- calcConnector.calculateTotalGain(totalGainAnswers)
      gainExists <- checkGainExists(totalGain.get)
      propertyLivedIn <- getPropertyLivedInResponse(gainExists, calcConnector)
      prrAnswers <- getPrrResponse(propertyLivedIn, calcConnector)
      isClaimingReliefs <- determineClaimingReliefs(totalGain.get)
      totalGainWithPRR <- getPrrIfApplicable(totalGainAnswers, prrAnswers, propertyLivedIn, calcConnector)
      allAnswers <- getFinalSectionsAnswers(totalGain.get, totalGainWithPRR, calcConnector, calcAnswersConstructor)
      backLink <- getBackLink(totalGain.get)
      otherReliefs <- getAllOtherReliefs(totalGain.get)
      taxYear <- getTaxYear(totalGainAnswers, calcConnector)
      maxAEA <- getMaxAEA(taxYear, calcConnector)
      taxOwed <- getTaxOwedIfApplicable(totalGainAnswers, prrAnswers, allAnswers, maxAEA.get, otherReliefs, propertyLivedIn)
      content <- calcElectionConstructor.generateElection(totalGain.get, totalGainWithPRR, taxOwed, otherReliefs)
      finalResult <- action(orderElements(content, isClaimingReliefs), isClaimingReliefs, backLink)
    } yield finalResult).recoverToStart
  }

  val submitCalculationElection: Action[AnyContent] = ValidateSession.async { implicit request =>

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

      def action(content: Seq[(String, String, String, String, Option[String], Option[BigDecimal])], isClaimingReliefs: Boolean, backLink: String) = {
        if (isClaimingReliefs) BadRequest(calculationElectionView(form, content))
        else BadRequest(calculationElectionNoReliefsView(form, content, backLink))
      }

      (for {
        totalGainAnswers <- calcAnswersConstructor.getNRTotalGainAnswers
        totalGain <- calcConnector.calculateTotalGain(totalGainAnswers)
        gainExists <- checkGainExists(totalGain.get)
        propertyLivedIn <- getPropertyLivedInResponse(gainExists, calcConnector)
        prrAnswers <- getPrrResponse(propertyLivedIn, calcConnector)
        isClaimingReliefs <- determineClaimingReliefs(totalGain.get)
        totalGainWithPRR <- getPrrIfApplicable(totalGainAnswers, prrAnswers, propertyLivedIn, calcConnector)
        allAnswers <- getFinalSectionsAnswers(totalGain.get, totalGainWithPRR, calcConnector, calcAnswersConstructor)
        backLink <- getBackLink(totalGain.get)
        otherReliefs <- getAllOtherReliefs(totalGain.get)
        taxYear <- getTaxYear(totalGainAnswers, calcConnector)
        maxAEA <- getMaxAEA(taxYear, calcConnector)
        taxOwed <- getTaxOwedIfApplicable(totalGainAnswers, prrAnswers, allAnswers, maxAEA.get, otherReliefs, propertyLivedIn)
        content <- calcElectionConstructor.generateElection(totalGain.get, totalGainWithPRR, taxOwed, otherReliefs)
      } yield {
        action(orderElements(content, isClaimingReliefs), isClaimingReliefs, backLink)
      }).recoverToStart
    }

    calculationElectionForm.bindFromRequest.fold(errorAction, successAction)
  }
}
