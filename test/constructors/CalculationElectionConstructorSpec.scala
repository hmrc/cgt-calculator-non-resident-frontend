/*
 * Copyright 2021 HM Revenue & Customs
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

import common.{CommonPlaySpec, WithCommonFakeApplication}
import models._
import org.scalatestplus.mockito.MockitoSugar

class CalculationElectionConstructorSpec()
  extends CommonPlaySpec with MockitoSugar with WithCommonFakeApplication {

  val onlyFlat = TotalGainResultsModel(BigDecimal(0), None, None)
  val flatAndRebased = TotalGainResultsModel(BigDecimal(-100), Some(BigDecimal(-50)), None)
  val flatAndTime = TotalGainResultsModel(BigDecimal(-20), None, Some(BigDecimal(-300)))
  val flatRebasedAndTime = TotalGainResultsModel(BigDecimal(0), Some(BigDecimal(0)), Some(BigDecimal(-300)))

  val target: CalculationElectionConstructor = new DefaultCalculationElectionConstructor()

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

      lazy val calculations = target.generateElection(flatAndRebased, Some(totalGainsAfterPRRFlatAndTASortByTotalGain), None,
        Some(AllOtherReliefsModel(Some(OtherReliefsModel(1000)), Some(OtherReliefsModel(1000)), Some(OtherReliefsModel(1000)))))

      "produce two entries in the sequence" in {
        calculations.size shouldBe 2
      }

      "should contain entries with reliefs included" in {
        await(calculations).forall(_._6.isDefined) shouldBe true
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
    TotalTaxOwedModel(4, 1, 1, Some(1), Some(1), 1, 1, Some(1), Some(1), Some(1), Some(1), 1, Some(1), Some(1), Some(1), Some(1), Some(1), Some(1), Some(1)),
    Some(TotalTaxOwedModel(1, 1, 1, Some(1), Some(1), 1, 1, Some(1), Some(1), Some(1),
      Some(1), 1, Some(1), Some(1), Some(1), Some(1), Some(1), Some(1), Some(1))),
    Some(TotalTaxOwedModel(2, 1, 1, Some(1), Some(1), 1, 1, Some(1), Some(1), Some(1),
      Some(1), 1, Some(1), Some(1), Some(1), Some(1), Some(1), Some(1), Some(1)))
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

  "Calling .rebasedElementConstructor" should {

    "return a CalculationElectionOption" which {
      lazy val model = target.rebasedElementConstructor(1000, "data", Some(5000))

      "has a calcType of 'rebased'" in {
        model.calcType shouldBe "rebased"
      }

      "has an amount of '1000.00'" in {
        model.amount shouldBe BigDecimal(1000.00)
      }

      s"has a message of 'calc.calculationElection.message.rebased'" in {
        model.message shouldBe "calc.calculationElection.message.rebased"
      }

      s"has a calcDescription of 'calc.calculationElection.description.rebased'" in {
        model.calcDescription shouldBe "calc.calculationElection.description.rebased"
      }

      s"has a date of 'calc.calculationElection.message.rebasedDate'" in {
        model.date shouldBe Some("calc.calculationElection.message.rebasedDate")
      }

      "has some data as a string of 'data'" in {
        model.data shouldBe "data"
      }

      "has other reliefs of 5000" in {
        model.otherReliefs shouldBe Some(5000)
      }
    }
  }

  "Calling .flatElementConstructor" should {

    "return a CalculationElectionOption" which {
      lazy val model = target.flatElementConstructor(1000, 100, None)

      "has a calcType of 'flat'" in {
        model.calcType shouldBe "flat"
      }

      "has an amount of '1000.00'" in {
        model.amount shouldBe BigDecimal(1000.00)
      }

      s"has a message of 'calc.calculationElection.message.flat'" in {
        model.message shouldBe "calc.calculationElection.message.flat"
      }

      s"has a calcDescription of 'calc.calculationElection.description.flat'" in {
        model.calcDescription shouldBe "calc.calculationElection.description.flat"
      }

      s"has no date" in {
        model.date shouldBe None
      }

      "has some data as an integer of '100'" in {
        model.data shouldBe 100
      }

      "has no other reliefs" in {
        model.otherReliefs shouldBe None
      }
    }
  }

  "Calling .timeElementConstructor" should {

    "return a CalculationElectionOption" which {
      lazy val model = target.timeElementConstructor(1000, "data", Some(5000))

      "has a calcType of 'time'" in {
        model.calcType shouldBe "time"
      }

      "has an amount of '1000.00'" in {
        model.amount shouldBe BigDecimal(1000.00)
      }

      s"has a message of 'calc.calculationElection.message.time'" in {
        model.message shouldBe "calc.calculationElection.message.time"
      }

      s"has a calcDescription of 'calc.calculationElection.description.time'" in {
        model.calcDescription shouldBe "calc.calculationElection.description.time"
      }

      s"has a date of 'calc.calculationElection.message.timeDate'" in {
        model.date shouldBe Some("calc.calculationElection.message.timeDate")
      }

      "has some data as a string of 'data'" in {
        model.data shouldBe "data"
      }

      "has other reliefs of 5000" in {
        model.otherReliefs shouldBe Some(5000)
      }
    }
  }
}
