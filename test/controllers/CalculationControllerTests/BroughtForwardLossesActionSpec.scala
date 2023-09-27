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

package controllers.CalculationControllerTests

import akka.stream.Materializer
import assets.MessageLookup.{NonResident => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import config.ApplicationConfig
import connectors.CalculatorConnector
import constructors.AnswersConstructor
import controllers.BroughtForwardLossesController
import controllers.helpers.FakeRequestHelper
import models._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.broughtForwardLosses

import scala.concurrent.{ExecutionContext, Future}

class BroughtForwardLossesActionSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {
  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))
  val ec = fakeApplication.injector.instanceOf[ExecutionContext]

  val materializer = mock[Materializer]
  val mockHttp =mock[DefaultHttpClient]
  val mockCalcConnector =mock[CalculatorConnector]
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockAnswerConstuctor = mock[AnswersConstructor]
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val broughtForwardLossesView = fakeApplication.injector.instanceOf[broughtForwardLosses]
  lazy val pageTitle = s"""${messages.BroughtForwardLosses.question} - ${messages.pageHeading} - GOV.UK"""

  class Setup {
    val controller = new BroughtForwardLossesController(
      mockCalcConnector,
      mockSessionCacheService,
      mockMessagesControllerComponents,
      broughtForwardLossesView
    )(ec)
  }


  def setupTarget(getData: Option[BroughtForwardLossesModel],
                  otherPropertiesModel: Option[OtherPropertiesModel] = None,
                  previousLossOrGainModel: Option[PreviousLossOrGainModel] = None,
                  howMuchGainModel: Option[HowMuchGainModel] = None,
                  howMuchLossModel: Option[HowMuchLossModel] = None): BroughtForwardLossesController = {

    when(mockSessionCacheService.fetchAndGetFormData[BroughtForwardLossesModel](
      ArgumentMatchers.eq(KeystoreKeys.broughtForwardLosses))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(getData)

    when(mockSessionCacheService.fetchAndGetFormData[OtherPropertiesModel](
      ArgumentMatchers.eq(KeystoreKeys.otherProperties))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(otherPropertiesModel))

    when(mockSessionCacheService.fetchAndGetFormData[PreviousLossOrGainModel]
      (ArgumentMatchers.eq(KeystoreKeys.previousLossOrGain))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(previousLossOrGainModel))

    when(mockSessionCacheService.fetchAndGetFormData[HowMuchGainModel](ArgumentMatchers.eq(KeystoreKeys.howMuchGain))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(howMuchGainModel))

    when(mockSessionCacheService.fetchAndGetFormData[HowMuchLossModel](ArgumentMatchers.eq(KeystoreKeys.howMuchLoss))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(howMuchLossModel))

    when(mockSessionCacheService.saveFormData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(("", "")))

    new BroughtForwardLossesController(mockCalcConnector, mockSessionCacheService, mockMessagesControllerComponents, broughtForwardLossesView)(ec) {
    }
  }

  "Calling .broughtForwardLosses" when {

    "provided with no previous data" should {
      val target = setupTarget(None)
      lazy val result = target.broughtForwardLosses(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the Brought Forward Losses page" in {
        document.title() shouldBe pageTitle
      }

      "have a back link to Annual Exempt Amount page" in {
        document.select(".govuk-back-link").attr("href") shouldBe "#"
      }
    }

    "provided with previous data" should {
      val target = setupTarget(Some(BroughtForwardLossesModel(isClaiming = false, None)))
      lazy val result = target.broughtForwardLosses(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the Brought Forward Losses page" in {
        document.title() shouldBe pageTitle
      }
    }

    "provided with no valid session" should {
      val target = setupTarget(None)
      lazy val result = target.broughtForwardLosses(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect the user to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling .submitBroughtForwardLosses" when {

    "provided with a valid form" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("isClaiming", "No"), ("broughtForwardLoss", "")).withMethod("POST")
      lazy val result = target.submitBroughtForwardLosses(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the Check Your Answers page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.CheckYourAnswersController.checkYourAnswers.url)
      }
    }

    "provided with an invalid form" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("isClaiming", "Yes"), ("broughtForwardLoss", ""))
      lazy val result = target.submitBroughtForwardLosses(request)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "load the Brought Forward Losses page" in {
        document.title() shouldBe s"Error: $pageTitle"
      }
    }

    "provided with an invalid session" should {
      val target = setupTarget(None)
      lazy val result = target.submitBroughtForwardLosses(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect the user to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling generateBackLink" should {

    "return a back link to the OtherProperties page when there is an answer of no to Other Properties" in {
      val target = setupTarget(None, Some(OtherPropertiesModel("No")))
      lazy val result = target.broughtForwardLosses(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      document.select(".govuk-back-link").attr("href") shouldBe "#"
    }

    "return a back link to the HowMuchGain page when there is a previous positive gain" in {
      val target = setupTarget(None, Some(OtherPropertiesModel("Yes")), Some(PreviousLossOrGainModel("Gain")), Some(HowMuchGainModel(1)))
      lazy val result = target.broughtForwardLosses(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      document.select(".govuk-back-link").attr("href") shouldBe "#"
    }

    "return a back link to the AnnualExemptAmount page when there is a previous gain of 0" in {
      val target = setupTarget(None, Some(OtherPropertiesModel("Yes")), Some(PreviousLossOrGainModel("Gain")), Some(HowMuchGainModel(0)))
      lazy val result = target.broughtForwardLosses(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      document.select(".govuk-back-link").attr("href") shouldBe "#"
    }

    "return a back link to the HowMuchLoss page when there is a previous positive loss" in {
      val target = setupTarget(None, Some(OtherPropertiesModel("Yes")), Some(PreviousLossOrGainModel("Loss")), howMuchLossModel = Some(HowMuchLossModel(1)))
      lazy val result = target.broughtForwardLosses(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      document.select(".govuk-back-link").attr("href") shouldBe "#"
    }

    "return a back link to the AnnualExemptAmount page when there is a previous loss of 0" in {
      val target = setupTarget(None, Some(OtherPropertiesModel("Yes")), Some(PreviousLossOrGainModel("Loss")), howMuchLossModel = Some(HowMuchLossModel(0)))
      lazy val result = target.broughtForwardLosses(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      document.select(".govuk-back-link").attr("href") shouldBe "#"
    }

    "return a back link to the AnnualExemptAmount page when there is a previous disposal that breaks even" in {
      val target = setupTarget(None, Some(OtherPropertiesModel("Yes")), Some(PreviousLossOrGainModel("Neither")))
      lazy val result = target.broughtForwardLosses(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      document.select(".govuk-back-link").attr("href") shouldBe "#"
    }
  }
}
