package org.jboss.hal.testsuite.test.configuration.connector;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.AdminObjectWizard;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ConfigPropertiesFragment;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ConfigPropertyWizard;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ConnectionDefinitionWizard;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ResourceAdapterWizard;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ResourceAdaptersConfigArea;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ResourceAdaptersFragment;
import org.jboss.hal.testsuite.page.config.ResourceAdaptersPage;
import org.jboss.hal.testsuite.test.category.Standalone;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.ResourceVerifier;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.jboss.hal.testsuite.cli.CliConstants.RESOURCE_ADAPTER_ADDRESS;
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
    private static final String PROPERTY_VALUE = "prop_"+ RandomStringUtils.randomAlphanumeric(5);
    private static final String PROPERTY_KEY = "val_"+ RandomStringUtils.randomAlphanumeric(5);
    private static final String CONNECTION_DEFINITION_NAME = "cdn_"+ RandomStringUtils.randomAlphanumeric(5);
    private static final String CONNECTION_DEFINITION_JNDI_NAME = "java:/cdjn_"+ RandomStringUtils.randomAlphanumeric(5);
    private static final String CONNECTION_DEFINITION_CLASS = "cdc_"+ RandomStringUtils.randomAlphanumeric(5);
    private static final String ADMIN_OBJECT_NAME = "aon_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String ADMIN_OBJECT_JNDI_NAME = "java:/aojn_"+ RandomStringUtils.randomAlphanumeric(5);
    private static final String ADMIN_OBJECT_CLASS = "aoc_"+ RandomStringUtils.randomAlphanumeric(5);

    private static final String DMR_ADAPTER_NO = RESOURCE_ADAPTER_ADDRESS + "=" + NAME_NO_TRANSACTION;
    private static final String DMR_ADAPTER_LOCAL = RESOURCE_ADAPTER_ADDRESS + "=" + NAME_LOCAL_TRANSACTION;
    private static final String DMR_ADAPTER_XA = RESOURCE_ADAPTER_ADDRESS + "=" + NAME_XA_TRANSACTION;
    private static final String DMR_PROPERTY = DMR_ADAPTER_NO + "/config-properties=" + PROPERTY_KEY;
    private static final String DMR_ADMIN_OBJ= DMR_ADAPTER_NO + "/admin-objects=" + ADMIN_OBJECT_NAME;
    private static final String DMR_CON_DEF = DMR_ADAPTER_NO + "/connection-definitions=" + CONNECTION_DEFINITION_NAME;

    private static CliClient client = CliClientFactory.getClient();
    private static ResourceVerifier verifier = new ResourceVerifier(DMR_ADAPTER_NO, client);

    @Drone
    public WebDriver browser;

    @Page
    public ResourceAdaptersPage page;

    @Before
    public void before(){
        Console.withBrowser(browser).refreshAndNavigate(ResourceAdaptersPage.class);
    }

    @After
    public void after(){
        client.reload(false);
    }

    @AfterClass
    public static void cleanUp(){
        client.removeResource(DMR_ADAPTER_NO);
        client.removeResource(DMR_ADAPTER_XA);
        client.removeResource(DMR_ADAPTER_LOCAL);
    }

    @Test
    @InSequence(0)
    public void createNoTransaction() {
        ResourceAdaptersFragment fragment = page.getContent();
        ResourceAdapterWizard wizard = fragment.addResourceAdapter();

        boolean result =
                wizard.name(NAME_NO_TRANSACTION)
                .archive(ARCHIVE)
                .tx(NO_TRANSACTION)
                .finish();

        assertTrue("Window should be closed", result);
        assertTrue("Resource adapter should be present in table", fragment.resourceIsPresent(NAME_NO_TRANSACTION));
        verifier.verifyResource(true);

    }

    @Test
    @InSequence(4)
    public void removeResourceAdapter(){
        ResourceAdaptersFragment fragment = page.getContent();

        fragment.removeResourceAdapter(NAME_NO_TRANSACTION);

        verifier.verifyResource(DMR_ADAPTER_NO, false);
    }

    @Test
    @InSequence(1)
    public void createProperties() {
        ResourceAdaptersConfigArea area = page.getConfigArea();
        ResourceAdaptersFragment content = page.getContent();
        ConfigPropertiesFragment fragment = area.switchToProperty();
        ConfigPropertyWizard wizard = fragment.addProperty();

        content.selectResourceAdapter(NAME_NO_TRANSACTION);
        boolean result =
                wizard.name(PROPERTY_KEY)
                .value(PROPERTY_VALUE)
                .finish();

        assertTrue("Window should be closed", result);
        assertTrue("Admin object should be present in table", fragment.resourceIsPresent(PROPERTY_KEY));
        verifier.verifyResource(DMR_PROPERTY, true);

        fragment.removeProperty(PROPERTY_KEY);

        verifier.verifyResource(DMR_PROPERTY, false);
    }

    @Test
    @InSequence(2)
    public void addManagedConnectionDefinition(){
        page.getResourceManager().viewByName(NAME_NO_TRANSACTION);
        page.switchSubTab("Connection Definitions");
        ConnectionDefinitionWizard wizard = page.getResourceManager().addResource(ConnectionDefinitionWizard.class);

        boolean result =
                wizard.name(CONNECTION_DEFINITION_NAME)
                .jndiName(CONNECTION_DEFINITION_JNDI_NAME)
                .connectionClass(CONNECTION_DEFINITION_CLASS)
                .finish();

        assertTrue("Window should be closed", result);
        verifier.verifyResource(DMR_CON_DEF, true);

        page.getResourceManager().removeResourceAndConfirm(CONNECTION_DEFINITION_NAME);

        verifier.verifyResource(DMR_CON_DEF, false);
    }

    @Test
    @InSequence(3)
    public void addAdminObject(){
        page.getResourceManager().viewByName(NAME_NO_TRANSACTION);
        page.switchSubTab("Admin Objects");
        AdminObjectWizard wizard = page.getResourceManager().addResource(AdminObjectWizard.class);

        boolean result =
                wizard.name(ADMIN_OBJECT_NAME)
                .jndiName(ADMIN_OBJECT_JNDI_NAME)
                .className(ADMIN_OBJECT_CLASS)
                .finish();

        assertTrue("Window should be closed", result);
        verifier.verifyResource(DMR_ADMIN_OBJ, true);

        page.getResourceManager().removeResourceAndConfirm(ADMIN_OBJECT_NAME);
        verifier.verifyResource(DMR_ADMIN_OBJ, false);
    }



    @Test
    public void createLocalTransaction(){
        ResourceAdaptersFragment fragment = page.getContent();
        ResourceAdapterWizard wizard = fragment.addResourceAdapter();

        boolean result =
                wizard.name(NAME_LOCAL_TRANSACTION)
                        .archive(ARCHIVE)
                        .tx(LOCAL_TRANSACTION)
                        .finish();

        assertTrue("Window should be closed", result);
        assertTrue("Resource adapter should be present in table", fragment.resourceIsPresent(NAME_LOCAL_TRANSACTION));
        verifier.verifyResource(DMR_ADAPTER_LOCAL, true);

        fragment.removeResourceAdapter(NAME_LOCAL_TRANSACTION);
        verifier.verifyResource(DMR_ADAPTER_LOCAL, false);
    }

    @Test
    public void createXATransaction(){
        ResourceAdaptersFragment fragment = page.getContent();
        ResourceAdapterWizard wizard = fragment.addResourceAdapter();

        boolean result =
                wizard.name(NAME_XA_TRANSACTION)
                        .archive(ARCHIVE)
                        .tx(XA_TRANSACTION)
                        .finish();


        assertTrue("Window should be closed", result);
        assertTrue("Admin object should be present in table", fragment.resourceIsPresent(NAME_XA_TRANSACTION));
        verifier.verifyResource(DMR_ADAPTER_XA, true);

        fragment.removeResourceAdapter(NAME_XA_TRANSACTION);

        verifier.verifyResource(DMR_ADAPTER_XA, false);
    }
}
