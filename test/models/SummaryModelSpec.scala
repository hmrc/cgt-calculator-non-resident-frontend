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
import common.nonresident.{Flat, Rebased, TimeApportioned}

class SummaryModelSpec extends CommonPlaySpec{

  "SummaryModel.reliefApplied" should {
    "return flat" when {
      "Calculation Type is flat and relief amount is greater than zero" in {
        val calculationType = CalculationElectionModel(Flat)
        val reliefs = OtherReliefsModel(BigDecimal(100))

        val model = SummaryModel(currentIncomeModel = CurrentIncomeModel(BigDecimal(0)),
          personalAllowanceModel = None,
          otherPropertiesModel = OtherPropertiesModel("None"),
          annualExemptAmountModel = None,
          acquisitionDateModel = DateModel(1,1, 2000),
          acquisitionValueModel = AcquisitionValueModel(BigDecimal(0)),
          rebasedValueModel = None,
          rebasedCostsModel = None,
          improvementsModel = None,
          disposalDateModel = DateModel(1,1, 2000),
          disposalValueModel = DisposalValueModel(BigDecimal(0)),
          acquisitionCostsModel = AcquisitionCostsModel(BigDecimal(0)),
          disposalCostsModel = DisposalCostsModel(BigDecimal(0)),
          calculationElectionModel = calculationType,
          otherReliefsModelFlat = reliefs,
          otherReliefsModelTA = reliefs,
          otherReliefsModelRebased = reliefs,
          privateResidenceReliefModel = None)

        model.reliefApplied() shouldBe Flat
      }
    }

    "return rebased" when {
      "Calculation Type is rebased and relief amount is greater than zero" in {
        val calculationType = CalculationElectionModel(Rebased)
        val reliefs = OtherReliefsModel(BigDecimal(100))

        val model = SummaryModel(currentIncomeModel = CurrentIncomeModel(BigDecimal(0)),
          personalAllowanceModel = None,
          otherPropertiesModel = OtherPropertiesModel("None"),
          annualExemptAmountModel = None,
          acquisitionDateModel = DateModel(1,1, 2000),
          acquisitionValueModel = AcquisitionValueModel(BigDecimal(0)),
          rebasedValueModel = None,
          rebasedCostsModel = None,
          improvementsModel = None,
          disposalDateModel = DateModel(1,1, 2000),
          disposalValueModel = DisposalValueModel(BigDecimal(0)),
          acquisitionCostsModel = AcquisitionCostsModel(BigDecimal(0)),
          disposalCostsModel = DisposalCostsModel(BigDecimal(0)),
          calculationElectionModel = calculationType,
          otherReliefsModelFlat = reliefs,
          otherReliefsModelTA = reliefs,
          otherReliefsModelRebased = reliefs,
          privateResidenceReliefModel = None)

        model.reliefApplied() shouldBe Rebased
      }
    }

    "return timeApportioned" when {
      "Calculation Type is timeApportioned and relief amount is greater than zero" in {
        val calculationType = CalculationElectionModel(TimeApportioned)
        val reliefs = OtherReliefsModel(BigDecimal(100))

        val model = SummaryModel(currentIncomeModel = CurrentIncomeModel(BigDecimal(0)),
          personalAllowanceModel = None,
          otherPropertiesModel = OtherPropertiesModel("None"),
          annualExemptAmountModel = None,
          acquisitionDateModel = DateModel(1,1, 2000),
          acquisitionValueModel = AcquisitionValueModel(BigDecimal(0)),
          rebasedValueModel = None,
          rebasedCostsModel = None,
          improvementsModel = None,
          disposalDateModel = DateModel(1,1, 2000),
          disposalValueModel = DisposalValueModel(BigDecimal(0)),
          acquisitionCostsModel = AcquisitionCostsModel(BigDecimal(0)),
          disposalCostsModel = DisposalCostsModel(BigDecimal(0)),
          calculationElectionModel = calculationType,
          otherReliefsModelFlat = reliefs,
          otherReliefsModelTA = reliefs,
          otherReliefsModelRebased = reliefs,
          privateResidenceReliefModel = None)

        model.reliefApplied() shouldBe TimeApportioned
      }
    }
  }

  "SummaryModel.deductionsDetailsRows" should {
    "return the correct model" in {
      val model = SummaryModel(currentIncomeModel = CurrentIncomeModel(BigDecimal(0)),
        personalAllowanceModel = None,
        otherPropertiesModel = OtherPropertiesModel("None"),
        annualExemptAmountModel = None,
        acquisitionDateModel = DateModel(1,1, 2000),
        acquisitionValueModel = AcquisitionValueModel(BigDecimal(0)),
        rebasedValueModel = None,
        rebasedCostsModel = None,
        improvementsModel = None,
        disposalDateModel = DateModel(1,1, 2000),
        disposalValueModel = DisposalValueModel(BigDecimal(0)),
        acquisitionCostsModel = AcquisitionCostsModel(BigDecimal(0)),
        disposalCostsModel = DisposalCostsModel(BigDecimal(0)),
        calculationElectionModel = CalculationElectionModel(TimeApportioned),
        otherReliefsModelFlat = OtherReliefsModel(BigDecimal(0)),
        otherReliefsModelTA = OtherReliefsModel(BigDecimal(0)),
        otherReliefsModelRebased = OtherReliefsModel(BigDecimal(0)),
        privateResidenceReliefModel = None)

      val qModel = Seq(QuestionAnswerModel[String]("", "", "", None))
      val calcmodel = CalculationResultModel(BigDecimal(0), BigDecimal(0), BigDecimal(0), 1, BigDecimal(0), None, None, None)

      model.deductionsDetailsRows(calcmodel) shouldBe qModel
    }
  }
}
