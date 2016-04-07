package org.jboss.hal.testsuite.test.configuration.transactions;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.hal.testsuite.creaper.command.AddSocketBinding;
import org.jboss.hal.testsuite.util.AvailablePortFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;

import java.io.IOException;

public class TransactionsOperations {

    private static final Logger log = LoggerFactory.getLogger(TransactionsOperations.class);

    private OnlineManagementClient client;

    public TransactionsOperations(OnlineManagementClient client) {
        this.client = client;
    }

    public String createSocketBinding() throws CommandFailedException, IOException {
        String name = "TransactionsOpsSB_" + RandomStringUtils.randomAlphanumeric(6);
        int port = AvailablePortFinder.getNextAvailableUserPort();
        log.info("Obtained port for socket binding '" + name + "' is " + port);
        client.apply(new AddSocketBinding.Builder(name)
                .port(port)
                .build());
        return  name;
    }
}
