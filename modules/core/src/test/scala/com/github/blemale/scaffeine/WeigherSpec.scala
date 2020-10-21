package com.github.blemale.scaffeine

import org.scalatest._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class WeigherSpec extends AnyWordSpec with Matchers with OptionValues {

  "Cache" should {
    "use weigher for calculate size based eviction" in {
      val cache = Scaffeine()
        .executor(DirectExecutor)
        .weigher[String, String]((_, value) => value.length)
        .maximumWeight(10)
        .build[String, String]()

      cache.put("foo", "word1")
      cache.put("bar", "word2")
      cache.put("baz", "word3")

      cache.getIfPresent("foo") should be(None)
    }
  }

}
