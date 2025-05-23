/*
 * Copyright 2025 HM Revenue & Customs
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

package common.nonresident

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.data.FormError
import play.api.libs.json.*

class CalculationTypeSpec extends AnyWordSpec with Matchers {

  "CalculationType JSON Format" should {

    "serialize CalculationType to JSON" in {
      Json.toJson[CalculationType](Flat) shouldBe JsString("flat")
      Json.toJson[CalculationType](Rebased) shouldBe JsString("rebased")
      Json.toJson[CalculationType](TimeApportioned) shouldBe JsString("timeApportioned")
    }

    "deserialize JSON to CalculationType" in {
      Json.fromJson[CalculationType](JsString("flat")) shouldBe JsSuccess(Flat)
      Json.fromJson[CalculationType](JsString("rebased")) shouldBe JsSuccess(Rebased)
      Json.fromJson[CalculationType](JsString("timeApportioned")) shouldBe JsSuccess(TimeApportioned)
      Json.fromJson[CalculationType](JsString("invalid")) shouldBe JsError("Invalid Calculation type")
    }
  }

  "CalculationType Formatter" should {

    val formatter = CalculationType.formatter

    "bind valid CalculationType from data" in {
      formatter.bind("key", Map("key" -> "flat")) shouldBe Right(Flat)
      formatter.bind("key", Map("key" -> "rebased")) shouldBe Right(Rebased)
      formatter.bind("key", Map("key" -> "timeApportioned")) shouldBe Right(TimeApportioned)
    }

    "return error when binding invalid CalculationType" in {
      formatter.bind("key", Map("key" -> "invalid")) shouldBe Left(Seq(FormError("key", "calc.key.errors.required")))
      formatter.bind("key", Map.empty) shouldBe Left(Seq(FormError("key", "calc.key.errors.required")))
    }

    "unbind CalculationType to data" in {
      formatter.unbind("key", Flat) shouldBe Map("key" -> "flat")
      formatter.unbind("key", Rebased) shouldBe Map("key" -> "rebased")
      formatter.unbind("key", TimeApportioned) shouldBe Map("key" -> "timeApportioned")
    }
  }
}
