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

package org.jboss.hal.testsuite.cli;

/**
 * @author rhatlapa (rhatlapa@redhat.com)
 */
public class CliConstants {
    public static final String WEB_SUBSYSTEM_JSP_CONFIGURATION_ADDRESS = "/subsystem=web/configuration=jsp-configuration";
    public static final String DEFAULT_HOST_VIRTUAL_SERVER_ADDRESS = "/subsystem=web/virtual-server=default-host";
    public static final String WEB_CONTAINER_CONFIGURATION_ADDRESS = "/subsystem=web/configuration=container";
    public static final String WEB_CONTAINER_STATIC_RESOURCES_ADDRESS = "/subsystem=web/configuration=static-resources";
    public static final String WEB_SUBSYSTEM_ADDRESS = "/subsystem=web";
    public static final String DATASOURCES_SUBSYSTEM_ADDRESS = "/subsystem=datasources";
    public static final String DATASOURCES_ADDRESS = "/subsystem=datasources/data-source";
    public static final String XA_DATASOURCES_ADDRESS = "/subsystem=datasources/xa-data-source";
    public static final String DEPLOYMENT_ADDRESS = "/deployment";
    public static final String SERVER_GROUP_ADDRESS = "/server-group";

    public static final String UNDEFINED = "undefined";


}
