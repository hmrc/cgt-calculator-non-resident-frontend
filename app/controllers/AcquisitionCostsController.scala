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
import common.TaxDates
import connectors.CalculatorConnector
import constructors.CalculationElectionConstructor
import controllers.predicates.ValidActiveSession
import forms.AcquisitionCostsForm._
import models.{AcquisitionCostsModel, AcquisitionDateModel, BoughtForLessModel, HowBecameOwnerModel}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object AcquisitionCostsController extends AcquisitionCostsController{
  val calcConnector = CalculatorConnector
}

trait AcquisitionCostsController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  val calcElectionConstructor = CalculationElectionConstructor

  def getBackLink(implicit hc:HeaderCarrier): Future[String] = {
    val getAcquisitionDate = calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate)
    val getHowBecameOwner = calcConnector.fetchAndGetFormData[HowBecameOwnerModel](KeystoreKeys.howBecameOwner)
    val getBoughtForLess = calcConnector.fetchAndGetFormData[BoughtForLessModel](KeystoreKeys.boughtForLess)
    def result(acquisitionDateModel: Option[AcquisitionDateModel],
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
    def result(backLink: String) = calcConnector.fetchAndGetFormData[AcquisitionCostsModel](KeystoreKeys.acquisitionCosts).map {
      case Some(data) => Ok(calculation.acquisitionCosts(acquisitionCostsForm.fill(data), backLink))
      case None => Ok(calculation.acquisitionCosts(acquisitionCostsForm, backLink))
    }

    for {
      backLink <- getBackLink
      result <- result(backLink)
    } yield result
  }

  val submitAcquisitionCosts: Action[AnyContent] = ValidateSession.async { implicit request =>

    val getRedirectRoute: Future[Result] = {
      val getAcquisitionDate = calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate)

      def result(acquisitionDateModel: Option[AcquisitionDateModel]) = acquisitionDateModel match {
        case Some(_) if TaxDates.dateAfterStart(acquisitionDateModel.get.get) =>
          Future.successful(Redirect(routes.ImprovementsController.improvements()))
        case _ => Future.successful(Redirect(routes.RebasedValueController.rebasedValue()))
      }

      for {
        acquisitionDate <- getAcquisitionDate
        result <- result(acquisitionDate)
      } yield result
    }

    def successAction(model: AcquisitionCostsModel) = {
      calcConnector.saveFormData(KeystoreKeys.acquisitionCosts, model)
      getRedirectRoute
    }

    def errorAction(form: Form[AcquisitionCostsModel]) = {
      def result(backLink: String) = Future.successful(BadRequest(calculation.acquisitionCosts(form, backLink)))

      for {
        backLink <- getBackLink
        result <- result(backLink)
      } yield result
    }

    acquisitionCostsForm.bindFromRequest.fold(errorAction, successAction)
  }
}
