package com.github.blemale.scaffeine

import java.util.concurrent.CompletableFuture

trait Sync[F[_]] {
  def suspend[A](a: A): F[A]
}

trait Async[F[_]] {
  type SyncType[_]

  def sync: Sync[SyncType]
  def map[A, B](fa: F[A])(f: A => B): F[B]
  def fromCompletableFuture[A](future: CompletableFuture[A]): F[A]
  def toCompletableFuture[A](fa: F[A]): CompletableFuture[A]
}
