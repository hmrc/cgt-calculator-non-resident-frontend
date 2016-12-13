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

import common.Dates.constructDate

import scala.util.{Failure, Success, Try}

object Validation {

  def isValidDate(day: Int, month: Int, year: Int): Boolean = Try(constructDate(day, month, year)) match {
    case Success(_) => true
    case _ => false
  }

  def isIntNumber(input: String): Boolean = Try(input.toInt) match {
    case Success(_) => true
    case Failure(_) => false
  }

  def isBigDecimalNumber(input: String): Boolean = Try(BigDecimal(input)) match {
    case Success(_) => true
    case Failure(_) => false
  }

  def isDoubleNumber(input: String): Boolean = Try(input.toDouble) match {
    case Success(_) => true
    case Failure(_) => false
  }

  val bigDecimalCheck: String => Boolean = input => Try(BigDecimal(input)) match {
    case Success(_) => true
    case Failure(_) if input.trim == "" => true
    case Failure(_) => false
  }

  val integerCheck: String => Boolean = input => Try(input.trim.toInt) match {
    case Success(_) => true
    case Failure(_) if input.trim == "" => true
    case Failure(_) => false
  }

  val isGreaterThanZero: BigDecimal => Boolean = amount => amount > 0

  val mandatoryCheck: String => Boolean = input => input.trim != ""

  val decimalPlacesCheck: BigDecimal => Boolean = input => input.scale < 3

  val decimalPlacesCheckNoDecimal: BigDecimal => Boolean = input => input.scale < 1

  val validYearRangeCheck: Int => Boolean = input => input >= 1900 && input <= 9999

  val maxCheck: BigDecimal => Boolean = input => input <= Constants.maxNumeric

  def maxPRRCheck(gain: BigDecimal): BigDecimal => Boolean = input => input <= gain

  val isPositive: BigDecimal => Boolean = input => input >= 0

  val yesNoCheck: String => Boolean = {
    case "Yes" => true
    case "No" => true
    case "" => true
    case _ => false
  }

  val fullPartNoneCheck: String => Boolean = {
    case "Full" => true
    case "Part" => true
    case "None" => true
    case "" => true
    case _ => false
  }

  val givenAwayCheck: String => Boolean = {
    case "Given" => true
    case "Sold" => true
    case "" => true
    case _ => false
  }

  val howBecameOwnerCheck: String => Boolean = {
    case "Bought" => true
    case "Gifted" => true
    case "Inherited" => true
    case "" => true
    case _ => false
  }

  val whoDidYouGiveItToCheck: String => Boolean = {
    case "Spouse" => true
    case "Charity" => true
    case "Other" => true
    case "" => true
    case _ => false
  }

  val previousLossOrGainCheck: String => Boolean = {
    case "Loss" => true
    case "Gain" => true
    case "Neither" => true
    case "" => true
    case _ => false
  }

  def isYesNoOption(electionMade: Boolean): Option[String] => Boolean = {
    case Some(value) if !electionMade => yesNoCheck(value)
    case _ => true
  }
}
