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

import akka.stream.Materializer
import assets.MessageLookup.NonResident.{CalculationElection => messages, CalculationElectionNoReliefs => nRMessages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.{CommonPlaySpec, TestModels, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import constructors.{AnswersConstructor, DefaultCalculationElectionConstructor}
import controllers.helpers.FakeRequestHelper
import controllers.{CalculationElectionController, routes}
import models.{TaxYearModel, _}
import org.jsoup._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.concurrent.Future

class CalculationElectionActionSpec ()
  extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))

  val materializer = mock[Materializer]
  val mockHttp =mock[DefaultHttpClient]
  val mockCalcConnector =mock[CalculatorConnector]
  val defaultCache = mock[CacheMap]
  val mockAnswersConstructor = mock[AnswersConstructor]
  val mockDefaultCalElecConstructor = mock[DefaultCalculationElectionConstructor]
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]

  class Setup {
    val controller = new CalculationElectionController(
      mockHttp,
      mockCalcConnector,
      mockAnswersConstructor,
      mockDefaultCalElecConstructor,
      mockMessagesControllerComponents
    )(mockConfig, fakeApplication)
  }


  def setupTarget(getData: Option[CalculationElectionModel],
                  postData: Option[CalculationElectionModel],
                  totalGainResultsModel: Option[TotalGainResultsModel],
                  contentElements: Seq[(String, String, String, String, Option[String], Option[BigDecimal])],
                  finalSummaryModel: TotalPersonalDetailsCalculationModel,
                  taxOwedResult: Option[CalculationResultsWithTaxOwedModel] = None,
                  claimingReliefs: Boolean = true
                 ): CalculationElectionController = {

    when(mockCalcConnector.fetchAndGetFormData[OtherReliefsModel](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(OtherReliefsModel(1000))))

    when(mockCalcConnector.fetchAndGetFormData[CalculationElectionModel](
      ArgumentMatchers.eq(KeystoreKeys.calculationElection))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockAnswersConstructor.getNRTotalGainAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(TestModels.businessScenarioFiveModel))

    when(mockCalcConnector.calculateTotalGain(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(totalGainResultsModel)

    when(mockDefaultCalElecConstructor.generateElection(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(contentElements))

    when(mockAnswersConstructor.getPersonalDetailsAndPreviousCapitalGainsAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(finalSummaryModel)))

    when(mockCalcConnector.getFullAEA(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(BigDecimal(11000))))

    when(mockCalcConnector.calculateNRCGTTotalTax(
      ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(),
      ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(taxOwedResult))

    when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(TaxYearModel("2015/16", isValidYear = true, "2015/16"))))

    when(mockCalcConnector.fetchAndGetFormData[ClaimingReliefsModel](
      ArgumentMatchers.eq(KeystoreKeys.claimingReliefs))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(ClaimingReliefsModel(claimingReliefs))))

    when(mockCalcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](
      ArgumentMatchers.eq(KeystoreKeys.privateResidenceRelief))(ArgumentMatchers.any(), ArgumentMatchers.any()))
    .thenReturn(Future.successful(Some(PrivateResidenceReliefModel("Yes", Some(0), Some(0)))))

    when(mockCalcConnector.calculateTaxableGainAfterPRR(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
    .thenReturn(Future.successful(Some(CalculationResultsWithPRRModel(GainsAfterPRRModel(0, 0, 0), None, None))))

    when(mockCalcConnector.fetchAndGetFormData[PropertyLivedInModel](ArgumentMatchers.eq(KeystoreKeys.propertyLivedIn))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(PropertyLivedInModel(true))))

    new CalculationElectionController(mockHttp, mockCalcConnector, mockAnswersConstructor, mockDefaultCalElecConstructor, mockMessagesControllerComponents)(mockConfig, fakeApplication)
  }

  val finalAnswersModel = TotalPersonalDetailsCalculationModel(
    CurrentIncomeModel(0),
    None,
    OtherPropertiesModel("No"),
    None,
    None,
    None,
    None,
    BroughtForwardLossesModel(isClaiming = false, None)
  )

  // GET Tests
  "In CalculationController calling the .calculationElection action" when {

    lazy val seq = Seq(("flat", "300", "A question", "description", Some("Another bit of a question"), Some(BigDecimal(100))))

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
      lazy val document = Jsoup.parse(bodyOf(result)(materializer))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "be on the calculation election page" in {
        document.title() shouldEqual nRMessages.title
      }
    }

    "supplied with a pre-existing model with tax owed" which {
      lazy val target = setupTarget(
        Some(CalculationElectionModel("flat")),
        None,
        Some(TotalGainResultsModel(200, None, None)),
        seq,
        finalAnswersModel
      )
      lazy val result = target.calculationElection(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "be on the calculation election page" in {
        document.title() shouldEqual messages.heading
      }

      s"has a back link of ${routes.ClaimingReliefsController.claimingReliefs().url}" in{
        document.select("a#back-link").attr("href") shouldBe routes.ClaimingReliefsController.claimingReliefs().url
      }
    }

    "supplied with a pre-existing model with no tax owed" which {
      lazy val target = setupTarget(
        Some(CalculationElectionModel("flat")),
        None,
        Some(TotalGainResultsModel(0, Some(0), Some(0))),
        seq,
        finalAnswersModel
      )
      lazy val result = target.calculationElection(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "be on the calculation election no reliefs page" in {
        document.title() shouldEqual nRMessages.title
      }

      s"has a back link of ${routes.CheckYourAnswersController.checkYourAnswers().url}" in{
        document.select("a#back-link").attr("href") shouldBe routes.CheckYourAnswersController.checkYourAnswers().url
      }
    }

    "supplied with a pre-existing model and not claiming reliefs" which {
      lazy val target = setupTarget(
        Some(CalculationElectionModel("flat")),
        None,
        Some(TotalGainResultsModel(1000, Some(0), Some(0))),
        seq,
        finalAnswersModel,
        claimingReliefs = false
      )
      lazy val result = target.calculationElection(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "be on the calculation election page" in {
        document.title() shouldEqual nRMessages.title
      }

      s"has a back link of ${routes.ClaimingReliefsController.claimingReliefs().url}" in{
        document.select("a#back-link").attr("href") shouldBe routes.ClaimingReliefsController.claimingReliefs().url
      }
    }
  }

  "In CalculationController calling the .submitCalculationElection action" when {

    lazy val seq = Seq(("flat", "300", "A question", "description", Some("Another bit of a question"), None))

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

      lazy val request = fakeRequestToPOSTWithSession(("calculationElection", "something"))
      lazy val target = setupTarget(
        None,
        None,
        Some(TotalGainResultsModel(0, Some(0), Some(0))),
        seq,
        finalAnswersModel
      )
      lazy val result = target.submitCalculationElection(request)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "return to the calculation election page" in {
        document.title shouldEqual nRMessages.title
      }
    }
  }



  "Calling .orderElements" should {
    val sequence: Seq[(String, String, String, String, Option[String], Option[BigDecimal])] = Seq(
      ("flat", "test", "test", "test", None, Some(500)),
      ("rebased", "test", "test", "test", None, None),
      ("time", "test", "test", "test", None, None)
    )
    val sequenceNoReliefs: Seq[(String, String, String, String, Option[String], Option[BigDecimal])] = Seq(
      ("flat", "test", "test", "test", None, None),
      ("rebased", "test", "test", "test", None, None),
      ("time", "test", "test", "test", None, None)
    )
    val orderedSequence: Seq[(String, String, String, String, Option[String], Option[BigDecimal])] = Seq(
      ("rebased", "test", "test", "test", None, None),
      ("time", "test", "test", "test", None, None),
      ("flat", "test", "test", "test", None, Some(500))
    )

    "return the original sequence if not claiming reliefs" in {

      lazy val target = setupTarget(
        None,
        None,
        Some(TotalGainResultsModel(0, Some(0), Some(0))),
        Seq.empty,
        finalAnswersModel
      )
      target.orderElements(sequence, false) shouldBe sequenceNoReliefs
    }

    "return the ordered sequence if claiming reliefs" in {
      lazy val target = setupTarget(
        None,
        None,
        Some(TotalGainResultsModel(0, Some(0), Some(0))),
        Seq.empty,
        finalAnswersModel
      )
      target.orderElements(sequence, true) shouldBe orderedSequence
    }
  }
}
