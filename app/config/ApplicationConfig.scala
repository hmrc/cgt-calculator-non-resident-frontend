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

package config

import javax.inject.Inject
import play.api.{Configuration, Environment, Mode, Play}
import play.api.Play.{configuration, current}
import uk.gov.hmrc.play.config.ServicesConfig

trait AppConfig {
  val assetsPrefix: String
  val analyticsToken: String
  val analyticsHost: String
  val contactFormServiceIdentifier: String
  val contactFrontendPartialBaseUrl: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String
  val nrIFormLink: String
  val govUkLink: String
  val urBannerLink: String
}

class ApplicationConfig @Inject()(override val runModeConfiguration: Configuration,
                                  val environment: Environment) extends AppConfig with ServicesConfig {

  override def mode :Mode.Mode = environment.mode

  private def loadConfig(key: String) = runModeConfiguration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))
  private def getFeature(key: String) = runModeConfiguration.getBoolean(key).getOrElse(false)

  private lazy val contactFrontendService = baseUrl("contact-frontend")
  private lazy val contactHost = runModeConfiguration.getString(s"$env.microservice.services.contact-frontend.host").getOrElse("")

  override lazy val assetsPrefix: String = loadConfig(s"assets.url") + loadConfig(s"assets.version")
  override lazy val analyticsToken: String = loadConfig(s"google-analytics.token")
  override lazy val analyticsHost: String = loadConfig(s"google-analytics.host")

  override val contactFormServiceIdentifier = "CGT"
  override lazy val contactFrontendPartialBaseUrl = s"$contactFrontendService"
  override lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  override lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

  override lazy val nrIFormLink: String = loadConfig("links.non-resident-iForm")
  override lazy val govUkLink: String = loadConfig("links.gov-uk")
  override val urBannerLink = "https://signup.take-part-in-research.service.gov.uk/?utm_campaign=CGT_non_resident_summary&utm_source=Survey_Banner&utm_medium=other&t=HMRC&id=116"
}
