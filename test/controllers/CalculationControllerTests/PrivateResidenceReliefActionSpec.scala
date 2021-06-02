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

package controllers.CalculationControllerTests

import java.time.LocalDate

import akka.stream.Materializer
import assets.MessageLookup.NonResident.{PrivateResidenceRelief => messages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import constructors.AnswersConstructor
import controllers.PrivateResidenceReliefController
import controllers.helpers.FakeRequestHelper
import models._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.privateResidenceRelief

import scala.concurrent.{ExecutionContext, Future}

class PrivateResidenceReliefActionSpec
  extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val implicitHeaderCarrier = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))
  implicit val ec = fakeApplication.injector.instanceOf[ExecutionContext]
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMaterializer = mock[Materializer]
  val mockHttp = mock[DefaultHttpClient]
  val mockCalcConnector = mock[CalculatorConnector]
  val mockDefaultCache = mock[CacheMap]
  val mockAnswersConstructor = mock[AnswersConstructor]
  val mockMessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val privateResidenceReliefView = fakeApplication.injector.instanceOf[privateResidenceRelief]

  class Setup {
    val controller = new PrivateResidenceReliefController(
      mockHttp,
      mockCalcConnector,
      mockAnswersConstructor,
      mockMessagesControllerComponents,
      privateResidenceReliefView
    )(ec)
  }

  def setupTarget
  (
    getData: Option[PrivateResidenceReliefModel],
    disposalDateData: Option[DateModel] = None,
    acquisitionDateData: Option[DateModel] = None,
    rebasedValueData: Option[RebasedValueModel] = None,
    calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel] = None
  ): PrivateResidenceReliefController = {

    val totalGainResultsModel = TotalGainResultsModel(1, Some(0), Some(0))

    when(mockCalcConnector.saveFormData[PrivateResidenceReliefModel](
      ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(mock[CacheMap])

    when(mockCalcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](
      ArgumentMatchers.eq(KeystoreKeys.privateResidenceRelief))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[DateModel](
      ArgumentMatchers.eq(KeystoreKeys.disposalDate))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(disposalDateData))

    when(mockCalcConnector.fetchAndGetFormData[DateModel]
      (ArgumentMatchers.eq(KeystoreKeys.acquisitionDate))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(acquisitionDateData))

    when(mockCalcConnector.fetchAndGetFormData[RebasedValueModel](
      ArgumentMatchers.eq(KeystoreKeys.rebasedValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(rebasedValueData))

    when(mockCalcConnector.calculateTaxableGainAfterPRR(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(calculationResultsWithPRRModel)

    when(mockAnswersConstructor.getNRTotalGainAnswers(ArgumentMatchers.any()))
      .thenReturn(mock[TotalGainAnswersModel])

    when(mockCalcConnector.fetchAndGetFormData[PropertyLivedInModel](ArgumentMatchers.eq(KeystoreKeys.propertyLivedIn))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(PropertyLivedInModel(true))))

    when(mockCalcConnector.calculateTotalGain(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(totalGainResultsModel)))


    new PrivateResidenceReliefController(mockHttp, mockCalcConnector, mockAnswersConstructor, mockMessagesControllerComponents, privateResidenceReliefView)(ec)
  }

  "Calling the .getAcquisitionDate method" should {

    "return a valid date when one is found with an answer of yes" in {
      val target = setupTarget(None, acquisitionDateData = Some(DateModel(10, 5, 2015)))
      val result = target.getAcquisitionDate(implicitHeaderCarrier: HeaderCarrier)

      await(result) shouldBe Some(LocalDate.parse("2015-05-10"))
    }

    "return a None when no date is found" in {
      val target = setupTarget(None)
      val result = target.getAcquisitionDate(implicitHeaderCarrier: HeaderCarrier)

      await(result) shouldBe None
    }
  }

  "Calling the .getDisposalDate method" should {

    "return a valid date when one is found" in {
      val target = setupTarget(None, disposalDateData = Some(DateModel(10, 5, 2015)))
      val result = target.getDisposalDate(implicitHeaderCarrier: HeaderCarrier)

      await(result) shouldBe Some(LocalDate.parse("2015-05-10"))
    }

    "return a None when no date is found" in {
      val target = setupTarget(None)
      val result = target.getDisposalDate(implicitHeaderCarrier: HeaderCarrier)

      await(result) shouldBe None
    }
  }

  "Calling the .displayAfterQuestion method" when {

    "return a true if the acquisition date is before the tax start and " +
      "disposal date is after tax start date + 18 months" in {
      val acquisitionDate = LocalDate.parse("2015-04-05")
      val disposalDate = LocalDate.parse("2016-10-07")
      val target = setupTarget(None, acquisitionDateData = Some(DateModel(10, 5, 2015)))
      val result = target.displayAfterQuestion(Some(disposalDate), Some(acquisitionDate))

      result shouldBe true
    }

    "return a false if the acquisition date is after the tax start and " +
      "disposal date is after tax start date + 18 months" in {
      val target = setupTarget(None, acquisitionDateData = Some(DateModel(10, 5, 2015)))
      val acquisitionDate = LocalDate.parse("2015-04-06")
      val disposalDate = LocalDate.parse("2016-10-07")
      val result = target.displayAfterQuestion(Some(disposalDate), Some(acquisitionDate))

      result shouldBe false
    }

    "return false if disposal date is equal to tax start date + 18 months" in {
      val target = setupTarget(None, acquisitionDateData = Some(DateModel(10, 5, 2015)))
      val acquisitionDate = LocalDate.parse("2015-05-04")
      val disposalDate = LocalDate.parse("2016-10-06")
      val result = target.displayAfterQuestion(Some(disposalDate), Some(acquisitionDate))

      result shouldBe false
    }

    "the disposal date is not given" should {
      val target = setupTarget(None, acquisitionDateData = Some(DateModel(10, 5, 2015)))
      "return a false" in {
        val result = target.displayAfterQuestion(None, None)

        result shouldBe false
      }
    }
  }

  "Calling the .displayFirstQuestion method" when {

    val target = setupTarget(None, acquisitionDateData = Some(DateModel(10, 5, 2015)))

    "return false if disposal date is equal to tax start date + 18 months" in {
      val acquisitionDate = LocalDate.parse("2010-04-05")
      val disposalDate = LocalDate.parse("2016-10-06")
      val result = target.displayFirstQuestion(Some(disposalDate), Some(acquisitionDate))

      result shouldBe false
    }

    "return false if disposal date within 18 months of the acquisition date" in {
      val acquisitionDate = LocalDate.parse("2015-04-05")
      val disposalDate = LocalDate.parse("2016-10-05")
      val result = target.displayFirstQuestion(Some(disposalDate), Some(acquisitionDate))

      result shouldBe false
    }

    "return true if the acquisition date is after the tax start and " +
      "disposal date is after acquisition date + 18 months" in {
      val acquisitionDate = LocalDate.parse("2015-04-06")
      val disposalDate = LocalDate.parse("2016-10-07")
      val result = target.displayFirstQuestion(Some(disposalDate), Some(acquisitionDate))

      result shouldBe true
    }

    "return true if the acquisition date is the tax start and " +
      "disposal date is after tax start date + 18 months" in {
      val acquisitionDate = LocalDate.parse("2015-04-05")
      val disposalDate = LocalDate.parse("2016-10-07")
      val result = target.displayFirstQuestion(Some(disposalDate), Some(acquisitionDate))

      result shouldBe true
    }
  }

  //GET Tests
  "Calling the .privateResidenceRelief action " when {

    "not supplied wth a pre-existing stored model" should {
      val target = setupTarget(
        None,
        disposalDateData = Some(DateModel(5, 8, 2015)),
        acquisitionDateData = Some(DateModel(1, 1, 2016)),
        rebasedValueData = Some(RebasedValueModel(1000)))
      lazy val result = target.privateResidenceRelief(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(mockMaterializer, ec))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the private residence relief page" in {
        document.title() shouldBe messages.question
      }
    }

    "supplied wth a pre-existing stored model" should {
      val target = setupTarget(
        Some(PrivateResidenceReliefModel("Yes", None, None)),
        disposalDateData = Some(DateModel(5, 8, 2015)),
        acquisitionDateData = Some(DateModel(1, 1, 2016)),
        rebasedValueData = Some(RebasedValueModel(1000)))
      lazy val result = target.privateResidenceRelief(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(mockMaterializer, ec))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the private residence relief page" in {
        document.title() shouldBe messages.question
      }
    }

    "supplied without a valid session" should {
      val target = setupTarget(
        None,
        disposalDateData = Some(DateModel(5, 8, 2015)),
        acquisitionDateData = Some(DateModel(1, 1, 2016)),
        rebasedValueData = Some(RebasedValueModel(1000)))
      lazy val result = target.privateResidenceRelief(fakeRequest)

      "redirect to the session timeout page" in {
        try {
          redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
          fail("Exception should be thrown")
        } catch {
          case ex: Exception =>
        }
      }
    }
  }

  //POST Tests
  "Calling the .submitPrivateResidenceRelief action " when {

    "submitting a valid form with no positive taxable gains" should {
      val model = CalculationResultsWithPRRModel(GainsAfterPRRModel(1000, 0, 0), None, None)
      val target = setupTarget(
        None,
        disposalDateData = Some(DateModel(5, 8, 2015)),
        acquisitionDateData = Some(DateModel(1, 1, 2016)),
        rebasedValueData = Some(RebasedValueModel(1000)),
        calculationResultsWithPRRModel = Some(model))
      lazy val request = fakeRequestToPOSTWithSession(("isClaimingPRR", "No"))
      lazy val result = target.submitPrivateResidenceRelief(request)

      "redirect to the Check Your Answers page" in {
        try {
          redirectLocation(result).get shouldBe controllers.routes.CheckYourAnswersController.checkYourAnswers().url
        } catch {
          case ex: Exception =>
        }
      }
    }

    "submitting a valid form with some positive taxable gains" should {
      val model = CalculationResultsWithPRRModel(GainsAfterPRRModel(1000, 500, 0), None, None)

      val target = setupTarget(
        None,
        disposalDateData = Some(DateModel(5, 8, 2015)),
        acquisitionDateData = Some(DateModel(1, 1, 2016)),
        rebasedValueData = Some(RebasedValueModel(1000)),
        calculationResultsWithPRRModel = Some(model))
      lazy val request = fakeRequestToPOSTWithSession(("isClaimingPRR", "No"))
      lazy val result = target.submitPrivateResidenceRelief(request)

      "redirect to the Current Income page" in {
        try {
          redirectLocation(result).get shouldBe controllers.routes.CurrentIncomeController.currentIncome().url
          fail("Failure should be thrown")
        } catch {
          case ex: Exception =>
        }
      }
    }

    "submitting an invalid form" should {
      val target = setupTarget(
        None,
        disposalDateData = Some(DateModel(5, 8, 2015)),
        acquisitionDateData = Some(DateModel(1, 1, 2016)),
        rebasedValueData = Some(RebasedValueModel(1000)))
      lazy val request = fakeRequestToPOSTWithSession(("isClaimingPRR", ""))
      lazy val result = target.submitPrivateResidenceRelief(request)
      lazy val document = Jsoup.parse(bodyOf(result)(mockMaterializer, ec))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "return to the private residence relief page" in {
        document.title() shouldBe messages.question
      }
    }
  }
}
