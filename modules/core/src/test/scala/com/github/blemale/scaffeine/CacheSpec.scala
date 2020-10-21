package com.github.blemale.scaffeine

import org.scalatest._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CacheSpec extends AnyWordSpec with Matchers with OptionValues {

  "Cache" should {
    "get value if present" in {
      val cache = Scaffeine().build[String, String]()
      cache.put("foo", "present")

      val valuePresent = cache.getIfPresent("foo")
      val valueAbsent  = cache.getIfPresent("bar")

      valuePresent.value should be("present")
      valueAbsent should be(None)
    }

    "get value or compute it when absent" in {
      val cache = Scaffeine().build[String, String]()
      cache.put("foo", "present")

      val present  = cache.get("foo", _ => "computed")
      val computed = cache.get("bar", _ => "computed")

      present should be("present")
      computed should be("computed")
    }

    "get all given values" in {
      val cache = Scaffeine().build[String, String]()
      cache.put("foo", "present")
      cache.put("bar", "present")

      val keyValues = cache.getAllPresent(List("foo", "bar", "baz"))

      keyValues should contain only ("foo" -> "present", "bar" -> "present")
    }

    "get or compute all values" in {
      val cache = Scaffeine().build[String, String]()
      cache.put("foo", "present")

      val keyValues =
        cache.getAll(List("foo", "bar"), _.map(key => (key, "computed")).toMap)

      keyValues should contain only ("foo" -> "present", "bar" -> "computed")
    }

    "put value" in {
      val cache = Scaffeine().build[String, String]()

      cache.put("foo", "present")

      val value = cache.getIfPresent("foo")

      value.value should be("present")
    }

    "put all given values" in {
      val cache = Scaffeine().build[String, String]()

      cache.putAll(Map("foo" -> "present", "bar" -> "present"))

      val fooValue = cache.getIfPresent("foo")
      val barValue = cache.getIfPresent("bar")

      fooValue.value should be("present")
      barValue.value should be("present")
    }

    "invalidate entry" in {
      val cache =
        Scaffeine()
          .executor(DirectExecutor)
          .build[String, String]()

      cache.put("foo", "present")
      cache.invalidate("foo")
      val fooValue = cache.getIfPresent("foo")

      fooValue should be(None)
    }

    "invalidate all given entries" in {
      val cache =
        Scaffeine()
          .executor(DirectExecutor)
          .build[String, String]()

      cache.put("foo", "present")
      cache.put("bar", "present")
      cache.invalidateAll(List("foo", "bar"))
      val values = cache.getAllPresent(List("foo", "bar"))

      values shouldBe empty
    }

    "invalidate all entries" in {
      val cache =
        Scaffeine()
          .executor(DirectExecutor)
          .build[String, String]()

      cache.put("foo", "present")
      cache.put("bar", "present")
      cache.invalidateAll()
      val values = cache.getAllPresent(List("foo", "bar"))

      values shouldBe empty
    }

    "estimate size" in {
      val cache = Scaffeine().build[String, String]()

      cache.put("foo", "present")
      cache.put("bar", "present")

      val estimateSize = cache.estimatedSize()

      estimateSize should be(2)
    }

    "give its stats" in {
      val cache = Scaffeine()
        .recordStats()
        .executor(DirectExecutor)
        .build[String, String]()

      cache.put("foo", "present")
      cache.getIfPresent("foo")
      cache.getIfPresent("bar")

      val stats = cache.stats()

      stats.requestCount should be(2)
      stats.hitRate should be(0.5 +- 0.01)
    }

    "be view as a map" in {
      val cache = Scaffeine().build[String, String]()

      cache.put("foo", "present")
      cache.put("bar", "present")

      val map = cache.asMap()

      map should contain only ("foo" -> "present", "bar" -> "present")
    }

    "give it policy" in {
      val cache =
        Scaffeine()
          .recordStats()
          .build[String, String]()

      val policy = cache.policy()

      policy.isRecordingStats should be(true)
    }
  }
}
