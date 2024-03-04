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

import common.nonresident.{Flat, Rebased}
import models._

object TestModels {

  val businessScenarioFiveModel = TotalGainAnswersModel(
    DateModel(5, 6, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(true)),
    DisposalValueModel(950000),
    DisposalCostsModel(15000),
    Some(HowBecameOwnerModel("Bought")),
    Some(BoughtForLessModel(false)),
    AcquisitionValueModel(1250000),
    AcquisitionCostsModel(20000),
    DateModel(1, 1, 2016),
    None,
    None,
    IsClaimingImprovementsModel(false),
    ImprovementsModel(),
    Some(OtherReliefsModel(0))
  )

  val sumModelFlat = SummaryModel(
    CurrentIncomeModel(1000),
    Some(PersonalAllowanceModel(11100)),
    OtherPropertiesModel("No"),
    None,
    DateModel(5, 4, 2016),
    AcquisitionValueModel(100000),
    None,
    None,
    ImprovementsModel(),
    DateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(0),
    DisposalCostsModel(0),
    CalculationElectionModel(Flat),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    None
  )

  val summaryFlatNoIncomeOtherPropNo = SummaryModel(
    CurrentIncomeModel(0),
    None,
    OtherPropertiesModel("No"),
    None,
    DateModel(5, 4, 2016),
    AcquisitionValueModel(100000),
    None,
    None,
    ImprovementsModel(),
    DateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(0),
    DisposalCostsModel(0),
    CalculationElectionModel(Flat),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    None
  )

  val summaryFlatWithoutAEA = SummaryModel(
    CurrentIncomeModel(1000),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No"),
    None,
    DateModel(5, 4, 2016),
    AcquisitionValueModel(100000),
    None,
    None,
    ImprovementsModel(8000),
    DateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(300),
    DisposalCostsModel(600),
    CalculationElectionModel(Flat),
    OtherReliefsModel(999),
    OtherReliefsModel(888),
    OtherReliefsModel(777),
    None
  )

  val summaryFlatWithAEA = SummaryModel(
    CurrentIncomeModel(1000),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("Yes"),
    Some(AnnualExemptAmountModel(1500)),
    DateModel(5, 4, 2016),
    AcquisitionValueModel(100000),
    None,
    None,
    ImprovementsModel(),
    DateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(0),
    DisposalCostsModel(0),
    CalculationElectionModel(Flat),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    None
  )

  val summaryIndividualImprovementsNoRebasedModel = SummaryModel (
    CurrentIncomeModel(1000),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No"),
    None,
    DateModel(9, 9, 1999),
    AcquisitionValueModel(100000),
    None,
    None,
    ImprovementsModel(8000),
    DateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(300),
    DisposalCostsModel(600),
    CalculationElectionModel(Flat),
    OtherReliefsModel(999),
    OtherReliefsModel(888),
    OtherReliefsModel(777),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val summaryImprovementsWithRebasedModel = SummaryModel(
    CurrentIncomeModel(1000),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No"),
    None,
    DateModel(9, 9, 1999),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(1000)),
    None,
    ImprovementsModel(8000, Some(1000)),
    DateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(300),
    DisposalCostsModel(600),
    CalculationElectionModel(Flat),
    OtherReliefsModel(999),
    OtherReliefsModel(888),
    OtherReliefsModel(777),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val summaryRebased = SummaryModel(
    CurrentIncomeModel(1000),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No"),
    None,
    DateModel(9, 9, 1999),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(150000)),
    Some(RebasedCostsModel("Yes", Some(1000))),
    ImprovementsModel(2000, Some(3000)),
    DateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(300),
    DisposalCostsModel(600),
    CalculationElectionModel(Rebased),
    OtherReliefsModel(999),
    OtherReliefsModel(888),
    OtherReliefsModel(777),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val summaryRebasedNoImprovements = SummaryModel(
    CurrentIncomeModel(1000),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No"),
    None,
    DateModel(9, 9, 1999),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(150000)),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel(),
    DateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(300),
    DisposalCostsModel(600),
    CalculationElectionModel(Rebased),
    OtherReliefsModel(999),
    OtherReliefsModel(888),
    OtherReliefsModel(0),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val summaryRebasedNoneImprovements = SummaryModel(
    CurrentIncomeModel(1000),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No"),
    None,
    DateModel(9, 9, 1999),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(150000)),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel(),
    DateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(300),
    DisposalCostsModel(600),
    CalculationElectionModel(Rebased),
    OtherReliefsModel(999),
    OtherReliefsModel(888),
    OtherReliefsModel(0),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val summaryPRRAcqDateAfterAndDisposalDateBefore = SummaryModel(
    CurrentIncomeModel(1000),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No"),
    None,
    DateModel(9, 9, 2016),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(150000)),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel(),
    DateModel(10, 10, 2018),
    DisposalValueModel(150000),
    AcquisitionCostsModel(300),
    DisposalCostsModel(600),
    CalculationElectionModel(Rebased),
    OtherReliefsModel(999),
    OtherReliefsModel(888),
    OtherReliefsModel(0),
    Some(PrivateResidenceReliefModel("Yes", Some(100), Some(50)))
  )

  val summaryPRRAcqDateAfterAndNoRebased = SummaryModel(
    CurrentIncomeModel(1000),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No"),
    None,
    DateModel(9, 9, 2012),
    AcquisitionValueModel(100000),
    None,
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel(),
    DateModel(10, 10, 2018),
    DisposalValueModel(150000),
    AcquisitionCostsModel(300),
    DisposalCostsModel(600),
    CalculationElectionModel(Rebased),
    OtherReliefsModel(999),
    OtherReliefsModel(888),
    OtherReliefsModel(0),
    Some(PrivateResidenceReliefModel("Yes", Some(100), Some(50)))
  )

  val summaryPRRAcqDateAfterAndDisposalDateAfter = SummaryModel(
    CurrentIncomeModel(1000),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No"),
    None,
    DateModel(9, 9, 2012),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(1000)),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel(),
    DateModel(10, 10, 2018),
    DisposalValueModel(150000),
    AcquisitionCostsModel(300),
    DisposalCostsModel(600),
    CalculationElectionModel(Rebased),
    OtherReliefsModel(999),
    OtherReliefsModel(888),
    OtherReliefsModel(0),
    Some(PrivateResidenceReliefModel("Yes", Some(100), Some(50)))
  )

  val summaryPRRAcqDateAfterAndDisposalDateAfterWithRebased = SummaryModel(
    CurrentIncomeModel(1000),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No"),
    None,
    DateModel(9, 9, 2016),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(1000)),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel(),
    DateModel(10, 10, 2018),
    DisposalValueModel(150000),
    AcquisitionCostsModel(300),
    DisposalCostsModel(600),
    CalculationElectionModel(Rebased),
    OtherReliefsModel(999),
    OtherReliefsModel(888),
    OtherReliefsModel(0),
    Some(PrivateResidenceReliefModel("Yes", Some(100), Some(50)))
  )

  val summaryPRRAcqDateBeforeAndDisposalDateAfterWithRebased = SummaryModel(
    CurrentIncomeModel(1000),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No"),
    None,
    DateModel(9, 9, 2012),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(1000)),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel(),
    DateModel(10, 10, 2018),
    DisposalValueModel(150000),
    AcquisitionCostsModel(300),
    DisposalCostsModel(600),
    CalculationElectionModel(Rebased),
    OtherReliefsModel(999),
    OtherReliefsModel(888),
    OtherReliefsModel(0),
    Some(PrivateResidenceReliefModel("Yes", Some(100), Some(50)))
  )

  val summaryWithAllOptions = SummaryModel(
    CurrentIncomeModel(1000),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No"),
    None,
    DateModel(9, 9, 1999),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(1000)),
    Some(RebasedCostsModel("Yes", Some(500))),
    ImprovementsModel(8000),
    DateModel(10, 10, 2018),
    DisposalValueModel(150000),
    AcquisitionCostsModel(300),
    DisposalCostsModel(600),
    CalculationElectionModel(Flat),
    OtherReliefsModel(999),
    OtherReliefsModel(888),
    OtherReliefsModel(777),
    Some(PrivateResidenceReliefModel("Yes", Some(100), Some(50)))
  )

  val totalGainAnswersModelWithRebasedTA = TotalGainAnswersModel(
    DateModel(5, 6, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(true)),
    DisposalValueModel(950000),
    DisposalCostsModel(15000),
    Some(HowBecameOwnerModel("Bought")),
    Some(BoughtForLessModel(false)),
    AcquisitionValueModel(1250000),
    AcquisitionCostsModel(20000),
    DateModel(10, 10, 2001),
    Some(RebasedValueModel(950000)),
    Some(RebasedCostsModel("No", None)),
    IsClaimingImprovementsModel(false),
    ImprovementsModel(),
    Some(OtherReliefsModel(0))
  )

  val calculationResultsModelWithRebased = CalculationResultsWithTaxOwedModel(
    TotalTaxOwedModel(100, 100, 20, None, None, 200, 100, None, None, None, None, 0, None, None, None, None, None, None, None),
    Some(TotalTaxOwedModel(500, 500, 20, None, None, 500, 500, None, None, None, None, 0, None, None, None, None, None, None, None)),
    None
  )

  val calculationResultsModelWithTA = CalculationResultsWithTaxOwedModel(
    TotalTaxOwedModel(100, 100, 20, None, None, 200, 100, None, None, None, None, 0, None, None, None, None, None, None, None),
    None,
    Some(TotalTaxOwedModel(500, 500, 20, None, None, 200, 100, None, None, None, None, 0, None, None, None, None, None, None, None))
  )

  val calculationResultsModelWithAll = CalculationResultsWithTaxOwedModel(
    TotalTaxOwedModel(100, 100, 20, None, None, 200, 100, None, None, None, None, 0, None, None, None, None, None, None, None),
    Some(TotalTaxOwedModel(500, 0, 20, None, None, 200, 100, None, None, None, None, 0, None, None, None, None, None, None, None)),
    Some(TotalTaxOwedModel(500, 0, 20, None, None, 200, 100, None, None, None, None, 0, None, None, None, None, None, None, None))
  )

  val personalDetailsCalculationModel = TotalPersonalDetailsCalculationModel(
    CurrentIncomeModel(20000),
    Some(PersonalAllowanceModel(0)),
    OtherPropertiesModel("Yes"),
    Some(PreviousLossOrGainModel("Neither")),
    None,
    None,
    Some(AnnualExemptAmountModel(0)),
    BroughtForwardLossesModel(isClaiming = false, None)
  )
}
