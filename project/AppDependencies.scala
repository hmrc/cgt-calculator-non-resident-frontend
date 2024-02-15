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

import play.core.PlayVersion
import sbt.*

object AppDependencies {

  val bootstrapVersion         = "7.22.0"
  val playFrontendVersion      = "7.29.0-play-28"
  val hmrcMongoVersion         = "1.3.0"

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28"    % bootstrapVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc"            % playFrontendVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"            % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "play-conditional-form-mapping" % "1.13.0-play-28",
    "org.julienrf"      %% "play-json-derived-codecs"      % "10.1.0"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc"             %% "bootstrap-test-play-28"   % bootstrapVersion    % scope,
        "org.scalatestplus.play"  %% "scalatestplus-play"       % "5.1.0"             % scope,
        "org.scalatestplus"       %% "scalatestplus-mockito"    % "1.0.0-M2"          % scope,
        "org.mockito"             %  "mockito-core"              % "5.5.0"            % scope,
        "org.pegdown"             %  "pegdown"                   % "1.6.0"             % scope,
        "org.jsoup"               %  "jsoup"                     % "1.16.1"            % scope,
        "com.typesafe.play"       %% "play-test"                % PlayVersion.current % scope,
        "uk.gov.hmrc.mongo"       %%  "hmrc-mongo-test-play-28" % hmrcMongoVersion      % scope
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test()
}


