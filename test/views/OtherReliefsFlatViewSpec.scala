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

import assets.MessageLookup.{NonResident => messages}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import models.CalculationResultModel
import org.jsoup.Jsoup
import forms.OtherReliefsForm._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.otherReliefsFlat
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class OtherReliefsFlatViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]

  "The Other Reliefs Flat view" when {
    "not supplied with a pre-existing stored value and a taxable gain" should {
      lazy val view = otherReliefsFlat(otherReliefsForm, hasExistingReliefAmount = false, BigDecimal(2000), BigDecimal(2500))(fakeRequest, applicationMessages,fakeApplication,mockConfig)
      lazy val document = Jsoup.parse(view.body)

      "have a back link" which {
        lazy val backLink = document.select("#back-link")

        "should have the text" in {
          backLink.text shouldEqual messages.back
        }

        s"should have a route to 'calculation-election'" in {
          backLink.attr("href") shouldEqual
            controllers.routes.CalculationElectionController.calculationElection().url
        }
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-xlarge" in {
          heading.attr("class") shouldBe "heading-xlarge"
        }

        s"has the text '${messages.pageHeading}'" in {
          heading.text shouldBe messages.OtherReliefs.question
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

        s"has an action of '${controllers.routes.OtherReliefsFlatController.otherReliefsFlat().url}'" in {
          form.attr("action") shouldBe controllers.routes.OtherReliefsFlatController.otherReliefsFlat().url
        }
      }

      s"have a label" which {
        lazy val label = document.body.select("label").first()
        s"has the text '${messages.OtherReliefs.question}'" in {
          label.text shouldBe messages.OtherReliefs.question
        }
        "has the class 'visuallyhidden'" in {
          label.attr("class") shouldBe "visuallyhidden"
        }
      }

      s"have the help text '${messages.OtherReliefs.help}'" in {
        document.body.select("form span.form-hint").text() shouldBe messages.OtherReliefs.help
      }

      "have additional content" which {
        lazy val content = document.select("form > div")

        "has a class of panel-indent gain-padding" in {
          content.attr("class") shouldBe "panel-indent gain-padding"
        }

        "has a list of class list" in {
          content.select("ul").attr("class") shouldBe "list"
        }

        "has a list entry with the total gain message and value" in {
          content.select("li#totalGain").text() shouldBe s"${messages.OtherReliefs.totalGain} £2,500"
        }

        "has a list entry with the taxable gain message and value" in {
          content.select("li#taxableGain").text() shouldBe s"${messages.OtherReliefs.taxableGain} £2,000"
        }
      }

      "have a button" which {
        lazy val button = document.select("button")

        "has the class 'button'" in {
          button.attr("class") shouldBe "button"
        }

        "has the id 'add-relief-button'" in {
          button.attr("id") shouldBe "add-relief-button"
        }

        s"has the text ${messages.OtherReliefs.addRelief}" in {
          button.text() shouldBe messages.OtherReliefs.addRelief
        }
      }
    }

    "supplied with a pre-existing stored value and a negative taxable gain" should {
      val map = Map("otherReliefs" -> "1000")
      lazy val view = otherReliefsFlat(otherReliefsForm.bind(map), hasExistingReliefAmount = true, BigDecimal(-1000), BigDecimal(2000))(fakeRequest, applicationMessages,fakeApplication,mockConfig)
      lazy val document = Jsoup.parse(view.body)

      "has a list entry with the loss carried forward message and value" in {
        document.select("li#taxableGain").text() shouldBe s"${messages.OtherReliefs.lossCarriedForward} £1,000"
      }

      "have a button" which {
        lazy val button = document.select("button")

        "has the class 'button'" in {
          button.attr("class") shouldBe "button"
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
      val model = CalculationResultModel(100, 1000, -100, 18, 0, None, None, None)
      val map = Map("otherReliefs" -> "-1000")
      lazy val view = otherReliefsFlat(otherReliefsForm.bind(map), hasExistingReliefAmount = true, BigDecimal(2000), BigDecimal(2000))(fakeRequest, applicationMessages,fakeApplication,mockConfig)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
