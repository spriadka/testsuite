package org.jboss.hal.testsuite.test.configuration.jgroups;

import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;

public class JGroupsOperations {

    private OnlineManagementClient client;
    private Operations operations;

    public JGroupsOperations(OnlineManagementClient client) {
        this.client = client;
        this.operations = new Operations(client);
    }

    public boolean addProperty(Address address, String name, String value) throws IOException {
        ModelNodeResult result = operations.add(address.and("property", name), Values.of("value", value));
        return result.isSuccess();
    }

    public void removeProperty(Address address, String name) throws IOException, OperationException {
        try {
            operations.removeIfExists(address.and("property", name));
        } catch (OperationException ignored) {
            //operation fails if resource does not exists
        }
    }

    public boolean propertyExists(Address address, String name) throws IOException {
        ModelNodeResult result = operations.readAttribute(address, "properties");
        return result.hasDefinedValue() && result.value().keys().contains(name);
    }
}
