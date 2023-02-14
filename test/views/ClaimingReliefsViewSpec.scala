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

package views

import assets.MessageLookup.NonResident.{ClaimingReliefs => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.ClaimingReliefsForm
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.claimingReliefs

class ClaimingReliefsViewSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val claimingReliefsView = fakeApplication.injector.instanceOf[claimingReliefs]

  lazy val pageTitle = s"""${messages.title} - ${commonMessages.pageHeading} - GOV.UK"""

  "ClaimingReliefs view" when {

    "supplied with no errors" should {
      lazy val form = ClaimingReliefsForm.claimingReliefsForm
      lazy val view = claimingReliefsView(form)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      s"have the title ${messages.title}" in {
        document.title() shouldBe pageTitle
      }

      "have a back link" which {
        lazy val backLink = document.select("#back-link")

        s"has text ${commonMessages.back}" in {
          backLink.text() shouldBe commonMessages.back
        }

        s"has a link to ${controllers.routes.CheckYourAnswersController.checkYourAnswers.url}" in {
          backLink.attr("href") shouldBe "javascript:history.back()"
        }
      }

      "have a h1" which {

        lazy val h1Tag = document.select("h1")

        s"has the text '${messages.title}'" in {
          h1Tag.text shouldBe messages.title
        }
      }

      s"have a p with text ${messages.helpText}" in {
        document.getElementsByClass("govuk-body").text() shouldBe messages.helpText
      }

      "render a form tag with a submit action" in {
        document.select("form").attr("action") shouldEqual controllers.routes.ClaimingReliefsController.submitClaimingReliefs.url
      }

      s"have a hidden legend with text ${messages.title}" in {
        document.select("legend").text shouldBe messages.title
      }

      "have inputs containing the id isClaimingReliefs" in {
        document.body().select("input").attr("id") should include("isClaimingReliefs")
      }

      "have a continue button" which {

        lazy val button = document.select("button")

        "has class of 'govuk-button'" in {
          button.attr("class") shouldEqual "govuk-button"
        }

        "has the id 'submit'" in {
          button.attr("id") shouldBe "submit"
        }

        s"has the text ${commonMessages.continue}" in {
          button.text shouldEqual s"${commonMessages.continue}"
        }
      }

      "should produce the same output when render and f are called" in {
        claimingReliefsView.f(form)(fakeRequest, mockMessage) shouldBe claimingReliefsView.render(form, fakeRequest, mockMessage)
      }
    }

    "supplied with form errors" should {
      lazy val form = ClaimingReliefsForm.claimingReliefsForm.bind(Map("isClaimingReliefs" -> "abc"))
      lazy val view = claimingReliefsView(form)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.getElementsByClass("govuk-error-summary").size() shouldBe 1
      }
    }
  }
}
