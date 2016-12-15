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

package connectors

import common.Validation._
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class ValidationSpec extends UnitSpec {

  //############# Tests for isValidDate function ##########################################
  "calling common.Validation.isValidDate(day, month, year) " should {

    "with no day value supplied 'isValidDate(0,1,2016)' return false" in {
      isValidDate(0, 1, 2016) shouldBe false
    }

    "with no month value supplied 'isValidDate(1,0,2016)' return false" in {
      isValidDate(1, 0, 2016) shouldBe false
    }

    "with no year value supplied 'isValidDate(0,1,2016)' return false" in {
      isValidDate(1, 1, 0) shouldBe false
    }

    "with invalid date 'isValidDate(32,1,2016)' return false" in {
      isValidDate(32, 1, 2016) shouldBe false
    }

    "with invalid leap year date 'isValidDate(29,2,2017)' return false" in {
      isValidDate(29, 2, 2017) shouldBe false
    }

    "with valid leap year date 'isValidDate(29,2,2016)' return true" in {
      isValidDate(29, 2, 2016) shouldBe true
    }

    "with valid  date 'isValidDate(12,09,1990)' return true" in {
      isValidDate(12, 9, 1990) shouldBe true
    }
  }


  //############# Tests for isPositive function ##########################################
  "calling common.Validation.isPositive(amount) " should {

    "with a positive numeric supplied isPositive(1) return true" in {
      isPositive(1) shouldBe true
    }

    "with Zero supplied return true" in {
      isPositive(0) shouldBe true
    }

    "with Negative supplied return false" in {
      isPositive(-1) shouldBe false
    }
  }


  //############# Tests for decimalPlacesCheck ##########################################
  "calling common.Validation.decimalPlacesCheck(amount) " should {

    "with no decimals supplied decimalPlacesCheck(1) return true" in {
      decimalPlacesCheck(1) shouldBe true
    }

    "with one decimal place supplied decimalPlacesCheck(1.1) return true" in {
      decimalPlacesCheck(1.1) shouldBe true
    }

    "with two decimal places supplied decimalPlacesCheck(1.11) return true" in {
      decimalPlacesCheck(1.11) shouldBe true
    }

    "with three decimal places supplied decimalPlacesCheck(1.111) return false" in {
      decimalPlacesCheck(1.111) shouldBe false
    }
  }

  //############# Tests for isLessThanMaxNumber ##########################################
  "calling common.Validation.isGreaterThanMaxNumeric(amount) " should {

    "with a value of 1000000000" in {
      maxCheck(1000000000) shouldBe true
    }

    "with a value of 1000000000.01" in {
      maxCheck(1000000000.01) shouldBe false
    }

    "with a value of 999999999.99" in {
      maxCheck(999999999.99) shouldBe true
    }

    //############# Tests for isBigDecimalNumber ##########################################
    "calling common.Validation.isBigDecimalNumber" should {

      "return false with a non-numeric value" in {
        isBigDecimalNumber("a") shouldBe false
      }

      "return true with a valid numeric value" in {
        isBigDecimalNumber("100") shouldBe true
      }
    }
  }

  //############# Tests for yesNoCheck ##########################################
  "calling common.Validation.yesNoCheck" should {

    "return false with a non yes/no value" in {
      yesNoCheck("a") shouldBe false
    }

    "return true with a yes value" in {
      yesNoCheck("Yes") shouldBe true
    }

    "return true with a no value" in {
      yesNoCheck("No") shouldBe true
    }
  }

  "calling bigDecimalCheck" when {

    "input contains non-numeric characters" should {
      "fail" in {
        bigDecimalCheck("abc") shouldBe false
      }
    }

    "empty input" should {
      "pass" in {
        bigDecimalCheck("") shouldBe true
      }
    }

    "empty space" should {
      "pass" in {
        bigDecimalCheck("   ") shouldBe true
      }
    }

    "input only contains numeric characters" should {
      "pass" in {
        bigDecimalCheck("123") shouldBe true
      }
    }
  }

  "calling mandatoryCheck" when {

    "input contains no data" should {
      "fail" in {
        mandatoryCheck("") shouldBe false
      }
    }

    "input contains only empty space" should {
      "fail" in {
        mandatoryCheck("    ") shouldBe false
      }
    }

    "input contains data" should {
      "pass" in {
        mandatoryCheck("123") shouldBe true
      }
    }
  }

  "calling decimalPlacesCheck" when {

    "input has no decimal places" should {
      "pass" in {
        decimalPlacesCheck(BigDecimal(1)) shouldBe true
      }
    }

    "input has 1 decimal place" should {
      "pass" in {
        decimalPlacesCheck(BigDecimal(1.1)) shouldBe true
      }
    }

    "input has 2 decimal places" should {
      "pass" in {
        decimalPlacesCheck(BigDecimal(1.11)) shouldBe true
      }
    }

    "input has 3 decimal places" should {
      "fail" in {
        decimalPlacesCheck(BigDecimal(1.111)) shouldBe false
      }
    }
  }

  "calling maxCheck" when {

    "input is less than max value" should {
      "pass" in {
        maxCheck(BigDecimal(900000000.99999)) shouldBe true
      }
    }

    "input is equal to max value" should {
      "pass" in {
        maxCheck(BigDecimal(1000000000)) shouldBe true
      }
    }

    "input is greater than max value" should {
      "fail" in {
        maxCheck(BigDecimal(1000000001)) shouldBe false
      }
    }
  }

  "calling isPositive" when {

    "input is more than min value" should {
      "pass" in {
        isPositive(BigDecimal(0.01)) shouldBe true
      }
    }

    "input is equal to min value" should {
      "pass" in {
        isPositive(BigDecimal(0)) shouldBe true
      }
    }

    "input is less than min value" should {
      "fail" in {
        isPositive(BigDecimal(-0.01)) shouldBe false
      }
    }
  }
  
  "calling yesNoCheck" when {

    "input is 'Yes'" should {
      "pass" in {
        yesNoCheck("Yes") shouldBe true
      }
    }

    "input is 'No'" should {
      "pass" in {
        yesNoCheck("No") shouldBe true
      }
    }

    "input is empty" should {
      "pass" in {
        yesNoCheck("") shouldBe true
      }
    }

    "input is 'yEs'" should {
      "fail" in {
        yesNoCheck("yEs") shouldBe false
      }
    }

    "input is 'nO'" should {
      "fail" in {
        yesNoCheck("nO") shouldBe false
      }
    }

    "input is empty space" should {
      "fail" in {
        yesNoCheck("    ") shouldBe false
      }
    }

  }

  "calling isYesNoOption" when {

    "calculation is not elected" should {

      "return a result from the yesNoCheck when there is a Some" in {
        isYesNoOption(false)(Some("No")) shouldBe yesNoCheck("No")
      }

      "return a true when there is a None" in {
        isYesNoOption(false)(None) shouldBe true
      }
    }

    "calculation is elected" should {

      "return a true when there is a Some" in {
        isYesNoOption(true)(Some("Yes")) shouldBe true
      }

      "return a true when there is a None" in {
        isYesNoOption(true)(None) shouldBe true
      }
    }
  }
}
