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

import java.time.LocalDate

class DeductionDetailsConstructorSpec extends CommonPlaySpec with WithCommonFakeApplication with AssertHelpers {

  val noneOtherReliefs = TotalGainAnswersModel(
    DateModel(10, 10, 2010),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(150000),
    DisposalCostsModel(600),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    DateModel(1, 1, 2009),
    None,
    None,
    IsClaimingImprovementsModel(false),
    None,
    None
  )

  val noOtherReliefs = TotalGainAnswersModel(
    DateModel(10, 10, 2010),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(150000),
    DisposalCostsModel(600),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    DateModel(1, 1, 2009),
    None,
    None,
    IsClaimingImprovementsModel(false),
    None,
    Some(OtherReliefsModel(0))
  )

  val yesOtherReliefs = TotalGainAnswersModel(
    DateModel(10, 10, 2018),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(150000),
    DisposalCostsModel(600),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    DateModel(1, 1, 2016),
    Some(RebasedValueModel(1)),
    Some(RebasedCostsModel("No", None)),
    IsClaimingImprovementsModel(false),
    None,
    Some(OtherReliefsModel(1450))
  )

  val within18Months = TotalGainAnswersModel(
    DateModel(10, 10, 2016),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(150000),
    DisposalCostsModel(600),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    DateModel(10, 4, 2015),
    None,
    None,
    IsClaimingImprovementsModel(false),
    None,
    Some(OtherReliefsModel(1450))
  )

  val validDates = TotalGainAnswersModel(
    DateModel(10, 10, 2016),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(150000),
    DisposalCostsModel(600),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    DateModel(10, 2, 2015),
    None,
    None,
    IsClaimingImprovementsModel(false),
    None,
    Some(OtherReliefsModel(1450))
  )

  val edgeDatesJustDaysBefore = TotalGainAnswersModel(
    DateModel(2, 3, 2017),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(150000),
    DisposalCostsModel(600),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    DateModel(10, 4, 2015),
    None,
    None,
    IsClaimingImprovementsModel(false),
    None,
    Some(OtherReliefsModel(1450))
  )

  val acquisitionDateAfterStart = TotalGainAnswersModel(
    DateModel(10, 10, 2018),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(150000),
    DisposalCostsModel(600),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    DateModel(10, 2, 2016),
    None,
    None,
    IsClaimingImprovementsModel(false),
    None,
    Some(OtherReliefsModel(1450))
  )

  val disposalDateWithin18Months = TotalGainAnswersModel(
    DateModel(10, 4, 2016),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(150000),
    DisposalCostsModel(600),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    DateModel(10, 2, 1990),
    None,
    None,
    IsClaimingImprovementsModel(false),
    None,
    Some(OtherReliefsModel(1450))
  )

  val yesPRRModel = PrivateResidenceReliefModel("Yes", Some(2), Some(3))

  private def assertExpectedResult[T](option: Option[T])(test: T => Unit) = assertOption("expected option is None")(option)(test)

  "Calling .deductionDetailsRows" when {

    "provided with reliefs, prr and property lived in" should {
      lazy val result = DeductionDetailsConstructor.deductionDetailsRows(validDates, Some(yesPRRModel), Some(PropertyLivedInModel(true)))

      "have a sequence of size 3" in {
        result.size shouldBe 4
      }

      "return a sequence with a property lived in question answer" in {
        result.contains(DeductionDetailsConstructor.propertyLivedInQuestionRow(Some(PropertyLivedInModel(true))))
      }

      "return a sequence with a prr question answer" in {
        result.contains(DeductionDetailsConstructor.privateResidenceReliefQuestionRow(Some(yesPRRModel)).get)
      }

      "return a sequence with the days claimed answer" in {
        result.contains(DeductionDetailsConstructor.privateResidenceReliefDaysClaimedBeforeRow(Some(yesPRRModel), validDates).get)
      }

      "return a sequence with the days claimed after answer" in {
        result.contains(DeductionDetailsConstructor.privateResidenceReliefDaysClaimedAfterRow(Some(yesPRRModel), validDates).get)
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
        val prrModel = PrivateResidenceReliefModel("Yes", None, None)
        lazy val result = DeductionDetailsConstructor.privateResidenceReliefQuestionRow(Some(prrModel))

        "return some value" in {
          result.isDefined shouldBe true
        }

        "return an id of nr:privateResidenceRelief" in {
          assertExpectedResult[QuestionAnswerModel[String]](result)(_.id shouldBe "nr:privateResidenceRelief")
        }

        "return a value of 'Yes'" in {
          assertExpectedResult[QuestionAnswerModel[String]](result)(_.data shouldBe "Yes")
        }

        "return a question for Private Residence Relief" in {
          assertExpectedResult[QuestionAnswerModel[String]](result)(_.question shouldBe "calc.privateResidenceRelief.question")
        }

        "return a link to the Private Residence Relief page" in {
          assertExpectedResult[QuestionAnswerModel[String]](result)(_.link shouldBe
            Some(controllers.routes.PrivateResidenceReliefController.privateResidenceRelief.url))
        }
      }
    }
  }

  "Calling .privateResidenceReliefDaysClaimedBeforeRow" when {

    "provided with no privateResidenceReliefModel" should {
      lazy val result = DeductionDetailsConstructor.privateResidenceReliefDaysClaimedBeforeRow(None, validDates)

      "return a None" in {
        result shouldBe None
      }
    }

    "provided with an answer of 'No' to prr" should {
      lazy val result = DeductionDetailsConstructor.privateResidenceReliefDaysClaimedBeforeRow(Some(PrivateResidenceReliefModel("No", None, None)), validDates)

      "return a None" in {
        result shouldBe None
      }
    }

    "provided with an an acquisition date and disposal date within 18 months of each other" should {
      lazy val result = DeductionDetailsConstructor.privateResidenceReliefDaysClaimedBeforeRow(
        Some(PrivateResidenceReliefModel("Yes", Some(4), None)), within18Months)

      "return a None" in {
        result shouldBe None
      }
    }

    "provided with a valid value and conditions to use it" should {
      lazy val result = DeductionDetailsConstructor.privateResidenceReliefDaysClaimedBeforeRow(
        Some(PrivateResidenceReliefModel("Yes", Some(4), None)), validDates)

      "return some value" in {
        result.isDefined shouldBe true
      }

      "return an id of nr:privateResidenceRelief-daysClaimed" in {
        assertExpectedResult[QuestionAnswerModel[String]](result)(_.id shouldBe "nr:privateResidenceRelief-daysClaimed")
      }

      "return a value of '4'" in {
        assertExpectedResult[QuestionAnswerModel[String]](result)(_.data shouldBe "4")
      }

      "return a question for Private Residence Relief" in {
        assertExpectedResult[QuestionAnswerModel[String]](result)(_.question shouldBe "calc.privateResidenceRelief.firstQuestion")
      }

      "return a link to the Private Residence Relief page" in {
        assertExpectedResult[QuestionAnswerModel[String]](result)(_.link shouldBe
          Some(controllers.routes.PrivateResidenceReliefController.privateResidenceRelief.url))
      }
    }

    "provided with an acquisition date after 6th April 2015 and disposal date over 18 months later" should {
      s"have the question message ${}" in {
        lazy val result = DeductionDetailsConstructor.privateResidenceReliefDaysClaimedBeforeRow(
          Some(PrivateResidenceReliefModel("Yes", Some(4), None)), edgeDatesJustDaysBefore)

        result.get.question shouldBe "calc.privateResidenceRelief.questionFlat"
        result.get.oDate shouldBe Some(LocalDate.of(2015, 9, 2))
      }
    }
  }

  "Calling privateResidenceReliefDaysClaimedAfterRow" when {

    "provided with no privateResidenceReliefModel" should {
      lazy val result = DeductionDetailsConstructor.privateResidenceReliefDaysClaimedAfterRow(None, validDates)

      "return a None" in {
        result shouldBe None
      }
    }

    "provided with an answer of 'No' to prr" should {
      lazy val result = DeductionDetailsConstructor.privateResidenceReliefDaysClaimedAfterRow(Some(PrivateResidenceReliefModel("No", None, None)), validDates)

      "return a None" in {
        result shouldBe None
      }
    }

    "provided with an acquisition date after the start" should {
      lazy val result = DeductionDetailsConstructor.privateResidenceReliefDaysClaimedAfterRow(
        Some(PrivateResidenceReliefModel("Yes", None, Some(3))), acquisitionDateAfterStart)

      "return a None" in {
        result shouldBe None
      }
    }

    "provided with a disposal date before the 18 month rebased period" should {
      lazy val result = DeductionDetailsConstructor.privateResidenceReliefDaysClaimedAfterRow(
        Some(PrivateResidenceReliefModel("Yes", None, Some(3))), disposalDateWithin18Months)

      "return a None" in {
        result shouldBe None
      }
    }

    "provided with no acquisition date and no rebased value" should {
      lazy val result = DeductionDetailsConstructor.privateResidenceReliefDaysClaimedAfterRow(
        Some(PrivateResidenceReliefModel("Yes", None, Some(3))), noOtherReliefs)

      "return a None" in {
        result shouldBe None
      }
    }

    "provided with a rebased value and no acquisition date" should {
      lazy val result = DeductionDetailsConstructor.privateResidenceReliefDaysClaimedAfterRow(
        Some(PrivateResidenceReliefModel("Yes", None, Some(3))), yesOtherReliefs)

      "return some value" in {
        result.isDefined shouldBe true
      }

      "return an id of nr:privateResidenceRelief-daysClaimedAfter" in {
        assertExpectedResult[QuestionAnswerModel[String]](result)(_.id shouldBe "nr:privateResidenceRelief-daysClaimedAfter")
      }

      "return a value of '3'" in {
        assertExpectedResult[QuestionAnswerModel[String]](result)(_.data shouldBe "3")
      }

      "return a question for Private Residence Relief" in {
        assertExpectedResult[QuestionAnswerModel[String]](result)(_.question shouldBe "calc.privateResidenceRelief.questionBetween")
        assertExpectedResult[QuestionAnswerModel[String]](result)(_.oDate shouldBe Some(LocalDate.of(2017, 4, 10)))
      }

      "return a link to the Private Residence Relief page" in {
        assertExpectedResult[QuestionAnswerModel[String]](result)(_.link shouldBe
          Some(controllers.routes.PrivateResidenceReliefController.privateResidenceRelief.url))
      }
    }

    "provided with a valid acquisition date" should {
      lazy val result = DeductionDetailsConstructor.privateResidenceReliefDaysClaimedAfterRow(
        Some(PrivateResidenceReliefModel("Yes", None, Some(3))), validDates)

      "return some value" in {
        result.isDefined shouldBe true
      }

      "return an id of nr:privateResidenceRelief-daysClaimedAfter" in {
        assertExpectedResult[QuestionAnswerModel[String]](result)(_.id shouldBe "nr:privateResidenceRelief-daysClaimedAfter")
      }

      "return a value of '3'" in {
        assertExpectedResult[QuestionAnswerModel[String]](result)(_.data shouldBe "3")
      }

      "return a question for Private Residence Relief" in {
        assertExpectedResult[QuestionAnswerModel[String]](result)(_.question shouldBe "calc.privateResidenceRelief.questionBetween")
        assertExpectedResult[QuestionAnswerModel[String]](result)(_.oDate shouldBe Some(LocalDate.of(2015, 4, 10)))
      }

      "return a link to the Private Residence Relief page" in {
        assertExpectedResult[QuestionAnswerModel[String]](result)(_.link shouldBe
          Some(controllers.routes.PrivateResidenceReliefController.privateResidenceRelief.url))
      }
    }
  }
}
