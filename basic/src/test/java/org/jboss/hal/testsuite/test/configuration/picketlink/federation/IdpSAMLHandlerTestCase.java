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

import java.io.IOException;

import static org.jboss.hal.testsuite.test.configuration.picketlink.federation.UtilFedName.HANDLER_CLASS_NAME;
import static org.jboss.hal.testsuite.test.configuration.picketlink.federation.UtilFedName.Key;
import static org.jboss.hal.testsuite.test.configuration.picketlink.federation.UtilFedName.NAME_LABEL;
import static org.jboss.hal.testsuite.test.configuration.picketlink.federation.UtilFedName.SAML_HANDLERS_LABEL;

/**
 * Created by pjelinek on Nov 23, 2015
 */
@RunWith(Arquillian.class)
@Category(PicketLink.class)
public class IdpSAMLHandlerTestCase extends AbstractFederationTestCase {

    private String
        federationName = name.getFederation(),
        idpName = name.getIdp(),
        handlerName = name.get(Key.HANDLER),
        className = name.get(Key.CLASS_NAME);
    private Address handlerAddress = federationOps.getIdpSAMLHandlerAddress(federationName, idpName, handlerName);
    private ResourceVerifier resourceVerifier = new ResourceVerifier(handlerAddress, client);

    @Before
    public void before() throws IOException, OperationException {
        setupFederation();
    }

    @After
    public void after() throws IOException, OperationException {
        cleanupFederation();
    }

    @Test
    public void addHandler() throws Exception {
        page.navigateToFederation(federationName).switchSubTab(SAML_HANDLERS_LABEL);
        WizardWindow wizard = page.getResourceManager().addResource();
        Editor editor = wizard.getEditor();
        editor.text(NAME_LABEL, handlerName);
        editor.text(HANDLER_CLASS_NAME, className);
        wizard.clickSave().assertClosed();

        resourceVerifier.verifyExists()
            .verifyAttribute(HANDLER_CLASS_NAME, className, FAIL_MESSAGE_HAL_1194);
    }

    @Test
    public void addInvalidHandler() throws Exception {
        page.navigateToFederation(federationName).switchSubTab(SAML_HANDLERS_LABEL);
        WizardWindow wizard = page.getResourceManager().addResource();
        Editor editor = wizard.getEditor();
        editor.text(NAME_LABEL, handlerName);
        editor.text(HANDLER_CLASS_NAME, ""); // invalid, either class-name or code has to be provided
        wizard.clickSave();

        resourceVerifier.verifyDoesNotExist(FAIL_MESSAGE_HAL_1194);
    }

    @Test
    public void removeHandler() throws Exception {
        federationOps.addIdpSAMLHandler(federationName, idpName, handlerName, className);
        page.navigateToFederation(federationName).switchSubTab(SAML_HANDLERS_LABEL);

        resourceVerifier.verifyExists();
        page.getResourceManager().removeResource(handlerName).confirm().assertClosed();
        resourceVerifier.verifyDoesNotExist();
    }

    @Override
    protected String getStringSuffix() {
        return "_SAML_HANDLER_TC";
    }

}
