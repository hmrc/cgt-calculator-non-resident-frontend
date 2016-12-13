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

import assets.MessageLookup.NonResident.{AllowableLosses => messages}
import common.{Constants, KeystoreKeys}
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.nonresident.{AllowableLossesController, routes}
import models.nonresident.{AcquisitionDateModel, AllowableLossesModel, RebasedValueModel}
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsFormUrlEncoded, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class AllowableLossesSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  implicit val hc = new HeaderCarrier()

  def setupTarget(
                   getData: Option[AllowableLossesModel],
                   postData: Option[AllowableLossesModel],
                   acquisitionDate: Option[AcquisitionDateModel],
                   rebasedData: Option[RebasedValueModel] = None): AllowableLossesController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[AllowableLossesModel](Matchers.eq(KeystoreKeys.allowableLosses))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionDateModel](Matchers.eq(KeystoreKeys.acquisitionDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(acquisitionDate))

    when(mockCalcConnector.fetchAndGetFormData[RebasedValueModel](Matchers.eq(KeystoreKeys.rebasedValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(rebasedData))

    lazy val data = CacheMap("form-id", Map("data" -> Json.toJson(postData.getOrElse(AllowableLossesModel("No", None)))))
    when(mockCalcConnector.saveFormData[AllowableLossesModel](Matchers.anyString(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(data))

    new AllowableLossesController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "AllowableLossesController" should {
    s"have a session timeout home link of '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
      AllowableLossesController.homeLink shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
    }
  }

  "In CalculationController calling the .allowableLosses action" when {

    "no prior data is supplied" should {

      lazy val controller = setupTarget(None, None, None, None)
      lazy val result = controller.allowableLosses(fakeRequestWithSession)

      "return a 200" in {
        status(result) shouldBe 200
      }

      "with the allowable losses page title" in {
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.yesNoQuestion
      }
    }

    "an allowable loss has already been entered" should {
      lazy val controller = setupTarget(None, None, None, None)
      lazy val result = controller.allowableLosses(fakeRequestWithSession)

      "return a 200" in {
        status(result) shouldBe 200
      }

      "with the allowable losses page title" in {
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.yesNoQuestion
      }
    }
  }

  "In CalculationController calling the .submitAllowableLosses action" when {
    def buildRequest(body: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest("POST",
      "/calculate-your-capital-gains/non-resident/allowable-losses")
      .withSession(SessionKeys.sessionId -> "12345")
      .withFormUrlEncodedBody(body: _*)

    def executeTargetWithMockData(answer: String,
                                  amount: String,
                                  acquisitionDate: AcquisitionDateModel,
                                  rebasedData: Option[RebasedValueModel] = None): Future[Result] = {

      lazy val fakeRequest = buildRequest(("isClaimingAllowableLosses", answer), ("allowableLossesAmt", amount))

      val mockData = amount match {
        case "" => AllowableLossesModel(answer, None)
        case _ => AllowableLossesModel(answer, Some(BigDecimal(amount)))
      }

      val target = setupTarget(None, Some(mockData), Some(acquisitionDate), rebasedData)
      target.submitAllowableLosses(fakeRequest)
    }

    "submitting a valid form with 'Yes' and an amount with no acquisition date" should {
      lazy val result = executeTargetWithMockData("Yes", "1000", AcquisitionDateModel("No", None, None, None))

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the brought forward losses page" in {
        redirectLocation(result) shouldBe Some(s"${routes.OtherReliefsController.otherReliefs()}")
      }
    }

    "submitting a valid form with 'Yes' and an amount with two decimal places with an acquisition date after the tax start date" should {
      lazy val result = executeTargetWithMockData("Yes", "1000.11", AcquisitionDateModel("Yes", Some(10), Some(10), Some(2016)))

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the brought forward losses page" in {
        redirectLocation(result) shouldBe Some(s"${routes.OtherReliefsController.otherReliefs()}")
      }
    }

    "submitting a valid form with 'No' and a null amount with an acquisition date before the tax start date" should {
      lazy val result = executeTargetWithMockData("No", "", AcquisitionDateModel("Yes", Some(9), Some(9), Some(1999)))

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the brought forward losses page" in {
        redirectLocation(result) shouldBe Some(s"${routes.CalculationElectionController.calculationElection()}")
      }
    }

    "submitting a valid form with 'No' and a negative amount with no returned acquisition date model" should {
      lazy val result = executeTargetWithMockData("No", "-1000", AcquisitionDateModel("No", None, None, None))

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the brought forward losses page" in {
        redirectLocation(result) shouldBe Some(s"${routes.OtherReliefsController.otherReliefs()}")
      }
    }

    "submitting a valid form when an acquisition date (before 2015-04-06) has been supplied but no property was not revalued" should {
      val dateBefore = AcquisitionDateModel("Yes", Some(1), Some(4), Some(2015))
      lazy val result = executeTargetWithMockData("No", "", dateBefore)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the calculation election view" in {
        redirectLocation(result) shouldBe Some(s"${routes.CalculationElectionController.calculationElection()}")
      }
    }

    "submitting a valid form when an acquisition date (after 2015-04-06) has been supplied but no property was not revalued" should {
      val dateAfter = AcquisitionDateModel("Yes", Some(1), Some(6), Some(2015))
      lazy val result = executeTargetWithMockData("No", "", dateAfter)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the other reliefs view" in {
        redirectLocation(result) shouldBe Some(s"${routes.OtherReliefsController.otherReliefs()}")
      }
    }

    "submitting a valid form when no acquisition date is supplied and the property was not revalued" should {
      val noDate = AcquisitionDateModel("No", None, None, None)
      lazy val result = executeTargetWithMockData("No", "", noDate)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the other reliefs view" in {
        redirectLocation(result) shouldBe Some(s"${routes.OtherReliefsController.otherReliefs()}")
      }
    }

    "submitting a valid form when no acquisition date is supplied and the property was revalued" should {
      val rebased = RebasedValueModel(Some(BigDecimal(1000)))
      val noDate = AcquisitionDateModel("No", None, None, None)
      lazy val result = executeTargetWithMockData("No", "", noDate, Some(rebased))

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the calculation election view" in {
        redirectLocation(result) shouldBe Some(s"${routes.CalculationElectionController.calculationElection()}")
      }
    }

    "submitting a valid form when an invalid Acquisition Date Model has been supplied and no property was revalued" should {
      val invalidDate = AcquisitionDateModel("invalid", None, None, None)
      lazy val result = executeTargetWithMockData("No", "", invalidDate)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.OtherReliefsController.otherReliefs()}" in {
        redirectLocation(result) shouldBe Some(s"${routes.OtherReliefsController.otherReliefs()}")
      }
    }

    "submitting a value which exceeds the maximum numeric" should {
      lazy val result = executeTargetWithMockData("Yes", (Constants.maxNumeric + 0.01).toString, AcquisitionDateModel("No", None, None, None))

      "return a 400" in {
        status(result) shouldBe 400
      }
    }
  }
}
