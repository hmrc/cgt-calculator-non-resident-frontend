/*
 * Copyright 2020 HM Revenue & Customs
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
import config.{AppConfig, ApplicationConfig}
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.{PropertyLivedInController, ReportController}
import models.PropertyLivedInModel
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import common.KeystoreKeys.{NonResidentKeys => keyStoreKeys}
import org.mockito.ArgumentMatchers
import assets.MessageLookup.{PropertyLivedIn => messages}
import org.jsoup.Jsoup
import play.api.test.Helpers.{contentType, _}
import uk.gov.hmrc.http.cache.client.CacheMap
import connectors.CalculatorConnector
import constructors.{AnswersConstructor, DefaultCalculationElectionConstructor}
import controllers.predicates.ValidActiveSession
import forms.PropertyLivedInForm._
import play.api.Environment
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.data.Form
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.SessionId
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.concurrent.Future

class PropertyLivedInActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))

  val materializer = mock[Materializer]
  val mockHttp =mock[DefaultHttpClient]
  val mockCalcConnector =mock[CalculatorConnector]
  val defaultCache = mock[CacheMap]
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]


  class Setup {
    val controller = new PropertyLivedInController(
      mockHttp,
      mockCalcConnector,
      mockMessagesControllerComponents
    )(mockConfig, fakeApplication)
  }


  def setupTarget(getData: Option[PropertyLivedInModel]): PropertyLivedInController= {

    when(mockCalcConnector.fetchAndGetFormData[PropertyLivedInModel](ArgumentMatchers.eq(keyStoreKeys.propertyLivedIn))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData[PropertyLivedInModel](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new PropertyLivedInController(mockHttp, mockCalcConnector, mockMessagesControllerComponents)(mockConfig, fakeApplication) {
      val calcConnector: CalculatorConnector = mockCalcConnector
          }
  }

  "Calling .propertyLivedIn from the resident PropertyLivedInController" when {

    "request has a valid session and no keystore value" should {

      lazy val target = setupTarget(None)
      lazy val result = target.propertyLivedIn(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(bodyOf(result)(materializer)).title shouldEqual messages.title
      }
    }

    "request has a valid session and some keystore value" should {

      lazy val target = setupTarget(Some(PropertyLivedInModel(true)))
      lazy val result = target.propertyLivedIn(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(bodyOf(result)(materializer)).title shouldEqual messages.title
      }
    }

    "request has an invalid session" should {

      lazy val target = setupTarget(None)
      lazy val result = target.propertyLivedIn(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling .submitPropertyLivedIn " when {

    "a valid form with the answer 'Yes' is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("propertyLivedIn", "Yes"))
      lazy val result = target.submitPropertyLivedIn(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the private residence relief page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/non-resident/private-residence-relief")
      }
    }

    "a valid form with the answer 'No' is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("propertyLivedIn", "No"))
      lazy val result = target.submitPropertyLivedIn(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the currentIncome page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/non-resident/current-income")
      }
    }

    "an invalid form with the answer '' is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("propertyLivedIn", ""))
      lazy val result = target.submitPropertyLivedIn(request)
      lazy val doc = Jsoup.parse(bodyOf(result)(materializer))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "render the Property Lived In page" in {
        doc.title() shouldEqual messages.title
      }
    }
  }
}