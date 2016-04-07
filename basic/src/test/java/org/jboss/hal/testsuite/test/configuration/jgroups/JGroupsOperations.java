package org.jboss.hal.testsuite.test.configuration.jgroups;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.hal.testsuite.creaper.command.AddSocketBinding;
import org.jboss.hal.testsuite.util.AvailablePortFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;

class JGroupsOperations {

    private Logger logger = LoggerFactory.getLogger(JGroupsOperations.class);

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

    public boolean propertyExists(Address address, String name, String value) throws IOException {
        ModelNode expectedProperty = new ModelNode().set(new Property(name, new ModelNode(value)));
        ModelNodeResult result = operations.readAttribute(address, "properties");
        return result.hasDefinedValue() && result.value().asList().contains(expectedProperty);
    }

    public String createSocketBinding(String name) throws IOException, CommandFailedException {
        //editing on full-ha profile in domain
        String socketBindingGroup = client.options().isDomain ? "full-ha-sockets" : "standard-sockets";
        int port = AvailablePortFinder.getNextAvailableNonPrivilegedPort();
        logger.info("Obtained port for socket binding '" + name + "' is " + port);
        client.apply(new AddSocketBinding.Builder(name, socketBindingGroup)
                .port(port)
                .build());
        return name;
    }

    public String createSocketBinding() throws IOException, CommandFailedException {
        return createSocketBinding("JGroupsSocketBinding_" + RandomStringUtils.randomAlphanumeric(6));
    }
}
