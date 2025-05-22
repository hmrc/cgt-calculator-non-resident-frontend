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

import assets.MessageLookup.NonResident.{DisposalDate => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.DisposalDateForm._
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.disposalDate

class DisposalDateViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {
  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  private val api: MessagesApi = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi
  implicit lazy val mockMessage: Messages = api.preferred(fakeRequest)
  lazy val disposalDateView: disposalDate = fakeApplication.injector.instanceOf[disposalDate]

  lazy val welshLanguage: Lang = Lang("cy")
  lazy val cyMockMessage: Messages = api.preferred(Seq(
    welshLanguage
  ))
  lazy val pageTitle: String = s"""${messages.question} - ${commonMessages.serviceName} - GOV.UK"""

  "The Disposal Date View" should {

    "return some HTML" which {

      lazy val view = disposalDateView(disposalDateForm)(fakeRequest, mockMessage, Lang("en"))
      lazy val document = Jsoup.parse(view.body)

      lazy val welshView = disposalDateView(disposalDateForm)(fakeRequest, cyMockMessage, welshLanguage)
      lazy val welshDocument = Jsoup.parse(welshView.body)

      s"have the title '$pageTitle" in {
        document.title shouldEqual pageTitle
      }

      s"have the heading ${messages.question} " in {
        document.body.getElementsByTag("h1").text shouldEqual messages.question
      }

      s"have the Welsh language option on the first page" in {
        document.body.select("body > div > nav > ul > li:nth-child(2) > a").attr("href") shouldEqual "/calculate-your-capital-gains/non-resident/hmrc-frontend/language/cy"
      }

      s"have the English language option selected on the first page" in {
        document.body.select("body > div > nav > ul > li:nth-child(1) > span").text() should include("English")
      }

      s"have the English language option on the first page when viewed in Welsh" in {
        welshDocument.body.select("body > div > nav > ul > li:nth-child(1) > a").attr("href") shouldEqual "/calculate-your-capital-gains/non-resident/hmrc-frontend/language/en"
      }

      s"have the Welsh language option selected on the first page when viewed in Welsh" in {
        welshDocument.body.select("body > div > nav > ul > li:nth-child(2) > span").text() should include("Cymraeg")
      }

      s"have the question '${messages.question}'" in {
        document.body.getElementsByTag("fieldset").text should include(messages.question)
      }

      "have inputs using the id acquisitionDate" in {
        document.body().getElementsByClass("govuk-date-input").attr("id") should include("disposalDate")
      }

      s"have a home link to '${controllers.routes.DisposalDateController.disposalDate.url}'" in {
        document.select("body > header > div > div > div.govuk-header__content > a").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate.url
      }

      "have a button" which {
        lazy val button = document.select("button")

        "has the class 'button'" in {
          button.attr("class") shouldBe "govuk-button"
        }

        "has the id 'continue-button'" in {
          button.attr("id") shouldBe "submit"
        }

        "has the text 'Continue'" in {
          button.text shouldEqual commonMessages.continue
        }
      }

      "should produce the same output when render and f are called" in {
        disposalDateView.f(disposalDateForm)(fakeRequest, mockMessage, Lang("en")) shouldBe disposalDateView.render(disposalDateForm, fakeRequest, mockMessage, Lang("en"))
      }
    }

    "supplied with errors" should {
      lazy val form = disposalDateForm.bind(Map("disposalDateDay" -> "a"))
      lazy val view = disposalDateView(form)(fakeRequest, mockMessage, Lang("en"))
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.getElementsByClass("govuk-error-summary").size() shouldBe 1
      }
    }
  }
}
