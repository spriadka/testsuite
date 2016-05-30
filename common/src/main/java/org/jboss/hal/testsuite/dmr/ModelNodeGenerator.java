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

package org.jboss.hal.testsuite.dmr;

import java.util.HashMap;
import java.util.Map;

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;

/**
 * Intended to help with creating {@link ModelNode} objects to be used e.g. in {@link ResourceVerifier}
 * Created by pjelinek on May 27, 2016
 */
public class ModelNodeGenerator {

    /**
     * @return {@link ModelNode} of type {@link ModelType} {@code OBJECT} including map of just one property from the parameter
     */
    public ModelNode createObjectNodeWithPropertyChild(String firstChildPropertyKey, String firstChildPropertyValue) {
        Map<String, ModelNode> propertyMap = new HashMap<>(1);
        propertyMap.put(firstChildPropertyKey, new ModelNode(firstChildPropertyValue));
        return createObjectNodeWithPropertyChildren(propertyMap);
    }

    /**
     * @return {@link ModelNode} of type {@link ModelType} {@code OBJECT} including map of properties from the parameter
     */
    public ModelNode createObjectNodeWithPropertyChildren(Map<String, ModelNode> childPropertiesMap) {
        ModelNode parent = new ModelNode();
        childPropertiesMap.forEach((propertyKey, propertyValue) -> parent.get(propertyKey).set(propertyValue));
        return parent;
    }
}
