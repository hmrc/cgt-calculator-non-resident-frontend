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

import assets.MessageLookup.NonResident.{DisposalDate => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.DisposalDateForm._
import org.jsoup.Jsoup
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.language.LanguageUtils
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.disposalDate

class DisposalDateViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  private val api: MessagesApi = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi
  implicit lazy val mockMessage = api.preferred(fakeRequest)

  lazy val welshLanguage: Lang = Lang("cy")
  lazy val cyMockMessage = api.preferred(Seq(
    welshLanguage
  ))

  "The Disposal Date View" should {

    "return some HTML" which {

      lazy val view = disposalDate(disposalDateForm)(fakeRequest, mockMessage, Lang("en"), fakeApplication, mockConfig)
      lazy val document = Jsoup.parse(view.body)

      lazy val welshView = disposalDate(disposalDateForm)(fakeRequest, cyMockMessage, welshLanguage, fakeApplication, mockConfig)
      lazy val welshDocument = Jsoup.parse(welshView.body)

      "have the title 'When did you sign the contract that made someone else the owner?'" in {
        document.title shouldEqual messages.question
      }

      s"have the heading ${messages.question} " in {
        document.body.getElementsByTag("h1").text shouldEqual messages.question
      }

      s"have the Welsh language option on the first page" in {
        document.body.getElementById("cymraeg-switch").attr("href") shouldEqual "/calculate-your-capital-gains/non-resident/language/cymraeg"
      }

      s"have the English language option selected on the first page" in {
        document.body.getElementById("cymraeg-switch").parent().text() should include("English |")
      }

      s"have the English language option on the first page when viewed in Welsh" in {
        welshDocument.body.getElementById("english-switch").attr("href") shouldEqual "/calculate-your-capital-gains/non-resident/language/english"
      }

      s"have the Welsh language option selected on the first page when viewed in Welsh" in {
        welshDocument.body.getElementById("english-switch").parent().text() should include("| Cymraeg")
      }

      s"have the question '${messages.question}'" in {
        document.body.getElementsByTag("fieldset").text should include(messages.question)
      }

      "have inputs using the id acquisitionDate" in {
        document.body().select("input[type=number]").attr("id") should include("disposalDate")
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate().url
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

        "has the text 'Continue'" in {
          button.text shouldEqual commonMessages.continue
        }
      }

      "should produce the same output when render and f are called" in {
        disposalDate.f(disposalDateForm)(fakeRequest, mockMessage, Lang("en"), fakeApplication, mockConfig) shouldBe disposalDate.render(disposalDateForm, fakeRequest, mockMessage, Lang("en"), fakeApplication, mockConfig)
      }
    }

    "supplied with errors" should {
      lazy val form = disposalDateForm.bind(Map("disposalDateDay" -> "a"))
      lazy val view = disposalDate(form)(fakeRequest, mockMessage, Lang("en"), fakeApplication, mockConfig)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
