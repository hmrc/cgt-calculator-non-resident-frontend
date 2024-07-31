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

lazy val appName = "cgt-calculator-non-resident-frontend"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(CodeCoverageSettings.settings *)
  .settings(majorVersion := 1)
  .settings(PlayKeys.playDefaultPort := 9902)
  .settings(
    scalaVersion := "2.13.12",
    libraryDependencies ++= AppDependencies(),
  )
  .settings(
    scalacOptions.+=("-Wconf:src=html/.*:s"), //suppresses warnings in twirl files and routes.
    scalacOptions.+=("-Wconf:src=routes/.*:s"), //these warnings are loud and inconsequential.
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings) *)
  .settings(TwirlKeys.templateImports ++= Seq(
    "uk.gov.hmrc.govukfrontend.views.html.components._",
    "uk.gov.hmrc.hmrcfrontend.views.html.components._",
    "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
    "uk.gov.hmrc.govukfrontend.views.html.components.implicits._"
  ))
