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
 * Created by pjelinek on Nov 29, 2015
 */
@RunWith(Arquillian.class)
@Category(PicketLink.class)
public class SpAttrTestCase extends AbstractFederationTestCase {

    private String
        federationName = name.getFederation(),
        spName = name.getSp(),
        url = name.get(Key.URL);

    private Address spAddress = federationOps.getServiceProviderAddress(federationName, spName);

    @Before
    public void before() throws IOException, OperationException {
        setupFederationWithSp();
        page.navigateToServiceProvider(federationName, spName).switchSubTab(SERVICE_PROVIDER_LABEL);
    }

    @After
    public void after() throws IOException, OperationException {
        cleanupFederation();
    }

    @Test
    public void errorPage() throws Exception {
        final String newErrorPage = "/my_error.jsp";
        edit(InputType.TEXT, spAddress, SP_ERROR_PAGE, newErrorPage)
            .verifyFormSaved()
            .verifyAttribute(SP_ERROR_PAGE, newErrorPage);
    }

    @Test
    public void logoutPage() throws Exception {
        final String newLogoutPage = "/my_logout.jsp";
        edit(InputType.TEXT, spAddress, SP_LOGOUT_PAGE, newLogoutPage)
            .verifyFormSaved()
            .verifyAttribute(SP_LOGOUT_PAGE, newLogoutPage);
    }

    @Test
    public void postBinding() throws Exception {
        edit(InputType.CHECKBOX, spAddress, SP_POST_BINDING, false)
            .verifyFormSaved()
            .verifyAttribute(SP_POST_BINDING, false);

        edit(InputType.CHECKBOX, spAddress, SP_POST_BINDING, true)
            .verifyFormSaved()
            .verifyAttribute(SP_POST_BINDING, true);
    }

    @Test
    public void strictPostBinding() throws Exception {
        edit(InputType.CHECKBOX, spAddress, SP_STRICT_POST_BINDING, false)
            .verifyFormSaved()
            .verifyAttribute(SP_STRICT_POST_BINDING, false);

        edit(InputType.CHECKBOX, spAddress, SP_STRICT_POST_BINDING, true)
            .verifyFormSaved()
            .verifyAttribute(SP_STRICT_POST_BINDING, true);
    }

    @Test
    public void url() throws Exception {
        edit(InputType.TEXT, spAddress, URL, "")
            .verifyFormNotSaved()
            .verifyAttribute(URL, name.get(Key.URL));

        edit(InputType.TEXT, spAddress, URL, url)
            .verifyFormSaved()
            .verifyAttribute(URL, url);
    }

    @Override
    protected String getStringSuffix() {
        return "_SP_ATTR_TC";
    }

}
