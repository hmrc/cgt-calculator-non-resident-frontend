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

package common

import common.Dates.{constructDate, formatter}
import models.DateModel

import java.time.LocalDate
import scala.concurrent.Future

object TaxDates {
  val legislationDate: LocalDate = LocalDate.parse("1/4/1982", formatter)
  val taxStartDate: LocalDate = LocalDate.parse("5/4/2015", formatter)
  val taxStartDatePlus18Months: LocalDate = LocalDate.parse("6/10/2016", formatter)
  val taxYearStartDate: LocalDate = LocalDate.parse("5/4/2016", formatter)
  val taxYearEndDate: LocalDate = LocalDate.parse("5/4/2017", formatter)
  private val pppReliefDeductionApplicableDate: LocalDate = LocalDate.parse("5/4/2020", formatter)

  def dateAfterStart(day: Int, month: Int, year: Int): Boolean = constructDate(day, month, year).isAfter(taxStartDate)

  def dateAfterStart(date: LocalDate): Boolean = date.isAfter(taxStartDate)

  def dateAfterStart(date: Option[DateModel]): Boolean =
    date.fold(false)(_.isDateAfter(DateModel(taxStartDate.getDayOfMonth, taxStartDate.getMonthValue, taxStartDate.getYear)))

  def dateBeforeLegislationStart(day: Int, month: Int, year: Int): Boolean = constructDate(day, month, year).isBefore(legislationDate)

  def dateBeforeLegislationStart(date: LocalDate): Boolean = date.isBefore(legislationDate)

  def dateAfter18Months(day: Int, month: Int, year: Int): Boolean = constructDate(day, month, year).isAfter(taxStartDatePlus18Months)

  def dateAfterOctober(date: LocalDate): Boolean = date.isAfter(taxStartDatePlus18Months)

  def dateInsideTaxYear(day: Int, month: Int, year: Int): Boolean =
    constructDate(day, month, year).isAfter(taxYearStartDate) && constructDate(day, month, year).isBefore(taxYearEndDate.plusDays(1))

  def taxYearStringToInteger(taxYear: String): Int = (taxYear.take(2) + taxYear.takeRight(2)).toInt

  case class PrivateResidenceReliefDateDetails(shortedPeriod: Boolean, months: Int, dateDeducted: Option[LocalDate])

  /**
   * Should Private Residence Relief be 9 months or 18
   * @param date
   * @return
   */
  def privateResidenceReliefMonthDeductionApplicable(date: Option[LocalDate]): PrivateResidenceReliefDateDetails = {

    if(date.isDefined) {
      val dateAfter = date.get.isAfter(pppReliefDeductionApplicableDate)
      val monthsToDeduct = dateAfter match {
        case true => 9
        case false => 18
      }
      val dateWithDeduction = Dates.dateMinusMonths(date, monthsToDeduct)
      PrivateResidenceReliefDateDetails(dateAfter, monthsToDeduct, dateWithDeduction)
    }else{
      PrivateResidenceReliefDateDetails(false, 18, None)
    }
  }

}
