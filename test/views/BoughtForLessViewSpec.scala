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

import assets.MessageLookup.{NonResident => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import constructors.helpers.AssertHelpers
import controllers.helpers.FakeRequestHelper
import forms.BoughtForLessForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.boughtForLess
import assets.MessageLookup.{NonResident => commonMessages}

class BoughtForLessViewSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper with AssertHelpers {

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val boughtForLessView = fakeApplication.injector.instanceOf[boughtForLess]

  lazy val pageTitle = s"""${messages.BoughtForLess.question} - ${commonMessages.serviceName} - GOV.UK"""

  "Bought for less view" when {

    "supplied with no errors" should {
      lazy val view = boughtForLessView(boughtForLessForm)(fakeRequest,mockMessage)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of ${messages.BoughtForLess.question}" in {
        document.title() shouldBe pageTitle
      }

      "have a back link" which {
        lazy val backLink = document.select(".govuk-back-link")

        "has only a single back link" in {
          backLink.size() shouldBe 1
        }

        "has a class of back-link" in {
          assertHTML(backLink)(_.attr("class") shouldBe "govuk-back-link")
        }

        "has a message of back-link" in {
          assertHTML(backLink)(_.text() shouldBe messages.back)
        }

        "has an href to the how became owner page" in {
          assertHTML(backLink)(_.attr("href") shouldBe "#")
        }
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate.url}'" in {
        document.getElementsByClass("hmrc-header__service-name hmrc-header__service-name--linked").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate.url
      }

      "have a H1 tag" which {
        lazy val header = document.select("h1")

        "has only a single header" in {
          header.size() shouldBe 1
        }

        s"has the text of ${messages.BoughtForLess.question}" in {
          assertHTML(header)(_.text() shouldBe messages.BoughtForLess.question)
        }

        "has the class" in {
          assertHTML(header)(_.attr("class") shouldBe "govuk-fieldset__heading")
        }
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.BoughtForLessController.submitBoughtForLess.url}'" in {
          form.attr("action") shouldBe controllers.routes.BoughtForLessController.submitBoughtForLess.url
        }
      }

      "have a visually hidden legend" which {
        lazy val legend = document.body().select("legend")

        s"has the text ${messages.BoughtForLess.question}" in {
          legend.text() shouldBe messages.BoughtForLess.question
        }
      }

      "has inputs with the id boughtForLess" in {
        document.select("input").attr("id") should include("boughtForLess")
      }

      "have a button" which {
        lazy val button = document.select("button")

        "has the class 'button'" in {
          button.attr("class") shouldBe "govuk-button"
        }

        "has the id 'continue-button'" in {
          button.attr("id") shouldBe "submit"
        }
      }
    }

    "supplied with errors" should {
      lazy val form = boughtForLessForm.bind(Map("boughtForLess" -> "invalid text"))
      lazy val view = boughtForLessView(form)(fakeRequest,mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.getElementsByClass("govuk-error-summary").size() shouldBe 1
      }
    }


    "should produce the same output when render and f are called" in {
      boughtForLessView.f(boughtForLessForm)(fakeRequest,mockMessage) shouldBe
        boughtForLessView.render(boughtForLessForm, fakeRequest,mockMessage)

    }
  }
}
