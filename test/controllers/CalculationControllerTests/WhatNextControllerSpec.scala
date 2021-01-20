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

package controllers.CalculationControllerTests

import akka.stream.Materializer
import assets.MessageLookup
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import constructors.AnswersConstructor
import controllers.WhatNextController
import controllers.helpers.FakeRequestHelper
import models._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import org.mockito.Mockito._

import scala.concurrent.Future

class WhatNextControllerSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc: HeaderCarrier = mock[HeaderCarrier]
  val mockHttp = mock[DefaultHttpClient]
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  lazy val materializer = mock[Materializer]
  val mockMessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val mockAnswersConstructor = mock[AnswersConstructor]

  def setupTarget(summary: TotalGainAnswersModel): WhatNextController = {
    when(mockAnswersConstructor.getNRTotalGainAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(summary))

    new WhatNextController(http = mock[DefaultHttpClient], mockAnswersConstructor, mockConfig, fakeApplication, mockMessagesControllerComponents)
  }

  lazy val answerModel = TotalGainAnswersModel(
    DateModel(6, 4, 2020),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(0),
    DisposalCostsModel(0),
    None,
    None,
    AcquisitionValueModel(0),
    AcquisitionCostsModel(0),
    DateModel(7, 4, 2020),
    None,
    None,
    ImprovementsModel("No", None, None),
    None
  )

  "Calling .whatNext" when {
    lazy val target = setupTarget(answerModel)
    "provided with a valid request" should {
      lazy val result = target.whatNext(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result)(materializer))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the whatNext page" in {
        document.title() shouldBe MessageLookup.WhatNext.title
        document.body().select("a.button").attr("href") shouldBe "/capital-gains-tax-uk-property/start/report-pay-capital-gains-tax-uk-property"
      }
    }

    "provided with no valid session" should {
      lazy val result = target.whatNext(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect the user to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

}
