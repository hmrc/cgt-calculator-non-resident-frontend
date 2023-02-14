/*
 * Copyright 2023 HM Revenue & Customs
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

import com.google.inject.Inject
import models.ImprovementsModel
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import views.html.playComponents.formHiddenYesNoRadioCurrencyInput

class ImprovementsConstructor @Inject()(formHiddenYesNoRadioView: formHiddenYesNoRadioCurrencyInput
                                        ) {
  def generateImprovements(improvementsForm: Form[ImprovementsModel], improvementsOptions: Boolean,
                           question: String)(implicit messages: Messages): HtmlFormat.Appendable = {
      if (improvementsOptions) {
        formHiddenYesNoRadioView(
          form = improvementsForm,
          yesNoFieldName = "isClaimingImprovements",
          conditionalInputFieldName = "improvementsAmt",
          yesNoQuestionText = question,
          conditionalInputQuestionText = messages("calc.improvements.questionThree"),
          hasSecondField = true,
          secondConditionalInputQuestionText = Some(messages("calc.improvements.questionFour")),
          secondConditionalInputFieldName = Some("improvementsAmtAfter"),
          yesNoQuestionTextIsHeading = false
        )(messages)
    } else {
        formHiddenYesNoRadioView(
          form = improvementsForm,
          yesNoFieldName = "isClaimingImprovements",
          conditionalInputFieldName = "improvementsAmt",
          yesNoQuestionText = question,
          conditionalInputQuestionText = messages("calc.improvements.questionTwo"),
          yesNoQuestionTextIsHeading = false
      )(messages)
    }
  }
}
