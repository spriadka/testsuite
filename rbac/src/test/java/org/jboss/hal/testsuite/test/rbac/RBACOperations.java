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

import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

/**
 * A convenience for common model related operations needed in RBAC use cases.
 */
public class RBACOperations  {

    private static final String
            INCLUDE = "include",
            ROLE_MAPPING = "role-mapping",
            REALM = "realm",
            TYPE = "type",
            NAME = "name",
            GROUP = "GROUP",
            USER = "USER";

    private static final Address AUTHORIZATION_ADDRESS =
            Address.coreService("management").and("access", "authorization");

    private final Operations ops;

    public RBACOperations(OnlineManagementClient client) {
        this.ops = new Operations(client);
    }

    public Address getUserIncludedInRole(String user, String realm, String role) {
        return getPrincipalIncludedInRole("user-" + user + "@" + realm, role);
    }

    public Address getGroupIncludedInRole(String group, String realm, String role) {
        return getPrincipalIncludedInRole("group-" + group + "@" + realm, role);
    }

    public Address getPrincipalIncludedInRole(String principal, String role) {
        return AUTHORIZATION_ADDRESS.and(ROLE_MAPPING, role).and(INCLUDE, principal);
    }

    public Address addUserIncludedInRole(String user, String realm, String role) throws IOException {
        return addPrincipalIncludedInRole(user, realm, role, USER);
    }

    public Address addGroupIncludedInRole(String group, String realm, String role) throws IOException {
        return addPrincipalIncludedInRole(group, realm, role, GROUP);
    }

    public void removePrincipalFromRole(String principal, String role) throws IOException {
        ops.remove(getPrincipalIncludedInRole(principal, role));
    }

    private Address addPrincipalIncludedInRole(String principal, String realm, String role, String type)
            throws IOException {
        Address principalAddress = getPrincipalIncludedInRole(principal, role);
        ops.add(principalAddress, Values.of(NAME, principal).and(TYPE, type).and(REALM, realm)).assertSuccess();
        return principalAddress;
    }
}
