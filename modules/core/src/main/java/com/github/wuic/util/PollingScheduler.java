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


package com.github.wuic.util;

import com.github.wuic.exception.wrapper.StreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.concurrent.Future;

/**
 * <p>
 * This class is able to schedule a polling operation in the {@link WuicScheduledThreadPool}.
 * </p>
 *
 * @author Guillaume DROUET
 * @version 1.0
 * @since 0.4.0
 * @param <T> the type of listener
 */
public abstract class PollingScheduler<T> implements Runnable {

    /**
     * The logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Polling interleave in seconds (-1 to disable).
     */
    private int pollingInterleave;

    /**
     * Help to know when a polling operation is done.
     */
    private Future<?> pollingResult;

    /**
     * All observers per nut.
     */
    private final Map<String, Polling> nutObservers;

    /**
     * Creates a new instance.
     */
    public PollingScheduler() {
        nutObservers = new HashMap<String, Polling>();
    }

    /**
     * <p>
     * Adds a set of listeners to be notified when a polling operation is perform on a nut matching the given pattern.
     * </p>
     *
     * @param pattern the pattern to use to retrieve the different real paths to poll
     * @param listeners some listeners to be notified when an update has been detected on a nut
     * @throws StreamException if an I/O occurs while retrieving last update of the nut
     */
    public final void observe(final String pattern, final T ... listeners) throws StreamException {
        synchronized (getNutObservers()) {
            final Polling polling = getNutObservers().containsKey(pattern)
                    ? getNutObservers().get(pattern) : new Polling(pattern);

            polling.addListeners(listeners);
            nutObservers.put(pattern, polling);
        }
    }

    /**
     * <p>
     * Gets polling data with observers.
     * </p>
     *
     * @return the observers
     */
    public Map<String, ? extends Polling> getNutObservers() {
        return nutObservers;
    }

    /**
     * <p>
     * Returns the polling interleave.
     * </p>
     *
     * @return the polling interleave
     */
    public final int getPollingInterleave() {
        return pollingInterleave;
    }

    /**
     * <p>
     * Defines a new polling interleave. If current polling operation are currently processed, then they are not interrupted
     * and a new scheduling is created if the given value is a positive number. If the value is not positive, then no
     * polling will occur.
     * </p>
     *
     * @param interleaveSeconds interleave in seconds
     */
    public final synchronized void setPollingInterleave(final int interleaveSeconds) {

        // Stop current scheduling
        if (pollingResult != null) {
            log.info("Cancelling repeated polling operation for {}", getClass().getName());
            pollingResult.cancel(false);
            pollingResult = null;
        }

        pollingInterleave = interleaveSeconds;

        // Create new scheduling if necessary
        if (pollingInterleave > 0) {
            log.info("Start polling operation for {} repeated every {} seconds", getClass().getName(), pollingInterleave);
            pollingResult = WuicScheduledThreadPool.getInstance().executeEveryTimeInSeconds(this, pollingInterleave);
        } else {
            log.info("Won't perform any polling operation for {}", getClass().getName());
        }
    }

    /**
     * <p>
     * Retrieves a timestamp that indicates the last time this nut has changed.
     * </p>
     *
     * @param path the real path of the nut
     * @return the timestamp
     * @throws StreamException if any I/O error occurs
     */
    protected abstract Long getLastUpdateTimestampFor(final String path) throws StreamException;

    /**
     * <p>
     * This class represents a polling information. It's composed of a pattern matching the desired nuts and a set of
     * listeners to be notified when a nut has been polled.
     * </p>
     *
     * @author Guillaume DROUET
     * @version 1.0
     * @since 0.4.0
     */
    public class Polling {

        /**
         * Listeners.
         */
        private Set<T> listeners;

        /**
         * The pattern.
         */
        private String pattern;

        /**
         * <p>
         * Creates a new instance.
         * </p>
         *
         * @param p the pattern matching the nuts to test
         */
        public Polling(final String p) {
            listeners = new HashSet<T>();
            pattern = p;
        }

        /**
         * <p>
         * Gets the pattern.
         * </p>
         *
         * @return the pattern
         */
        public String getPattern() {
            return pattern;
        }

        /**
         * <p>
         * Adds all the specified listeners.
         * </p>
         *
         * @param listener the array to add
         */
        public void addListeners(final T ... listener) {
            Collections.addAll(listeners, listener);
        }

        /**
         * <p>
         * Gets the listeners.
         * </p>
         *
         * @return the listeners
         */
        public Set<T> getListeners() {
            return listeners;
        }
    }
}