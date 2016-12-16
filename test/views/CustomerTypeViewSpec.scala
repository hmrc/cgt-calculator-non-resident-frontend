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

import assets.MessageLookup.NonResident.{CustomerType => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import forms.CustomerTypeForm._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.customerType
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class CustomerTypeViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {



  "The Customer Type View" should {
    val dummyBackLink = controllers.routes.PrivateResidenceReliefController.privateResidenceRelief().url
    lazy val view = customerType(customerTypeForm, dummyBackLink)
    lazy val document = Jsoup.parse(view.body)

    "return some HTML that" which {

      s"have the title ${messages.question}" in {
        document.title shouldEqual messages.question
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-large" in {
          heading.attr("class") shouldBe "heading-xlarge"
        }

        s"has the text '${commonMessages.pageHeading}'" in {
          heading.text shouldBe messages.question
        }
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate().url
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.CustomerTypeController.submitCustomerType().url}'" in {
          form.attr("action") shouldBe controllers.routes.CustomerTypeController.submitCustomerType().url
        }
      }

      s"have a legend" which {
        lazy val legend = document.body.getElementsByTag("legend")
        s"has the help text ${messages.question}" in {
          legend.text shouldEqual messages.question
        }

        "has the class 'visually-hidden'" in {
          legend.attr("class") shouldBe "visuallyhidden"
        }

        s"have a back link" which {
          lazy val backLink = document.getElementById("back-link")
          s"has the href value ${dummyBackLink}" in {
            backLink.attr("href") shouldBe dummyBackLink
          }

          s"has the class 'back-link'" in {
            backLink.attr("class") shouldBe "back-link"
          }
        }

      }

      s"display a radio button with the option ${messages.individual}" in {
        document.body.getElementById("customerType-individual").parent.text shouldEqual messages.individual
      }

      "have the radio option `individual` not selected by default" in {
        document.body.getElementById("customerType-individual").parent.classNames().contains("selected") shouldBe false
      }

      s"display a radio button with the option ${messages.trustee}" in {
        document.body.getElementById("customerType-trustee").parent.text shouldEqual messages.trustee
      }

      "have the radio option `trustee` not selected by default" in {
        document.body.getElementById("customerType-trustee").parent.classNames().contains("selected") shouldBe false
      }

      s"display a radio button with the option ${messages.personalRep}" in {
        document.body.getElementById("customerType-personalrep").parent.text shouldEqual messages.personalRep
      }

      "have the radio option `personalrep` not selected by default" in {
        document.body.getElementById("customerType-personalrep").parent.classNames().contains("selected") shouldBe false
      }

      "display a 'Continue' button " in {
        document.body.getElementById("continue-button").text shouldEqual commonMessages.continue
      }
    }
  }
}
