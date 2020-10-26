package com.github.blemale

import java.util.concurrent.CompletableFuture

import scala.concurrent.{ExecutionContext, Future}
import scala.compat.java8.FutureConverters._

package object scaffeine {
  type Id[A] = A

  type Cache[K, V]             = SyncCacheF[Id, K, V]
  type LoadingCache[K, V]      = LoadingSyncCacheF[Id, K, V]
  type AsyncCache[K, V]        = AsyncCacheF[Future, K, V]
  type AsyncLoadingCache[K, V] = AsyncLoadingCacheF[Future, K, V]

  implicit val idSync: Sync[Id] = new Sync[Id] {
    override def suspend[A](a: A): Id[A] = a
  }

  implicit val futureAsync: Async[Future] = new Async[Future] {
    implicit private[this] val ec: ExecutionContext = DirectExecutionContext

    override type SyncType[A] = Id[A]

    override def sync: Sync[SyncType] = idSync

    override def map[A, B](fa: Future[A])(f: A => B): Future[B] =
      fa.map(f)

    override def fromCompletableFuture[A](
        future: CompletableFuture[A]
    ): Future[A] = future.toScala

    override def toCompletableFuture[A](fa: Future[A]): CompletableFuture[A] =
      fa.toJava.toCompletableFuture
  }
}
