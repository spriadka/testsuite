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
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.jboss.hal.testsuite.test.configuration.picketlink.federation.UtilFedInputChecker.InputType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

/**
 * Created by pjelinek on Nov 25, 2015
 */
@RunWith(Arquillian.class)
@Category(PicketLink.class)
public class KeyStoreTestCase extends AbstractFederationTestCase {

    private final String
        fedNameValue = name.getFederation(),
        fileNameValue = "fileName",
        passValue = "pass",
        keyAliasValue = "keyAlias",
        keyPassValue = "keyPass",
        nameValue = "local",
        hostValue = "localhost";
    private final Address
        keyStoreAddress = federationOps.getKeyStoreAddress(name.getFederation()),
        hostKeyAddress = keyStoreAddress.and("key", nameValue);

    @Before
    public void before() throws IOException, OperationException {
        setupFederation();
    }

    @After
    public void after() throws IOException, OperationException {
        cleanupFederation();
    }

    @Test
    public void editKeyStore() throws Exception {
        final String
            newPassValue = "newPass",
            relativeToValue = "user.home";
        page.navigateToFederation(fedNameValue).switchSubTab(KEY_STORE_LABEL);
        ConfigFragment config = page.getConfigFragment();

        Editor editor = config.edit();
        editor.text(KS_FILE, fileNameValue);
        editor.text(KS_PASS, passValue);
        editor.text(KS_RELATIVE_TO, relativeToValue);
        editor.text(KS_KEY_ALIAS, keyAliasValue);
        editor.text(KS_KEY_PASS, keyPassValue);
        config.save();

        new ResourceVerifier(keyStoreAddress, client).verifyExists()
            .verifyAttribute(KS_FILE, fileNameValue)
            .verifyAttribute(KS_PASS, passValue)
            .verifyAttribute(KS_RELATIVE_TO, relativeToValue)
            .verifyAttribute(KS_KEY_ALIAS, keyAliasValue)
            .verifyAttribute(KS_KEY_PASS, keyPassValue);

        edit(InputType.TEXT, keyStoreAddress, KS_PASS, newPassValue)
            .verifyFormSaved()
            .verifyAttribute(KS_PASS, newPassValue);
    }

    @Test
    public void addHostKey() throws Exception {
        addKeyStoreByCLI();
        page.navigateToFederation(fedNameValue).switchSubTab(KEY_STORE_LABEL);
        page.switchConfigAreaTabTo(HOST_KEYS_LABEL);

        WizardWindow wizard = page.getResourceManager().addResource();
        Editor editor = wizard.getEditor();
        editor.text(NAME_LABEL, "");
        editor.text(KS_HK_HOST, hostValue);
        wizard.assertFinish(false);

        editor.text(NAME_LABEL, nameValue);
        editor.text(KS_HK_HOST, "");
        wizard.assertFinish(false);

        editor.text(NAME_LABEL, nameValue);
        editor.text(KS_HK_HOST, hostValue);
        wizard.assertFinish(true);

        new ResourceVerifier(hostKeyAddress, client).verifyExists()
            .verifyAttribute(KS_HK_HOST, hostValue);
    }

    @Test
    public void removeHostKey() throws Exception {
        ResourceVerifier verifier = new ResourceVerifier(hostKeyAddress, client);
        addKeyStoreByCLI();
        federationOps.add(hostKeyAddress, Values.of(KS_HK_HOST, hostValue)).assertSuccess();
        page.navigateToFederation(fedNameValue).switchSubTab(KEY_STORE_LABEL);
        page.switchConfigAreaTabTo(HOST_KEYS_LABEL);
        verifier.verifyExists();
        page.getResourceManager().removeResource(nameValue).confirm().assertClosed();
        verifier.verifyDoesNotExist();
    }

    @Override
    protected String getStringSuffix() {
        return "_KS_TC";
    }

    private void addKeyStoreByCLI() throws IOException {
        federationOps.addKeyStore(fedNameValue, fileNameValue, passValue, keyAliasValue, keyPassValue);
    }

}
