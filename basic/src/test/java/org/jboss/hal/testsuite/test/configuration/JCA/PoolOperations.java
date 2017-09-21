package org.jboss.hal.testsuite.test.configuration.JCA;

import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.jboss.hal.testsuite.test.configuration.JCA.PoolConfigurationConstants.*;

public final class PoolOperations {

    private final OnlineManagementClient client;
    private final Address operationsAddress;
    private final Operations operations;
    private final Administration administration;

    public PoolOperations(OnlineManagementClient client, Address address) {
        this.client = client;
        operationsAddress = address;
        operations = new Operations(client);
        administration = new Administration(client);
    }

    public void setCapacityDecrementerClassInModel(String decrementerClass) throws IOException, TimeoutException, InterruptedException {
        operations.writeAttribute(operationsAddress, CAPACITY_DECREMENTER_CLASS, decrementerClass).assertSuccess();
        administration.reloadIfRequired();
    }

    public void setCapacityDecrementerPropertyInModel(String propertyKey, String propertyValue) throws InterruptedException, TimeoutException, IOException {
        operations.writeAttribute(operationsAddress, CAPACITY_DECREMENTER_PROPERTIES,
                new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty(propertyKey, propertyValue).build())
                .assertSuccess();
        administration.reloadIfRequired();
    }

    public void setCapacityIncrementerClassInModel(String incrementerClass) throws IOException, TimeoutException, InterruptedException {
        operations.writeAttribute(operationsAddress, CAPACITY_INCREMENTER_CLASS, incrementerClass).assertSuccess();
        administration.reloadIfRequired();
    }

    public void setCapacityIncrementerPropertyInModel(String propertyKey, String propertyValue) throws InterruptedException, TimeoutException, IOException {
        operations.writeAttribute(operationsAddress, CAPACITY_INCREMENTER_PROPERTIES,
                new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty(propertyKey, propertyValue).build())
                .assertSuccess();
        administration.reloadIfRequired();
    }

}
