package org.jboss.hal.testsuite.creaper.command;

import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import java.io.IOException;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 18.12.15.
 */
public class RemoveSocketBinding implements OnlineCommand {

    private final String socketBindingName;
    private final String socketBindingGroup;

    public RemoveSocketBinding(String socketBindingName, String socketBindingGroup) {
        if (socketBindingName == null) {
            throw new IllegalArgumentException("Name of the socket binding must be specified as non null value");
        }

        this.socketBindingName = socketBindingName;
        this.socketBindingGroup = socketBindingGroup;
    }

    public RemoveSocketBinding(String name) {
        this(name, null);
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws CommandFailedException {

        Operations operations = new Operations(ctx.client);
        String socketBindingGroup = this.socketBindingGroup;

        if (socketBindingGroup == null) {
            socketBindingGroup = ctx.client.options().isDomain ? "full-sockets" : "standard-sockets";
        }

        Address socketBindingAddress = Address.of("socket-binding-group", socketBindingGroup)
                .and("socket-binding", socketBindingName);

        try {
            operations.remove(socketBindingAddress);
        } catch (IOException e) {
            throw new CommandFailedException(e);
        }

    }
}
