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

package constructors

import models._
import uk.gov.hmrc.play.test.UnitSpec

class TotalGainRequestConstructorSpec extends UnitSpec {

  "Calling .disposalValue" should {

    "produce a valid query string" in {
      val result = TotalGainRequestConstructor.disposalValue(DisposalValueModel(1000))

      result shouldBe "disposalValue=1000"
    }
  }

  "Calling .disposalCosts" should {

    "produce a valid query string" in {
      val result = TotalGainRequestConstructor.disposalCosts(DisposalCostsModel(100))

      result shouldBe "&disposalCosts=100"
    }
  }

  "Calling .acquisitionValue" should {

    "produce a valid query string" in {
      val result = TotalGainRequestConstructor.acquisitionValue(AcquisitionValueModel(2000))

      result shouldBe "&acquisitionValue=2000"
    }
  }

  "Calling .acquisitionCosts" should {

    "produce a valid query string" in {
      val result = TotalGainRequestConstructor.acquisitionCosts(AcquisitionCostsModel(200))

      result shouldBe "&acquisitionCosts=200"
    }
  }

  "Calling .improvements" should {

    "produce a valid query string with a provided value" in {
      val result = TotalGainRequestConstructor.improvements(ImprovementsModel("Yes", Some(300), None))

      result shouldBe "&improvements=300"
    }

    "produce a valid query string with no provided value" in {
      val result = TotalGainRequestConstructor.improvements(ImprovementsModel("No", Some(300), None))

      result shouldBe ""
    }

    "produce a valid query string with a None provided value" in {
      val result = TotalGainRequestConstructor.improvements(ImprovementsModel("Yes", None, None))

      result shouldBe ""
    }
  }

  "Calling .rebasedCosts" should {

    "produce a valid query string with a provided value" in {
      val result = TotalGainRequestConstructor.rebasedCosts(RebasedCostsModel("Yes", Some(300)))

      result shouldBe "&rebasedCosts=300"
    }

    "produce a valid query string with no provided value" in {
      val result = TotalGainRequestConstructor.rebasedCosts(RebasedCostsModel("No", Some(300)))

      result shouldBe ""
    }

    "produce a valid query string with a None provided value" in {
      val result = TotalGainRequestConstructor.rebasedCosts(RebasedCostsModel("Yes", None))

      result shouldBe ""
    }
  }

  "Calling .improvementsAfterTaxStarted" should {

    "produce a valid query string with a provided value" in {
      val result = TotalGainRequestConstructor.improvementsAfterTaxStarted(ImprovementsModel("Yes", None, Some(300)))

      result shouldBe "&improvementsAfterTaxStarted=300"
    }

    "produce a valid query string with no provided value" in {
      val result = TotalGainRequestConstructor.improvementsAfterTaxStarted(ImprovementsModel("No", None, Some(300)))

      result shouldBe ""
    }

    "produce a valid query string with a None provided value" in {
      val result = TotalGainRequestConstructor.improvementsAfterTaxStarted(ImprovementsModel("Yes", None, None))

      result shouldBe ""
    }
  }

  "Calling .timeApportionedValues" should {

    "produce a valid query string with both dates provided" in {
      val result = TotalGainRequestConstructor.timeApportionedValues(
        DisposalDateModel(15, 10, 2018),
        AcquisitionDateModel("Yes", Some(4), Some(5), Some(2013)))

      result shouldBe "&disposalDate=2018-10-15&acquisitionDate=2013-5-4"
    }

    "produce an empty string with no acquisition date" in {
      val result = TotalGainRequestConstructor.timeApportionedValues(
        DisposalDateModel(15, 10, 2018),
        AcquisitionDateModel("No", None, None, None))

      result shouldBe "&disposalDate=2018-10-15"
    }

    "produce an empty string with missing acquisition date" in {
      val result = TotalGainRequestConstructor.timeApportionedValues(
        DisposalDateModel(15, 10, 2018),
        AcquisitionDateModel("Yes", None, None, None))

      result shouldBe "&disposalDate=2018-10-15"
    }

    "produce an empty string with an acquisition date after tax start" in {
      val result = TotalGainRequestConstructor.timeApportionedValues(
        DisposalDateModel(15, 10, 2018),
        AcquisitionDateModel("Yes", Some(4), Some(5), Some(2016)))

      result shouldBe "&disposalDate=2018-10-15"
    }
  }

  "Calling .rebasedValues" should {

    "produce a valid query string with an acquisition date before tax start and rebased value" in {
      val result = TotalGainRequestConstructor.rebasedValues(Some(RebasedValueModel(Some(3000))),
        Some(RebasedCostsModel("Yes", Some(300))),
        ImprovementsModel("Yes", None, Some(30)),
        AcquisitionDateModel("Yes", Some(1), Some(1), Some(2013)))

      result shouldBe "&rebasedValue=3000&rebasedCosts=300&improvementsAfterTaxStarted=30"
    }

    "produce an empty query string with no rebased value" in {
      val result = TotalGainRequestConstructor.rebasedValues(Some(RebasedValueModel(None)),
        None,
        ImprovementsModel("Yes", None, None),
        AcquisitionDateModel("Yes", Some(1), Some(1), Some(2013)))

      result shouldBe ""
    }

    "produce an empty query string with an acquisition date after tax start date" in {
      val result = TotalGainRequestConstructor.rebasedValues(None,
        None,
        ImprovementsModel("Yes", None, Some(30)),
        AcquisitionDateModel("Yes", Some(1), Some(1), Some(2016)))

      result shouldBe ""
    }

    "produce a valid query string with no acquisition date and with a rebased value" in {
      val result = TotalGainRequestConstructor.rebasedValues(Some(RebasedValueModel(Some(3000))),
        Some(RebasedCostsModel("Yes", Some(300))),
        ImprovementsModel("Yes", None, Some(30)),
        AcquisitionDateModel("No", None, None, None))

      result shouldBe "&rebasedValue=3000&rebasedCosts=300&improvementsAfterTaxStarted=30"
    }

    "produce an empty query string with no acquisition date or rebased value" in {
      val result = TotalGainRequestConstructor.rebasedValues(Some(RebasedValueModel(None)),
        None,
        ImprovementsModel("Yes", None, Some(30)),
        AcquisitionDateModel("No", None, None, None))

      result shouldBe ""
    }
  }

  "Calling .totalGainQuery" should {

    "produce a valid query string" in {
      val model = TotalGainAnswersModel(DisposalDateModel(5, 10, 2016),
        SoldOrGivenAwayModel(true),
        Some(SoldForLessModel(false)),
        DisposalValueModel(1000),
        DisposalCostsModel(100),
        Some(HowBecameOwnerModel("Gifted")),
        Some(BoughtForLessModel(false)),
        AcquisitionValueModel(2000),
        AcquisitionCostsModel(200),
        AcquisitionDateModel("Yes", Some(4), Some(10), Some(2013)),
        Some(RebasedValueModel(Some(3000))),
        Some(RebasedCostsModel("Yes", Some(300))),
        ImprovementsModel("Yes", Some(10), Some(20)),
        None)

      val result = TotalGainRequestConstructor.totalGainQuery(model)

      result shouldBe "disposalValue=1000&disposalCosts=100&acquisitionValue=2000&acquisitionCosts=200&improvements=10" +
      "&rebasedValue=3000&rebasedCosts=300&improvementsAfterTaxStarted=20&disposalDate=2016-10-5&acquisitionDate=2013-10-4"
    }
  }
}
