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
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

/**
 * Created by pjelinek on Nov 26, 2015
 */
@RunWith(Arquillian.class)
@Category(PicketLink.class)
public class TrustedDomainsTestCase extends AbstractFederationTestCase {

    private final String
        federationName = name.getFederation(),
        idpName = name.getIdp(),
        domainName = "localhost";
    private final Address trustDomainAddress = federationOps.getIdpTrustDomain(federationName, idpName, domainName);

    @Before
    public void before() throws IOException, OperationException {
        setupFederation();
    }

    @After
    public void after() throws IOException, OperationException {
        cleanupFederation();
    }

    @Test
    public void addDomain() throws Exception {
        page.navigateToFederation(federationName).switchSubTab(TRUSTED_DOMAINS_LABEL);
        WizardWindow wizard = page.getResourceManager().addResource();
        Editor editor = wizard.getEditor();

        editor.text(NAME_LABEL, "");
        wizard.assertFinish(false);

        editor.text(NAME_LABEL, domainName);
        wizard.assertFinish(true);

        new ResourceVerifier(trustDomainAddress, client).verifyExists();
    }

    @Test
    public void removeDomain() throws Exception {
        ResourceVerifier verifier = new ResourceVerifier(trustDomainAddress, client);
        federationOps.addTrustDomain(federationName, idpName, domainName);
        page.navigateToFederation(federationName).switchSubTab(TRUSTED_DOMAINS_LABEL);
        verifier.verifyExists();
        page.getResourceManager().removeResource(domainName).confirm().assertClosed();
        verifier.verifyDoesNotExist();
    }

    @Override
    protected String getStringSuffix() {
        return "_TD_TC";
    }
}
