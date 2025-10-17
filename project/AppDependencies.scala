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

import sbt.*

object AppDependencies {
  val bootstrapVersion         = "10.2.0"
  val playVersion              = "play-30"
  val hmrcMongoVersion         = "2.9.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% s"bootstrap-frontend-$playVersion"             % bootstrapVersion,
    "uk.gov.hmrc"       %% s"play-frontend-hmrc-$playVersion"             % "12.17.0",
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo-$playVersion"                     % hmrcMongoVersion,
    "uk.gov.hmrc"       %% s"play-conditional-form-mapping-$playVersion"  % "3.3.0"
  )

  def test(scope: String = "test"): Seq[sbt.ModuleID] = Seq(
    "uk.gov.hmrc"             %% s"bootstrap-test-$playVersion"  % bootstrapVersion   % scope,
    "uk.gov.hmrc.mongo"       %% s"hmrc-mongo-test-$playVersion" % hmrcMongoVersion    % scope
  )
}


