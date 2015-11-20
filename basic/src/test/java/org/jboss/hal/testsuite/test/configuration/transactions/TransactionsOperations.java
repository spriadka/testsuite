package org.jboss.hal.testsuite.test.configuration.transactions;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.DomainManager;
import org.jboss.hal.testsuite.dmr.AddressTemplate;
import org.jboss.hal.testsuite.dmr.DefaultContext;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.DmrResponse;
import org.jboss.hal.testsuite.dmr.Operation;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.StatementContext;
import org.jboss.hal.testsuite.util.ConfigUtils;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 15.10.15.
 */
public class TransactionsOperations {

    private Dispatcher dispatcher;
    private StatementContext context = new DefaultContext();

    private AddressTemplate socketBindingAddressTemplate = AddressTemplate.of("/socket-binding-group=standard-sockets/socket-binding=*");

    public TransactionsOperations(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public String createSocketBinding() {
        String name = "TransactionsOpsSB_" + RandomStringUtils.randomAlphanumeric(6);
        ResourceAddress address = socketBindingAddressTemplate.resolve(context, name);
        Map<String, String> params = ImmutableMap.of("port", String.valueOf(ThreadLocalRandom.current().nextInt(1000, 9999)));
        executeAddAction(address, params);
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
