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

import common.{CommonPlaySpec, WithCommonFakeApplication}
import constructors.helpers.AssertHelpers
import models._

class DeductionDetailsConstructorSpec extends CommonPlaySpec with WithCommonFakeApplication with AssertHelpers {

  val noneOtherReliefs: TotalGainAnswersModel = TotalGainAnswersModel(
    DateModel(10, 10, 2010),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(150000),
    DisposalCostsModel(600),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    Some(AcquisitionCostsModel(200)),
    DateModel(1, 1, 2009),
    None,
    None,
    IsClaimingImprovementsModel(false),
    None,
    None
  )

  val noOtherReliefs: TotalGainAnswersModel = TotalGainAnswersModel(
    DateModel(10, 10, 2010),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(150000),
    DisposalCostsModel(600),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    Some(AcquisitionCostsModel(200)),
    DateModel(1, 1, 2009),
    None,
    None,
    IsClaimingImprovementsModel(false),
    None,
    Some(OtherReliefsModel(0))
  )

  val yesOtherReliefs: TotalGainAnswersModel = TotalGainAnswersModel(
    DateModel(10, 10, 2018),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(150000),
    DisposalCostsModel(600),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    Some(AcquisitionCostsModel(200)),
    DateModel(1, 1, 2016),
    Some(RebasedValueModel(1)),
    Some(RebasedCostsModel("No", None)),
    IsClaimingImprovementsModel(false),
    None,
    Some(OtherReliefsModel(1450))
  )

  val within18Months: TotalGainAnswersModel = TotalGainAnswersModel(
    DateModel(10, 10, 2016),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(150000),
    DisposalCostsModel(600),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    Some(AcquisitionCostsModel(200)),
    DateModel(10, 4, 2015),
    None,
    None,
    IsClaimingImprovementsModel(false),
    None,
    Some(OtherReliefsModel(1450))
  )

  val validDates: TotalGainAnswersModel = TotalGainAnswersModel(
    DateModel(10, 10, 2016),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(150000),
    DisposalCostsModel(600),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    Some(AcquisitionCostsModel(200)),
    DateModel(10, 2, 2015),
    None,
    None,
    IsClaimingImprovementsModel(false),
    None,
    Some(OtherReliefsModel(1450))
  )

  val edgeDatesJustDaysBefore: TotalGainAnswersModel = TotalGainAnswersModel(
    DateModel(2, 3, 2017),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(150000),
    DisposalCostsModel(600),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    Some(AcquisitionCostsModel(200)),
    DateModel(10, 4, 2015),
    None,
    None,
    IsClaimingImprovementsModel(false),
    None,
    Some(OtherReliefsModel(1450))
  )

  val acquisitionDateAfterStart: TotalGainAnswersModel = TotalGainAnswersModel(
    DateModel(10, 10, 2018),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(150000),
    DisposalCostsModel(600),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    Some(AcquisitionCostsModel(200)),
    DateModel(10, 2, 2016),
    None,
    None,
    IsClaimingImprovementsModel(false),
    None,
    Some(OtherReliefsModel(1450))
  )

  val disposalDateWithin18Months: TotalGainAnswersModel = TotalGainAnswersModel(
    DateModel(10, 4, 2016),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(150000),
    DisposalCostsModel(600),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    Some(AcquisitionCostsModel(200)),
    DateModel(10, 2, 1990),
    None,
    None,
    IsClaimingImprovementsModel(false),
    None,
    Some(OtherReliefsModel(1450))
  )

  val yesPRRModel: PrivateResidenceReliefModel = PrivateResidenceReliefModel("Yes", Some(2))

  private def assertExpectedResult[T](option: Option[T])(test: T => Unit): Unit = assertOption("expected option is None")(option)(test)

  "Calling .deductionDetailsRows" when {

    "provided with reliefs, prr and property lived in" should {
      lazy val result = DeductionDetailsConstructor.deductionDetailsRows(validDates, Some(yesPRRModel), Some(PropertyLivedInModel(true)))

      "have a sequence of size 3" in {
        result.size shouldBe 3
      }

      "return a sequence with a property lived in question answer" in {
        result.contains(DeductionDetailsConstructor.propertyLivedInQuestionRow(Some(PropertyLivedInModel(true))))
      }

      "return a sequence with a prr question answer" in {
        result.contains(DeductionDetailsConstructor.privateResidenceReliefQuestionRow(Some(yesPRRModel)).get)
      }

      "return a sequence with the days claimed answer" in {
        result.contains(DeductionDetailsConstructor.privateResidenceReliefAmountRow(Some(yesPRRModel), validDates).get)
      }
    }
  }

  "Calling .propertyLivedInQuestionRow" when {
    "provided with no propertyLivedInModel" should {
      "return an empty sequence" in {
        lazy val result = DeductionDetailsConstructor.propertyLivedInQuestionRow(None)

        result shouldBe Seq()
      }
    }

    "provided with a propertyLivedInModel with a false value" should {

      lazy val result = DeductionDetailsConstructor.propertyLivedInQuestionRow(Some(PropertyLivedInModel(false)))

      "has a head of type QuestionAnswerModel[String]" in {
        result.head shouldBe an[QuestionAnswerModel[String]]
      }

      "and a length of 1" in {
        result.length shouldBe 1
      }
    }

    "provided with a propertyLivedInModel with a true value" should {
      "return a sequence " which {
        lazy val result = DeductionDetailsConstructor.propertyLivedInQuestionRow(Some(PropertyLivedInModel(true)))

        "has a head of type QuestionAnswerModel[String]" in {
          result.head shouldBe an[QuestionAnswerModel[String]]
        }

        "and a length of 1" in {
          result.length shouldBe 1
        }
      }
    }
  }

  "Calling .privateResidenceReliefQuestionRow" when {

    "provided with no privateResidenceRelief model" should {
      lazy val result = DeductionDetailsConstructor.privateResidenceReliefQuestionRow(None)

      "return a None" in {
        result shouldBe None
      }
    }

    "provided with a privateResidenceRelief model" should {

      "with answer 'Yes'" should {
        val prrModel = PrivateResidenceReliefModel("Yes", None)
        lazy val result = DeductionDetailsConstructor.privateResidenceReliefQuestionRow(Some(prrModel))

        "return some value" in {
          result.isDefined shouldBe true
        }

        "return an id of nr:privateResidenceRelief" in {
          assertExpectedResult(result)(_.id shouldBe "nr:privateResidenceRelief")
        }

        "return a value of 'Yes'" in {
          assertExpectedResult(result)(_.data shouldBe "Yes")
        }

        "return a question for Private Residence Relief" in {
          assertExpectedResult(result)(_.question shouldBe "calc.privateResidenceRelief.question")
        }

        "return a link to the Private Residence Relief page" in {
          assertExpectedResult(result)(_.link shouldBe
            Some(controllers.routes.PrivateResidenceReliefController.privateResidenceRelief.url))
        }
      }
    }
  }

  "Calling .privateResidenceReliefprrClaimedBeforeRow" when {

    "provided with no privateResidenceReliefModel" should {
      lazy val result = DeductionDetailsConstructor.privateResidenceReliefAmountRow(None, validDates)

      "return a None" in {
        result shouldBe None
      }
    }

    "provided with an answer of 'No' to prr" should {
      lazy val result = DeductionDetailsConstructor.privateResidenceReliefAmountRow(Some(PrivateResidenceReliefModel("No", None)), validDates)

      "return a None" in {
        result shouldBe None
      }
    }

    "provided with an an acquisition date and disposal date within 18 months of each other" should {
      lazy val result = DeductionDetailsConstructor.privateResidenceReliefAmountRow(
        Some(PrivateResidenceReliefModel("Yes", Some(4))), within18Months)

      "return a None" in {
        result shouldBe None
      }
    }

    "provided with a valid value and conditions to use it" should {
      lazy val result = DeductionDetailsConstructor.privateResidenceReliefAmountRow(
        Some(PrivateResidenceReliefModel("Yes", Some(4))), validDates)

      "return some value" in {
        result.isDefined shouldBe true
      }

      "return an id of nr:privateResidenceRelief-prrClaimed" in {
        assertExpectedResult(result)(_.id shouldBe "nr:privateResidenceRelief-prrClaimed")
      }

      "return a value of '4'" in {
        assertExpectedResult(result)(_.data.toString() shouldBe "4")
      }

      "return a question for Private Residence Relief" in {
        assertExpectedResult(result)(_.question shouldBe "calc.privateResidenceReliefValue.title")
      }

      "return a link to the Private Residence Relief page" in {
        assertExpectedResult(result)(_.link shouldBe
          Some(controllers.routes.PrivateResidenceReliefController.privateResidenceRelief.url))
      }
    }

    "provided with an acquisition date after 6th April 2015 and disposal date over 18 months later" should {
      s"have the question message" in {
        lazy val result = DeductionDetailsConstructor.privateResidenceReliefAmountRow(
          Some(PrivateResidenceReliefModel("Yes", Some(4))), edgeDatesJustDaysBefore)

        result.get.question shouldBe "calc.privateResidenceReliefValue.title"
        assertExpectedResult(result)(_.data.toString() shouldBe "4")
      }
    }
  }
}
