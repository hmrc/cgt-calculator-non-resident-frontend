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

import assets.MessageLookup.{NonResident => commonMessages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import constructors.AnswersConstructor
import controllers.helpers.FakeRequestHelper
import controllers.{ImprovementsController, routes}
import models._
import org.jsoup._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.{improvements, improvementsRebased, isClaimingImprovements}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IsClaimingImprovementsActionSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockHttp: DefaultHttpClient =mock[DefaultHttpClient]
  val mockCalcConnector: CalculatorConnector =mock[CalculatorConnector]
  val mockAnswersConstructor: AnswersConstructor = mock[AnswersConstructor]
  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val improvementsView: improvements = fakeApplication.injector.instanceOf[improvements]
  val improvementsRebasedView: improvementsRebased = fakeApplication.injector.instanceOf[improvementsRebased]
  val isClaimingImprovementsView: isClaimingImprovements = fakeApplication.injector.instanceOf[isClaimingImprovements]
  lazy val pageTitle: String = s"""${commonMessages.IsClaimingImprovements.title} - ${commonMessages.serviceName} - GOV.UK"""
  lazy val pageTitleBeforeLegislation: String =
    s"${commonMessages.IsClaimingImprovements.ownerBeforeLegislationStartQuestion} - ${commonMessages.serviceName} - GOV.UK"
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
    )
  }

  def setupTarget(getData: Option[IsClaimingImprovementsModel],
                  acquisitionDateData: Option[DateModel],
                  rebasedValueData: Option[RebasedValueModel] = None,
                  totalGainResultsModel: Option[TotalGainResultsModel] = None
                 ): ImprovementsController = {

    when(mockSessionCacheService.fetchAndGetFormData[IsClaimingImprovementsModel](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

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

    when(mockSessionCacheService.saveFormData[IsClaimingImprovementsModel](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(("", "")))

    new ImprovementsController(mockCalcConnector, mockSessionCacheService, mockAnswersConstructor, mockMessagesControllerComponents, improvementsView, improvementsRebasedView, isClaimingImprovementsView)
  }

  "In CalculationController calling the .getIsClaimingImprovements action " when {

    "not supplied with a pre-existing stored model" should {

      "when Acquisition Date is > 31 March 1982" should {

        lazy val target = setupTarget(None, Some(DateModel(1, 1, 2017)), Some(RebasedValueModel(1000)))
        lazy val result = target.getIsClaimingImprovements(fakeRequestWithSession)
        lazy val document = Jsoup.parse(contentAsString(result))

        "return a 200" in {
          status(result) shouldBe 200
        }

        "return some HTML that" should {

          s"have the title $pageTitle" in {
            document.title shouldEqual pageTitle
          }
        }

        s"have a 'Back' link" in {
          document.body.getElementsByClass("govuk-back-link").attr("href") shouldEqual "#"
        }
      }

      "when Acquisition Date is <= 31 March 1982" should {

        lazy val target = setupTarget(
          None,
          Some(DateModel(1, 1, 1974)),
          Some(RebasedValueModel(500))
        )
        lazy val result = target.getIsClaimingImprovements(fakeRequestWithSession)
        lazy val document = Jsoup.parse(contentAsString(result))

        s"have the title $pageTitleBeforeLegislation" in {
          document.title shouldEqual pageTitleBeforeLegislation
        }

        s"have a back link that contains ${commonMessages.back}" in {
          document.body.getElementsByClass("govuk-back-link").text shouldEqual commonMessages.back
        }

      }
    }
  }

  "In CalculationController calling the .submitIsClaimingImprovements action " when {

    "submitting a valid form with total gains of less than 0" should {

      lazy val gainsModel = Some(TotalGainResultsModel(-1000, None, None))
      lazy val target = setupTarget(None, Some(DateModel(1, 1, 2014)), None, gainsModel)
      lazy val request = fakeRequestToPOSTWithSession("isClaimingImprovements" -> "No").withMethod("POST")
      lazy val result = target.submitIsClaimingImprovements(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.CheckYourAnswersController.checkYourAnswers}" in {
        redirectLocation(result) shouldBe Some(s"${routes.CheckYourAnswersController.checkYourAnswers}")
      }
    }

    "submitting a valid form with a rebased value" should {

      lazy val gainsModel = Some(TotalGainResultsModel(1000, Some(2000), None))
      lazy val target = setupTarget(None, Some(DateModel(1, 1, 2014)), Some(RebasedValueModel(2000)), gainsModel)
      lazy val request = fakeRequestToPOSTWithSession("isClaimingImprovements" -> "No").withMethod("POST")
      lazy val result = target.submitIsClaimingImprovements(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${controllers.routes.PropertyLivedInController.propertyLivedIn}" in {
        redirectLocation(result) shouldBe Some(s"${controllers.routes.PropertyLivedInController.propertyLivedIn}")
      }
    }

    "submitting an invalid form with 'testData123'" should {

      val target = setupTarget(None, Some(DateModel(1, 1, 1974)))
      lazy val request = fakeRequestToPOSTWithSession("isClaimingImprovements" -> "testData123").withMethod("POST")
      lazy val result = target.submitIsClaimingImprovements(request)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "return to the isClaimingImprovements page" in {
        document.title shouldBe "Error: " + pageTitle
      }
    }
  }
}
