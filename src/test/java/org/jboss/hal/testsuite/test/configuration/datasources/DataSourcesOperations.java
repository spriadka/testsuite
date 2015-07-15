package org.jboss.hal.testsuite.test.configuration.datasources;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliConstants;
import org.jboss.hal.testsuite.util.ConfigUtils;

/**
 * Created by mkrajcov on 4/13/15.
 */
public class DataSourcesOperations {

    private CliClient client;

    private String profile = "";

    public DataSourcesOperations(CliClient client) {
        this.client = client;
        if (ConfigUtils.isDomain()) {
            profile = " --profile=full";
        }
    }

    public DataSourcesOperations(CliClient client, String profile) {
        this(client);
        this.profile = " --profile=" + profile;
    }

    public static String getDsAddress(String name) {
        return CliConstants.DATASOURCES_ADDRESS + "=" + name;
    }

    public static String getXADsAddress(String name) {
        return CliConstants.XA_DATASOURCES_ADDRESS + "=" + name;
    }

    public String createDataSource(String name, String url) {
        String command = "data-source add" +
                profile +
                " --name=" + name +
                " --jndi-name=java:/datasources/" + name +
                " --driver-name=h2" +
                " --connection-url=\"" + url + "\"" +
                " --enabled=true";
        client.executeCommand(command);
        return name;
    }

    public String createDataSource(String url) {
        String name = RandomStringUtils.randomAlphanumeric(5);
        return createDataSource(name, url);
    }

    public String createXADataSource(String name, String url) {
        String command = "xa-data-source add" +
                profile +
                " --name=" + name +
                " --jndi-name=java:/xa-datasources/" + name +
                " --driver-name=h2" +
                " --enabled=true" +
                " --xa-datasource-properties=URL=\"" + url + "\"";
        client.executeCommand(command);
        return name;
    }

    public String createXADataSource(String url) {
        String name = RandomStringUtils.randomAlphanumeric(5);
        return createXADataSource(name, url);
    }

    public void removeXADataSource(String name) {
        String command = "xa-data-source remove" +
                profile +
                " --name=" + name;
        client.executeCommand(command);
    }

    public void removeDataSource(String name) {
        String command = "data-source remove" +
                profile +
                " --name=" + name;
        client.executeCommand(command);
    }

    public boolean exists(String address) {
        return client.executeForSuccess(address + ":read-resource");
    }
}
