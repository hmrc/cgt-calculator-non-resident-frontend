/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.i18n.{Messages, MessagesApi}
import play.twirl.api.HtmlFormat
import views.html.helpers.{formHiddenYesNoRadio, formMultipleInputMoney, formInputMoney}

class ImprovementsConstructor @Inject()(formHiddenYesNoRadioView: formHiddenYesNoRadio,
                                        formMultipleInputMoneyView: formMultipleInputMoney,
                                        formInputMoneyView: formInputMoney
                                        ) {
  def generateImprovements(improvementsForm: Form[ImprovementsModel], improvementsOptions: Boolean,
                           question: String)(implicit messages: Messages, messagesApi: Option[MessagesApi] = None): HtmlFormat.Appendable = {
      if (improvementsOptions) {
        formHiddenYesNoRadioView(
        improvementsForm,
        "isClaimingImprovements",
        question,
          formMultipleInputMoneyView(
          improvementsForm,
          Seq(
            ("improvementsAmt", "calc.improvements.questionThree", None),
            ("improvementsAmtAfter", "calc.improvements.questionFour", None)
          ),
          boldText = true
        )(messages, messagesApi),
        None,
        hideLegend = true
      )(messages)
    } else {
        formHiddenYesNoRadioView(
        improvementsForm,
        "isClaimingImprovements",
        question,
          formInputMoneyView(improvementsForm, "improvementsAmt", "calc.improvements.questionTwo", labelClasses = "bold-small")(messages, messagesApi),
        None,
        hideLegend = true
      )(messages)
    }
  }
}
