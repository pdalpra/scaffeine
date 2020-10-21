package com.github.blemale.scaffeine

import com.github.benmanes.caffeine.cache.stats.CacheStats
import com.github.benmanes.caffeine.cache.{Policy, Cache => CaffeineCache}

import scala.collection.JavaConverters._
import scala.compat.java8.FunctionConverters._

object SyncCacheF {

  def apply[F[_], K, V](cache: CaffeineCache[K, V])(implicit
      lift: Sync[F]
  ): SyncCacheF[F, K, V] =
    new SyncCacheF(cache)
}

class SyncCacheF[F[_], K, V](val underlying: CaffeineCache[K, V])(implicit
    lift: Sync[F]
) {

  /**
    * Returns the value associated with `key` in this cache, or `None` if there is no
    * cached value for `key`.
    *
    * @param key key whose associated value is to be returned
    * @return an option value containing the value to which the specified key is mapped,
    *         or `None` if this map contains no mapping for the key
    */
  def getIfPresent(key: K): F[Option[V]] =
    lift.lift(Option(underlying.getIfPresent(key)))

  /**
    * Returns the value associated with `key` in this cache, obtaining that value from
    * `mappingFunction` if necessary. This method provides a simple substitute for the
    * conventional "if cached, return; otherwise create, cache and return" pattern.
    *
    * @param key             key with which the specified value is to be associated
    * @param mappingFunction the function to compute a value
    * @return the current (existing or computed) value associated with the specified key
    * @throws java.lang.IllegalStateException if the computation detectably attempts a recursive update to this
    *                               cache that would otherwise never complete
    * @throws java.lang.RuntimeException      or Error if the mappingFunction does so, in which case the mapping is
    *                               left unestablished
    */
  def get(key: K, mappingFunction: K => V): F[V] =
    lift.lift(underlying.get(key, mappingFunction.asJava))

  /**
    * Returns a map of the values associated with `keys` in this cache. The returned map will
    * only contain entries which are already present in the cache.
    *
    * @param keys the keys whose associated values are to be returned
    * @return the mapping of keys to values for the specified keys found in this cache
    */
  def getAllPresent(keys: Iterable[K]): F[Map[K, V]] =
    lift.lift(underlying.getAllPresent(keys.asJava).asScala.toMap)

  /**
    * Returns the future of a map of the values associated with `keys`, creating or retrieving
    * those values if necessary. The returned map contains entries that were already cached, combined
    * with newly loaded entries.
    *
    * A single request to the `mappingFunction` is performed for all keys which are not already
    * present in the cache.
    *
    * @param keys the keys whose associated values are to be returned
    * @param mappingFunction the function to compute the values
    * @return an unmodifiable mapping of keys to values for the specified keys in this cache
    * @throws java.lang.RuntimeException or Error if the mappingFunction does so, in which
    *                                        case the mapping is left unestablished
    */
  def getAll(
      keys: Iterable[K],
      mappingFunction: Iterable[K] => Map[K, V]
  ): F[Map[K, V]] =
    lift.lift(
      underlying
        .getAll(
          keys.asJava,
          asJavaFunction((ks: java.lang.Iterable[_ <: K]) =>
            mappingFunction(ks.asScala).asJava
          )
        )
        .asScala
        .toMap
    )

  /**
    * Associates `value` with `key` in this cache. If the cache previously contained a
    * value associated with `key`, the old value is replaced by `value`.
    *
    * @param key   key with which the specified value is to be associated
    * @param value value to be associated with the specified key
    */
  def put(key: K, value: V): F[Unit] =
    lift.lift(underlying.put(key, value))

  /**
    * Copies all of the mappings from the specified map to the cache.
    *
    * @param map mappings to be stored in this cache
    */
  def putAll(map: Map[K, V]): F[Unit] =
    lift.lift(underlying.putAll(map.asJava))

  /**
    * Discards any cached value for key `key`.
    *
    * @param key key whose mapping is to be removed from the cache
    */
  def invalidate(key: K): F[Unit] =
    lift.lift(underlying.invalidate(key))

  /**
    * Discards any cached values for keys `keys`.
    *
    * @param keys the keys whose associated values are to be removed
    */
  def invalidateAll(keys: Iterable[K]): F[Unit] =
    lift.lift(underlying.invalidateAll(keys.asJava))

  /**
    * Discards all entries in the cache.
    */
  def invalidateAll(): F[Unit] =
    lift.lift(underlying.invalidateAll())

  /**
    * Returns the approximate number of entries in this cache.
    *
    * @return the estimated number of mappings
    */
  def estimatedSize(): Long =
    underlying.estimatedSize()

  /**
    * Returns a current snapshot of this cache's cumulative statistics. All statistics are
    * initialized to zero, and are monotonically increasing over the lifetime of the cache.
    *
    * @return the current snapshot of the statistics of this cache
    */
  def stats(): F[CacheStats] =
    lift.lift(underlying.stats())

  /**
    * Returns a view of the entries stored in this cache as a thread-safe map. Modifications made to
    * the map directly affect the cache.
    *
    * @return a thread-safe view of this cache
    */
  def asMap(): F[collection.concurrent.Map[K, V]] =
    lift.lift(underlying.asMap().asScala)

  /**
    * Performs any pending maintenance operations needed by the cache. Exactly which activities are
    * performed -- if any -- is implementation-dependent.
    */
  def cleanUp(): F[Unit] =
    lift.lift(underlying.cleanUp())

  /**
    * Returns access to inspect and perform low-level operations on this cache based on its runtime
    * characteristics. These operations are optional and dependent on how the cache was constructed
    * and what abilities the implementation exposes.
    *
    * @return access to inspect and perform advanced operations based on the cache's characteristics
    */
  def policy(): F[Policy[K, V]] =
    lift.lift(underlying.policy())

  override def toString = s"Cache($underlying)"
}
