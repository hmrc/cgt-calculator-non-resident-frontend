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

import assets.MessageLookup.NonResident.{AcquisitionCosts => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.AcquisitionCostsController
import controllers.helpers.FakeRequestHelper
import models.{AcquisitionCostsModel, BoughtForLessModel, DateModel, HowBecameOwnerModel}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import views.html.calculation.acquisitionCosts

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AcquisitionCostsActionSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper
  with BeforeAndAfterEach {
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMessagesControllerComponents: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val acquisitionCostsView: acquisitionCosts = fakeApplication.injector.instanceOf[acquisitionCosts]
  lazy val pageTitle = s"""${messages.question} - ${commonMessages.serviceName} - GOV.UK"""

  class Setup {
    val controller = new AcquisitionCostsController(
      mockSessionCacheService,
      mockMessagesControllerComponents,
      acquisitionCostsView
    )
  }

  def setupTarget(getData: Option[AcquisitionCostsModel],
                  acquisitionDateData: Option[DateModel] = Some(DateModel(10, 5, 2001)),
                  howBecameOwnerData: Option[HowBecameOwnerModel] = None,
                  boughtForLessData: Option[BoughtForLessModel] = None): AcquisitionCostsController = {

    when(mockSessionCacheService.fetchAndGetFormData[AcquisitionCostsModel](
      ArgumentMatchers.eq(KeystoreKeys.acquisitionCosts))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheService.fetchAndGetFormData[DateModel](
      ArgumentMatchers.eq(KeystoreKeys.acquisitionDate))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(acquisitionDateData))

    when(mockSessionCacheService.fetchAndGetFormData[HowBecameOwnerModel](
      ArgumentMatchers.eq(KeystoreKeys.howBecameOwner))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(howBecameOwnerData))

    when(mockSessionCacheService.fetchAndGetFormData[BoughtForLessModel](
      ArgumentMatchers.eq(KeystoreKeys.boughtForLess))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(boughtForLessData))

    when(mockSessionCacheService.saveFormData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(("", "")))

    new AcquisitionCostsController(mockSessionCacheService, mockMessagesControllerComponents, acquisitionCostsView)
  }

  "Calling the .backLink method" should {

    "return a link to WorthOnLegislationStart page with an acquisition date before legislation start" in {
      val target = setupTarget(None, acquisitionDateData = Some(DateModel(10, 5, 1972)))
      val result = target.getBackLink

      await(result) shouldBe controllers.routes.WorthBeforeLegislationStartController.worthBeforeLegislationStart.url
    }

    "return a link to WorthWhenGifted page with an acquisition date after legislation start and gifted option" in {
      val target = setupTarget(None,
        acquisitionDateData = Some(DateModel(10, 5, 2000)),
        howBecameOwnerData = Some(HowBecameOwnerModel("Gifted")))
      val result = target.getBackLink

      await(result) shouldBe controllers.routes.WorthWhenGiftedToController.worthWhenGiftedTo.url
    }

    "return a link to WorthWhenInherited when property was inherited" in {
      val target = setupTarget(None, howBecameOwnerData = Some(HowBecameOwnerModel("Inherited")))
      val result = target.getBackLink

      await(result) shouldBe controllers.routes.WorthWhenInheritedController.worthWhenInherited.url
    }

    "return a link to BoughtForLess when property was bought for less" in {
      val target = setupTarget(None, howBecameOwnerData = Some(HowBecameOwnerModel("Bought")), boughtForLessData = Some(BoughtForLessModel(true)))
      val result = target.getBackLink

      await(result) shouldBe controllers.routes.WorthWhenBoughtForLessController.worthWhenBoughtForLess.url
    }

    "return a link to acquisition value when not bought for less" in {
      val target = setupTarget(None, acquisitionDateData = Some(DateModel(1, 1, 2016)),
        howBecameOwnerData = Some(HowBecameOwnerModel("Bought")),
        boughtForLessData = Some(BoughtForLessModel(false)))
      val result = target.getBackLink

      await(result) shouldBe controllers.routes.AcquisitionValueController.acquisitionValue.url
    }
  }

  "Calling the .acquisitionCosts action " should {

    "not supplied with a pre-existing stored model" should {

      "return a 200" in {
        val target = setupTarget(None, acquisitionDateData = Some(DateModel(1, 1, 2016)), Some(HowBecameOwnerModel("Gifted")))
        lazy val result = target.acquisitionCosts(fakeRequestWithSession)
        status(result) shouldBe 200
      }

      "load the acquisitionCosts page" in {
        val target = setupTarget(None, acquisitionDateData = Some(DateModel(1, 1, 2016)), Some(HowBecameOwnerModel("Gifted")))
        lazy val result = target.acquisitionCosts(fakeRequestWithSession)
        lazy val document = Jsoup.parse(contentAsString(result))
        document.title shouldBe pageTitle
      }

      "have a back link to the WorthWhenGiftedTo page" in {
        val target = setupTarget(None, acquisitionDateData = Some(DateModel(1, 1, 2016)), Some(HowBecameOwnerModel("Gifted")))
        lazy val result = target.acquisitionCosts(fakeRequestWithSession)
        lazy val document = Jsoup.parse(contentAsString(result))
        document.select(".govuk-back-link").attr("href") shouldBe "#"
      }
    }

    "supplied with a pre-existing stored model" should {

      "return a 200" in {
        val testAcquisitionCostsModel = new AcquisitionCostsModel(1000)
        val target = setupTarget(Some(testAcquisitionCostsModel))
        lazy val result = target.acquisitionCosts(fakeRequestWithSession)
        status(result) shouldBe 200
      }

      "load the acquisitionCosts page" in {
        val testAcquisitionCostsModel = new AcquisitionCostsModel(1000)
        val target = setupTarget(Some(testAcquisitionCostsModel))
        lazy val result = target.acquisitionCosts(fakeRequestWithSession)
        lazy val document = Jsoup.parse(contentAsString(result))
        document.title shouldBe pageTitle
      }
    }

    "without a valid session" should {

      "return a 303" in {
        val target = setupTarget(None)
        lazy val result = target.acquisitionCosts(fakeRequest)
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        val target = setupTarget(None)
        lazy val result = target.acquisitionCosts(fakeRequest)
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling the .submitAcquisitionCosts action" when {

    "supplied with an acquisition date after the tax start" should{

      "return a 303" in {
        val target = setupTarget(None, acquisitionDateData = Some(DateModel(10, 5, 2016)))
        lazy val request = fakeRequestToPOSTWithSession(("acquisitionCosts", "1000")).withMethod("POST")
        lazy val result = target.submitAcquisitionCosts(request)
        status(result) shouldBe 303
      }

      s"redirect to '${controllers.routes.ImprovementsController.getIsClaimingImprovements.url}'" in {
        val target = setupTarget(None, acquisitionDateData = Some(DateModel(10, 5, 2016)))
        lazy val request = fakeRequestToPOSTWithSession(("acquisitionCosts", "1000")).withMethod("POST")
        lazy val result = target.submitAcquisitionCosts(request)
        redirectLocation(result).get shouldBe controllers.routes.ImprovementsController.getIsClaimingImprovements.url
      }
    }

    "supplied with an acquisition date before the tax start" should {

      "return a 303" in {
        val target = setupTarget(None, acquisitionDateData = Some(DateModel(10, 5, 2000)))
        lazy val request = fakeRequestToPOSTWithSession(("acquisitionCosts", "1000")).withMethod("POST")
        lazy val result = target.submitAcquisitionCosts(request)
        status(result) shouldBe 303
      }

      s"redirect to '${controllers.routes.RebasedValueController.rebasedValue.url}'" in {
        val target = setupTarget(None, acquisitionDateData = Some(DateModel(10, 5, 2000)))
        lazy val request = fakeRequestToPOSTWithSession(("acquisitionCosts", "1000")).withMethod("POST")
        lazy val result = target.submitAcquisitionCosts(request)
        redirectLocation(result).get shouldBe controllers.routes.RebasedValueController.rebasedValue.url
      }
    }

    "supplied with an invalid form" should {

      "return a 400" in {
        val target = setupTarget(None, acquisitionDateData = Some(DateModel(1, 1, 2016)), Some(HowBecameOwnerModel("Inherited")))
        lazy val request = fakeRequestToPOSTWithSession(("acquisitionCosts", "a"))
        lazy val result = target.submitAcquisitionCosts(request)
        status(result) shouldBe 400
      }

      "return to the acquisition costs page" in {
        val target = setupTarget(None, acquisitionDateData = Some(DateModel(1, 1, 2016)), Some(HowBecameOwnerModel("Inherited")))
        lazy val request = fakeRequestToPOSTWithSession(("acquisitionCosts", "a"))
        lazy val result = target.submitAcquisitionCosts(request)
        lazy val document = Jsoup.parse(contentAsString(result))
        document.title shouldBe s"""Error: $pageTitle"""
      }

      "have a back link to the WorthWhenInherited page" in {
        val target = setupTarget(None, acquisitionDateData = Some(DateModel(1, 1, 2016)), Some(HowBecameOwnerModel("Inherited")))
        lazy val request = fakeRequestToPOSTWithSession(("acquisitionCosts", "a"))
        lazy val result = target.submitAcquisitionCosts(request)
        lazy val document = Jsoup.parse(contentAsString(result))
        document.select(".govuk-back-link").attr("href") shouldBe "#"
      }
    }
  }
}
