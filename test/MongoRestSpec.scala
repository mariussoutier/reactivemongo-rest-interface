import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

@RunWith(classOf[JUnitRunner])
class MongoRestSpec extends Specification {

  // TODO Add embedded Mongo

  "GET requests" should {

    "return 500 ISE for invalid database and collection names" in new WithApplication {
      val invalid = route(FakeRequest(GET, "/wurst/wurst1234")).get
      status(invalid) must equalTo(NOT_FOUND)
    }

    "return 200 OK and the JSON rows for existing database" in new WithApplication {
      val request = route(FakeRequest(GET, "/test/something")).get
      status(request) must equalTo(OK)
    }

  }

}
