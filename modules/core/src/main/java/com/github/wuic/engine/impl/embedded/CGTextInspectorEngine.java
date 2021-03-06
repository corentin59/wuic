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

import com.github.wuic.NutType;
import com.github.wuic.engine.Engine;
import com.github.wuic.engine.EngineRequest;
import com.github.wuic.engine.LineInspector;
import com.github.wuic.exception.WuicException;
import com.github.wuic.exception.wrapper.StreamException;
import com.github.wuic.nut.Nut;
import com.github.wuic.nut.core.ByteArrayNut;
import com.github.wuic.util.IOUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * <p>
 * Basic inspector engine for text nuts processing text line per line. This kind of engine inspects
 * each nut of a request to eventually alter their content or to extract other referenced nuts
 * thanks to a set of {@link LineInspector inspectors}.
 * </p>
 *
 * @author Guillaume DROUET
 * @version 1.2
 * @since 0.3.3
 */
public abstract class CGTextInspectorEngine extends Engine {

    /**
     * The inspectors of each line
     */
    private LineInspector[] lineInspectors;

    /**
     * Inspects or not.
     */
    private Boolean doInspection;

    /**
     * The charset of inspected file.
     */
    private String charset;

    /**
     * <p>
     * Builds a new instance.
     * </p>
     *
     * @param inspect activate inspection or not
     * @param cs files charset
     * @param inspectors the line inspectors to use
     */
    public CGTextInspectorEngine(final Boolean inspect, final String cs, final LineInspector... inspectors) {
        lineInspectors = inspectors;
        doInspection = inspect;
        charset = cs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Nut> internalParse(final EngineRequest request) throws WuicException {
        // Will contains both heap's nuts eventually modified or extracted nuts.
        final List<Nut> retval = new ArrayList<Nut>();

        if (works()) {
            for (Nut nut : request.getNuts()) {
                retval.add(inspect(nut, request));
            }
        }

        if (getNext() != null) {
            return getNext().parse(new EngineRequest(retval, request));
        } else {
            return retval;
        }
    }

    /**
     * <p>
     * Extracts from the given nut all the nuts referenced by the @import statement in CSS.
     * </p>
     *
     * <p>
     * This method is recursive.
     * </p>
     *
     * @param nut the nut
     * @param request the initial request
     * @return the nut corresponding the inspected nut specified in parameter
     * @throws WuicException if an I/O error occurs while reading
     */
    protected Nut inspect(final Nut nut, final EngineRequest request)
            throws WuicException {
        // Extracts the location where nut is listed in order to compute the location of the extracted imported nuts
        final int lastIndexOfSlash = nut.getName().lastIndexOf("/") + 1;
        final String name = nut.getName();
        final String nutLocation = lastIndexOfSlash == 0 ? "" : name.substring(0, lastIndexOfSlash);

        BufferedReader br = null;
        String line;

        try {
            // Read the path line per line
            br = new BufferedReader(new InputStreamReader(nut.openStream(), charset));

            // Reads each line and keep the transformations in memory
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            final List<Nut> referencednuts = new ArrayList<Nut>();

            while ((line = br.readLine()) != null) {
                for (LineInspector inspector : lineInspectors) {
                    line = inspectLine(line, nutLocation, request, inspector, referencednuts);
                }

                os.write((line + "\n").getBytes());
            }

            // Create and add the inspected nut with its transformations
            final Nut inspected = new ByteArrayNut(os.toByteArray(), nut.getName(), nut.getNutType());
            inspected.setCacheable(nut.isCacheable());
            inspected.setAggregatable(nut.isAggregatable());
            inspected.setTextCompressible(nut.isTextCompressible());
            inspected.setBinaryCompressible(nut.isBinaryCompressible());

            // Also add all the referenced nuts
            for (Nut ref : referencednuts) {
                inspected.addReferencedNut(ref);
            }

            return inspected;
        } catch (IOException ioe) {
            throw new StreamException(ioe);
        } finally {
            IOUtils.close(br);
        }
    }

    /**
     * <p>
     * Inspects the given line and eventually adds some extracted nuts to the nut referencing it.
     * </p>
     *
     * <p>
     * This method is recursive.
     * </p>
     *
     * @param line the line to be inspected
     * @param nutLocation the location of the nut
     * @param request the initial request
     * @param inspector the inspector to use
     * @param referencednuts the collection where any referenced nut identified by the method will be added
     * @throws WuicException if an I/O error occurs while reading
     * @return the given line eventually transformed
     */
    protected String inspectLine(final String line,
                                 final String nutLocation,
                                 final EngineRequest request,
                                 final LineInspector inspector,
                                 final List<Nut> referencednuts)
            throws WuicException {
        // Use a builder to transform the line
        final StringBuffer retval = new StringBuffer();

        // Looking for import statements
        final Matcher matcher = inspector.getPattern().matcher(line);

        while (matcher.find()) {
            // Compute replacement, extract nut name and referenced nuts
            final StringBuilder replacement = new StringBuilder();
            final String nutName = inspector.appendTransformation(matcher, replacement,
                    IOUtils.mergePath(request.getContextPath(), request.getHeap().getId()),
                    nutLocation, request.getHeap().getNutDao());
            matcher.appendReplacement(retval, replacement.toString());

            // If nut name is null, it means that nothing has been changed by the inspector
            if (nutName != null) {
                // TODO : fix => nut's location could be empty because it is not specified in a recursive call
                final List<Nut> res = new ArrayList<Nut>(request.getHeap().getNutDao().create(nutName).keySet());

                // Process nut
                //if (getNext() != null) {
                     // TODO : inspection fails when file is compressed => need to improve regex usage to uncomment this
                //    res = getNext().parse(new EngineRequest(res, request));
                //}

                // Add the nut and inspect it recursively if it's a CSS path
                for (final Nut r : res) {
                    Nut inspected = r;

                    if (r.getNutType().equals(NutType.CSS)) {
                        inspected = inspect(r, request);
                    }

                    configureExtracted(inspected);
                    referencednuts.add(inspected);
                }
            }
        }

        matcher.appendTail(retval);

        return retval.toString();
    }

    /**
     * <p>
     * Configures the given extracted nuts to know if it should be aggregated, compressed, cached, etc.
     * </p>
     *
     * @param nut the nut to configure
     */
    private void configureExtracted(final Nut nut) {
        // TODO : is it really required ? Why ???
        nut.setAggregatable(Boolean.FALSE);
        nut.setTextCompressible(nut.getNutType().isText());
        nut.setBinaryCompressible(!nut.getNutType().isText());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean works() {
        return doInspection;
    }
}
