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

import assets.MessageLookup.NonResident.{CalculationElection => messages}
import common.TestModels
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import connectors.CalculatorConnector
import constructors.{AnswersConstructor, CalculationElectionConstructor}
import controllers.CalculationElectionController
import controllers.helpers.FakeRequestHelper
import controllers.nonresident.routes
import models.{TaxYearModel, _}
import org.jsoup._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class CalculationElectionActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[CalculationElectionModel],
                  postData: Option[CalculationElectionModel],
                  totalGainResultsModel: Option[TotalGainResultsModel],
                  contentElements: Seq[(String, String, String, Option[String], Option[BigDecimal])],
                  finalSummaryModel: TotalPersonalDetailsCalculationModel,
                  taxOwedResult: Option[CalculationResultsWithTaxOwedModel] = None
                 ): CalculationElectionController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockCalcElectionConstructor = mock[CalculationElectionConstructor]
    val mockCalcAnswersConstructor = mock[AnswersConstructor]

    when(mockCalcConnector.fetchAndGetFormData[CalculationElectionModel](Matchers.eq(KeystoreKeys.calculationElection))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcAnswersConstructor.getNRTotalGainAnswers(Matchers.any()))
      .thenReturn(Future.successful(TestModels.businessScenarioFiveModel))

    when(mockCalcConnector.calculateTotalGain(Matchers.any())(Matchers.any()))
      .thenReturn(totalGainResultsModel)

    when(mockCalcElectionConstructor.generateElection(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(contentElements))

    when(mockCalcAnswersConstructor.getPersonalDetailsAndPreviousCapitalGainsAnswers(Matchers.any()))
      .thenReturn(Future.successful(Some(finalSummaryModel)))

    when(mockCalcConnector.getFullAEA(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(BigDecimal(11000))))

    when(mockCalcConnector.getPartialAEA(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(BigDecimal(5500))))

    when(mockCalcConnector.calculateNRCGTTotalTax(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(taxOwedResult))

    when(mockCalcConnector.getTaxYear(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(TaxYearModel("2015/16", true, "2015/16"))))

    new CalculationElectionController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val calcElectionConstructor: CalculationElectionConstructor = mockCalcElectionConstructor
      override val calcAnswersConstructor: AnswersConstructor = mockCalcAnswersConstructor
    }
  }

  "CalculationElectionController" should {
    s"have a session timeout home link of '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
      CalculationElectionController.homeLink shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
    }
  }

  val finalAnswersModel = TotalPersonalDetailsCalculationModel(
    CustomerTypeModel("representative"),
    None,
    None,
    None,
    OtherPropertiesModel("No"),
    None,
    None,
    None,
    None,
    BroughtForwardLossesModel(false, None)
  )

  // GET Tests
  "In CalculationController calling the .calculationElection action" when {

    lazy val seq = Seq(("flat", "300", "A question", Some("Another bit of a question"), Some(BigDecimal(100))))

    "supplied with no pre-existing session" should {

      lazy val target = setupTarget(
        None,
        None,
        Some(TotalGainResultsModel(0, Some(0), Some(0))),
        seq,
        finalAnswersModel
      )
      lazy val result = target.calculationElection(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }

    "supplied with no pre-existing data" should {

      lazy val target = setupTarget(
        None,
        None,
        Some(TotalGainResultsModel(0, Some(0), Some(0))),
        seq,
        finalAnswersModel
      )
      lazy val result = target.calculationElection(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "be on the calculation election page" in {
        document.title() shouldEqual messages.question
      }
    }

    "supplied with a pre-existing model" which {
      lazy val target = setupTarget(
        Some(CalculationElectionModel("flat")),
        None,
        Some(TotalGainResultsModel(0, Some(0), Some(0))),
        seq,
        finalAnswersModel
      )
      lazy val result = target.calculationElection(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "be on the calculation election page" in {
        document.title() shouldEqual messages.question
      }
    }
  }

  "In CalculationController calling the .submitCalculationElection action" when {

    lazy val seq = Seq(("flat", "300", "A question", Some("Another bit of a question"), None))

    "submitting a valid calculation election" should {

      lazy val request = fakeRequestToPOSTWithSession(("calculationElection", "flat"))
      lazy val target = setupTarget(
        None,
        None,
        Some(TotalGainResultsModel(0, Some(0), Some(0))),
        seq,
        finalAnswersModel
      )
      lazy val result = target.submitCalculationElection(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the summary page" in {
        redirectLocation(result) shouldBe Some(s"${routes.SummaryController.summary()}")
      }
    }

    "submitting a valid calculation election using the flat reliefs button" should {
      lazy val request = fakeRequestToPOSTWithSession(("calculationElection", "flat"), ("action", "flat"))
      lazy val target = setupTarget(
        None,
        None,
        Some(TotalGainResultsModel(0, Some(0), Some(0))),
        seq,
        finalAnswersModel
      )
      lazy val result = target.submitCalculationElection(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the other reliefs flat page" in {
        redirectLocation(result) shouldBe Some(s"${routes.OtherReliefsFlatController.otherReliefsFlat()}")
      }
    }

    "submitting a valid calculation election using the rebased reliefs button" should {
      lazy val request = fakeRequestToPOSTWithSession(("calculationElection", "rebased"), ("action", "rebased"))
      lazy val target = setupTarget(
        None,
        None,
        Some(TotalGainResultsModel(0, Some(0), Some(0))),
        seq,
        finalAnswersModel
      )
      lazy val result = target.submitCalculationElection(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the other reliefs rebased page" in {
        redirectLocation(result) shouldBe Some(s"${routes.OtherReliefsRebasedController.otherReliefsRebased()}")
      }
    }

    "submitting a valid calculation election using the time apportioned reliefs button" should {
      lazy val request = fakeRequestToPOSTWithSession(("calculationElection", "time"), ("action", "time"))
      lazy val target = setupTarget(
        None,
        None,
        Some(TotalGainResultsModel(0, Some(0), Some(0))),
        seq,
        finalAnswersModel
      )
      lazy val result = target.submitCalculationElection(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the other reliefs flat page" in {
        redirectLocation(result) shouldBe Some(s"${routes.OtherReliefsTAController.otherReliefsTA()}")
      }
    }

    "submitting an invalid calculation election" should {

      lazy val request = fakeRequestToPOSTWithSession(("calculationElection", "fehuifoh"))
      lazy val target = setupTarget(
        None,
        None,
        Some(TotalGainResultsModel(0, Some(0), Some(0))),
        seq,
        finalAnswersModel
      )
      lazy val result = target.submitCalculationElection(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "return to the calculation election page" in {
        document.title shouldEqual messages.question
      }
    }
  }

  "CalculationElectionController" should {
    "use the correct keystore connector" in {
      CalculationElectionController.calcConnector shouldBe CalculatorConnector
    }
  }
}
