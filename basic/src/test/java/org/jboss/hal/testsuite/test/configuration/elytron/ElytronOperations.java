package org.jboss.hal.testsuite.test.configuration.elytron;

import static org.wildfly.extras.creaper.core.online.Constants.EXTENSION;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

/**
 * Encapsulates Creaper operations in Elytron subsystem
 *
 */
public class ElytronOperations {

    private static final String ELYTRON_EXTENSION_NAME = "org.wildfly.extension.elytron";
    private static final Address
        ELYTRON_EXTENSION_ADDRESS = Address.of(EXTENSION, ELYTRON_EXTENSION_NAME),
        ELYTRON_SUBSYSTEM_ADDRESS = Address.subsystem("elytron");
    static final String
        PROVIDER_LOADER = "provider-loader";

    private final Operations ops;
    private final Administration adminOps;

    public ElytronOperations(OnlineManagementClient client) {
        this.ops = new Operations(client);
        this.adminOps = new Administration(client);
    }

    public void enableElytronSubsystem() throws IOException, InterruptedException, TimeoutException {
        ops.add(ELYTRON_EXTENSION_ADDRESS, Values.of("module", ELYTRON_EXTENSION_NAME)).assertSuccess();
        ops.add(ELYTRON_SUBSYSTEM_ADDRESS).assertSuccess();
        adminOps.reloadIfRequired();
    }

    public void disableElytronSubsystem() throws IOException, OperationException, InterruptedException, TimeoutException {
        ops.removeIfExists(ELYTRON_SUBSYSTEM_ADDRESS);
        ops.removeIfExists(ELYTRON_EXTENSION_ADDRESS);
        adminOps.reloadIfRequired();
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
}
