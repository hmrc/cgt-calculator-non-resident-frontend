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
import controllers.routes
import forms.IsClaimingImprovementsForm.isClaimingImprovementsForm
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.isClaimingImprovements

class IsClaimingImprovementsViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val isClaimingImprovementsView: isClaimingImprovements = fakeApplication.injector.instanceOf[isClaimingImprovements]
  lazy val headingBeforeLegislationStart: String = messages.IsClaimingImprovements.ownerBeforeLegislationStartQuestion
  lazy val headingAfterLegislationStart: String = messages.IsClaimingImprovements.title
  lazy val pageTitle = s"$headingAfterLegislationStart - ${messages.serviceName} - GOV.UK"
  lazy val pageTitleOwnerBeforeLegislationStart = s"$headingBeforeLegislationStart - ${messages.serviceName} - GOV.UK"


  "IsClaimingImprovements view" should {

    "render with no errors when is owner after legislation start" should {

      lazy val view = isClaimingImprovementsView(isClaimingImprovementsForm, ownerBeforeLegislationStart = false)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "return some HTML that" should {

        s"has the title of $pageTitle" in {
          document.title shouldBe pageTitle
        }

        s"has the heading of $headingAfterLegislationStart" in {
          document.body().getElementsByTag("h1").first().text shouldBe headingAfterLegislationStart
        }

        s"has paragraph content above the form" in {
          document.body().select("#main-content p.govuk-body").first().text shouldBe
            messages.IsClaimingImprovements.helpOne + " " + messages.IsClaimingImprovements.helpTwo
          document.body().select("#main-content p.govuk-body").last().text shouldBe
            messages.IsClaimingImprovements.exampleOne + " " + messages.IsClaimingImprovements.exampleTwo
        }

        "have a back link" which {

          lazy val backLink = document.body().select(".govuk-back-link")

          s"has the text ${messages.back}" in {
            backLink.text shouldEqual messages.back
          }

          "has a class of 'back-link'" in {
            backLink.attr("class") shouldBe "govuk-back-link"
          }

          s"has a route to 'back-link'" in {
            backLink.attr("href") shouldBe "#"
          }
        }

        s"have a home link to '${controllers.routes.DisposalDateController.disposalDate.url}'" in {
          document.getElementsByClass("hmrc-header__service-name hmrc-header__service-name--linked").attr("href") shouldEqual controllers.routes.DisposalDateController.disposalDate.url
        }

        "have a form" which {
          lazy val form = document.body().select("form")

          "has a method of POST" in {
            form.attr("method") shouldBe "POST"
          }

          s"has an action of '${routes.ImprovementsController.submitIsClaimingImprovements.url}'" in {
            form.attr("action") shouldBe controllers.routes.ImprovementsController.submitIsClaimingImprovements.url
          }
        }

        "have a button" which {
          lazy val button = document.select("button")

          "has the class 'button'" in {
            button.attr("class") shouldBe "govuk-button"
          }

          "has the id 'submit'" in {
            button.attr("id") shouldBe "submit"
          }
        }
      }

      "should produce the same output when render and f are called" in {
        isClaimingImprovementsView.f(isClaimingImprovementsForm, false)(fakeRequest, mockMessage) shouldBe
          isClaimingImprovementsView.render(isClaimingImprovementsForm, ownerBeforeLegislationStart = false, fakeRequest, mockMessage)
      }
    }

    "render with no errors when is owner before legislation start" should {

      lazy val view = isClaimingImprovementsView(isClaimingImprovementsForm, ownerBeforeLegislationStart = true)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "return some HTML that" should {

        "have that content" which {

          s"has the title of $pageTitleOwnerBeforeLegislationStart" in {
            document.title shouldBe pageTitleOwnerBeforeLegislationStart
          }

          s"has the heading of $headingBeforeLegislationStart" in {
            document.body().getElementsByTag("h1").first.text shouldBe headingBeforeLegislationStart
          }

          s"has yes/no radio inputs" in {
            lazy val labels = document.body().select("label.govuk-label")
            labels.first().text shouldBe "Yes"
            labels.last().text shouldBe "No"
          }

          "have a legend that is visually hidden" should {

            lazy val legend = document.select("legend.govuk-visually-hidden")

            s"have the text $headingBeforeLegislationStart}" in {
              legend.text shouldEqual headingBeforeLegislationStart
            }
          }
        }
      }
    }

    "supplied with errors" should {
      lazy val form = isClaimingImprovementsForm.bind(Map("isClaimingImprovements" -> ""))
      lazy val view = isClaimingImprovementsView(form, ownerBeforeLegislationStart = true)(fakeRequest, mockMessage)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.getElementsByClass("govuk-error-summary").size() shouldBe 1
      }
    }
  }
}
