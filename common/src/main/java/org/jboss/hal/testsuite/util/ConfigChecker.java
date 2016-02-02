/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
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

package org.jboss.hal.testsuite.util;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.junit.Assert;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;

/**
 * Abstraction for editing of {@link ConfigFragment} fields and verification of successful save.
 */
public final class ConfigChecker {

    private OnlineManagementClient client;
    private Address resourceAddress;
    private boolean saved;

    /**
     * Edits field identified by <b>{@code attrName}</b> with <b>{@code attrValue}</b> and try to save.
     */
    public static ConfigChecker edit(ConfigFragment config, InputType inputType, String attrName, Object attrValue,
            Address resourceAddress, OnlineManagementClient client) throws IOException, InterruptedException,
            TimeoutException {
        return new ConfigChecker(client, config, inputType, resourceAddress, attrName, attrValue);
    }

    /**
     * Verifies that form switch back to read-only mode.
     * @return {@link ResourceVerifier} to facilitate the subsequent verification compared to model
     */
    public ResourceVerifier verifyFormSaved() throws Exception {
        Assert.assertTrue("Configuration should switch into read-only mode.", saved);
        return new ResourceVerifier(resourceAddress, client);
    }

    /**
     * Verifies that form doesn't switch back to read-only mode (probably due to validation error).
     * @return {@link ResourceVerifier} to facilitate the subsequent verification compared to model
     */
    public ResourceVerifier verifyFormNotSaved() throws Exception {
        Assert.assertFalse("Configuration should NOT switch into read-only mode.", saved);
        return new ResourceVerifier(resourceAddress, client);
    }

    private ConfigChecker(OnlineManagementClient client, ConfigFragment config, InputType inputType,
            Address resourceAddress, String identifier, Object attrValue)
            throws IOException, InterruptedException, TimeoutException {
        this.client = client;
        this.resourceAddress = resourceAddress;
        attrValue = getAttrValue(attrValue, inputType);
        edit(inputType, config, identifier, attrValue);
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

    private void edit(InputType inputType, ConfigFragment config, String identifier, Object attrValue) throws IOException, InterruptedException, TimeoutException {
        switch (inputType) {
            case TEXT:
                saved = config.editTextAndSave(identifier, (String) attrValue); break;
            case CHECKBOX:
                saved = config.editCheckboxAndSave(identifier, (Boolean) attrValue); break;
            case SELECT:
                saved = config.selectOptionAndSave(identifier, (String) attrValue); break;
        }
        if (!saved) {
            config.cancel(); // cleanup
        }
    }

    public enum InputType {
        TEXT, SELECT, CHECKBOX;
    }
}
