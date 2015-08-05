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
    private String protocol = "";

    public JGroupsOperations(CliClient client, String stackName, String transport, String protocol) {
        this(client);
        this.stackName = stackName;
        this.transport = transport;
        this.protocol = protocol;
    }

    public JGroupsOperations(CliClient client) {
        this.client = client;
        if (ConfigUtils.isDomain()) {
            profile = "/profile=full-ha";
        }
        protocol = "";//TODO: default protocol name
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
        return getBaseDmrPath() + "transport=" + transport + "/";
    }

    private String getProtocolDrmPath() {//TODO: verify
        return getBaseDmrPath() + "protocol=" + protocol + "/";
    }

    /**/

    private boolean addProperty(String path, String name, String value) {
        String command = path + "property=" + name + ":add(value=" + value + ")";
        return client.executeForSuccess(command);
    }

    private void removeProperty(String path, String name) {
        String command = path + "property=" + name + ":remove";
        client.executeCommand(command);
    }

    public boolean verifyProperty(String path, String name, String value) {
        String command = path + ":read-resource";
        return client.executeForResponse(command)
                .get("result")
                .get("properties")
                .toJSONString(false)
                .contains("\"" + name + "\" : \"" + value + "\"");
    }

    /*transport properties*/

    public boolean addTransportProperty(String name, String value) {
        return addProperty(getTransportDrmPath(), name, value);
    }

    public void removeTransportProperty(String name, String value) {
        removeProperty(getTransportDrmPath(), name);
    }

    public boolean verifyTransportProperty(String name, String value) {
        return verifyProperty(getTransportDrmPath(), name, value);
    }

    /*protocol properties*/

    public boolean addProtocolProperty(String name, String value) {//TODO: verify
        return addProperty(getProtocolDrmPath(), name, value);
    }

    public boolean verifyProtocolProperty(String name, String value) {//todo verify
        return verifyProperty(getProtocolDrmPath(), name, value);
    }

    public void removeProtocolProperty(String name) {//todo verify
        removeProperty(getProtocolDrmPath(), name);
    }
}
