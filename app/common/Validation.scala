/*
 * Copyright 2023 HM Revenue & Customs
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
import common.nonresident.MoneyPounds
import play.api.data.validation._

import scala.util.{Failure, Success, Try}

object Validation {

  def stopOnFirstFail[T](constraints: Constraint[T]*): Constraint[T] = Constraint { field: T =>
    constraints.toList.dropWhile(constraint => constraint(field) == Valid) match {
      case Nil => Valid
      case constraint :: _ => constraint(field)
    }
  }

  def isValidDate(day: Int, month: Int, year: Int): Boolean = Try(constructDate(day, month, year)) match {
    case Success(_) => true
    case _ => false
  }

  def isBigDecimalNumber(input: String): Boolean = Try(BigDecimal(input)) match {
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

  val mandatoryCheck: String => Boolean = input => input.trim != ""

  val decimalPlacesCheck: BigDecimal => Boolean = input => input.scale < 3

  val decimalPlacesCheckNoDecimal: BigDecimal => Boolean = input => input.scale < 1

  val maxCheck: BigDecimal => Boolean = input => input <= Constants.maxNumeric

  def decimalPlaceConstraint(errMsgKey: String, decimalPlace: Int = 3): Constraint[BigDecimal] = Constraint{
    case input if input.scale < decimalPlace =>
      Valid
    case _ =>
      Invalid(errMsgKey)
  }

  def negativeConstraint(errMsgKey: String): Constraint[BigDecimal] = Constraint{
    case input if input >= 0 =>
      Valid
    case _ =>
      Invalid(errMsgKey)
  }

  def maxMonetaryValueConstraint(
                                  maxValue: BigDecimal = Constants.maxNumeric,
                                  errMsgKey: String = "calc.common.error.maxNumericExceeded"
                                ): Constraint[BigDecimal] = Constraint("constraints.maxValue")({
    value => maxMoneyCheck(value, maxValue, errMsgKey)
  })

  def maxMonetaryValueConstraint[T](maxValue: BigDecimal, extractMoney: T => Option[BigDecimal], errMsgKey: Option[String]): Constraint[T] = {
    Constraint("constraints.maxValueCustom")({
      data => extractMoney(data).map {
        maxMoneyCheck(_, maxValue, errMsgKey.getOrElse("calc.common.error.maxNumericExceeded"))
      }.getOrElse(Valid)
    })
  }

  private def maxMoneyCheck(value: BigDecimal, maxValue: BigDecimal, errMsgKey: String): ValidationResult = {
    if(value <= maxValue) {
      Valid
    } else {
      Invalid(ValidationError(errMsgKey, MoneyPounds(maxValue, 0).quantity))
    }
  }

  val maxMoney: BigDecimal => Boolean = input => input <= Constants.maxNumeric

  val isPositive: BigDecimal => Boolean = input => input >= 0

  val yesNoCheck: String => Boolean = {
    case "Yes" => true
    case "No" => true
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

  val previousLossOrGainCheck: String => Boolean = {
    case "Loss" => true
    case "Gain" => true
    case "Neither" => true
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


  def isYesNoOption(electionMade: Boolean): Option[String] => Boolean = {
    case Some(value) if !electionMade => yesNoCheck(value)
    case _ => true
  }
}
