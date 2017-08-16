package org.jboss.hal.testsuite.test.configuration.elytron.securityrealmmapper;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.securityrealmmapper.AddCustomRealmMapperWizard;
import org.jboss.hal.testsuite.page.config.elytron.SecurityRealmMapperPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.test.configuration.elytron.securityrealmmapper.custommodule.CustomRealmMapper;
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
public class ElytronCustomRealmMapperTestCase extends AbstractElytronTestCase {

    @Page
    private SecurityRealmMapperPage page;

    private static final String
            CUSTOM_REALM_MAPPER = "custom-realm-mapper",
            ARCHIVE_NAME = "customrealmmappertest";

    private static String customRealmMapperModuleName;

    private static final Path CUSTOM_REALM_MAPPER_MODULE_PATH = Paths.get("test", "elytron",
            "securityrealm" + randomAlphanumeric(5));
    private static ModuleUtils moduleUtils;

    @BeforeClass
    public static void beforeClass() throws Exception {
        moduleUtils = new ModuleUtils(client);
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, ARCHIVE_NAME).addClasses(CustomRealmMapper.class);
        customRealmMapperModuleName = moduleUtils.createModule(CUSTOM_REALM_MAPPER_MODULE_PATH, jar,
                "org.wildfly.extension.elytron", "org.wildfly.security.elytron-private");
    }

    @AfterClass
    public static void afterClass() throws Exception {
        moduleUtils.removeModule(CUSTOM_REALM_MAPPER_MODULE_PATH);
    }

    /**
     * @tpTestDetails Try to create Elytron Custom realm mapper in Web Console's Elytron subsystem configuration.
     * Validate created resource is visible in Custom realm mapper table.
     * Validate created resource is present in model.
     * Validate attributes of created resource in model.
     */
    @Test
    public void testAddCustomRealmMapper() throws Exception {
        final Address realmMapperAddress = elyOps.getElytronAddress(CUSTOM_REALM_MAPPER, RandomStringUtils.randomAlphanumeric(7));

        try {
            page.navigate();
            page.switchToCustomRealmMappers()
                    .getResourceManager()
                    .addResource(AddCustomRealmMapperWizard.class)
                    .name(realmMapperAddress.getLastPairValue())
                    .className(CustomRealmMapper.class.getName())
                    .module(customRealmMapperModuleName)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();

            Assert.assertTrue(page.getResourceManager().isResourcePresent(realmMapperAddress.getLastPairValue()));

            new ResourceVerifier(realmMapperAddress, client).verifyExists()
                    .verifyAttribute(CLASS_NAME, CustomRealmMapper.class.getName())
                    .verifyAttribute(MODULE, customRealmMapperModuleName);
        } finally {
            ops.removeIfExists(realmMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Custom realm mapper instance in model and try to remove it in Web Console's
     * Elytron subsystem configuration.
     * Validate the resource is not any more visible in Custom realm mapper table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void testRemoveCustomRealmMapper() throws Exception {
        final Address realmMapperAddress = createCustomRealmMapper();

        try {
            page.navigate();
            page.switchToCustomRealmMappers()
                    .getResourceManager()
                    .removeResource(realmMapperAddress.getLastPairValue())
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();

            Assert.assertFalse(page.getResourceManager().isResourcePresent(realmMapperAddress.getLastPairValue()));

            new ResourceVerifier(realmMapperAddress, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(realmMapperAddress);
            adminOps.reloadIfRequired();
        }

    }

    /**
     * @tpTestDetails Create Elytron Custom realm mapper instance in model and try to edit its class-name attribute
     * value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editClassName() throws Exception {
        final Address realmMapperAddress = createCustomRealmMapper();

        final String value = RandomStringUtils.randomAlphanumeric(7);

        try {
            page.navigate();
            page.switchToCustomRealmMappers()
                    .getResourceManager()
                    .selectByName(realmMapperAddress.getLastPairValue());

            new ConfigChecker.Builder(client, realmMapperAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, CLASS_NAME, value)
                    .verifyFormSaved()
                    .verifyAttribute(CLASS_NAME, value);
        } finally {
            ops.removeIfExists(realmMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Custom realm mapper instance in model and try to edit its module attribute value in
     * Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editModule() throws Exception {
        final Address realmMapperAddress = createCustomRealmMapper();

        final String value = RandomStringUtils.randomAlphanumeric(7);

        try {
            page.navigate();
            page.switchToCustomRealmMappers()
                    .getResourceManager()
                    .selectByName(realmMapperAddress.getLastPairValue());

            new ConfigChecker.Builder(client, realmMapperAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, MODULE, value)
                    .verifyFormSaved()
                    .verifyAttribute(MODULE, value);
        } finally {
            ops.removeIfExists(realmMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Custom realm mapper instance in model and try to edit its configuration attribute
     * value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editConfiguration() throws Exception {
        final Address realmMapperAddress = createCustomRealmMapper();

        final String key = RandomStringUtils.randomAlphanumeric(7),
                value = RandomStringUtils.randomAlphanumeric(7);

        try {
            page.navigate();
            page.switchToCustomRealmMappers()
                    .getResourceManager()
                    .selectByName(realmMapperAddress.getLastPairValue());

            new ConfigChecker.Builder(client, realmMapperAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, CONFIGURATION, key + "=" + value)
                    .verifyFormSaved()
                    .verifyAttribute(CONFIGURATION, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(key, value).build());
        } finally {
            ops.removeIfExists(realmMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    private Address createCustomRealmMapper() throws IOException {
        final Address realmMapperAddress = elyOps.getElytronAddress(CUSTOM_REALM_MAPPER, RandomStringUtils.randomAlphanumeric(7));
        ops.add(realmMapperAddress, Values.of(CLASS_NAME, CustomRealmMapper.class.getName())
                .and(MODULE, customRealmMapperModuleName)).assertSuccess();
        return realmMapperAddress;
    }

}
