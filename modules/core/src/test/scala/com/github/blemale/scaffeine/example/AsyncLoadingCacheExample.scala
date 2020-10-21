package com.github.blemale.scaffeine.example

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

class AsyncLoadingCacheExample
    extends AnyFlatSpec
    with Matchers
    with ScalaFutures {

  "AsyncLoadingCache" should "be created from Scaffeine builder with synchronous loader" in {
    import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}

    import scala.concurrent.duration._

    val cache: AsyncLoadingCache[Int, String] =
      Scaffeine()
        .recordStats()
        .expireAfterWrite(1.hour)
        .maximumSize(500)
        .buildAsync((i: Int) => s"foo$i")

    whenReady(cache.get(1))(value => value should be("foo1"))
  }

  "AsyncLoadingCache" should "be created from Scaffeine builder with asynchronous loader" in {
    import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}

    import scala.concurrent.duration._

    val cache: AsyncLoadingCache[Int, String] =
      Scaffeine()
        .recordStats()
        .expireAfterWrite(1.hour)
        .maximumSize(500)
        .buildAsyncFuture((i: Int) => Future.successful(s"foo$i"))

    whenReady(cache.get(1))(value => value should be("foo1"))
  }
}
