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

package common

import common.nonresident.CustomerTypeKeys
import uk.gov.hmrc.play.test.UnitSpec

class CustomerTypeKeysSpec extends UnitSpec {
  "CustomerTypeKeys" should {
    "return individual" in {
      CustomerTypeKeys.individual shouldBe "individual"
    }

    "return trustee" in {
      CustomerTypeKeys.trustee shouldBe "trustee"
    }

    "return personalRep" in {
      CustomerTypeKeys.personalRep shouldBe "personalRep"
    }
  }
}
