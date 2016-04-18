package org.jboss.hal.testsuite.test.configuration.transactions;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.creaper.command.RemoveSocketBinding;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Batch;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class ProcessIDTestCase extends TransactionsTestCaseAbstract {

    private final String PROCESS_ID_UUID = "process-id-uuid";
    private final String PROCESS_ID_SOCKET_BINDING = "process-id-socket-binding";
    private final String PROCESS_ID_SOCKET_MAX_PORTS = "process-id-socket-max-ports";

    private final String PROCESS_ID_UUID_ATTR = "process-id-uuid";
    private final String PROCESS_ID_SOCKET_BINDING_ATTR = "process-id-socket-binding";
    private final String PROCESS_ID_SOCKET_MAX_PORTS_ATTR = "process-id-socket-max-ports";

    private static String socketBinding;

    @BeforeClass
    public static void setUp() throws IOException, CommandFailedException {
        socketBinding = transactionsOps.createSocketBinding();
    }

    @Before
    public void before() throws IOException, TimeoutException, InterruptedException {
        prepareProcessIDConfiguration("", true);
        administration.reloadIfRequired();
        page.navigate();
        page.getConfig().switchTo("Process ID");
    }

    @AfterClass
    public static void afterClass() throws CommandFailedException {
        client.apply(new RemoveSocketBinding(socketBinding));
    }

    @Test
    public void setProcessIDUUIDToTrue() throws Exception {
        editAndVerifyUUIDAndSocketBinding(true, "");
    }

    @Test
    public void setProcessIDUUIDToFalse() throws Exception {
        editAndVerifyUUIDAndSocketBinding(false, socketBinding);
    }

    @Test
    public void editProcessIDSocketBinding() throws Exception {
        editAndVerifyUUIDAndSocketBinding(false, socketBinding);
    }

    @Test
    public void editProcessIDSocketMaxPorts() throws Exception {
        prepareProcessIDConfiguration(socketBinding, false);
        page.navigate();
        page.getConfig().switchTo("Process ID");
        editTextAndVerify(TRANSACTIONS_ADDRESS, PROCESS_ID_SOCKET_MAX_PORTS_ATTR, 15);
    }

    @Test
    public void editProcessIDSocketMaxPortsInvalid() throws IOException, InterruptedException {
        verifyIfErrorAppears(PROCESS_ID_SOCKET_MAX_PORTS, "foobar");
    }

    private void prepareProcessIDConfiguration(String socketBinding, boolean enableProcessUUID) throws IOException {
        if (enableProcessUUID) {
            operations.writeAttribute(TRANSACTIONS_ADDRESS, PROCESS_ID_UUID_ATTR, true);
        } else {
            Batch batch = new Batch()
                    .undefineAttribute(TRANSACTIONS_ADDRESS, PROCESS_ID_UUID_ATTR)
                    .writeAttribute(TRANSACTIONS_ADDRESS, PROCESS_ID_SOCKET_BINDING_ATTR, socketBinding);
            operations.batch(batch);
        }
    }

    private void editUUIDAndSocketBinding(boolean enableUUID, String socketBinding) {
        ConfigFragment config = page.getConfigFragment();
        Editor editor = config.edit();
        editor.checkbox(PROCESS_ID_UUID, enableUUID);
        editor.text(PROCESS_ID_SOCKET_BINDING, socketBinding);
        config.save();
    }

    private void editAndVerifyUUIDAndSocketBinding(boolean enableUUID, String socketBinding) throws Exception {
        editUUIDAndSocketBinding(enableUUID, socketBinding);
        administration.reloadIfRequired();
        ResourceVerifier resourceVerifier = new ResourceVerifier(TRANSACTIONS_ADDRESS, client)
                .verifyAttribute(PROCESS_ID_UUID_ATTR, enableUUID);
        if (enableUUID) {
            resourceVerifier.verifyAttributeIsUndefined(PROCESS_ID_SOCKET_BINDING_ATTR);
        } else {
            resourceVerifier.verifyAttribute(PROCESS_ID_SOCKET_BINDING_ATTR, socketBinding);
        }
    }
}
