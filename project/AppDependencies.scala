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

  val bootstrapVersion         = "7.15.0"
  val playPartialsVersion      = "8.4.0-play-28"
  val httpCachingClientVersion = "10.0.0-play-28"
  val play2PdfVersion          = "1.11.0"
  val playLanguageVersion      = "6.2.0-play-28"
  val playFrontendVersion      = "7.5.0-play-28"

  val compile = Seq(
    ws,
    "uk.gov.hmrc"     %% "bootstrap-frontend-play-28"   % bootstrapVersion,
    "uk.gov.hmrc"     %% "play-frontend-hmrc"           % playFrontendVersion,
    "uk.gov.hmrc"     %% "play-partials"                % playPartialsVersion,
    "uk.gov.hmrc"     %% "http-caching-client"          % httpCachingClientVersion,
    "uk.gov.hmrc"     %% "play-language"                % playLanguageVersion,
    "it.innove"       % "play2-pdf"                     % play2PdfVersion exclude("com.typesafe.play","*"),
    nettyServer
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
        "org.mockito"             %  "mockito-core"              % "4.11.0"            % scope,
        "org.pegdown"             %  "pegdown"                   % "1.6.0"             % scope,
        "org.jsoup"               %  "jsoup"                     % "1.14.3"            % scope,
        "com.typesafe.play"       %% "play-test"                % PlayVersion.current % scope
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test()
}


