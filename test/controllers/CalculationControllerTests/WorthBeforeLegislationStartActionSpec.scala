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

import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.nonresident.WorthBeforeLegislationStartController
import models.nonresident.WorthBeforeLegislationStartModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.http.HeaderCarrier
import play.api.test.Helpers._
import assets.MessageLookup.NonResident.{WorthBeforeLegislationStart => messages}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class WorthBeforeLegislationStartActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper{

  implicit val hc = new HeaderCarrier()

  def setUpTarget(getData: Option[WorthBeforeLegislationStartModel]): WorthBeforeLegislationStartController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[WorthBeforeLegislationStartModel](Matchers.anyString())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    new WorthBeforeLegislationStartController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "WorthBeforeLegislationStartController" when{

    s"have a session timeout home link of '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
      WorthBeforeLegislationStartController.homeLink shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
    }

    "calling .worthBeforeLegislationStart" should {


      "request has a valid session and no keystore value" should {

        lazy val target = setUpTarget(None)
        lazy val result = target.worthBeforeLegislationStart(fakeRequestWithSession)

        "return a status of 200" in {
          status(result) shouldBe 200
        }

        s"return some html with title of ${messages.question}" in {
          contentType(result) shouldBe Some("text/html")
          Jsoup.parse(bodyOf(result)).title shouldEqual messages.question
        }
      }

      "request has a valid session and some keystore value" should {

        lazy val target = setUpTarget(Some(WorthBeforeLegislationStartModel(BigDecimal(1000.00))))
        lazy val result = target.worthBeforeLegislationStart(fakeRequestWithSession)

        "return a status of 200" in {
          status(result) shouldBe 200
        }

        s"return some html with title of ${messages.question}" in {
          contentType(result) shouldBe Some("text/html")
          Jsoup.parse(bodyOf(result)).title shouldEqual messages.question
        }
      }

      "request has an invalid session" should {

        lazy val target = setUpTarget(None)
        lazy val result = target.worthBeforeLegislationStart(fakeRequest)

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        "return you to the session timeout page" in {
          redirectLocation(result).get should include ("/calculate-your-capital-gains/non-resident/session-timeout")
        }
      }
    }

    "Calling .submitWorthBeforeLegislationStart" should {

      "with valid form with the answer '1000.00'" should {

        lazy val target = setUpTarget(None)
        lazy val request = fakeRequestToPOSTWithSession(("worthBeforeLegislationStart", "1000.00"))
        lazy val result = target.submitWorthBeforeLegislationStart(request)

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        "redirect to the acquisition costs page" in {
          redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/non-resident/acquisition-costs")
        }
      }

      "with an invalid form with the answer 'a'" should {

        lazy val target = setUpTarget(None)
        lazy val request = fakeRequestToPOSTWithSession(("worthBeforeLegislationStart", "a"))
        lazy val result = target.submitWorthBeforeLegislationStart(request)
        lazy val doc = Jsoup.parse(bodyOf(result))

        "return a status of 400" in {
          status(result) shouldBe 400
        }

        "return to the Worth Before Legislation Start page" in {
          doc.title() shouldEqual messages.question
        }
      }
    }
  }

}
