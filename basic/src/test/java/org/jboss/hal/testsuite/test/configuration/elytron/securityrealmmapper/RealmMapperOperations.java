package org.jboss.hal.testsuite.test.configuration.elytron.securityrealmmapper;


import org.apache.commons.lang.RandomStringUtils;
import org.jboss.hal.testsuite.test.configuration.elytron.ElytronOperations;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;

/**
 * Class containing helper methods for security realm mapper tests.
 */
class RealmMapperOperations {

    private final Operations ops;
    private final ElytronOperations elyOps;

    private static final String
            REALM_NAME = "realm-name",
            IDENTITY = "identity",
            IDENTITY_REALM = "identity-realm",
            CONSTANT_REALM_MAPPER = "constant-realm-mapper";

    RealmMapperOperations(OnlineManagementClient client) {
        this.ops = new Operations(client);
        this.elyOps = new ElytronOperations(client);
    }

    Address createConstantRealmMapper(Address realmAddress) throws IOException {
        final Address realmMapperAddress = elyOps.getElytronAddress(CONSTANT_REALM_MAPPER, RandomStringUtils.randomAlphanumeric(7));
        ops.add(realmMapperAddress, Values.of(REALM_NAME, realmAddress.getLastPairValue())).assertSuccess();
        return realmMapperAddress;
    }

    Address createIdentityRealm() throws IOException {
        final Address realmAddress = elyOps.getElytronAddress(IDENTITY_REALM, RandomStringUtils.randomAlphabetic(7));
        ops.add(realmAddress, Values.of(IDENTITY, RandomStringUtils.randomAlphanumeric(7))).assertSuccess();
        return realmAddress;
    }
}
