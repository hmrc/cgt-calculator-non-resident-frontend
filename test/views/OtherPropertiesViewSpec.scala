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

import assets.MessageLookup.NonResident.{OtherProperties => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.OtherPropertiesForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.otherProperties

class OtherPropertiesViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  "The other properties view" should {

    "return some HTML that, when the hidden question is displayed" should {

      lazy val view = otherProperties(otherPropertiesForm)(fakeRequest, mockMessage, fakeApplication, mockConfig)
      lazy val document = Jsoup.parse(view.body)

      s"have the title '${messages.question}'" in {
        document.title shouldEqual messages.question
      }

      s"have the heading ${messages.question}" in {
        document.body.getElementsByTag("h1").text shouldEqual messages.question
      }

      s"have a 'Back' link to ${controllers.routes.PersonalAllowanceController.personalAllowance().url}" which {

        "should have the text" in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
        }

        s"should have an href to '${controllers.routes.PersonalAllowanceController.personalAllowance().url}'" in {
          document.body.getElementById("back-link").attr("href") shouldEqual controllers.routes.PersonalAllowanceController.personalAllowance().url
        }
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate().url
      }

      s"have a legend of the input" which {
        lazy val legend = document.body.getElementsByTag("legend")
        s"has the text ${messages.question}" in {
          legend.text should include(messages.question)
        }
        "has the class 'visuallyhidden'" in {
          legend.attr("class") shouldBe "visuallyhidden"
        }
      }

      "have inputs using the id 'otherProperties'" in {
        document.body().select("input[type=radio]").attr("id") should include("otherProperties")
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

        "has the text 'Continue'" in {
          button.text shouldEqual commonMessages.continue
        }
      }

      "should produce the same output when render and f are called" in {
        otherProperties.f(otherPropertiesForm)(fakeRequest, mockMessage, fakeApplication, mockConfig) shouldBe otherProperties.render(otherPropertiesForm, fakeRequest, mockMessage, fakeApplication, mockConfig)
      }
    }

    "return some HTML that, when the hidden question is not displayed" should {

      lazy val view = otherProperties(otherPropertiesForm)(fakeRequest, mockMessage, fakeApplication, mockConfig)
      lazy val document = Jsoup.parse(view.body)

      s"have the title '${messages.question}'" in {
        document.title shouldEqual messages.question
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
      lazy val view = otherProperties(form)(fakeRequest, mockMessage, fakeApplication, mockConfig)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
