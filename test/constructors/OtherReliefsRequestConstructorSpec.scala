/*
 * Copyright 2022 HM Revenue & Customs
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

package constructors

import common.CommonPlaySpec
import models.{AllOtherReliefsModel, OtherReliefsModel}

class OtherReliefsRequestConstructorSpec extends CommonPlaySpec {

  val allReliefs = AllOtherReliefsModel(
    Some(OtherReliefsModel(1)),
    Some(OtherReliefsModel(2)),
    Some(OtherReliefsModel(3))
  )

  "Calling .otherReliefsQuery" should {

    "when called with an empty model" in {
      OtherReliefsRequestConstructor.otherReliefsQuery(None) shouldEqual ""
    }

    "when called with some data" in {
      OtherReliefsRequestConstructor.otherReliefsQuery(Some(allReliefs)).equals("") shouldEqual false
    }
  }

  "Calling .flatReliefsQuery" should {

    "when called with an empty model" in {
      OtherReliefsRequestConstructor.flatReliefsQuery(None) shouldEqual ""
    }

    "when called with some data" in {
      OtherReliefsRequestConstructor.flatReliefsQuery(allReliefs.otherReliefsFlat) shouldEqual "&otherReliefsFlat=1"
    }
  }

  "Calling .rebasedReliefsQuery" should {

    "when called with an empty model" in {
      OtherReliefsRequestConstructor.rebasedReliefsQuery(None) shouldEqual ""
    }

    "when called with some data" in {
      OtherReliefsRequestConstructor.rebasedReliefsQuery(allReliefs.otherReliefsRebased) shouldEqual "&otherReliefsRebased=2"
    }
  }

  "Calling .timeApportionedReliefsQuery" should {

    "when called with an empty model" in {
      OtherReliefsRequestConstructor.timeApportionedReliefsQuery(None) shouldEqual ""
    }

    "when called with some data" in {
      OtherReliefsRequestConstructor.timeApportionedReliefsQuery(allReliefs.otherReliefsTime) shouldEqual "&otherReliefsTimeApportioned=3"
    }
  }
}
