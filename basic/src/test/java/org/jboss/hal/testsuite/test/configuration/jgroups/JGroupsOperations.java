package org.jboss.hal.testsuite.test.configuration.jgroups;

import org.jboss.hal.testsuite.cli.CliClient;

/**
 * Created by jkasik on 23.7.15.
 */
public class JGroupsOperations {

    private CliClient client;
    private String transport = "";
    private String baseDrmPath = "";

    public JGroupsOperations(CliClient client) {
        this.client = client;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    private String getBaseDmrPath() {
        return baseDrmPath;
    }

    public void setBaseDrmPath(String baseDrmPath) {
        this.baseDrmPath = baseDrmPath;
    }

    private String getTransportDrmPath() {
        return getBaseDmrPath() + "transport=" + transport + "/";
    }

    private String getProtocolDrmPath(String name) {//TODO: verify
        return getBaseDmrPath() + "protocol=" + name + "/";
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

    public boolean addProtocolProperty(String protocol, String name, String value) {
        return addProperty(getProtocolDrmPath(protocol), name, value);
    }

    public boolean verifyProtocolProperty(String protocol, String name, String value) {
        return verifyProperty(getProtocolDrmPath(protocol), name, value);
    }

    public void removeProtocolProperty(String protocol, String name) {
        removeProperty(getProtocolDrmPath(protocol), name);
    }
}
