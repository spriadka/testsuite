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

import static org.jboss.hal.testsuite.test.configuration.picketlink.federation.UtilFedName.*;
import static org.jboss.hal.testsuite.test.configuration.picketlink.federation.UtilFedInputChecker.InputType.TEXT;
import java.io.IOException;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.PicketLink;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

/**
 * Created by pjelinek on Nov 18, 2015
 */
@RunWith(Arquillian.class)
@Category(PicketLink.class)
public class SAMLConfigTestCase extends AbstractFederationTestCase {

    private Address samlAddress = federationOps.getSAMLConfigAddress(name.getFederation());

    @Before
    public void before() throws IOException, OperationException {
        setupFederation();
        page.navigateToFederation(name.getFederation()).switchSubTab(SAML_CONFIGURATION_LABEL);
    }

    @After
    public void after() throws IOException, OperationException {
        cleanupFederation();
    }

    @Test
    public void editClockSkew() throws Exception {
        int invalidClockSkew = -6, validClockSkew = 6;
        edit(TEXT, samlAddress, SAML_CLOCK_SKEW, invalidClockSkew)
            .verifyFormNotSaved()
            .verifyAttributeNotEqual(SAML_CLOCK_SKEW, new ModelNode(invalidClockSkew));

        edit(TEXT, samlAddress, SAML_CLOCK_SKEW, validClockSkew)
            .verifyFormSaved()
            .verifyAttribute(SAML_CLOCK_SKEW, validClockSkew);
    }

    @Test
    public void editTokenTimeout() throws Exception {
        int invalidTokenTimeout = -7, validTokenTimeout = 7;
        edit(TEXT, samlAddress, SAML_TOKEN_TIMEOUT, invalidTokenTimeout)
            .verifyFormNotSaved()
            .verifyAttributeNotEqual(SAML_TOKEN_TIMEOUT, new ModelNode(invalidTokenTimeout));
        edit(TEXT, samlAddress, SAML_TOKEN_TIMEOUT, validTokenTimeout)
            .verifyFormSaved()
            .verifyAttribute(SAML_TOKEN_TIMEOUT, validTokenTimeout);
    }

    @Override
    protected String getStringSuffix() {
        return "_SAML_CONFIG_TC";
    }


}
