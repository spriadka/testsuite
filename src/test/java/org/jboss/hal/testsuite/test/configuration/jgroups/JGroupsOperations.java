package org.jboss.hal.testsuite.test.configuration.jgroups;

import org.jboss.as.cli.scriptsupport.CLI;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.util.ConfigUtils;

/**
 * Created by jkasik on 23.7.15.
 */
public class JGroupsOperations {

    private CliClient client;
    private String profile = "";
    private String stackName = "";
    private String transport = "";

    public JGroupsOperations(CliClient client, String stackName, String transport) {
        this(client);
        this.stackName = stackName;
        this.transport = transport;
    }

    public JGroupsOperations(CliClient client) {
        this.client = client;
        if (ConfigUtils.isDomain()) {
            profile = "/profile=full-ha";
        }
    }

    public String getStackName() {
        return stackName;
    }

    public void setStackName(String stackName) {
        this.stackName = stackName;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    private String getBaseDmrPath() {
        return  profile +
                "/subsystem=jgroups/" +
                "stack=" + stackName + "/";
    }

    private String getTransportDrmPath() {
        return  getBaseDmrPath() + "transport=" + transport + "/";
    }

    /*transport properties*/

    public boolean addTransportProperty(String name, String value) {
        String command = getTransportDrmPath() + "property=" + name + ":add(value=" + value + ")";
        CLI.Result result = client.executeCommand(command);
        return result.isSuccess();
    }

    public void removeTransportProperty(String name, String value) {
        String command = getTransportDrmPath() + "property=" + name + ":remove";
        client.executeCommand(command);
    }

    public boolean verifyTransportProperty(String name, String value) {
        String command = getTransportDrmPath() + ":read-resource";
        CLI.Result result = client.executeCommand(command);
        return result.getResponse()
                .get("result")
                .get("properties")
                .toJSONString(false)
                .contains("\"" + name + "\" : \"" + value + "\"");
    }

    /*protocol properties*/

    public boolean addProtocolProperty(String name, String value) {
        //TODO
        return false;
    }

    public boolean verifyProtocolProperty(String name, String value) {
        //TODO
        return false;
    }

    public void removeProtocolProperty() {
        //TODO
    }
}
