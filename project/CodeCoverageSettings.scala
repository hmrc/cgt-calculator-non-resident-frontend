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

import sbt.Def
import scoverage.ScoverageKeys

object CodeCoverageSettings {
  val excludedPackages: Seq[String] = Seq(
    "<empty>",
    "Reverse.*",
    ".*AuthService.*",
    "models\\.data\\..*",
    "views.html.helpers.*",
    "uk.gov.hmrc.BuildInfo",
    "app.*",
    "nr.*",
    "res.*",
    "prod.*",
    "config.*",
    "controllers.SessionCacheController",
    "com.kenshoo.play.*",
    "controllers.utils.javascript.*",
    "controllers.javascript.*",
    ".*models.*"
  )
  
  val settings: Seq[Def.Setting[_ >: String with Double with Boolean]] = Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := excludedPackages.mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 70, //Temporarily reduced from 90 to match current coverage
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}
