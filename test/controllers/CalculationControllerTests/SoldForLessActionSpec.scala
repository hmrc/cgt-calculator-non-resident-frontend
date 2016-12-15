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

import assets.MessageLookup.NonResident.{SoldForLess => messages}
import common.KeystoreKeys.{NonResidentKeys => keyStoreKeys}
import connectors.CalculatorConnector
import controllers.SoldForLessController
import controllers.helpers.FakeRequestHelper
import models.SoldForLessModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class SoldForLessActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget(getData: Option[SoldForLessModel]): SoldForLessController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[SoldForLessModel](Matchers.eq(keyStoreKeys.soldForLess))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData[SoldForLessModel](Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new SoldForLessController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "SoldForLessController" should {
    s"have a session timeout home link of '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
      SoldForLessController.homeLink shouldEqual controllers.routes.DisposalDateController.disposalDate().url
    }
  }

  "Calling .soldForLess from the nonresident SoldForLess" when {

    "request has a valid session and no keystore value" should {

      lazy val target = setupTarget(None)
      lazy val result = target.soldForLess(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.question}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.question
      }
    }

    "request has a valid session and some keystore value" should {

      lazy val target = setupTarget(Some(SoldForLessModel(true)))
      lazy val result = target.soldForLess(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.question}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.question
      }
    }

    "request has an invalid session" should {

      lazy val target = setupTarget(None)
      lazy val result = target.soldForLess(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling .submitSoldForLess from the nonresident SoldForLessController" should {

    "with valid form with the answer 'Yes'" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("soldForLess", "Yes"))
      lazy val result = target.submitSoldForLess(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the market value when sold page" in {
        redirectLocation(result).get shouldBe controllers.routes.MarketValueWhenSoldOrGaveAwayController.marketValueWhenSold().url

      }
    }

    "with valid form with the answer 'No'" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("soldForLess", "No"))
      lazy val result = target.submitSoldForLess(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the disposal value page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/non-resident/disposal-value")
      }
    }

    "with an invalid form with the answer ''" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("soldForLess", ""))
      lazy val result = target.submitSoldForLess(request)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "return to the Sold for Less page" in {
        doc.title() shouldEqual messages.question
      }
    }
  }
}
