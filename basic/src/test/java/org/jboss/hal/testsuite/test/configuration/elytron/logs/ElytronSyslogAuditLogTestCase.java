package org.jboss.hal.testsuite.test.configuration.elytron.logs;

import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.AddResourceWizard;
import org.jboss.hal.testsuite.page.config.elytron.ElytronLogsPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.AvailablePortFinder;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronOperations.CLIENT_SSL_CONTEXT;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronOperations.KEY_MANAGER;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronOperations.KEY_STORE;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.SELECT;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;

@RunWith(Arquillian.class)
public class ElytronSyslogAuditLogTestCase extends AbstractElytronTestCase {

    private static final String
            FORMAT = "format",
            HOST_NAME = "host-name",
            PORT = "port",
            SERVER_ADDRESS = "server-address",
            SYSLOG_AUDIT_LOG = "syslog-audit-log",
            SYSLOG_AUDIT_LOG_LABEL = "Syslog Audit Log",
            SSL_CONTEXT = "ssl-context",
            TRANSPORT = "transport";

    @Page
    private ElytronLogsPage page;

    @Test
    public void testAddSyslogAuditLog() throws Exception {
        final Address syslogAuditLogAddress = elyOps.getElytronAddress(SYSLOG_AUDIT_LOG, randomAlphabetic(7));
        try {
            page.navigateToApplication()
                    .switchSubTab(SYSLOG_AUDIT_LOG_LABEL);

            page.getResourceManager()
                    .selectByName(syslogAuditLogAddress.getLastPairValue());

            page.getResourceManager()
                    .addResource(AddResourceWizard.class)
                    .name(syslogAuditLogAddress.getLastPairValue())
                    .text(HOST_NAME, "localhost")
                    .text(SERVER_ADDRESS, "localhost")
                    .text(PORT, String.valueOf(AvailablePortFinder.getNextAvailableNonPrivilegedPort()))
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();

            Assert.assertTrue(page.getResourceManager()
                    .isResourcePresent(syslogAuditLogAddress.getLastPairValue()));

            new ResourceVerifier(syslogAuditLogAddress, client).verifyExists();
        } finally {
            ops.removeIfExists(syslogAuditLogAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void testRemoveSyslogAuditLog() throws Exception {
        final Address syslogAuditLogAddress = elyOps.getElytronAddress(SYSLOG_AUDIT_LOG, randomAlphabetic(7));
        try {
            createSyslogAuditLog(syslogAuditLogAddress);

            page.navigateToApplication()
                    .switchSubTab(SYSLOG_AUDIT_LOG_LABEL);

            page.getResourceManager()
                    .selectByName(syslogAuditLogAddress.getLastPairValue());

            page.getResourceManager()
                    .removeResource(syslogAuditLogAddress.getLastPairValue())
                    .confirmAndDismissReloadRequiredMessage();

            Assert.assertFalse(page.getResourceManager()
                    .isResourcePresent(syslogAuditLogAddress.getLastPairValue()));

            new ResourceVerifier(syslogAuditLogAddress, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(syslogAuditLogAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void selectFormat() throws Exception {
        final Address syslogAuditLogAddress = elyOps.getElytronAddress(SYSLOG_AUDIT_LOG, randomAlphabetic(7));
        final String formatValue = "JSON";
        try {
            createSyslogAuditLog(syslogAuditLogAddress);

            page.navigateToApplication()
                    .switchSubTab(SYSLOG_AUDIT_LOG_LABEL);

            page.getResourceManager()
                    .selectByName(syslogAuditLogAddress.getLastPairValue());

            new ConfigChecker.Builder(client, syslogAuditLogAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(SELECT, FORMAT, formatValue)
                    .verifyFormSaved()
                    .verifyAttribute(FORMAT, formatValue);
        } finally {
            ops.removeIfExists(syslogAuditLogAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editHostname() throws Exception {
        final Address syslogAuditLogAddress = elyOps.getElytronAddress(SYSLOG_AUDIT_LOG, randomAlphabetic(7));
        final String hostnameValue = randomAlphabetic(7);
        try {
            createSyslogAuditLog(syslogAuditLogAddress);

            page.navigateToApplication()
                    .switchSubTab(SYSLOG_AUDIT_LOG_LABEL);

            page.getResourceManager()
                    .selectByName(syslogAuditLogAddress.getLastPairValue());

            new ConfigChecker.Builder(client, syslogAuditLogAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, HOST_NAME, hostnameValue)
                    .verifyFormSaved()
                    .verifyAttribute(HOST_NAME, hostnameValue);
        } finally {
            ops.removeIfExists(syslogAuditLogAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editPort() throws Exception {
        final Address syslogAuditLogAddress = elyOps.getElytronAddress(SYSLOG_AUDIT_LOG, randomAlphabetic(7));
        final int portValue = AvailablePortFinder.getNextAvailableNonPrivilegedPort();
        try {
            createSyslogAuditLog(syslogAuditLogAddress);

            page.navigateToApplication()
                    .switchSubTab(SYSLOG_AUDIT_LOG_LABEL);

            page.getResourceManager()
                    .selectByName(syslogAuditLogAddress.getLastPairValue());

            new ConfigChecker.Builder(client, syslogAuditLogAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, PORT, portValue)
                    .verifyFormSaved()
                    .verifyAttribute(PORT, portValue);
        } finally {
            ops.removeIfExists(syslogAuditLogAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editServerAddress() throws Exception {
        final Address syslogAuditLogAddress = elyOps.getElytronAddress(SYSLOG_AUDIT_LOG, randomAlphabetic(7));
        final String serverAddressValue = randomAlphabetic(7);
        try {
            createSyslogAuditLog(syslogAuditLogAddress);

            page.navigateToApplication()
                    .switchSubTab(SYSLOG_AUDIT_LOG_LABEL);

            page.getResourceManager()
                    .selectByName(syslogAuditLogAddress.getLastPairValue());

            new ConfigChecker.Builder(client, syslogAuditLogAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, SERVER_ADDRESS, serverAddressValue)
                    .verifyFormSaved()
                    .verifyAttribute(SERVER_ADDRESS, serverAddressValue);
        } finally {
            ops.removeIfExists(syslogAuditLogAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editSSLContext() throws Exception {
        final Address syslogAuditLogAddress = elyOps.getElytronAddress(SYSLOG_AUDIT_LOG, randomAlphabetic(7)),
                sslContextAddress = elyOps.getElytronAddress(CLIENT_SSL_CONTEXT, randomAlphabetic(7)),
                keyManagerAddress = elyOps.getElytronAddress(KEY_MANAGER, randomAlphabetic(7)),
                keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, randomAlphabetic(7));
        try {
            createSyslogAuditLog(syslogAuditLogAddress);

            elyOps.createKeyStore(keyStoreAddress);
            elyOps.createKeyManager(keyManagerAddress, keyStoreAddress);
            elyOps.createSSLContext(sslContextAddress, keyManagerAddress);

            page.navigateToApplication()
                    .switchSubTab(SYSLOG_AUDIT_LOG_LABEL);

            page.getResourceManager()
                    .selectByName(syslogAuditLogAddress.getLastPairValue());

            new ConfigChecker.Builder(client, syslogAuditLogAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, SSL_CONTEXT, sslContextAddress.getLastPairValue())
                    .verifyFormSaved()
                    .verifyAttribute(SSL_CONTEXT, sslContextAddress.getLastPairValue());
        } finally {
            ops.removeIfExists(syslogAuditLogAddress);
            ops.removeIfExists(sslContextAddress);
            ops.removeIfExists(keyManagerAddress);
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void selectTransport() throws Exception {
        final Address syslogAuditLogAddress = elyOps.getElytronAddress(SYSLOG_AUDIT_LOG, randomAlphabetic(7));
        final String formatValue = "UDP";
        try {
            createSyslogAuditLog(syslogAuditLogAddress);

            page.navigateToApplication()
                    .switchSubTab(SYSLOG_AUDIT_LOG_LABEL);

            page.getResourceManager()
                    .selectByName(syslogAuditLogAddress.getLastPairValue());

            new ConfigChecker.Builder(client, syslogAuditLogAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(SELECT, TRANSPORT, formatValue)
                    .verifyFormSaved()
                    .verifyAttribute(TRANSPORT, formatValue);
        } finally {
            ops.removeIfExists(syslogAuditLogAddress);
            adminOps.reloadIfRequired();
        }
    }

    private void createSyslogAuditLog(Address address) throws IOException {
        ops.add(address, Values.empty()
                .and(PORT, AvailablePortFinder.getNextAvailableNonPrivilegedPort())
                .and(SERVER_ADDRESS, "localhost")
                .and(HOST_NAME, "localhost"))
                .assertSuccess();
    }
}
