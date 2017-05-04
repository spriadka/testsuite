package org.jboss.hal.testsuite.test.configuration.elytron.authentication;

import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

public class ElytronAuthenticationOperations {

    private OnlineManagementClient client;

    public ElytronAuthenticationOperations(OnlineManagementClient client) {
        this.client = client;
    }

    /**
     * Back ups values of attributes, performs and {@link Action} and reverts the attributes to their original value.
     *  @param address        Address of attributes
     * @param action         Action performed with backed up attributes
     */
    public void performActionOnAttributeAndRevertItsValueToOriginal(Address address, String attribute, Action action) throws Exception {
        final Operations operations = new Operations(client);

        final ModelNodeResult modelNodeResult = operations.readAttribute(address, attribute);
        modelNodeResult.assertSuccess();

        try {
            action.perform();
        } finally {
            operations.writeAttribute(address, attribute, modelNodeResult.value());
        }

    }

    @FunctionalInterface
    public interface Action {
        void perform() throws Exception;
    }
}
