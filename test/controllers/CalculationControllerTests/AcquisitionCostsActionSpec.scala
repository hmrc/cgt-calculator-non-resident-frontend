/*
 * Copyright 2018 HM Revenue & Customs
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
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import connectors.CalculatorConnector
import controllers.AcquisitionCostsController
import controllers.helpers.FakeRequestHelper
import models.{AcquisitionCostsModel, AcquisitionDateModel, BoughtForLessModel, HowBecameOwnerModel}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

class AcquisitionCostsActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[AcquisitionCostsModel],
                  acquisitionDateData: Option[AcquisitionDateModel] = Some(AcquisitionDateModel(10, 5, 2001)),
                  howBecameOwnerData: Option[HowBecameOwnerModel] = None,
                  boughtForLessData: Option[BoughtForLessModel] = None): AcquisitionCostsController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionCostsModel](
      ArgumentMatchers.eq(KeystoreKeys.acquisitionCosts))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionDateModel](
      ArgumentMatchers.eq(KeystoreKeys.acquisitionDate))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(acquisitionDateData)

    when(mockCalcConnector.fetchAndGetFormData[HowBecameOwnerModel](
      ArgumentMatchers.eq(KeystoreKeys.howBecameOwner))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(howBecameOwnerData)

    when(mockCalcConnector.fetchAndGetFormData[BoughtForLessModel](
      ArgumentMatchers.eq(KeystoreKeys.boughtForLess))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(boughtForLessData)

    when(mockCalcConnector.saveFormData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map.empty)))

    new AcquisitionCostsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "Calling the .backLink method" should {

    "return a link to WorthOnLegislationStart page with an acquisition date before legislation start" in {
      val target = setupTarget(None, acquisitionDateData = Some(AcquisitionDateModel(10, 5, 1972)))
      val result = target.getBackLink

      await(result) shouldBe controllers.routes.WorthBeforeLegislationStartController.worthBeforeLegislationStart().url
    }

    "return a link to WorthWhenGifted page with an acquisition date after legislation start and gifted option" in {
      val target = setupTarget(None,
        acquisitionDateData = Some(AcquisitionDateModel(10, 5, 2000)),
        howBecameOwnerData = Some(HowBecameOwnerModel("Gifted")))
      val result = target.getBackLink

      await(result) shouldBe controllers.routes.WorthWhenGiftedToController.worthWhenGiftedTo().url
    }

    "return a link to WorthWhenInherited when property was inherited" in {
      val target = setupTarget(None, howBecameOwnerData = Some(HowBecameOwnerModel("Inherited")))
      val result = target.getBackLink

      await(result) shouldBe controllers.routes.WorthWhenInheritedController.worthWhenInherited().url
    }

    "return a link to BoughtForLess when property was bought for less" in {
      val target = setupTarget(None, howBecameOwnerData = Some(HowBecameOwnerModel("Bought")), boughtForLessData = Some(BoughtForLessModel(true)))
      val result = target.getBackLink

      await(result) shouldBe controllers.routes.WorthWhenBoughtForLessController.worthWhenBoughtForLess().url
    }

    "return a link to acquisition value when not bought for less" in {
      val target = setupTarget(None, acquisitionDateData = Some(AcquisitionDateModel(1, 1, 2016)),
        howBecameOwnerData = Some(HowBecameOwnerModel("Bought")),
        boughtForLessData = Some(BoughtForLessModel(false)))
      val result = target.getBackLink

      await(result) shouldBe controllers.routes.AcquisitionValueController.acquisitionValue().url
    }
  }

  "Calling the .acquisitionCosts action " should {

    "not supplied with a pre-existing stored model" should {
      val target = setupTarget(None, acquisitionDateData = Some(AcquisitionDateModel(1, 1, 2016)), Some(HowBecameOwnerModel("Gifted")))
      lazy val result = target.acquisitionCosts(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the acquisitionCosts page" in {
        document.title shouldBe messages.question
      }

      "have a back link to the WorthWhenGiftedTo page" in {
        document.select("#back-link").attr("href") shouldBe controllers.routes.WorthWhenGiftedToController.worthWhenGiftedTo().url
      }
    }

    "supplied with a pre-existing stored model" should {
      val testAcquisitionCostsModel = new AcquisitionCostsModel(1000)
      val target = setupTarget(Some(testAcquisitionCostsModel))
      lazy val result = target.acquisitionCosts(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the acquisitionCosts page" in {
        document.title shouldBe messages.question
      }
    }

    "without a valid session" should {
      val target = setupTarget(None)
      lazy val result = target.acquisitionCosts(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling the .submitAcquisitionCosts action" when {

    "supplied with an acquisition date after the tax start" should{
      val target = setupTarget(None, acquisitionDateData = Some(AcquisitionDateModel(10, 5, 2016)))
      lazy val request = fakeRequestToPOSTWithSession(("acquisitionCosts", "1000"))
      lazy val result = target.submitAcquisitionCosts(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to '${controllers.routes.ImprovementsController.improvements().url}'" in {
        redirectLocation(result).get shouldBe controllers.routes.ImprovementsController.improvements().url
      }
    }

    "supplied with an acquisition date before the tax start" should {
      val target = setupTarget(None, acquisitionDateData = Some(AcquisitionDateModel(10, 5, 2000)))
      lazy val request = fakeRequestToPOSTWithSession(("acquisitionCosts", "1000"))
      lazy val result = target.submitAcquisitionCosts(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to '${controllers.routes.RebasedValueController.rebasedValue().url}'" in {
        redirectLocation(result).get shouldBe controllers.routes.RebasedValueController.rebasedValue().url
      }
    }

    "supplied with an invalid form" should {
      val target = setupTarget(None, acquisitionDateData = Some(AcquisitionDateModel(1, 1, 2016)), Some(HowBecameOwnerModel("Inherited")))
      lazy val request = fakeRequestToPOSTWithSession(("acquisitionCosts", "a"))
      lazy val result = target.submitAcquisitionCosts(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "return to the acquisition costs page" in {
        document.title shouldBe messages.question
      }

      "have a back link to the WorthWhenInherited page" in {
        document.select("#back-link").attr("href") shouldBe controllers.routes.WorthWhenInheritedController.worthWhenInherited().url
      }
    }
  }
}
