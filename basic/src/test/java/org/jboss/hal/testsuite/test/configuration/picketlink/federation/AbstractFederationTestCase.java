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

package org.jboss.hal.testsuite.test.configuration.picketlink.federation;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.page.config.FederationPage;
import org.jboss.hal.testsuite.test.configuration.picketlink.federation.UtilFedInputChecker.InputType;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.jboss.hal.testsuite.test.configuration.picketlink.federation.UtilFedName.Key;
import static org.jboss.hal.testsuite.test.configuration.picketlink.federation.UtilFedName.SD_OTHER;

/**
 * Created by pjelinek on Nov 18, 2015
 */
public abstract class AbstractFederationTestCase {

    protected static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    protected static final FederationOperations federationOps = new FederationOperations(client);
    protected static final Administration adminOps = new Administration(client);
    protected UtilFedName name = new UtilFedName(this);

    @Drone
    protected WebDriver browser;

    @Page
    protected FederationPage page;

    @BeforeClass
    public static final void beforeClass() throws IOException, InterruptedException, TimeoutException {
        if (changeSubsystem()) {
            federationOps.addSubsystem();
        }
    }

    @AfterClass
    public static final void afterClass() throws IOException, InterruptedException, TimeoutException {
        try {
            if (changeSubsystem()) {
                federationOps.removeSubsystem();
            } else {
                adminOps.reloadIfRequired();
            }
        } finally {
            IOUtils.closeQuietly(client);
        }
    }

    protected final void setupFederation() throws IOException, OperationException {
        federationOps.addFederation(name.getFederation());
        federationOps.addIdp(name.getFederation(), name.getIdp(), name.get(Key.URL), SD_OTHER);
    }

    protected final void setupFederationWithSp() throws IOException, OperationException {
        setupFederation();
        federationOps.addSp(name.getFederation(), name.getSp(), name.get(Key.URL), SD_OTHER);
    }

    protected final void cleanupFederation() throws IOException, OperationException {
        federationOps.removeFederation(name.getFederation());
    }

    /**
     * Valid {@code inputType} and {@code attrValue} parameter combinations are:
     * <ul>
     * <li>{@code InputType.TEXT} or {@code InputType.SELECT}
     * and {@code attrValue} of type {@code String}, {@code Integer} or {@code Long}</li>
     * <li>{@code InputType.CHECKBOX} and {@code attrValue} of type {@code Boolean}</li>
     * </ul>
     */
    protected final UtilFedInputChecker edit(InputType inputType, Address resourceAddress, String dmrAttrName,
            Object attrValue) throws IOException, InterruptedException, TimeoutException {
        return new UtilFedInputChecker(client, page.getConfigFragment(), inputType, resourceAddress, dmrAttrName,
                attrValue);
    }

    /**
     * testcase specific substring used to distinguish between resources created by various testcases
     */
    protected abstract String getStringSuffix();

    /**
     * @return '{@code true}' unless system property '{@code federation.already.enabled}' is set to '{@code true}'
     */
    private static boolean changeSubsystem() {
        return !Boolean.parseBoolean(ConfigUtils.get("federation.already.enabled"));
    }

}
