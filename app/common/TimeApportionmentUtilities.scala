/*
 * Copyright 2018 HM Revenue & Customs
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

object TimeApportionmentUtilities {

  def percentageOfTotalGain(flatGain: BigDecimal, timeApportionedGain: BigDecimal): Int =
        //This if statement should, by the very nature of the calculations NEVER be triggered but it's a catch all.
        if(Math.abs(flatGain.toDouble) < Math.abs(timeApportionedGain.toDouble)) throw new Exception("TA gain larger than FLAT")
        else Math.round(Math.abs(timeApportionedGain.toDouble)/Math.abs(flatGain.toDouble)*100).toInt
}
