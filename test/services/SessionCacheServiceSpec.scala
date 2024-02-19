/*
 * Copyright 2023 HM Revenue & Customs
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

package services

import common.{CommonPlaySpec, KeystoreKeys, WithCommonFakeApplication}
import models.DisposalValueModel
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import repositories.SessionRepository
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.mongo.CurrentTimestampSupport
import uk.gov.hmrc.mongo.test.MongoSupport
import KeystoreKeys.{NonResidentKeys => Key}

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class SessionCacheServiceSpec extends CommonPlaySpec with WithCommonFakeApplication with MongoSupport with MockitoSugar {

  implicit val ec: ExecutionContext = fakeApplication.injector.instanceOf[ExecutionContext]
  val sessionId: String = UUID.randomUUID.toString
  val sessionPair: (String, String) = SessionKeys.sessionId -> sessionId
  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(sessionPair)
  val sessionRepository = new SessionRepository(mongoComponent = mongoComponent,
    config = fakeApplication.configuration, timestampSupport = new CurrentTimestampSupport())

  class Setup {
    val sessionCacheService = new SessionCacheService(sessionRepository)
  }

  "SessionCacheService" should {
    val testModel = DisposalValueModel(1000)

    "fetch and get from keystore" in new Setup {
      val saveResult: Future[(String, String)] = sessionCacheService.saveFormData(Key.disposalValue, testModel)
      await(saveResult)
      val fetchResult: Future[Option[DisposalValueModel]] = sessionCacheService.fetchAndGetFormData[DisposalValueModel](Key.disposalValue)
      await(fetchResult) shouldBe Some(testModel)
    }

    "save data to keystore and return the same session ID if it already exists" in new Setup {

      lazy val result = sessionCacheService.saveFormData(Key.disposalValue, testModel)
      await(result) shouldBe sessionPair
    }

    "clear the keystore" in new Setup {
      await(sessionCacheService.saveFormData(Key.disposalValue, testModel))
      await(sessionCacheService.fetchAndGetFormData[DisposalValueModel](Key.disposalValue)) shouldBe Some(testModel)
      await(sessionCacheService.clearSession)
      await(sessionCacheService.fetchAndGetFormData[DisposalValueModel](Key.disposalValue)) shouldBe None
    }
  }


}
