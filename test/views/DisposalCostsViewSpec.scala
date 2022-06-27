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

import assets.MessageLookup.NonResident.{DisposalCosts => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.DisposalCostsForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.disposalCosts

class DisposalCostsViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val disposalCostsView = fakeApplication.injector.instanceOf[disposalCosts]
  lazy val pageTitle = s"""${messages.question} - ${commonMessages.pageHeading} - GOV.UK"""

  "Disposal Costs view" when {

    "supplied with no errors" should {
      lazy val view = disposalCostsView(disposalCostsForm, "back-link")(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '${pageTitle}'" in {
        document.title shouldBe pageTitle
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has a class of 'back-link'" in {
          backLink.attr("class") shouldBe "govuk-back-link"
        }

        "has the text" in {
          backLink.text shouldBe commonMessages.back
        }

        s"has a route to 'back-link'" in {
          backLink.attr("href") shouldBe "javascript:history.back()"
        }
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-xlarge" in {
          heading.attr("class") shouldBe "govuk-heading-xl"
        }

        s"has the text '${messages.question}'" in {
          heading.text shouldBe messages.question
        }
      }

      "have a hint" which {

        lazy val hint = document.select("#input-hint")

        "should have the class form-hint" in {
          hint.hasClass("govuk-body") shouldEqual true
        }

        "should contain a bullet list" which {

          s"has the title ${messages.helpTitle}" in {
            hint.select("p").text shouldEqual messages.helpTitle
          }

          s"has a bullet point with the text ${messages.helpBulletOne}" in {
            hint.select("ul li").text should include(messages.helpBulletOne)
          }

          s"has a bullet point with the text ${messages.helpBulletTwo}" in {
            hint.select("ul li").text should include(messages.helpBulletTwo)
          }

          s"has a bullet point with the text ${messages.helpBulletThree}" in {
            hint.select("ul li").text should include(messages.helpBulletThree)
          }

          s"has a bullet point with the text ${messages.helpBulletFour}" in {
            hint.select("ul li").text should include(messages.helpBulletFour)
          }
        }
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a POST method" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.DisposalCostsController.submitDisposalCosts.url}'" in {
          form.attr("action") shouldBe controllers.routes.DisposalCostsController.submitDisposalCosts.url
        }

        s"have a paragraph with the text ${messages.jointOwnership}" in {
          document.getElementsByClass("govuk-inset-text").text shouldBe messages.jointOwnership
        }
      }

      s"have the question ${messages.question}" in {
        document.getElementsByClass("govuk-heading-xl").first.text shouldBe messages.question
      }

      "have an input with the id 'disposalCosts" in {
        document.body.select("input").attr("id") shouldBe "disposalCosts"
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

      "should produce the same output when render and f are called" in {
        disposalCostsView.f(disposalCostsForm, "back-link")(fakeRequest, mockMessage) shouldBe disposalCostsView.render(disposalCostsForm, "back-link", fakeRequest, mockMessage)
      }
    }

    "supplied with errors" should {
      lazy val form = disposalCostsForm.bind(Map("disposalCosts" -> "a"))
      lazy val view = disposalCostsView(form, "back-link")(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.getElementsByClass("govuk-error-summary").size() shouldBe 1
      }
    }
  }
}
