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
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.dmr.Dispatcher;
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
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.junit.Before;
import org.junit.Test;
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
    Dispatcher dispatcher = new Dispatcher();
    ResourceVerifier verifier = new ResourceVerifier(dispatcher);

    @Page
    IIOPPage page;

    @Before
    public void before() {
        if (ConfigUtils.isDomain()) {

        } else {
            navigation = new FinderNavigation(browser, StandaloneConfigurationPage.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                    .addAddress(FinderNames.SUBSYSTEM, "IIOP");
        }
        navigation.selectRow().invoke(FinderNames.VIEW);
        Application.waitUntilVisible();
    }

    @Test
    public void expectErrorForInvalidNumberToReclaimValue() {
        page.switchToEditMode();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("number-to-reclaim", "-1");
        boolean finished = editPanelFragment.save();

        assertFalse("Config wasn't supposed to be saved, read-write view should be active.", finished);
    }

    @Test
    public void NumberToReclaimValueTest() {
        page.switchToEditMode();
        ConfigFragment editPanelFragment = page.getConfigFragment();
        editPanelFragment.getEditor().text("number-to-reclaim", "5");
        boolean finished = editPanelFragment.save();

        assertTrue("Config wasn't supposed to be saved, read-write view should be active.", finished);

        editPanelFragment.getEditor().text("number-to-reclaim", ""); // undefined? co nastavit prazdny string
        finished = editPanelFragment.save();

        assertTrue("Config wasn't supposed to be saved, read-write view should be active.", finished);
    }

    @Test
    public void expectErrorForInvalidHighWaterMarkValue() {
        page.switchToEditMode();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("high-water-mark", "-1");
        boolean finished = editPanelFragment.save();
        log.debug("f : " + finished);
        assertFalse("Config wasn't supposed to be saved, read-write view should be active.", finished);
    }

    @Test
    public void setNoneAuthMethod() {
        page.switchToEditMode();
        ConfigFragment editPanelFragment = page.getConfigFragment();
        editPanelFragment.getEditor().select("auth-method", "none");
        boolean finished = editPanelFragment.save();
        log.debug("f : " + finished);

        assertTrue("Config should be saved and closed.", finished);

        editPanelFragment.getEditor().select("auth-method", "");
        finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);

        editPanelFragment.getEditor().select("auth-method", "username_password");
        finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
    }

    @Test
    public void addProperty(){
        ConfigPropertiesFragment properties = page.getConfig().propertiesConfig();
        ConfigPropertyWizard wizard = properties.addProperty();

        boolean result = wizard.name(KEY_VALUE).value(VALUE).finish();

        assertTrue("Property shoudl be added and wizard closed.",result);

        List<ResourceTableRowFragment> actual = page.getResourceManager().getResourceTable().getAllRows();

        assertEquals("Talbe should have one row.", 1, actual.size());
    }

    @Test
    public void removeProperty(){
        ConfigPropertiesFragment properties = page.getConfig().propertiesConfig();
        properties.removeProperty(KEY_VALUE);

        List<ResourceTableRowFragment> actual = page.getResourceManager().getResourceTable().getAllRows();

        assertEquals("Talbe should be empty.", 0, actual.size());
    }
}
