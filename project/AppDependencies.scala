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

import sbt._
import play.sbt.PlayImport._
import play.core.PlayVersion

object AppDependencies {

  val compile = Seq(
    ws,
    "uk.gov.hmrc"     %% "bootstrap-frontend-play-27"      % "3.0.0",
    "uk.gov.hmrc"     %% "play-partials"          % "7.1.0-play-27",
    "uk.gov.hmrc"     %% "http-caching-client"    % "9.2.0-play-27",
    "uk.gov.hmrc"     %% "mongo-caching"          % "6.16.0-play-27",
    "uk.gov.hmrc"     %% "play-language"          % "4.7.0-play-27",
    "it.innove"       % "play2-pdf"               % "1.10.0" exclude("com.typesafe.play","*"),
    "uk.gov.hmrc"     %% "govuk-template"         % "5.61.0-play-27",
    "uk.gov.hmrc"     %% "play-ui"                % "8.20.0-play-27",
    nettyServer
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc"             %% "hmrctest"             % "3.9.0-play-26"     % scope,
        "org.scalatestplus.play"  %% "scalatestplus-play"   % "4.0.3"             % scope,
        "org.mockito"             % "mockito-core"          % "3.3.3"             % scope,
        "org.pegdown"             % "pegdown"               % "1.6.0"             % scope,
        "org.jsoup"               % "jsoup"                 % "1.13.1"            % scope,
        "com.typesafe.play"       %% "play-test"            % PlayVersion.current % scope
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test()
}


