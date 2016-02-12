package org.jboss.hal.testsuite.test.configuration.connector;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.AdminObjectWizard;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ConfigPropertyWizard;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ConnectionDefinitionWizard;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ResourceAdapterWizard;
import org.jboss.hal.testsuite.page.config.ResourceAdaptersPage;
import org.jboss.hal.testsuite.util.Console;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import static org.junit.Assert.assertTrue;

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
    private static final String PROPERTY_KEY = "val_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String CONNECTION_DEFINITION_NAME = "cdn_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String CONNECTION_DEFINITION_JNDI_NAME = "java:/cdjn_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String CONNECTION_DEFINITION_CLASS = "cdc_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String ADMIN_OBJECT_NAME = "aon_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String ADMIN_OBJECT_JNDI_NAME = "java:/aojn_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String ADMIN_OBJECT_CLASS = "aoc_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String RESOURCE_ADAPTER = "resource-adapter";

    private static final Address
        subsystemAddress = Address.subsystem("resource-adapters"),
        adapterNoTransAddress = subsystemAddress.and(RESOURCE_ADAPTER, NAME_NO_TRANSACTION),
        adapterLocalTransAddress = subsystemAddress.and(RESOURCE_ADAPTER, NAME_LOCAL_TRANSACTION),
        adapterXaTransAddress = subsystemAddress.and(RESOURCE_ADAPTER, NAME_XA_TRANSACTION),
        propertyAdress = adapterNoTransAddress.and("config-properties", PROPERTY_KEY),
        adminObjectAdress = adapterNoTransAddress.and("admin-objects", ADMIN_OBJECT_NAME),
        connectionDefinitionAdress = adapterNoTransAddress.and("connection-definitions", CONNECTION_DEFINITION_NAME);

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations ops = new Operations(client);
    private final Administration adminOps = new Administration(client);

    @Drone
    public WebDriver browser;

    @Page
    public ResourceAdaptersPage page;

    @Before
    public void before() {
        Console.withBrowser(browser).refreshAndNavigate(ResourceAdaptersPage.class);
    }

    @After
    public void after() throws IOException, InterruptedException, TimeoutException {
        adminOps.reloadIfRequired();
    }

    @AfterClass
    public static void cleanUp() throws IOException, OperationException {
        try {
            ops.removeIfExists(adapterNoTransAddress);
            ops.removeIfExists(adapterXaTransAddress);
            ops.removeIfExists(adapterLocalTransAddress);
        } finally {
            IOUtils.closeQuietly(client);
        }
    }

    @Test
    @InSequence(0)
    public void createNoTransaction() throws Exception {
        ResourceAdapterWizard wizard = page.addResourceAdapter();

        boolean result =
                wizard.name(NAME_NO_TRANSACTION)
                .archive(ARCHIVE)
                .tx(NO_TRANSACTION)
                .finish();

        assertTrue("Window should be closed", result);
        new ResourceVerifier(adapterNoTransAddress, client).verifyExists();

    }

    @Test
    @InSequence(1)
    public void createProperties() throws Exception {
        ResourceVerifier propertyVerifier = new ResourceVerifier(propertyAdress, client);

        page.navigate2ra(NAME_NO_TRANSACTION).switchSubTab("Configuration");
        page.switchConfigAreaTabTo("Properties");

        ConfigPropertyWizard wizard = page.getResourceManager().addResource(ConfigPropertyWizard.class);
        wizard.getEditor().text("name", PROPERTY_KEY);
        boolean result = wizard
                .value(PROPERTY_VALUE)
                .finish();

        assertTrue("Window should be closed", result);
        propertyVerifier.verifyExists();

        page.getResourceManager().removeResource(PROPERTY_KEY).confirmAndDismissReloadRequiredMessage();
        propertyVerifier.verifyDoesNotExist();
    }

    @Test
    @InSequence(2)
    public void addManagedConnectionDefinition() throws Exception {
        ResourceVerifier connectionDefinitionVerifier = new ResourceVerifier(connectionDefinitionAdress, client);

        page.navigate2ra(NAME_NO_TRANSACTION).switchSubTab("Connection Definitions");

        ConnectionDefinitionWizard wizard = page.getResourceManager().addResource(ConnectionDefinitionWizard.class);

        boolean result = wizard
                .name(CONNECTION_DEFINITION_NAME)
                .connectionClass(CONNECTION_DEFINITION_CLASS)
                .finish();

        assertTrue("Window should be closed", result);
        connectionDefinitionVerifier.verifyExists();

        page.getResourceManager().removeResource(CONNECTION_DEFINITION_NAME).confirmAndDismissReloadRequiredMessage();
        connectionDefinitionVerifier.verifyDoesNotExist();
    }

    @Test
    @InSequence(3)
    public void addAdminObject() throws Exception {
        ResourceVerifier adminObjectVerifier = new ResourceVerifier(adminObjectAdress, client);

        page.navigate2ra(NAME_NO_TRANSACTION).switchSubTab("Admin Objects");

        AdminObjectWizard wizard = page.getResourceManager().addResource(AdminObjectWizard.class);
        boolean result = wizard
                .name(ADMIN_OBJECT_NAME)
                .className(ADMIN_OBJECT_CLASS)
                .finish();

        assertTrue("Window should be closed", result);
        adminObjectVerifier.verifyExists();

        page.getResourceManager().removeResource(ADMIN_OBJECT_NAME).confirmAndDismissReloadRequiredMessage();
        adminObjectVerifier.verifyDoesNotExist();
    }


    @Test
    @InSequence(4)
    public void removeResourceAdapter() throws Exception {
        page.removeRa(NAME_NO_TRANSACTION).assertClosed();
        new ResourceVerifier(adapterNoTransAddress, client).verifyDoesNotExist();
    }

    @Test
    public void createLocalTransaction() throws Exception {
        ResourceVerifier resourceVerifier = new ResourceVerifier(adapterLocalTransAddress, client);

        ResourceAdapterWizard wizard = page.addResourceAdapter();

        boolean result =
                wizard.name(NAME_LOCAL_TRANSACTION)
                        .archive(ARCHIVE)
                        .tx(LOCAL_TRANSACTION)
                        .finish();

        assertTrue("Window should be closed", result);
        resourceVerifier.verifyExists();

        page.removeRa(NAME_LOCAL_TRANSACTION).assertClosed();
        resourceVerifier.verifyDoesNotExist();
    }

    @Test
    public void createXATransaction() throws Exception {
        ResourceVerifier resourceVerifier = new ResourceVerifier(adapterXaTransAddress, client);

        ResourceAdapterWizard wizard = page.addResourceAdapter();

        boolean result = wizard
                .name(NAME_XA_TRANSACTION)
                .archive(ARCHIVE)
                .tx(XA_TRANSACTION)
                .finish();

        assertTrue("Window should be closed", result);
        resourceVerifier.verifyExists();

        page.removeRa(NAME_XA_TRANSACTION).assertClosed();
        resourceVerifier.verifyDoesNotExist();
    }
}
