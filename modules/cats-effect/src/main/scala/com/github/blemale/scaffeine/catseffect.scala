package com.github.blemale.scaffeine

import java.util.concurrent.{CancellationException, CompletableFuture, CompletionException}

import scala.compat.java8.FunctionConverters._

object catseffect {

  implicit class CatsEffectScaffeine[K, V](val scaffeine: Scaffeine[K, V]) {

    /**
      * Builds a cache which does not automatically load values when keys are requested.
      *
      * @tparam K1 the key type of the cache
      * @tparam V1 the value type of the cache
      * @return a cache having the requested features
      */
    def buildSyncF[F[_]: cats.effect.Sync, K1 <: K, V1 <: V]()
        : SyncCacheF[F, K1, V1] =
      SyncCacheF[F, K1, V1](scaffeine.underlying.build())

    /**
      * Builds a cache, which either returns an already-loaded value for a given key or atomically
      * computes or retrieves it using the supplied `loader`. If another thread is currently
      * loading the value for this key, simply waits for that thread to finish and returns its loaded
      * value. Note that multiple threads can concurrently load values for distinct keys.
      *
      * @param loader the loader used to obtain new values
      * @param allLoader the loader used to obtain new values in bulk, called by [[LoadingSyncCacheF.getAll(keys:Iterable[K])*]]
      * @param reloadLoader the loader used to obtain already-cached values
      * @tparam K1 the key type of the loader
      * @tparam V1 the value type of the loader
      * @return a cache having the requested features
      */
    def buildSyncF[F[_]: cats.effect.Sync, K1 <: K, V1 <: V](
        loader: K1 => V1,
        allLoader: Option[Iterable[K1] => Map[K1, V1]] = None,
        reloadLoader: Option[(K1, V1) => V1] = None
    ): LoadingSyncCacheF[F, K1, V1] =
      LoadingSyncCacheF(
        scaffeine.underlying.build(
          scaffeine.toCacheLoader(
            loader,
            allLoader,
            reloadLoader
          )
        )
      )

    /**
      * Builds a cache, which either returns a [[scala.concurrent.Future]] already loaded or currently
      * computing the value for a given key, or atomically computes the value asynchronously through a
      * supplied mapping function or the supplied `loader`. If the asynchronous computation
      * fails then the entry will be automatically removed. Note that multiple threads can
      * concurrently load values for distinct keys.
      *
      * @param loader the loader used to obtain new values
      * @param allLoader the loader used to obtain new values in bulk, called by [[AsyncLoadingCacheF.getAll(keys:Iterable[K])*]]
      * @param reloadLoader the loader used to obtain already-cached values
      * @tparam K1 the key type of the loader
      * @tparam V1 the value type of the loader
      * @return a cache having the requested features
      * @throws java.lang.IllegalStateException if the value strength is weak or soft
      */
    def buildAsyncF[F[_]: cats.effect.Async, K1 <: K, V1 <: V](
        loader: K1 => V1,
        allLoader: Option[Iterable[K1] => Map[K1, V1]] = None,
        reloadLoader: Option[(K1, V1) => V1] = None
    ): AsyncLoadingCacheF[F, K1, V1] =
      AsyncLoadingCacheF(
        scaffeine.underlying.buildAsync[K1, V1](
          scaffeine.toCacheLoader(
            loader,
            allLoader,
            reloadLoader
          )
        )
      )

    /**
      * Builds a cache, which either returns a [[scala.concurrent.Future]] already loaded or currently
      * computing the value for a given key, or atomically computes the value asynchronously through a
      * supplied mapping function or the supplied async `loader`. If the asynchronous
      * computation fails then the entry will be automatically removed.
      * Note that multiple threads can concurrently load values for distinct keys.
      *
      * @param loader the loader used to obtain new values
      * @param allLoader the loader used to obtain new values in bulk, called by [[AsyncLoadingCacheF.getAll(keys:Iterable[K])*]]
      * @param reloadLoader the loader used to obtain already-cached values
      * @tparam K1 the key type of the loader
      * @tparam V1 the value type of the loader
      * @throws java.lang.IllegalStateException if the value strength is weak or soft
      */
    def buildAsyncFF[F[_]: cats.effect.Async, K1 <: K, V1 <: V](
        loader: K1 => F[V1],
        allLoader: Option[Iterable[K1] => F[Map[K1, V1]]] = None,
        reloadLoader: Option[(K1, V1) => F[V1]] = None
    ): AsyncLoadingCacheF[F, K1, V1] =
      AsyncLoadingCacheF(
        scaffeine.underlying.buildAsync[K1, V1](
          scaffeine.toAsyncCacheLoader(
            loader,
            allLoader,
            reloadLoader
          )
        )
      )
  }

  implicit def catsEffectSync[F[_]](implicit
      underlying: cats.effect.Sync[F]
  ): Sync[F] =
    new Sync[F] {

      override def lift[A](a: A): F[A] =
        underlying.delay(a)
    }

  implicit def catsEffectAsync[F[_]](implicit
      underlying: cats.effect.Async[F]
  ): Async[F] =
    new Async[F] {
      override type SyncType[A] = F[A]

      override def sync: Sync[SyncType] = catsEffectSync

      override def map[A, B](fa: F[A])(f: A => B) =
        underlying.map(fa)(f)

      // Heavily inspired by monix-catnap
      override def fromCompletableFuture[A](future: CompletableFuture[A]) =
        underlying.async { callback =>
          val _ = future.handle[Unit](
            asJavaBiFunction[A, Throwable, Unit](
              (result: A, throwable: Throwable) =>
                throwable match {
                  case null =>
                    callback(Right(result))
                  case _: CancellationException =>
                    ()
                  case ex: CompletionException if ex.getCause ne null =>
                    callback(Left(ex.getCause))
                  case ex =>
                    callback(Left(ex))
                }
            )
          )
        }

      override def toCompletableFuture[A](fa: F[A]): CompletableFuture[A] = {
        val completableFuture = new CompletableFuture[A]()
        val _ = underlying.map(underlying.attempt(fa)) {
          case Left(ex)     => completableFuture.completeExceptionally(ex)
          case Right(value) => completableFuture.complete(value)
        }
        completableFuture
      }
    }
}
