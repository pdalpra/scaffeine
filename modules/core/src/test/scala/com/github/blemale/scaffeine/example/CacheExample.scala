package com.github.blemale.scaffeine.example

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CacheExample extends AnyFlatSpec with Matchers {

  "Cache" should "be created from Scaffeine builder" in {
    import com.github.blemale.scaffeine.{Cache, Scaffeine}

    import scala.concurrent.duration._

    val cache: Cache[Int, String] =
      Scaffeine()
        .recordStats()
        .expireAfterWrite(1.hour)
        .maximumSize(500)
        .build[Int, String]()

    cache.put(1, "foo")

    cache.getIfPresent(1) should be(Some("foo"))
    cache.getIfPresent(2) should be(None)
  }
}
