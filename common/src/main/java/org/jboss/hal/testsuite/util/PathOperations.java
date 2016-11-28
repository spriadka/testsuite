package org.jboss.hal.testsuite.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathOperations {

    private static final Logger log = LoggerFactory.getLogger(PathOperations.class);

    private final Operations ops;

    public PathOperations(final OnlineManagementClient managementClient) {
        this.ops = new Operations(managementClient);
    }

    /**
     * Resolve full filesystem path of path defined by /path=&lt;pathName&gt;. Path is resolved recursively until relative-to
     * attribute is specified.
     *
     * @param pathName name of path in configuration
     * @return full path
     * @throws IOException when some IO error occurs during reading of attribute
     */
    public String resolveFullPath(final String pathName) throws IOException {
        log.debug("Resolving full path of '" + pathName + "'");
        Address pathAddress = Address.of("path", pathName);
        String path = ops.readAttribute(pathAddress, "path").stringValue();
        log.debug("Resolved path: " + path);
        ModelNodeResult relativeTo = ops.readAttribute(pathAddress, "relative-to");

        Path resolved;
        if (relativeTo.isFailed() || !relativeTo.hasDefinedValue()) {
            resolved = Paths.get(path);
        } else {
            resolved = Paths.get(resolveFullPath(relativeTo.stringValue()), path);
        }

        log.debug("Resolved FULL path: " + path);
        return resolved.toString();
    }

    public Path getServerLogFile() throws IOException {
        return Paths.get(this.resolveFullPath("jboss.server.log.dir"),
                ops.readChildrenNames(Address.subsystem("logging"), "log-file").listValue().get(0).asString());
    }
}
