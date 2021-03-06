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
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.propertyLivedIn

class PropertyLivedInViewSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper with AssertHelpers {
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val propertyLivedInView = fakeApplication.injector.instanceOf[propertyLivedIn]

  "PropertyLivedIn view" when {

    "supplied with no errors" should {
      lazy val form = PropertyLivedInForm.propertyLivedInForm
      lazy val view = propertyLivedInView(form)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of ${messages.title}" in {
        document.title() shouldBe messages.title
      }

      "have a H1 tag that" should {

        lazy val h1Tag = document.select("h1")

        s"have the page heading '${messages.title}'" in {
          h1Tag.text shouldBe messages.title
        }

        "have the heading-large class" in {
          h1Tag.hasClass("heading-xlarge") shouldBe true
        }
      }

      "have a back button" which {

        lazy val backLink = document.select("a#back-link")

        "has the correct back link text" in {
          backLink.text shouldBe commonMessages.back
        }

        "has the back-link class" in {
          backLink.hasClass("back-link") shouldBe true
        }

        "has a back link to 'back'" in {
          backLink.attr("href") shouldBe controllers.routes.ImprovementsController.improvements().url
        }
      }

      "render a form tag with a submit action" in {
        document.select("form").attr("action") shouldEqual controllers.routes.PropertyLivedInController.submitPropertyLivedIn().url
      }

      "have a legend for the radio inputs" which {

        lazy val legend = document.select("legend")

        s"contain the text ${messages.title}" in {
          legend.text should include(s"${messages.title}")
        }
      }

      "have a set of radio inputs" which {

        "for the option 'Yes'" should {

          lazy val YesRadioOption = document.select(".block-label[for=propertyLivedIn-yes]")

          "have a label with class 'block-label'" in {
            YesRadioOption.hasClass("block-label") shouldEqual true
          }

          "have the property 'for'" in {
            YesRadioOption.hasAttr("for") shouldEqual true
          }

          "the for attribute has the value propertyLivedIn-Yes" in {
            YesRadioOption.attr("for") shouldEqual "propertyLivedIn-yes"
          }

          "have the text 'Yes'" in {
            YesRadioOption.text shouldEqual "Yes"
          }

          "have an input under the label that" should {

            lazy val optionLabel = document.select("#propertyLivedIn-yes")

            "have the id 'propertyLivedIn-Yes'" in {
              optionLabel.attr("id") shouldEqual "propertyLivedIn-yes"
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

          lazy val NoRadioOption = document.select(".block-label[for=propertyLivedIn-no]")

          "have a label with class 'block-label'" in {
            NoRadioOption.hasClass("block-label") shouldEqual true
          }

          "have the property 'for'" in {
            NoRadioOption.hasAttr("for") shouldEqual true
          }

          "the for attribute has the value propertyLivedIn-No" in {
            NoRadioOption.attr("for") shouldEqual "propertyLivedIn-no"
          }

          "have the text 'No'" in {
            NoRadioOption.text shouldEqual "No"
          }

          "have an input under the label that" should {

            lazy val optionLabel = document.select("#propertyLivedIn-no")

            "have the id 'propertyLivedIn-No'" in {
              optionLabel.attr("id") shouldEqual "propertyLivedIn-no"
            }

            "have the value 'No'" in {
              optionLabel.attr("value") shouldEqual "No"
            }

            "be of type radio" in {
              optionLabel.attr("type") shouldEqual "radio"
            }
          }
        }
      }

      "have a continue button" which {

        lazy val button = document.select("button")

        "has class 'button'" in {
          button.hasClass("button") shouldEqual true
        }

        "has attribute 'type'" in {
          button.hasAttr("type") shouldEqual true
        }

        "has type value of 'submit'" in {
          button.attr("type") shouldEqual "submit"
        }

        "has attribute id" in {
          button.hasAttr("id") shouldEqual true
        }

        "has id equal to continue-button" in {
          button.attr("id") shouldEqual "continue-button"
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
      lazy val view = propertyLivedInView(propertyLivedInForm.fill(PropertyLivedInModel(true)))(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "for the option 'Yes'" should {

        lazy val YesRadioOption = document.select(".block-label[for=propertyLivedIn-yes]")

        "have the option auto-selected" in {
          YesRadioOption.attr("class") shouldBe "block-label selected"
        }
      }
    }

    "Property Lived In view with form errors" should {

      lazy val form = propertyLivedInForm.bind(Map("propertyLivedIn" -> ""))
      lazy val view = propertyLivedInView(form)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" which {
        "display an error summary message for the page" in {
          document.body.select("#propertyLivedIn-error-summary").size shouldBe 1
        }

        "display an error message for the input" in {
          document.body.select(".form-group .error-notification").size shouldBe 1
        }
      }
    }
  }
}

