package com.github.wuic.engine.impl.hazelcast;

import com.github.wuic.engine.EngineRequest;
import com.github.wuic.engine.impl.embedded.AbstractCacheEngine;
import com.hazelcast.core.HazelcastInstance;

/**
 * <p>
 * This {@link com.github.wuic.engine.Engine engine} reads from a cache provided by Hazelcast the nuts associated to a
 * workflow to be processed.
 * </p>
 *
 * @author Corentin AZELART
 * @version 1.0
 * @since 0.4.0
 */
public class HazelcastEngine extends AbstractCacheEngine {

    /**
     * The Hazelcast instance.
     */
    private HazelcastInstance hazelcastInstance;

    /**
     * <p>
     * Builds a new engine.
     * </p>
     *
     * @param work if cache should be activated or not
     * @param cache the cache to be wrapped
     * @param bestEffort enable best effort mode or not
     */
    public HazelcastEngine(final Boolean work, final HazelcastInstance cache, final Boolean bestEffort) {
        super(work, bestEffort);
        hazelcastInstance = cache;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putToCache(final EngineRequest.Key request, final CacheResult nuts) {
        hazelcastInstance.getMap("wuicCache").put(request, nuts);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeFromCache(final EngineRequest.Key request) {
        hazelcastInstance.getMap("wuicCache").evict(request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CacheResult getFromCache(final EngineRequest.Key request) {
        return (CacheResult) hazelcastInstance.getMap("wuicCache").get(request);
    }
}
