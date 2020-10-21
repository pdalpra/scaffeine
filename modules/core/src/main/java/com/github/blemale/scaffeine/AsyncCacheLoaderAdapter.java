package com.github.blemale.scaffeine;

import com.github.benmanes.caffeine.cache.AsyncCacheLoader;
import scala.Function1;
import scala.Function2;
import scala.Option;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


class AsyncCacheLoaderAdapter<K, V> implements AsyncCacheLoader<K, V> {

  private final Function1<K, CompletableFuture<V>> loader;
  private final Option<Function2<K, V, CompletableFuture<V>>> reloadLoader;

  AsyncCacheLoaderAdapter(Function1<K, CompletableFuture<V>> loader, Option<Function2<K, V, CompletableFuture<V>>> reloadLoader) {
    this.loader = loader;
    this.reloadLoader = reloadLoader;
  }

  @Nonnull
  @Override
  public CompletableFuture<V> asyncLoad(@Nonnull K key, @Nonnull Executor executor) {
    return loader.apply(key);
  }

  @Nonnull
  @Override
  public CompletableFuture<V> asyncReload(@Nonnull K key, @Nonnull V oldValue, @Nonnull Executor executor) {
    if (reloadLoader.isEmpty()) {
      return AsyncCacheLoader.super.asyncReload(key, oldValue, executor);
    } else {
      return reloadLoader.get().apply(key, oldValue);
    }
  }
}
