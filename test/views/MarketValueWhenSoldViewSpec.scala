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

import assets.MessageLookup
import assets.MessageLookup.NonResident.{MarketValue => MarketValueMessages}
import assets.MessageLookup.{NonResident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.MarketValueWhenSoldForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.marketValueSold

class MarketValueWhenSoldViewSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val marketValueSoldView = fakeApplication.injector.instanceOf[marketValueSold]


  "The market value when gave away page" should {

    lazy val view = marketValueSoldView(marketValueForm)(fakeRequest, mockMessage)
    lazy val document = Jsoup.parse(view.body)

    "supplied with no errors" should {
      s"have a title of ${MarketValueMessages.disposalSoldQuestion}" in {
        document.title() shouldBe MarketValueMessages.disposalSoldQuestion
      }

      s"have a header" which {
        lazy val header = document.select("h1")
        s"has the text '${MarketValueMessages.disposalSoldQuestion}'" in {
          header.text() shouldBe MarketValueMessages.disposalSoldQuestion
        }

        s"has the class 'head-xlarge'" in {
          header.attr("class") shouldBe "heading-xlarge"
        }
      }

      s"have a paragraph" which {
        lazy val helpText = document.select("p.form-hint")
        s"has the help text'${MarketValueMessages.disposalHelpText}'" in {
          helpText.html() shouldBe MarketValueMessages.disposalHelpText +
            " " + MarketValueMessages.disposalHelpTextAdditional
        }
        s"has the class 'form-hint'" in {
          helpText.attr("class") shouldBe "form-hint"
        }
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has a class of 'back-link'" in {
          backLink.attr("class") shouldBe "back-link"
        }

        "has the text" in {
          backLink.text shouldBe commonMessages.back
        }

        s"has a route to 'sold-for-less'" in {
          backLink.attr("href") shouldBe controllers.routes.SoldForLessController.soldForLess().url
        }
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.MarketValueWhenSoldOrGaveAwayController.submitMarketValueWhenSold()}'" in {
          form.attr("action") shouldBe controllers.routes.MarketValueWhenSoldOrGaveAwayController.submitMarketValueWhenSold().url
        }

        s"has the hidden text ${MessageLookup.NonResident.MarketValue.disposalSoldQuestion}" in {
          form.select("div.visuallyhidden").text() shouldBe MessageLookup.NonResident.MarketValue.disposalSoldQuestion
        }

        s"has the input ID disposalValue" in {
          form.select("input").attr("id") shouldBe "disposalValue"
        }

        s"that has a paragraph with the text ${MarketValueMessages.jointOwnership}" in {
          document.select("p.panel-indent").text shouldBe MarketValueMessages.jointOwnership
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

      "should produce the same output when render and f are called" in {
        marketValueSoldView.f(marketValueForm)(fakeRequest, mockMessage) shouldBe marketValueSoldView.render(marketValueForm, fakeRequest, mockMessage)
      }
    }

    "supplied with a form with errors" should {
      lazy val form = marketValueForm.bind(Map("disposalValue" -> "testData"))
      lazy val view = marketValueSoldView(form)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
