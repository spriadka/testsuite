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

package org.jboss.hal.testsuite.test.configuration.profiles;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.page.config.DomainConfigurationPage;
import org.jboss.hal.testsuite.page.config.ProfilesConfigurationPage;
import org.jboss.hal.testsuite.page.home.HomePage;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.ResourceVerifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertTrue;

/**
 * Created by pjelinek on Jul 9, 2015
 */
@RunWith(Arquillian.class)
@Category(Domain.class)
public class ProfileCloningTestCase {

    @Drone
    public WebDriver browser;

    @Page
    public ProfilesConfigurationPage page;

    private CliClient client = CliClientFactory.getClient();

    private static final String DEFAULT = "default";
    private static final String NEW_PROFILE_NAME = "qwerty" + RandomStringUtils.randomAlphabetic(5);
    private static final String NEW_PROFILE_NAME_WITH_WHITESPACE = "qw rty" + RandomStringUtils.randomAlphabetic(5);
    private static final String DMR_NEW_PROFILE_NAME_WITH_WHITESPACE = NEW_PROFILE_NAME_WITH_WHITESPACE
            .replaceAll(" ", "\\\\ ");
    private static final String DMR_PROFILE_PREFIX = "/profile=";

    @Before
    public void before() {
        browser.navigate().refresh();
        Graphene.goTo(HomePage.class);
        Console.withBrowser(browser).waitUntilLoaded();
        Graphene.goTo(DomainConfigurationPage.class);
        Console.withBrowser(browser).waitUntilLoaded().maximizeWindow();
    }

    @Test
    public void cloneDefaultProfile() {
        page.tryToCloneProfile(DEFAULT, NEW_PROFILE_NAME);
        assertTrue("Clone profile dialog should be successfully closed!", page.isCreateNewProfileWindowOpened());
        new ResourceVerifier(DMR_PROFILE_PREFIX + NEW_PROFILE_NAME, client).verifyResource(true);
        page.tryToCloneProfile(DEFAULT, NEW_PROFILE_NAME_WITH_WHITESPACE);
        assertTrue("Whitespace is allowed in profile name. Clone profile dialog should be successfully closed!",
                page.isCreateNewProfileWindowOpened());
        new ResourceVerifier(DMR_PROFILE_PREFIX + DMR_NEW_PROFILE_NAME_WITH_WHITESPACE, client).verifyResource(true);
    }

    @After
    public void cleanUp() {
        removeProfile(NEW_PROFILE_NAME);
        removeProfile(DMR_NEW_PROFILE_NAME_WITH_WHITESPACE);
    }

    private void removeProfile(String profileName) {
        client.removeResource(DMR_PROFILE_PREFIX + profileName);
    }
}
