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

package constructors

import models._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class CalculationElectionConstructorSpec extends UnitSpec with MockitoSugar with WithFakeApplication {

  val target = CalculationElectionConstructor
  val onlyFlat = TotalGainResultsModel(BigDecimal(0), None, None)
  val flatAndRebased = TotalGainResultsModel(BigDecimal(-100), Some(BigDecimal(-50)), None)
  val flatAndTime = TotalGainResultsModel(BigDecimal(-20), None, Some(BigDecimal(-300)))
  val flatRebasedAndTime = TotalGainResultsModel(BigDecimal(0), Some(BigDecimal(0)), Some(BigDecimal(-300)))

  "Calling generateElection with only a TotalGainsResultsModel" should {

    "when only a flat calculation result is provided" should {

      lazy val calculations = target.generateElection(onlyFlat, None, None, None)
      "produce sequence with one element" in {
        calculations.size shouldBe 1
      }

      "contain a flat calculation" in {
        calculations.head._1 shouldEqual "flat"
      }
    }

    "when a flat calculation and a rebased calculation result are provided" should {

      lazy val calculations = target.generateElection(flatAndRebased, None, None, None)

      "produce two entries in the sequence" in {
        calculations.size shouldBe 2
      }

      "should be returned with an order" which {

        "contain a flat calculation" in {
          calculations.head._1 shouldEqual "flat"
        }

        "should have rebased as the second element" in {
          calculations(1)._1 shouldEqual "rebased"
        }
      }
    }

    "when a flat calculation and a time calculation result are provided" should {

      lazy val calculations = target.generateElection(flatAndTime, None, None, None)

      "produce two entries in the sequence" in {
        calculations.size shouldBe 2
      }

      "should be returned with an order" which {

        "contain a time calculation" in {
          calculations.head._1 shouldEqual "time"
        }

        "should have flat as the second element" in {
          calculations(1)._1 shouldEqual "flat"
        }
      }
    }

    "when a flat, rebased and time are all provided" should {

      lazy val calculations = target.generateElection(flatRebasedAndTime, None, None, None)

      "produce a three entry sequence" in {
        calculations.size shouldBe 3
      }

      "should be returned with an order" which {

        "contain a time calculation" in {
          calculations.head._1 shouldEqual "time"
        }

        "should have flat as the second element" in {
          calculations(1)._1 shouldEqual "flat"
        }

        "should have rebased as the third element" in {
          calculations(2)._1 shouldEqual "rebased"
        }
      }
    }
  }

  //order: rebased, flat
  val totalGainsAfterPRRFlatAndTASortByTotalGain = CalculationResultsWithPRRModel(
    GainsAfterPRRModel(2, 1, 0),
    Some(GainsAfterPRRModel(1, 1, 0)),
    None
  )

  //order: rebased, flat
  val totalGainsAfterPRRFlatAndTASortByTaxableGain = CalculationResultsWithPRRModel(
    GainsAfterPRRModel(2, 3, 0),
    Some(GainsAfterPRRModel(2, 1, 0)),
    None
  )

  //order: time, rebased, flat
  val totalGainsAllSortByTotalGain = CalculationResultsWithPRRModel(
    GainsAfterPRRModel(3, 1, 0),
    Some(GainsAfterPRRModel(2, 1, 0)),
    Some(GainsAfterPRRModel(1, 4, 0))
  )

  //order: rebased, flat, time
  val totalGainsAllSortByTaxableGain = CalculationResultsWithPRRModel(
    GainsAfterPRRModel(3, 3, 0),
    Some(GainsAfterPRRModel(3, 1, 0)),
    Some(GainsAfterPRRModel(3, 4, 0))
  )

  "Calling generateElection with a TotalGainsResultsModel, and a CalculationResultsWithPRRModel" should {

    "when a flat calculation and a rebased PRR calculation result are provided" should {

      lazy val calculations = target.generateElection(flatAndRebased, Some(totalGainsAfterPRRFlatAndTASortByTotalGain), None, None)

      "produce two entries in the sequence" in {
        calculations.size shouldBe 2
      }

      "should be returned with an order" which {

        "contain a rebased as the first element" in {
          calculations.head._1 shouldEqual "rebased"
        }

        "should have flat as the second element" in {
          calculations(1)._1 shouldEqual "flat"
        }
      }
    }

    "when a flat calculation and a rebased calculation result are provided that should be ordered by taxable gain" should {

      lazy val calculations = target.generateElection(flatAndTime, Some(totalGainsAfterPRRFlatAndTASortByTaxableGain), None, None)

      "produce two entries in the sequence" in {
        calculations.size shouldBe 2
      }

      "should be returned with an order" which {

        "contain a rebased calculation" in {
          calculations.head._1 shouldEqual "rebased"
        }

        "should have flat as the second element" in {
          calculations(1)._1 shouldEqual "flat"
        }
      }
    }

    "when a flat, rebased and time prr result should be sorted by total gain" should {

      lazy val calculations = target.generateElection(flatRebasedAndTime, Some(totalGainsAllSortByTotalGain), None, None)

      "produce a three entry sequence" in {
        calculations.size shouldBe 3
      }

      "should be returned with an order" which {

        "contain a time calculation" in {
          calculations.head._1 shouldEqual "time"
        }

        "should have rebased as the second element" in {
          calculations(1)._1 shouldEqual "rebased"
        }

        "should have flat as the third element" in {
          calculations(2)._1 shouldEqual "flat"
        }
      }
    }

    "when a flat, rebased and time prr result should be sorted by taxable gain" should {

      lazy val calculations = target.generateElection(flatRebasedAndTime, Some(totalGainsAllSortByTaxableGain), None, None)

      "produce a three entry sequence" in {
        calculations.size shouldBe 3
      }

      "should be returned with an order" which {

        "contain a rebased calculation" in {
          calculations.head._1 shouldEqual "rebased"
        }

        "should have flat as the second element" in {
          calculations(1)._1 shouldEqual "flat"
        }

        "should have time as the third element" in {
          calculations(2)._1 shouldEqual "time"
        }
      }
    }
  }

  val calculationResultsTotalSortByTaxOwed = CalculationResultsWithTaxOwedModel(
    TotalTaxOwedModel(4, 1, 1, Some(1), Some(1), 1, 1, Some(1), Some(1), Some(1), Some(1), 1, Some(1)),
    Some(TotalTaxOwedModel(1, 1, 1, Some(1), Some(1), 1, 1, Some(1), Some(1), Some(1), Some(1), 1, Some(1))),
    Some(TotalTaxOwedModel(2, 1, 1, Some(1), Some(1), 1, 1, Some(1), Some(1), Some(1), Some(1), 1, Some(1)))
  )

  "Calling generateElection with a TotalGainsResultsModel, and a CalculationResultsWithTaxOwedModel" should {

    "when a flat, rebased and time prr result should be sorted by taxable gain" should {

      lazy val calculations = target.generateElection(flatRebasedAndTime, None, Some(calculationResultsTotalSortByTaxOwed), None)

      "produce a three entry sequence" in {
        calculations.size shouldBe 3
      }

      "should be returned with an order" which {

        "contain a rebased calculation" in {
          calculations.head._1 shouldEqual "rebased"
        }

        "should have time as the second element" in {
          calculations(1)._1 shouldEqual "time"
        }

        "should have flat as the third element" in {
          calculations(2)._1 shouldEqual "flat"
        }
      }
    }
  }
}
