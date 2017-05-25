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

package common

import common.nonresident.CustomerTypeKeys
import models._

object TestModels {

  val businessScenarioFiveModel = TotalGainAnswersModel(
    DisposalDateModel(5, 6, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(true)),
    DisposalValueModel(950000),
    DisposalCostsModel(15000),
    Some(HowBecameOwnerModel("Bought")),
    Some(BoughtForLessModel(false)),
    AcquisitionValueModel(1250000),
    AcquisitionCostsModel(20000),
    AcquisitionDateModel(1, 1, 2016),
    None,
    None,
    ImprovementsModel("No", None),
    Some(OtherReliefsModel(0))
  )

  val sumModelFlat = SummaryModel(
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(11100)),
    OtherPropertiesModel("No"),
    None,
    AcquisitionDateModel(5, 4, 2016),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(None)),
    None,
    ImprovementsModel("No", None),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(0),
    DisposalCostsModel(0),
    CalculationElectionModel("flat"),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    None
  )

  val summaryIndividualFlatNoIncomeOtherPropNo = SummaryModel(
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(0)),
    None,
    OtherPropertiesModel("No"),
    None,
    AcquisitionDateModel(5, 4, 2016),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(None)),
    None,
    ImprovementsModel("No", None),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(0),
    DisposalCostsModel(0),
    CalculationElectionModel("flat"),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    None
  )

  val summaryIndividualFlatWithoutAEA = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No"),
    None,
    AcquisitionDateModel(5, 4, 2016),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(None)),
    None,
    ImprovementsModel("Yes", Some(8000)),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(300),
    DisposalCostsModel(600),
    CalculationElectionModel("flat"),
    OtherReliefsModel(999),
    OtherReliefsModel(888),
    OtherReliefsModel(777),
    None
  )

  val summaryIndividualFlatWithAEA = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("Yes"),
    Some(AnnualExemptAmountModel(1500)),
    AcquisitionDateModel(5, 4, 2016),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(None)),
    None,
    ImprovementsModel("No", None),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(0),
    DisposalCostsModel(0),
    CalculationElectionModel("flat"),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    OtherReliefsModel(0),
    None
  )

  val summaryTrusteeTAWithoutAEA = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.trustee),
    Some(DisabledTrusteeModel("No")),
    None,
    None,
    OtherPropertiesModel("No"),
    None,
    AcquisitionDateModel(9, 9, 1999),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(None)),
    None,
    ImprovementsModel("Yes", Some(8000)),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(300),
    DisposalCostsModel(0),
    CalculationElectionModel("time"),
    OtherReliefsModel(999),
    OtherReliefsModel(888),
    OtherReliefsModel(777),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val summaryIndividualImprovementsNoRebasedModel = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No"),
    None,
    AcquisitionDateModel(9, 9, 1999),
    AcquisitionValueModel(100000),
    None,
    None,
    ImprovementsModel("Yes", Some(8000)),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(300),
    DisposalCostsModel(600),
    CalculationElectionModel("flat"),
    OtherReliefsModel(999),
    OtherReliefsModel(888),
    OtherReliefsModel(777),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val summaryIndividualImprovementsWithRebasedModel = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No"),
    None,
    AcquisitionDateModel(9, 9, 1999),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(Some(1000))),
    None,
    ImprovementsModel("Yes", Some(8000), Some(1000)),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(300),
    DisposalCostsModel(600),
    CalculationElectionModel("flat"),
    OtherReliefsModel(999),
    OtherReliefsModel(888),
    OtherReliefsModel(777),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val summaryIndividualRebased = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No"),
    None,
    AcquisitionDateModel(9, 9, 1999),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(Some(150000))),
    Some(RebasedCostsModel("Yes", Some(1000))),
    ImprovementsModel("Yes", Some(2000), Some(3000)),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(300),
    DisposalCostsModel(600),
    CalculationElectionModel("rebased"),
    OtherReliefsModel(999),
    OtherReliefsModel(888),
    OtherReliefsModel(777),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val summaryIndividualRebasedNoImprovements = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No"),
    None,
    AcquisitionDateModel(9, 9, 1999),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(Some(150000))),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel("No", None, None),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(300),
    DisposalCostsModel(600),
    CalculationElectionModel("rebased"),
    OtherReliefsModel(999),
    OtherReliefsModel(888),
    OtherReliefsModel(0),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val summaryIndividualRebasedNoneImprovements = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No"),
    None,
    AcquisitionDateModel(9, 9, 1999),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(Some(150000))),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel("Yes", None, None),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(300),
    DisposalCostsModel(600),
    CalculationElectionModel("rebased"),
    OtherReliefsModel(999),
    OtherReliefsModel(888),
    OtherReliefsModel(0),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val summaryIndividualPRRAcqDateAfterAndDisposalDateBefore = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No"),
    None,
    AcquisitionDateModel(9, 9, 2016),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(Some(150000))),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel("Yes", None, None),
    DisposalDateModel(10, 10, 2018),
    DisposalValueModel(150000),
    AcquisitionCostsModel(300),
    DisposalCostsModel(600),
    CalculationElectionModel("rebased"),
    OtherReliefsModel(999),
    OtherReliefsModel(888),
    OtherReliefsModel(0),
    Some(PrivateResidenceReliefModel("Yes", Some(100), Some(50)))
  )

  val summaryIndividualPRRAcqDateAfterAndNoRebased = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No"),
    None,
    AcquisitionDateModel(9, 9, 2012),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(None)),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel("Yes", None, None),
    DisposalDateModel(10, 10, 2018),
    DisposalValueModel(150000),
    AcquisitionCostsModel(300),
    DisposalCostsModel(600),
    CalculationElectionModel("rebased"),
    OtherReliefsModel(999),
    OtherReliefsModel(888),
    OtherReliefsModel(0),
    Some(PrivateResidenceReliefModel("Yes", Some(100), Some(50)))
  )

  val summaryIndividualPRRAcqDateAfterAndDisposalDateAfter = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No"),
    None,
    AcquisitionDateModel(9, 9, 2012),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(Some(1000))),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel("Yes", None, None),
    DisposalDateModel(10, 10, 2018),
    DisposalValueModel(150000),
    AcquisitionCostsModel(300),
    DisposalCostsModel(600),
    CalculationElectionModel("rebased"),
    OtherReliefsModel(999),
    OtherReliefsModel(888),
    OtherReliefsModel(0),
    Some(PrivateResidenceReliefModel("Yes", Some(100), Some(50)))
  )

  val summaryIndividualPRRAcqDateAfterAndDisposalDateAfterWithRebased = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No"),
    None,
    AcquisitionDateModel(9, 9, 2016),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(Some(1000))),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel("Yes", None, None),
    DisposalDateModel(10, 10, 2018),
    DisposalValueModel(150000),
    AcquisitionCostsModel(300),
    DisposalCostsModel(600),
    CalculationElectionModel("rebased"),
    OtherReliefsModel(999),
    OtherReliefsModel(888),
    OtherReliefsModel(0),
    Some(PrivateResidenceReliefModel("Yes", Some(100), Some(50)))
  )

  val summaryIndividualPRRAcqDateBeforeAndDisposalDateAfterWithRebased = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No"),
    None,
    AcquisitionDateModel(9, 9, 2012),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(Some(1000))),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel("Yes", None, None),
    DisposalDateModel(10, 10, 2018),
    DisposalValueModel(150000),
    AcquisitionCostsModel(300),
    DisposalCostsModel(600),
    CalculationElectionModel("rebased"),
    OtherReliefsModel(999),
    OtherReliefsModel(888),
    OtherReliefsModel(0),
    Some(PrivateResidenceReliefModel("Yes", Some(100), Some(50)))
  )

  val summaryIndividualWithAllOptions = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No"),
    None,
    AcquisitionDateModel(9, 9, 1999),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel(Some(1000))),
    Some(RebasedCostsModel("Yes", Some(500))),
    ImprovementsModel("Yes", Some(8000)),
    DisposalDateModel(10, 10, 2018),
    DisposalValueModel(150000),
    AcquisitionCostsModel(300),
    DisposalCostsModel(600),
    CalculationElectionModel("flat"),
    OtherReliefsModel(999),
    OtherReliefsModel(888),
    OtherReliefsModel(777),
    Some(PrivateResidenceReliefModel("Yes", Some(100), Some(50)))
  )

  val totalGainAnswersModelWithRebasedTA = TotalGainAnswersModel(
    DisposalDateModel(5, 6, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(true)),
    DisposalValueModel(950000),
    DisposalCostsModel(15000),
    Some(HowBecameOwnerModel("Bought")),
    Some(BoughtForLessModel(false)),
    AcquisitionValueModel(1250000),
    AcquisitionCostsModel(20000),
    AcquisitionDateModel(10, 10, 2001),
    Some(RebasedValueModel(Some(950000))),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel("No", None),
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

  val personalDetailsCalculationModelIndividual = TotalPersonalDetailsCalculationModel(
    CustomerTypeModel("individual"),
    Some(CurrentIncomeModel(20000)),
    Some(PersonalAllowanceModel(0)),
    None,
    OtherPropertiesModel("Yes"),
    Some(PreviousLossOrGainModel("Neither")),
    None,
    None,
    Some(AnnualExemptAmountModel(0)),
    BroughtForwardLossesModel(isClaiming = false, None)
  )
}
