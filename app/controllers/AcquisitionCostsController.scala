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
import common.TaxDates
import config.ApplicationConfig
import connectors.CalculatorConnector
import constructors.DefaultCalculationElectionConstructor
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.AcquisitionCostsForm._
import javax.inject.Inject
import models.{AcquisitionCostsModel, BoughtForLessModel, DateModel, HowBecameOwnerModel}
import play.api.Play.current
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AcquisitionCostsController @Inject()(http: DefaultHttpClient,
                                           calcConnector: CalculatorConnector,
                                           calcElectionConstructor: DefaultCalculationElectionConstructor,
                                           mcc: MessagesControllerComponents
                                           )
                                          (implicit val appConfig: ApplicationConfig)
  extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  private def isOwnerBeforeLegislationStart(implicit hc: HeaderCarrier): Future[Boolean] = {
    calcConnector.fetchAndGetFormData[DateModel](KeystoreKeys.acquisitionDate).map { date =>
      TaxDates.dateBeforeLegislationStart(date.get.get)
    }
  }

  def getBackLink(implicit hc:HeaderCarrier): Future[String] = {
    val getAcquisitionDate = calcConnector.fetchAndGetFormData[DateModel](KeystoreKeys.acquisitionDate)
    val getHowBecameOwner = calcConnector.fetchAndGetFormData[HowBecameOwnerModel](KeystoreKeys.howBecameOwner)
    val getBoughtForLess = calcConnector.fetchAndGetFormData[BoughtForLessModel](KeystoreKeys.boughtForLess)
    def result(acquisitionDateModel: Option[DateModel],
               howBecameOwnerModel: Option[HowBecameOwnerModel],
               boughtForLessModel: Option[BoughtForLessModel]) = (acquisitionDateModel, howBecameOwnerModel, boughtForLessModel) match {
      case (Some(_), _, _) if TaxDates.dateBeforeLegislationStart(acquisitionDateModel.get.get) =>
        Future.successful(controllers.routes.WorthBeforeLegislationStartController.worthBeforeLegislationStart().url)
      case (_, Some(HowBecameOwnerModel("Inherited")),_) =>
        Future.successful(controllers.routes.WorthWhenInheritedController.worthWhenInherited().url)
      case (_, Some(HowBecameOwnerModel("Gifted")),_) =>
        Future.successful(controllers.routes.WorthWhenGiftedToController.worthWhenGiftedTo().url)
      case (_, _, Some(BoughtForLessModel(true))) =>
        Future.successful(controllers.routes.WorthWhenBoughtForLessController.worthWhenBoughtForLess().url)
      case _ => Future.successful(controllers.routes.AcquisitionValueController.acquisitionValue().url)
    }

    for {
      acquisitionDateModel <- getAcquisitionDate
      howBecameOwnerModel <- getHowBecameOwner
      boughtForLessModel <- getBoughtForLess
      result <- result(acquisitionDateModel, howBecameOwnerModel, boughtForLessModel)
    } yield result
  }

  val acquisitionCosts: Action[AnyContent] = ValidateSession.async { implicit request =>
    def result(backLink: String, isOwnerBeforeLegislationStart: Boolean) = {
      calcConnector.fetchAndGetFormData[AcquisitionCostsModel](KeystoreKeys.acquisitionCosts).map {
        case Some(data) => Ok(calculation.acquisitionCosts(acquisitionCostsForm.fill(data), backLink, isOwnerBeforeLegislationStart))
        case None => Ok(calculation.acquisitionCosts(acquisitionCostsForm, backLink, isOwnerBeforeLegislationStart))
      }
    }

    (for {
      backLink <- getBackLink
      isOwnerBeforeLegislationStart <- isOwnerBeforeLegislationStart
      result <- result(backLink, isOwnerBeforeLegislationStart)
    } yield result).recoverToStart
  }

  val submitAcquisitionCosts: Action[AnyContent] = ValidateSession.async { implicit request =>

    val getRedirectRoute: Future[Result] = {
      val getAcquisitionDate = calcConnector.fetchAndGetFormData[DateModel](KeystoreKeys.acquisitionDate)

      def result(acquisitionDateModel: Option[DateModel]) = acquisitionDateModel match {
        case Some(_) if TaxDates.dateAfterStart(acquisitionDateModel.get.get) =>
          Future.successful(Redirect(routes.ImprovementsController.improvements()))
        case _ => Future.successful(Redirect(routes.RebasedValueController.rebasedValue()))
      }

      (for {
        acquisitionDate <- getAcquisitionDate
        result <- result(acquisitionDate)
      } yield result).recoverToStart
    }

    def successAction(model: AcquisitionCostsModel): Future[Result] = {
      calcConnector.saveFormData(KeystoreKeys.acquisitionCosts, model).flatMap {
        _ => getRedirectRoute
      }
    }

    def errorAction(form: Form[AcquisitionCostsModel]): Future[Result] = {
      def result(backLink: String, isOwnerBeforeLegislationStart: Boolean) =
        Future.successful(BadRequest(calculation.acquisitionCosts(form, backLink, isOwnerBeforeLegislationStart)))

      (for {
        backLink <- getBackLink
        isOwnerBeforeLegislationStart <- isOwnerBeforeLegislationStart
        result <- result(backLink, isOwnerBeforeLegislationStart)
      } yield result).recoverToStart
    }

    acquisitionCostsForm.bindFromRequest.fold(errorAction, successAction)
  }
}
