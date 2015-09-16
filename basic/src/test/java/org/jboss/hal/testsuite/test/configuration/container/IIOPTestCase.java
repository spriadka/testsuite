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

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.ResourceVerifier;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ConfigPropertiesFragment;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ConfigPropertyWizard;
import org.jboss.hal.testsuite.fragment.shared.table.ResourceTableRowFragment;
import org.jboss.hal.testsuite.page.config.IIOPPage;
import org.jboss.hal.testsuite.page.config.StandaloneConfigurationPage;
import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Harald Pehl
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class IIOPTestCase {

    private static final Logger log = LoggerFactory.getLogger(IIOPTestCase.class);
    private static final String KEY_VALUE = RandomStringUtils.randomAlphanumeric(5);
    private static final String VALUE = RandomStringUtils.randomAlphanumeric(5);

    @Drone WebDriver browser;
    private FinderNavigation navigation;
    private FinderNavigation transactionNavigation;
    private ModelNode path = new ModelNode("/subsystem=iiop-openjdk");
    private ResourceAddress address = new ResourceAddress(path);
    private ModelNode transactionPath = new ModelNode("/subsystem=transactions");
    private ResourceAddress transactionAddress = new ResourceAddress(transactionPath);
    Dispatcher dispatcher = new Dispatcher();
    ResourceVerifier verifier = new ResourceVerifier(dispatcher);
    private CliClient cliClient = CliClientFactory.getClient();

    @Page
    IIOPPage page;

    @Before
    public void before() {
        transactionNavigation = new FinderNavigation(browser, StandaloneConfigurationPage.class)
                .addAddress(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                .addAddress(FinderNames.SUBSYSTEM, "Transactions");

        navigation = new FinderNavigation(browser, StandaloneConfigurationPage.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                    .addAddress(FinderNames.SUBSYSTEM, "IIOP");

        navigation.selectRow().invoke(FinderNames.VIEW);
        Application.waitUntilVisible();
    }

    @After
    public void after(){
        cliClient.reload();
    }




    @Test
    @InSequence(0)
    public void expectErrorForInvalidNumberToReclaimValue() {
        page.switchToEditMode();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("number-to-reclaim", "-1");
        boolean finished = editPanelFragment.save();

        assertFalse("Config wasn't supposed to be saved, read-write view should be active.", finished);
        verifier.verifyAttribute(address, "number-to-reclaim", "undefined");
    }

    @Test
    @InSequence(1)
    public void numberToReclaimValueTest() {
        page.switchToEditMode();
        ConfigFragment editPanelFragment = page.getConfigFragment();
        editPanelFragment.getEditor().text("number-to-reclaim", "5");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "number-to-reclaim", "5");

        page.switchToEditMode();
        editPanelFragment = page.getConfigFragment();
        editPanelFragment.getEditor().text("number-to-reclaim", "");
        finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "number-to-reclaim","undefined"); //https://issues.jboss.org/browse/HAL-790
    }

    @Test
    @InSequence(2)
    public void expectErrorForInvalidHighWaterMarkValue() {
        page.switchToEditMode();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("high-water-mark", "-1");
        boolean finished = editPanelFragment.save();
        log.debug("f : " + finished);
        assertFalse("Config wasn't supposed to be saved, read-write view should be active.", finished);
        verifier.verifyAttribute(address, "high-water-mark","undefined");
    }

    @Test
    @InSequence(3)
    public void highWaterMarkValueTest() {
        page.switchToEditMode();
        ConfigFragment editPanelFragment = page.getConfigFragment();
        editPanelFragment.getEditor().text("high-water-mark", "7");
        boolean finished = editPanelFragment.save();

        assertTrue("onfig should be saved and closed.", finished);
        verifier.verifyAttribute(address, "high-water-mark", "7");

        page.switchToEditMode();
        editPanelFragment = page.getConfigFragment();
        editPanelFragment.getEditor().text("high-water-mark", "");
        finished = editPanelFragment.save();

        assertTrue("onfig should be saved and closed.", finished);
        verifier.verifyAttribute(address, "high-water-mark", "undefined"); //https://issues.jboss.org/browse/HAL-790
    }

    @Test
    @InSequence(4)
    public void setAuthMethodValues() {
        page.switchToEditMode();
        ConfigFragment editPanelFragment = page.getConfigFragment();
        editPanelFragment.getEditor().select("auth-method", "none");
        boolean finished = editPanelFragment.save();
        log.debug("f : " + finished);

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "auth-method", "none");

        page.switchToEditMode();
        editPanelFragment = page.getConfigFragment();
        editPanelFragment.getEditor().select("auth-method", "");
        finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        //when setting undefined ("") value , value is set to default
        verifier.verifyAttribute(address, "auth-method", "username_password");

        page.switchToEditMode();
        editPanelFragment = page.getConfigFragment();
        editPanelFragment.getEditor().select("auth-method", "username_password");
        finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "auth-method", "username_password");
    }

    @Test
    @InSequence(5)
    public void addProperty(){
        ConfigPropertiesFragment properties = page.getConfig().propertiesConfig();
        page.waitUntilPropertiesAreVisible();
        ConfigPropertyWizard wizard = properties.addProperty();

        boolean result = wizard.name(KEY_VALUE).value(VALUE).finish();

        assertTrue("Property shoudl be added and wizard closed.",result);

        List<ResourceTableRowFragment> actual = page.getResourceManager().getResourceTable().getAllRows();

        assertEquals("Talbe should have one row.", 1, actual.size());
    }

    @Test
    @InSequence(6)
    public void removeProperty(){
        ConfigPropertiesFragment properties = page.getConfig().propertiesConfig();
        page.waitUntilPropertiesAreVisible();
        properties.removeProperty(KEY_VALUE);

        List<ResourceTableRowFragment> actual = page.getResourceManager().getResourceTable().getAllRows();

        assertEquals("Talbe should be empty.", 0, actual.size());
    }

    @Test
    @InSequence(9)
    public void setTransactionsToNoneWithEnablingJTS() {
        page.switchToEditMode();
        ConfigFragment editPanelFragment = page.getConfigFragment();
        editPanelFragment.getEditor().select("transactions", "none");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "transactions", "none");

        transactionNavigation.selectRow().invoke("View");
        Application.waitUntilVisible();

        page.switchToEditMode();
        editPanelFragment = page.getConfigFragment();
        editPanelFragment.getEditor().checkbox("jts", true);
        finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(transactionAddress, "jts", true);
    }

    @Test
    @InSequence(10)
    public void setTransactionsToSpecWithDisablingJTS()
    {
        page.switchToEditMode();
        ConfigFragment editPanelFragment = page.getConfigFragment();
        editPanelFragment.getEditor().select("transactions", "spec");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "transactions", "spec");

        transactionNavigation.selectRow().invoke("View");
        Application.waitUntilVisible();

        page.switchToEditMode();
        editPanelFragment = page.getConfigFragment();
        editPanelFragment.getEditor().checkbox("jts", false);
        finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(transactionAddress, "jts", false);
    }

    @Test
    @InSequence(7)
    public void setRealmValues() {
        page.switchToEditMode();
        ConfigFragment editPanelFragment = page.getConfigFragment();
        editPanelFragment.getEditor().text("realm", VALUE);
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "realm", VALUE);

        page.switchToEditMode();
        editPanelFragment = page.getConfigFragment();
        editPanelFragment.getEditor().text("realm", "");
        finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "realm", "undefined");
    }

    @Test
    @InSequence(8)
    public void setSecurityDomainValues() {
        page.switchToEditMode();
        ConfigFragment editPanelFragment = page.getConfigFragment();
        editPanelFragment.getEditor().text("security-domain", VALUE);
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "security-domain", VALUE);

        page.switchToEditMode();
        editPanelFragment = page.getConfigFragment();
        editPanelFragment.getEditor().text("security-domain", "");
        finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "security-domain", "undefined");
    }
}
