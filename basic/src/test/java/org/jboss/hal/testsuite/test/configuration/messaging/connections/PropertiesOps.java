package org.jboss.hal.testsuite.test.configuration.messaging.connections;

import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.test.configuration.messaging.AbstractMessagingTestCase;
import org.wildfly.extras.creaper.core.online.operations.Address;

import java.io.IOException;

public class PropertiesOps extends AbstractMessagingTestCase {


    protected static boolean isPropertyPresentInParams(Address address, String key) throws IOException {
        ModelNode result = operations.readAttribute(address, "params").value();
        return result.isDefined() && result.keys().contains(key);
    }

}
