package org.jboss.hal.testsuite.test.configuration.elytron.transformer;

import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

public class PrincipalTransformerUtils {

    private static final String CONSTANT = "constant";

    private final Operations operations;

    public PrincipalTransformerUtils(OnlineManagementClient client) {
        operations = new Operations(client);
    }

    public void createConstantTransformer(Address constantTransformerAddress) throws IOException {
        final String transformerValue = randomAlphanumeric(5);
        operations.add(constantTransformerAddress, Values.of(CONSTANT, transformerValue)).assertSuccess();
    }

}
