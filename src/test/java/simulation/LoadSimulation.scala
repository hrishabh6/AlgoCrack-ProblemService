import com.intuit.karate.gatling.PreDef._
import io.gatling.core.Predef._
import scala.concurrent.duration._

class LoadSimulation extends Simulation {

  val protocol = karateProtocol()

  val createQuestion = scenario("Create Question Load Test")
    .exec(karateFeature("classpath:karate/questions.feature@create"))

  val getQuestion = scenario("Get Question Load Test")
    .exec(karateFeature("classpath:karate/questions.feature@get"))

  val updateQuestion = scenario("Update Question Load Test")
    .exec(karateFeature("classpath:karate/questions.feature@update"))

  val deleteQuestion = scenario("Delete Question Load Test")
    .exec(karateFeature("classpath:karate/questions.feature@delete"))

  setUp(
    createQuestion.inject(
      rampUsers(50) during (30.seconds)
    ),
    getQuestion.inject(
      constantUsersPerSec(20) during (1.minute)
    ),
    updateQuestion.inject(
      rampUsersPerSec(10).to(40).during(30.seconds)
    ),
    deleteQuestion.inject(
      atOnceUsers(10)
    )
  ).protocols(protocol)
}
