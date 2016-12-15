/*
 * Copyright 2016 HM Revenue & Customs
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

import uk.gov.hmrc.play.test.UnitSpec
import java.time.LocalDate
import common.Dates.formatter
import play.api.libs.concurrent.Execution.Implicits._

class DatesSpec extends UnitSpec {

  "Calling constructDate method" should {

    "return a valid date object with single digit inputs" in {
      Dates.constructDate(1, 2, 1990) shouldBe LocalDate.parse("01/02/1990", formatter)
    }

    "return a valid date object with double digit inputs" in {
      Dates.constructDate(10, 11, 2016) shouldBe LocalDate.parse("10/11/2016", formatter)
    }
  }

  "Calling getDay" should {
    "return an integer value of the day" in {
      Dates.getDay(LocalDate.parse("12/12/2014", formatter)) shouldEqual 12
    }
  }

  "Calling getMonth" should {
    "return an integer value of the month" in {
      Dates.getMonth(LocalDate.parse("11/12/2014", formatter)) shouldEqual 12
    }
  }
  "Calling getYear" should {
    "return an integer value of the year" in {
      Dates.getYear(LocalDate.parse("12/12/2014", formatter)) shouldEqual 2014
    }
  }

  "Calling getCurrent Tax Year" should {
    "return the current tax year in the form YYYY/YY" in {
      for {
        date <- Dates.getCurrentTaxYear
      } yield date.length shouldEqual 7
    }
  }

  "Calling returnDisposalYear" should {

    "when called with 5/4/2016" in {
      Dates.getDisposalYear(5, 4, 2016) shouldEqual 2016
    }

    "when called with 6/4/2016" in {
      Dates.getDisposalYear(6, 4, 2016) shouldEqual 2017
    }

    "when called with 5/4/2015" in {
      Dates.getDisposalYear(5, 4, 2015) shouldEqual 2015
    }
  }
}
