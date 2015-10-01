package org.jboss.hal.testsuite.test.configuration.undertow;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.DomainManager;
import org.jboss.hal.testsuite.dmr.AddressTemplate;
import org.jboss.hal.testsuite.dmr.DefaultContext;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.DmrResponse;
import org.jboss.hal.testsuite.dmr.Operation;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.StatementContext;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 17.9.15.
 */
public class UndertowOperations {

    private static final Logger log = LoggerFactory.getLogger(UndertowOperations.class);

    private Dispatcher dispatcher;
    private StatementContext context = new DefaultContext();
    private AddressTemplate undertowAddressTemplate = AddressTemplate.of("{default.profile}/subsystem=undertow");
    private AddressTemplate ioWorkerAdressTemplate = AddressTemplate.of("{default.profile}/subsystem=io/worker=*");
    private AddressTemplate ioBufferPoolAdressTemplate = AddressTemplate.of("{default.profile}/subsystem=io/buffer-pool=*");
    private AddressTemplate httpServerTemplate = undertowAddressTemplate.append("/server=*");
    private AddressTemplate servletContainerTemplate = undertowAddressTemplate.append("/servlet-container=*");
    private AddressTemplate ajpListenerTemplate = httpServerTemplate.append("/ajp-listener=*");
    private AddressTemplate httpListenerTemplate = httpServerTemplate.append("/http-listener=*");
    private AddressTemplate httpsListenerTemplate = httpServerTemplate.append("/https-listener=*");

    public UndertowOperations(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public static void reloadIfRequiredAndWaitForRunning() {
        final int timeout = 60000;
        if (ConfigUtils.isDomain()) {
            new DomainManager(CliClientFactory.getClient()).reloadIfRequiredAndWaitUntilRunning(timeout);
        } else {
            CliClientFactory.getClient().reload();
        }
    }

    public static void reloadAndWaitForRunning() {
        final int timeout = 60000;
        if (ConfigUtils.isDomain()) {
            new DomainManager(CliClientFactory.getClient()).reloadAndWaitUntilRunning(timeout);
        } else {
            CliClientFactory.getClient().reload(true);
        }
    }

    public String createAJPListener(String httpServer) {
        String name = RandomStringUtils.randomAlphanumeric(6);
        Map<String, String> params = ImmutableMap.of("socket-binding", "ajp");
        executeAddAction(ajpListenerTemplate.resolve(context, httpServer, name), params);
        return name;
    }

    public void removeAJPListener(String httpServer, String listenerName) {
        executeRemoveAction(ajpListenerTemplate.resolve(context, httpServer, listenerName));
    }

    public String createHTTPListener(String httpServer) {
        String name = RandomStringUtils.randomAlphanumeric(6);
        Map<String, String> params = ImmutableMap.of("socket-binding", "https");
        executeAddAction(httpListenerTemplate.resolve(context, httpServer, name), params);
        return name;
    }

    public void removeHTTPListener(String httpServer, String listenerName) {
        executeRemoveAction(httpListenerTemplate.resolve(context, httpServer, listenerName));
    }

    public String createHTTPSListener(String httpServer) {
        String name = RandomStringUtils.randomAlphanumeric(6);
        ResourceAddress address = httpsListenerTemplate.resolve(context, httpServer, name);
        Map <String, String> properties = ImmutableMap.of("socket-binding", "https", "security-realm", "ManagementRealm");
        executeAddAction(address, properties);
        return name;
    }

    public void removeHTTPSListener(String httpServer, String listenerName) {
        ResourceAddress address = httpsListenerTemplate.resolve(context, httpServer, listenerName);
        executeRemoveAction(address);
    }

    public String createHTTPServer() {
        String name = RandomStringUtils.randomAlphanumeric(6);
        ResourceAddress address = httpServerTemplate.resolve(context, name);
        executeAddAction(address);
        return name;
    }

    public void removeHTTPServer(String httpServer) {
        executeRemoveAction(httpServerTemplate.resolve(context, httpServer));
    }

    public String createServletContainer() {
        String name = RandomStringUtils.randomAlphanumeric(6);
        ResourceAddress address = servletContainerTemplate.resolve(context, name);
        executeAddAction(address);
        return name;
    }

    public void removeServletContainer(String servletContainer) {
        executeRemoveAction(servletContainerTemplate.resolve(context, servletContainer));
    }

    private void executeRemoveAction(ResourceAddress address) {
        dispatcher.execute(new Operation.Builder("remove", address).build());
        reloadIfRequiredAndWaitForRunning();
    }

    private void executeAddAction(ResourceAddress address, Map<String, String> params) {
        Operation.Builder operationBuilder = new Operation.Builder("add", address);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            operationBuilder.param(entry.getKey(), entry.getValue());
        }
        DmrResponse response = dispatcher.execute(operationBuilder.build());
        log.debug(response.response.asString());
        reloadIfRequiredAndWaitForRunning();
    }

    private void executeAddAction(ResourceAddress address) {
        executeAddAction(address, Collections.emptyMap());
    }

    public String createWorker() {
        String name = RandomStringUtils.randomAlphanumeric(6);
        ResourceAddress address = ioWorkerAdressTemplate.resolve(context, name);
        executeAddAction(address);
        return name;
    }

    public void removeWorker(String name) {
        ResourceAddress address = ioWorkerAdressTemplate.resolve(context, name);
        executeRemoveAction(address);
    }

    public String createBufferPool() {
        String name = RandomStringUtils.randomAlphanumeric(6);
        ResourceAddress address = ioBufferPoolAdressTemplate.resolve(context, name);
        executeAddAction(address);
        return name;
    }

    public void removeBufferPool(String name) {
        ResourceAddress address = ioBufferPoolAdressTemplate.resolve(context, name);
        executeRemoveAction(address);
    }
}
