/*
 * Copyright 2018 HM Revenue & Customs
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

    "use the acquisition costs for a post 31 March 1982 date" in {
      val result = TotalGainRequestConstructor.acquisitionCosts(Some(AcquisitionCostsModel(200)), None, AcquisitionDateModel(1, 4, 1990))

      result shouldBe "&acquisitionCosts=200"
    }

    "use the cost of valuation from the 31 March 1982 for a prior date" in {
      val result = TotalGainRequestConstructor.acquisitionCosts(None,
        Some(CostsAtLegislationStartModel("Yes", Some(100))), AcquisitionDateModel(1, 4, 1980))

      result shouldBe "&acquisitionCosts=100"
    }

    "return 0 costs for a prior date when not given" in {
      val result = TotalGainRequestConstructor.acquisitionCosts(Some(AcquisitionCostsModel(200)),
        Some(CostsAtLegislationStartModel("No", Some(100))), AcquisitionDateModel(1, 4, 1980))

      result shouldBe "&acquisitionCosts=0"
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

  "Calling .acquisitionDate" should {

    "produce a valid query string" in {
      val result = TotalGainRequestConstructor.acquisitionDate(AcquisitionDateModel(4, 5, 2013))

      result shouldBe "&acquisitionDate=2013-5-4"
    }
  }

  "Calling .disposalDate" should {

    "produce a valid query string" in {
      val result = TotalGainRequestConstructor.disposalDate(DisposalDateModel(15, 10, 2018))

      result shouldBe "&disposalDate=2018-10-15"
    }
  }

  "Calling .rebasedValues" should {

    "produce a valid query string with an acquisition date before tax start" in {
      val result = TotalGainRequestConstructor.rebasedValues(Some(RebasedValueModel(3000)),
        Some(RebasedCostsModel("Yes", Some(300))),
        ImprovementsModel("Yes", None, Some(30)),
        AcquisitionDateModel(5, 4, 2015))

      result shouldBe "&rebasedValue=3000&rebasedCosts=300&improvementsAfterTaxStarted=30"
    }

    "produce an empty query string with an acquisition date after tax start" in {
      val result = TotalGainRequestConstructor.rebasedValues(None,
        None,
        ImprovementsModel("Yes", None, None),
        AcquisitionDateModel(6, 4, 2015))

      result shouldBe ""
    }
  }

  "Calling .totalGainQuery" should {

    "produce a valid query string for a flat calculation" in {
      val model = TotalGainAnswersModel(DisposalDateModel(5, 10, 2016),
        SoldOrGivenAwayModel(true),
        Some(SoldForLessModel(false)),
        DisposalValueModel(1000),
        DisposalCostsModel(100),
        Some(HowBecameOwnerModel("Gifted")),
        Some(BoughtForLessModel(false)),
        AcquisitionValueModel(2000),
        AcquisitionCostsModel(200),
        AcquisitionDateModel(6, 4, 2015),
        None,
        None,
        ImprovementsModel("Yes", Some(10), None),
        None)

      val result = TotalGainRequestConstructor.totalGainQuery(model)

      result shouldBe "disposalValue=1000&disposalCosts=100&acquisitionValue=2000&acquisitionCosts=200&improvements=10" +
        "&disposalDate=2016-10-5&acquisitionDate=2015-4-6"
    }

    "produce a valid query string for multiple types calculation" in {
      val model = TotalGainAnswersModel(DisposalDateModel(5, 10, 2016),
        SoldOrGivenAwayModel(true),
        Some(SoldForLessModel(false)),
        DisposalValueModel(1000),
        DisposalCostsModel(100),
        Some(HowBecameOwnerModel("Gifted")),
        Some(BoughtForLessModel(false)),
        AcquisitionValueModel(2000),
        AcquisitionCostsModel(200),
        AcquisitionDateModel(4, 10, 2013),
        Some(RebasedValueModel(3000)),
        Some(RebasedCostsModel("Yes", Some(300))),
        ImprovementsModel("Yes", Some(10), Some(20)),
        None)

      val result = TotalGainRequestConstructor.totalGainQuery(model)

      result shouldBe "disposalValue=1000&disposalCosts=100&acquisitionValue=2000&acquisitionCosts=200&improvements=10" +
      "&rebasedValue=3000&rebasedCosts=300&improvementsAfterTaxStarted=20&disposalDate=2016-10-5&acquisitionDate=2013-10-4"
    }
  }
}
