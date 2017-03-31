package org.jboss.hal.testsuite.test.configuration.remoting.outboundconnections;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.hal.testsuite.creaper.command.AddSocketBinding;
import org.jboss.hal.testsuite.util.AvailablePortFinder;
import org.wildfly.extras.creaper.commands.socketbindings.RemoveSocketBinding;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

class OutboundSocketBindingProvider {

    private final Type type;
    private final String socketBindingReference;
    private final OnlineManagementClient client;

    private final List<Address> referencesToRemove = new LinkedList<>();
    private final List<String> socketBindingsToRemove = new LinkedList<>();

    private OutboundSocketBindingProvider(Builder builder) {
        this.type = builder.type;
        this.socketBindingReference = builder.socketBindingReference;
        this.client = builder.client;
    }

    private Address buildAddress(String socketBindingName) {
        final String socketBindingGroup = client.options().isDomain ? "full-sockets" : "standard-sockets";
        return Address.of("socket-binding-group", socketBindingGroup).and(type.getModelName(), socketBindingName);
    }

    public String createOutboundSocketBinding() throws IOException, CommandFailedException {
        return createOutboundSocketBinding("generated-outbound-socket-binding_" + RandomStringUtils.randomAlphanumeric(7));
    }

    public String createOutboundSocketBinding(String socketBindingName) throws CommandFailedException, IOException {
        String contextSocketBindingReference = socketBindingReference;
        if (contextSocketBindingReference == null) {
            contextSocketBindingReference = "generated-socket-binding-reference_" + RandomStringUtils.randomAlphanumeric(7);
            client.apply(new AddSocketBinding.Builder(contextSocketBindingReference).build());
            socketBindingsToRemove.add(contextSocketBindingReference);
        }
        final Address address = buildAddress(socketBindingName);
        Values values = Values.empty();
        if (type == Type.REMOTE) {
            values = values.and("host", "default").and("port", AvailablePortFinder.getNextAvailableNonPrivilegedPort());
        }
        new Operations(client).add(address, values.and("socket-binding-ref", contextSocketBindingReference)).assertSuccess();
        referencesToRemove.add(address);
        return socketBindingName;
    }

    /**
     * Removes all created socket binding references and outbound socket binding configurations
     */
    public void clean() throws CommandFailedException, IOException, OperationException {
        Operations operations = new Operations(client);
        for (Address reference : referencesToRemove) {
            operations.removeIfExists(reference);
        }
        for (String socketBinding : socketBindingsToRemove) {
            client.apply(new RemoveSocketBinding(socketBinding));
        }
    }

    public enum Type {
        REMOTE("remote-destination-outbound-socket-binding"),
        LOCAL("local-destination-outbound-socket-binding");

        private String modelName;

        Type(String modelName) {
            this.modelName = modelName;
        }

        public String getModelName() {
            return modelName;
        }
    }

    public static final class Builder {

        private Type type;
        private String socketBindingReference;
        private OnlineManagementClient client;

        public Builder(Type type) {
            this.type = type;
        }

        public Builder client(OnlineManagementClient client) {
            this.client = client;
            return this;
        }

        /**
         * Specifies attribute socket-binding-ref for new outbound socket binding. If equal null, new reference is
         * created automagically
         * @param socketBindingReference name of already created socket binding
         */
        public Builder socketBindingReference(String socketBindingReference) {
            this.socketBindingReference = socketBindingReference;
            return this;
        }

        public OutboundSocketBindingProvider build() {
            return new OutboundSocketBindingProvider(this);
        }
    }
}
