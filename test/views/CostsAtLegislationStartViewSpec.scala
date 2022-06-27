/*
 * Copyright 2022 HM Revenue & Customs
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

import assets.MessageLookup.{NonResident => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.CostsAtLegislationStartForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.costsAtLegislationStart

class CostsAtLegislationStartViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val costsAtLegislationStartView = fakeApplication.injector.instanceOf[costsAtLegislationStart]

  "The costs at legislation start date view" when {

    "not supplied with a pre-existing stored model" should {
      lazy val view = costsAtLegislationStartView(costsAtLegislationStartForm)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      s"Have the title ${messages.CostsAtLegislationStart.title}" in {
        document.title shouldEqual messages.CostsAtLegislationStart.title
      }

      "have a back link" which {
        lazy val backLink = document.select("#back-link")

        "has the correct text" in {
          backLink.text shouldBe messages.back
        }

        s"has a route to 'worth-before-legislation-start'" in {
          backLink.attr("href") shouldBe "javascript:history.back()"
        }
      }

      "have a heading" which {
        lazy val heading = document.select("h1")

        "has a class of heading-xlarge" in {
          heading.attr("class") shouldBe "govuk-fieldset__heading"
        }

        s"has the text '${messages.CostsAtLegislationStart.heading}'" in {
          heading.text shouldBe messages.CostsAtLegislationStart.heading
        }
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate.url}'" in {
        document.select("body > header > div > div > div.govuk-header__content > a").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate.url
      }

      "have a form" which {
        lazy val form = document.select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.CostsAtLegislationStartController.costsAtLegislationStart.url}'" in {
          form.attr("action") shouldBe controllers.routes.CostsAtLegislationStartController.costsAtLegislationStart.url
        }
      }

      s"have an primary question with the correct text" in {
        document.select("h1").text() shouldBe messages.CostsAtLegislationStart.heading
      }

      s"have a secondary question with the correct text" in {
        document.select("#conditional-costs > div > label").text() shouldBe messages.CostsAtLegislationStart.howMuch
      }

      s"have help text for the secondary question" in {
        document.select("#costs-hint").text() shouldBe messages.CostsAtLegislationStart.helpText
      }

      "have a value input with the id 'costs'" in {
        document.select("#costs").size() shouldBe 1
      }

      "have a continue button" which {
        lazy val button = document.select("button")

        "has the class 'button'" in {
          button.attr("class") shouldBe "govuk-button"
        }

        "has the id 'submit'" in {
          button.attr("id") shouldBe "submit"
        }
      }

      "should produce the same output when render and f are called" in {
        costsAtLegislationStartView.f(costsAtLegislationStartForm)(fakeRequest, mockMessage) shouldBe
          costsAtLegislationStartView.render(costsAtLegislationStartForm, fakeRequest, mockMessage)
      }
    }

    "supplied with errors" should {
      lazy val form = costsAtLegislationStartForm.bind(Map(
        "hasCosts" -> "Yes",
        "costs" -> ""
      ))
      lazy val view = costsAtLegislationStartView(form)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select(".govuk-error-summary").size() shouldBe 1
      }
    }
  }
}
