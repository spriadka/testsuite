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

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.CliUtils;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.WindowFragment;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.runtime.DomainRuntimeEntryPoint;
import org.jboss.hal.testsuite.util.Console;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Harald Pehl
 */
@RunWith(Arquillian.class)
@Category(Domain.class)
public class ServerOperationsTestCase {

    private static CliClient cli = CliClientFactory.getDomainClient("full");

    @Drone WebDriver browser;
    private FinderNavigation navigationByHost;
    private FinderNavigation navigationByServerGroup;

    @Before
    public void before() {
        navigationByHost = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                .addAddress(FinderNames.BROWSE_DOMAIN_BY, FinderNames.HOSTS)
                .addAddress(FinderNames.HOST, "master");

        navigationByServerGroup = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                .addAddress(FinderNames.BROWSE_DOMAIN_BY, FinderNames.SERVER_GROUPS)
                .addAddress(FinderNames.SERVER_GROUP, "main-server-group");
    }

    @Test(expected = TimeoutException.class)  //NoSuchElementException.class - wainting until element is visible, so different exception
    public void wrongSelection() {
        new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                .addAddress(FinderNames.BROWSE_DOMAIN_BY, FinderNames.HOSTS)
                .addAddress(FinderNames.HOST, "master")
                .addAddress(FinderNames.SERVER, "unknown")
                .selectColumn();
    }

    @Test
    public void selectServerByHost() {
        // makes sure we can select a server by host
        navigationByHost.addAddress(FinderNames.SERVER).selectColumn();
    }

    @Test
    public void selectServerByServerGroup() {
        // makes sure we can select a server by server group
        navigationByServerGroup.addAddress(FinderNames.SERVER).selectColumn();
    }

    @Test
    public void addServer() {
        String serverName = "srv_" + RandomStringUtils.randomAlphanumeric(8);

        try {
            navigationByServerGroup.addAddress(FinderNames.SERVER).selectColumn().invoke("Add");

            WindowFragment addServerDialog = Console.withBrowser(browser).openedWindow();
            addServerDialog.getEditor().text("name", serverName);
            addServerDialog.getEditor().text("portOffset", "999");
            addServerDialog.getEditor().clickButton("Save");
            addServerDialog.waitUntilClosed();

            assertTrue(cli
                    .executeForSuccess(
                            CliUtils.buildCommand("/host=master/server-config=" + serverName, ":read-resource")));

        } finally {
            removeIfPresent(serverName);
        }
    }

    @Test
    public void removeServer() {
        String serverName = "srv_" + RandomStringUtils.randomAlphanumeric(8);
        cli.executeCommand(CliUtils.buildCommand("/host=master/server-config=" + serverName, ":add",
                new String[]{"group=main-server-group", "socket-binding-port-offset=999"}));

        try {
            navigationByServerGroup.addAddress(FinderNames.SERVER, serverName).selectRow().invoke("Remove");

            ConfirmationWindow window = Console.withBrowser(browser).openedWindow(ConfirmationWindow.class);
            window.confirm();
            Graphene.waitGui().until().element(window.getRoot()).is().not().present();

            assertFalse(cli
                    .executeForSuccess(
                            CliUtils.buildCommand("/host=master/server-config=" + serverName, ":read-resource")));

        } finally {
            removeIfPresent(serverName);
        }
    }

    private void removeIfPresent(final String serverName) {
        if (cli.executeForSuccess(
                CliUtils.buildCommand("/host=master/server-config=" + serverName, ":read-resource"))) {
            cli.executeCommand(CliUtils.buildCommand("/host=master/server-config=" + serverName, ":remove"));
        }
    }
}
