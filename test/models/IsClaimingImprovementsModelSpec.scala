/*
 * Copyright 2024 HM Revenue & Customs
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

package models

import common.CommonPlaySpec
import play.api.libs.json.Json

class IsClaimingImprovementsModelSpec extends CommonPlaySpec {

  "Post Writes" should {
    "Write data to json" when {
      "IsClaimingImprovements model has isClaimingImprovements = true defined" in {
        val value = IsClaimingImprovementsModel(true)

        IsClaimingImprovementsModel.postWrites.writes(value) shouldBe
          Json.obj(("isClaimingImprovements", true))
      }
      "IsClaimingImprovements model has isClaimingImprovements = false defined" in {
        val value = IsClaimingImprovementsModel(false)

        IsClaimingImprovementsModel.postWrites.writes(value) shouldBe
          Json.obj(("isClaimingImprovements", false))
      }
    }
  }
}