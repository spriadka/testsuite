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

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.page.config.StandaloneConfigurationPage;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertFalse;

/**
 * @author Harald Pehl
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class IIOPTestCase {

    @Drone WebDriver browser;
    private FinderNavigation navigation;

    @Before
    public void before() {
        navigation = new FinderNavigation(browser, StandaloneConfigurationPage.class)
                .addAddress(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                .addAddress(FinderNames.SUBSYSTEM, "IIOP");
    }

    @Test
    public void expectErrorForInvalidHighWaterMarkValue() {
        navigation.selectRow().invoke(FinderNames.VIEW);
        Application.waitUntilVisible();

        WebElement viewPanel = browser.findElement(By.className("form-view-panel"));
        WebElement editLink = viewPanel.findElement(By.className("form-edit-button"));
        editLink.click();

        // wait until we switched to edit mode
        Graphene.waitGui().until().element(By.className("form-edit-panel")).is().visible();

        WebElement editPanel = browser.findElement(By.className("form-edit-panel"));
        ConfigFragment editPanelFragment = Graphene.createPageFragment(ConfigFragment.class, editPanel);
        editPanelFragment.getEditor().text("high-water-mark", "-1");
        boolean finished = editPanelFragment.save();

        assertFalse("Config wasn't supposed to be saved, read-write view should be active.", finished);
    }
}
