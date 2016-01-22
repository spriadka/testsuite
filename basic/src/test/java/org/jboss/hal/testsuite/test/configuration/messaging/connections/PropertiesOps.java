package org.jboss.hal.testsuite.test.configuration.messaging.connections;

import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import java.io.IOException;

public class PropertiesOps {


    protected static boolean isPropertyPresentInParams(Address address, OnlineManagementClient client, String key) throws IOException {
        Operations operations = new Operations(client);
        ModelNode result = operations.readAttribute(address, "params").value();
        return result.isDefined() && result.keys().contains(key);
    }

    protected static void addProperty(Address address, OnlineManagementClient client, String propKey, String propValue) throws IOException {
        Operations operations = new Operations(client);
        operations.writeAttribute(address, "params." + propKey, propValue);
    }

}
