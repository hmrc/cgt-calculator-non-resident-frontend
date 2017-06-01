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

package views

import controllers.helpers.FakeRequestHelper
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.NonResident.{CalculationElectionNoReliefs => messages}
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class CalculationElectionNoReliefsViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Calculation Election No Reliefs View" should {

    val rebasedLowestTaxOwed: Seq[(String, String, String, String, Option[String], Option[BigDecimal])] =
      Seq(
        ("flat", "1000", Messages("calc.calculationElectionNoReliefs.flatGain"), "", None, None),
        ("rebased", "0", Messages("calc.calculationElectionNoReliefs.rebasing"), "", None, None),
        ("time", "2000", Messages("calc.calculationElectionNoReliefs.straightLine"), "", None, None)
      )

    "have a h1 tag" which {

      s"has the text ${messages.title}" in {

      }
    }
  }
}
