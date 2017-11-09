package org.jboss.hal.testsuite.test.configuration.connector;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.AdminObjectWizard;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ConfigPropertyWizard;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ConnectionDefinitionWizard;
import org.jboss.hal.testsuite.page.config.ResourceAdaptersPage;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class ResourceAdaptersTestCase {

    private static final String NAME_LOCAL_TRANSACTION = "ran_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String NAME_NO_TRANSACTION = "ran_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String NAME_XA_TRANSACTION = "ran_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String ARCHIVE = "ran_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String NO_TRANSACTION = "NoTransaction";
    private static final String LOCAL_TRANSACTION = "LocalTransaction";
    private static final String XA_TRANSACTION = "XATransaction";
    private static final String PROPERTY_VALUE = "prop_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String PROPERTY_NAME = "val_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String CONNECTION_DEFINITION_NAME = "cdn_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String CONNECTION_DEFINITION_CLASS = "cdc_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String ADMIN_OBJECT_NAME = "aon_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String ADMIN_OBJECT_CLASS = "aoc_" + RandomStringUtils.randomAlphanumeric(5);

    private static final String RESOURCE_ADAPTER = "resource-adapter";
    private static final String ARCHIVE_ATTRIBUTE = "archive";
    private static final String TRANSACTION_ATTRIBUTE = "transaction";
    private static final String CONFIG_PROPERTIES_ATTRIBUTE = "config-properties";
    private static final String ADMIN_OBJECTS_ATTRIBUTE = "admin-objects";
    private static final String CONNECTION_DEFINITIONS_ATTRIBUTE = "connection-definitions";

    private static final Address
            subsystemAddress = Address.subsystem("resource-adapters"),
            NO_TRANSACTION_ADAPTER_ADDRESS = subsystemAddress.and(RESOURCE_ADAPTER, NAME_NO_TRANSACTION),
            LOCAL_TRANSACTION_ADAPTER_ADDRESS = subsystemAddress.and(RESOURCE_ADAPTER, NAME_LOCAL_TRANSACTION),
            XA_TRANSACTION_ADAPTER_ADDRESS = subsystemAddress.and(RESOURCE_ADAPTER, NAME_XA_TRANSACTION);

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations ops = new Operations(client);
    private final Administration adminOps = new Administration(client);

    @Drone
    public WebDriver browser;

    @Page
    public ResourceAdaptersPage page;

    @AfterClass
    public static void cleanUp() throws IOException, OperationException {
        try {
            ops.removeIfExists(NO_TRANSACTION_ADAPTER_ADDRESS);
            ops.removeIfExists(XA_TRANSACTION_ADAPTER_ADDRESS);
            ops.removeIfExists(LOCAL_TRANSACTION_ADAPTER_ADDRESS);
        } finally {
            IOUtils.closeQuietly(client);
        }
    }

    @Test
    public void createNoTransaction() throws Exception {
        try {
            page.addResourceAdapter()
                    .name(NAME_NO_TRANSACTION)
                    .archive(ARCHIVE)
                    .tx(NO_TRANSACTION)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            new ResourceVerifier(NO_TRANSACTION_ADAPTER_ADDRESS, client).verifyExists();
        } finally {
            ops.removeIfExists(NO_TRANSACTION_ADAPTER_ADDRESS);
            adminOps.reloadIfRequired();
        }

    }

    @Test
    public void createProperties() throws Exception {
        final Address configPropertyAddress = NO_TRANSACTION_ADAPTER_ADDRESS.and(CONFIG_PROPERTIES_ATTRIBUTE, PROPERTY_NAME);
        try {
            createResourceAdapterInModel(NO_TRANSACTION_ADAPTER_ADDRESS);
            ResourceVerifier propertyVerifier = new ResourceVerifier(configPropertyAddress, client);
            page.navigateToResourceAdapter(NAME_NO_TRANSACTION).switchSubTab("Configuration");
            page.switchConfigAreaTabTo("Properties");
            page.getResourceManager()
                .addResource(ConfigPropertyWizard.class)
                .name(PROPERTY_NAME)
                .value(PROPERTY_VALUE)
                .saveAndDismissReloadRequiredWindowWithState()
                .assertWindowClosed();
            propertyVerifier.verifyExists();
            page.getResourceManager().removeResource(PROPERTY_NAME).confirmAndDismissReloadRequiredMessage();
            propertyVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(NO_TRANSACTION_ADAPTER_ADDRESS);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addManagedConnectionDefinition() throws Exception {
        final Address connectionDefinitionAddress = NO_TRANSACTION_ADAPTER_ADDRESS
                .and(CONNECTION_DEFINITIONS_ATTRIBUTE, CONNECTION_DEFINITION_NAME);
        try {
            createResourceAdapterInModel(NO_TRANSACTION_ADAPTER_ADDRESS);
            ResourceVerifier connectionDefinitionVerifier = new ResourceVerifier(connectionDefinitionAddress, client);

            page.navigateToResourceAdapter(NAME_NO_TRANSACTION).switchSubTab("Connection Definitions");
            page.getResourceManager()
                    .addResource(ConnectionDefinitionWizard.class)
                    .name(CONNECTION_DEFINITION_NAME)
                    .connectionClass(CONNECTION_DEFINITION_CLASS)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            connectionDefinitionVerifier.verifyExists();
            page.getResourceManager().removeResource(CONNECTION_DEFINITION_NAME).confirmAndDismissReloadRequiredMessage();
            connectionDefinitionVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(NO_TRANSACTION_ADAPTER_ADDRESS);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addAdminObject() throws Exception {
        final Address adminObjectAddress = NO_TRANSACTION_ADAPTER_ADDRESS.and(ADMIN_OBJECTS_ATTRIBUTE, ADMIN_OBJECT_NAME);
        try {
            createResourceAdapterInModel(NO_TRANSACTION_ADAPTER_ADDRESS);
            ResourceVerifier adminObjectVerifier = new ResourceVerifier(adminObjectAddress, client);
            page.navigateToResourceAdapter(NAME_NO_TRANSACTION).switchSubTab("Admin Objects");
            page.getResourceManager().addResource(AdminObjectWizard.class)
                    .name(ADMIN_OBJECT_NAME)
                    .className(ADMIN_OBJECT_CLASS)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            adminObjectVerifier.verifyExists();
            page.getResourceManager().removeResource(ADMIN_OBJECT_NAME).confirmAndDismissReloadRequiredMessage();
            adminObjectVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(NO_TRANSACTION_ADAPTER_ADDRESS);
            adminOps.reloadIfRequired();
        }
    }


    @Test
    public void removeResourceAdapter() throws Exception {
        try {
            createResourceAdapterInModel(NO_TRANSACTION_ADAPTER_ADDRESS);
            page.removeResourceAdapter(NAME_NO_TRANSACTION).assertClosed();
            new ResourceVerifier(NO_TRANSACTION_ADAPTER_ADDRESS, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(NO_TRANSACTION_ADAPTER_ADDRESS);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void createLocalTransaction() throws Exception {
        try {
            ResourceVerifier resourceVerifier = new ResourceVerifier(LOCAL_TRANSACTION_ADAPTER_ADDRESS, client);
            page.addResourceAdapter()
                    .name(NAME_LOCAL_TRANSACTION)
                    .archive(ARCHIVE)
                    .tx(LOCAL_TRANSACTION)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            resourceVerifier.verifyExists();
            page.removeResourceAdapter(NAME_LOCAL_TRANSACTION).assertClosed();
            resourceVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(LOCAL_TRANSACTION_ADAPTER_ADDRESS);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void createXATransaction() throws Exception {
        try {
            ResourceVerifier resourceVerifier = new ResourceVerifier(XA_TRANSACTION_ADAPTER_ADDRESS, client);
            page.addResourceAdapter()
                    .name(NAME_XA_TRANSACTION)
                    .archive(ARCHIVE)
                    .tx(XA_TRANSACTION)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            resourceVerifier.verifyExists();
            page.removeResourceAdapter(NAME_XA_TRANSACTION).assertClosed();
            resourceVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(XA_TRANSACTION_ADAPTER_ADDRESS);
            adminOps.reloadIfRequired();
        }
    }

    private void createResourceAdapterInModel(Address address) throws IOException, TimeoutException, InterruptedException {
        ops.add(address, Values.of(ARCHIVE_ATTRIBUTE, ARCHIVE).and(TRANSACTION_ATTRIBUTE, NO_TRANSACTION));
        adminOps.reloadIfRequired();
    }
}
