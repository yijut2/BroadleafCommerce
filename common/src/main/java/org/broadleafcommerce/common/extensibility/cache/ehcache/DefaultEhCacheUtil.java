/*
 * #%L
 * BroadleafCommerce Common Libraries
 * %%
 * Copyright (C) 2009 - 2019 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.common.extensibility.cache.ehcache;

import org.broadleafcommerce.common.extensibility.cache.DefaultJCacheUtil;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.ExpiryPolicy;
import org.ehcache.jsr107.Eh107Configuration;

import java.net.URI;
import java.time.Duration;

import javax.cache.Cache;
import javax.cache.CacheManager;

/**
 * Allows an encapsulated way to create caches programmatically from an EhCache {@link CacheManager}.  The standard APIs do not provide enough control, so we encapsulate it here.
 * @author Kelly Tisdell
 *
 */
public class DefaultEhCacheUtil extends DefaultJCacheUtil {
    
    public static final String EH_CACHE_MERGED_XML_RESOUCE_URI = "ehcache:merged-xml-resource";
    
    public DefaultEhCacheUtil() {
        super(EH_CACHE_MERGED_XML_RESOUCE_URI);
    }
    
    public DefaultEhCacheUtil(String uri) {
        super(uri);
    }
    
    public DefaultEhCacheUtil(CacheManager cacheManager) {
        super(cacheManager);
    }
    
    public DefaultEhCacheUtil(URI uri) {
        super(uri);
    }

    @Override
    public <K,V> Cache<K,V> getCache(String cacheName) {
        return getCacheManager().getCache(cacheName);
    }

    @Override
    public synchronized Cache<Object, Object> createCache(String cacheName, int ttlSeconds, int maxElementsInMemory) {
        return createCache(cacheName, ttlSeconds, maxElementsInMemory, Object.class, Object.class);
    }

    @Override
    public synchronized <K, V> Cache<K, V> createCache(String cacheName, int ttlSeconds, int maxElementsInMemory, Class<K> key, Class<V> value) {
        ExpiryPolicy<Object, Object> policy;
        if (ttlSeconds < 1) {
            //Make it eternal
            policy = ExpiryPolicyBuilder.noExpiration();
        } else {
            policy = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds((long)ttlSeconds));
        }
        
        CacheConfiguration<K, V> config = CacheConfigurationBuilder.
                newCacheConfigurationBuilder(key, value, ResourcePoolsBuilder.heap((long)maxElementsInMemory))
                .withExpiry(policy)
                .build();
        
        Cache<K,V> cache = getCacheManager().createCache(cacheName, Eh107Configuration.fromEhcacheCacheConfiguration(config));
        enableManagement(cache);
        enableStatistics(cache);
        return cache;
    }
    
}
