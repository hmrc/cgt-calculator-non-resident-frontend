/*
 * Copyright 2019 HM Revenue & Customs
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

import assets.MessageLookup.NonResident.{SoldForLess => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.SoldForLessForm._
import org.jsoup.Jsoup
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.soldForLess

class SoldForLessViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)


  "The Sold for Less view spec"  when {

    "supplied with no errors" should {

      lazy val view = soldForLess(soldForLessForm)(fakeRequest, mockMessage,fakeApplication,mockConfig)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '${messages.question}'" in {
        document.title() shouldBe messages.question
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has a class of 'back-link'" in {
          backLink.attr("class") shouldBe "back-link"
        }

        "has the text" in {
          backLink.text shouldBe commonMessages.back
        }

        s"has a route to 'sold-or-given-away'" in {
          backLink.attr("href") shouldBe controllers.routes.SoldOrGivenAwayController.soldOrGivenAway().url
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

        s"has the text '${messages.question}'" in {
          heading.text shouldBe messages.question
        }
      }

      s"have the legend" which {

        lazy val legend = document.select("legend")

        s"has the question ${messages.question}" in {
          legend.text shouldEqual messages.question
        }

        "has the class visuallyhidden" in {
          document.body.select("legend").hasClass("visuallyhidden") shouldBe true
        }
      }

      "have inputs containing the id 'soldForLess'" in {
        document.body().select("input").attr("id") should include("soldForLess")
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.SoldForLessController.submitSoldForLess().url}'" in {
          form.attr("action") shouldBe controllers.routes.SoldForLessController.submitSoldForLess().url
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
    }

    "supplied with a form with errors" should {

      lazy val form = soldForLessForm.bind(Map("soldForLess" -> "a"))
      lazy val view = soldForLess(form)(fakeRequest, mockMessage,fakeApplication,mockConfig)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }

  }

}
