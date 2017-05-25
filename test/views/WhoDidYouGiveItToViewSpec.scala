/*
 * Copyright 2017 HM Revenue & Customs
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

import assets.MessageLookup.{Resident, WhoDidYouGiveItTo}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.whoDidYouGiveItTo
import assets.MessageLookup.{WhoDidYouGiveItTo => messages}
import assets.MessageLookup.{Resident => commonMessages}
import forms.WhoDidYouGiveItToForm._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current


class WhoDidYouGiveItToViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {
  "Property Recipient view" should {

    lazy val view = whoDidYouGiveItTo(whoDidYouGiveItToForm)(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title of ${messages.title}" in {
      doc.title() shouldBe messages.title
    }

    "have a back button that" should {
      lazy val backLink = doc.select("a#back-link")

      "have the correct back link text" in {
        backLink.text shouldBe commonMessages.back
      }

      "have the back-link class" in {
        backLink.hasClass("back-link") shouldBe true
      }

      "have a link to Did You Sell or Give Away" in {
        backLink.attr("href") shouldBe controllers.routes.SoldOrGivenAwayController.soldOrGivenAway().toString()
      }
    }

    "have a H1 tag that" should {
      lazy val heading = doc.select("h1")

      s"have the page heading '${messages.title}'" in {
        heading.text shouldBe messages.title
      }
      "have the heading-large class" in {
        heading.hasClass("heading-large") shouldBe true
      }
    }

    "have a form" which {
      lazy val form = doc.getElementsByTag("form")

      s"has the action '${controllers.routes.WhoDidYouGiveItToController.submitWhoDidYouGiveItTo().toString}" in {
        form.attr("action") shouldBe controllers.routes.WhoDidYouGiveItToController.submitWhoDidYouGiveItTo().toString
      }

      "has the method of POST" in {
        form.attr("method") shouldBe "POST"
      }

    }

    "have additional content that" should {
      s"have a label for the Your Spouse or Civil Partner option" in {
        doc.select("label:nth-of-type(1)").text() shouldEqual messages.spouse
      }

      s"have a label for the A Charity option" in {
        doc.select("label:nth-of-type(2)").text() shouldEqual messages.charity
      }

      s"have a label for the Someone Else option" in {
        doc.select("label:nth-of-type(3)").text() shouldEqual messages.other
      }
    }

    "has a continue button that" should {
      lazy val continueButton = doc.select("button#continue-button")

      s"have the button text '${commonMessages.continue}'" in {
        continueButton.text shouldBe commonMessages.continue
      }

      "be of type submit" in {
        continueButton.attr("type") shouldBe "submit"
      }

      "have the class 'button'" in {
        continueButton.hasClass("button") shouldBe true
      }
    }
  }

  "WhoDidYouGiveItToView with form with errors" should {
    lazy val form = whoDidYouGiveItToForm.bind(Map("whoDidYouGiveItTo" -> ""))
    lazy val view = whoDidYouGiveItTo(form)(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message regarding incorrect value being inputted" in {
      doc.body.select(".form-group .error-notification").size shouldBe 1
    }
  }
}
