import com.intuit.karate.gatling.PreDef._
import io.gatling.core.Predef._
import scala.concurrent.duration._

class SpikeSimulation extends Simulation {

  val protocol = karateProtocol()

  val spikeTest = scenario("Spike Test - Sudden Traffic")
    .exec(karateFeature("classpath:karate/questions.feature"))

  setUp(
    spikeTest.inject(
      nothingFor(3.seconds),
      atOnceUsers(1000) // sudden spike
    ).protocols(protocol)
  )
}
