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

package common

import java.time.LocalDate

import common.Dates.{TemplateImplicits, formatter}
import controllers.helpers.FakeRequestHelper
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.Lang
import play.api.mvc.MessagesControllerComponents

import scala.concurrent.ExecutionContext

class DatesSpec extends CommonPlaySpec with GuiceOneAppPerSuite with MockitoSugar with FakeRequestHelper {
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  implicit val ec = fakeApplication.injector.instanceOf[ExecutionContext]
  lazy val cyMockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(Seq(
    Lang("cy")
  ))

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

  "Calling taxYearOfDateLongHand" should {
    "when called with 2016/4/6 return 2016 to 2017" in {
      Dates.taxYearOfDateLongHand(LocalDate.of(2016, 4, 6)) shouldBe "2016 to 2017"
    }

    "when called with a date of 2016/4/5 return 2015 to 2016" in {
      Dates.taxYearOfDateLongHand(LocalDate.of(2016, 4, 5)) shouldBe "2015 to 2016"
    }

    "when called with 1999/4/6 return 1999 to 2000" in {
      Dates.taxYearOfDateLongHand(LocalDate.of(1999, 4, 6)) shouldBe "1999 to 2000"
    }

    "when called with 1999/4/5 return 1999 to 2000" in {
      Dates.taxYearOfDateLongHand(LocalDate.of(1999, 4, 5)) shouldBe "1998 to 1999"
    }
  }

  "localFormat" should {
    import TemplateImplicits._
    "format an English date" in {
      implicit val lang: Lang = Lang("en")
      val date = LocalDate.of(2018, 3, 19)
      date.localFormat("d MMMM yyyy") shouldBe "19 March 2018"
    }
    "format a Welsh date" in {
      val date = LocalDate.of(2014, 11, 22)
      implicit val lang: Lang = Lang("cy")
      date.localFormat("d MMMM yyyy")(lang, cyMockMessage) shouldBe "22 Tachwedd 2014"
    }
    "format a Spanish date" in {
      val date = LocalDate.of(1999, 1, 12)
      implicit val lang: Lang = Lang("es")
      date.localFormat("d MMMM yyyy") shouldBe "12 enero 1999"
    }
  }
}
