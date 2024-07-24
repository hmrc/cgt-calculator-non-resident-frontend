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
import views.html.calculation.otherReliefsFlat

class OtherReliefsFlatViewSpec
    extends CommonPlaySpec
    with WithCommonFakeApplication
    with MockitoSugar
    with FakeRequestHelper {

  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage: Messages = fakeApplication.injector
    .instanceOf[MessagesControllerComponents]
    .messagesApi
    .preferred(fakeRequest)
  lazy val otherReliefsFlatView: otherReliefsFlat =
    fakeApplication.injector.instanceOf[otherReliefsFlat]

  "The Other Reliefs Flat view" when {
    "not supplied with a pre-existing stored value and a taxable gain" should {
      lazy val view = otherReliefsFlatView(
        otherReliefsForm,
        hasExistingReliefAmount = false,
        BigDecimal(2000),
        BigDecimal(2500)
      )(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have a back link" which {
        lazy val backLink = document.select(".govuk-back-link")

        "should have the text" in {
          backLink.text shouldEqual messages.back
        }

        s"should have a route to 'calculation-election'" in {
          backLink.attr("href") shouldEqual "#"
        }
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-xlarge" in {
          heading.attr("class") shouldBe "govuk-heading-xl"
        }

        s"has the text '${messages.serviceName}'" in {
          heading.text shouldBe messages.OtherReliefs.question
        }
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate.url}'" in {
        document
          .getElementsByClass(
            "hmrc-header__service-name hmrc-header__service-name--linked"
          )
          .attr(
            "href"
          ) shouldEqual controllers.routes.DisposalDateController.disposalDate.url
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.routes.OtherReliefsFlatController.otherReliefsFlat.url}'" in {
          form.attr(
            "action"
          ) shouldBe controllers.routes.OtherReliefsFlatController.otherReliefsFlat.url
        }
      }

      s"have a label" which {
        lazy val label = document.body.select("label").first()
        s"has the text '${messages.OtherReliefs.question}'" in {
          label.text shouldBe messages.OtherReliefs.question
        }
      }

      s"have the help text '${messages.OtherReliefs.help}'" in {
        document.body
          .getElementsByClass("govuk-hint")
          .text() should include(messages.OtherReliefs.help)
      }

      "have additional content" which {
        lazy val content = document.select("form > div")

        "has a class of panel-indent gain-padding" in {
          content.attr("class") shouldBe "govuk-inset-text"
        }

        "has a list of class list" in {
          content.select("ul").attr("class") shouldBe "govuk-list"
        }

        "has a list entry with the total gain message and value" in {
          content
            .select("li#totalGain")
            .text() shouldBe s"${messages.OtherReliefs.totalGain} £2,500"
        }

        "has a list entry with the taxable gain message and value" in {
          content
            .select("li#taxableGain")
            .text() shouldBe s"${messages.OtherReliefs.taxableGain} £2,000"
        }
      }

      "have a button" which {
        lazy val button = document.getElementsByClass("govuk-button")

        "has the class 'button'" in {
          button.attr("class") shouldBe "govuk-button"
        }

        "has the id 'add-relief-button'" in {
          button.attr("id") shouldBe "add-relief-button"
        }

        s"has the text ${messages.OtherReliefs.addRelief}" in {
          button.text() shouldBe messages.OtherReliefs.addRelief
        }
      }

      "should produce the same output when render and f are called" in {
        otherReliefsFlatView.f(
          otherReliefsForm,
          false,
          BigDecimal(2000),
          BigDecimal(2500)
        )(fakeRequest, mockMessage) shouldBe otherReliefsFlatView.render(
          otherReliefsForm,
          hasExistingReliefAmount = false,
          BigDecimal(2000),
          BigDecimal(2500),
          fakeRequest,
          mockMessage
        )
      }
    }

    "supplied with a pre-existing stored value and a negative taxable gain" should {
      val map = Map("otherReliefs" -> "1000")
      lazy val view = otherReliefsFlatView(
        otherReliefsForm.bind(map),
        hasExistingReliefAmount = true,
        BigDecimal(-1000),
        BigDecimal(2000)
      )(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "has a list entry with the loss carried forward message and value" in {
        document
          .select("li#taxableGain")
          .text() shouldBe s"${messages.OtherReliefs.lossCarriedForward} £1,000"
      }

      "have a button" which {
        lazy val button = document.select("button")

        "has the class 'button'" in {
          button.attr("class") shouldBe "govuk-button"
        }

        "has the id 'update-relief-button'" in {
          button.attr("id") shouldBe "add-relief-button"
        }

        s"has the text ${messages.OtherReliefs.updateRelief}" in {
          button.text() shouldBe messages.OtherReliefs.updateRelief
        }
      }
    }

    "supplied with an invalid map" should {
      val map = Map("otherReliefs" -> "-1000")
      lazy val view = otherReliefsFlatView(
        otherReliefsForm.bind(map),
        hasExistingReliefAmount = true,
        BigDecimal(2000),
        BigDecimal(2000)
      )(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.getElementsByClass("govuk-error-summary").size() shouldBe 1
      }
    }
  }
}
