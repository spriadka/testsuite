package org.jboss.hal.testsuite.creaper.command;

/**
 * Command which creates and adds a connector to given server
 */
public final class AddConnector extends AbstractTransportConfigAddCommand {

    private AddConnector(InVmBuilder builder) {
        super(builder);
    }

    private AddConnector(GenericBuilder builder) {
        super(builder);
    }

    private AddConnector(RemoteBuilder builder) {
        super(builder);
    }

    @Override
    protected TransportConfigItem getConfigType() {
        return TransportConfigItem.CONNECTOR;
    }

    public static final class InVmBuilder extends AbstractTransportConfigAddCommand.InVmBuilder<InVmBuilder> {

        public InVmBuilder(String name) {
            super(name);
        }

        public InVmBuilder(String name, String serverName) {
            super(name, serverName);
        }

        @Override
        public AddConnector build() {
            return new AddConnector(validate());
        }
    }

    public static final class RemoteBuilder extends AbstractTransportConfigAddCommand.RemoteBuilder<RemoteBuilder> {

        public RemoteBuilder(String name) {
            super(name);
        }

        public RemoteBuilder(String name, String serverName) {
            super(name, serverName);
        }

        @Override
        public AddConnector build() {
            return new AddConnector(this);
        }
    }

    public static final class GenericBuilder extends AbstractTransportConfigAddCommand.GenericBuilder<GenericBuilder> {

        public GenericBuilder(String name) {
            super(name);
        }

        public GenericBuilder(String name, String serverName) {
            super(name, serverName);
        }

        @Override
        public AddConnector build() {
            return new AddConnector(this);
        }
    }
}
