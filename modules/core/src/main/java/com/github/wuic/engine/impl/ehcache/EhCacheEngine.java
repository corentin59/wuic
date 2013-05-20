/*
 * "Copyright (c) 2013   Capgemini Technology Services (hereinafter "Capgemini")
 *
 * License/Terms of Use
 * Permission is hereby granted, free of charge and for the term of intellectual
 * property rights on the Software, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to use, copy, modify and
 * propagate free of charge, anywhere in the world, all or part of the Software
 * subject to the following mandatory conditions:
 *
 * -   The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Any failure to comply with the above shall automatically terminate the license
 * and be construed as a breach of these Terms of Use causing significant harm to
 * Capgemini.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, PEACEFUL ENJOYMENT,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Capgemini shall not be used in
 * advertising or otherwise to promote the use or other dealings in this Software
 * without prior written authorization from Capgemini.
 *
 * These Terms of Use are subject to French law.
 *
 * IMPORTANT NOTICE: The WUIC software implements software components governed by
 * open source software licenses (BSD and Apache) of which CAPGEMINI is not the
 * author or the editor. The rights granted on the said software components are
 * governed by the specific terms and conditions specified by Apache 2.0 and BSD
 * licenses."
 */


package com.github.wuic.engine.impl.ehcache;

import com.github.wuic.resource.impl.ByteArrayWuicResource;
import com.github.wuic.resource.WuicResource;
import com.github.wuic.configuration.Configuration;
import com.github.wuic.engine.Engine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.github.wuic.engine.EngineRequest;
import com.github.wuic.util.IOUtils;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This {@link Engine engine} reads from a cache specified in the WUIC XML file
 * the given set of elements. If they exists, then they are returned and no more
 * engine is executed. Otherwise, the chain is executed and the result is put in
 * cache.
 * </p>
 * 
 * @author Guillaume DROUET
 * @version 1.5
 * @since 0.1.1
 */
public class EhCacheEngine extends Engine {

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * The configuration.
     */
    private Configuration configuration;

    /**
     * <p>
     * Builds a new engine.
     * </p>
     * 
     * @param conf the configuration to use
     */
    public EhCacheEngine(final Configuration conf) {
        configuration = conf;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<WuicResource> parse(final EngineRequest request)
            throws IOException {

        if (configuration.cache()) {
            // calculate the hash code key
            // use the hash code of the class and make it positive if negative as multiplier
            final int multiplier = getClass().hashCode() * (getClass().hashCode() < 0 ? -1 : 1);
            int key = multiplier + 1;

            /*
             * Same sets of resources could be found in different groups. Their
             * difference is in the engines that parse it so get different hash
             * by appending the next engine in the builder
             */
            Engine engine = this;

            while ((engine = engine.getNext()) != null) {
                key *= multiplier + getNext().getClass().hashCode();
            }

            for (WuicResource resource : request.getResources()) {
                key *= multiplier + resource.getName().hashCode();
            }

            final Element value = getConfiguration().getCache().get(key);

            // Resources exist in cache, returns them
            if (value != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Resources for group '" + key + "' found in cache");
                }

                return (List<WuicResource>) value.getObjectValue();
            } else if (getNext() != null) {
                final List<WuicResource> resources = getNext().parse(request);
                final List<WuicResource> toCache = new ArrayList<WuicResource>(resources.size());

                InputStream is = null;

                for (WuicResource resource : resources) {
                    try {
                        is = resource.openStream();
                        final byte[] bytes = load(is);
                        toCache.add(new ByteArrayWuicResource(bytes, resource.getName(),
                                resource.getFileType()));
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }
                }

                if (log.isDebugEnabled()) {
                    log.debug("Caching resource with " + key);
                }

                getConfiguration().getCache().put(new Element(key, toCache));

                return toCache;
            } else {
                return null;
            }
        // we don't cache so just call the next engine if exists
        } else if (getNext() != null) {
            return getNext().parse(request);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean works() {
        // WUIC framework ALWAYS uses cache
        return Boolean.TRUE;
    }

    /**
     * <p>
     * Loads the given stream into a byte array.
     * </p>
     * 
     * @param is the input stream
     * @return the byte array
     * @throws IOException if the stream could not be read
     */
    private byte[] load(final InputStream is) throws IOException {
        final byte[] readBuf = new byte[IOUtils.WUIC_BUFFER_LEN];

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int count = is.read(readBuf);

        while (count > 0) {
            baos.write(readBuf, 0, count);
            count = is.read(readBuf);
        }

        is.close();

        return baos.toByteArray();
    }
}
