/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.hal.testsuite.test.rbac;

import java.io.IOException;

import org.wildfly.extras.creaper.core.online.Constants;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

/**
 * A convenience for not administration operations needed in managed domain RBAC use cases.
 */
public final class RBACDomainOperations {

    private final Operations ops;

    RBACDomainOperations(OnlineManagementClient client) {
        ops = new Operations(client);
    }

    Address getServerAddress(String host, String server) {
        return Address.host(host).and(Constants.SERVER, server);
    }

    boolean isServerRunning(String host, String server) throws IOException {
        Address serverAddress = getServerAddress(host, server);
        ModelNodeResult result = ops.readAttribute(serverAddress, Constants.SERVER_STATE);
        if (!result.hasDefinedValue()) {
            return false;
        }
        String serverState = result.stringValue();
        if (Constants.CONTROLLER_PROCESS_STATE_RUNNING.equals(serverState)
                || Constants.CONTROLLER_PROCESS_STATE_RELOAD_REQUIRED.equals(serverState)
                || Constants.CONTROLLER_PROCESS_STATE_RESTART_REQUIRED.equals(serverState)) {
            return true;
        }
        return false;
    }

}
