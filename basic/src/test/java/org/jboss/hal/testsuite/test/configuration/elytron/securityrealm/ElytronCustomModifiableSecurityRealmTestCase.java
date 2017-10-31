package org.jboss.hal.testsuite.test.configuration.elytron.securityrealm;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.securityrealm.AddCustomSecurityRealmWizard;
import org.jboss.hal.testsuite.page.config.elytron.SecurityRealmPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.test.configuration.elytron.securityrealm.custommodule.CustomSecurityRealm;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.ModuleUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

@Category(Elytron.class)
@RunWith(Arquillian.class)
@RunAsClient
public class ElytronCustomModifiableSecurityRealmTestCase extends AbstractElytronTestCase {

    @Page
    private SecurityRealmPage page;

    private static final String CUSTOM_MODIFIABLE_REALM = "custom-modifiable-realm";
    private static final String ARCHIVE_NAME = "elytron.securityrealm.custommodifiable.jar";

    private static String customSecurityRealmModuleName;

    private static final Path CUSTOM_SECURITY_REALM_MODULE_PATH = Paths.get("test", "elytron",
            "securityrealm" + randomAlphanumeric(5));
    private static ModuleUtils moduleUtils;

    @BeforeClass
    public static void beforeClass() throws Exception {
        moduleUtils = new ModuleUtils(client);
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, ARCHIVE_NAME).addClasses(CustomSecurityRealm.class);
        customSecurityRealmModuleName = moduleUtils.createModule(CUSTOM_SECURITY_REALM_MODULE_PATH, jar,
                "org.wildfly.extension.elytron", "org.wildfly.security.elytron-private");
    }

    @AfterClass
    public static void afterClass() throws Exception {
        moduleUtils.removeModule(CUSTOM_SECURITY_REALM_MODULE_PATH);
    }

    /**
     * @tpTestDetails Try to create Elytron Custom modifiable security realm instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Custom modifiable security realm table.
     * Validate created resource is present in model.
     * Validate attributes of created resource in model.
     */
    @Test
    public void testAddCustomModifiableSecurityRealm() throws Exception {
        final String securityRealmName = "security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final Address securityRealmAddress = elyOps.getElytronAddress(CUSTOM_MODIFIABLE_REALM, securityRealmName);
        try {
            page.navigate();
            page.switchToCustomModifiableRealms()
                    .getResourceManager()
                    .selectByName(securityRealmName);
            page.getResourceManager()
                    .addResource(AddCustomSecurityRealmWizard.class)
                    .name(securityRealmAddress.getLastPairValue())
                    .className(CustomSecurityRealm.class.getName())
                    .module(customSecurityRealmModuleName)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            Assert.assertTrue("Recently created custom modifiable security realm should be present in the table",
                    page.getResourceManager().isResourcePresent(securityRealmAddress.getLastPairValue()));
            new ResourceVerifier(securityRealmAddress, client).verifyExists();
        } finally {
            ops.removeIfExists(securityRealmAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Custom modifiable security realm instance in model and try to remove it in Web
     * Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Custom modifiable security realm table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void testRemoveCustomModifiableSecurityRealm() throws Exception {
        final String securityRealmName = "security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final Address securityRealmAddress = elyOps.getElytronAddress(CUSTOM_MODIFIABLE_REALM, securityRealmName);
        try {
            createCustomModifiableSecurityRealmInModel(securityRealmAddress);
            page.navigate();
            page.switchToCustomModifiableRealms()
                    .getResourceManager()
                    .removeResource(securityRealmName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            Assert.assertFalse("Newly removed custom modifiable security realm should not be present in the table",
                    page.getResourceManager().isResourcePresent(securityRealmName));
            new ResourceVerifier(securityRealmAddress, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(securityRealmAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Custom modifiable security realm instance in model and try to edit its class-name
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editClassName() throws Exception {
        final String securityRealmName = "security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String value = RandomStringUtils.randomAlphanumeric(7);
        final Address securityRealmAddress = elyOps.getElytronAddress(CUSTOM_MODIFIABLE_REALM, securityRealmName);
        try {
            createCustomModifiableSecurityRealmInModel(securityRealmAddress);
            page.navigate();
            page.switchToCustomModifiableRealms()
                    .getResourceManager()
                    .selectByName(securityRealmAddress.getLastPairValue());

            new ConfigChecker.Builder(client, securityRealmAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, CLASS_NAME, value)
                    .verifyFormSaved()
                    .verifyAttribute(CLASS_NAME, value);
        } finally {
            ops.removeIfExists(securityRealmAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Custom modifiable security realm instance in model and try to edit its module
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editModule() throws Exception {
        final String securityRealmName = "security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String value = RandomStringUtils.randomAlphanumeric(7);
        final Address securityRealmAddress = elyOps.getElytronAddress(CUSTOM_MODIFIABLE_REALM, securityRealmName);
        try {
            createCustomModifiableSecurityRealmInModel(securityRealmAddress);
            page.navigate();
            page.switchToCustomModifiableRealms()
                    .getResourceManager()
                    .selectByName(securityRealmName);
            new ConfigChecker.Builder(client, securityRealmAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, MODULE, value)
                    .verifyFormSaved()
                    .verifyAttribute(MODULE, value);
        } finally {
            ops.removeIfExists(securityRealmAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Custom modifiable security realm instance in model and try to edit its
     * configuration attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editConfiguration() throws Exception {
        final String securityRealmName = "security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String key = RandomStringUtils.randomAlphanumeric(7);
        final String value = RandomStringUtils.randomAlphanumeric(7);
        final Address securityRealmAddress = elyOps.getElytronAddress(CUSTOM_MODIFIABLE_REALM, securityRealmName);
        try {
            createCustomModifiableSecurityRealmInModel(securityRealmAddress);
            page.navigate();
            page.switchToCustomModifiableRealms()
                    .getResourceManager()
                    .selectByName(securityRealmName);
            new ConfigChecker.Builder(client, securityRealmAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, CONFIGURATION, key + "=" + value)
                    .verifyFormSaved()
                    .verifyAttribute(CONFIGURATION, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(key, value).build());
        } finally {
            ops.removeIfExists(securityRealmAddress);
            adminOps.reloadIfRequired();
        }
    }

    private void createCustomModifiableSecurityRealmInModel(Address realmAddress) throws IOException, TimeoutException, InterruptedException {
        ops.add(realmAddress, Values.of(MODULE, customSecurityRealmModuleName)
                .and(CLASS_NAME, CustomSecurityRealm.class.getName())).assertSuccess();
        adminOps.reloadIfRequired();
    }

}
