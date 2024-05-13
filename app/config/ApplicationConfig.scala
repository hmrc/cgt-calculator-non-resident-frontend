/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.Environment
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject

trait AppConfig {
  val assetsPrefix: String
  val contactFormServiceIdentifier: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String
  val nrIFormLink: String
  val govUkLink: String
  val urBannerLink: String
  val isWelshTranslationAvailable: Boolean
}

class ApplicationConfig @Inject()(val servicesConfig: ServicesConfig,
                                  val environment: Environment) extends AppConfig {
  private def loadConfig(key: String): String = servicesConfig.getString(key)

  lazy val contactFrontendService = servicesConfig.getConfString("contact-frontend.www", "")

  lazy val assetsPrefix: String = loadConfig("assets.url") + loadConfig("assets.version")

  lazy val contactFormServiceIdentifier = "CGT"
  lazy val reportAProblemPartialUrl = s"$contactFrontendService/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"$contactFrontendService/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

  lazy val nrIFormLink: String = loadConfig("links.non-resident-iForm")
  lazy val nrReportServiceLink: String = loadConfig("links.non-resident-report-service")
  lazy val govUkLink: String = loadConfig("links.gov-uk")
  lazy val urBannerLink = "https://signup.take-part-in-research.service.gov.uk/?utm_campaign=CGT_non_resident_summary&utm_source=Survey_Banner&utm_medium=other&t=HMRC&id=116"
  lazy val isWelshTranslationAvailable: Boolean = servicesConfig.getBoolean("microservice.services.features.welsh-translation")
  lazy val userResearchBannerEnabled: Boolean = servicesConfig.getBoolean(("user-research-banner.enabled"))

}
