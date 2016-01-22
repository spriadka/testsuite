package org.jboss.hal.testsuite.creaper.command;

/**
 * Command which creates and adds an acceptor to given server
 */
public final class AddAcceptor extends AbstractTransportConfigAddCommand {

    private AddAcceptor(InVmBuilder builder) {
        super(builder);
    }

    private AddAcceptor(GenericBuilder builder) {
        super(builder);
    }

    private AddAcceptor(RemoteBuilder builder) {
        super(builder);
    }

    @Override
    protected TransportConfigItem getConfigType() {
        return TransportConfigItem.ACCEPTOR;
    }

    public static final class InVmBuilder extends AbstractTransportConfigAddCommand.InVmBuilder<InVmBuilder> {

        public InVmBuilder(String name) {
            super(name);
        }

        public InVmBuilder(String name, String serverName) {
            super(name, serverName);
        }

        @Override
        public AddAcceptor build() {
            return new AddAcceptor(validate());
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
        public AddAcceptor build() {
            return new AddAcceptor(this);
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
        public AddAcceptor build() {
            return new AddAcceptor(this);
        }
    }
}
