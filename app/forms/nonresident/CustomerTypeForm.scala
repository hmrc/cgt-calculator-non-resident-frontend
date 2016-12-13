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

package forms.nonresident

import common.nonresident.CustomerTypeKeys
import models._
import models.nonresident.CustomerTypeModel
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages

object CustomerTypeForm {

  def validate: String => Boolean = customerType => customerType match {
    case CustomerTypeKeys.individual => true
    case CustomerTypeKeys.trustee => true
    case CustomerTypeKeys.personalRep => true
    case _ => false
  }

  val customerTypeForm = Form(
    mapping(
      "customerType" -> text.verifying(Messages("calc.common.invalidError"), validate)
    )(CustomerTypeModel.apply)(CustomerTypeModel.unapply)
  )
}
