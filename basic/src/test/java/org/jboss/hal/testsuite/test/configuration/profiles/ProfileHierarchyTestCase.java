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

import static org.junit.Assert.*;
import static org.jboss.hal.testsuite.finder.FinderNames.*;


import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.page.config.DomainConfigEntryPoint;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

/**
 * Created by pjelinek on Aug 17, 2015
 */
@RunWith(Arquillian.class)
@Category(Domain.class)
public class ProfileHierarchyTestCase {

    private static final Dispatcher dispatcher = new Dispatcher();
    private static final String profileMailName = "profileMail", profileIOName = "profileIO", profileComposedName = "profileComposed";

    @Drone
    private WebDriver browser;

    private final ProfileOperations ops = new ProfileOperations(dispatcher);
    private FinderNavigation navi;

    @Before
    public void before() {
        ops.addProfileWithSubsystem(profileIOName, "io");
        ops.addProfileWithSubsystem(profileMailName, "mail");
        ops.addComposedProfile(profileComposedName, profileIOName, profileMailName);
        navi = newNavi()
            .step(CONFIGURATION, PROFILES)
            .step(PROFILE, profileComposedName);
    }

    @After
    public void after() {
        ops.removeProfile(profileComposedName);
        ops.removeProfile(profileIOName);
        ops.removeProfile(profileMailName);
    }

    @AfterClass
    public static void closeDispatcher() {
        dispatcher.close();
    }

    @Test
    public void includeTest() throws Exception {
        String rowText = navi.selectRow().getText();
        assertTrue(profileComposedName + " should contain info that it includes " + profileIOName, rowText.contains(profileIOName));
        assertTrue(profileComposedName + " should contain info that it includes " + profileMailName, rowText.contains(profileMailName));

        navi.step(SUBSYSTEM, "IO").selectRow();
        assertEquals(profileIOName, navi.getPreview().getIncludedFromProfile());

        navi = newNavi()
            .step(CONFIGURATION, PROFILES)
            .step(PROFILE, profileComposedName)
            .step(SUBSYSTEM, "Mail");
        navi.selectRow();
        assertEquals(profileMailName, navi.getPreview().getIncludedFromProfile());
    }

    /**
     * See profile composed of two profiles, than remove one and verify that nothing is cached after browser reload.
     */
    @Test
    public void removeOneParentProfileTest() throws Exception {
        navi.selectRow();
        ops.removeProfileFromIncludes(profileMailName, profileComposedName);

        navi = newNavi()
            .step(CONFIGURATION, PROFILES)
            .step(PROFILE, profileComposedName);

        String rowText = navi.selectRow().getText();
        assertTrue(profileComposedName + " should contain info that it includes " + profileIOName, rowText.contains(profileIOName));
        assertFalse(profileComposedName + " should NOT contain info that it includes " + profileMailName, rowText.contains(profileMailName));

        navi.step(SUBSYSTEM, "IO").selectRow();
        assertEquals(profileIOName, navi.getPreview().getIncludedFromProfile());

        navi = newNavi()
            .step(CONFIGURATION, PROFILES)
            .step(PROFILE, profileComposedName)
            .step(SUBSYSTEM, "Mail");
        try {
            navi.selectRow();
        } catch (NoSuchElementException e) {
            return;
        }
        fail("Mail susbsystem should not be present any more!");
    }

    @Test
    public void addSubsystemToParentProfileTest() throws Exception {
        navi.selectRow();
        ops.addSubsystem(profileIOName, "datasources");

        String rowText = navi.selectRow().getText();
        assertTrue(profileComposedName + " should contain info that it includes " + profileIOName, rowText.contains(profileIOName));
        assertTrue(profileComposedName + " should contain info that it includes " + profileMailName, rowText.contains(profileMailName));

        navi.step(SUBSYSTEM, "IO").selectRow();
        assertEquals(profileIOName, navi.getPreview().getIncludedFromProfile());

        navi = newNavi()
            .step(CONFIGURATION, PROFILES)
            .step(PROFILE, profileComposedName)
            .step(SUBSYSTEM, "Mail");
        navi.selectRow();
        assertEquals(profileMailName, navi.getPreview().getIncludedFromProfile());

        navi = newNavi()
            .step(CONFIGURATION, PROFILES)
            .step(PROFILE, profileComposedName)
            .step(SUBSYSTEM, "Datasources");
        navi.selectRow();
        assertEquals(profileIOName, navi.getPreview().getIncludedFromProfile());
    }

    // HAL-803 workaround
    private FinderNavigation newNavi() {
        return new FinderNavigation(browser, DomainConfigEntryPoint.class, 200);
    }

}
