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

/**
 * <p>
 * This package defines a little API used by WUIC to resolve a hierarchical path with each element corresponding to
 * either a path, a directory, a ZIP archive or an entry of a ZIP archive.
 * </p>
 *
 * <p>
 * For instance, the aim is to access a path in /foo/lib.jar/statics/fwk.zip/path.js where :
 * <ul>
 *     <li>/foo is a directory</li>
 *     <li>lib.jar is a JAR path</li>
 *     <li>statics is a directory entry inside lib.jar</li>
 *     <li>statics/fwk.zip is a path entry inside lib.jar which is itself a ZIP archive</li>
 *     <li>path.js is a path stored as a path entry inside fwk.zip</li>
 * </ul>
 * </p>
 *
 * <p>
 * See this {@link com.github.wuic.util.IOUtils.buildPath(String) helper} to build a path.
 * </p>
 *
 * <p>
 * Classes in this package don't throw WUIC exceptions like {@link com.github.wuic.exception.wrapper.StreamException}
 * or {@link com.github.wuic.exception.wrapper.BadArgumentException} to ease the reuse of this code outside WUIC.
 * </p>
 *
 * @author Guillaume DROUET
 */
package com.github.wuic.util.path;