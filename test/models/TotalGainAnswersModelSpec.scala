/*
 * Copyright 2020 HM Revenue & Customs
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

import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class TotalGainAnswersModelSpec extends UnitSpec with MockitoSugar {

  "TotalGainAnswersModel" should {
    "write to Json" in {
      val outputJson = Json.parse(
        """
          |{
          |"disposalValue":500000,
          |"disposalCosts":20000,
          |"acquisitionValue":350000,
          |"acquisitionCosts":20000,
          |"improvements":9000,
          |"rebasedValue":450000,
          |"rebasedCosts":20000,
          |"disposalDate":"2017-05-12",
          |"acquisitionDate":"2014-08-14",
          |"improvementsAfterTaxStarted":1000
          |}
        """.stripMargin)

      val model = TotalGainAnswersModel(
        DateModel(12, 5, 2017),
        SoldOrGivenAwayModel(true),
        None,
        DisposalValueModel(500000),
        DisposalCostsModel(20000),
        None, None,
        AcquisitionValueModel(350000),
        Some(AcquisitionCostsModel(20000)),
        DateModel(14, 8, 2014),
        Some(RebasedValueModel(450000)),
        Some(RebasedCostsModel("Yes", Some(20000))),
        ImprovementsModel("Yes", Some(9000), Some(1000)),
        None, None)

      Json.toJson(model) shouldBe outputJson
    }


      "return 0 acquisitionCost" in {
        val outputJson = Json.parse(
          """
            |{
            |"disposalValue":500000,
            |"disposalCosts":20000,
            |"acquisitionValue":350000,
            |"acquisitionCosts":0,
            |"improvements":9000,
            |"rebasedValue":450000,
            |"rebasedCosts":20000,
            |"disposalDate":"2017-05-12",
            |"acquisitionDate":"2014-08-14",
            |"improvementsAfterTaxStarted":1000
            |}
          """.stripMargin)

        val model = TotalGainAnswersModel(
          DateModel(12, 5, 2017),
          SoldOrGivenAwayModel(false),
          None,
          DisposalValueModel(500000),
          DisposalCostsModel(20000),
          None, None,
          AcquisitionValueModel(350000),
          None,
          DateModel(14, 8, 2014),
          Some(RebasedValueModel(450000)),
          Some(RebasedCostsModel("Yes", Some(20000))),
          ImprovementsModel("Yes", Some(9000), Some(1000)),
          None,
          Some(CostsAtLegislationStartModel("Yes", Some(10000)))
        )

        Json.toJson(model) shouldBe outputJson
      }


    "return no optional Values" in {
      val outputJson = Json.parse(
        """
          |{
          |"disposalValue":500000,
          |"disposalCosts":20000,
          |"acquisitionValue":350000,
          |"disposalDate":"2017-05-12",
          |"acquisitionDate":"2015-04-06",
          |"acquisitionCosts":0
          |}
        """.stripMargin)

      val model = TotalGainAnswersModel(
        DateModel(12, 5, 2017),
        SoldOrGivenAwayModel(true),
        None,
        DisposalValueModel(500000),
        DisposalCostsModel(20000),
        None, None,
        AcquisitionValueModel(350000),
        None,
        DateModel(6, 4, 2015),
        None, None,
        ImprovementsModel("Yes", None, None),
        None, None)

      Json.toJson(model) shouldBe outputJson
    }

    "return all optional Values" in {
      val outputJson = Json.parse(
        """
          |{
          |"disposalValue":500000,
          |"disposalCosts":20000,
          |"acquisitionValue":350000,
          |"acquisitionCosts":6000,
          |"improvements":9000,
          |"rebasedValue":450000,
          |"rebasedCosts":20000,
          |"disposalDate":"2017-05-12",
          |"acquisitionDate":"1981-08-14",
          |"improvementsAfterTaxStarted":1000
          |}
        """.stripMargin)

      val model = TotalGainAnswersModel(
        DateModel(12, 5, 2017),
        SoldOrGivenAwayModel(true),
        Some(SoldForLessModel(true)),
        DisposalValueModel(500000),
        DisposalCostsModel(20000),
        Some(HowBecameOwnerModel("I found it")),
        Some(BoughtForLessModel(false)),
        AcquisitionValueModel(350000),
        Some(AcquisitionCostsModel(20000)),
        DateModel(14, 8, 1981),
        Some(RebasedValueModel(450000)),
        Some(RebasedCostsModel("Yes", Some(20000))),
        ImprovementsModel("Yes", Some(9000), Some(1000)),
        Some(OtherReliefsModel(4500)),
        Some(CostsAtLegislationStartModel("Yes", Some(6000))))

      Json.toJson(model) shouldBe outputJson
    }

    "return same json objedct as backend example" in {
      val outputJson = Json.parse(
          """
            |{
            |"disposalValue":500000,
            |"disposalCosts":20000,
            |"acquisitionValue":350000,
            |"acquisitionCosts":20000,
            |"improvements":9000,
            |"rebasedValue":450000,
            |"rebasedCosts":20000,
            |"disposalDate":"2017-05-12",
            |"acquisitionDate":"2014-08-14",
            |"improvementsAfterTaxStarted":1000
            |}
          """.stripMargin)

      val model = TotalGainAnswersModel(
        DateModel(12, 5, 2017),
        SoldOrGivenAwayModel(true),
        None,
        DisposalValueModel(500000),
        DisposalCostsModel(20000),
        None,None,
        AcquisitionValueModel(350000),
        Some(AcquisitionCostsModel(20000)),
        DateModel(14, 8, 2014),
        Some(RebasedValueModel(450000)),
        Some(RebasedCostsModel("Yes", Some(20000))),
        ImprovementsModel("Yes", Some(9000), Some(1000)),
        None,
        None)

      Json.toJson(model) shouldBe outputJson
    }
  }
}