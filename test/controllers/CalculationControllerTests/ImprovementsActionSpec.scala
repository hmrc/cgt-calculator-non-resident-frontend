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

package controllers.CalculationControllerTests

import org.apache.pekko.stream.Materializer
import assets.MessageLookup.{NonResident => commonMessages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import constructors.AnswersConstructor
import controllers.helpers.FakeRequestHelper
import controllers.ImprovementsController
import models._
import org.jsoup._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.{improvementsRebased, improvements, isClaimingImprovements}

import scala.concurrent.{ExecutionContext, Future}

class ImprovementsActionSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc: HeaderCarrier = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))

  val materializer: Materializer = mock[Materializer]
  val ec: ExecutionContext = fakeApplication.injector.instanceOf[ExecutionContext]
  val mockHttp: DefaultHttpClient =mock[DefaultHttpClient]
  val mockCalcConnector: CalculatorConnector =mock[CalculatorConnector]
  val mockAnswersConstructor: AnswersConstructor = mock[AnswersConstructor]
  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val improvementsView: improvements = fakeApplication.injector.instanceOf[improvements]
  val improvementsRebasedView: improvementsRebased = fakeApplication.injector.instanceOf[improvementsRebased]
  val isClaimingImprovementsView: isClaimingImprovements = fakeApplication.injector.instanceOf[isClaimingImprovements]
  lazy val pageTitle = s"""${commonMessages.Improvements.title} - ${commonMessages.serviceName} - GOV.UK"""
  lazy val pageTitleBeforeTax = s"""${commonMessages.ImprovementsRebased.title} - ${commonMessages.serviceName} - GOV.UK"""
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]

  class Setup {
    val controller = new ImprovementsController(
      mockCalcConnector,
      mockSessionCacheService,
      mockAnswersConstructor,
      mockMessagesControllerComponents,
      improvementsView,
      improvementsRebasedView,
      isClaimingImprovementsView
    )(ec)
  }

  def setupTarget(
                   isClaimingImprovementsData: IsClaimingImprovementsModel,
                   getData: ImprovementsModel,
                   acquisitionDateData: Option[DateModel],
                   rebasedValueData: Option[RebasedValueModel] = None,
                   totalGainResultsModel: Option[TotalGainResultsModel] = None
                 ): ImprovementsController = {

    when(mockSessionCacheService.fetchAndGetFormData[IsClaimingImprovementsModel](
      ArgumentMatchers.eq(KeystoreKeys.isClaimingImprovements))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(isClaimingImprovementsData)))

    when(mockSessionCacheService.fetchAndGetFormData[ImprovementsModel](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(getData)))

    when(mockSessionCacheService.fetchAndGetFormData[RebasedValueModel](
      ArgumentMatchers.eq(KeystoreKeys.rebasedValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(rebasedValueData))

    when(mockSessionCacheService.fetchAndGetFormData[DateModel](
      ArgumentMatchers.eq(KeystoreKeys.acquisitionDate))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(acquisitionDateData))

    when(mockAnswersConstructor.getNRTotalGainAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[TotalGainAnswersModel]))

    when(mockCalcConnector.calculateTotalGain(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGainResultsModel))

    when(mockSessionCacheService.saveFormData[IsClaimingImprovementsModel]
      (ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(("", "")))

    when(mockSessionCacheService.saveFormData[ImprovementsModel]
      (ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(("", "")))

    new ImprovementsController(
      mockCalcConnector,
      mockSessionCacheService,
      mockAnswersConstructor,
      mockMessagesControllerComponents,
      improvementsView,
      improvementsRebasedView,
      isClaimingImprovementsView
    )(ec)
  }

  "In CalculationController calling the .improvements action " when {

    "not supplied with a pre-existing stored model" should {

      "when Acquisition Date is > 5 April 2015" should {

        lazy val target = setupTarget(
          IsClaimingImprovementsModel(true),
          ImprovementsModel(),
          Some(DateModel(1, 1, 2017)),
          Some(RebasedValueModel(1000))
        )
        lazy val result = target.improvements(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

        "return a 200" in {
          status(result) shouldBe 200
        }

        "return some HTML that" should {

          s"have the title ${pageTitle}" in {
            document.title shouldEqual pageTitle
          }
        }

        s"have a 'Back' link" in {
          document.body.getElementsByClass("govuk-back-link").attr("href") shouldEqual "#"
        }
      }
    }
  }

  "In CalculationController calling the .submitImprovements action " when {

    "submitting a valid form" should {

      lazy val gainsModel = Some(TotalGainResultsModel(1000, Some(2000), None))
      lazy val target = setupTarget(
        IsClaimingImprovementsModel(true),
        ImprovementsModel(),
        Some(DateModel(1, 1, 2016)),
        Some(RebasedValueModel(2000)),
        gainsModel
      )
      lazy val request = fakeRequestToPOSTWithSession("improvementsAmt" -> "100").withMethod("POST")
      lazy val result = target.submitImprovements(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${controllers.routes.PropertyLivedInController.propertyLivedIn}" in {
        redirectLocation(result) shouldBe Some(controllers.routes.PropertyLivedInController.propertyLivedIn.url)
      }
    }

    "submitting an invalid form with a value of 'fhu39awd8'" should {

      val target = setupTarget(IsClaimingImprovementsModel(true), ImprovementsModel(), Some(DateModel(1, 1, 2016)))
      lazy val request = fakeRequestToPOSTWithSession("improvementsAmt" -> "fhu39awd8").withMethod("POST")
      lazy val result = target.submitImprovements(request)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "return to the improvements page" in {
        document.title shouldBe "Error: " + pageTitle
      }
    }
  }
}
