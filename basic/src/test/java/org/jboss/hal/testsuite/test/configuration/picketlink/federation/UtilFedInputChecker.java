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

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.junit.Assert;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

/**
 * Utility class to make edits of config fields and following verification easier.
 */
public final class UtilFedInputChecker {

    private OnlineManagementClient client;
    private Administration adminOps;
    private InputType inputType;
    private ConfigFragment config;
    private Address resourceAddress;
    private String dmrAttrName;
    private ModelNode attrValue;
    private boolean saved;

    UtilFedInputChecker(OnlineManagementClient client, ConfigFragment config, InputType inputType,
            Address resourceAddress, String dmrAttrName, ModelNode attrValue)
            throws IOException, InterruptedException, TimeoutException {
        this.client = client;
        adminOps = new Administration(client);
        this.config = config;
        this.inputType = inputType;
        this.resourceAddress = resourceAddress;
        this.dmrAttrName = dmrAttrName;
        this.attrValue = attrValue;
        edit();
    }

    private void edit() throws IOException, InterruptedException, TimeoutException {
        switch (inputType) {
            case TEXT:
                saved = config.editTextAndSave(dmrAttrName, attrValue.asString()); break;
            case CHECKBOX:
                saved = config.editCheckboxAndSave(dmrAttrName, attrValue.asBoolean()); break;
            case SELECT:
                saved = config.selectOptionAndSave(dmrAttrName, attrValue.asString()); break;
            default:
                throw new IllegalStateException("Not yet implemented input type " + inputType);
        }
        if (!saved) {
            config.cancel(); // cleanup
        }
        adminOps.reloadIfRequired();
    }

    void andVerifySuccess() throws Exception {
        Assert.assertTrue("Configuration should switch into read-only mode.", saved);
        new ResourceVerifier(resourceAddress, client).verifyAttribute(dmrAttrName, attrValue);
    }

    void andVerifyFailure() throws Exception {
        Assert.assertFalse("Configuration should NOT switch into read-only mode.", saved);
        new ResourceVerifier(resourceAddress, client).verifyAttributeNotEqual(dmrAttrName, attrValue);
    }

    enum InputType {
        TEXT, SELECT, CHECKBOX;
    }

}
