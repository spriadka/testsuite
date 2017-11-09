package org.jboss.hal.testsuite.test.configuration.webservices;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.command.BackupAndRestoreAttributes;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class WebServicesProviderTestCase extends WebServicesTestCaseAbstract {

    private static final String MODIFY_SOAP_ADDRESS = "modify-wsdl-address";
    private static final String WSDL_HOST = "wsdl-host";
    private static final String WSDL_PORT = "wsdl-port";
    private static final String WSDL_SECURE_PORT = "wsdl-secure-port";

    private static final int PORT_VALUE = 50;
    private static final String PORT_VALUE_NEGATIVE = "-50";
    private static final String SIMPLE_IP = "127.0.0.2";

    private static BackupAndRestoreAttributes backup;

    @BeforeClass
    public static void beforeClass() throws CommandFailedException {
        backup = new BackupAndRestoreAttributes.Builder(WEBSERVICES_ADDRESS).build();
        client.apply(backup.backup());
    }

    @Before
    public void before() {
        page.navigate();
    }

    @AfterClass
    public static void afterClass() throws CommandFailedException, IOException, TimeoutException, InterruptedException {
        try {
            client.apply(backup.restore());
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void modifySoapAddress() throws Exception {
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(WEBSERVICES_ADDRESS, MODIFY_SOAP_ADDRESS);
        originalModelNodeResult.assertSuccess();
        final boolean originalBooleanValue = originalModelNodeResult.booleanValue();
        try {
            new ConfigChecker.Builder(client, WEBSERVICES_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, MODIFY_SOAP_ADDRESS, !originalBooleanValue)
                    .verifyFormSaved()
                    .verifyAttribute(MODIFY_SOAP_ADDRESS, !originalBooleanValue);

            new ConfigChecker.Builder(client, WEBSERVICES_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, MODIFY_SOAP_ADDRESS, originalBooleanValue)
                    .verifyFormSaved()
                    .verifyAttribute(MODIFY_SOAP_ADDRESS, originalBooleanValue);
        } finally {
            operations.writeAttribute(WEBSERVICES_ADDRESS, MODIFY_SOAP_ADDRESS, originalModelNodeResult.value());
        }
    }

    @Test
    public void setWsdlPort() throws Exception {
        new ConfigChecker.Builder(client, WEBSERVICES_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, WSDL_PORT, PORT_VALUE)
                .verifyFormSaved()
                .verifyAttribute(WSDL_PORT, PORT_VALUE);
    }

    @Test
    public void setWsdlPortNegative() throws Exception {
        new ConfigChecker.Builder(client, WEBSERVICES_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, WSDL_PORT, PORT_VALUE_NEGATIVE)
                .verifyFormNotSaved();
    }

    @Test
    public void setWsdlSecurePort() throws Exception {
        new ConfigChecker.Builder(client, WEBSERVICES_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, WSDL_SECURE_PORT, PORT_VALUE)
                .verifyFormSaved()
                .verifyAttribute(WSDL_PORT, PORT_VALUE);
    }

    @Test
    public void setWsdlSecurePortNegative() throws Exception {
        new ConfigChecker.Builder(client, WEBSERVICES_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, WSDL_SECURE_PORT, PORT_VALUE_NEGATIVE)
                .verifyFormNotSaved();
    }

    @Test
    public void setWsdlHostSimpleIP() throws Exception {
        new ConfigChecker.Builder(client, WEBSERVICES_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, WSDL_HOST, SIMPLE_IP)
                .verifyFormSaved()
                .verifyAttribute(WSDL_HOST, SIMPLE_IP);
    }
}
