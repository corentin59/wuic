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


package com.github.wuic.engine.impl.embedded;

import com.github.wuic.exception.wrapper.BadClassException;
import com.github.wuic.exception.wrapper.StreamException;
import com.github.wuic.exception.xml.WuicXmlReadException;
import com.github.wuic.configuration.Configuration;
import com.github.wuic.configuration.ImageConfiguration;
import com.github.wuic.util.IOUtils;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>
 * Engine that compress images. Currently do nothing but could be completed in
 * future enhancement.
 * </p>
 * 
 * @author Guillaume DROUET
 * @version 1.3
 * @since 0.2.0
 */
public class CGImageCompressorEngine extends CGAbstractCompressorEngine {

    /**
     * The configuration.
     */
    private ImageConfiguration configuration;
    
    /**
     * <p>
     * Creates a new {@link com.github.wuic.engine.Engine}. An
     * {@link com.github.wuic.exception.wrapper.BadClassException} will be thrown if the configuration
     * is not an {@link ImageConfiguration}.
     * </p>
     * 
     * @param config the {@link Configuration}
     * @throws com.github.wuic.exception.xml.WuicXmlReadException if a bad configuration is detected
     */
    public CGImageCompressorEngine(final Configuration config) throws WuicXmlReadException {
        if (config instanceof ImageConfiguration) {
            configuration = (ImageConfiguration) config;
        } else {

            throw new BadClassException(config, ImageConfiguration.class);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void compress(final InputStream source, final OutputStream target)
            throws StreamException {
        // Do not use char set here !
        IOUtils.copyStream(source, target);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration getConfiguration() {
        return configuration;
    }
}
