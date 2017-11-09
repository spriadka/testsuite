package org.jboss.hal.testsuite.test.configuration.elytron.securityrealm;

import org.apache.commons.lang.RandomStringUtils;
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

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

@Category(Elytron.class)
@RunWith(Arquillian.class)
public class ElytronCustomSecurityRealmTestCase extends AbstractElytronTestCase {

    @Page
    private SecurityRealmPage page;

    private static final String
            CUSTOM_REALM = "custom-realm",
            ARCHIVE_NAME = "elytron.securityrealm.custom.jar";

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
     * @tpTestDetails Try to create Elytron Custom security realm instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Custom security realm table.
     * Validate created resource is present in model.
     * Validate attributes of created resource in model.
     */
    @Test
    public void testAddCustomSecurityRealm() throws Exception {
        final Address securityRealmAddress = elyOps.getElytronAddress(CUSTOM_REALM, RandomStringUtils.randomAlphanumeric(7));

        page.navigate();
        page.switchToCustomRealms()
                .getResourceManager()
                .selectByName(securityRealmAddress.getLastPairValue());

        try {
            page.getResourceManager()
                    .addResource(AddCustomSecurityRealmWizard.class)
                    .name(securityRealmAddress.getLastPairValue())
                    .className(CustomSecurityRealm.class.getName())
                    .module(customSecurityRealmModuleName)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();

            Assert.assertTrue(page.getResourceManager().isResourcePresent(securityRealmAddress.getLastPairValue()));

            new ResourceVerifier(securityRealmAddress, client).verifyExists();
        } finally {
            ops.removeIfExists(securityRealmAddress);
        }
    }

    /**
     * @tpTestDetails Create Elytron Custom security realm instance in model and try to remove it in Web Console's
     * Elytron subsystem configuration.
     * Validate the resource is not any more visible in Custom security realm table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void testRemoveCustomSecurityRealm() throws Exception {
        final Address securityRealmAddress = createCustomSecurityRealm();

        try {
            page.navigate();
            page.switchToCustomRealms()
                    .getResourceManager()
                    .removeResource(securityRealmAddress.getLastPairValue())
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();

            Assert.assertFalse(page.getResourceManager().isResourcePresent(securityRealmAddress.getLastPairValue()));

            new ResourceVerifier(securityRealmAddress, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(securityRealmAddress);
        }
    }

    /**
     * @tpTestDetails Create Elytron Custom security realm instance in model and try to edit its class-name attribute
     * value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editClassName() throws Exception {
        final Address securityRealmAddress = createCustomSecurityRealm();

        final String value = RandomStringUtils.randomAlphanumeric(7);

        try {
            page.navigate();
            page.switchToCustomRealms()
                    .getResourceManager()
                    .selectByName(securityRealmAddress.getLastPairValue());

            new ConfigChecker.Builder(client, securityRealmAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, CLASS_NAME, value)
                    .verifyFormSaved()
                    .verifyAttribute(CLASS_NAME, value);
        } finally {
            ops.removeIfExists(securityRealmAddress);
        }
    }

    /**
     * @tpTestDetails Create Elytron Custom security realm instance in model and try to edit its module attribute value
     * in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editModule() throws Exception {
        final Address securityRealmAddress = createCustomSecurityRealm();

        final String value = RandomStringUtils.randomAlphanumeric(7);

        try {
            page.navigate();
            page.switchToCustomRealms()
                    .getResourceManager()
                    .selectByName(securityRealmAddress.getLastPairValue());

            new ConfigChecker.Builder(client, securityRealmAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, MODULE, value)
                    .verifyFormSaved()
                    .verifyAttribute(MODULE, value);
        } finally {
            ops.removeIfExists(securityRealmAddress);
        }
    }

    /**
     * @tpTestDetails Create Elytron Custom security realm instance in model and try to edit its configuration attribute
     * value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editConfiguration() throws Exception {
        final Address securityRealmAddress = createCustomSecurityRealm();

        final String key = RandomStringUtils.randomAlphanumeric(7),
                value = RandomStringUtils.randomAlphanumeric(7);

        try {
            page.navigate();
            page.switchToCustomRealms()
                    .getResourceManager()
                    .selectByName(securityRealmAddress.getLastPairValue());

            new ConfigChecker.Builder(client, securityRealmAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, CONFIGURATION, key + "=" + value)
                    .verifyFormSaved()
                    .verifyAttribute(CONFIGURATION, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(key, value).build());
        } finally {
            ops.removeIfExists(securityRealmAddress);
        }
    }

    private Address createCustomSecurityRealm() throws IOException {
        final Address realmAddress = elyOps.getElytronAddress(CUSTOM_REALM, RandomStringUtils.randomAlphanumeric(7));
        ops.add(realmAddress, Values.of(MODULE, customSecurityRealmModuleName)
                .and(CLASS_NAME, CustomSecurityRealm.class.getName())).assertSuccess();
        return realmAddress;
    }

}
