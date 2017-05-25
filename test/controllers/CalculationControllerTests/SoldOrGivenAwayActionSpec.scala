/*
 * Copyright 2017 HM Revenue & Customs
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

import assets.MessageLookup.NonResident.{SoldOrGivenAway => messages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import connectors.CalculatorConnector
import controllers.SoldOrGivenAwayController
import controllers.helpers.FakeRequestHelper
import models.SoldOrGivenAwayModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class SoldOrGivenAwayActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setUpTarget(getData: Option[SoldOrGivenAwayModel]): SoldOrGivenAwayController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[SoldOrGivenAwayModel](
      ArgumentMatchers.eq(KeystoreKeys.soldOrGivenAway))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    new SoldOrGivenAwayController  {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  //GET Tests
  "Calling the SellOrGiveAway .sellOrGiveAway" when {

    "not supplied with a pre-existing model" should {
      val target = setUpTarget(None)
      lazy val result = target.soldOrGivenAway(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200 response" in {
        status(result) shouldBe 200
      }

      s"have the title of ${messages.question}" in {
        document.title() shouldBe messages.question
      }
    }

    "supplied with a pre-existing model" should {
      val target = setUpTarget(Some(SoldOrGivenAwayModel(true)))
      lazy val result = target.soldOrGivenAway(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200 response" in {
        status(result) shouldBe 200
      }

      s"have the title of ${messages.question}" in {
        document.title() shouldBe messages.question
      }
    }

    "not supplied with a valid session" should {
      val target = setUpTarget(Some(SoldOrGivenAwayModel(true)))
      lazy val result = target.soldOrGivenAway(fakeRequest)

      "return a 303 response" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  //POST Tests
  "Calling the SoldOrGivenAway .submitSoldOrGivenAway" when {

    "a valid form is submitted with Yes" should {
      val target = setUpTarget(None)
      lazy val request = fakeRequestToPOSTWithSession("soldIt" -> "Yes")
      lazy val result = target.submitSoldOrGivenAway(request)

      "return a 303 response" in {
        status(result) shouldBe 303
      }

      "redirect to the Sold For Less Page" in{
        redirectLocation(result).get shouldBe controllers.routes.SoldForLessController.soldForLess().url
      }
    }

    "a valid form is submitted with No" should {
      val target = setUpTarget(None)
      lazy val request = fakeRequestToPOSTWithSession("soldIt" -> "No")
      lazy val result = target.submitSoldOrGivenAway(request)

      "return a 303 response" in {
        status(result) shouldBe 303
      }

      "redirect to the Who Did You Give This To Page" in{
        redirectLocation(result).get shouldBe controllers.routes.WhoDidYouGiveItToController.whoDidYouGiveItTo().url
      }
    }

    "an invalid form is submitted with no data" should {
      val target = setUpTarget(None)
      lazy val request = fakeRequestToPOSTWithSession("soldIt" -> "")
      lazy val result = target.submitSoldOrGivenAway(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400 response" in {
        status(result) shouldBe 400
      }

      "stay on the SoldOrGivenAway page" in {
        document.title shouldBe messages.question
      }
    }
  }
}
