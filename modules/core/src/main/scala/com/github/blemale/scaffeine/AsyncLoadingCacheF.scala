package com.github.blemale.scaffeine

import com.github.benmanes.caffeine.cache.{AsyncLoadingCache => CaffeineAsyncLoadingCache}

import scala.collection.JavaConverters._

object AsyncLoadingCacheF {

  def apply[F[_], K, V](
      asyncLoadingCache: CaffeineAsyncLoadingCache[K, V]
  )(implicit async: Async[F]): AsyncLoadingCacheF[F, K, V] =
    new AsyncLoadingCacheF(asyncLoadingCache)
}

class AsyncLoadingCacheF[F[_], K, V](
    override val underlying: CaffeineAsyncLoadingCache[K, V]
)(implicit override val async: Async[F])
    extends AsyncCacheF[F, K, V](underlying) {

  /**
    * Returns the future associated with `key` in this cache, obtaining that value from
    * `loader` if necessary. If the asynchronous computation fails, the entry
    * will be automatically removed.
    *
    * @param key key with which the specified value is to be associated
    * @return the current (existing or computed) future value associated with the specified key
    * @throws java.lang.RuntimeException     or Error if the `loader` does when constructing the future,
    *                                                      in which case the mapping is left unestablished
    */
  def get(key: K): F[V] =
    async.fromCompletableFuture(underlying.get(key))

  /**
    * Returns the future of a map of the values associated with `keys`, creating or retrieving
    * those values if necessary. The returned map contains entries that were already cached, combined
    * with newly loaded entries. If the any of the asynchronous computations fail, those entries will
    * be automatically removed.
    *
    * @param keys the keys whose associated values are to be returned
    * @return the future containing an mapping of keys to values for the specified keys in this cache
    * @throws java.lang.RuntimeException     or Error if the `loader` does so
    */
  def getAll(keys: Iterable[K]): F[Map[K, V]] =
    async.map(async.fromCompletableFuture(underlying.getAll(keys.asJava)))(
      _.asScala.toMap
    )

  /**
    * Returns a view of the entries stored in this cache as a synchronous [[LoadingSyncCacheF]]. A
    * mapping is not present if the value is currently being loaded. Modifications made to the
    * synchronous cache directly affect the asynchronous cache. If a modification is made to a
    * mapping that is currently loading, the operation blocks until the computation completes.
    *
    * @return a thread-safe synchronous view of this cache
    */
  override def synchronous(): LoadingSyncCacheF[async.SyncType, K, V] =
    LoadingSyncCacheF[async.SyncType, K, V](underlying.synchronous())(
      async.sync
    )

  override def toString = s"AsyncLoadingCache($underlying)"
}
