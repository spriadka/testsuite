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
public class ElytronFilePeriodicRotatingAuditLogTestCase extends AbstractElytronTestCase {

    private static final String
            FILE_PERIODIC_ROTATING_AUDIT_LOG = "periodic-rotating-file-audit-log",
            FILE_PERIODIC_ROTATING_AUDIT_LOG_LABEL = "File Periodic Rotating Audit Log",
            FORMAT = "format",
            PATH = "path",
            RELATIVE_TO = "relative-to",
            SUFFIX = "suffix",
            SYNCHRONIZED = "synchronized";

    @Page
    private ElytronLogsPage page;

    /**
     * @tpTestDetails Try to create File Periodic Rotating Audit Log instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in File Periodic Rotating Audit Log.
     * Validate created resource is present in model.
     * Validate attributes of created resource in model.
     */
    @Test
    public void testAddFilePeriodicRotatingAuditLog() throws Exception {
        final Address auditLogAddress = elyOps.getElytronAddress(FILE_PERIODIC_ROTATING_AUDIT_LOG,
                randomAlphabetic(7));
        final String pathValue = randomAlphabetic(7),
                suffixValue = "%H%m";
        try {
            page.navigateToApplication()
                    .switchSubTab(FILE_PERIODIC_ROTATING_AUDIT_LOG_LABEL);

            page.getResourceManager()
                    .addResource(AddResourceWizard.class)
                    .name(auditLogAddress.getLastPairValue())
                    .text(PATH, pathValue)
                    .text(SUFFIX, suffixValue)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();

            assertTrue(page.getResourceManager().isResourcePresent(auditLogAddress.getLastPairValue()));

            new ResourceVerifier(auditLogAddress, client)
                    .verifyExists()
                    .verifyAttribute(PATH, pathValue)
                    .verifyAttribute(SUFFIX, suffixValue);
        } finally {
            ops.removeIfExists(auditLogAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create File Periodic Rotating Audit Log instance in model and try to remove it in Web Console's
     * Elytron subsystem configuration.
     * Validate the resource is not any more visible in File Periodic Rotating Audit Log table.
     * Validate removed resource is not any more present in the model.
     */
    @Test
    public void testRemoveFilePeriodicRotatingAuditLog() throws Exception {
        final Address auditLogAddress = elyOps.getElytronAddress(FILE_PERIODIC_ROTATING_AUDIT_LOG,
                randomAlphabetic(7));
        createPeriodicRotatingFileAuditLog(auditLogAddress);
        try {
            page.navigateToApplication()
                    .switchSubTab(FILE_PERIODIC_ROTATING_AUDIT_LOG_LABEL);

            page.getResourceManager()
                    .removeResource(auditLogAddress.getLastPairValue())
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();

            assertFalse(page.getResourceManager().isResourcePresent(auditLogAddress.getLastPairValue()));

            new ResourceVerifier(auditLogAddress, client)
                    .verifyDoesNotExist();
        } finally {
            ops.removeIfExists(auditLogAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron File Periodic Rotating Audit Log instance in model and try to edit its format
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void selectFormat() throws Exception {
        final Address auditLogAddress = elyOps.getElytronAddress(FILE_PERIODIC_ROTATING_AUDIT_LOG, randomAlphabetic(7));
        final String formatValue = "JSON";
        try {
            createPeriodicRotatingFileAuditLog(auditLogAddress);

            page.navigateToApplication()
                    .switchSubTab(FILE_PERIODIC_ROTATING_AUDIT_LOG_LABEL);

            page.getResourceManager().selectByName(auditLogAddress.getLastPairValue());

            new ConfigChecker.Builder(client, auditLogAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(SELECT, FORMAT, formatValue)
                    .verifyFormSaved()
                    .verifyAttribute(FORMAT, formatValue);
        } finally {
            ops.removeIfExists(auditLogAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron File Periodic Rotating Audit Log instance in model and try to edit its suffix
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editSuffix() throws Exception {
        final Address auditLogAddress = elyOps.getElytronAddress(FILE_PERIODIC_ROTATING_AUDIT_LOG,
                randomAlphabetic(7));
        final String newSuffixValue = "%m%H";
        createPeriodicRotatingFileAuditLog(auditLogAddress);
        try {
            page.navigateToApplication()
                    .switchSubTab(FILE_PERIODIC_ROTATING_AUDIT_LOG_LABEL);

            page.getResourceManager().selectByName(auditLogAddress.getLastPairValue());

            new ConfigChecker.Builder(client, auditLogAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, SUFFIX, newSuffixValue)
                    .verifyFormSaved()
                    .verifyAttribute(SUFFIX, newSuffixValue);

            new ResourceVerifier(auditLogAddress, client)
                    .verifyAttribute(SUFFIX, newSuffixValue);
        } finally {
            ops.removeIfExists(auditLogAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron File Periodic Rotating Audit Log instance in model and try to edit its path
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editPath() throws Exception {
        final Address auditLogAddress = elyOps.getElytronAddress(FILE_PERIODIC_ROTATING_AUDIT_LOG,
                randomAlphabetic(7));
        final String newPathValue = "%m%H";
        createPeriodicRotatingFileAuditLog(auditLogAddress);
        try {
            page.navigateToApplication()
                    .switchSubTab(FILE_PERIODIC_ROTATING_AUDIT_LOG_LABEL);

            page.getResourceManager().selectByName(auditLogAddress.getLastPairValue());

            new ConfigChecker.Builder(client, auditLogAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, PATH, newPathValue)
                    .verifyFormSaved()
                    .verifyAttribute(PATH, newPathValue);

            new ResourceVerifier(auditLogAddress, client)
                    .verifyAttribute(PATH, newPathValue);
        } finally {
            ops.removeIfExists(auditLogAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron File Periodic Rotating Audit Log instance in model and try to edit its relative-to
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editRelativeTo() throws Exception {
        final Address auditLogAddress = elyOps.getElytronAddress(FILE_PERIODIC_ROTATING_AUDIT_LOG, randomAlphabetic(7));
        final String relativeToValue = randomAlphabetic(7);
        try {
            createPeriodicRotatingFileAuditLog(auditLogAddress);

            page.navigateToApplication()
                    .switchSubTab(FILE_PERIODIC_ROTATING_AUDIT_LOG_LABEL);

            page.getResourceManager().selectByName(auditLogAddress.getLastPairValue());

            new ConfigChecker.Builder(client, auditLogAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, RELATIVE_TO, relativeToValue)
                    .verifyFormSaved()
                    .verifyAttribute(RELATIVE_TO, relativeToValue);
        } finally {
            ops.removeIfExists(auditLogAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron File Periodic Rotating Audit Log instance in model and try to edit its synchronized
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void toggleSynchronized() throws Exception {
        final Address auditLogAddress = elyOps.getElytronAddress(FILE_PERIODIC_ROTATING_AUDIT_LOG, randomAlphabetic(7));
        try {
            createPeriodicRotatingFileAuditLog(auditLogAddress);

            final ModelNodeResult originalModelNodeResult = ops.readAttribute(auditLogAddress, SYNCHRONIZED);
            originalModelNodeResult.assertSuccess();

            page.navigateToApplication()
                    .switchSubTab(FILE_PERIODIC_ROTATING_AUDIT_LOG_LABEL);

            page.getResourceManager().selectByName(auditLogAddress.getLastPairValue());

            new ConfigChecker.Builder(client, auditLogAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(CHECKBOX, SYNCHRONIZED, !originalModelNodeResult.booleanValue())
                    .verifyFormSaved()
                    .verifyAttribute(SYNCHRONIZED, !originalModelNodeResult.booleanValue());

            new ConfigChecker.Builder(client, auditLogAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(CHECKBOX, SYNCHRONIZED, originalModelNodeResult.booleanValue())
                    .verifyFormSaved()
                    .verifyAttribute(SYNCHRONIZED, originalModelNodeResult.booleanValue());

        } finally {
            ops.removeIfExists(auditLogAddress);
            adminOps.reloadIfRequired();
        }
    }

    private void createPeriodicRotatingFileAuditLog(Address address) throws IOException {
        ops.add(address, Values.empty()
            .and(PATH, randomAlphabetic(7))
            .and(SUFFIX, "%H%m"))
            .assertSuccess();
    }
}
