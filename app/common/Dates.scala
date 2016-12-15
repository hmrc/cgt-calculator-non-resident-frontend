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

import java.time._
import java.time.format.{DateTimeFormatter, ResolverStyle}
import java.time.temporal.ChronoUnit

import scala.concurrent.Future

object Dates {

  val taxYearEnd = "04-05"
  val formatter = DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT)
  val requestFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT)
  val datePageFormatNoZero = DateTimeFormatter.ofPattern("d MMMM uuuu").withResolverStyle(ResolverStyle.STRICT)

  def constructDate(day: Int, month: Int, year: Int): LocalDate = LocalDate.parse(s"$day/$month/$year", formatter)

  def dateMinusMonths(date: Option[LocalDate], months: Int): String = date.fold("") {
    a => a.minus(months, ChronoUnit.MONTHS).format(datePageFormatNoZero)
  }

  def getDay(date: LocalDate): Int = date.getDayOfMonth

  def getMonth(date: LocalDate): Int = date.getMonthValue

  def getYear(date: LocalDate): Int = date.getYear

  def taxYearToString(input: Int): String = {
    val startYear = input - 1
    val endYear = input.toString.takeRight(2)
    s"$startYear/$endYear"
  }

  def getCurrentTaxYear: Future[String] = {
    val now = LocalDate.now()
    val year = now.getYear
    if (now.isAfter(LocalDate.parse(s"${year.toString}-$taxYearEnd"))) {
      Future.successful(taxYearToString(year + 1))
    }
    else {
      Future.successful(taxYearToString(year))
    }
  }

  def getDisposalYear(day: Int, month: Int, year: Int): Int = {
    val disposalYear = LocalDate.of(year, month, day)
    if (disposalYear.isAfter(LocalDate.parse(s"${year.toString}-$taxYearEnd"))) {
      year + 1
    }
    else {
      year
    }
  }
}

