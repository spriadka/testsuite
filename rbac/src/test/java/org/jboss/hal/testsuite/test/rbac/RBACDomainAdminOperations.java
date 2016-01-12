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
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.admin.DomainAdministration;

/**
 * A convenience for administration operations needed in managed domain RBAC use cases.
 */
public class RBACDomainAdminOperations {

    private DomainAdministration adminOpts;

    RBACDomainAdminOperations(OnlineManagementClient client) {
        adminOpts = new DomainAdministration(client);
    }

    void reloadAllHostsIfRequired() throws IOException, InterruptedException, TimeoutException {
        List<String> hosts = adminOpts.hosts();
        for (String host : hosts) {
            adminOpts.reloadIfRequired(host);
        }
    }
}
