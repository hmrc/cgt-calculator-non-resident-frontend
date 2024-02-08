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

import assets.MessageLookup.NonResident.{OtherProperties => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.OtherPropertiesForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.otherProperties

class OtherPropertiesViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val otherPropertiesView = fakeApplication.injector.instanceOf[otherProperties]
  lazy val pageTitle = s"""${messages.question} - ${commonMessages.pageHeading} - GOV.UK"""

  "The other properties view" should {

    "return some HTML that, when the hidden question is displayed" should {

      lazy val view = otherPropertiesView(otherPropertiesForm)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      s"have the title '${pageTitle}'" in {
        document.title shouldEqual pageTitle
      }

      s"have the heading ${messages.question}" in {
        document.body.getElementsByTag("h1").text shouldEqual messages.question
      }

      s"have a 'Back' link to ${controllers.routes.PersonalAllowanceController.personalAllowance.url}" which {

        "should have the text" in {
          document.body.getElementsByClass("govuk-back-link").text shouldEqual commonMessages.back
        }

        s"should have an href to '${controllers.routes.PersonalAllowanceController.personalAllowance.url}'" in {
          document.body.getElementsByClass("govuk-back-link").attr("href") shouldEqual "#"
        }
      }

      "have inputs using the id 'otherProperties'" in {
        document.body().select("input[type=radio]").attr("id") should include("otherProperties")
      }

      "have a button" which {
        lazy val button = document.select("button")

        "has the class 'govuk-button'" in {
          button.attr("class") shouldBe "govuk-button"
        }

        "has the id 'submit'" in {
          button.attr("id") shouldBe "submit"
        }

        "has the text 'Continue'" in {
          button.text shouldEqual commonMessages.continue
        }
      }

      "should produce the same output when render and f are called" in {
        otherPropertiesView.f(otherPropertiesForm)(fakeRequest, mockMessage) shouldBe otherPropertiesView.render(otherPropertiesForm, fakeRequest, mockMessage)
      }
    }

    "return some HTML that, when the hidden question is not displayed" should {

      lazy val view = otherPropertiesView(otherPropertiesForm)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      s"have the title '${pageTitle}'" in {
        document.title shouldEqual pageTitle
      }

      "have inputs using the id 'otherProperties'" in {
        document.body().select("input[type=radio]").attr("id") should include("otherProperties")
      }

      "have inputs using the id otherPropertiesAmt" in {
        document.body().select("input[type=number]").attr("id") shouldEqual ""
      }
    }

    "when passed a form with errors" should {

      lazy val form = otherPropertiesForm.bind(Map("otherProperties" -> "bad-data"))
      lazy val view = otherPropertiesView(form)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.getElementsByClass("govuk-error-summary").size shouldBe 1
      }
    }
  }
}
