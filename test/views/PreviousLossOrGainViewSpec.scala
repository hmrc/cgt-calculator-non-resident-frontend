/*
 * Copyright 2016 HM Revenue & Customs
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

import assets.MessageLookup.NonResident.{PreviousLossOrGain => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import forms.PreviousLossOrGainForm._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.previousLossOrGain
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class PreviousLossOrGainViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val fr = fakeRequest

  "The PreviousLossOrGain view" should {

    lazy val view = previousLossOrGain(previousLossOrGainForm)
    lazy val document = Jsoup.parse(view.body)

    "return some HTML" which {
      s"has the title ${messages.question}" in {
        document.title shouldEqual messages.question
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-large" in {
          heading.attr("class") shouldBe "heading-xlarge"
        }
      }
    }

    "have a legend that" should {
      lazy val legend = document.body.select("legend")

      s"have the text of ${messages.question}" in {
        legend.text shouldBe messages.question
      }

      "be visually hidden" in {
        legend.hasClass("visuallyhidden") shouldEqual true
      }
    }

    "have a back button that" should {
      lazy val backLink = document.select("a#back-link")

      "have the correct back link text" in {
        backLink.text shouldBe commonMessages.back
      }

      "have the back-link class" in {
        backLink.hasClass("back-link") shouldBe true
      }

      "have a link to Other Properties" in {
        backLink.attr("href") shouldBe controllers.routes.OtherPropertiesController.otherProperties().url
      }
    }

    "has a form" which {
      lazy val form = document.getElementsByTag("form")

      s"has the action '${controllers.routes.PreviousGainOrLossController.submitPreviousGainOrLoss().toString}" in {
        form.attr("action") shouldBe controllers.routes.PreviousGainOrLossController.submitPreviousGainOrLoss().toString()
      }

      "has the method of POST" in {
        form.attr("method") shouldBe "POST"
      }
    }

    "has a series of options that" should {
      s"have a label for the Loss option" in {
        document.select("label:nth-of-type(1)").text() shouldEqual messages.loss
      }
      s"have a label for the Gain option" in {
        document.select("label:nth-of-type(2)").text() shouldEqual messages.gain
      }
      s"have a label for the Neither option" in {
        document.select("label:nth-of-type(3)").text() shouldEqual messages.neither
      }
    }

    "has a continue button that" should {
      lazy val continueButton = document.select("button#continue-button")

      s"have the button text '${commonMessages.continue}" in {
        continueButton.text shouldBe commonMessages.continue
      }

      "be of type submit" in {
        continueButton.attr("type") shouldBe "submit"
      }

      "have the class 'button'" in {
        continueButton.hasClass("button") shouldBe true
      }
    }

    "have a sidebar that" should{
      "have a link for help with CGT" which {
        lazy val linkOne = document.body.getElementById("capitalGainsTaxHelp")

        s"have the text ${messages.CGTlink}" in {
          linkOne.text shouldBe messages.CGTlink + " " + commonMessages.externalLink
        }

        "has the href to 'https://www.gov.uk/capital-gains-tax" in {
          linkOne.attr("href") shouldBe "https://www.gov.uk/capital-gains-tax"
        }

        "has a link with the class 'external-link'" in {
          linkOne.attr("class") shouldBe "external-link"
        }

        "has a link with a rel of 'external'" in {
          linkOne.attr("rel") shouldBe "external"
        }

        "has a link with a target of '_blank'" in {
          linkOne.attr("target") shouldBe "_blank"
        }
      }

      "have a link for help with Previous Tax Years" which {
        lazy val linkTwo = document.body.getElementById("previousTaxYearsHelp")

        s"have the text ${messages.previousTaxLink}" in {
          linkTwo.text shouldBe messages.previousTaxLink + " " + commonMessages.externalLink
        }

        s"link two should have an href to 'https://www.gov.uk/income-tax-rates/previous-tax-years'" in {
          linkTwo.attr("href") shouldEqual "https://www.gov.uk/income-tax-rates/previous-tax-years"
        }
        "has a link with the class 'external-link'" in {
          linkTwo.attr("class") shouldBe "external-link"
        }

        "has a link with a rel of 'external'" in {
          linkTwo.attr("rel") shouldBe "external"
        }

        "has a link with a target of '_blank'" in {
          linkTwo.attr("target") shouldBe "_blank"
        }
      }
    }

  }

  "PreviousLossOrGainView with form errors" should {
    val form = previousLossOrGainForm.bind(Map("previousLossOrGain" -> ""))
    lazy val view = previousLossOrGain(form)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message regarding incorrect value being inputted" in {
      doc.body.select(".form-group .error-notification").size shouldBe 1
    }
  }

}
