package models

import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class TotalGainAnswersModelSpec extends UnitSpec with MockitoSugar {

  "TotalGainAnswersModel" should {
    "write to Json" in {
      val outputJson = Json.parse(
        """
          |{
          |"disposalValue":500000,
          |"disposalCosts":20000,
          |"acquisitionValue":350000,
          |"acquisitionCosts":20000,
          |"improvements":9000,
          |"rebasedValue":450000,
          |"rebasedCosts":20000,
          |"disposalDate":"2017-05-12",
          |"acquisitionDate":"2014-08-14",
          |"improvementsAfterTaxStarted":1000
          |}
        """.
          stripMargin)

      val model = TotalGainAnswersModel()

      Json.toJson(model) shouldBe outputJson
    }
  }

}
