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

package views.nonResident

import assets.MessageLookup
import assets.MessageLookup.NonResident.{AllowableLosses => messages}
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.jsoup.Jsoup
import views.html.calculation.{nonresident => views}
import forms.nonresident.AllowableLossesForm._
import models.nonresident.AllowableLossesModel
import controllers.helpers.FakeRequestHelper

class AllowableLossesViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  "The allowable losses view" when {

    "not supplied with a pre-existing stored value" should {
      lazy val view = views.allowableLosses(allowableLossesForm, "back-link")(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "return some HTML that" should {

        "have a back link" which {

          "should have the text" in {
            document.body.getElementById("back-link").text shouldEqual MessageLookup.NonResident.back
          }

          s"should have a route too 'back-link'" in {
            document.body.getElementById("back-link").attr("href") shouldEqual "back-link"
          }
        }

        s"have a home link to '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
          document.select("#homeNavHref").attr("href") shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
        }

        "have the title 'Are you claiming any allowable losses?'" in {
          document.title shouldEqual messages.yesNoQuestion
        }

        s"have the heading ${MessageLookup.NonResident.pageHeading}'" in {
          document.body.getElementsByTag("H1").text shouldEqual MessageLookup.NonResident.pageHeading
        }

        "have a yes no helper" which {

          "has a label with yes" in {
            document.body.getElementById("isClaimingAllowableLosses-yes").parent.text shouldBe MessageLookup.NonResident.yes
          }

          "has a label with no" in {
            document.body.getElementById("isClaimingAllowableLosses-no").parent.text shouldBe MessageLookup.NonResident.no
          }

          s"has the question ${messages.yesNoQuestion}" in {
            document.body.getElementsByTag("legend").text shouldBe messages.yesNoQuestion
          }

        }

        "have an input" which {

          "has the name allowableLossesAmt" in {
            document.body.getElementById("allowableLossesAmt").attr("name") shouldEqual "allowableLossesAmt"
          }

          s"has the question ${messages.inputQuestion}" in {
            document.select("label[for=allowableLossesAmt]").text should include(messages.inputQuestion)
          }

          "has no value auto-filled" in {
            document.getElementById("allowableLossesAmt").attr("value") shouldBe empty
          }
        }

        "have a hidden help text section with summary 'What are allowable losses?' and correct content" which {

          lazy val helpText = document.select("div#allowableLossesHiddenHelp")

          s"should have the title ${messages.helpTextTitle}" in {
            helpText.text() should include(messages.helpTextTitle)
          }

          s"have the sub-paragraph ${messages.helpTextLead}" in {
            helpText.text() should include(messages.helpTextLead)
          }

          s"have the bullet point ${messages.helpTextBulletOne}" in {
            helpText.text() should include(messages.helpTextBulletOne)
          }

          s"have the bullet point ${messages.helpTextBulletTwo}" in {
            helpText.text() should include(messages.helpTextBulletTwo)
          }

          s"have the bullet point ${messages.helpTextBulletThree}" in {
            helpText.text() should include(messages.helpTextBulletThree)
          }
        }

        "has a Continue button" in {
          document.body.getElementById("continue-button").text shouldEqual MessageLookup.NonResident.continue
        }
      }
    }

    "supplied with a pre-existing stored model for yes" should {
      lazy val model = AllowableLossesModel("Yes", Some(9999.54))
      lazy val view = views.allowableLosses(allowableLossesForm.fill(model), "back-link")(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "return some HTML that" should {

        "have the 'Yes' Radio option selected" in {
          document.getElementById("isClaimingAllowableLosses-yes").parent.classNames().contains("selected") shouldBe true
        }

        "have the value 9999.54 auto-filled into the input box" in {
          document.getElementById("allowableLossesAmt").attr("value") shouldEqual "9999.54"
        }
      }
    }

    "supplied with a pre-existing stored model for no" should {
      lazy val model = AllowableLossesModel("No", None)
      lazy val view = views.allowableLosses(allowableLossesForm.fill(model), "back-link")(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "return some HTML that" should {

        "have the 'No' Radio option selected" in {
          document.getElementById("isClaimingAllowableLosses-no").parent.classNames().contains("selected") shouldBe true
        }

        "has an empty input box" in {
          document.getElementById("allowableLossesAmt").attr("value") shouldBe empty
        }
      }
    }

    "supplied with a number that is less than the minimum" should {

      //Just a note how this is working here;
      //The map is only binding the value for the amount but the isClaimingAllowableLosses is not supplied so it is the isClaiming
      //field that is raising the error.
      lazy val view = views.allowableLosses(allowableLossesForm.bind(Map(("allowableLossesAmt", ""))), "back-link")(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "return some HTML that" should {

        "display an error summary message for the amount" in {
          document.body.select("#isClaimingAllowableLosses-error-summary").size shouldBe 1
        }

        "display an error message for the input" in {
          document.body.select("span.error-notification").size shouldBe 1
        }
      }
    }
  }
}
