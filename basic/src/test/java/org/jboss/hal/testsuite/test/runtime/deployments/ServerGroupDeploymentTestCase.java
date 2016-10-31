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
package org.jboss.hal.testsuite.test.runtime.deployments;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.cli.Library;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.page.runtime.DomainDeploymentEntryPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

/**
 * @author Harald Pehl
 */
@RunWith(Arquillian.class)
@Category(Domain.class)
public class ServerGroupDeploymentTestCase {

    @Drone WebDriver browser;
    private FinderNavigation navigation;

    @Before
    public void before() {
        navigation = new FinderNavigation(browser, DomainDeploymentEntryPoint.class)
                .step(FinderNames.BROWSE_BY, FinderNames.SERVER_GROUPS)
                .step(FinderNames.SERVER_GROUP, "main-server-group")
                .step(FinderNames.DEPLOYMENT);
    }

    @Test
    public void openWizard() {
        navigation.selectColumn().invoke("Add");
        Library.letsSleep(1000);
    }
}
