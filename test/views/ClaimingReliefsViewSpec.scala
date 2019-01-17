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

package views

import assets.MessageLookup.{NonResident => commonMessages}
import assets.MessageLookup.NonResident.{ClaimingReliefs => messages}
import controllers.helpers.FakeRequestHelper
import forms.ClaimingReliefsForm
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.claimingReliefs
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class ClaimingReliefsViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  "ClaimingReliefs view" when {

    "supplied with no errors" should {
      lazy val form = ClaimingReliefsForm.claimingReliefsForm
      lazy val view = claimingReliefs(form)
      lazy val document = Jsoup.parse(view.body)

      s"have the title ${messages.title}" in {
        document.title() shouldBe messages.title
      }

      "have a back link" which {
        lazy val backLink = document.select("#back-link")

        s"has text ${commonMessages.back}" in {
          backLink.text() shouldBe commonMessages.back
        }

        s"has a link to ${controllers.routes.CheckYourAnswersController.checkYourAnswers().url}" in {
          backLink.attr("href") shouldBe controllers.routes.CheckYourAnswersController.checkYourAnswers().url
        }
      }

      "have a h1" which {

        lazy val h1Tag = document.select("h1")

        s"has the text '${messages.title}'" in {
          h1Tag.text shouldBe messages.title
        }
      }

      s"have a p with text ${messages.helpText}" in {
        document.select("#content > article > p").text() shouldBe messages.helpText
      }

      "render a form tag with a submit action" in {
        document.select("form").attr("action") shouldEqual controllers.routes.ClaimingReliefsController.submitClaimingReliefs().url
      }

      s"have a hidden legend with text ${messages.title}" in {
        document.select("legend").text shouldBe messages.title
      }

      "have inputs containing the id isClaimingReliefs" in {
        document.body().select("input").attr("id") should include("isClaimingReliefs")
      }

      "have a continue button" which {

        lazy val button = document.select("button")

        "has type of 'submit'" in {
          button.attr("type") shouldEqual "submit"
        }

        "has the id 'continue-button'" in {
          button.attr("id") shouldEqual "continue-button"
        }

        s"has the text ${commonMessages.continue}" in {
          button.text shouldEqual s"${commonMessages.continue}"
        }
      }
    }

    "supplied with form errors" should {
      lazy val form = ClaimingReliefsForm.claimingReliefsForm.bind(Map("isClaimingReliefs" -> "abc"))
      lazy val view = claimingReliefs(form)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
