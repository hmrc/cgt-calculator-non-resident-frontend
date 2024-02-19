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
import assets.MessageLookup.NonResident.{DisposalCosts => messages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.{DisposalCostsController, routes}
import models._
import org.jsoup._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.calculation.disposalCosts
import assets.MessageLookup.{NonResident => commonMessages}
import services.SessionCacheService

import scala.concurrent.{ExecutionContext, Future}

class DisposalCostsActionSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper with BeforeAndAfterEach {
  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("SessionId")))

  val materializer = mock[Materializer]
  val ec = fakeApplication.injector.instanceOf[ExecutionContext]
  val mockHttp =mock[DefaultHttpClient]
  val mockCalcConnector =mock[CalculatorConnector]
  val mockMessage = mock[Messages]
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val disposalCostsView = fakeApplication.injector.instanceOf[disposalCosts]
  val pageTitle = s"""${messages.question} - ${commonMessages.pageHeading} - GOV.UK"""
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]

  class Setup {
    val controller = new DisposalCostsController(
      mockSessionCacheService,
      mockMessagesControllerComponents,
      disposalCostsView
    )(ec)
  }

  def setupTarget(getData: Option[DisposalCostsModel],
                  soldOrGivenModel: Option[SoldOrGivenAwayModel],
                  soldForLessModel: Option[SoldForLessModel]): DisposalCostsController = {

    when(mockSessionCacheService.fetchAndGetFormData[DisposalCostsModel](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheService.fetchAndGetFormData[SoldOrGivenAwayModel](
      ArgumentMatchers.eq(KeystoreKeys.soldOrGivenAway))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(soldOrGivenModel))

    when(mockSessionCacheService.fetchAndGetFormData[SoldForLessModel](ArgumentMatchers.eq(KeystoreKeys.soldForLess))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(soldForLessModel))

    when(mockSessionCacheService.saveFormData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(("", "")))

    new DisposalCostsController(mockSessionCacheService, mockMessagesControllerComponents, disposalCostsView)(ec)
  }

  //GET Tests
  "In CalculationController calling the .disposalCosts action " should {

    "not supplied with a pre-existing stored model" should {

      "return a 200" in new Setup {
        val target = setupTarget(None, None, None)
        lazy val result = target.disposalCosts(fakeRequestWithSession)
        status(result) shouldBe 200
      }

      s"have the title ${pageTitle}" in new Setup {
        val target = setupTarget(None, None, None)
        lazy val result = target.disposalCosts(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))
        document.getElementsByTag("title").text shouldBe pageTitle
      }

      "have a back link ot the missing data route" in new Setup {
        val target = setupTarget(None, None, None)
        lazy val result = target.disposalCosts(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))
        document.select(".govuk-back-link").attr("href") shouldEqual "#"
      }
    }

    "supplied with a pre-existing stored model" should {

      "return a 200" in {
        val target = setupTarget(None, None, None)
        lazy val result = target.disposalCosts(fakeRequestWithSession)
        status(result) shouldBe 200
      }

      s"have the title ${pageTitle}" in {
        val target = setupTarget(None, None, None)
        lazy val result = target.disposalCosts(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))

        document.getElementsByTag("title").text shouldBe pageTitle
      }
    }

    "supplied with an invalid session" should {

      "return a 303" in new Setup {
        val target = setupTarget(Some(DisposalCostsModel(1000)), None, None)
        lazy val result = target.disposalCosts(fakeRequest)
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in new Setup {
        val target = setupTarget(Some(DisposalCostsModel(1000)), None, None)
        lazy val result = target.disposalCosts(fakeRequest)
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }

    "when the property was given away" should {

      "return a 200" in new Setup {
        val target = setupTarget(None, Some(SoldOrGivenAwayModel(false)), Some(SoldForLessModel(true)))
        lazy val result = target.disposalCosts(fakeRequestWithSession)
        status(result) shouldBe 200
      }

      s"have the title ${pageTitle}" in new Setup {
        val target = setupTarget(None, Some(SoldOrGivenAwayModel(false)), Some(SoldForLessModel(true)))
        lazy val result = target.disposalCosts(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))
        document.getElementsByTag("title").text shouldBe pageTitle
      }

      "have a back link to the market value controller gave away" in new Setup {
        val target = setupTarget(None, Some(SoldOrGivenAwayModel(false)), Some(SoldForLessModel(true)))
        lazy val result = target.disposalCosts(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))
        document.select(".govuk-back-link").attr("href") shouldEqual "#"
      }
    }

    "when the property was sold and sold for less" should {

      "return a 200" in {
        val target = setupTarget(None, Some(SoldOrGivenAwayModel(true)), Some(SoldForLessModel(true)))
        lazy val result = target.disposalCosts(fakeRequestWithSession)
        status(result) shouldBe 200
      }

      s"have the title ${pageTitle}" in {
        val target = setupTarget(None, Some(SoldOrGivenAwayModel(true)), Some(SoldForLessModel(true)))
        lazy val result = target.disposalCosts(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))
        document.getElementsByTag("title").text shouldBe pageTitle
      }

      "have a back link to the market value controller when sold" in {
        val target = setupTarget(None, Some(SoldOrGivenAwayModel(true)), Some(SoldForLessModel(true)))
        lazy val result = target.disposalCosts(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))
        document.select(".govuk-back-link").attr("href") shouldEqual "#"
      }
    }

    "when the property was sold and not sold for less" should {

      "return a 200" in {
        val target = setupTarget(None, Some(SoldOrGivenAwayModel(true)), Some(SoldForLessModel(false)))
        lazy val result = target.disposalCosts(fakeRequestWithSession)
        status(result) shouldBe 200
      }

      s"have the title ${pageTitle}" in {
        val target = setupTarget(None, Some(SoldOrGivenAwayModel(true)), Some(SoldForLessModel(false)))
        lazy val result = target.disposalCosts(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))
        document.getElementsByTag("title").text shouldBe pageTitle
      }

      "have a back link to the market value controller disposal value" in {
        val target = setupTarget(None, Some(SoldOrGivenAwayModel(true)), Some(SoldForLessModel(false)))
        lazy val result = target.disposalCosts(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))
        document.select(".govuk-back-link").attr("href") shouldEqual "#"
      }
    }
  }

  //POST Tests
  "In CalculationController calling the .submitDisposalCosts action" when {

    "submitting a valid form with 1000" should {

      "return a 303" in {
        val target = setupTarget(None, None, None)
        lazy val request = fakeRequestToPOSTWithSession(("disposalCosts", "1000")).withMethod("POST")
        lazy val result = target.submitDisposalCosts(request)
        status(result) shouldBe 303
      }

      s"redirect to ${routes.AcquisitionDateController.acquisitionDate}" in {
        val target = setupTarget(None, None, None)
        lazy val request = fakeRequestToPOSTWithSession(("disposalCosts", "1000")).withMethod("POST")
        lazy val result = target.submitDisposalCosts(request)
        redirectLocation(result) shouldBe Some(s"${routes.AcquisitionDateController.acquisitionDate}")
      }
    }

    "submitting an invalid form with no value" should {

      "return a 400" in {
        val target = setupTarget(None, None, None)
        lazy val request = fakeRequestToPOSTWithSession(("disposalCosts", ""))
        lazy val result = target.submitDisposalCosts(request)
        status(result) shouldBe 400
      }

      "return to the disposal costs page" in {
        val target = setupTarget(None, None, None)
        lazy val request = fakeRequestToPOSTWithSession(("disposalCosts", ""))
        lazy val result = target.submitDisposalCosts(request)
        lazy val document = Jsoup.parse(bodyOf(result)(materializer, ec))
        document.title shouldEqual s"""Error: ${pageTitle}"""
      }
    }
  }
}
