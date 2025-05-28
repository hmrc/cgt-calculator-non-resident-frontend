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

package views

import assets.MessageLookup.NonResident.{SoldOrGivenAway => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.SoldOrGivenAwayForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.soldOrGivenAway

class SoldOrGivenAwayViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val soldOrGivenAwayView: soldOrGivenAway = fakeApplication.injector.instanceOf[soldOrGivenAway]

  "The Sold Or Given Away View" when {

    "not supplied with a pre-existing model" should {
      lazy val view = soldOrGivenAwayView(soldOrGivenAwayForm)(using fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have a 'back-link' that" should {
        lazy val backLink = document.body().select(".govuk-back-link")

        "have the class of 'govuk-back-link'" in {
          backLink.attr("class") shouldBe "govuk-back-link"
        }

        s"have the text of ${commonMessages.back}" in {
          backLink.text() shouldBe commonMessages.back
        }

        "have the route to Disposal Date" in {
          backLink.attr("href") shouldBe "#"
        }
      }

      "have a heading that" should {
        lazy val heading = document.select("h1")

        "have the class of govuk-fieldset__heading" in {
          heading.attr("class") shouldBe "govuk-fieldset__heading"
        }

        s"have the text of ${messages.question}" in {
          heading.text shouldBe messages.question
        }
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate.url}'" in {
        document.select("body > header > div > div > div.govuk-header__content > a")
          .attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate.url
      }

      "have a form that" should {
        lazy val form = document.body().select("form")

        "has a post method" in {
          form.attr("method") shouldBe "POST"
        }

        s"have an action of ${controllers.routes.SoldOrGivenAwayController.soldOrGivenAway.url}" in {
          form.attr("action") shouldBe controllers.routes.SoldOrGivenAwayController.soldOrGivenAway.url
        }
      }

      "have an options input with id 'soldIt" in {
        document.body().select("input[type=radio]").attr("id") should include("soldIt")
      }

      "have a legend that" should {
        lazy val legend = document.body.select("legend")

        s"have the text of ${messages.question}" in {
          legend.text shouldBe messages.question
        }

      }

      "have a button that" should {
        lazy val button = document.select("button")

        "has the class of govuk-button" in {
          button.attr("class") shouldBe "govuk-button"
        }

        s"has the text of ${commonMessages.continue}" in {
          button.text shouldBe commonMessages.continue
        }

        "has the id of 'submit'" in {
          button.attr("id") shouldBe "submit"
        }
      }

      "should produce the same output when render and f are called" in {
        soldOrGivenAwayView.f(soldOrGivenAwayForm)(fakeRequest, mockMessage) shouldBe soldOrGivenAwayView.render(soldOrGivenAwayForm, fakeRequest, mockMessage)
      }
    }

    "provided with errors" should {
      lazy val form = soldOrGivenAwayForm.bind(Map("soldIt" -> "999"))
      lazy val view = soldOrGivenAwayView(form)(using fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.getElementsByClass("govuk-error-summary").size shouldBe 1
      }
    }
  }
}
