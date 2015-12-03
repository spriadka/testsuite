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

import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.junit.Assert;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;

/**
 * Utility class to make edits of config fields and following verification easier.
 */
public final class UtilFedInputChecker {

    private OnlineManagementClient client;
    private Address resourceAddress;
    private boolean saved;

    UtilFedInputChecker(OnlineManagementClient client, ConfigFragment config, InputType inputType,
            Address resourceAddress, String dmrAttrName, Object attrValue)
            throws IOException, InterruptedException, TimeoutException {
        this.client = client;
        this.resourceAddress = resourceAddress;
        attrValue = getAttrValue(attrValue, inputType);
        edit(inputType, config, dmrAttrName, attrValue);
    }

    private Object getAttrValue(Object attrValue, InputType inputType) {
        switch (inputType) {
            case TEXT: case SELECT:
                if (attrValue instanceof String) {
                    return attrValue;
                } else if (attrValue instanceof Long) {
                    return String.valueOf((long) attrValue);
                } else if (attrValue instanceof Integer) {
                    return String.valueOf((int) attrValue);
                } else {
                    throw new IllegalArgumentException(attrValue + " should be String, Integer or Long!");
                }
            case CHECKBOX:
                if (attrValue instanceof Boolean) {
                    return attrValue;
                } else {
                    throw new IllegalArgumentException(attrValue + " should be Boolean!");
                }
            default:
                throw new IllegalArgumentException("Not yet supported inputType: " + inputType);
        }
    }

    private void edit(InputType inputType, ConfigFragment config, String dmrAttrName, Object attrValue) throws IOException, InterruptedException, TimeoutException {
        switch (inputType) {
            case TEXT:
                saved = config.editTextAndSave(dmrAttrName, (String) attrValue); break;
            case CHECKBOX:
                saved = config.editCheckboxAndSave(dmrAttrName, (Boolean) attrValue); break;
            case SELECT:
                saved = config.selectOptionAndSave(dmrAttrName, (String) attrValue); break;
        }
        if (!saved) {
            config.cancel(); // cleanup
        }
    }

    ResourceVerifier verifyFormSaved() throws Exception {
        Assert.assertTrue("Configuration should switch into read-only mode.", saved);
        return new ResourceVerifier(resourceAddress, client);
    }

    ResourceVerifier verifyFormNotSaved() throws Exception {
        Assert.assertFalse("Configuration should NOT switch into read-only mode.", saved);
        return new ResourceVerifier(resourceAddress, client);
    }

    enum InputType {
        TEXT, SELECT, CHECKBOX;
    }

}
