/*
 * Copyright 2019 HM Revenue & Customs
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

import assets.MessageLookup
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import models.WhoDidYouGiveItToModel
import org.mockito.ArgumentMatchers
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import common.KeystoreKeys.{NonResidentKeys => keystoreKeys}
import config.AppConfig
import controllers.WhoDidYouGiveItToController
import org.jsoup.Jsoup
import org.mockito.Mockito._
import play.api.test.Helpers._
import assets.MessageLookup.{NoTaxToPay => messages}
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future


class WhoDidYouGiveItToControllerSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget(getData: Option[WhoDidYouGiveItToModel]): WhoDidYouGiveItToController = {
    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[WhoDidYouGiveItToModel](ArgumentMatchers.eq(keystoreKeys.whoDidYouGiveItTo))
    (ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map.empty)))

    new WhoDidYouGiveItToController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      val config: AppConfig = mock[AppConfig]
    }
  }


  "Calling .whoDidYouGiveItTo from the GainsController" when {
    "there is no keystore data" should {
      lazy val target = setupTarget(None)
      lazy val result = target.whoDidYouGiveItTo(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldEqual 200
      }

      s"return some html with title of ${MessageLookup.WhoDidYouGiveItTo.title}" in {
        Jsoup.parse(bodyOf(result)).select("h1").text shouldEqual MessageLookup.WhoDidYouGiveItTo.title
      }
    }

    "there is keystore data" should {
      lazy val target = setupTarget(Some(WhoDidYouGiveItToModel("Charity")))
      lazy val result = target.whoDidYouGiveItTo(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldEqual 200
      }

      s"return some html with title of ${MessageLookup.WhoDidYouGiveItTo.title}" in {
        Jsoup.parse(bodyOf(result)).select("h1").text shouldEqual MessageLookup.WhoDidYouGiveItTo.title
      }
    }
  }

  "Calling .submitWhoDidYouGiveItTo with a Charity value" should {
      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("whoDidYouGiveItTo","Charity"))
      lazy val result = target.submitWhoDidYouGiveItTo(request)

      "when supplied with a valid form" which {
        "redirects" in {
          status(result) shouldEqual 303
        }

        "to the You Have No Tax To Pay page" in {
          redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/non-resident/no-tax-to-pay")
        }
      }
  }

  "Calling .submitWhoDidYouGiveItTo from the GainController with a Spouse value" should {
    lazy val target = setupTarget(None)
    lazy val request = fakeRequestToPOSTWithSession(("whoDidYouGiveItTo", "Spouse"))
    lazy val result = target.submitWhoDidYouGiveItTo(request)

    "when supplied with a valid form" which {
      "redirects" in {
        status(result) shouldEqual 303
      }

      "to the You Have No Tax To Pay page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/non-resident/no-tax-to-pay")
      }
    }
  }

  "Calling .submitWhoDidYouGiveItTo with a Someone Else value" should {
    lazy val target = setupTarget(None)
    lazy val request = fakeRequestToPOSTWithSession(("whoDidYouGiveItTo", "Other"))
    lazy val result = target.submitWhoDidYouGiveItTo(request)

    "when supplied with a valid form" which {
      "redirect" in {
        status(result) shouldEqual 303
      }

      "to the page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/non-resident/market-value-when-gave-away")
      }
    }
  }

  "Calling .noTaxToPay" when {
    "A valid session is provided when gifted to charity" should {
      lazy val target = setupTarget(Some(WhoDidYouGiveItToModel("Charity")))
      lazy val result = target.noTaxToPay(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title}" in {
        doc.title shouldEqual messages.title
      }

      "have text explaining why tax is not owed" in {
        doc.body().select("article p").text() shouldBe messages.charityText
      }

      "A valid session is provided when gifted to a spouse" should {
        lazy val target = setupTarget(Some(WhoDidYouGiveItToModel("Spouse")))
        lazy val result = target.noTaxToPay(fakeRequestWithSession)
        lazy val doc = Jsoup.parse(bodyOf(result))

        "return a status of 200" in {
          status(result) shouldBe 200
        }

        s"return some html with title of ${messages.title}" in {
          doc.title shouldEqual messages.title
        }

        "have text explaining why tax is not owed" in {
          doc.body().select("article p").text() shouldBe messages.spouseText
        }
      }

      "An invalid session is provided" should {
        lazy val target = setupTarget(Some(WhoDidYouGiveItToModel("Other")))
        lazy val result = target.noTaxToPay(fakeRequest)
        lazy val doc = Jsoup.parse(bodyOf(result))

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        "return you to the session timeout page" in {
          redirectLocation(result).get should include ("/calculate-your-capital-gains/non-resident/session-timeout")
        }
      }
    }
  }

}
