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
import java.util.concurrent.TimeoutException;

import org.jboss.hal.testsuite.test.configuration.picketlink.PicketlinkOperations;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

/**
 * Created by pjelinek on Nov 16, 2015
 */
public final class UtilFedOperations {

    private static final Address SUBSYSTEM_ADDRESS = Address.subsystem("picketlink-federation");

    private Operations ops;

    private PicketlinkOperations picketlinkOps;

    private Administration adminOps;

    public UtilFedOperations(OnlineManagementClient client) {
        this.ops = new Operations(client);
        this.picketlinkOps = new PicketlinkOperations(client);
        this.adminOps = new Administration(client);
    }

    public void addSubsystem() throws IOException, InterruptedException, TimeoutException {
        picketlinkOps.enableExtension(false);
        ops.add(SUBSYSTEM_ADDRESS);
        adminOps.reloadIfRequired();
    }

    public void removeSubsystem() throws IOException, InterruptedException, TimeoutException {
        ops.remove(SUBSYSTEM_ADDRESS);
        picketlinkOps.disableExtension(true);
    }

    public void addFederation(String federationName) throws IOException, OperationException {
        Address federationAddress = getFederationAddress(federationName);
        if (ops.exists(federationAddress)) {
            removeFederation(federationName);
        }
        ops.add(federationAddress).assertSuccess();
    }

    public void addIdp(String federationName, String idpName, String idpUrl, String secDomain)
            throws IOException {
        ops.add(getIdentityProviderAddress(federationName, idpName),
                Values.of(URL, idpUrl).and(SECURITY_DOMAIN, secDomain))
                .assertSuccess();
    }

    public void removeFederation(String federationName) throws IOException, OperationException {
        ops.removeIfExists(getFederationAddress(federationName));
    }

    public void addIdpSAMLHandler(String federationName, String idpName, String handlerName, String className)
            throws IOException {
        ops.add(getIdpSAMLHandlerAddress(federationName, idpName, handlerName),
                Values.of(HANDLER_CLASS_NAME, className))
                .assertSuccess();
    }

    public void addKeyStore(
            String federationName, String file, String pass, String keyAlias, String keyPass) throws IOException {
        ops.add(getKeyStoreAddress(federationName),
                Values.of(KS_FILE, file).and(KS_PASS, pass).and(KS_KEY_ALIAS, keyAlias).and(KS_KEY_PASS, keyPass))
                .assertSuccess();
    }

    public void addTrustDomain(String federationName, String idpName, String domainName) throws IOException {
        ops.add(getIdpTrustDomain(federationName, idpName, domainName));
    }

    public void addSp(String federationName, String spName, String spUrl, String secDomain) throws IOException {
        ops.add(getServiceProviderAddress(federationName, spName),
                Values.of(URL, spUrl).and(SECURITY_DOMAIN, secDomain)).assertSuccess();
    }

    public void addSpSAMLHandler(String federationName, String spName, String handlerName, String className)
            throws IOException {
        ops.add(getSpSAMLHandlerAddress(federationName, spName, handlerName),
                Values.of(HANDLER_CLASS_NAME, className))
                .assertSuccess();
    }

    public ModelNodeResult add(Address address, Values parameters) throws IOException {
        return ops.add(address, parameters);
    }

    public Address getFederationAddress(String federationName) {
        return SUBSYSTEM_ADDRESS.and(FEDERATION, federationName);
    }

    public Address getIdentityProviderAddress(String federationName, String idpName) {
        return getFederationAddress(federationName).and(IDENTITY_PROVIDER, idpName);
    }

    public Address getIdpSAMLHandlerAddress(String federationName, String idpName, String handlerName) {
        return getIdentityProviderAddress(federationName, idpName).and(HANDLER, handlerName);
    }

    public Address getSAMLConfigAddress(String federationName) {
        return getFederationAddress(federationName).and(SAML, SAML);
    }

    public Address getKeyStoreAddress(String federationName) {
        return getFederationAddress(federationName).and(KEY_STORE, KEY_STORE);
    }

    public Address getIdpTrustDomain(String federationName, String idpName, String domainName) {
        return getIdentityProviderAddress(federationName, idpName).and(IDP_TRUST_DOMAIN, domainName);
    }

    public Address getServiceProviderAddress(String federationName, String spName) {
        return getFederationAddress(federationName).and(SERVICE_PROVIDER, spName);
    }

    public Address getSpSAMLHandlerAddress(String federationName, String spName, String handlerName) {
        return getServiceProviderAddress(federationName, spName).and(HANDLER, handlerName);
    }

}
