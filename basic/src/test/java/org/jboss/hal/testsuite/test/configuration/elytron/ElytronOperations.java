package org.jboss.hal.testsuite.test.configuration.elytron;

import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import java.io.IOException;

/**
 * Encapsulates Creaper operations in Elytron subsystem
 *
 */
public class ElytronOperations {

    private static final Address ELYTRON_SUBSYSTEM_ADDRESS = Address.subsystem("elytron");
    public static final String
        PROVIDER_LOADER = "provider-loader";

    private final Operations ops;

    public ElytronOperations(OnlineManagementClient client) {
        this.ops = new Operations(client);
    }

    public void addProviderLoader(final String providerLoaderName) throws IOException {
        ops.add(ELYTRON_SUBSYSTEM_ADDRESS.and(PROVIDER_LOADER, providerLoaderName)).assertSuccess();
    }

    public void removeProviderLoader(final String providerLoaderName) throws IOException {
        ops.remove(ELYTRON_SUBSYSTEM_ADDRESS.and(PROVIDER_LOADER, providerLoaderName));
    }

    public Address getElytronAddress(final String key, final String value) {
        return ELYTRON_SUBSYSTEM_ADDRESS.and(key, value);
    }

    public static Address getElytronSubsystemAddress() {
        return ELYTRON_SUBSYSTEM_ADDRESS;
    }
}
