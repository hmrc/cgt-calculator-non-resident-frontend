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
import assets.MessageLookup.NonResident.{Improvements => messages}
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
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.improvements

import scala.concurrent.{ExecutionContext, Future}

class ImprovementsActionSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))

  val materializer = mock[Materializer]
  val ec = fakeApplication.injector.instanceOf[ExecutionContext]
  val mockHttp =mock[DefaultHttpClient]
  val mockCalcConnector =mock[CalculatorConnector]
  val defaultCache = mock[CacheMap]
  val mockAnswersConstructor = mock[AnswersConstructor]
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val improvementsView = fakeApplication.injector.instanceOf[improvements]

  class Setup {
    val controller = new ImprovementsController(
      mockHttp,
      mockCalcConnector,
      mockAnswersConstructor,
      mockMessagesControllerComponents,
      improvementsView
    )(ec)
  }

  def setupTarget(getData: Option[ImprovementsModel],
                  acquisitionDateData: Option[DateModel],
                  rebasedValueData: Option[RebasedValueModel] = None,
                  totalGainResultsModel: Option[TotalGainResultsModel] = None
                 ): ImprovementsController = {

    when(mockCalcConnector.fetchAndGetFormData[ImprovementsModel](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[RebasedValueModel](
      ArgumentMatchers.eq(KeystoreKeys.rebasedValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(rebasedValueData))

    when(mockCalcConnector.fetchAndGetFormData[DateModel](
      ArgumentMatchers.eq(KeystoreKeys.acquisitionDate))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(acquisitionDateData))

    when(mockAnswersConstructor.getNRTotalGainAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[TotalGainAnswersModel]))

    when(mockCalcConnector.calculateTotalGain(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGainResultsModel))

    when(mockCalcConnector.saveFormData[ImprovementsModel](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new ImprovementsController(mockHttp, mockCalcConnector, mockAnswersConstructor, mockMessagesControllerComponents, improvementsView)(ec)
  }

  "In CalculationController calling the .improvements action " when {

    "not supplied with a pre-existing stored model" should {

      "when Acquisition Date is > 5 April 2015" should {

        lazy val target = setupTarget(None, Some(DateModel(1, 1, 2017)), Some(RebasedValueModel(1000)))
        lazy val result = target.improvements(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

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

      "when Acquisition Date is <= 5 April 2015" should {

        lazy val target = setupTarget(
          None,
          Some(DateModel(1, 1, 2014)),
          Some(RebasedValueModel(500))
        )
        lazy val result = target.improvements(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

        s"have a back link that contains ${commonMessages.back}" in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
        }

        s"have a 'Back' link to ${routes.RebasedCostsController.rebasedCosts().url} " in {
          document.body.getElementById("back-link").attr("href") shouldEqual routes.RebasedCostsController.rebasedCosts().url
        }
      }
    }
  }

  "In CalculationController calling the .submitImprovements action " when {

    "submitting a valid form with but no gains model returned" should {

      lazy val gainsModel = None
      lazy val target = setupTarget(None, Some(DateModel(1, 1, 2016)), gainsModel)
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
      lazy val target = setupTarget(None, Some(DateModel(1, 1, 2014)), None, gainsModel)
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
      lazy val target = setupTarget(None, Some(DateModel(1, 1, 2014)), Some(RebasedValueModel(2000)), gainsModel)
      lazy val request = fakeRequestToPOSTWithSession("isClaimingImprovements" -> "No", "improvementsAmt" -> "")
      lazy val result = target.submitImprovements(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${controllers.routes.PropertyLivedInController.propertyLivedIn()}" in {
        redirectLocation(result) shouldBe Some(s"${controllers.routes.PropertyLivedInController.propertyLivedIn()}")
      }
    }

    "submitting an invalid form with 'testData123' and a value of 'fhu39awd8'" should {

      val target = setupTarget(None, Some(DateModel(1, 1, 2014)))
      lazy val request = fakeRequestToPOSTWithSession("isClaimingImprovements" -> "testData123", "improvementsAmt" -> "fhu39awd8")
      lazy val result = target.submitImprovements(request)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "return to the improvements page" in {
        document.title shouldBe messages.question
      }
    }
  }
}
