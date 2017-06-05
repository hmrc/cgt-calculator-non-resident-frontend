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

package constructors

import models.ImprovementsModel
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import views.html.helpers._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object ImprovementsConstructor {
  def generateImprovements(improvementsForm: Form[ImprovementsModel], improvementsOptions: Boolean,
                           question: String): HtmlFormat.Appendable = {
      if (improvementsOptions) {
      formHiddenYesNoRadio(
        improvementsForm,
        "isClaimingImprovements",
        question,
        formMultipleInputMoney(
          improvementsForm,
          Seq(
            ("improvementsAmt", Messages("calc.improvements.questionThree"), None),
            ("improvementsAmtAfter", Messages("calc.improvements.questionFour"), None)
          ),
          boldText = true
        ),
        None,
        hideLegend = true
      )
    } else {
      formHiddenYesNoRadio(
        improvementsForm,
        "isClaimingImprovements",
        question,
        formInputMoney(improvementsForm, "improvementsAmt", Messages("calc.improvements.questionTwo"), labelClasses = "bold-small"),
        None,
        hideLegend = true
      )
    }
  }
}
