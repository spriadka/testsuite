package org.jboss.hal.testsuite.test.configuration.mail;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.hal.testsuite.creaper.command.AddSocketBinding;
import org.jboss.hal.testsuite.util.AvailablePortFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;

import java.io.IOException;

class MailSubsystemOperations {

    private static final Logger log = LoggerFactory.getLogger(MailSubsystemOperations.class);

    private final OnlineManagementClient client;

    MailSubsystemOperations(OnlineManagementClient client) {
        this.client = client;
    }

    String createSocketBinding() throws IOException, CommandFailedException {
        String name = "mailTestCaseSb_" + RandomStringUtils.randomAlphanumeric(6);
        int port = AvailablePortFinder.getNextAvailableNonPrivilegedPort();
        log.info("Obtained port for socket binding '" + name + "' is " + port);
        client.apply(new AddSocketBinding.Builder(name)
                .port(port)
                .build());
        return name;
    }

}
