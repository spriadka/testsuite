package org.jboss.hal.testsuite.test.configuration.datasources;

import org.apache.commons.lang3.RandomStringUtils;
import org.wildfly.extras.creaper.commands.datasources.AddDataSource;
import org.wildfly.extras.creaper.commands.datasources.AddXADataSource;
import org.wildfly.extras.creaper.commands.datasources.RemoveDataSource;
import org.wildfly.extras.creaper.commands.datasources.RemoveXADataSource;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import java.io.IOException;

/**
 * Created by mkrajcov on 4/13/15.
 */
public class DataSourcesOperations {

    private OnlineManagementClient client;
    private Operations operations;

    private String profile;

    public DataSourcesOperations(OnlineManagementClient client) {
        this.client = client;
        this.operations = new Operations(client);
    }

    public DataSourcesOperations(OnlineManagementClient client, String profile) {
        this(client);
        this.profile = profile;
    }

    public static Address getDsAddress(String name) {
        return Address.subsystem("datasources").and("data-source", name);
    }

    public static Address getXADsAddress(String name) {
        return Address.subsystem("datasources").and("xa-data-source", name);
    }

    public String createDataSource(String name, String url) throws CommandFailedException {
        client.apply(new AddDataSource.Builder(name)
                .jndiName("java:/datasources/" + name)
                .enableAfterCreate()
                .driverName("h2")
                .connectionUrl(url)
                .build());
        return name;
    }

    public String createDataSource(String url) throws CommandFailedException {
        String name = "dsOps_" + RandomStringUtils.randomAlphanumeric(5);
        return createDataSource(name, url);
    }

    public String createXADataSource(String name, String url) throws CommandFailedException {
        client.apply(new AddXADataSource.Builder(name)
                .jndiName("java:/xa-datasources/" + name)
                .driverName("h2")
                .enableAfterCreate()
                .addXaDatasourceProperty("URL", url)
                .build());
        return name;
    }

    public String createXADataSource(String url) throws CommandFailedException {
        String name = "XAdsOps_" + RandomStringUtils.randomAlphanumeric(5);
        return createXADataSource(name, url);
    }

    public void removeXADataSource(String name) throws CommandFailedException {
        client.apply(new RemoveXADataSource(name));
    }

    public void removeDataSource(String name) throws CommandFailedException {
        client.apply(new RemoveDataSource(name));
    }

    public boolean exists(Address address) throws IOException, OperationException {
        return operations.exists(address);
    }
}
