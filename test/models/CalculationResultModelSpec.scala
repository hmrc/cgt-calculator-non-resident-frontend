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

package models

import common.CommonPlaySpec

class CalculationResultModelSpec  extends CommonPlaySpec{

  "calculationDetailsRows" should {
    "return the correct model" in {
        val model = CalculationResultModel(BigDecimal(1.00), BigDecimal(1.00), BigDecimal(1.00), 1, BigDecimal(1.00), Some(BigDecimal(1.00)), None, None)
        val qModel = Seq(QuestionAnswerModel[String]("", "", "", None))

        model.calculationDetailsRows("a") shouldBe qModel
    }
  }
}
