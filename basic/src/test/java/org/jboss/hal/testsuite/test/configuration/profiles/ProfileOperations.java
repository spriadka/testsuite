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

package org.jboss.hal.testsuite.test.configuration.profiles;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

import java.util.Arrays;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.dmr.AddressTemplate;
import org.jboss.hal.testsuite.dmr.DefaultContext;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.Operation;
import org.jboss.hal.testsuite.dmr.StatementContext;

/**
 * This class is intended to provide model management operations on profiles and their subsystems.
 */
public class ProfileOperations {

    private static final AddressTemplate profileTemplate = AddressTemplate.of("/profile=*");
    private static final AddressTemplate subsystemTemplate = AddressTemplate.of("/profile=*/subsystem=*");

    private final Dispatcher dispatcher;
    private final StatementContext ctx = new DefaultContext();

    public ProfileOperations(Dispatcher dispatcher){
        this.dispatcher = dispatcher;
    }

    public void addProfileWithSubsystem(String profileName, String subsystemName){
        dispatcher.execute(new Operation.Builder(
            ADD,
            profileTemplate.resolve(ctx, profileName))
            .build()
        );
        addSubsystem(profileName, subsystemName);
    }

    public void addSubsystem(String profileName, String subsystemName){
        dispatcher.execute(new Operation.Builder(
                ADD,
                subsystemTemplate.resolve(ctx, profileName, subsystemName))
                .build()
            );
    }

    public void addComposedProfile(String profileName, String momName, String dadName){
        List<ModelNode> parentNodes = Arrays.asList(new ModelNode[]{
            new ModelNode(dadName),
            new ModelNode(momName)
        });
        dispatcher.execute(new Operation.Builder(
            ADD,
            profileTemplate.resolve(ctx, profileName))
            .param(INCLUDES, parentNodes)
            .build()
        );
    }

    public void removeProfile(String profileName){
        dispatcher.execute(new Operation.Builder(
            REMOVE,
            profileTemplate.resolve(ctx, profileName))
            .build()
        );
    }

    public void removeProfileFromIncludes(String parentProfileName, String childProfileName){
        dispatcher.execute(new Operation.Builder(
            "list-remove",
            profileTemplate.resolve(ctx, childProfileName))
            .param(NAME, INCLUDES)
            .param(VALUE, parentProfileName)
            .build()
        );
    }
}
