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
package org.jboss.hal.testsuite.test.configuration.container;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.iiop.IIOPPropertyWizard;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ConfigPropertiesFragment;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.table.ResourceTableRowFragment;
import org.jboss.hal.testsuite.page.config.IIOPPage;
import org.jboss.hal.testsuite.page.config.StandaloneConfigurationPage;
import org.jboss.hal.testsuite.util.Console;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extras.creaper.commands.foundation.online.SnapshotBackup;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Harald Pehl
 */
@RunWith(Arquillian.class)
@RunAsClient
@Category(Standalone.class)
public class IIOPTestCase {

    private static final Logger log = LoggerFactory.getLogger(IIOPTestCase.class);
    private static final String KEY_VALUE = "IIOPKey_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String VALUE = "IIOPValue_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String HIGH_WATER_MARK = "high-water-mark";
    private static final String SECURITY_DOMAIN = "security-domain";

    private FinderNavigation navigation;
    private FinderNavigation transactionNavigation;
    private Address iiopSubsystemAddress = Address.subsystem("iiop-openjdk");
    private Address transactionSubsystemAddress = Address.subsystem("transactions");
    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);
    private static final Administration adminOps = new Administration(client);

    private static final SnapshotBackup backup = new SnapshotBackup();

    @Page
    IIOPPage page;

    @Drone
    private WebDriver browser;

    @BeforeClass
    public static void init() throws CommandFailedException {
        client.apply(backup.backup());
    }

    @AfterClass
    public static void tearDown() throws CommandFailedException, InterruptedException, TimeoutException, IOException {
        try {
            client.apply(backup.restore());
            adminOps.reloadIfRequired();
        } finally {
            IOUtils.closeQuietly(client);
        }
    }

    @After
    public void after() throws IOException, InterruptedException, TimeoutException {
        adminOps.restartIfRequired();
        adminOps.reloadIfRequired();
    }

    private void navigateToIIOPSubsystemPage() {
        transactionNavigation = new FinderNavigation(browser, StandaloneConfigurationPage.class)
                .step(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                .step(FinderNames.SUBSYSTEM, "Transactions");

        navigation = new FinderNavigation(browser, StandaloneConfigurationPage.class)
                .step(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                .step(FinderNames.SUBSYSTEM, "IIOP");

        navigation.selectRow().invoke(FinderNames.VIEW);
        Application.waitUntilVisible();
    }

    @Test
    public void expectErrorForInvalidNumberToReclaimValue() throws Exception {
        navigateToIIOPSubsystemPage();
        page.switchToEditMode();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("number-to-reclaim", "-1");
        boolean finished = editPanelFragment.save();

        assertFalse("Config wasn't supposed to be saved, read-write view should be active.", finished);
        new ResourceVerifier(iiopSubsystemAddress, client).verifyAttributeIsUndefined("number-to-reclaim");
    }

    @Test
    public void numberToReclaimValueTest() throws Exception {
        navigateToIIOPSubsystemPage();
        page.switchToEditMode();
        ConfigFragment editPanelFragment = page.getConfigFragment();
        editPanelFragment.getEditor().text("number-to-reclaim", "5");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        new ResourceVerifier(iiopSubsystemAddress, client).verifyAttribute("number-to-reclaim", 5);

        page.switchToEditMode();
        editPanelFragment = page.getConfigFragment();
        editPanelFragment.getEditor().text("number-to-reclaim", "");
        finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        new ResourceVerifier(iiopSubsystemAddress, client).verifyAttributeIsUndefined("number-to-reclaim"); //https://issues.jboss.org/browse/HAL-790
    }

    @Test
    public void expectErrorForInvalidHighWaterMarkValue() throws Exception {
        navigateToIIOPSubsystemPage();
        page.switchToEditMode();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("high-water-mark", "-1");
        boolean finished = editPanelFragment.save();
        log.debug("f : " + finished);
        assertFalse("Config wasn't supposed to be saved, read-write view should be active.", finished);
        new ResourceVerifier(iiopSubsystemAddress, client).verifyAttributeIsUndefined("high-water-mark");
    }

    @Test
    public void highWaterMarkValueTest() throws Exception {
        navigateToIIOPSubsystemPage();
        page.switchToEditMode();
        ConfigFragment editPanelFragment = page.getConfigFragment();
        Editor editor = editPanelFragment.getEditor();
        Console.withBrowser(browser).scrollIntoView(editor.getText(HIGH_WATER_MARK));
        editor.text(HIGH_WATER_MARK, "7");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        new ResourceVerifier(iiopSubsystemAddress, client).verifyAttribute("high-water-mark", 7);

        page.switchToEditMode();
        editPanelFragment = page.getConfigFragment();
        editPanelFragment.getEditor().text("high-water-mark", "");
        finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        new ResourceVerifier(iiopSubsystemAddress, client).verifyAttributeIsUndefined("high-water-mark"); //https://issues.jboss.org/browse/HAL-790
    }

    @Test
    public void setAuthMethodValues() throws Exception {
        navigateToIIOPSubsystemPage();
        final String attributeName = "auth-method";

        final ModelNodeResult originalModelNodeResult = operations.readAttribute(iiopSubsystemAddress, attributeName);
        originalModelNodeResult.assertSuccess();

        try {
            boolean finished;

            page.switchToEditMode();
            ConfigFragment editPanelFragment = page.getConfigFragment();

            editPanelFragment.getEditor().select(attributeName, "none");
            finished = editPanelFragment.save();
            log.debug("f : " + finished);

            assertTrue("Config should be saved and closed.", finished);
            new ResourceVerifier(iiopSubsystemAddress, client).verifyAttribute(attributeName, "none");

            page.switchToEditMode();
            editPanelFragment = page.getConfigFragment();
            editPanelFragment.getEditor().select(attributeName, "username_password");
            finished = editPanelFragment.save();

            assertTrue("Config should be saved and closed.", finished);
            new ResourceVerifier(iiopSubsystemAddress, client).verifyAttribute(attributeName, "username_password");
        } finally {
            operations.writeAttribute(iiopSubsystemAddress, attributeName, originalModelNodeResult.value()).assertSuccess();
            adminOps.reloadIfRequired();
        }

    }

    @Test
    public void addProperty() throws Exception {
        navigateToIIOPSubsystemPage();
        ConfigPropertiesFragment properties = page.getConfig().propertiesConfig();
        page.waitUntilPropertiesAreVisible();
        try {
            properties.getResourceManager()
                    .addResource(IIOPPropertyWizard.class)
                    .key(KEY_VALUE)
                    .value(VALUE)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            List<ResourceTableRowFragment> actual = page.getResourceManager().getResourceTable().getAllRows();

            assertEquals("Table should have one row.", 1, actual.size());

            ModelNode expectedPropertiesNode = new ModelNodeGenerator().createObjectNodeWithPropertyChild(KEY_VALUE, VALUE);
            new ResourceVerifier(iiopSubsystemAddress, client).verifyAttribute("properties", expectedPropertiesNode);
        } finally {
            operations.invoke("map-remove", iiopSubsystemAddress, Values.of("name", "properties")
                    .and("key", KEY_VALUE));
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void removeProperty() throws Exception {
        operations.invoke("map-put", iiopSubsystemAddress,
                Values.of("name", "properties").and("key", KEY_VALUE)
                        .and("value", VALUE));
        adminOps.reloadIfRequired();
        navigateToIIOPSubsystemPage();
        ConfigPropertiesFragment properties = page.getConfig().propertiesConfig();
        page.waitUntilPropertiesAreVisible();
        properties.removePropertyAndDismissReloadRequiredButton(KEY_VALUE);

        List<ResourceTableRowFragment> actual = page.getResourceManager().getResourceTable().getAllRows();

        assertEquals("Table should be empty.", 0, actual.size());
        new ResourceVerifier(iiopSubsystemAddress, client).verifyAttributeIsUndefined("properties");
    }

    @Test
    public void setRealmValues() throws Exception {
        navigateToIIOPSubsystemPage();
        page.switchToEditMode();
        ConfigFragment editPanelFragment = page.getConfigFragment();
        editPanelFragment.getEditor().text("realm", VALUE);
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        new ResourceVerifier(iiopSubsystemAddress, client).verifyAttribute("realm", VALUE);

        page.switchToEditMode();
        editPanelFragment = page.getConfigFragment();
        editPanelFragment.getEditor().text("realm", "");
        finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        new ResourceVerifier(iiopSubsystemAddress, client).verifyAttributeIsUndefined("realm");
    }

    @Test
    public void setSecurityDomainValues() throws Exception {
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(iiopSubsystemAddress, SECURITY_DOMAIN);
        originalModelNodeResult.assertSuccess();
        Address securityDomainAddress = Address.subsystem("security").and(SECURITY_DOMAIN, VALUE);
        try {
            operations.add(securityDomainAddress);
            navigateToIIOPSubsystemPage();
            page.switchToEditMode();
            ConfigFragment editPanelFragment = page.getConfigFragment();
            editPanelFragment.getEditor().text(SECURITY_DOMAIN, VALUE);
            boolean finished = editPanelFragment.save();
            assertTrue("Config should be saved and closed.", finished);
            new ResourceVerifier(iiopSubsystemAddress, client).verifyAttribute(SECURITY_DOMAIN, VALUE);
            page.switchToEditMode();
            editPanelFragment = page.getConfigFragment();
            editPanelFragment.getEditor().text(SECURITY_DOMAIN, "");
            finished = editPanelFragment.save();
            assertTrue("Config should be saved and closed.", finished);
            new ResourceVerifier(iiopSubsystemAddress, client).verifyAttributeIsUndefined(SECURITY_DOMAIN);
        } finally {
            operations.writeAttribute(iiopSubsystemAddress, SECURITY_DOMAIN, originalModelNodeResult.value());
            operations.removeIfExists(securityDomainAddress);
        }
    }

    @Test
    public void setTransactionsToNoneWithEnablingJTS() throws Exception {
        navigateToIIOPSubsystemPage();
        page.switchToEditMode();
        ConfigFragment editPanelFragment = page.getConfigFragment();
        editPanelFragment.getEditor().select("transactions", "none");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        new ResourceVerifier(iiopSubsystemAddress, client).verifyAttribute("transactions", "none");

        transactionNavigation.selectRow().invoke("View");
        Application.waitUntilVisible();

        page.switchToEditMode();
        editPanelFragment = page.getConfigFragment();
        editPanelFragment.getEditor().checkbox("jts", true);
        finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        new ResourceVerifier(transactionSubsystemAddress, client).verifyAttribute("jts", true);
    }

    @Test
    public void setTransactionsToSpecWithDisablingJTS() throws Exception {
        navigateToIIOPSubsystemPage();
        page.switchToEditMode();
        ConfigFragment editPanelFragment = page.getConfigFragment();
        editPanelFragment.getEditor().select("transactions", "spec");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        new ResourceVerifier(iiopSubsystemAddress, client).verifyAttribute("transactions", "spec");

        transactionNavigation.selectRow().invoke("View");
        Application.waitUntilVisible();

        page.switchToEditMode();
        editPanelFragment = page.getConfigFragment();
        editPanelFragment.getEditor().checkbox("jts", false);
        finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        new ResourceVerifier(transactionSubsystemAddress, client).verifyAttribute("jts", false);
    }
}
