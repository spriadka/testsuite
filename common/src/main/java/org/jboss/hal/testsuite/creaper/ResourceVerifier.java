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

package org.jboss.hal.testsuite.creaper;

import java.io.IOException;

import org.jboss.dmr.ModelNode;
import org.junit.Assert;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;

/**
 * Helper class to verify resource existence and attribute values in model.
 * Created by pjelinek on Nov 16, 2015
 */
public class ResourceVerifier {

    private Address resourceAddress;
    private Operations ops;

    public ResourceVerifier(Address resourceAddress, OnlineManagementClient client) {
        this.resourceAddress = resourceAddress;
        this.ops = new Operations(client);
    }

    /**
     * Verifies resource exists in model.
     * @param errorMessageSuffix is intended to be used for e.g. passing related tracked issue.
     */
    public ResourceVerifier verifyExists(String errorMessageSuffix) throws IOException, OperationException {
        Assert.assertTrue("Resource '" + resourceAddress + "' should exist!" + errorMessageSuffix,
                ops.exists(resourceAddress));
        return this;
    }

    /**
     * Verifies resource exists in model.
     */
    public ResourceVerifier verifyExists() throws IOException, OperationException {
        return verifyExists("");
    }

    /**
     * Verifies resource doesn't exist in model.
     * @param errorMessageSuffix is intended to be used for e.g. passing related tracked issue.
     */
    public ResourceVerifier verifyDoesNotExist(String errorMessageSuffix) throws IOException, OperationException {
        Assert.assertFalse("Resource '" + resourceAddress + "' should NOT exist! " + errorMessageSuffix,
                ops.exists(resourceAddress));
        return this;
    }

    /**
     * Verifies resource doesn't exist in model.
     */
    public ResourceVerifier verifyDoesNotExist() throws IOException, OperationException {
        return verifyDoesNotExist("");
    }

    /**
     * Verifies the value of attribute in model.
     * @param errorMessageSuffix is intended to be used for e.g. passing related tracked issue.
     */
    public ResourceVerifier verifyAttribute(
            String attributeName, final ModelNode expectedValue, String errorMessageSuffix) throws IOException {
        ModelNodeResult actualResult = ops.readAttribute(resourceAddress, attributeName);
        actualResult.assertDefinedValue();
        Assert.assertEquals("Attribute value is different in model!" + errorMessageSuffix,
                expectedValue, actualResult.value());
        return this;
    }

    /**
     * Verifies the value of attribute in model.
     */
    public ResourceVerifier verifyAttribute(String attributeName, final ModelNode expectedValue) throws IOException {
        return verifyAttribute(attributeName, expectedValue, "");
    }

    /**
     * Verifies the value of attribute in model.
     * @param errorMessageSuffix is intended to be used for e.g. passing related tracked issue.
     */
    public ResourceVerifier verifyAttribute(
            String attributeName, String expectedValue, String errorMessageSuffix) throws IOException {
        return verifyAttribute(attributeName, new ModelNode(expectedValue), errorMessageSuffix);
    }

    /**
     * Verifies the value of attribute in model.
     */
    public ResourceVerifier verifyAttribute(String attributeName, String expectedValue) throws IOException {
        return verifyAttribute(attributeName, new ModelNode(expectedValue));
    }

    /**
     * Verifies the value of attribute in model.
     * @param errorMessageSuffix is intended to be used for e.g. passing related tracked issue.
     */
    public ResourceVerifier verifyAttribute(
            String attributeName, boolean expectedValue, String errorMessageSuffix) throws IOException {
        return verifyAttribute(attributeName, new ModelNode(expectedValue), errorMessageSuffix);
    }

    /**
     * Verifies the value of attribute in model.
     */
    public ResourceVerifier verifyAttribute(String attributeName, boolean expectedValue) throws IOException {
        return verifyAttribute(attributeName, new ModelNode(expectedValue));
    }

    /**
     * Verifies the value of attribute in model.
     * @param errorMessageSuffix is intended to be used for e.g. passing related tracked issue.
     */
    public ResourceVerifier verifyAttribute(
            String attributeName, int expectedValue, String errorMessageSuffix) throws IOException {
        return verifyAttribute(attributeName, new ModelNode(expectedValue), errorMessageSuffix);
    }

    /**
     * Verifies the value of attribute in model.
     */
    public ResourceVerifier verifyAttribute(String attributeName, int expectedValue) throws IOException {
        return verifyAttribute(attributeName, new ModelNode(expectedValue));
    }

    /**
     * Verifies the value of attribute in model.
     * @param errorMessageSuffix is intended to be used for e.g. passing related tracked issue.
     */
    public ResourceVerifier verifyAttribute(
            String attributeName, long expectedValue, String errorMessageSuffix) throws IOException {
        return verifyAttribute(attributeName, new ModelNode(expectedValue), errorMessageSuffix);
    }

    /**
     * Verifies the value of attribute in model.
     */
    public ResourceVerifier verifyAttribute(String attributeName, long expectedValue) throws IOException {
        return verifyAttribute(attributeName, new ModelNode(expectedValue));
    }

    /**
     * @throws AssertionError if resource exists and has attribute with attributeName
     * with value equal to notExpectedValue.
     */
    public ResourceVerifier verifyAttributeNotEqual(String attributeName, ModelNode notExpectedValue)
            throws IOException {
        ModelNodeResult actualResult = ops.readAttribute(resourceAddress, attributeName);
        Assert.assertFalse(attributeName + " should not have value " + notExpectedValue,
                actualResult.isSuccess()
                && actualResult.hasDefinedValue()
                && actualResult.value().equals(notExpectedValue));
        return this;
    }

    /**
     * Verifies the value of attribute in model is undefined.
     */
    public ResourceVerifier verifyAttributeIsUndefined(String attributeName) throws IOException {
        ModelNodeResult actualResult = ops.readAttribute(resourceAddress, attributeName);
        actualResult.assertNotDefinedValue();
        return this;
    }

}
