package com.github.blemale.scaffeine.example

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CacheExample extends AnyFlatSpec with Matchers {

  "Cache" should "be created from Scaffeine builder" in {
    import scala.concurrent.duration._

    import cats.effect.IO
    import com.github.blemale.scaffeine.{Scaffeine, SyncCacheF}
    import com.github.blemale.scaffeine.catseffect._

    val cache: SyncCacheF[IO, Int, String] =
      Scaffeine()
        .recordStats()
        .expireAfterWrite(1.hour)
        .maximumSize(500)
        .buildSyncF()

    cache.put(1, "foo").unsafeRunSync()

    cache.getIfPresent(1).unsafeRunSync() should be(Some("foo"))
    cache.getIfPresent(2).unsafeRunSync() should be(None)
  }
}
