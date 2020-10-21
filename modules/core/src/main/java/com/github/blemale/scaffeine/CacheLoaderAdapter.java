package com.github.blemale.scaffeine;

import com.github.benmanes.caffeine.cache.CacheLoader;
import scala.Function1;
import scala.Function2;
import scala.Option;

import javax.annotation.Nonnull;

class CacheLoaderAdapter<K, V> implements CacheLoader<K, V>{

  private final Function1<K, V> loader;
  private final Option<Function2<K, V, V>> reloadLoader;

  CacheLoaderAdapter(Function1<K, V> loader, Option<Function2<K, V, V>> reloadLoader) {
    this.loader = loader;
    this.reloadLoader = reloadLoader;
  }

  @Override
  public V load(@Nonnull K key) {
    return loader.apply(key);
  }

  @Override
  public V reload(@Nonnull K key, @Nonnull V oldValue) throws Exception {
    if (reloadLoader.isEmpty()) {
      return CacheLoader.super.reload(key, oldValue);
    } else {
      return reloadLoader.get().apply(key, oldValue);
    }
  }
}
