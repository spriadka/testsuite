package org.jboss.hal.testsuite.test.configuration.elytron.role.mapper;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.SELECT;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator.ModelNodeListBuilder;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator.ModelNodePropertiesBuilder;
import org.jboss.hal.testsuite.fragment.config.AddResourceWizard;
import org.jboss.hal.testsuite.page.config.elytron.MapperDecoderPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.ModuleUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

@RunWith(Arquillian.class)
@Category(Elytron.class)
public class ElytronRoleMapperTestCase extends AbstractElytronTestCase {

    private static final String
        ADD_PREFIX_ROLE_MAPPER = "add-prefix-role-mapper",
        ADD_PREFIX_ROLE_MAPPER_LABEL = "Add Prefix Role Mapper",
        PREFIX = "prefix",
        ADD_SUFFIX_ROLE_MAPPER = "add-suffix-role-mapper",
        ADD_SUFFIX_ROLE_MAPPER_LABEL = "Add Suffix Role Mapper",
        SUFFIX = "suffix",
        AGGREGATE_ROLE_MAPPER = "aggregate-role-mapper",
        AGGREGATE_ROLE_MAPPER_LABEL = "Aggregate Role Mapper",
        ROLE_MAPPERS = "role-mappers",
        CONSTANT_ROLE_MAPPER = "constant-role-mapper",
        CONSTANT_ROLE_MAPPER_LABEL = "Constant Role Mapper",
        ROLES = "roles",
        ARCHIVE_NAME = "elytron.customer.role.mapper.jar",
        CUSTOM_ROLE_MAPPER = "custom-role-mapper",
        CUSTOM_ROLE_MAPPER_LABEL = "Custom Role Mapper",
        LOGICAL_ROLE_MAPPER = "logical-role-mapper",
        LOGICAL_ROLE_MAPPER_LABEL = "Logical Role Mapper",
        LOGICAL_OPERATION = "logical-operation",
        LEFT = "left",
        RIGHT = "right",
        AND = "and",
        XOR = "xor";

    private static final Path CUSTOM_ROLE_MAPPER_PATH = Paths.get("test", "elytron",
            "role", "mapper" + randomAlphanumeric(5));

    private static String customRoleMapperModuleName;

    private static ModuleUtils moduleUtils;

    @Page
    private MapperDecoderPage page;

    @BeforeClass
    public static void beforeClass() throws Exception {
        moduleUtils = new ModuleUtils(client);
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, ARCHIVE_NAME);
        jar.addClasses(UppercaseCustomRoleMapper.class, LowercaseCustomRoleMapper.class);
        customRoleMapperModuleName = moduleUtils.createModule(CUSTOM_ROLE_MAPPER_PATH, jar,
                "org.wildfly.extension.elytron", "org.wildfly.security.elytron-private");
    }

    @AfterClass
    public static void afterClass() throws Exception {
        moduleUtils.removeModule(CUSTOM_ROLE_MAPPER_PATH);
    }

    /**
     * @tpTestDetails Try to create Elytron Add Prefix Role Mapper instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Add Prefix Role Mapper table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addAddPrefixRoleMapperTest() throws Exception {
        String
            addPrefixRoleMapperName = randomAlphanumeric(5),
            prefixValue = randomAlphanumeric(5);
        Address addPrefixRoleMapperAddress = elyOps.getElytronAddress(ADD_PREFIX_ROLE_MAPPER, addPrefixRoleMapperName);

        try {
            page.navigateToRoleMapper()
                .selectResource(ADD_PREFIX_ROLE_MAPPER_LABEL)
                .getResourceManager()
                .addResource(AddResourceWizard.class)
                .name(addPrefixRoleMapperName)
                .text(PREFIX, prefixValue)
                .saveWithState()
                .assertWindowClosed();

            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(addPrefixRoleMapperName));
            new ResourceVerifier(addPrefixRoleMapperAddress, client).verifyExists()
                    .verifyAttribute(PREFIX, prefixValue);
        } finally {
            ops.removeIfExists(addPrefixRoleMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Add Prefix Role Mapper instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editAddPrefixRoleMapperAttributesTest() throws Exception {
        String
            addPrefixRoleMapperName = randomAlphanumeric(5),
            originalPrefixValue = randomAlphanumeric(5),
            newPrefixValue = randomAlphanumeric(5);
        Address addPrefixRoleMapperAddress =
                elyOps.getElytronAddress(ADD_PREFIX_ROLE_MAPPER, addPrefixRoleMapperName);

        try {
            ops.add(addPrefixRoleMapperAddress, Values.of(PREFIX, originalPrefixValue)).assertSuccess();

            page.navigateToRoleMapper().selectResource(ADD_PREFIX_ROLE_MAPPER_LABEL).getResourceManager()
                    .selectByName(addPrefixRoleMapperName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, addPrefixRoleMapperAddress)
                .configFragment(page.getConfigFragment())
                .editAndSave(TEXT, PREFIX, newPrefixValue)
                .verifyFormSaved()
                .verifyAttribute(PREFIX, newPrefixValue);
        } finally {
            ops.removeIfExists(addPrefixRoleMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Add Prefix Role Mapper instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Add Prefix Role Mapper table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeAddPrefixRoleMapperTest() throws Exception {
        String
            addPrefixRoleMapperName = randomAlphanumeric(5),
            prefixValue = randomAlphanumeric(5);
        Address addPrefixRoleMapperAddress = elyOps.getElytronAddress(ADD_PREFIX_ROLE_MAPPER, addPrefixRoleMapperName);
        ResourceVerifier addPrefixRoleMapperVerifier = new ResourceVerifier(addPrefixRoleMapperAddress, client);

        try {
            ops.add(addPrefixRoleMapperAddress, Values.of(PREFIX, prefixValue)).assertSuccess();
            addPrefixRoleMapperVerifier.verifyExists();

            page.navigateToRoleMapper().selectResource(ADD_PREFIX_ROLE_MAPPER_LABEL).getResourceManager()
                    .removeResource(addPrefixRoleMapperName).confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(addPrefixRoleMapperName));
            addPrefixRoleMapperVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(addPrefixRoleMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to create Elytron Add Suffix Role Mapper instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Add Suffix Role Mapper table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addAddSuffixRoleMapperTest() throws Exception {
        String
            addSuffixRoleMapperName = randomAlphanumeric(5),
            suffixValue = randomAlphanumeric(5);
        Address addSuffixRoleMapperAddress = elyOps.getElytronAddress(ADD_SUFFIX_ROLE_MAPPER, addSuffixRoleMapperName);

        try {
            page.navigateToRoleMapper()
                .selectResource(ADD_SUFFIX_ROLE_MAPPER_LABEL)
                .getResourceManager()
                .addResource(AddResourceWizard.class)
                .name(addSuffixRoleMapperName)
                .text(SUFFIX, suffixValue)
                .saveWithState()
                .assertWindowClosed();

            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(addSuffixRoleMapperName));
            new ResourceVerifier(addSuffixRoleMapperAddress, client).verifyExists()
                    .verifyAttribute(SUFFIX, suffixValue);
        } finally {
            ops.removeIfExists(addSuffixRoleMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Add Suffix Role Mapper instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editAddSuffixRoleMapperAttributesTest() throws Exception {
        String
            addSuffixRoleMapperName = randomAlphanumeric(5),
            originalSuffixValue = randomAlphanumeric(5),
            newSuffixValue = randomAlphanumeric(5);
        Address addSuffixRoleMapperAddress =
                elyOps.getElytronAddress(ADD_SUFFIX_ROLE_MAPPER, addSuffixRoleMapperName);

        try {
            ops.add(addSuffixRoleMapperAddress, Values.of(SUFFIX, originalSuffixValue)).assertSuccess();

            page.navigateToRoleMapper().selectResource(ADD_SUFFIX_ROLE_MAPPER_LABEL).getResourceManager()
                    .selectByName(addSuffixRoleMapperName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, addSuffixRoleMapperAddress)
                .configFragment(page.getConfigFragment())
                .editAndSave(TEXT, SUFFIX, newSuffixValue)
                .verifyFormSaved()
                .verifyAttribute(SUFFIX, newSuffixValue);
        } finally {
            ops.removeIfExists(addSuffixRoleMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Add Suffix Role Mapper instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Add Suffix Role Mapper table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeAddSuffixRoleMapperTest() throws Exception {
        String
            addSuffixRoleMapperName = randomAlphanumeric(5),
            suffixValue = randomAlphanumeric(5);
        Address addSuffixRoleMapperAddress = elyOps.getElytronAddress(ADD_SUFFIX_ROLE_MAPPER, addSuffixRoleMapperName);
        ResourceVerifier addSuffixRoleMapperVerifier = new ResourceVerifier(addSuffixRoleMapperAddress, client);

        try {
            ops.add(addSuffixRoleMapperAddress, Values.of(SUFFIX, suffixValue)).assertSuccess();
            addSuffixRoleMapperVerifier.verifyExists();

            page.navigateToRoleMapper().selectResource(ADD_SUFFIX_ROLE_MAPPER_LABEL).getResourceManager()
                    .removeResource(addSuffixRoleMapperName).confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(addSuffixRoleMapperName));
            addSuffixRoleMapperVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(addSuffixRoleMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to create Elytron Aggregate Role Mapper instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Aggregate Role Mapper table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addAggregateRoleMapperTest() throws Exception {
        String
            aggregateRoleMapperName = randomAlphanumeric(5),
            addPrefixRoleMapperName = randomAlphanumeric(5),
            prefixValue = randomAlphanumeric(5),
            addSuffixRoleMapperName = randomAlphanumeric(5),
            suffixValue = randomAlphanumeric(5),
            roleMappersValue = addPrefixRoleMapperName + "\n" + addSuffixRoleMapperName;
        Address
            addPrefixRoleMapperAddress = elyOps.getElytronAddress(ADD_PREFIX_ROLE_MAPPER, addPrefixRoleMapperName),
            addSuffixRoleMapperAddress = elyOps.getElytronAddress(ADD_SUFFIX_ROLE_MAPPER, addSuffixRoleMapperName),
            aggregateRoleMapperAddress = elyOps.getElytronAddress(AGGREGATE_ROLE_MAPPER, aggregateRoleMapperName);

        try {
            ops.add(addPrefixRoleMapperAddress, Values.of(PREFIX, prefixValue)).assertSuccess();
            ops.add(addSuffixRoleMapperAddress, Values.of(SUFFIX, suffixValue)).assertSuccess();

            page.navigateToRoleMapper()
                .selectResource(AGGREGATE_ROLE_MAPPER_LABEL)
                .getResourceManager()
                .addResource(AddResourceWizard.class)
                .name(aggregateRoleMapperName)
                .text(ROLE_MAPPERS, roleMappersValue)
                .saveWithState()
                .assertWindowClosed();

            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(aggregateRoleMapperName));
            new ResourceVerifier(aggregateRoleMapperAddress, client).verifyExists()
                    .verifyAttribute(ROLE_MAPPERS, new ModelNodeListBuilder()
                            .addNode(new ModelNode(addPrefixRoleMapperName))
                            .addNode(new ModelNode(addSuffixRoleMapperName))
                            .build());
        } finally {
            ops.removeIfExists(aggregateRoleMapperAddress);
            ops.removeIfExists(addPrefixRoleMapperAddress);
            ops.removeIfExists(addSuffixRoleMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Aggregate Role Mapper instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editAggregateRoleMapperAttributesTest() throws Exception {
        String
        aggregateRoleMapperName = randomAlphanumeric(5),
        addPrefixRoleMapperName = randomAlphanumeric(5),
        prefixValue = randomAlphanumeric(5),
        addSuffixRoleMapperName1 = randomAlphanumeric(5),
        suffixValue1 = randomAlphanumeric(5),
        addSuffixRoleMapperName2 = randomAlphanumeric(5),
        suffixValue2 = randomAlphanumeric(5),
        newRoleMappersValue = addSuffixRoleMapperName1 + "\n" + addPrefixRoleMapperName + "\n" + addSuffixRoleMapperName2;
    Address
        addPrefixRoleMapperAddress = elyOps.getElytronAddress(ADD_PREFIX_ROLE_MAPPER, addPrefixRoleMapperName),
        addSuffixRoleMapperAddress1 = elyOps.getElytronAddress(ADD_SUFFIX_ROLE_MAPPER, addSuffixRoleMapperName1),
        addSuffixRoleMapperAddress2 = elyOps.getElytronAddress(ADD_SUFFIX_ROLE_MAPPER, addSuffixRoleMapperName2),
        aggregateRoleMapperAddress = elyOps.getElytronAddress(AGGREGATE_ROLE_MAPPER, aggregateRoleMapperName);

        try {
            ops.add(addPrefixRoleMapperAddress, Values.of(PREFIX, prefixValue)).assertSuccess();
            ops.add(addSuffixRoleMapperAddress1, Values.of(SUFFIX, suffixValue1)).assertSuccess();
            ops.add(addSuffixRoleMapperAddress2, Values.of(SUFFIX, suffixValue2)).assertSuccess();
            ops.add(aggregateRoleMapperAddress, Values.of(ROLE_MAPPERS, new ModelNodeListBuilder()
                    .addNode(new ModelNode(addPrefixRoleMapperName))
                    .addNode(new ModelNode(addSuffixRoleMapperName1))
                    .build())).assertSuccess();

            page.navigateToRoleMapper().selectResource(AGGREGATE_ROLE_MAPPER_LABEL).getResourceManager()
                    .selectByName(aggregateRoleMapperName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, aggregateRoleMapperAddress)
                .configFragment(page.getConfigFragment())
                .editAndSave(TEXT, ROLE_MAPPERS, newRoleMappersValue)
                .verifyFormSaved()
                .verifyAttribute(ROLE_MAPPERS, new ModelNodeListBuilder()
                        .addNode(new ModelNode(addSuffixRoleMapperName1))
                        .addNode(new ModelNode(addPrefixRoleMapperName))
                        .addNode(new ModelNode(addSuffixRoleMapperName2))
                        .build());
        } finally {
            ops.removeIfExists(aggregateRoleMapperAddress);
            ops.removeIfExists(addPrefixRoleMapperAddress);
            ops.removeIfExists(addSuffixRoleMapperAddress1);
            ops.removeIfExists(addSuffixRoleMapperAddress2);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Aggregate Role Mapper instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Aggregate Role Mapper table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeAggregateRoleMapperTest() throws Exception {
        String
            aggregateRoleMapperName = randomAlphanumeric(5),
            addPrefixRoleMapperName = randomAlphanumeric(5),
            prefixValue = randomAlphanumeric(5),
            addSuffixRoleMapperName = randomAlphanumeric(5),
            suffixValue = randomAlphanumeric(5);
        Address
            addPrefixRoleMapperAddress = elyOps.getElytronAddress(ADD_PREFIX_ROLE_MAPPER, addPrefixRoleMapperName),
            addSuffixRoleMapperAddress = elyOps.getElytronAddress(ADD_SUFFIX_ROLE_MAPPER, addSuffixRoleMapperName),
            aggregateRoleMapperAddress = elyOps.getElytronAddress(AGGREGATE_ROLE_MAPPER, aggregateRoleMapperName);
        ResourceVerifier aggregateRoleMapperVerifier = new ResourceVerifier(aggregateRoleMapperAddress, client);

        try {
            ops.add(addPrefixRoleMapperAddress, Values.of(PREFIX, prefixValue)).assertSuccess();
            ops.add(addSuffixRoleMapperAddress, Values.of(SUFFIX, suffixValue)).assertSuccess();
            ops.add(aggregateRoleMapperAddress, Values.of(ROLE_MAPPERS, new ModelNodeListBuilder()
                    .addNode(new ModelNode(addPrefixRoleMapperName))
                    .addNode(new ModelNode(addSuffixRoleMapperName))
                    .build())).assertSuccess();
            aggregateRoleMapperVerifier.verifyExists();

            page.navigateToRoleMapper().selectResource(AGGREGATE_ROLE_MAPPER_LABEL).getResourceManager()
                    .removeResource(aggregateRoleMapperName).confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(aggregateRoleMapperName));
            aggregateRoleMapperVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(aggregateRoleMapperAddress);
            ops.removeIfExists(addPrefixRoleMapperAddress);
            ops.removeIfExists(addSuffixRoleMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to create Elytron Constant Role Mapper instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Constant Role Mapper table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addConstantRoleMapperTest() throws Exception {
        String
            constantRoleMapperName = randomAlphanumeric(5),
            role1name = randomAlphanumeric(5),
            role2name = randomAlphanumeric(5),
            rolesValue = role1name + "\n" + role2name;
        Address constantRoleMapperAddress = elyOps.getElytronAddress(CONSTANT_ROLE_MAPPER, constantRoleMapperName);

        try {
            page.navigateToRoleMapper()
                .selectResource(CONSTANT_ROLE_MAPPER_LABEL)
                .getResourceManager()
                .addResource(AddResourceWizard.class)
                .name(constantRoleMapperName)
                .text(ROLES, rolesValue)
                .saveWithState()
                .assertWindowClosed();

            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(constantRoleMapperName));
            new ResourceVerifier(constantRoleMapperAddress, client).verifyExists()
                    .verifyAttribute(ROLES, new ModelNodeListBuilder()
                            .addNode(new ModelNode(role1name))
                            .addNode(new ModelNode(role2name))
                            .build());
        } finally {
            ops.removeIfExists(constantRoleMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Constant Role Mapper instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editConstantRoleMapperAttributesTest() throws Exception {
        String
        constantRoleMapperName = randomAlphanumeric(5),
        role1name = randomAlphanumeric(5),
        role2name = randomAlphanumeric(5),
        role3name = randomAlphanumeric(5),
        newRoleMappersValue = role2name + "\n" + role1name + "\n" + role3name;
    Address constantRoleMapperAddress = elyOps.getElytronAddress(CONSTANT_ROLE_MAPPER, constantRoleMapperName);

        try {
            ops.add(constantRoleMapperAddress, Values.of(ROLES, new ModelNodeListBuilder()
                    .addNode(new ModelNode(role1name))
                    .addNode(new ModelNode(role2name))
                    .build())).assertSuccess();

            page.navigateToRoleMapper().selectResource(CONSTANT_ROLE_MAPPER_LABEL).getResourceManager()
                    .selectByName(constantRoleMapperName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, constantRoleMapperAddress)
                .configFragment(page.getConfigFragment())
                .editAndSave(TEXT, ROLES, newRoleMappersValue)
                .verifyFormSaved()
                .verifyAttribute(ROLES, new ModelNodeListBuilder()
                        .addNode(new ModelNode(role2name))
                        .addNode(new ModelNode(role1name))
                        .addNode(new ModelNode(role3name))
                        .build());
        } finally {
            ops.removeIfExists(constantRoleMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Constant Role Mapper instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Constant Role Mapper table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeConstantRoleMapperTest() throws Exception {
        String
            constantRoleMapperName = randomAlphanumeric(5),
            role1name = randomAlphanumeric(5),
            role2name = randomAlphanumeric(5);
        Address constantRoleMapperAddress = elyOps.getElytronAddress(CONSTANT_ROLE_MAPPER, constantRoleMapperName);
        ResourceVerifier constantRoleMapperVerifier = new ResourceVerifier(constantRoleMapperAddress, client);

        try {
            ops.add(constantRoleMapperAddress, Values.of(ROLES, new ModelNodeListBuilder()
                    .addNode(new ModelNode(role1name))
                    .addNode(new ModelNode(role2name))
                    .build())).assertSuccess();
            constantRoleMapperVerifier.verifyExists();

            page.navigateToRoleMapper().selectResource(CONSTANT_ROLE_MAPPER_LABEL).getResourceManager()
                    .removeResource(constantRoleMapperName).confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(constantRoleMapperName));
            constantRoleMapperVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(constantRoleMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to create Elytron Custom Role Mapper instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Custom Role Mapper table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addCustomRoleMapperTest() throws Exception {
        String customRoleMapperName = randomAlphanumeric(5);
        Address customRoleMapperAddress = elyOps.getElytronAddress(CUSTOM_ROLE_MAPPER, customRoleMapperName);

        try {
            page.navigateToRoleMapper()
                .selectResource(CUSTOM_ROLE_MAPPER_LABEL)
                .getResourceManager()
                .addResource(AddResourceWizard.class)
                .name(customRoleMapperName)
                .text(CLASS_NAME, LowercaseCustomRoleMapper.class.getName())
                .text(MODULE, customRoleMapperModuleName)
                .saveWithState()
                .assertWindowClosed();

            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(customRoleMapperName));
            new ResourceVerifier(customRoleMapperAddress, client).verifyExists()
                    .verifyAttribute(CLASS_NAME, LowercaseCustomRoleMapper.class.getName())
                    .verifyAttribute(MODULE, customRoleMapperModuleName);
        } finally {
            ops.removeIfExists(customRoleMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Custom Role Mapper instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editCustomRoleMapperAttributesTest() throws Exception {
        String
            customRoleMapperName = randomAlphanumeric(5),
            key1 = randomAlphanumeric(5),
            value1 = randomAlphanumeric(5),
            key2 = randomAlphanumeric(5),
            value2 = randomAlphanumeric(5);
        Address customRoleMapperAddress = elyOps.getElytronAddress(CUSTOM_ROLE_MAPPER, customRoleMapperName);

        try {
            ops.add(customRoleMapperAddress, Values.of(CLASS_NAME, LowercaseCustomRoleMapper.class.getName())
                    .and(MODULE, customRoleMapperModuleName)).assertSuccess();

            page.navigateToRoleMapper().selectResource(CUSTOM_ROLE_MAPPER_LABEL).getResourceManager()
                    .selectByName(customRoleMapperName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, customRoleMapperAddress)
                .configFragment(page.getConfigFragment())
                .edit(TEXT, CLASS_NAME, UppercaseCustomRoleMapper.class.getName())
                .edit(TEXT, CONFIGURATION, key1 + "=" + value1 + "\n" + key2 + "=" + value2)
                .andSave().verifyFormSaved()
                .verifyAttribute(CLASS_NAME, UppercaseCustomRoleMapper.class.getName())
                .verifyAttribute(CONFIGURATION, new ModelNodePropertiesBuilder()
                        .addProperty(key1, value1)
                        .addProperty(key2, value2)
                        .build());
        } finally {
            ops.removeIfExists(customRoleMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Custom Role Mapper instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Custom Role Mapper table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeCustomRoleMapperTest() throws Exception {
        String customRoleMapperName = randomAlphanumeric(5);
        Address customRoleMapperAddress = elyOps.getElytronAddress(CUSTOM_ROLE_MAPPER, customRoleMapperName);
        ResourceVerifier customRoleMapperVerifier = new ResourceVerifier(customRoleMapperAddress, client);

        try {
            ops.add(customRoleMapperAddress, Values.of(CLASS_NAME, LowercaseCustomRoleMapper.class.getName())
                    .and(MODULE, customRoleMapperModuleName)).assertSuccess();
            customRoleMapperVerifier.verifyExists();

            page.navigateToRoleMapper().selectResource(CUSTOM_ROLE_MAPPER_LABEL).getResourceManager()
                    .removeResource(customRoleMapperName).confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(customRoleMapperName));
            customRoleMapperVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(customRoleMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to create Elytron Logical Role Mapper instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Logical Role Mapper table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addLogicalRoleMapperTest() throws Exception {
        String logicalRoleMapperName = randomAlphanumeric(5);
        Address logicalRoleMapperAddress = elyOps.getElytronAddress(LOGICAL_ROLE_MAPPER, logicalRoleMapperName);

        try {
            page.navigateToRoleMapper()
                .selectResource(LOGICAL_ROLE_MAPPER_LABEL)
                .getResourceManager()
                .addResource(AddResourceWizard.class)
                .name(logicalRoleMapperName)
                .select(LOGICAL_OPERATION, AND)
                .saveWithState()
                .assertWindowClosed();

            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(logicalRoleMapperName));
            new ResourceVerifier(logicalRoleMapperAddress, client).verifyExists()
                    .verifyAttribute(LOGICAL_OPERATION, AND);
        } finally {
            ops.removeIfExists(logicalRoleMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Logical Role Mapper instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editLogicalRoleMapperAttributesTest() throws Exception {
        String
        logicalRoleMapperName = randomAlphanumeric(5),
        addPrefixRoleMapperName = randomAlphanumeric(5),
        prefixValue = randomAlphanumeric(5),
        addSuffixRoleMapperName = randomAlphanumeric(5),
        suffixValue = randomAlphanumeric(5);
    Address
    addPrefixRoleMapperAddress = elyOps.getElytronAddress(ADD_PREFIX_ROLE_MAPPER, addPrefixRoleMapperName),
    addSuffixRoleMapperAddress = elyOps.getElytronAddress(ADD_SUFFIX_ROLE_MAPPER, addSuffixRoleMapperName),
    logicalRoleMapperAddress = elyOps.getElytronAddress(LOGICAL_ROLE_MAPPER, logicalRoleMapperName);

        try {
            ops.add(logicalRoleMapperAddress, Values.of(LOGICAL_OPERATION, AND)).assertSuccess();
            ops.add(addPrefixRoleMapperAddress, Values.of(PREFIX, prefixValue)).assertSuccess();
            ops.add(addSuffixRoleMapperAddress, Values.of(SUFFIX, suffixValue)).assertSuccess();

            page.navigateToRoleMapper().selectResource(LOGICAL_ROLE_MAPPER_LABEL).getResourceManager()
                    .selectByName(logicalRoleMapperName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, logicalRoleMapperAddress)
                .configFragment(page.getConfigFragment())
                .edit(TEXT, LEFT, addPrefixRoleMapperName)
                .edit(SELECT, LOGICAL_OPERATION, XOR)
                .edit(TEXT, RIGHT, addSuffixRoleMapperName)
                .andSave().verifyFormSaved()
                .verifyAttribute(LEFT, addPrefixRoleMapperName)
                .verifyAttribute(LOGICAL_OPERATION, XOR)
                .verifyAttribute(RIGHT, addSuffixRoleMapperName);
        } finally {
            ops.removeIfExists(logicalRoleMapperAddress);
            ops.removeIfExists(addPrefixRoleMapperAddress);
            ops.removeIfExists(addSuffixRoleMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Logical Role Mapper instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Logical Role Mapper table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeLogicalRoleMapperTest() throws Exception {
        String logicalRoleMapperName = randomAlphanumeric(5);
        Address logicalRoleMapperAddress = elyOps.getElytronAddress(LOGICAL_ROLE_MAPPER, logicalRoleMapperName);
        ResourceVerifier logicalRoleMapperVerifier = new ResourceVerifier(logicalRoleMapperAddress, client);

        try {
            ops.add(logicalRoleMapperAddress, Values.of(LOGICAL_OPERATION, AND)).assertSuccess();
            logicalRoleMapperVerifier.verifyExists();

            page.navigateToRoleMapper().selectResource(LOGICAL_ROLE_MAPPER_LABEL).getResourceManager()
                    .removeResource(logicalRoleMapperName).confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(logicalRoleMapperName));
            logicalRoleMapperVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(logicalRoleMapperAddress);
            adminOps.reloadIfRequired();
        }
    }
}
