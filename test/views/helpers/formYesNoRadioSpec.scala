/*
 * Copyright 2019 HM Revenue & Customs
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

package views.helpers

import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.jsoup.Jsoup
import views.html.helpers._
import assets.MessageLookup.{NonResident => messages}
import forms.BoughtForLessForm._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class formYesNoRadioSpec extends UnitSpec with WithFakeApplication {

  "formYesNoRadio" when {

    "not supplied with legend class" should {
      lazy val helper = formYesNoRadio(boughtForLessForm.apply("boughtForLess"), "legend")
      lazy val document = Jsoup.parse(helper.body)

      "contain inputs with the id boughtForLess" in {
        document.select("input").attr("id") should include ("boughtForLess")
      }

      "contain an input with the value 'Yes'" in {
        document.select("input#boughtForLess-yes").attr("value") shouldBe "Yes"
      }

      "contain an input with the value 'No'" in {
        document.select("input#boughtForLess-no").attr("value") shouldBe "No"
      }

      s"contain an label with the message ${messages.yes}" in {
        document.select("[for=boughtForLess-yes]").text() shouldBe messages.yes
      }

      s"contain an label with the message ${messages.no}" in {
        document.select("[for=boughtForLess-no]").text() shouldBe messages.no
      }

      "have a legend" which {
        lazy val legend = document.select("legend")

        "have a legend with no class set" in {
          legend.attr("class").isEmpty shouldBe true
        }

        "have a legend with the text 'legend'" in {
          legend.text() shouldBe "legend"
        }

        "have a legend with the id 'boughtForLess'" in {
          legend.attr("id") shouldBe "boughtForLess"
        }
      }

      "have labels with the class block-label" in {
        document.select("label").attr("class") shouldBe "block-label"
      }

      "have a fieldset with the class 'inline form-group radio-list'" in {
        document.select("fieldset").attr("class") shouldBe "inline form-group radio-list"
      }
    }

    "supplied with no legend" should {
      lazy val helper = formYesNoRadio(boughtForLessForm.apply("boughtForLess"), "legend")
      lazy val document = Jsoup.parse(helper.body)

      "have a legend with no class set" in {
        document.select("legend").attr("class").isEmpty shouldBe true
      }
    }

    "supplied with a legend" should {
      lazy val helper = formYesNoRadio(boughtForLessForm.apply("boughtForLess"), "legend", legendClass = Some("class"))
      lazy val document = Jsoup.parse(helper.body)

      "have a legend with a class of class" in {
        document.select("legend").attr("class") shouldBe "class"
      }
    }
  }
}
