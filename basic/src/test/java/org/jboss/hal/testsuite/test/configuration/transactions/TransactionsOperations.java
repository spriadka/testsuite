package org.jboss.hal.testsuite.test.configuration.transactions;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.mina.util.AvailablePortFinder;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.DomainManager;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.command.AddSocketBinding;
import org.jboss.hal.testsuite.dmr.AddressTemplate;
import org.jboss.hal.testsuite.dmr.DefaultContext;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.DmrResponse;
import org.jboss.hal.testsuite.dmr.Operation;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.StatementContext;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 15.10.15.
 */
public class TransactionsOperations {

    private static final Logger log = LoggerFactory.getLogger(TransactionsOperations.class);

    private Dispatcher dispatcher;
    private StatementContext context = new DefaultContext();

    private AddressTemplate socketBindingAddressTemplate = AddressTemplate.of("/socket-binding-group=standard-sockets/socket-binding=*");

    public TransactionsOperations(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public String createSocketBinding() throws CommandFailedException, IOException {
        String name = "TransactionsOpsSB_" + RandomStringUtils.randomAlphanumeric(6);
        try (OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient()) {
            int port = AvailablePortFinder.getNextAvailable();
            log.info("Obtained port for socket binding '" + name + "' is " + port);
            client.apply(new AddSocketBinding.Builder(name)
                    .port(port)
                    .build());
        }
        return  name;
    }

    public void removeSocketBinding(String name) {
        ResourceAddress address = socketBindingAddressTemplate.resolve(context, name);
        executeRemoveAction(address);
    }

    private void executeAddAction(ResourceAddress address, Map<String, String> params) {
        Operation.Builder operationBuilder = new Operation.Builder("add", address);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            operationBuilder.param(entry.getKey(), entry.getValue());
        }
        DmrResponse response = dispatcher.execute(operationBuilder.build());
        reloadIfRequiredAndWaitForRunning();
    }

    private void executeAddAction(ResourceAddress address) {
        executeAddAction(address, Collections.emptyMap());
    }

    private void executeRemoveAction(ResourceAddress address) {
        dispatcher.execute(new Operation.Builder("remove", address).build());
        reloadIfRequiredAndWaitForRunning();
    }

    public static void reloadIfRequiredAndWaitForRunning() {
        final int timeout = 60000;
        if (ConfigUtils.isDomain()) {
            new DomainManager(CliClientFactory.getClient()).reloadIfRequiredAndWaitUntilRunning(timeout);
        } else {
            CliClientFactory.getClient().reload(false);
        }
    }

    public static void reloadAndWaitForRunning() {
        final int timeout = 60000;
        if (ConfigUtils.isDomain()) {
            new DomainManager(CliClientFactory.getClient()).reloadAndWaitUntilRunning(timeout);
        } else {
            CliClientFactory.getClient().reload(true);
        }
    }

}
