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

import assets.MessageLookup
import controllers.WhatNextController
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class WhatNextControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {
  implicit val hc: HeaderCarrier = mock[HeaderCarrier]

  "Calling .whatNext" when {
    lazy val target = new WhatNextController{}
    "provided with a valid request" should {
      lazy val result = target.whatNext(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the whatNext page" in {
        document.title() shouldBe MessageLookup.WhatNext.title
      }
    }

    "provided with no valid session" should {
      lazy val result = target.whatNext(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect the user to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

}
