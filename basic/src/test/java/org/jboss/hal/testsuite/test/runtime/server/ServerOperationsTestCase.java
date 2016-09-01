/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.hal.testsuite.test.runtime.server;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.WindowFragment;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.runtime.DomainRuntimeEntryPoint;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.Console;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import static org.junit.Assert.fail;

import java.io.IOException;

/**
 * @author Harald Pehl
 */
@RunWith(Arquillian.class)
@Category(Domain.class)
public class ServerOperationsTestCase {

    @Drone WebDriver browser;
    private final String
        hostName = ConfigUtils.getDefaultHost(),
        serverGroupName = ConfigUtils.get("suite.domain.server.group", "main-server-group"),
        serverName2beSelected = ConfigUtils.get("suite.domain.server", "server-two");
    private FinderNavigation navigationByHost;
    private FinderNavigation navigationByServerGroup;
    private final Address hostAddress = Address.host(hostName);
    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private final Operations ops = new Operations(client);

    @Before
    public void before() {
        navigationByHost = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                .step(FinderNames.BROWSE_DOMAIN_BY, FinderNames.HOSTS)
                .step(FinderNames.HOST, hostName);

        navigationByServerGroup = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                .step(FinderNames.BROWSE_DOMAIN_BY, FinderNames.SERVER_GROUPS)
                .step(FinderNames.SERVER_GROUP, serverGroupName);
    }

    @AfterClass
    public static void afterClass() {
        IOUtils.closeQuietly(client);
    }

    @Test(expected = TimeoutException.class)  //NoSuchElementException.class - wainting until element is visible, so different exception
    public void wrongSelection() {
        navigationByHost.step(FinderNames.SERVER, "unknown").selectRow();
    }

    @Test
    public void selectServerByHost() {
        // makes sure we can select a server by host
        navigationByHost.step(FinderNames.SERVER, serverName2beSelected).selectRow();
    }

    @Test
    public void selectServerByServerGroup() {
        // makes sure we can select a server by server group
        navigationByServerGroup.step(FinderNames.SERVER, serverName2beSelected).selectRow();
    }

    @Test
    public void addServer() throws Exception {
        final String serverName = "ServerOperationsSrv_" + RandomStringUtils.randomAlphanumeric(8);
        final Address serverAddress = hostAddress.and("server-config", serverName);

        try {
            navigationByServerGroup.step(FinderNames.SERVER).selectColumn().invoke("Add");

            WindowFragment addServerDialog = null;
            try {
                addServerDialog = Console.withBrowser(browser).openedWindow();
            } catch (TimeoutException e) {
                fail("'Create New Server Configuration' dialog didn't open in time. See https://issues.jboss.org/browse/JBEAP-4902");
            }
            addServerDialog.getEditor().text("name", serverName);
            addServerDialog.getEditor().text("portOffset", "999");
            addServerDialog.getEditor().clickButton("Save");
            addServerDialog.waitUntilClosed();

            new ResourceVerifier(serverAddress, client).verifyExists();

        } finally {
            removeIfExists(serverAddress);
        }
    }

    @Test
    public void removeServer() throws Exception {
        final String serverName = "ServerOperationsSrv_" + RandomStringUtils.randomAlphanumeric(8);
        final Address serverAddress = hostAddress.and("server-config", serverName);
        ops.add(serverAddress, Values.of("group", serverGroupName).and("socket-binding-port-offset", 999));

        try {
            navigationByServerGroup.step(FinderNames.SERVER, serverName).selectRow().invoke("Remove");

            ConfirmationWindow window = Console.withBrowser(browser).openedWindow(ConfirmationWindow.class);
            window.confirm();
            Graphene.waitGui().until().element(window.getRoot()).is().not().present();

            new ResourceVerifier(serverAddress, client).verifyDoesNotExist();

        } finally {
            removeIfExists(serverAddress);
        }
    }

    // Cannot use ops.removeIfExists(Address) due to https://github.com/wildfly-extras/creaper/issues/137
    private void removeIfExists(Address address) throws IOException, OperationException {
        if (ops.exists(address)) {
            ops.remove(address);
        }
    }
}
