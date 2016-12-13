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

import assets.MessageLookup.{NonResident => messages}
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.nonresident.BoughtForLessController
import models.nonresident.BoughtForLessModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class BoughtForLessActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()
  def setupTarget(getData: Option[BoughtForLessModel]): BoughtForLessController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[BoughtForLessModel](Matchers.anyString())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData[BoughtForLessModel](Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new BoughtForLessController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "BoughtForLessController" should {
    s"have a session timeout home link of '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
      BoughtForLessController.homeLink shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
    }
  }

  "Calling .boughtForLess" when {

    "provided with no previous data" should {
      val target = setupTarget(None)
      lazy val result = target.boughtForLess(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the bought for less page" in {
        document.title() shouldBe messages.BoughtForLess.question
      }
    }

    "provided with some previous data" should {
      val target = setupTarget(Some(BoughtForLessModel(true)))
      lazy val result = target.boughtForLess(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the bought for less page" in {
        document.title() shouldBe messages.BoughtForLess.question
      }
    }

    "provided without a valid session" should {
      val target = setupTarget(None)
      lazy val result = target.boughtForLess(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect the user to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling .submitBoughtForLess" when {

    "submitting a valid form with an answer of Yes" should {
      lazy val request = fakeRequestToPOSTWithSession(("boughtForLess", "Yes"))
      lazy val target = setupTarget(None)
      lazy val result = target.submitBoughtForLess(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the worthWhenBoughtForLess page" in {
        redirectLocation(result) shouldBe Some(controllers.nonresident.routes.WorthWhenBoughtForLessController.worthWhenBoughtForLess().url)
      }
    }

    "submitting a valid form with an answer of No" should {
      lazy val request = fakeRequestToPOSTWithSession(("boughtForLess", "No"))
      lazy val target = setupTarget(None)
      lazy val result = target.submitBoughtForLess(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the acquisitionValue page" in {
        redirectLocation(result) shouldBe Some(controllers.nonresident.routes.AcquisitionValueController.acquisitionValue().url)
      }
    }

    "submitting an invalid form" should {
      lazy val request = fakeRequestToPOSTWithSession(("boughtForLess", ""))
      lazy val target = setupTarget(None)
      lazy val result = target.submitBoughtForLess(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "return to the Bought For Less page" in {
        document.title() shouldBe messages.BoughtForLess.question
      }
    }
  }
}
