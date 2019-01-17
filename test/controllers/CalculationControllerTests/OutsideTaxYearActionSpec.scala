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

package controllers.CalculationControllerTests

import controllers.helpers.FakeRequestHelper
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.test.Helpers._
import assets.MessageLookup.{OutsideTaxYears => messages}
import config.AppConfig
import connectors.CalculatorConnector
import controllers.OutsideTaxYearController
import models.{DateModel, TaxYearModel}
import org.jsoup.Jsoup
import org.scalatest.mockito.MockitoSugar
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._

import scala.concurrent.Future

class OutsideTaxYearActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget(disposalDateModel: Option[DateModel], taxYearModel: Option[TaxYearModel]): OutsideTaxYearController = {
    val mockCalcConnector: CalculatorConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[DateModel](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(disposalDateModel))

    when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(taxYearModel))

    new OutsideTaxYearController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "Calling .outsideTaxYears from the outsideTaxYearController" when {

    "there is a valid session" should {
      lazy val target = setupTarget(Some(DateModel(10, 10, 2018)), Some(TaxYearModel("2018/19", false, "2018/19")))
      lazy val result = target.outsideTaxYear(fakeRequestWithSession)

      "return a 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a title of ${messages.title}" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }

      s"have a back link to '${controllers.routes.DisposalDateController.disposalDate().url}'" in {
        Jsoup.parse(bodyOf(result)).getElementById("back-link").attr("href") shouldBe controllers.routes.DisposalDateController.disposalDate().url
      }
    }

    "there is no valid session" should {
      lazy val result = OutsideTaxYearController.outsideTaxYear(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }
}
