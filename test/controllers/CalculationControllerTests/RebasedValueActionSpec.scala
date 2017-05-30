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

import assets.MessageLookup.NonResident.{RebasedValue => messages}
import common.KeystoreKeys.{NonResidentKeys => KeystoreKeys}
import connectors.CalculatorConnector
import controllers.RebasedValueController
import controllers.helpers.FakeRequestHelper
import controllers.routes
import models.{AcquisitionDateModel, RebasedValueModel}
import org.jsoup._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class RebasedValueActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[RebasedValueModel],
                  acquisitionDateModel: Option[AcquisitionDateModel]
                 ): RebasedValueController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[RebasedValueModel](
      ArgumentMatchers.eq(KeystoreKeys.rebasedValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionDateModel](
      ArgumentMatchers.eq(KeystoreKeys.acquisitionDate))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(acquisitionDateModel))

    when(mockCalcConnector.saveFormData[RebasedValueModel](
      ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new RebasedValueController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  //GET tests
  "In CalculationController calling the .rebasedValue action " when {

    "no session is active" should {

      val target = setupTarget(None, Some(AcquisitionDateModel(1, 1, 2016)))
      lazy val result = target.rebasedValue(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }

    "not supplied with a pre-existing stored model and an acquisition date before 6/4/2015 (mandatory rebased value view)" should {

      val target = setupTarget(None, Some(AcquisitionDateModel(5, 4, 2015)))
      lazy val result = target.rebasedValue(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"route to the mandatory rebased value view with the question ${messages.question}" in {
        document.title shouldEqual messages.question
      }
    }
  }
  //POST Tests
  "In CalculationController calling the .submitRebasedValue action with no acquisition date" when {

    "with an acquisition date before 6/4/2015 (mandatory rebased value view)" should {

      lazy val target = setupTarget(None, Some(AcquisitionDateModel(5, 4, 2015)))

      "submitting a valid form with Yes" should {

        lazy val request = fakeRequestToPOSTWithSession(("rebasedValueAmt", "100"))
        lazy val result = target.submitRebasedValue(request)

        "return a 303" in {
          status(result) shouldBe 303
        }

        "redirect to the rebased costs page" in {
          redirectLocation(result).get shouldEqual routes.RebasedCostsController.rebasedCosts().url
        }
      }

      "submitting an invalid form" should {

        lazy val request = fakeRequestToPOSTWithSession(("rebasedValueAmt", ""))
        lazy val result = target.submitRebasedValue(request)
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 400" in {
          status(result) shouldBe 400
        }

        s"return to the mandatory rebased value page that does NOT have a paragraph with the text ${messages.questionOptionalText}" in {
          document.select("""article > p[class=""]""").text shouldEqual ""
        }
      }
    }
  }
}
