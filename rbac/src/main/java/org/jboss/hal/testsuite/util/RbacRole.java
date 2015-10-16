package org.jboss.hal.testsuite.util;

/**
 * @author jcechace
 */
public enum RbacRole {
    // Global roles
    DEPLOYER("Deployer"),
    MONITOR("Monitor"),
    OPERATOR("Monitor"),
    MAINTAINER("Maintainer"),
    ADMINISTRATOR("Administrator"),
    SUPERUSER("SuperUser"),
    AUDITOR("Auditor"),
    MAIN_ADMINISTRATOR("MainAdministrator"),
    OTHER_ADMINISTRATOR("OtherAdministrator"),
    HOST_MASTER_ADMINISTRATOR("HostMasterAdministrator"),
    HOST_SLAVE_ADMINISTRATOR("HostSlaveAdministrator"),
    MAIN_MONITOR("MainMonitor"),
    HOST_MASTER_MONITOR("HostMasterMonitor"),

    /*// Group scoped roles
    MAIN_DEPLOYER,
    MAIN_MONITOR,
    MAIN_OPERATOR,
    MAIN_MAINTAINER,
    MAIN_ADMINISTRATOR,
    MAIN_SUPERUSER,
    MAIN_AUDITOR,
    OTHER_DEPLOYER,
    OTHER_MONITOR,
    OTHER_OPERATOR,
    OTHER_MAINTAINER,
    OTHER_ADMINISTRATOR,
    OTHER_SUPERUSER,
    OTHER_AUDITOR,
    MAINMASTER_ADMINISTRATOR,
    // Host scoped roles
    HOST_MASTER_DEPLOYER,
    HOST_MASTER_MONITOR,
    HOST_MASTER_OPERATOR,
    HOST_MASTER_MAINTAINER,
    HOST_MASTER_ADMINISTRATOR,
    HOST_MASTER_SUPERUSER,
    HOST_MASTER_AUDITOR,
    HOST_SLAVE_DEPLOYER,
    HOST_SLAVE_MONITOR,
    HOST_SLAVE_OPERATOR,
    HOST_SLAVE_MAINTAINER,
    HOST_SLAVE_ADMINISTRATOR,
    HOST_SLAVE_SUPERUSER,
    HOST_SLAVE_AUDITOR */
    ;

    public final String username;
    public final String password;
    public final String label;

    RbacRole(String label) {
        String role = toString().toLowerCase().replaceAll("_", "-");
        username = System.getProperty("auth." + role + ".username", role);
        password = System.getProperty("auth." + role + ".password", "asd1asd!");
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
