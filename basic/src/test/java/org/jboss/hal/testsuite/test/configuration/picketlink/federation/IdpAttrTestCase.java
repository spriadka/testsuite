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

import java.io.IOException;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.PicketLink;
import org.jboss.hal.testsuite.test.configuration.picketlink.federation.UtilFedInputChecker.InputType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

/**
 * Created by pjelinek on Nov 23, 2015
 */
@RunWith(Arquillian.class)
@Category(PicketLink.class)
public class IdpAttrTestCase extends AbstractFederationTestCase {

    private Address idpAddress = federationOps.getIdentityProviderAddress(name.getFederation(), name.getIdp());

    @Before
    public void before() throws IOException, OperationException {
        setupFederation();
        page.navigateToFederation(name.getFederation()).switchSubTab(IDENTITY_PROVIDER_LABEL);
    }

    @After
    public void after() throws IOException, OperationException {
        cleanupFederation();
    }

    @Test
    public void secDomain() throws Exception {
        edit(InputType.SELECT, idpAddress, SECURITY_DOMAIN, SD_JBOSS_WEB_POLICY)
            .verifyFormSaved()
            .verifyAttribute(SECURITY_DOMAIN, SD_JBOSS_WEB_POLICY);
    }

    @Test
    public void url() throws Exception {
        String
            emptyUrl = "",
            validUrl = "http://example.net/changed-idp/";

        edit(InputType.TEXT, idpAddress, URL, emptyUrl)
            .verifyFormNotSaved()
            .verifyAttribute(URL, name.get(Key.URL));

        edit(InputType.TEXT, idpAddress, URL, validUrl)
            .verifyFormSaved()
            .verifyAttribute(URL, validUrl);
    }

    @Test
    public void external() throws Exception {
        edit(InputType.CHECKBOX, idpAddress, IDP_EXTERNAL, true)
            .verifyFormSaved()
            .verifyAttribute(IDP_EXTERNAL, true);
        edit(InputType.CHECKBOX, idpAddress, IDP_EXTERNAL, false)
            .verifyFormSaved()
            .verifyAttribute(IDP_EXTERNAL, false);
    }

    @Override
    protected String getStringSuffix() {
        return "_IDP_TC";
    }

}
