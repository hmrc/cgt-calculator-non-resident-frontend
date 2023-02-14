/*
 * Copyright 2023 HM Revenue & Customs
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
import common.nonresident.CalculationType
import common.nonresident.TaxableGainCalculation._
import connectors.CalculatorConnector
import constructors.{AnswersConstructor, YourAnswersConstructor}
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import it.innove.play.pdf.PdfGenerator
import javax.inject.Inject
import models._
import play.api.Configuration
import play.api.i18n.{I18nSupport, Lang, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, RequestHeader}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.summaryReport

import scala.concurrent.{ExecutionContext, Future}


class ReportController @Inject()(config: Configuration,
                                 http: DefaultHttpClient,
                                 calcConnector: CalculatorConnector,
                                 answersConstructor: AnswersConstructor,
                                 mcc: MessagesControllerComponents,
                                 pdfGenerator: PdfGenerator,
                                 summaryReportView: summaryReport)(implicit ec: ExecutionContext)
                                  extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  lazy val platformHost: Option[String] = config.getOptional[String]("platform.frontend.host")

  def host(implicit request: RequestHeader): String =
    if (platformHost.isDefined) s"https://${request.host}" else s"http://${request.host}"

  val summaryReport: Action[AnyContent] = ValidateSession.async { implicit request =>

    def getCalculationResult(calculationResultsWithTaxOwedModel: Option[CalculationResultsWithTaxOwedModel],
                             calculationElection: String): Future[TotalTaxOwedModel] = {
      (calculationResultsWithTaxOwedModel, calculationElection) match {
        case (Some(model), CalculationType.flat) => Future.successful(model.flatResult)
        case (Some(model), CalculationType.rebased) => Future.successful(model.rebasedResult.get)
        case (Some(model), CalculationType.timeApportioned) => Future.successful(model.timeApportionedResult.get)
        case _ => throw new MatchError("Unexpected values for: (calculationResultsWithTaxOwedModel, calculationElection)")
      }
    }

    def calculateTaxOwed(totalGainAnswersModel: TotalGainAnswersModel,
                         privateResidenceReliefModel: Option[PrivateResidenceReliefModel],
                         propertyLivedInModel: Option[PropertyLivedInModel],
                         totalPersonalDetailsCalculationModel: Option[TotalPersonalDetailsCalculationModel],
                         maxAEA: BigDecimal,
                         otherReliefs: Option[AllOtherReliefsModel]): Future[Option[CalculationResultsWithTaxOwedModel]] = {
      calcConnector.calculateNRCGTTotalTax(totalGainAnswersModel,
        privateResidenceReliefModel,
        propertyLivedInModel,
        totalPersonalDetailsCalculationModel,
        maxAEA,
        otherReliefs)
    }

    def getAllOtherReliefs(totalPersonalDetailsCalculationModel: Option[TotalPersonalDetailsCalculationModel])
                          (implicit hc: HeaderCarrier): Future[Option[AllOtherReliefsModel]] = {
      totalPersonalDetailsCalculationModel match {
        case Some(_) =>
          val flat = calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat)
          val rebased = calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsRebased)
          val time = calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsTA)

          for {
            flatReliefs <- flat
            rebasedReliefs <- rebased
            timeReliefs <- time
          } yield Some(AllOtherReliefsModel(flatReliefs, rebasedReliefs, timeReliefs))
        case _ => Future.successful(None)
      }
    }

    def questionAnswerRows(totalGainAnswersModel: TotalGainAnswersModel,
                           totalGainResultsModel: TotalGainResultsModel,
                           privateResidenceReliefModel: Option[PrivateResidenceReliefModel],
                           personalAndPreviousDetailsModel: Option[TotalPersonalDetailsCalculationModel],
                           propertyLivedInModel: Option[PropertyLivedInModel]): Future[Seq[QuestionAnswerModel[Any]]] = {

      val optionSeq = Seq(totalGainResultsModel.rebasedGain, totalGainResultsModel.timeApportionedGain).flatten
      val finalSeq = Seq(totalGainResultsModel.flatGain) ++ optionSeq

      if (finalSeq.forall(_ <= 0)) {
        Future.successful(YourAnswersConstructor.fetchYourAnswers(totalGainAnswersModel, privateResidenceReliefModel, None, propertyLivedInModel))
      }
      else Future.successful(YourAnswersConstructor.fetchYourAnswers(totalGainAnswersModel, privateResidenceReliefModel, personalAndPreviousDetailsModel, propertyLivedInModel))
    }

    (for {
      answers <- answersConstructor.getNRTotalGainAnswers(hc)
      totalGainResultsModel <- calcConnector.calculateTotalGain(answers)
      gainExists <- checkGainExists(totalGainResultsModel.get)
      propertyLivedIn <- getPropertyLivedInResponse(gainExists, calcConnector)
      prrModel <- getPrrResponse(propertyLivedIn, calcConnector)
      totalGainWithPRR <- getPrrIfApplicable(answers, prrModel, propertyLivedIn, calcConnector)
      finalAnswers <- getFinalSectionsAnswers(totalGainResultsModel.get, totalGainWithPRR, calcConnector, answersConstructor)
      otherReliefsModel <- getAllOtherReliefs(finalAnswers)
      taxYearModel <- getTaxYear(answers, calcConnector)
      maxAEA <- getMaxAEA(taxYearModel, calcConnector)
      finalResult <- calculateTaxOwed(answers, prrModel, propertyLivedIn, finalAnswers, maxAEA.get, otherReliefsModel)
      questionAnswerRows <- questionAnswerRows(answers, totalGainResultsModel.get, prrModel, finalAnswers, propertyLivedIn)
      calculationType <- calcConnector.fetchAndGetFormData[CalculationElectionModel](KeystoreKeys.calculationElection)
      totalCosts <- calcConnector.calculateTotalCosts(answers, calculationType)
      calculationResult <- getCalculationResult(finalResult, calculationType.get.calculationType)
    } yield {
      lazy val view = calculationType.get.calculationType match {
        case CalculationType.flat =>
          implicit val lang: Lang = mcc.messagesApi.preferred(request).lang
          summaryReportView(questionAnswerRows, calculationResult, taxYearModel.get, calculationType.get.calculationType,
            answers.disposalValueModel.disposalValue,
            answers.acquisitionValueModel.acquisitionValueAmt,
            totalCosts,
            reliefsUsed = calculationResult.prrUsed.getOrElse(BigDecimal(0)) + calculationResult.otherReliefsUsed.getOrElse(BigDecimal(0)))

        case CalculationType.timeApportioned =>
          implicit val lang: Lang = mcc.messagesApi.preferred(request).lang
          summaryReportView(questionAnswerRows, calculationResult, taxYearModel.get, calculationType.get.calculationType,
            answers.disposalValueModel.disposalValue,
            answers.acquisitionValueModel.acquisitionValueAmt,
            totalCosts,
            Some(finalResult.get.flatResult.totalGain),
            reliefsUsed = calculationResult.prrUsed.getOrElse(BigDecimal(0)) + calculationResult.otherReliefsUsed.getOrElse(BigDecimal(0)))

        case CalculationType.rebased =>
          implicit val lang: Lang = mcc.messagesApi.preferred(request).lang
          summaryReportView(questionAnswerRows, calculationResult, taxYearModel.get, calculationType.get.calculationType,
            answers.disposalValueModel.disposalValue,
            answers.rebasedValueModel.get.rebasedValueAmt,
            totalCosts,
            None,
            reliefsUsed = calculationResult.prrUsed.getOrElse(BigDecimal(0)) + calculationResult.otherReliefsUsed.getOrElse(BigDecimal(0)))
      }
      pdfGenerator.ok(view, host).asScala()
        .withHeaders("Content-Disposition" ->s"""attachment; filename="${Messages("calc.summary.title")}.pdf"""")
    }).recoverToStart
  }
}
