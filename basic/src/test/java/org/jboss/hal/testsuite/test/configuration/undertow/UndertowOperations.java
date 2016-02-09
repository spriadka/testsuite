package org.jboss.hal.testsuite.test.configuration.undertow;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.mina.util.AvailablePortFinder;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.dmr.AddressTemplate;
import org.jboss.hal.testsuite.dmr.DefaultContext;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.Operation;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.StatementContext;
import org.jboss.hal.testsuite.creaper.command.AddSocketBinding;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extras.creaper.commands.undertow.AddHttpsSecurityRealm;
import org.wildfly.extras.creaper.commands.undertow.AddUndertowListener;
import org.wildfly.extras.creaper.commands.undertow.RemoveUndertowListener;
import org.wildfly.extras.creaper.commands.undertow.SslVerifyClient;
import org.wildfly.extras.creaper.commands.undertow.UndertowListenerType;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 17.9.15.
 */
public class UndertowOperations {

    private static final Logger log = LoggerFactory.getLogger(UndertowOperations.class);

    private StatementContext context = new DefaultContext();
    private AddressTemplate undertowAddressTemplate = AddressTemplate.of("{default.profile}/subsystem=undertow");
    private AddressTemplate ioWorkerAddressTemplate = AddressTemplate.of("{default.profile}/subsystem=io/worker=*");
    private AddressTemplate ioBufferPoolAddressTemplate = AddressTemplate.of("{default.profile}/subsystem=io/buffer-pool=*");
    private AddressTemplate socketBindingAddressTemplate;
    private AddressTemplate httpServerTemplate = undertowAddressTemplate.append("/server=*");
    private AddressTemplate servletContainerTemplate = undertowAddressTemplate.append("/servlet-container=*");
    private AddressTemplate hostTemplate = httpServerTemplate.append("/host=*");

    private OnlineManagementClient client;
    private Administration administration;
    private Operations operations;

    public UndertowOperations(OnlineManagementClient client) {
        this.client = client;
        this.operations = new Operations(client);
        this.administration = new Administration(client, 60000);
        String socketBindingGroup = ConfigUtils.isDomain() ? "full-sockets" : "standard-sockets";
        this.socketBindingAddressTemplate = AddressTemplate.of("/socket-binding-group=" + socketBindingGroup + "/socket-binding=*");
    }

    public void reloadIfRequiredAndWaitForRunning() throws InterruptedException, TimeoutException, IOException {
        administration.reloadIfRequired();
    }

    public void reloadAndWaitForRunning() throws InterruptedException, TimeoutException, IOException {
        administration.reload();
    }

    public String createAJPListener(String httpServer) throws IOException, CommandFailedException, TimeoutException, InterruptedException {
        String name = RandomStringUtils.randomAlphanumeric(6);
        log.info("Creating AJP listener " + name);
        client.apply(new AddUndertowListener.AjpBuilder(name, httpServer, createSocketBinding()).enabled(true).build());
        administration.reloadIfRequired();
        return name;
    }

    public void removeListener(String httpServer, String listenerName, UndertowListenerType listenerType) throws CommandFailedException, InterruptedException, TimeoutException, IOException {
        client.apply(new RemoveUndertowListener.Builder(listenerType, listenerName)
                .forServer(httpServer));
        administration.reloadIfRequired();
    }

    public void removeAJPListener(String httpServer, String listenerName) throws CommandFailedException, InterruptedException, IOException, TimeoutException {
        removeListener(httpServer, listenerName, UndertowListenerType.AJP_LISTENER);
    }

    public String createHTTPListener(String httpServer) throws IOException, CommandFailedException, TimeoutException, InterruptedException {
        String name = RandomStringUtils.randomAlphanumeric(6);
        client.apply(new AddUndertowListener.HttpBuilder(name, httpServer, createSocketBinding()).enabled(true).build());
        administration.reloadIfRequired();
        return name;
    }

    public void removeHTTPListener(String httpServer, String listenerName) throws CommandFailedException, InterruptedException, IOException, TimeoutException {
        removeListener(httpServer, listenerName, UndertowListenerType.HTTP_LISTENER);
    }

    public String createHTTPSListener(String httpServer) throws IOException, CommandFailedException, TimeoutException, InterruptedException, OperationException {
        String name = RandomStringUtils.randomAlphanumeric(6);
        String securityRealm = "SecurityRealm_" + RandomStringUtils.randomAlphanumeric(6);
        client.apply(new AddHttpsSecurityRealm.Builder(securityRealm)
            .keystorePassword("random")
            .truststorePassword("random")
            .keyPassword("random")
            .keystorePath(getClass().getClassLoader().getResource("clientkeystore").getPath())
            .build());
        administration.reload();
        client.apply(new AddUndertowListener.HttpsBuilder(name, httpServer, createSocketBinding())
                .securityRealm(securityRealm)
                .verifyClient(SslVerifyClient.NOT_REQUESTED)
                .enabled(true)
                .build());
        administration.reloadIfRequired();
        return name;
    }

    public void removeHTTPSListener(String httpServer, String listenerName) throws CommandFailedException, InterruptedException, IOException, TimeoutException {
        removeListener(httpServer, listenerName, UndertowListenerType.HTTPS_LISTENER);
    }

    public String createHTTPServerHost(String httpServer) throws InterruptedException, IOException, TimeoutException {
        String hostName = RandomStringUtils.randomNumeric(6);
        createHTTPServerHost(httpServer, hostName);
        return hostName;
    }

    public String createHTTPServerHost(String httpServer, String hostName) throws InterruptedException, TimeoutException, IOException {
        ResourceAddress address = hostTemplate.resolve(context, httpServer, hostName);
        executeAddAction(address);
        return hostName;
    }

    public void removeHTTPServerHost(String httpServer, String ajpListener) throws InterruptedException, TimeoutException, IOException {
        ResourceAddress address = hostTemplate.resolve(context, httpServer, ajpListener);
        executeRemoveAction(address);
    }

    public void removeHTTPServerHostIfExists(String httpServer, String ajpListener) throws InterruptedException, TimeoutException, IOException {
        ResourceAddress address = hostTemplate.resolve(context, httpServer, ajpListener);
        if (executeReadResourceAction(address)) {
            executeRemoveAction(address);
        }
    }

    public String createHTTPServer() throws InterruptedException, IOException, TimeoutException {
        String name = "HTTPServer_" + RandomStringUtils.randomAlphanumeric(6);
        ResourceAddress address = httpServerTemplate.resolve(context, name);
        executeAddAction(address, true);
        operations.readResource(Address.of("subsystem", "undertow").and("server", name)); //Workaround for situation when server is listed as unavailable dependency
        return name;
    }

    public void removeHTTPServer(String httpServer) throws InterruptedException, TimeoutException, IOException {
        executeRemoveAction(httpServerTemplate.resolve(context, httpServer));
    }

    public String createServletContainer() throws InterruptedException, TimeoutException, IOException {
        String name = "ServletContainer_" + RandomStringUtils.randomAlphanumeric(6);
        ResourceAddress address = servletContainerTemplate.resolve(context, name);
        executeAddAction(address);
        return name;
    }

    public void removeServletContainer(String servletContainer) throws InterruptedException, TimeoutException, IOException {
        executeRemoveAction(servletContainerTemplate.resolve(context, servletContainer));
    }

    private void executeRemoveAction(ResourceAddress address) throws InterruptedException, IOException, TimeoutException {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.execute(new Operation.Builder("remove", address).build()); //TODO add creaper to all methods, then remove
        dispatcher.close();
        reloadIfRequiredAndWaitForRunning();
    }

    private boolean executeReadResourceAction(ResourceAddress address) {
        Dispatcher dispatcher = new Dispatcher();
        boolean result = dispatcher.execute(new Operation.Builder("read-resource", address).build()).isSuccessful();
        dispatcher.close();
        return result;
    }

    private void executeAddAction(ResourceAddress address, Map<String, String> params, boolean reload) throws InterruptedException, IOException, TimeoutException {
        Operation.Builder operationBuilder = new Operation.Builder("add", address);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            operationBuilder.param(entry.getKey(), entry.getValue());
        }
        Dispatcher dispatcher = new Dispatcher(); //TODO add creaper to all methods, then remove
        dispatcher.execute(operationBuilder.build());
        dispatcher.close();
        if (reload) reloadIfRequiredAndWaitForRunning();
    }

    private void executeAddAction(ResourceAddress address, Map<String, String> params) throws InterruptedException, TimeoutException, IOException {
       executeAddAction(address, params, true);
    }

    private void executeAddAction(ResourceAddress address) throws InterruptedException, IOException, TimeoutException {
        executeAddAction(address, Collections.emptyMap());
    }

    private void executeAddAction(ResourceAddress address, boolean reload) throws InterruptedException, TimeoutException, IOException {
        executeAddAction(address, Collections.emptyMap(), reload);
    }

    public String createWorker() throws InterruptedException, TimeoutException, IOException {
        String name = "UndertowWorker_" + RandomStringUtils.randomAlphanumeric(6);
        ResourceAddress address = ioWorkerAddressTemplate.resolve(context, name);
        executeAddAction(address);
        return name;
    }

    public void removeWorker(String name) throws InterruptedException, TimeoutException, IOException {
        ResourceAddress address = ioWorkerAddressTemplate.resolve(context, name);
        executeRemoveAction(address);
    }

    public String createBufferPool() throws InterruptedException, TimeoutException, IOException {
        String name = "UndertowBufferPool_" + RandomStringUtils.randomAlphanumeric(6);
        ResourceAddress address = ioBufferPoolAddressTemplate.resolve(context, name);
        executeAddAction(address);
        return name;
    }

    public void removeBufferPool(String name) throws InterruptedException, TimeoutException, IOException {
        ResourceAddress address = ioBufferPoolAddressTemplate.resolve(context, name);
        executeRemoveAction(address);
    }

    public String createSocketBinding() throws CommandFailedException, IOException {
        String name = "UndertowSocketBinding_" + RandomStringUtils.randomAlphanumeric(6);
        try (OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient()) {
            int port = AvailablePortFinder.getNextAvailable();
            log.info("Obtained port for socket binding '" + name + "' is " + port);
            client.apply(new AddSocketBinding.Builder(name)
                    .port(port)
                    .build());
        }
        return name;
    }

    public void removeSocketBinding(String name) throws InterruptedException, TimeoutException, IOException {
        ResourceAddress address = socketBindingAddressTemplate.resolve(context, name);
        executeRemoveAction(address);
    }

    public String addBufferCache() throws IOException {
        String name = "BufferCache_" + RandomStringUtils.randomAlphanumeric(6);
        Address address = Address.subsystem("undertow").and("buffer-cache", name);
        operations.add(address);
        return name;
    }

    public void removeBufferCache(String name) throws IOException, OperationException {
        Address address = Address.subsystem("undertow").and("buffer-cache", name);
        operations.removeIfExists(address);
    }

    public void writeAttribute(ResourceAddress resourceAddress, String attrName, boolean attrValue) {
        Operation writeAttrOperation = new Operation.Builder(WRITE_ATTRIBUTE_OPERATION, resourceAddress)
                .param(NAME, attrName)
                .param(VALUE, attrValue)
                .build();
        Dispatcher dispatcher = new Dispatcher();
        try {
            dispatcher.execute(writeAttrOperation);
        } finally {
            dispatcher.close();
        }
    }
}
