package org.jboss.hal.testsuite.test.configuration.elytron.logs;

import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.AddResourceWizard;
import org.jboss.hal.testsuite.page.config.elytron.ElytronLogsPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.CHECKBOX;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.SELECT;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(Elytron.class)
@RunWith(Arquillian.class)
public class ElytronFileAuditLogTestCase extends AbstractElytronTestCase {

    private static final String
            PATH = "path",
            FORMAT = "format",
            SYNCHRONIZED = "synchronized",
            RELATIVE_TO = "relative-to",
            FILE_AUDIT_LOG = "file-audit-log",
            FILE_AUDIT_LOG_LABEL = "File Audit Log";

    @Page
    private ElytronLogsPage page;

    /**
     * @tpTestDetails Try to create File Audit Log instance in Web Console's Elytron subsystem configuration.
     * Validate created resource is visible in File Audit Log table.
     * Validate created resource is present in model.
     * Validate attributes of created resource in model.
     */
    @Test
    public void testAddFileAuditLog() throws Exception {
        final Address fileAuditAddress = elyOps.getElytronAddress(FILE_AUDIT_LOG, randomAlphabetic(7));
        final String pathValue = randomAlphabetic(7);
        try {
            page.navigateToApplication()
                    .switchSubTab(FILE_AUDIT_LOG_LABEL);

            page.getResourceManager()
                    .addResource(AddResourceWizard.class)
                    .name(fileAuditAddress.getLastPairValue())
                    .text(PATH, pathValue)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();

            assertTrue(page.getResourceManager().isResourcePresent(fileAuditAddress.getLastPairValue()));

            new ResourceVerifier(fileAuditAddress, client).verifyExists().verifyAttribute(PATH, pathValue);
        } finally {
            ops.removeIfExists(fileAuditAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron File Audit Log instance in model and try to remove it in Web Console's Elytron
     * subsystem configuration.
     * Validate the resource is not any more visible in File Audit Log table.
     * Validate removed resource is not any more present in the model.
     */
    @Test
    public void testRemoveFileAuditLog() throws Exception {
        final Address fileAuditAddress = elyOps.getElytronAddress(FILE_AUDIT_LOG, randomAlphabetic(7));
        try {
            createFileAuditLog(fileAuditAddress);

            page.navigateToApplication()
                    .switchSubTab(FILE_AUDIT_LOG_LABEL);

            page.getResourceManager()
                    .removeResource(fileAuditAddress.getLastPairValue())
                    .confirmAndDismissReloadRequiredMessage();

            assertFalse(page.getResourceManager().isResourcePresent(fileAuditAddress.getLastPairValue()));

            new ResourceVerifier(fileAuditAddress, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(fileAuditAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron File Audit Log instance in model and try to edit its format attribute value in
     * Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editFormat() throws Exception {
        final Address fileAuditAddress = elyOps.getElytronAddress(FILE_AUDIT_LOG, randomAlphabetic(7));
        final String formatValue = "JSON";
        try {
            createFileAuditLog(fileAuditAddress);

            page.navigateToApplication()
                    .switchSubTab(FILE_AUDIT_LOG_LABEL);

            page.getResourceManager().selectByName(fileAuditAddress.getLastPairValue());

            new ConfigChecker.Builder(client, fileAuditAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(SELECT, FORMAT, formatValue)
                    .verifyFormSaved()
                    .verifyAttribute(FORMAT, formatValue);
        } finally {
            ops.removeIfExists(fileAuditAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron File Audit Log instance in model and try to edit its path attribute value in Web
     * Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editPath() throws Exception {
        final Address fileAuditAddress = elyOps.getElytronAddress(FILE_AUDIT_LOG, randomAlphabetic(7));
        final String pathValue = randomAlphabetic(7);
        try {
            createFileAuditLog(fileAuditAddress);

            page.navigateToApplication()
                    .switchSubTab(FILE_AUDIT_LOG_LABEL);

            page.getResourceManager().selectByName(fileAuditAddress.getLastPairValue());

            new ConfigChecker.Builder(client, fileAuditAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, PATH, pathValue)
                    .verifyFormSaved()
                    .verifyAttribute(PATH, pathValue);
        } finally {
            ops.removeIfExists(fileAuditAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron File Audit Log instance in model and try to edit its relative-to attribute value in
     * Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editRelativeTo() throws Exception {
        final Address fileAuditAddress = elyOps.getElytronAddress(FILE_AUDIT_LOG, randomAlphabetic(7));
        final String relativeToValue = randomAlphabetic(7);
        try {
            createFileAuditLog(fileAuditAddress);

            page.navigateToApplication()
                    .switchSubTab(FILE_AUDIT_LOG_LABEL);

            page.getResourceManager().selectByName(fileAuditAddress.getLastPairValue());

            new ConfigChecker.Builder(client, fileAuditAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, RELATIVE_TO, relativeToValue)
                    .verifyFormSaved()
                    .verifyAttribute(RELATIVE_TO, relativeToValue);
        } finally {
            ops.removeIfExists(fileAuditAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron File Audit Log instance in model and try to edit its synchronized attribute value
     * in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void toggleSynchronized() throws Exception {
        final Address fileAuditAddress = elyOps.getElytronAddress(FILE_AUDIT_LOG, randomAlphabetic(7));
        try {
            createFileAuditLog(fileAuditAddress);

            final ModelNodeResult originalModelNodeResult = ops.readAttribute(fileAuditAddress, SYNCHRONIZED);
            originalModelNodeResult.assertSuccess();

            page.navigateToApplication()
                    .switchSubTab(FILE_AUDIT_LOG_LABEL);

            page.getResourceManager().selectByName(fileAuditAddress.getLastPairValue());

            new ConfigChecker.Builder(client, fileAuditAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(CHECKBOX, SYNCHRONIZED, !originalModelNodeResult.booleanValue())
                    .verifyFormSaved()
                    .verifyAttribute(SYNCHRONIZED, !originalModelNodeResult.booleanValue());

            new ConfigChecker.Builder(client, fileAuditAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(CHECKBOX, SYNCHRONIZED, originalModelNodeResult.booleanValue())
                    .verifyFormSaved()
                    .verifyAttribute(SYNCHRONIZED, originalModelNodeResult.booleanValue());

        } finally {
            ops.removeIfExists(fileAuditAddress);
            adminOps.reloadIfRequired();
        }
    }

    private void createFileAuditLog(Address address) throws IOException {
        ops.add(address, Values.of(PATH, randomAlphabetic(7))).assertSuccess();
    }
}
