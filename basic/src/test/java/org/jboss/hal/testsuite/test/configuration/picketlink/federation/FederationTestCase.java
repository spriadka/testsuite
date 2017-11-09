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
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.WindowFragment;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

/**
 * Created by pjelinek on Nov 16, 2015
 */
@RunWith(Arquillian.class)
@Category(PicketLink.class)
public class FederationTestCase extends AbstractFederationTestCase {

    private String
        federationName = name.getFederation(),
        idpName = name.getIdp(),
        idpUrl = name.get(Key.URL);
    private Address federationAddress = federationOps.getFederationAddress(name.getFederation());

    @After
    public void after() throws IOException, OperationException {
        cleanupFederation();
    }

    @Test
    public void addFederation() throws Exception {
        Address idpAddress = federationOps.getIdentityProviderAddress(name.getFederation(), idpName);

        WindowFragment window = page.addFederationWindow();
        Editor editor = window.getEditor();
        editor.text(NAME_LABEL, federationName);
        editor.text(IDENTITY_PROVIDER, idpName);
        editor.select(SECURITY_DOMAIN, SD_OTHER);
        editor.text(URL, idpUrl);

        window.clickSave().assertClosed();
        new ResourceVerifier(federationAddress, client).verifyExists();
        new ResourceVerifier(idpAddress, client).verifyExists()
            .verifyAttribute(SECURITY_DOMAIN, SD_OTHER)
            .verifyAttribute(URL, idpUrl);
    }

    @Test
    public void removeFederation() throws Exception {
        federationOps.addFederation(federationName);
        ResourceVerifier verifier = new ResourceVerifier(federationAddress, client);
        verifier.verifyExists();
        page.removeFederation(federationName).assertClosed();
        verifier.verifyDoesNotExist();
    }

    @Override
    protected String getStringSuffix() {
        return "_Fed_TC";
    }

}
