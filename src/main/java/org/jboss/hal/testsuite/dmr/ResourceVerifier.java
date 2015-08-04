/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.hal.testsuite.dmr;

import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Harald Pehl
 */
public class ResourceVerifier {

    private final Dispatcher dispatcher;

    public ResourceVerifier(final Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void verifyResource(ResourceAddress address) {
        verifyResource(address, true);
    }

    public void verifyResource(ResourceAddress address, int timeout) {
        verifyResource(address, true, timeout);
    }

    public void verifyResource(ResourceAddress address, boolean expected) {
        verifyResource(address, expected, 0);
    }

    public void verifyResource(ResourceAddress address, boolean expected, int timeout) {
        Operation operation = new Operation.Builder(ModelDescriptionConstants.READ_RESOURCE_OPERATION, address).withTimeout(timeout).build();
        assertEquals(expected, dispatcher.execute(operation).isSuccessful());
    }

    public void verifyAttribute(ResourceAddress address, String attribute, boolean expected) {
        verifyAttribute(address, attribute, expected, 0);
    }

    public void verifyAttribute(ResourceAddress address, String attribute, boolean expected, int timeout) {
        DmrResponse response = dispatcher.execute(readAttributeOperation(address, attribute, timeout));
        assertTrue(response.isSuccessful());
        assertEquals(expected, response.payload().asBoolean());
    }

    public void verifyAttribute(ResourceAddress address, String attribute, String[] expected) {
        verifyAttribute(address, attribute, expected, 0);
    }

    public void verifyAttribute(ResourceAddress address, String attribute, String[] expected, int timeout) {
        DmrResponse response = dispatcher.execute(readAttributeOperation(address, attribute, timeout));
        assertTrue(response.isSuccessful());
        String[] values = response.payload().asList().stream().map(ModelNode::asString).toArray(String[]::new);
        assertArrayEquals(expected, values);
    }

    public void verifyAttribute(ResourceAddress address, String attribute, String expected) {
        verifyAttribute(address, attribute, expected, 0);
    }

    public void verifyAttribute(ResourceAddress address, String attribute, String expected, int timeout) {
        DmrResponse response = dispatcher.execute(readAttributeOperation(address, attribute, timeout));
        assertTrue(response.isSuccessful());
        assertEquals(expected, response.payload().asString());
    }

    private Operation readAttributeOperation(ResourceAddress address, String attribute, int timeout) {
        return new Operation.Builder(ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION, address)
                .param(ModelDescriptionConstants.NAME, attribute)
                .withTimeout(timeout).build();
    }
}
