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

import assets.MessageLookup.NonResident.{DisposalCosts => messages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import models._
import connectors.CalculatorConnector
import controllers.DisposalCostsController
import controllers.helpers.FakeRequestHelper
import controllers.routes
import org.jsoup._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

class DisposalCostsActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[DisposalCostsModel],
                  soldOrGivenModel: Option[SoldOrGivenAwayModel],
                  soldForLessModel: Option[SoldForLessModel]): DisposalCostsController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[DisposalCostsModel](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[SoldOrGivenAwayModel](
      ArgumentMatchers.eq(KeystoreKeys.soldOrGivenAway))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(soldOrGivenModel))

    when(mockCalcConnector.fetchAndGetFormData[SoldForLessModel](ArgumentMatchers.eq(KeystoreKeys.soldForLess))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(soldForLessModel))

    when(mockCalcConnector.saveFormData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map.empty)))

    new DisposalCostsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  //GET Tests
  "In CalculationController calling the .disposalCosts action " should {

    "not supplied with a pre-existing stored model" should {

      val target = setupTarget(None, None, None)
      lazy val result = target.disposalCosts(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"have the title ${messages.question}" in {
        document.getElementsByTag("title").text shouldBe messages.question
      }

      "have a back link ot the missing data route" in {
        document.select("#back-link").attr("href") shouldEqual common.DefaultRoutes.missingDataRoute
      }
    }

    "supplied with a pre-existing stored model" should {

      val target = setupTarget(Some(DisposalCostsModel(1000)), None, None)
      lazy val result = target.disposalCosts(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"have the title ${messages.question}" in {
        document.getElementsByTag("title").text shouldBe messages.question
      }
    }

    "supplied with an invalid session" should {
      val target = setupTarget(Some(DisposalCostsModel(1000)), None, None)
      lazy val result = target.disposalCosts(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }

    "when the property was given away" should {
      val target = setupTarget(None, Some(SoldOrGivenAwayModel(false)), Some(SoldForLessModel(true)))
      lazy val result = target.disposalCosts(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"have the title ${messages.question}" in {
        document.getElementsByTag("title").text shouldBe messages.question
      }

      "have a back link to the market value controller" in {
        document.select("#back-link").attr("href") shouldEqual routes.MarketValueWhenSoldOrGaveAwayController.marketValueWhenGaveAway().url
      }
    }

    "when the property was sold and sold for less" should {
      val target = setupTarget(None, Some(SoldOrGivenAwayModel(true)), Some(SoldForLessModel(true)))
      lazy val result = target.disposalCosts(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"have the title ${messages.question}" in {
        document.getElementsByTag("title").text shouldBe messages.question
      }

      "have a back link to the market value controller" in {
        document.select("#back-link").attr("href") shouldEqual routes.MarketValueWhenSoldOrGaveAwayController.marketValueWhenSold().url
      }
    }

    "when the property was sold and not sold for less" should {
      val target = setupTarget(None, Some(SoldOrGivenAwayModel(true)), Some(SoldForLessModel(false)))
      lazy val result = target.disposalCosts(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"have the title ${messages.question}" in {
        document.getElementsByTag("title").text shouldBe messages.question
      }

      "have a back link to the market value controller" in {
        document.select("#back-link").attr("href") shouldEqual routes.DisposalValueController.disposalValue().url
      }
    }
  }

  //POST Tests
  "In CalculationController calling the .submitDisposalCosts action" when {

    "submitting a valid form with 1000" should {
      val target = setupTarget(None, None, None)
      lazy val request = fakeRequestToPOSTWithSession(("disposalCosts", "1000"))
      lazy val result = target.submitDisposalCosts(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.AcquisitionDateController.acquisitionDate()}" in {
        redirectLocation(result) shouldBe Some(s"${routes.AcquisitionDateController.acquisitionDate()}")
      }
    }

    "submitting an invalid form with no value" should {
      val target = setupTarget(None, None, None)
      lazy val request = fakeRequestToPOSTWithSession(("disposalCosts", ""))
      lazy val result = target.submitDisposalCosts(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "return to the disposal costs page" in {
        document.title shouldEqual messages.question
      }
    }
  }
}
