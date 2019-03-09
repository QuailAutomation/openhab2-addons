/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.innogysmarthome.internal.client.exception;

/**
 * Thrown, if the session is not initialized or disconnected.
 *
 * @author Oliver Kuhl - Initial contribution
 *
 */
@SuppressWarnings("serial")
public class SessionNotFoundException extends ApiException {

    public SessionNotFoundException() {
        super();
    }

    public SessionNotFoundException(String message) {
        super(message);
    }

}
