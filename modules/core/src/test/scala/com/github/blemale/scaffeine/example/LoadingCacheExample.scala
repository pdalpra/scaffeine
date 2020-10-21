package com.github.blemale.scaffeine.example

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class LoadingCacheExample extends AnyFlatSpec with Matchers {

  "LoadingCache" should "be created from Scaffeine builder" in {
    import com.github.blemale.scaffeine.{LoadingCache, Scaffeine}

    import scala.concurrent.duration._

    val cache: LoadingCache[Int, String] =
      Scaffeine()
        .recordStats()
        .expireAfterWrite(1.hour)
        .maximumSize(500)
        .build((i: Int) => s"foo$i")

    cache.get(1) should be("foo1")
  }
}
