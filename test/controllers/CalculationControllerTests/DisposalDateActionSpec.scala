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

import assets.MessageLookup.NonResident.{DisposalDate => messages}
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.nonresident.{DisposalDateController, routes}
import models.nonresident.DisposalDateModel
import org.jsoup._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class DisposalDateActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[DisposalDateModel]
                 ): DisposalDateController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[DisposalDateModel](Matchers.anyString())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    new DisposalDateController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "DisposalDateController" should {
    s"have a session timeout home link of '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
      DisposalDateController.homeLink shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
    }
  }

  // GET Tests
  "Calling the CalculationController.disposalDate" when {

    "not supplied with a pre-existing stored model" should {

      lazy val target = setupTarget(None)
      lazy val result = target.disposalDate(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "should return to the disposal date page" in {
        document.title shouldEqual messages.question
      }
    }

    "supplied with a pre-existing stored model" should {

      lazy val target = setupTarget(Some(DisposalDateModel(1, 3, 2016)))
      lazy val result = target.disposalDate(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "should return to the disposal date page" in {
        document.title shouldEqual messages.question
      }
    }

    "supplied with a pre-existing stored model without a session" should {
      lazy val target = setupTarget(Some(DisposalDateModel(1, 3, 2016)))
      lazy val result = target.disposalDate(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "should return to the disposal date page" in {
        document.title shouldEqual messages.question
      }
    }
  }

  // POST Tests
  "In CalculationController calling the .submitDisposalDate action" when {

    "submitting a valid date 31/01/2016" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("disposalDateDay", "31"), ("disposalDateMonth", "1"), ("disposalDateYear", "2016"))
      lazy val result = target.submitDisposalDate(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.SoldOrGivenAwayController.soldOrGivenAway()}" in {
        redirectLocation(result) shouldBe Some(s"${routes.SoldOrGivenAwayController.soldOrGivenAway()}")
      }
    }

    "submitting a valid leap year date 29/02/2016" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("disposalDateDay", "29"), ("disposalDateMonth", "2"), ("disposalDateYear", "2016"))
      lazy val result = target.submitDisposalDate(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.SoldOrGivenAwayController.soldOrGivenAway()}" in {
        redirectLocation(result) shouldBe Some(s"${routes.SoldOrGivenAwayController.soldOrGivenAway()}")
      }
    }

    "submitting a valid date of 20/02/2014 before the tax start date" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("disposalDateDay", "20"), ("disposalDateMonth", "2"), ("disposalDateYear", "2014"))
      lazy val result = target.submitDisposalDate(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.NoCapitalGainsTaxController.noCapitalGainsTax()}" in {
        redirectLocation(result) shouldBe Some(s"${routes.NoCapitalGainsTaxController.noCapitalGainsTax()}")
      }
    }
  }
}
