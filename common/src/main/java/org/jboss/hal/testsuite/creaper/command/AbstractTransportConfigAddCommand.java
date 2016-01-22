package org.jboss.hal.testsuite.creaper.command;

import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * An abstract class which implements common properties of connector and acceptor in messaging connections.
 */
abstract class AbstractTransportConfigAddCommand implements OnlineCommand {

    protected TransportConfigType type;
    protected String name;
    protected String serverName;
    protected Map<String, String> params;
    protected boolean replaceExisting;

    //in-vm
    protected Integer serverId;

    //generic and remote
    protected String socketBinding;

    //remote
    protected String factoryClass;

    protected AbstractTransportConfigAddCommand(InVmBuilder builder) {
        this.type = TransportConfigType.IN_VM;
        this.serverId = builder.serverId;
        initCommonOptions(builder);
    }

    protected AbstractTransportConfigAddCommand(GenericBuilder builder) {
        this.type = TransportConfigType.GENERIC;
        this.factoryClass = builder.factoryClass;
        this.socketBinding = builder.socketBinding;
        initCommonOptions(builder);
    }

    protected AbstractTransportConfigAddCommand(RemoteBuilder builder) {
        this.type = TransportConfigType.REMOTE;
        this.socketBinding = builder.socketBinding;
        initCommonOptions(builder);
    }

    private void initCommonOptions(Builder builder) {
        this.name = builder.name;
        this.serverName = builder.serverName;
        this.params = builder.params;
        this.replaceExisting = builder.replaceExisting;
    }

    /**
     * Get type of transport configuration's instance
     */
    protected abstract TransportConfigItem getConfigType();

    private Address getAddress(OnlineManagementClient client) throws CommandFailedException {
        String serverName = this.serverName;
        if (serverName == null) {
            serverName = MessagingUtils.DEFAULT_SERVER_NAME;
        }
        return MessagingUtils
                .address(client, serverName)
                .and(type.getPrefix() + getConfigType().getName(), name);
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws CommandFailedException, IOException {
        Address address = getAddress(ctx.client);
        Operations operations = new Operations(ctx.client);

        if (replaceExisting) {
            try {
                operations.removeIfExists(address);
            } catch (OperationException e) {
                throw new CommandFailedException("Failed to remove previously defined " + getConfigType().getName() + "!");
            }
        }

        Values params = Values.empty().andObjectOptional("params", Values.fromMap(this.params));

        switch (this.type) {
            case GENERIC:
                params = params.and("factory-class", factoryClass)
                        .and("socket-binding", socketBinding);
                break;
            case IN_VM:
                params = params.and("server-id", serverId);
                break;
            case REMOTE:
                params = params.and("socket-binding", socketBinding);
                break;
            default:
                break;
        }

        operations.add(address, params);
    }

    private abstract static class Builder<THIS extends AbstractTransportConfigAddCommand.Builder<THIS>> {
        private String name;
        private String serverName;
        private Map<String, String> params;
        private boolean replaceExisting;

        private Builder(String name) {
            this(name, null);
        }

        private Builder(String name, String serverName) {
            if (name == null) {
                throw new IllegalArgumentException("Name has to be defined!");
            }
            this.name = name;
            this.serverName = serverName;
        }

        public final THIS param(String key, String value) {
            if (params == null) {
                params = new HashMap<String, String>();
            }
            params.put(key, value);
            return (THIS) this;
        }

        public final THIS replaceExisting() {
            this.replaceExisting = true;
            return (THIS) this;
        }

        public abstract AbstractTransportConfigAddCommand build();
    }

    /**
     * Builder which builds transport configuration item with type 'In-VM'
     */
    protected abstract static class InVmBuilder<THIS extends InVmBuilder<THIS>> extends Builder<THIS> {
        private Integer serverId;

        protected InVmBuilder(String name) {
            super(name);
        }

        protected InVmBuilder(String name, String serverName) {
            super(name, serverName);
        }

        public THIS serverId(Integer serverId) {
            this.serverId = serverId;
            return (THIS) this;
        }

        public THIS validate() {
            if (serverId == null) {
                throw new IllegalArgumentException("Server id has to be set!");
            }
            return (THIS) this;
        }
    }

    /**
     * Builder which builds transport configuration item with type 'Generic'
     */
    protected abstract static class GenericBuilder<THIS extends GenericBuilder<THIS>> extends Builder<THIS>  {
        private String socketBinding;
        private String factoryClass;

        protected GenericBuilder(String name) {
            super(name);
        }

        protected GenericBuilder(String name, String serverName) {
            super(name, serverName);
        }

        public THIS socketBinding(String socketBinding) {
            this.socketBinding = socketBinding;
            return (THIS) this;
        }

        public THIS factoryClass(String factoryClass) {
            this.factoryClass = factoryClass;
            return (THIS) this;
        }

        public THIS validate() {
            if (socketBinding == null || factoryClass == null) {
                throw new IllegalArgumentException("Both socket binding and factory class have to be set!");
            }
            return (THIS) this;
        }
    }

    /**
     * Builder which builds transport configuration item with type 'Remote'
     */
    protected abstract static class RemoteBuilder<THIS extends RemoteBuilder<THIS>> extends Builder<THIS>  {
        private String socketBinding;

        protected RemoteBuilder(String name) {
            super(name);
        }

        protected RemoteBuilder(String name, String serverName) {
            super(name, serverName);
        }

        public THIS socketBinding(String socketBinding) {
            this.socketBinding = socketBinding;
            return (THIS) this;
        }

        public THIS validate() {
            if (socketBinding == null) {
                throw new IllegalArgumentException("Socket binding has to be set!");
            }
            return (THIS) this;
        }
    }
}
