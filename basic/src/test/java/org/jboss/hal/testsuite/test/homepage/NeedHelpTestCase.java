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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.fragment.WindowFragment;
import org.jboss.hal.testsuite.fragment.homepage.HomepageNeedHelpSectionFragment;
import org.jboss.hal.testsuite.page.home.HomePage;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.PropUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Created by pjelinek on Oct 21, 2015
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class NeedHelpTestCase {

    private static boolean notYetNavigated = true;

    @Drone
    private WebDriver browser;

    @Page
    private HomePage homePage;

    @Before
    public void before() {
        if (notYetNavigated) {
            homePage.navigate();
            notYetNavigated = false;
        }
    }

    @Test
    public void generalResourcesTest() throws Exception {
        String[] keys = {"doc", "about", "help", "training"};
        Map<String, String> links = expectedLinks("general", keys);

        String title = PropUtils.get("homepage.needhelp.general.title");
        HomepageNeedHelpSectionFragment section = homePage.getNeedHelpSection(title);

        assertLinksArePresent(section, links);
    }

    @Test
    public void getHelpTest() throws Exception {
        String[] keys = {"quickstarts", "community", "matrix", "kb", "consulting"};
        Map<String, String> links = expectedLinks("gethelp", keys);

        String title = PropUtils.get("homepage.needhelp.gethelp.title");
        HomepageNeedHelpSectionFragment section = homePage.getNeedHelpSection(title);

        assertLinksArePresent(section, links);
    }

    @Test
    public void newToEAP7Test() {
        WebElement root = homePage.getContentRoot();

        assertElementIsPresent("'New to EAP?' text should be present.", root,
                new ByJQuery(":contains('" + PropUtils.get("homepage.newtoeap.text") + "')"));

        By linkSelector = new ByJQuery("a:contains('" + PropUtils.get("homepage.newtoeap.link") + "')");
        assertElementIsPresent("'New to EAP?' link should be present.", root, linkSelector);

        root.findElement(linkSelector).click();

        Console.withBrowser(browser).openedWindow(WindowFragment.class);
    }

    private Map<String, String> expectedLinks(String sectionKey, String[] linkKeys) {
        return Arrays.stream(linkKeys).collect(Collectors.toMap(
                link -> PropUtils.get("homepage.needhelp." + sectionKey + ".links." + link + ".label"),
                link -> PropUtils.get("homepage.needhelp." + sectionKey + ".links." + link + ".href")));
    }

    private void assertLinksArePresent(HomepageNeedHelpSectionFragment section, Map<String, String> links) {
        Map<String, String> sectionLinks = section.getAllLinks();

        for (String label : links.keySet()) {
            Assert.assertTrue("Link <" + label + "> not found", sectionLinks.containsKey(label));
            Assert.assertEquals("Different address for link <" + label + ">", links.get(label), sectionLinks.get(label));
            sectionLinks.remove(label);
        }

        Assert.assertTrue("Unexpected links: " + sectionLinks, sectionLinks.isEmpty());
    }

    private void assertElementIsPresent(String message, WebElement root, By selector) {
        List<WebElement> found = root.findElements(selector);

        Assert.assertFalse(message, found.isEmpty());
    }

}
