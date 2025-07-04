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
import controllers.helpers.FakeRequestHelper
import forms.OtherReliefsForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.otherReliefs

class OtherReliefsViewSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val otherReliefsView: otherReliefs = fakeApplication.injector.instanceOf[otherReliefs]

  "The Other Reliefs Flat view" when {

    "not supplied with a pre-existing stored value and a taxable gain" should {
      val totalGain = 1234
      val totalChargeableGain = 4321
      lazy val view = otherReliefsView(otherReliefsForm, totalChargeableGain, totalGain)(using fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of ${messages.OtherReliefs.question}" in {
        document.title() shouldBe messages.OtherReliefs.title
      }

      "have a back link" which {
        lazy val backLink = document.select(".govuk-back-link")

        "should have the text" in {
          backLink.text shouldEqual messages.back
        }

        "should have a class of back-link" in {
          backLink.attr("class") shouldBe "govuk-back-link"
        }

        s"should have a route to 'check-your-answers'" in {
          backLink.attr("href") shouldEqual "#"
        }
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-xlarge" in {
          heading.attr("class") shouldBe "govuk-heading-xl"
        }

        s"has the text '${messages.OtherReliefs.question}'" in {
          heading.text shouldBe messages.OtherReliefs.question
        }
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate.url}'" in {
        document.getElementsByClass("govuk-header__link govuk-header__service-name").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate.url
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.OtherReliefsController.otherReliefs.url}'" in {
          form.attr("action") shouldBe controllers.routes.OtherReliefsController.otherReliefs.url
        }
      }

      s"have the question '${messages.OtherReliefs.question}'" in {
        document.body.select("label").text should include(messages.OtherReliefs.question)
      }

      "have an input using the id otherReliefs" in {
        document.body().getElementsByClass("govuk-input govuk-input--width-10").attr("id") should include("otherReliefs")
      }

      "have the correct help text" in {
        document.body().select("#otherReliefHelp").text().replaceAll("[\\n]", " ") shouldBe
          s"${messages.OtherReliefs.help} ${messages.OtherReliefs.helpTwo}"
      }

      "have the correct gain values in the additional help text" in {
        val expectedText = messages.OtherReliefs.additionalHelp(totalGain, totalChargeableGain)
        document.body().select("#main-content > div > div > p").text() shouldBe expectedText
      }

      "have a button" which {
        lazy val button = document.select("button")

        "has the class 'button'" in {
          button.attr("class") shouldBe "govuk-button"
        }

        "has the id 'continue-button'" in {
          button.attr("id") shouldBe "submit"
        }

        s"has the text ${messages.continue}" in {
          button.text() shouldBe messages.continue
        }
      }
      "should produce the same output when render and f are called" in {
        otherReliefsView.f(otherReliefsForm, totalChargeableGain, totalGain)(fakeRequest, mockMessage) shouldBe otherReliefsView.render(otherReliefsForm, totalChargeableGain, totalGain, fakeRequest, mockMessage)
      }
    }

    "the gain and chargeable gain are negative" should {
      val totalGain = -1234
      val totalChargeableGain = -4321

      lazy val view = otherReliefsView(otherReliefsForm, totalChargeableGain, totalGain)(using fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have the correct additional body text" in {
        val expectedText = messages.OtherReliefs.additionalHelp(totalGain, totalChargeableGain)
        document.body().select("#main-content > div > div > p").text() shouldBe expectedText
      }

      "have the words 'total loss' in the additional body text" in {
        document.body().select("#main-content > div > div > p").text() should include("total loss of £1,234")
      }

      "have the words 'allowable loss' in the additional body text" in {
        document.body().select("#main-content > div > div > p").text() should include("an allowable loss of £4,321")
      }
    }

    "supplied with an invalid map" should {
      val map = Map("otherReliefs" -> "-1000")
      lazy val view = otherReliefsView(otherReliefsForm.bind(map), 0, 0)(using fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.getElementsByClass("govuk-error-summary").size() shouldBe 1
      }
    }
  }
}
