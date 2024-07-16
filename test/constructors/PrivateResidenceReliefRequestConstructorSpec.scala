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

package constructors

import common.CommonPlaySpec
import models._
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table

class PrivateResidenceReliefRequestConstructorSpec extends CommonPlaySpec{

  import PrivateResidenceReliefRequestConstructor._

  val modelDatesWithin18Months = TotalGainAnswersModel(
    DateModel(7, 7, 2016),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(10000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    DateModel(7, 1, 2015),
    None,
    None,
    IsClaimingImprovementsModel(false),
    None,
    None
  )

  val modelDatesAcquisitionDateAfterStart = TotalGainAnswersModel(
    DateModel(10, 10, 2016),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(10000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    DateModel(10, 10, 2015),
    None,
    None,
    IsClaimingImprovementsModel(false),
    None,
    None
  )

  val modelWithValidDates = TotalGainAnswersModel(
    DateModel(10, 10, 2016),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(10000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    DateModel(10, 10, 2000),
    None,
    None,
    IsClaimingImprovementsModel(false),
    None,
    None
  )

  val modelWithRebasedValue = TotalGainAnswersModel(
    DateModel(10, 10, 2016),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(10000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    DateModel(10, 10, 2000),
    Some(RebasedValueModel(1000)),
    None,
    IsClaimingImprovementsModel(false),
    None,
    None
  )

  private def d(x: Int) = BigDecimal(x)

  "Calling the privateResidenceReliefQuery method" should {

    "prr claimed, property lived in and dates are valid" in {
      val fractions =
        Table(
          ("claiming", "amount",    "livedIn",   "expected"),
          ("Yes",      Some(d(4)),  Some(true),  "&prrClaimed=4"),
          ("Yes",      Some(d(42)), Some(true),  "&prrClaimed=42"),
          ("Yes",      Some(d(4)),  Some(true),  "&prrClaimed=4"),
          ("Yes",      None,        Some(true),  ""),
          ("No",       None,        Some(true),  ""),
          ("Yes",      Some(d(4)),  Some(false), ""),
          ("Yes",      Some(d(4)),  None,        ""),
          ("No",       None,        None,        ""),
        )
      forAll (fractions) { (claiming, amount, livedIn, expected) =>
        val result = privateResidenceReliefQuery(Some(PrivateResidenceReliefModel(claiming, amount)), livedIn.map(PropertyLivedInModel(_)))
        result shouldBe expected
      }
    }
  }
}
