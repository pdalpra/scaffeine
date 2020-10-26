package com.github.blemale.scaffeine.example

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class LoadingCacheExample extends AnyFlatSpec with Matchers {

  "LoadingCache" should "be created from Scaffeine builder" in {
    import scala.concurrent.duration._

    import cats.effect.IO
    import com.github.blemale.scaffeine.{LoadingSyncCacheF, Scaffeine}
    import com.github.blemale.scaffeine.catseffect._

    val cache: LoadingSyncCacheF[IO, Int, String] =
      Scaffeine()
        .recordStats()
        .expireAfterWrite(1.hour)
        .maximumSize(500)
        .buildSyncF(i => s"foo$i")

    cache.get(1).unsafeRunSync() should be("foo1")

    cache.put(2, "bar").unsafeRunSync()
    cache.get(2).unsafeRunSync() should be("bar")
  }
}
