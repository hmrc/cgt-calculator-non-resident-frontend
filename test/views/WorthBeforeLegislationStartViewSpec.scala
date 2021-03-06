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

package views

import assets.MessageLookup.NonResident.WorthBeforeLegislationStart
import assets.MessageLookup.{NonResident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.WorthBeforeLegislationStartForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.worthBeforeLegislationStart

class WorthBeforeLegislationStartViewSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val worthBeforeLegislationStartView = fakeApplication.injector.instanceOf[worthBeforeLegislationStart]

  "The Worth Before Legislation Start view spec" when {

    "supplied with no errors" should {

      lazy val view = worthBeforeLegislationStartView(worthBeforeLegislationStartForm)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '${WorthBeforeLegislationStart.question}'" in {
        document.title() shouldBe WorthBeforeLegislationStart.question
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has a class of 'back-link'" in {
          backLink.attr("class") shouldBe "back-link"
        }

        "has the text" in {
          backLink.text shouldBe commonMessages.back
        }

        s"has a route to 'acquisition-date'" in {
          backLink.attr("href") shouldBe controllers.routes.AcquisitionDateController.acquisitionDate().url
        }
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate().url
      }

      "have a heading" which {

        lazy val heading = document.body().select("h1")

        "has a class of heading-large" in {
          heading.attr("class") shouldBe "heading-xlarge"
        }

        s"has the text '${WorthBeforeLegislationStart.question}'" in {
          heading.text shouldBe WorthBeforeLegislationStart.question
        }
      }

      s"has the information text ${WorthBeforeLegislationStart.information}" in {
        document.select("article > p").text should include(WorthBeforeLegislationStart.information)
      }

      s"has the hint text ${WorthBeforeLegislationStart.hintText}" in {
        document.select("article > div.form-hint > p").text shouldEqual WorthBeforeLegislationStart.hintText
      }

      s"has the joint ownership text ${WorthBeforeLegislationStart.jointOwnership}" in {
        document.select("article > div.panel-indent > p").text shouldEqual WorthBeforeLegislationStart.jointOwnership
      }


      "have input containing the id 'worthBeforeLegislationStart'" in {
        document.body().select("input").attr("id") should include("worthBeforeLegislationStart")
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.WorthBeforeLegislationStartController.submitWorthBeforeLegislationStart().url}'" in {
          form.attr("action") shouldBe controllers.routes.WorthBeforeLegislationStartController.submitWorthBeforeLegislationStart().url
        }
      }

      "have a button" which {
        lazy val button = document.select("button")

        "has the class 'button'" in {
          button.attr("class") shouldBe "button"
        }

        "has the type 'submit'" in {
          button.attr("type") shouldBe "submit"
        }

        "has the id 'continue-button'" in {
          button.attr("id") shouldBe "continue-button"
        }
      }
      "should produce the same output when render and f are called" in {
        worthBeforeLegislationStartView.f(worthBeforeLegislationStartForm)(fakeRequest, mockMessage) shouldBe worthBeforeLegislationStartView.render(worthBeforeLegislationStartForm, fakeRequest, mockMessage)
      }

      }

    "supplied with a form with errors" should {

      lazy val form = worthBeforeLegislationStartForm.bind(Map("worthBeforeLegislationStart" -> "a"))
      lazy val view = worthBeforeLegislationStartView(form)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
