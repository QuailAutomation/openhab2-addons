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
package org.openhab.binding.nikohomecontrol.internal.protocol.nhc1;

/**
 * Class {@link NhcMessageCmd1} used as input to gson to send commands to Niko Home Control. Extends
 * {@link NhcMessageBase1}.
 * <p>
 * Example: <code>{"cmd":"executeactions","id":1,"value1":0}</code>
 *
 * @author Mark Herwege - Initial Contribution
 */
@SuppressWarnings("unused")
class NhcMessageCmd1 extends NhcMessageBase1 {

    private int id;
    private int value1;
    private int value2;
    private int value3;
    private int mode;
    private int overrule;
    private String overruletime;

    NhcMessageCmd1(String cmd) {
        super.setCmd(cmd);
    }

    NhcMessageCmd1(String cmd, int id) {
        this(cmd);
        this.id = id;
    }

    NhcMessageCmd1(String cmd, int id, int value1) {
        this(cmd, id);
        this.value1 = value1;
    }

    NhcMessageCmd1(String cmd, int id, int value1, int value2, int value3) {
        this(cmd, id, value1);
        this.value2 = value2;
        this.value3 = value3;
    }

    NhcMessageCmd1 withMode(int mode) {
        this.mode = mode;
        return this;
    }

    NhcMessageCmd1 withOverrule(int overrule) {
        this.overrule = overrule;
        return this;
    }

    NhcMessageCmd1 withOverruletime(String overruletime) {
        this.overruletime = overruletime;
        return this;
    }
}
