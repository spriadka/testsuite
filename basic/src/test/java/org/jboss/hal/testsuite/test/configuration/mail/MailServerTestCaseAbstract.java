package org.jboss.hal.testsuite.test.configuration.mail;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.page.config.MailSessionsPage;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public abstract class MailServerTestCaseAbstract {


    protected static final String MAIL_SESSION_NAME = "java:/mail_" + RandomStringUtils.randomAlphanumeric(5);
    protected static final Address MAIL_SESSION_ADDRESS = Address.subsystem("mail").and("mail-session", MAIL_SESSION_NAME);

    protected static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    protected static final Administration administration = new Administration(client);
    protected static final Operations operations = new Operations(client);

    protected static final MailSubsystemOperations mailOperations = new MailSubsystemOperations(client);

    protected static String SOCKET_BINDING;

    protected static final String OUTBOUND_SOCKET_BINDING_REFERENCE = "outbound-socket-binding-ref";
    protected static final String USERNAME_ATTRIBUTE = "username";
    protected static final String PASSWORD_ATTRIBUTE = "password";
    protected static final String SSL_ATTRIBUTE = "ssl";
    protected static final String TLS_ATTRIBUTE = "tls";

    @Drone
    public WebDriver browser;

    @Page
    public MailSessionsPage page;

    protected void createMailServerInstanceInModel(Address mailServerAddress, String socketBinding) throws InterruptedException, TimeoutException, IOException {
        operations.add(mailServerAddress, Values.of(OUTBOUND_SOCKET_BINDING_REFERENCE, socketBinding));
        administration.reloadIfRequired();
    }

}
