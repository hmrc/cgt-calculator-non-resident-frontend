import sbt._

private object AppDependencies {
  import play.sbt.PlayImport._
  import play.core.PlayVersion

  private val scope = "test"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-26" % "1.3.0",
    "uk.gov.hmrc" %% "play-partials" % "6.9.0-play-26",
    "uk.gov.hmrc" %% "http-caching-client" % "9.0.0-play-26" exclude("uk.gov.hmrc", "json-encryption"),
    "uk.gov.hmrc" %% "mongo-caching" % "6.6.0-play-26",
    "uk.gov.hmrc" %% "play-language" % "3.4.0",
    "it.innove"   % "play2-pdf" % "1.5.2",
    "uk.gov.hmrc" %% "govuk-template" % "5.48.0-play-26",
    "uk.gov.hmrc" %% "play-ui" % "8.7.0-play-26",
    "com.typesafe.play" %% "play-java" % "2.6.25",
    nettyServer
  )

  val test = Seq(
    "uk.gov.hmrc" %% "hmrctest" % "3.9.0-play-26" % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % scope,
    "org.mockito" % "mockito-core" % "3.2.4" % scope,
    "org.pegdown" % "pegdown" % "1.6.0" % scope,
    "org.jsoup" % "jsoup" % "1.12.1" % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope
  )

  def apply() = compile ++ test
}