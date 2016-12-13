/*
 * Copyright 2016 HM Revenue & Customs
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

import common.KeystoreKeys
import connectors.CalculatorConnector
import org.mockito.Matchers
import org.mockito.Mockito._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.jsoup._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import assets.MessageLookup.NonResident.{Improvements => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import constructors.nonresident.AnswersConstructor
import controllers.helpers.FakeRequestHelper

import scala.concurrent.Future
import controllers.nonresident.{ImprovementsController, routes}
import models.nonresident._
import uk.gov.hmrc.http.cache.client.CacheMap

class ImprovementsActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[ImprovementsModel],
                  acquisitionDateData: Option[AcquisitionDateModel],
                  rebasedValueData: Option[RebasedValueModel] = None,
                  totalGainResultsModel: Option[TotalGainResultsModel] = None
                 ): ImprovementsController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockAnswersConstructor = mock[AnswersConstructor]

    when(mockCalcConnector.fetchAndGetFormData[ImprovementsModel](Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[RebasedValueModel](Matchers.eq(KeystoreKeys.rebasedValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(rebasedValueData))

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionDateModel](Matchers.eq(KeystoreKeys.acquisitionDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(acquisitionDateData))

    when(mockAnswersConstructor.getNRTotalGainAnswers(Matchers.any()))
      .thenReturn(Future.successful(mock[TotalGainAnswersModel]))

    when(mockCalcConnector.calculateTotalGain(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(totalGainResultsModel))

    when(mockCalcConnector.saveFormData[ImprovementsModel](Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new ImprovementsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val answersConstructor: AnswersConstructor = mockAnswersConstructor
    }
  }

  "ImprovementsController" should {
    s"have a session timeout home link of '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
      ImprovementsController.homeLink shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
    }
  }

  "In CalculationController calling the .improvements action " when {

    "not supplied with a pre-existing stored model" should {

      "when Acquisition Date is supplied and > 5 April 2015" should {

        lazy val target = setupTarget(None, Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))), Some(RebasedValueModel(Some(1000))))
        lazy val result = target.improvements(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 200" in {
          status(result) shouldBe 200
        }

        "return some HTML that" should {

          s"have the title ${messages.question}" in {
            document.title shouldEqual messages.question
          }
        }

        s"have a 'Back' link to ${routes.AcquisitionCostsController.acquisitionCosts().url} " in {
          document.body.getElementById("back-link").attr("href") shouldEqual routes.AcquisitionCostsController.acquisitionCosts().url
        }
      }

      "when Acquisition Date is supplied and <= 5 April 2015" +
        "and a rebased value is supplied" should {

        lazy val target = setupTarget(
          None,
          Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2014))),
          Some(RebasedValueModel(Some(500)))
        )
        lazy val result = target.improvements(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have a back link that contains ${commonMessages.back}" in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
        }

        s"have a 'Back' link to ${routes.RebasedCostsController.rebasedCosts().url} " in {
          document.body.getElementById("back-link").attr("href") shouldEqual routes.RebasedCostsController.rebasedCosts().url
        }
      }

      "when Acquisition Date is not supplied" +
        "and a rebased value is supplied" should {

        lazy val target = setupTarget(
          None,
          //These values have been left in to make sure the controller is ignoring them as required
          Some(AcquisitionDateModel("No", Some(1), Some(1), Some(2014))),
          Some(RebasedValueModel(Some(500)))
        )
        lazy val result = target.improvements(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have a back link that contains ${commonMessages.back}" in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
        }

        s"have a 'Back' link to ${routes.RebasedCostsController.rebasedCosts().url} " in {
          document.body.getElementById("back-link").attr("href") shouldEqual routes.RebasedCostsController.rebasedCosts().url
        }
      }

      "when Acquisition Date is not supplied" +
        "and a rebased value is supplied but left blank" should {

        lazy val target = setupTarget(
          None,
          //These values have been left in to make sure the controller is ignoring them as required
          Some(AcquisitionDateModel("No", Some(1), Some(1), Some(2014))),
          Some(RebasedValueModel(None))
        )
        lazy val result = target.improvements(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have a back link that contains ${commonMessages.back}" in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
        }

        s"have a 'Back' link to ${routes.RebasedValueController.rebasedValue().url} " in {
          document.body.getElementById("back-link").attr("href") shouldEqual routes.RebasedValueController.rebasedValue().url
        }
      }

      "when Acquisition Date is supplied and <= 5 April 2015" +
        "and no rebased value is supplied" should {

        lazy val target = setupTarget(
          None,
          Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2014))),
          Some(RebasedValueModel(None))
        )
        lazy val result = target.improvements(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have a back link that contains ${commonMessages.back}" in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
        }

        s"have a 'Back' link to a missing data route ${controllers.nonresident.routes.DisposalDateController.disposalDate().url} " in {
          document.body.getElementById("back-link").attr("href") shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
        }
      }
    }
  }

  "In CalculationController calling the .submitImprovements action " when {

    "submitting a valid form with 'Yes' and a value of 12045 for improvementsAmt and no acquisition date but a rebased value of None" should {

      lazy val gainsModel = Some(TotalGainResultsModel(1000, Some(2000), None))
      lazy val target = setupTarget(None, Some(AcquisitionDateModel("No", Some(1), Some(1), Some(2016))), Some(RebasedValueModel(None)), gainsModel)
      lazy val request = fakeRequestToPOSTWithSession("isClaimingImprovements" -> "Yes", "improvementsAmt" -> "12045")
      lazy val result = target.submitImprovements(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${controllers.nonresident.routes.CustomerTypeController.customerType()}" in {
        redirectLocation(result) shouldBe Some(s"${controllers.nonresident.routes.CustomerTypeController.customerType()}")
      }
    }

    "submitting a valid form with but no gains model returned" should {

      lazy val gainsModel = None
      lazy val target = setupTarget(None, Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2016))), gainsModel)
      lazy val request = fakeRequestToPOSTWithSession("isClaimingImprovements" -> "No", "improvementsAmt" -> "")
      lazy val result = target.submitImprovements(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${common.DefaultRoutes.missingDataRoute}" in {
        redirectLocation(result) shouldBe Some(s"${common.DefaultRoutes.missingDataRoute}")
      }
    }

    "submitting a valid form with 'No' and total gains of less than 0" should {

      lazy val gainsModel = Some(TotalGainResultsModel(-1000, None, None))
      lazy val target = setupTarget(None, Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2014))), None, gainsModel)
      lazy val request = fakeRequestToPOSTWithSession("isClaimingImprovements" -> "No", "improvementsAmt" -> "")
      lazy val result = target.submitImprovements(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.CheckYourAnswersController.checkYourAnswers()}" in {
        redirectLocation(result) shouldBe Some(s"${routes.CheckYourAnswersController.checkYourAnswers()}")
      }
    }

    "submitting a valid form with a rebased value" should {

      lazy val gainsModel = Some(TotalGainResultsModel(1000, Some(2000), None))
      lazy val target = setupTarget(None, Some(AcquisitionDateModel("No", None, None, None)), Some(RebasedValueModel(Some(2000))), gainsModel)
      lazy val request = fakeRequestToPOSTWithSession("isClaimingImprovements" -> "No", "improvementsAmt" -> "")
      lazy val result = target.submitImprovements(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${controllers.nonresident.routes.PrivateResidenceReliefController.privateResidenceRelief()}" in {
        redirectLocation(result) shouldBe Some(s"${controllers.nonresident.routes.PrivateResidenceReliefController.privateResidenceRelief()}")
      }
    }

    "submitting an invalid form with 'testData123' and a value of 'fhu39awd8'" should {

      val target = setupTarget(None, None)
      lazy val request = fakeRequestToPOSTWithSession("isClaimingImprovements" -> "testData123", "improvementsAmt" -> "fhu39awd8")
      lazy val result = target.submitImprovements(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "return to the improvements page" in {
        document.title shouldBe messages.question
      }
    }
  }
}
