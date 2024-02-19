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

import scala.util.{Failure, Success, Try}

object Transformers {

  val stripCurrencyCharacters: String => String = (input) =>
    input
      .trim()
      .replaceAll(",", "")
      .replaceAll("£", "")

  val stripOptionalCurrencyCharacters: Option[String] => Option[String] = (input) => input match {
    case Some(v) => Some(stripCurrencyCharacters(v))
    case None => None
  }

  val stringToBigDecimal: String => BigDecimal = (input) => Try(BigDecimal(input.trim)) match {
    case Success(value) => value
    case Failure(_) => BigDecimal(0)
  }

  val stringToOptionalBigDecimal: String => Option[BigDecimal] = (input) => {
    Try(BigDecimal(input.trim)) match {
      case Success(value) => Some(value)
      case Failure(_) => None
    }
  }

  val optionalStringToOptionalBigDecimal: Option[String] => Option[BigDecimal] = {
    case Some(input) => stringToOptionalBigDecimal(input)
    case None => None
  }

  val optionalBigDecimalToOptionalString: Option[BigDecimal] => Option[String] = {
    case Some(data) => Some(bigDecimalToString(data))
    case None => None
  }

  val bigDecimalToString: BigDecimal => String = (input) => input.scale match {
    case scale if scale < 2 && scale != 0 => input.setScale(2).toString()
    case _ => input.toString
  }

  val optionalBigDecimalToString: Option[BigDecimal] => String = {
    case Some(data) => bigDecimalToString(data)
    case None => ""
  }

  val stringToInteger: String => Int = (input) => Try(input.trim.toInt) match {
    case Success(value) => value
    case Failure(_) => 0
  }

  val stringToBoolean: String => Boolean = {
    case "Yes" => true
    case _ => false
  }

  val booleanToString: Boolean => String = (input) => if (input) "Yes" else "No"

  val booleanToMessageString: Boolean => String = (input) => if (input) "calc.base.yes" else "calc.base.no"

  def checkIfBooleanAsString(input: String): String = input match {
    case "Yes" => "calc.base.yes"
    case "No" => "calc.base.no"
    case _ => input
  }

  val finalDate: Boolean => String = (input) => if (input)
    "calc.privateResidenceRelief.questionBetween.partOneAndTwo" else ""

  val localDateMonthKey: Int => String = (input) => s"calc.month.$input"
}
