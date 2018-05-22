/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.eagle200.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Eagle200Configuration} class contains fields mapping thing configuration parameters.
 *
 * @author Thomas Hentschel - Initial contribution
 */
public class Eagle200Configuration {

    private final Logger logger = LoggerFactory.getLogger(Eagle200Configuration.class);

    /**
     * hostname.
     */
    public String hostname;
    /**
     * cloudid.
     */
    public String cloudid;
    /**
     * install code.
     */
    public String installcode;

    public boolean isComplete() {

        if (hostname == null || hostname.isEmpty()) {
            logger.info("missing or empty hostname");
            return false;
        }
        if (cloudid == null || cloudid.isEmpty()) {
            logger.info("missing or empty cloudid");
            return false;
        }
        if (installcode == null || installcode.isEmpty()) {
            logger.info("missing or empty installcode");
            return false;
        }
        return true;
    }
}
