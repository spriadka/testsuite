package org.jboss.hal.testsuite.test.configuration.transactions;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.dmr.Composite;
import org.jboss.hal.testsuite.dmr.Operation;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.UNDEFINE_ATTRIBUTE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 12.10.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class ProcessIDTestCase extends TransactionsTestCaseAbstract {

    private final String PROCESS_ID_UUID = "process-id-uuid";
    private final String PROCESS_ID_SOCKET_BINDING = "process-id-socket-binding";
    private final String PROCESS_ID_SOCKET_MAX_PORTS = "process-id-socket-max-ports";

    private final String PROCESS_ID_UUID_ATTR = "process-id-uuid";
    private final String PROCESS_ID_SOCKET_BINDING_ATTR = "process-id-socket-binding";
    private final String PROCESS_ID_SOCKET_MAX_PORTS_ATTR = "process-id-socket-max-ports";

    @Before
    public void before() {
        prepareProcessIDConfiguration("", true);
        reloadIfRequiredAndWaitForRunning();
        page.navigate();
        page.getConfig().switchTo("Process ID");
    }

    @Test
    public void setProcessIDUUIDToTrue() throws IOException, InterruptedException {
        editAndVerifyUUIDAndSocketBinding(true, "");
    }

    @Test
    public void setProcessIDUUIDToFalse() throws IOException, InterruptedException {
        editAndVerifyUUIDAndSocketBinding(false, RandomStringUtils.randomAlphanumeric(6));
    }

    @Test
    public void editProcessIDSocketBinding() throws IOException, InterruptedException {
        editAndVerifyUUIDAndSocketBinding(false, RandomStringUtils.randomAlphanumeric(6));
    }

    @Test
    public void editProcessIDSocketMaxPorts() throws IOException, InterruptedException {
        editTextAndVerify(address, PROCESS_ID_SOCKET_MAX_PORTS, PROCESS_ID_SOCKET_MAX_PORTS_ATTR);
    }

    @Test
    public void editProcessIDSocketMaxPortsInvalid() throws IOException, InterruptedException {
        verifyIfErrorAppears(PROCESS_ID_SOCKET_MAX_PORTS, "adfs");
    }

    private void prepareProcessIDConfiguration(String socketBinding, boolean enableProcessUUID) {
        if (enableProcessUUID) {
            Operation setEnable = new Operation.Builder(WRITE_ATTRIBUTE_OPERATION, address)
                    .param(NAME, PROCESS_ID_UUID_ATTR)
                    .param(VALUE, true)
                    .build();
            dispatcher.execute(setEnable);
        } else {
            Operation undefineUUID = new Operation.Builder(UNDEFINE_ATTRIBUTE_OPERATION, address)
                    .param(NAME, PROCESS_ID_UUID_ATTR)
                    .build();
            Operation setSocketBinding = new Operation.Builder(WRITE_ATTRIBUTE_OPERATION, address)
                    .param(NAME, PROCESS_ID_SOCKET_BINDING_ATTR)
                    .param(VALUE, socketBinding)
                    .build();
            Composite composite = new Composite(undefineUUID, setSocketBinding);
            dispatcher.execute(composite);
        }
    }

    private void editUUIDAndSocketBinding(boolean enableUUID, String socketBinding) {
        ConfigFragment config = page.getConfigFragment();
        Editor editor = config.edit();
        editor.checkbox(PROCESS_ID_UUID, enableUUID);
        editor.text(PROCESS_ID_SOCKET_BINDING, socketBinding);
        config.save();
    }

    private void editAndVerifyUUIDAndSocketBinding(boolean enableUUID, String socketBinding) {
        editUUIDAndSocketBinding(enableUUID, socketBinding);
        reloadIfRequiredAndWaitForRunning();
        verifier.verifyAttribute(address, PROCESS_ID_SOCKET_BINDING_ATTR, socketBinding);
        verifier.verifyAttribute(address, PROCESS_ID_UUID_ATTR, enableUUID);
    }
}
