/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
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

package org.jboss.hal.testsuite.test.configuration.picketlink;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.jboss.as.controller.client.helpers.ClientConstants;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

/**
 * Created by pjelinek on Nov 16, 2015
 */
public class PicketlinkOperations {

    private Operations ops;
    private Administration adminOps;
    private static final String PICKETLINK_EXTENSION = "org.wildfly.extension.picketlink";
    private static final Address PICKETLINK_EXTENSION_ADDRESS =
            Address.of(ClientConstants.EXTENSION, PICKETLINK_EXTENSION);

    public PicketlinkOperations(OnlineManagementClient client) {
        this.ops = new Operations(client);
        this.adminOps = new Administration(client);
    }

    public void enableExtension(boolean reloadImmediatellyIfRequired)
            throws IOException, InterruptedException, TimeoutException {
        ops.add(PICKETLINK_EXTENSION_ADDRESS, Values.of("module", PICKETLINK_EXTENSION));
        if (reloadImmediatellyIfRequired) {
            adminOps.reloadIfRequired();
        }
    }

    public void disableExtension(boolean reloadImmediatellyIfRequired) throws IOException, InterruptedException, TimeoutException {
        ops.remove(PICKETLINK_EXTENSION_ADDRESS);
        if (reloadImmediatellyIfRequired) {
            adminOps.reloadIfRequired();
        }
    }
}
