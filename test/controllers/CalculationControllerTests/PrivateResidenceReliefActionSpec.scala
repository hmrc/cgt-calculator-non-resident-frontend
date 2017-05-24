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

package controllers.CalculationControllerTests

import java.time.LocalDate

import assets.MessageLookup.NonResident.{PrivateResidenceRelief => messages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import connectors.CalculatorConnector
import constructors.AnswersConstructor
import controllers.PrivateResidenceReliefController
import controllers.helpers.FakeRequestHelper
import models._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class PrivateResidenceReliefActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget
  (
    getData: Option[PrivateResidenceReliefModel],
    disposalDateData: Option[DisposalDateModel] = None,
    acquisitionDateData: Option[AcquisitionDateModel] = None,
    rebasedValueData: Option[RebasedValueModel] = None,
    calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel] = None
  ): PrivateResidenceReliefController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockAnswersConstructor = mock[AnswersConstructor]

    when(mockCalcConnector.saveFormData[PrivateResidenceReliefModel](
      ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(mock[CacheMap])

    when(mockCalcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](
      ArgumentMatchers.eq(KeystoreKeys.privateResidenceRelief))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[DisposalDateModel](
      ArgumentMatchers.eq(KeystoreKeys.disposalDate))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(disposalDateData))

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionDateModel]
      (ArgumentMatchers.eq(KeystoreKeys.acquisitionDate))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(acquisitionDateData))

    when(mockCalcConnector.fetchAndGetFormData[RebasedValueModel](
      ArgumentMatchers.eq(KeystoreKeys.rebasedValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(rebasedValueData))

    when(mockCalcConnector.calculateTaxableGainAfterPRR(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(calculationResultsWithPRRModel)

    when(mockAnswersConstructor.getNRTotalGainAnswers(ArgumentMatchers.any()))
      .thenReturn(mock[TotalGainAnswersModel])

    new PrivateResidenceReliefController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val answersConstructor: AnswersConstructor = mockAnswersConstructor
    }
  }

  "Calling the .getAcquisitionDate method" should {

    "return a valid date when one is found with an answer of yes" in {
      val target = setupTarget(None, acquisitionDateData = Some(AcquisitionDateModel("Yes", Some(10), Some(5), Some(2015))))
      val result = target.getAcquisitionDate(hc: HeaderCarrier)

      await(result) shouldBe Some(LocalDate.parse("2015-05-10"))
    }

    "return a None when a date is found with an answer of no" in {
      val target = setupTarget(None, acquisitionDateData = Some(AcquisitionDateModel("No", None, None, None)))
      val result = target.getAcquisitionDate(hc: HeaderCarrier)

      await(result) shouldBe None
    }

    "return a None when no date is found" in {
      val target = setupTarget(None)
      val result = target.getAcquisitionDate(hc: HeaderCarrier)

      await(result) shouldBe None
    }
  }

  "Calling the .getDisposalDate method" should {

    "return a valid date when one is found" in {
      val target = setupTarget(None, disposalDateData = Some(DisposalDateModel(10, 5, 2015)))
      val result = target.getDisposalDate(hc: HeaderCarrier)

      await(result) shouldBe Some(LocalDate.parse("2015-05-10"))
    }

    "return a None when no date is found" in {
      val target = setupTarget(None)
      val result = target.getDisposalDate(hc: HeaderCarrier)

      await(result) shouldBe None
    }
  }

  "Calling the .getRebasedAmount method" should {

    "return a true if a value is found with the answer 'Yes'" in {
      val target = setupTarget(None, rebasedValueData = Some(RebasedValueModel(Some(1000))))
      val result = target.getRebasedAmount(hc: HeaderCarrier)

      await(result) shouldBe true
    }

    "return a false if a value is found with the answer 'No'" in {
      val target = setupTarget(None, rebasedValueData = Some(RebasedValueModel(None)))
      val result = target.getRebasedAmount(hc: HeaderCarrier)

      await(result) shouldBe false
    }

    "return a false if no value is found" in {
      val target = setupTarget(None)
      val result = target.getRebasedAmount(hc: HeaderCarrier)

      await(result) shouldBe false
    }
  }

  "Calling the .displayBetweenQuestion method" when {

    "the disposal date is after the 18 month period" should {
      val disposalDate = LocalDate.parse("2018-10-05")

      "return a true if the acquisition date is before the tax start" in {
        val acquisitionDate = LocalDate.parse("2010-05-04")
        val result = PrivateResidenceReliefController.displayBetweenQuestion(Some(disposalDate), Some(acquisitionDate), false)

        result shouldBe true
      }

      "return a false if the acquisition date is after the tax start" in {
        val acquisitionDate = LocalDate.parse("2016-05-04")
        val result = PrivateResidenceReliefController.displayBetweenQuestion(Some(disposalDate), Some(acquisitionDate), false)

        result shouldBe false
      }

      "return a true if there is no acquisition date but a rebased value is given" in {
        val result = PrivateResidenceReliefController.displayBetweenQuestion(Some(disposalDate), None, true)

        result shouldBe true
      }

      "return a true if there is no acquisition date and no rebased value is given" in {
        val result = PrivateResidenceReliefController.displayBetweenQuestion(Some(disposalDate), None, false)

        result shouldBe false
      }
    }

    "the disposal date is before the 18 month period" should {
      val disposalDate = LocalDate.parse("2015-10-05")

      "return a false if the acquisition date is before the tax start" in {
        val acquisitionDate = LocalDate.parse("2010-05-04")
        val result = PrivateResidenceReliefController.displayBetweenQuestion(Some(disposalDate), Some(acquisitionDate), false)

        result shouldBe false
      }

      "return a false if the acquisition date is after the tax start" in {
        val acquisitionDate = LocalDate.parse("2016-05-04")
        val result = PrivateResidenceReliefController.displayBetweenQuestion(Some(disposalDate), Some(acquisitionDate), false)

        result shouldBe false
      }

      "return a false if there is no acquisition date but a rebased value is given" in {
        val result = PrivateResidenceReliefController.displayBetweenQuestion(Some(disposalDate), None, true)

        result shouldBe false
      }

      "return a true if there is no acquisition date and no rebased value is given" in {
        val result = PrivateResidenceReliefController.displayBetweenQuestion(Some(disposalDate), None, false)

        result shouldBe false
      }
    }

    "the disposal date is not given" should {

      "return a false" in {
        val result = PrivateResidenceReliefController.displayBetweenQuestion(None, None, true)

        result shouldBe false
      }
    }
  }

  "Calling the .displayBeforeQuestion method" when {

    "the disposal date is after the 18 month period" should {
      val disposalDate = LocalDate.parse("2018-10-05")

      "return a true if the acquisition date is before the tax start" in {
        val acquisitionDate = LocalDate.parse("2010-05-04")
        val result = PrivateResidenceReliefController.displayBeforeQuestion(Some(disposalDate), Some(acquisitionDate))

        result shouldBe true
      }

      "return a true if the acquisition date is after the tax start" in {
        val acquisitionDate = LocalDate.parse("2016-05-04")
        val result = PrivateResidenceReliefController.displayBeforeQuestion(Some(disposalDate), Some(acquisitionDate))

        result shouldBe true
      }

      "return a false if there is no acquisition date" in {
        val result = PrivateResidenceReliefController.displayBeforeQuestion(Some(disposalDate), None)

        result shouldBe false
      }
    }

    "the disposal date is before the 18 month period" should {
      val disposalDate = LocalDate.parse("2015-10-05")

      "return a true if the acquisition date is before the tax start" in {
        val acquisitionDate = LocalDate.parse("2010-05-04")
        val result = PrivateResidenceReliefController.displayBeforeQuestion(Some(disposalDate), Some(acquisitionDate))

        result shouldBe true
      }

      "return a false if the acquisition date is after the tax start" in {
        val acquisitionDate = LocalDate.parse("2016-05-04")
        val result = PrivateResidenceReliefController.displayBeforeQuestion(Some(disposalDate), Some(acquisitionDate))

        result shouldBe false
      }

      "return a false if there is no acquisition date" in {
        val result = PrivateResidenceReliefController.displayBeforeQuestion(Some(disposalDate), None)

        result shouldBe false
      }
    }

    "the disposal date not given" should {

      "return a false if the acquisition date is before the tax start" in {
        val acquisitionDate = LocalDate.parse("2010-05-04")
        val result = PrivateResidenceReliefController.displayBeforeQuestion(None, Some(acquisitionDate))

        result shouldBe false
      }

      "return a false if the acquisition date is after the tax start" in {
        val acquisitionDate = LocalDate.parse("2016-05-04")
        val result = PrivateResidenceReliefController.displayBeforeQuestion(None, Some(acquisitionDate))

        result shouldBe false
      }

      "return a false if there is no acquisition date" in {
        val result = PrivateResidenceReliefController.displayBeforeQuestion(None, None)

        result shouldBe false
      }
    }
  }

  //GET Tests
  "Calling the .privateResidenceRelief action " when {

    "not supplied wth a pre-existing stored model" should {
      val target = setupTarget(
        None,
        disposalDateData = Some(DisposalDateModel(5, 8, 2015)),
        acquisitionDateData = Some(AcquisitionDateModel("No", None, None, None)),
        rebasedValueData = Some(RebasedValueModel(Some(1000))))
      lazy val result = target.privateResidenceRelief(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

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
        disposalDateData = Some(DisposalDateModel(5, 8, 2015)),
        acquisitionDateData = Some(AcquisitionDateModel("No", None, None, None)),
        rebasedValueData = Some(RebasedValueModel(Some(1000))))
      lazy val result = target.privateResidenceRelief(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

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
        disposalDateData = Some(DisposalDateModel(5, 8, 2015)),
        acquisitionDateData = Some(AcquisitionDateModel("No", None, None, None)),
        rebasedValueData = Some(RebasedValueModel(Some(1000))))
      lazy val result = target.privateResidenceRelief(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  //POST Tests
  "Calling the .submitPrivateResidenceRelief action " when {

    "submitting a valid form" should {
      val model = CalculationResultsWithPRRModel(GainsAfterPRRModel(1000, 0, 0), None, None)
      val target = setupTarget(
        None,
        disposalDateData = Some(DisposalDateModel(5, 8, 2015)),
        acquisitionDateData = Some(AcquisitionDateModel("No", None, None, None)),
        rebasedValueData = Some(RebasedValueModel(Some(1000))),
        calculationResultsWithPRRModel = Some(model))
      lazy val request = fakeRequestToPOSTWithSession(("isClaimingPRR", "No"))
      lazy val result = target.submitPrivateResidenceRelief(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the Check Your Answers page" in {
        redirectLocation(result).get shouldBe controllers.routes.CurrentIncomeController.currentIncome().url
      }
    }

    "submitting an invalid form" should {
      val target = setupTarget(
        None,
        disposalDateData = Some(DisposalDateModel(5, 8, 2015)),
        acquisitionDateData = Some(AcquisitionDateModel("No", None, None, None)),
        rebasedValueData = Some(RebasedValueModel(Some(1000))))
      lazy val request = fakeRequestToPOSTWithSession(("isClaimingPRR", ""))
      lazy val result = target.submitPrivateResidenceRelief(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "return to the private residence relief page" in {
        document.title() shouldBe messages.question
      }
    }
  }
}
