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

import assets.MessageLookup.NonResident.{RebasedCosts => messages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import connectors.CalculatorConnector
import controllers.RebasedCostsController
import controllers.helpers.FakeRequestHelper
import models.RebasedCostsModel
import org.jsoup._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class RebasedCostsActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[RebasedCostsModel]): RebasedCostsController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[RebasedCostsModel](
      ArgumentMatchers.eq(KeystoreKeys.rebasedCosts))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    new RebasedCostsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "RebasedCostsController" should {
    s"have a session timeout home link of '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
      RebasedCostsController.homeLink shouldEqual controllers.routes.DisposalDateController.disposalDate().url
    }
  }

  // GET Tests
  "Calling the CalculationController.rebasedCosts" when {

    "not supplied with a pre-existing stored model" should {
      val target = setupTarget(None)
      lazy val result = target.rebasedCosts(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the rebased costs page" in {
        document.title() shouldBe messages.question
      }

      "a previous value is supplied with a pre-existing stored model" should {
        val target = setupTarget(Some(RebasedCostsModel("Yes", Some(1500))))
        lazy val result = target.rebasedCosts(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 200" in {
          status(result) shouldBe 200
        }

        "load the rebased costs page" in {
          document.title() shouldBe messages.question
        }
      }

      "a request is made without a session" should {
        val target = setupTarget(Some(RebasedCostsModel("Yes", Some(1500))))
        lazy val result = target.rebasedCosts(fakeRequest)

        "return a 303" in {
          status(result) shouldBe 303
        }

        "redirect to the session timeout page" in {
          redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
        }
      }
    }
  }

  // POST Tests
  "Calling the .submitRebasedCosts action" when {

    "a valid form is submitted" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession("hasRebasedCosts" -> "Yes", "rebasedCosts" -> "1000")
      lazy val result = target.submitRebasedCosts(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the improvements page" in {
        redirectLocation(result).get shouldBe controllers.routes.ImprovementsController.improvements().url
      }
    }

    "an invalid form is submitted" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession("hasRebasedCosts" -> "Yes", "rebasedCosts" -> "")
      lazy val result = target.submitRebasedCosts(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "return to the rebased costs page" in {
        document.title() shouldBe messages.question
      }
    }
  }
}
