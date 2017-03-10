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

import assets.MessageLookup.NonResident.{OtherProperties => messages}
import common.DefaultRoutes._
import common.nonresident.CustomerTypeKeys
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import connectors.CalculatorConnector
import controllers.OtherPropertiesController
import controllers.helpers.FakeRequestHelper
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.jsoup._
import org.scalatest.mock.MockitoSugar

import scala.concurrent.Future
import controllers.routes
import models.{CurrentIncomeModel, CustomerTypeModel, OtherPropertiesModel}
import uk.gov.hmrc.http.cache.client.CacheMap

class OtherPropertiesActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[OtherPropertiesModel], customerTypeData:Option[CustomerTypeModel] = None,
                  currentIncomeData:Option[CurrentIncomeModel] = None): OtherPropertiesController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[OtherPropertiesModel](ArgumentMatchers.anyString())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData[OtherPropertiesModel](ArgumentMatchers.any(), ArgumentMatchers.any()) (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(mock[CacheMap])

    when(mockCalcConnector.fetchAndGetFormData[CustomerTypeModel](
      ArgumentMatchers.eq(KeystoreKeys.customerType))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(customerTypeData))

    when(mockCalcConnector.fetchAndGetFormData[CurrentIncomeModel](
      ArgumentMatchers.eq(KeystoreKeys.currentIncome))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(currentIncomeData))

    new OtherPropertiesController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "OtherPropertiesController" should {
    s"have a session timeout home link of '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
      OtherPropertiesController.homeLink shouldEqual controllers.routes.DisposalDateController.disposalDate().url
    }
  }

  // GET Tests
  "Calling the CalculationController.otherProperties" when {

    "no session is active" should {
      lazy val target = setupTarget(None)
      lazy val result = target.otherProperties(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${controllers.utils.TimeoutController.timeout("restart", "home")}" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }

    "not supplied with a pre-existing stored model" should {

      "for a customer type of Individual" should {

        val target = setupTarget(None, Some(CustomerTypeModel(CustomerTypeKeys.individual)), Some(CurrentIncomeModel(100)))
        lazy val result = target.otherProperties(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 200" in {
          status(result) shouldBe 200
        }

        "display the other properties page" in {
          document.title shouldEqual messages.question
        }

        s"has a 'Back' link to ${routes.PersonalAllowanceController.personalAllowance().url}" in {
          document.body.getElementById("back-link").attr("href").trim() shouldEqual routes.PersonalAllowanceController.personalAllowance().url
        }
      }

      "for a Customer Type of Individual with no Current Income" should {

        val target = setupTarget(None,Some(CustomerTypeModel(CustomerTypeKeys.individual)), Some(CurrentIncomeModel(0)))
        lazy val result = target.otherProperties(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 200" in {
          status(result) shouldBe 200
        }

        "display the other properties page" in {
          document.title shouldEqual messages.question
        }

        s"have a 'Back' link to ${routes.CurrentIncomeController.currentIncome().url}" in {
          document.body.getElementById("back-link").attr("href") shouldEqual routes.CurrentIncomeController.currentIncome().url
        }
      }

      "for a Customer Type of Trustee" should {

        val target = setupTarget(None, Some(CustomerTypeModel(CustomerTypeKeys.trustee)))
        lazy val result = target.otherProperties(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 200" in {
          status(result) shouldBe 200
        }

        "display the other properties page" in {
          document.title shouldEqual messages.question
        }

        s"have a 'Back' link to ${routes.DisabledTrusteeController.disabledTrustee().url}" in {
          document.body.getElementById("back-link").attr("href") shouldEqual routes.DisabledTrusteeController.disabledTrustee().url
        }
      }

      "for a Customer Type of Personal Rep" should {

        val target = setupTarget(None, Some(CustomerTypeModel(CustomerTypeKeys.personalRep)))
        lazy val result = target.otherProperties(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 200" in {
          status(result) shouldBe 200
        }

        "display the other properties page" in {
          document.title shouldEqual messages.question
        }

        s"have a 'Back' link to ${routes.CustomerTypeController.customerType().url}" in {
          document.body.getElementById("back-link").attr("href") shouldEqual routes.CustomerTypeController.customerType().url
        }
      }

      "if no customer type model exists" should {
        val target = setupTarget(None)
        lazy val result = target.otherProperties(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 200" in {
          status(result) shouldBe 200
        }

        "display the other properties page" in {
          document.title shouldEqual messages.question
        }

        s"have a 'Back' link to $missingDataRoute " in {
          document.body.getElementById("back-link").attr("href") shouldEqual missingDataRoute
        }
      }
    }
  }

  // POST Tests
  "In CalculationController calling the .submitOtherProperties action" when {

    "submitting a valid form with 'Yes'" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("otherProperties", "Yes"))
      lazy val result = target.submitOtherProperties(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "should redirect to the previous gain or loss page" in {
        redirectLocation(result) shouldBe Some(s"${routes.PreviousGainOrLossController.previousGainOrLoss()}")
      }
    }


    "submitting a valid form with 'No'" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("otherProperties", "No"))
      lazy val result = target.submitOtherProperties(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "should redirect to the broughtForwardLosses page" in {
        redirectLocation(result) shouldBe Some(s"${routes.BroughtForwardLossesController.broughtForwardLosses()}")
      }
    }

    "submitting an form with no data" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("otherProperties", ""))
      lazy val result = target.submitOtherProperties(request)

      "return a 400" in {
        status(result) shouldBe 400
      }

      "return to the other properties page" in {
        Jsoup.parse(bodyOf(result)).select("title").text shouldEqual messages.question
      }
    }
  }
}
