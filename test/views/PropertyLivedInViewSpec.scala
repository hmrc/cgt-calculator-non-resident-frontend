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

import assets.MessageLookup.{NonResident => commonMessages, PropertyLivedIn => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import constructors.helpers.AssertHelpers
import controllers.helpers.FakeRequestHelper
import forms.PropertyLivedInForm
import forms.PropertyLivedInForm._
import models.PropertyLivedInModel
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.propertyLivedIn

class PropertyLivedInViewSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper with AssertHelpers {
  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val propertyLivedInView: propertyLivedIn = fakeApplication.injector.instanceOf[propertyLivedIn]

  "PropertyLivedIn view" when {

    "supplied with no errors" should {
      lazy val form = PropertyLivedInForm.propertyLivedInForm
      lazy val view = propertyLivedInView(form)(using fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of ${messages.title}" in {
        document.title() shouldBe s"${messages.title} - Calculate your Non-Resident Capital Gains Tax - GOV.UK"
      }

      "have a H1 tag that" should {

        lazy val heading = document.select("legend")

        s"have the page heading '${messages.title}'" in {
          heading.text shouldBe s"${messages.title}"
        }

        "have the govuk-label--l class" in {
          heading.hasClass("govuk-fieldset__legend govuk-fieldset__legend--l") shouldBe true
        }
      }

      "have a back button" which {

        lazy val backLink = document.select("a.govuk-back-link")

        "has the correct back link text" in {
          backLink.text shouldBe commonMessages.back
        }

        "has the back-link class" in {
          backLink.attr("class") shouldBe "govuk-back-link"
        }

        "has a back link to 'back'" in {
          backLink.attr("href") shouldBe "#"
        }
      }

      "render a form tag with a submit action" in {
        document.select("form").attr("action") shouldEqual controllers.routes.PropertyLivedInController.submitPropertyLivedIn.url
      }

      "have a legend for the radio inputs" which {

        lazy val legend = document.select("legend")

        s"contain the text ${messages.title}" in {
          legend.text should include(s"${messages.title}")
        }
      }

      "have a set of radio inputs" which {

        "for the option 'Yes'" should {

          lazy val YesRadioOption = document.select("#propertyLivedIn")

          "have the property 'for'" in {
            YesRadioOption.hasAttr("value") shouldEqual true
          }

          "the for attribute has the value propertyLivedIn-Yes" in {
            YesRadioOption.attr("value") shouldEqual "Yes"
          }

          "have an input under the label that" should {

            lazy val optionLabel = document.select("#propertyLivedIn")

            "have the id 'propertyLivedIn-Yes'" in {
              optionLabel.attr("id") shouldEqual "propertyLivedIn"
            }

            "have the value 'Yes'" in {
              optionLabel.attr("value") shouldEqual "Yes"
            }

            "be of type radio" in {
              optionLabel.attr("type") shouldEqual "radio"
            }
          }
        }

        "for the option 'No'" should {

          lazy val NoRadioOption = document.select("#propertyLivedIn-2")

          "have a label with class 'govuk-radios__input'" in {
            NoRadioOption.hasClass("govuk-radios__input") shouldEqual true
          }

          "have the property 'for'" in {
            NoRadioOption.hasAttr("value") shouldEqual true
          }

          "the for attribute has the value propertyLivedIn-No" in {
            NoRadioOption.attr("value") shouldEqual "No"
          }
        }
      }

      "have a continue button" which {

        lazy val button = document.select("button")

        "has class 'button'" in {
          button.hasClass("govuk-button") shouldEqual true
        }

        "has type value of 'submit'" in {
          button.attr("id") shouldEqual "submit"
        }

        "has attribute id" in {
          button.hasAttr("id") shouldEqual true
        }

        "has id equal to continue-button" in {
          button.attr("id") shouldEqual "submit"
        }

        s"has the text ${commonMessages.continue}" in {
          button.text shouldEqual s"${commonMessages.continue}"
        }
      }

      "should produce the same output when render and f are called" in {
        propertyLivedInView.f(form)(fakeRequest, mockMessage) shouldBe propertyLivedInView.render(form, fakeRequest, mockMessage)
      }
    }

    "supplied with a filled form of 'Yes'" which {
      lazy val view = propertyLivedInView(propertyLivedInForm.fill(PropertyLivedInModel(true)))(using fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "for the option 'Yes'" should {

        lazy val YesRadioOption = document.getElementsByClass("govuk-radios__input")

        "have the option auto-selected" in {
          YesRadioOption.attr("name") shouldBe "propertyLivedIn"
        }
      }
    }

    "Property Lived In view with form errors" should {

      lazy val form = propertyLivedInForm.bind(Map("propertyLivedIn" -> ""))
      lazy val view = propertyLivedInView(form)(using fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" which {
        "display an error summary message for the page" in {
          document.getElementsByClass("govuk-error-summary").size() shouldBe 1
        }

        "display an error message for the input" in {
          document.getElementsByClass("govuk-error-summary").size() shouldBe 1
        }
      }
    }
  }
}

