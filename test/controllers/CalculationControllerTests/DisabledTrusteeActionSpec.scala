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

import assets.MessageLookup.NonResident.{DisabledTrustee => messages}
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.jsoup._
import org.scalatest.mock.MockitoSugar
import scala.concurrent.Future
import controllers.nonresident.DisabledTrusteeController
import models.nonresident.DisabledTrusteeModel

class DisabledTrusteeActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[DisabledTrusteeModel]): DisabledTrusteeController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[DisabledTrusteeModel](Matchers.anyString())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    new DisabledTrusteeController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "DisabledTrusteeController" should {
    s"have a session timeout home link of '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
      DisabledTrusteeController.homeLink shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
    }
  }

  // GET Tests
  "Calling the CalculationController.disabledTrustee" when {

    "not supplied with a pre-existing stored model" should {
      val target = setupTarget(None)
      lazy val result = target.disabledTrustee(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "have the title 'How much of your Capital Gains Tax allowance have you got left?'" in {
        document.title shouldEqual messages.question
      }
    }

    "supplied with a pre-existing stored model" should {
      val target = setupTarget(Some(DisabledTrusteeModel("Yes")))
      lazy val result = target.disabledTrustee(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "have the title 'How much of your Capital Gains Tax allowance have you got left?'" in {
        document.title shouldEqual messages.question
      }
    }

    "not supplied with a valid session" should {
      val target = setupTarget(None)
      lazy val result = target.disabledTrustee(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  // POST Tests
  "In CalculationController calling the .submitDisabledTrustee action" when {

    "submitting a valid form" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("isVulnerable", "Yes"))
      lazy val result = target.submitDisabledTrustee(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the Other Properties page" in {
        redirectLocation(result) shouldBe Some(controllers.nonresident.routes.OtherPropertiesController.otherProperties().url)
      }
    }

    "submitting an invalid form" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("isVulnerable", "a"))
      lazy val result = target.submitDisabledTrustee(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "return to the Disabled Trustee page" in {
        document.title shouldEqual messages.question
      }
    }
  }
}
