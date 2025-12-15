import com.intuit.karate.gatling.PreDef._
import io.gatling.core.Predef._
import scala.concurrent.duration._

class StressSimulation extends Simulation {

  val protocol = karateProtocol()

  val stressTest = scenario("Stress Test - Extreme Load")
    .exec(karateFeature("classpath:karate/questions.feature"))

  setUp(
    stressTest.inject(
      rampUsersPerSec(10).to(300).during(2.minutes),
      stressPeakUsers(2000).during(30.seconds)
    ).protocols(protocol)
  )
}
