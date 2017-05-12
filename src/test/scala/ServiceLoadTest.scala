import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class ServiceLoadTest extends Simulation {
  val baseUrl = "http://localhost:8080"

  val httpProtocol = http
    .baseURL(baseUrl)
    .inferHtmlResources()
    .userAgentHeader("Apache-HttpClient/4.1.1 (java 1.5)")

  val s = scenario("Simulation")
    .exec(http("request_0")
      .get("/lookup/service10")
    )

  setUp(s.inject(constantUsersPerSec(1100) during(10 seconds))).protocols(httpProtocol)
}