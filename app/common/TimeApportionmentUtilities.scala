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

package common

object TimeApportionmentUtilities {

  def percentageOfTotalGain(flatGain: BigDecimal, timeApportionedGain: BigDecimal): Int =
        //This if statement should, by the very nature of the calculations NEVER be triggered but it's a catch all.
        if(flatGain < timeApportionedGain || flatGain <= 0 || timeApportionedGain <= 0) throw new Exception
        else Math.round((timeApportionedGain.toDouble/flatGain.toDouble)*100).toInt
}
