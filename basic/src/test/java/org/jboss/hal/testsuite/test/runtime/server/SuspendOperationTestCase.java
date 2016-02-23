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

package org.jboss.hal.testsuite.test.runtime.server;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HOST;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RESUME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RESUME_SERVERS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SERVER;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SERVER_CONFIG;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SERVER_GROUP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUSPEND;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUSPEND_SERVERS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUSPEND_STATE;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.Operation;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.ResourceVerifier;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.WindowFragment;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.runtime.DomainRuntimeEntryPoint;
import org.jboss.hal.testsuite.page.runtime.StandaloneRuntimeEntryPoint;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.Console;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

/**
 * Created by pjelinek on Sep 12, 2015
 */
@RunWith(Arquillian.class)
public class SuspendOperationTestCase {

    private static final String
        DEFAULT_HOST = ConfigUtils.getDefaultHost(),
        MAIN_SERVER_GROUP = "main-server-group",
        SERVER_ONE = "server-one",
        SERVER_TWO = "server-two",
        RUNNING = "RUNNING",
        SUSPENDED = "SUSPENDED";

    @Drone
    private WebDriver browser;

    private WebUI gui = new WebUI();
    private static ModelOperations cli = new ModelOperations();

    @AfterClass
    public static void afterClass() {
        cli.closeClient();
    }

    @Test
    @Category(Standalone.class)
    public void testSuspendStandaloneServer() throws Exception {
        try {
            cli.assertSuspendState(RUNNING);
            FinderNavigation serverNavi = gui.getServerNavigation();
            gui.suspendServer(serverNavi, 0);
            cli.assertSuspendState(SUSPENDED);
        } finally {
            cli.resumeServer();
        }
    }

    @Test
    @Category(Standalone.class)
    public void testResumeStandaloneServer() throws Exception {
        try {
            cli.suspendServer();
            cli.assertSuspendState(SUSPENDED);
            FinderNavigation serverNavi = gui.getServerNavigation();
            gui.resumeServer(serverNavi);
            cli.assertSuspendState(RUNNING);
        } finally {
            cli.resumeServer();
        }
    }

    @Test
    @Category(Domain.class)
    public void testSuspendServerOfHost() throws Exception {
        try {
            cli.assertSuspendState(SERVER_ONE, RUNNING);
            FinderNavigation serverNavi = gui.getServerByHostNavigation(DEFAULT_HOST, SERVER_ONE);
            gui.suspendServer(serverNavi, 0);
            cli.assertSuspendState(SERVER_ONE, SUSPENDED);
        } finally {
            cli.resumeServer(SERVER_ONE);
        }
    }

    @Test
    @Category(Domain.class)
    public void testSuspendGroup() throws Exception {
        try {
            cli.assertSuspendState(SERVER_ONE, RUNNING);
            cli.assertSuspendState(SERVER_TWO, RUNNING);
            FinderNavigation groupNavi = gui.getGroupNavigation(MAIN_SERVER_GROUP);
            gui.suspendGroup(groupNavi, 0);
            cli.assertSuspendState(SERVER_ONE, SUSPENDED);
            cli.assertSuspendState(SERVER_TWO, SUSPENDED);
        } finally {
            cli.resumeGroup(MAIN_SERVER_GROUP);
        }
    }

    @Test
    @Category(Domain.class)
    public void testResumeServerOfHost() throws Exception {
        try {
            cli.suspendServer(SERVER_ONE);
            cli.assertSuspendState(SERVER_ONE, SUSPENDED);
            FinderNavigation serverNavi = gui.getServerByHostNavigation(DEFAULT_HOST, SERVER_ONE);
            gui.resumeServer(serverNavi);
        } finally {
            cli.resumeServer(SERVER_ONE);
        }
    }

    @Test
    @Category(Domain.class)
    public void testResumeGroup() throws Exception {
        try {
            cli.suspendGroup(MAIN_SERVER_GROUP);
            cli.assertSuspendState(SERVER_ONE, SUSPENDED);
            cli.assertSuspendState(SERVER_TWO, SUSPENDED);
            FinderNavigation groupNavi = gui.getGroupNavigation(MAIN_SERVER_GROUP);
            gui.resumeGroup(groupNavi);
            cli.assertSuspendState(SERVER_ONE, RUNNING);
            cli.assertSuspendState(SERVER_TWO, RUNNING);
        } finally {
            cli.resumeGroup(MAIN_SERVER_GROUP);
        }
    }

    private class WebUI {

        private static final String
            SUSPEND_SERVER = "Suspend Server",
            SUSPEND_GROUP = "Suspend Group";

        FinderNavigation getServerNavigation() {
            return new FinderNavigation(browser, StandaloneRuntimeEntryPoint.class)
                            .step(FinderNames.SERVER, "Standalone Server");
        }

        FinderNavigation getGroupNavigation(String group) {
            return new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                            .step(FinderNames.BROWSE_DOMAIN_BY, FinderNames.SERVER_GROUPS)
                            .step(FinderNames.SERVER_GROUP, group);
        }

        FinderNavigation getServerByHostNavigation(String host, String server) {
            return new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                            .step(FinderNames.BROWSE_DOMAIN_BY, FinderNames.HOSTS)
                            .step(FinderNames.HOST, host)
                            .step(FinderNames.SERVER, server);
        }

        void suspendServer(FinderNavigation navi, int timeout) {
            suspend(navi, timeout, SUSPEND_SERVER);
        }

        void suspendGroup(FinderNavigation navi, int timeout) {
            suspend(navi, timeout, SUSPEND_GROUP);
        }

        void resumeServer(FinderNavigation navi) {
            navi.selectRow().invoke("Resume");
            Console.withBrowser(browser).waitUntilLoaded();
        }

        void resumeGroup(FinderNavigation navi) {
            navi.selectRow().invoke("Resume");
            Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
            Console.withBrowser(browser).waitUntilLoaded();
        }

        private void suspend(FinderNavigation navi, int timeout, String action) {
            navi.selectRow().invoke("Suspend");
            WindowFragment windowFragment = Console.withBrowser(browser).openedWindow();
            windowFragment.getEditor().text("timeout", String.valueOf(timeout));
            windowFragment.clickButton(action);
            windowFragment.waitUntilClosed();
            Console.withBrowser(browser).waitUntilLoaded();
        }
    }

    private static class ModelOperations {

        private static final int TIMEOUT = 5000;

        private static final Dispatcher dispatcher = new Dispatcher();

        private static final ResourceVerifier verifier = new ResourceVerifier(dispatcher);

        void closeClient() {
            dispatcher.close();
        }

        void suspendServer() {
            suspendServer(root());
        }

        void suspendServer(String serverName) {
            suspendServer(getHostAddress().add(SERVER_CONFIG, serverName));
        }

        void suspendGroup(String group) {
            dispatcher.execute(new Operation.Builder(SUSPEND_SERVERS,
                    root().add(SERVER_GROUP, group)).build());
        }

        void resumeServer() {
            resumeServer(root());
        }

        void resumeServer(String serverName) {
            resumeServer(getHostAddress().add(SERVER_CONFIG, serverName));
        }

        void resumeGroup(String group) {
            dispatcher.execute(new Operation.Builder(RESUME_SERVERS,
                    root().add(SERVER_GROUP, group)).build());
        }

        void assertSuspendState(String suspendState) {
            assertSuspendState(root(), suspendState);
        }

        void assertSuspendState(String serverName, String suspendState) {
            ResourceAddress resourceAddress = getHostAddress().add(SERVER, serverName);
            assertSuspendState(resourceAddress, suspendState);
        }

        private void suspendServer(ResourceAddress address) {
            dispatcher.execute(new Operation.Builder(SUSPEND, address).build());
        }

        private void resumeServer(ResourceAddress address) {
            dispatcher.execute(new Operation.Builder(RESUME, address).build());
        }

        private void assertSuspendState(ResourceAddress serverAddress, String suspendState) {
            verifier.verifyAttribute(serverAddress, SUSPEND_STATE, suspendState, TIMEOUT);
        }

        private ResourceAddress getHostAddress() {
            return root().add(HOST, DEFAULT_HOST);
        }

        private ResourceAddress root() {
            return new ResourceAddress();
        }

    }

}
