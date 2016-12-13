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

import assets.MessageLookup.NonResident.{HowBecameOwner => messages}
import common.KeystoreKeys
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.nonresident.HowBecameOwnerController
import models.nonresident.{AcquisitionDateModel, HowBecameOwnerModel, RebasedValueModel}
import org.jsoup._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.openqa.selenium.Keys
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class HowBecameOwnerActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget(getData: Option[HowBecameOwnerModel],
                  acquisitionDateModel: AcquisitionDateModel,
                  rebasedValueModel: RebasedValueModel): HowBecameOwnerController = {

    val mockConnector = mock[CalculatorConnector]

    when(mockConnector.fetchAndGetFormData[HowBecameOwnerModel](Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(getData)

    when(mockConnector.saveFormData[HowBecameOwnerModel](Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(mock[CacheMap])

    when(mockConnector.fetchAndGetFormData[AcquisitionDateModel](Matchers.eq(KeystoreKeys.acquisitionDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Some(acquisitionDateModel))

    when(mockConnector.fetchAndGetFormData[RebasedValueModel](Matchers.eq(KeystoreKeys.rebasedValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Some(rebasedValueModel))

    new HowBecameOwnerController {
      override val calcConnector: CalculatorConnector = mockConnector
    }
  }

  "HowBecameOwnerController" should {
    s"have a session timeout home link of '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
      HowBecameOwnerController.homeLink shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
    }
  }

  "Calling .howBecameOwner action" when {

    "provided with a valid session while valid for prr" should {
      lazy val target = setupTarget(None, AcquisitionDateModel("Yes", Some(1), Some(1), Some(1990)), RebasedValueModel(Some(100)))
      lazy val result = target.howBecameOwner(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.question}" in {
        doc.title shouldEqual messages.question
      }
    }

    "provided with a valid session with stored data and is not valid for prr" should {
      lazy val target = setupTarget(Some(HowBecameOwnerModel("Bought")), AcquisitionDateModel("No", None, None, None), RebasedValueModel(None))
      lazy val result = target.howBecameOwner(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.question}" in {
        doc.title shouldEqual messages.question
      }
    }

    "provided with an invalid session" should {
      lazy val target = setupTarget(None, AcquisitionDateModel("No", None, None, None), RebasedValueModel(None))
      lazy val result = target.howBecameOwner(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling .submitHowBecameOwner action" when {
    "a valid form with the answer 'Bought' is submitted" should {
      lazy val target = setupTarget(None, AcquisitionDateModel("No", None, None, None), RebasedValueModel(None))
      lazy val result = target.submitHowBecameOwner(fakeRequestToPOSTWithSession(("gainedBy", "Bought")))

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the bought-for-less-than-worth page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/non-resident/bought-for-less")
      }
    }
  }

  "a valid form with the answer 'Inherited' is submitted" should {
    lazy val target = setupTarget(None, AcquisitionDateModel("No", None, None, None), RebasedValueModel(None))
    lazy val result = target.submitHowBecameOwner(fakeRequestToPOSTWithSession(("gainedBy", "Inherited")))

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "redirect to the worth-when-inherited page" in {
      redirectLocation(result).get should include ("/calculate-your-capital-gains/non-resident/worth-when-inherited")
    }
  }


  "a valid form with the answer 'Gifted' is submitted" should {
    lazy val target = setupTarget(None, AcquisitionDateModel("No", None, None, None), RebasedValueModel(None))
    lazy val result = target.submitHowBecameOwner(fakeRequestToPOSTWithSession(("gainedBy", "Gifted")))

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "redirect to the worth-when-gifted page" in {
      redirectLocation(result).get should include ("/calculate-your-capital-gains/non-resident/worth-when-gifted")
    }
  }


  "an invalid form with no answer is submitted" should {
    lazy val target = setupTarget(None, AcquisitionDateModel("No", None, None, None), RebasedValueModel(None))
    lazy val result = target.submitHowBecameOwner(fakeRequestToPOSTWithSession(("gainedBy", "")))
    lazy val doc = Jsoup.parse(bodyOf(result))

    "return a status of 400" in {
      status(result) shouldBe 400
    }

    "return to the page" in {
      doc.title shouldEqual messages.question
    }

    "raise an error on the page" in {
      doc.body.select("#gainedBy-error-summary").size shouldBe 1
    }
  }
}
