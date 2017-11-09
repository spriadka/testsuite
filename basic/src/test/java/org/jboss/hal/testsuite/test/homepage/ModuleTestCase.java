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

package org.jboss.hal.testsuite.test.homepage;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.page.BasePage;
import org.jboss.hal.testsuite.page.admin.RoleAssignmentPage;
import org.jboss.hal.testsuite.page.config.DomainConfigEntryPoint;
import org.jboss.hal.testsuite.page.config.StandaloneConfigEntryPoint;
import org.jboss.hal.testsuite.page.home.HomePage;
import org.jboss.hal.testsuite.page.runtime.DomainDeploymentEntryPoint;
import org.jboss.hal.testsuite.page.runtime.DomainRuntimeEntryPoint;
import org.jboss.hal.testsuite.page.runtime.PatchManagementPage;
import org.jboss.hal.testsuite.page.runtime.StandaloneDeploymentEntryPoint;
import org.jboss.hal.testsuite.page.runtime.StandaloneRuntimeEntryPoint;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.PropUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Created by pjelinek on Oct 9, 2015
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class ModuleTestCase {

    private static boolean notYetNavigated = true;

    @Drone private WebDriver browser;

    @Page private HomePage homePage;

    @Before public void before() {
        if (notYetNavigated) {
            homePage.navigate();
            notYetNavigated = false;
        }
    }

    @Test
    public void deployments() throws Exception {
        Class <? extends BasePage> expectedAddressPage = ConfigUtils.isDomain() ?
                DomainDeploymentEntryPoint.class
              : StandaloneDeploymentEntryPoint.class;
        assertModule("deployments", expectedAddressPage, 1);
    }

    @Test
    public void configuration() throws Exception {
        Class <? extends BasePage> expectedAddressPage = ConfigUtils.isDomain() ?
                DomainConfigEntryPoint.class
              : StandaloneConfigEntryPoint.class;
        assertModule("config", expectedAddressPage, 2);
    }

    @Test
    public void runtime() throws Exception {
        Class <? extends BasePage> expectedAddressPage = ConfigUtils.isDomain() ?
                DomainRuntimeEntryPoint.class
              : StandaloneRuntimeEntryPoint.class;
        int expectedLinksNo = ConfigUtils.isDomain() ? 3 : 1;
        assertModule("runtime", expectedAddressPage, expectedLinksNo);
    }

    @Test
    public void accessControl() throws Exception {
        assertModule("access.control", RoleAssignmentPage.class, 1);
    }

    @Test
    public void patching() throws Exception {
        assertModule("patching", PatchManagementPage.class, 1);
    }

    private void assertModule(String key, Class<? extends BasePage> expectedAddressPage, int expectedLinksNo) {
        assertModuleIsPresent(key);
        assertMainModuleLinkIsPresent(key, expectedAddressPage);
        assertStartModuleLinkArePresent(key, expectedAddressPage, expectedLinksNo);
    }

    private void assertModuleIsPresent(String key) {
        String title = getModuleTitle(key);
        Assert.assertNotNull("Unable to find module " + title, homePage.getModule(title));
    }

    private void assertMainModuleLinkIsPresent(String key, Class<? extends BasePage> expectedAddressPage) {
        String moduleTitle = getModuleTitle(key);
        String hrefEnding = ConfigUtils.getPageLocation(expectedAddressPage);
        List<WebElement> links = homePage.getModule(moduleTitle).getLinks(moduleTitle, hrefEnding);
        Assert.assertEquals(1, links.size());
    }

    private void assertStartModuleLinkArePresent(String key, Class<? extends BasePage> expectedAddressPage, int expectedLinksNo) {
        String moduleTitle = getModuleTitle(key);
        String linkLabel = PropUtils.get("homepage.module.start.link.label");
        String hrefEnding = ConfigUtils.getPageLocation(expectedAddressPage);
        List<WebElement> links = homePage.getModule(moduleTitle).getLinks(linkLabel, hrefEnding);
        Assert.assertEquals(expectedLinksNo, links.size());
    }

    private String getModuleTitle(String key) {
        return PropUtils.get("homepage.module." + key + ".link.label");
    }

}
