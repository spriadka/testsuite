package org.jboss.hal.testsuite.test.configuration.webservices;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.page.config.WebServicesPage;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;


public class WebServicesTestCaseAbstract {
    protected static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();

    protected static final Administration administration = new Administration(client);

    protected static final Operations operations = new Operations(client);

    protected static final Address WEBSERVICES_ADDRESS = Address.subsystem("webservices");

    protected static final String CLIENT_CONFIGURATION = "client-config";
    protected static final String ENDPOINT_CONFIGURATION = "endpoint-config";
    protected static final String PRE_HANDLER_CHAIN = "pre-handler-chain";
    protected static final String POST_HANDLER_CHAIN = "post-handler-chain";
    protected static final String HANDLER = "handler";
    protected static final String PROPERTY = "property";
    protected static final String PROTOCOL_BINDINGS = "protocol-bindings";
    protected static final String PRE_HANDLER_CHAIN_TAB_LABEL = "Pre Handler Chain";
    protected static final String POST_HANDLER_CHAIN_TAB_LABEL = "Post Handler Chain";

    protected static final String HANDLER_CLASSES_TAB_LABEL = "Handler classes";

    @Page
    protected WebServicesPage page;

    @Drone
    protected WebDriver browser;
}
