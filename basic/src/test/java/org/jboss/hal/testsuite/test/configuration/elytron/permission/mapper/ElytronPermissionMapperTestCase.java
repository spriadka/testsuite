package org.jboss.hal.testsuite.test.configuration.elytron.permission.mapper;

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
import org.jboss.hal.testsuite.category.Shared;
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
@Category(Shared.class)
public class ElytronPermissionMapperTestCase extends AbstractElytronTestCase {

    private static final String
        SIMPLE_PERMISSION_MAPPER = "simple-permission-mapper",
        SIMPLE_PERMISSION_MAPPER_LABEL = "Simple Permission Mapper",
        MAPPING_MODE = "mapping-mode",
        PERMISSION_MAPPINGS = "permission-mappings",
        PERMISSION_MAPPINGS_LABEL = "Permission Mapping",
        PRINCIPALS = "principals",
        ROLES = "roles",
        UNLESS = "unless",
        FIRST = "first",
        LOGICAL_PERMISSION_MAPPER = "logical-permission-mapper",
        LOGICAL_PERMISSION_MAPPER_LABEL = "Logical Permission Mapper",
        LEFT = "left",
        LOGICAL_OPERATION = "logical-operation",
        RIGHT = "right",
        XOR = "xor",
        ARCHIVE_NAME = "elytron.customer.permission.mapper.jar",
        CUSTOM_PERMISSION_MAPPER = "custom-permission-mapper",
        CUSTOM_PERMISSION_MAPPER_LABEL = "Custom Permission Mapper",
        CONSTANT_PERMISSION_MAPPER = "constant-permission-mapper",
        CONSTANT_PERMISSION_MAPPER_LABEL = "Constant Permission Mapper",
        TARGET_NAME = "target-name",
        ACTION = "action",
        PERMISSIONS = "permissions",
        PERMISSIONS_LABEL = "Permissions";

    private static final Path CUSTOM_ROLE_MAPPER_PATH = Paths.get("test", "elytron",
            "permission", "mapper" + randomAlphanumeric(5));

    private static String customPermissionMapperModuleName;

    private static ModuleUtils moduleUtils;

    @Page
    private MapperDecoderPage page;

    @BeforeClass
    public static void beforeClass() throws Exception {
        moduleUtils = new ModuleUtils(client);
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, ARCHIVE_NAME);
        jar.addClasses(OptimisticCustomPermissionMapper.class, PesimisticCustomPermissionMapper.class);
        customPermissionMapperModuleName = moduleUtils.createModule(CUSTOM_ROLE_MAPPER_PATH, jar,
                "org.wildfly.extension.elytron", "org.wildfly.security.elytron-private");
    }

    @AfterClass
    public static void afterClass() throws Exception {
        moduleUtils.removeModule(CUSTOM_ROLE_MAPPER_PATH);
    }

    /**
     * @tpTestDetails Try to create Elytron Simple Permission Mapper instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Simple Permission Mapper table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addSimplePermissionMapperTest() throws Exception {
        String
            simplePermissionMapperName = randomAlphanumeric(5);
        Address simplePermissionMapperAddress =
                elyOps.getElytronAddress(SIMPLE_PERMISSION_MAPPER, simplePermissionMapperName);

        try {
            page.navigateToPermissionMapper()
                .selectResource(SIMPLE_PERMISSION_MAPPER_LABEL)
                .getResourceManager()
                .addResource(AddResourceWizard.class)
                .name(simplePermissionMapperName)
                .select(MAPPING_MODE, UNLESS)
                .saveWithState()
                .assertWindowClosed();

            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(simplePermissionMapperName));
            new ResourceVerifier(simplePermissionMapperAddress, client).verifyExists()
                    .verifyAttribute(MAPPING_MODE, UNLESS);
        } finally {
            ops.removeIfExists(simplePermissionMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Simple Permission Mapper instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editSimplePermissionMapperAttributesTest() throws Exception {
        String simplePermissionMapperName = randomAlphanumeric(5);
        Address simplePermissionMapperAddress =
                elyOps.getElytronAddress(SIMPLE_PERMISSION_MAPPER, simplePermissionMapperName);

        try {
            ops.add(simplePermissionMapperAddress, Values.of(MAPPING_MODE, UNLESS)).assertSuccess();

            page.navigateToPermissionMapper().selectResource(SIMPLE_PERMISSION_MAPPER_LABEL).getResourceManager()
                    .selectByName(simplePermissionMapperName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, simplePermissionMapperAddress)
                .configFragment(page.getConfigFragment())
                .editAndSave(SELECT, MAPPING_MODE, FIRST)
                .verifyFormSaved()
                .verifyAttribute(MAPPING_MODE, FIRST);
        } finally {
            ops.removeIfExists(simplePermissionMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Simple Permission Mapper instance in model
     * and try to add item to it's permission-mappings list in Web Console's Elytron subsystem configuration.
     * Validate added item in the model.
     */
    @Test
    public void addPermissionMappingItemOfSimplePermissionMapperTest() throws Exception {
        String
            simplePermissionMapperName = randomAlphanumeric(5),
            principal1name = randomAlphanumeric(5),
            principal2name = randomAlphanumeric(5),
            principal3name = randomAlphanumeric(5),
            role1name = randomAlphanumeric(5),
            role2name = randomAlphanumeric(5);
        Address simplePermissionMapperAddress =
                elyOps.getElytronAddress(SIMPLE_PERMISSION_MAPPER, simplePermissionMapperName);

        try {
            ops.add(simplePermissionMapperAddress).assertSuccess();

            page.navigateToPermissionMapper().selectResource(SIMPLE_PERMISSION_MAPPER_LABEL).getResourceManager()
                .selectByName(simplePermissionMapperName);
            page.switchToConfigAreaTab(PERMISSION_MAPPINGS_LABEL);

            page.getConfigAreaResourceManager().addResource(AddResourceWizard.class)
                    .text(PRINCIPALS, principal1name + "\n" + principal2name + "\n" + principal3name)
                    .text(ROLES, role1name + "\n" + role2name)
                    .saveAndDismissReloadRequiredWindowWithState().assertWindowClosed();
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInConfigAreaTable(getArrayString(principal1name, principal2name, principal3name)));
            new ResourceVerifier(simplePermissionMapperAddress, client).verifyAttribute(PERMISSION_MAPPINGS,
                        new ModelNodeListBuilder()
                                .addNode(new ModelNodePropertiesBuilder()
                                        .addProperty(PRINCIPALS, new ModelNodeListBuilder()
                                                .addAll(principal1name, principal2name, principal3name)
                                                .build())
                                        .addProperty(ROLES, new ModelNodeListBuilder()
                                                .addAll(role1name, role2name)
                                                .build())
                                        .addUndefinedProperty(PERMISSIONS)
                                        .build())
                                .build());
        } finally {
            ops.removeIfExists(simplePermissionMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Simple Permission Mapper instance in model
     * and try to remove item from it's permission-mappings list in Web Console's Elytron subsystem configuration.
     * Validate added item in the model.
     */
    @Test
    public void removePermissionMappingItemOfSimplePermissionMapperTest() throws Exception {
        String
            simplePermissionMapperName = randomAlphanumeric(5),
            principalItemName = randomAlphanumeric(5),
            principalsListValue = getArrayString(principalItemName);
        Address simplePermissionMapperAddress =
            elyOps.getElytronAddress(SIMPLE_PERMISSION_MAPPER, simplePermissionMapperName);

        try {
            ops.add(simplePermissionMapperAddress, Values.of(PERMISSION_MAPPINGS, new ModelNodeListBuilder()
                    .addNode(new ModelNodePropertiesBuilder()
                            .addProperty(PRINCIPALS, new ModelNodeListBuilder()
                                    .addNode(new ModelNode(principalItemName))
                                    .build())
                            .build())
                    .build())).assertSuccess();

            page.navigateToPermissionMapper().selectResource(SIMPLE_PERMISSION_MAPPER_LABEL).getResourceManager()
                .selectByName(simplePermissionMapperName);
            page.switchToConfigAreaTab(PERMISSION_MAPPINGS_LABEL);

            page.getConfigAreaResourceManager().removeResource(principalsListValue)
                .confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Deleted resource should not be present in the table any more!",
                page.resourceIsPresentInConfigAreaTable(principalsListValue));
            new ResourceVerifier(simplePermissionMapperAddress, client).verifyAttribute(PERMISSION_MAPPINGS,
                    new ModelNodeListBuilder().empty().build());
        } finally {
            ops.removeIfExists(simplePermissionMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Simple Permission Mapper instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Simple Permission Mapper table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeSimplePermissionMapperTest() throws Exception {
        String simplePermissionMapperName = randomAlphanumeric(5);
        Address simplePermissionMapperAddress =
                elyOps.getElytronAddress(SIMPLE_PERMISSION_MAPPER, simplePermissionMapperName);
        ResourceVerifier simplePermissionMapperVerifier = new ResourceVerifier(simplePermissionMapperAddress, client);

        try {
            ops.add(simplePermissionMapperAddress).assertSuccess();
            simplePermissionMapperVerifier.verifyExists();

            page.navigateToPermissionMapper().selectResource(SIMPLE_PERMISSION_MAPPER_LABEL).getResourceManager()
                    .removeResource(simplePermissionMapperName).confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(simplePermissionMapperName));
            simplePermissionMapperVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(simplePermissionMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to create Elytron Logical Permission Mapper instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Logical Permission Mapper table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addLogicalPermissionMapperTest() throws Exception {
        String
            permissionMapper1name = randomAlphanumeric(5),
            permissionMapper2name = randomAlphanumeric(5),
            logicalPermissionMapperName = randomAlphanumeric(5);
        Address
            permissionMapper1address =
                elyOps.getElytronAddress(SIMPLE_PERMISSION_MAPPER, permissionMapper1name),
            permissionMapper2address =
                elyOps.getElytronAddress(SIMPLE_PERMISSION_MAPPER, permissionMapper2name),
            logicalPermissionMapperAddress =
                elyOps.getElytronAddress(LOGICAL_PERMISSION_MAPPER, logicalPermissionMapperName);

        try {
            ops.add(permissionMapper1address).assertSuccess();
            ops.add(permissionMapper2address).assertSuccess();
            page.navigateToPermissionMapper()
                .selectResource(LOGICAL_PERMISSION_MAPPER_LABEL)
                .getResourceManager()
                .addResource(AddResourceWizard.class)
                .name(logicalPermissionMapperName)
                .text(LEFT, permissionMapper1name)
                .select(LOGICAL_OPERATION, UNLESS)
                .text(RIGHT, permissionMapper2name)
                .saveWithState()
                .assertWindowClosed();

            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(logicalPermissionMapperName));
            new ResourceVerifier(logicalPermissionMapperAddress, client).verifyExists()
                    .verifyAttribute(LEFT, permissionMapper1name)
                    .verifyAttribute(LOGICAL_OPERATION, UNLESS)
                    .verifyAttribute(RIGHT, permissionMapper2name);
        } finally {
            ops.removeIfExists(logicalPermissionMapperAddress);
            ops.removeIfExists(permissionMapper1address);
            ops.removeIfExists(permissionMapper2address);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Logical Permission Mapper instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editLogicalPermissionMapperAttributesTest() throws Exception {
        String
            permissionMapper1name = randomAlphanumeric(5),
            permissionMapper2name = randomAlphanumeric(5),
            permissionMapper3name = randomAlphanumeric(5),
            logicalPermissionMapperName = randomAlphanumeric(5);
        Address
            permissionMapper1address =
                elyOps.getElytronAddress(SIMPLE_PERMISSION_MAPPER, permissionMapper1name),
            permissionMapper2address =
                elyOps.getElytronAddress(SIMPLE_PERMISSION_MAPPER, permissionMapper2name),
            permissionMapper3address =
                elyOps.getElytronAddress(SIMPLE_PERMISSION_MAPPER, permissionMapper3name),
            logicalPermissionMapperAddress =
                elyOps.getElytronAddress(LOGICAL_PERMISSION_MAPPER, logicalPermissionMapperName);

        try {
            ops.add(permissionMapper1address).assertSuccess();
            ops.add(permissionMapper2address).assertSuccess();
            ops.add(permissionMapper3address).assertSuccess();
            ops.add(logicalPermissionMapperAddress, Values.of(LEFT, permissionMapper1name)
                    .and(LOGICAL_OPERATION, UNLESS).and(RIGHT, permissionMapper2name)).assertSuccess();

            page.navigateToPermissionMapper().selectResource(LOGICAL_PERMISSION_MAPPER_LABEL).getResourceManager()
                    .selectByName(logicalPermissionMapperName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, logicalPermissionMapperAddress)
                .configFragment(page.getConfigFragment())
                .edit(TEXT, LEFT, permissionMapper2name)
                .edit(SELECT, LOGICAL_OPERATION, XOR)
                .edit(TEXT, RIGHT, permissionMapper3name)
                .andSave().verifyFormSaved()
                .verifyAttribute(LEFT, permissionMapper2name)
                .verifyAttribute(LOGICAL_OPERATION, XOR)
                .verifyAttribute(RIGHT, permissionMapper3name);
        } finally {
            ops.removeIfExists(logicalPermissionMapperAddress);
            ops.removeIfExists(permissionMapper1address);
            ops.removeIfExists(permissionMapper2address);
            ops.removeIfExists(permissionMapper3address);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Logical Permission Mapper instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Logical Permission Mapper table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeLogicalPermissionMapperTest() throws Exception {
        String
            permissionMapper1name = randomAlphanumeric(5),
            permissionMapper2name = randomAlphanumeric(5),
            logicalPermissionMapperName = randomAlphanumeric(5);
        Address
            permissionMapper1address =
                elyOps.getElytronAddress(SIMPLE_PERMISSION_MAPPER, permissionMapper1name),
            permissionMapper2address =
                elyOps.getElytronAddress(SIMPLE_PERMISSION_MAPPER, permissionMapper2name),
            logicalPermissionMapperAddress =
                elyOps.getElytronAddress(LOGICAL_PERMISSION_MAPPER, logicalPermissionMapperName);
        ResourceVerifier logicalPermissionMapperVerifier = new ResourceVerifier(logicalPermissionMapperAddress, client);

        try {
            ops.add(permissionMapper1address).assertSuccess();
            ops.add(permissionMapper2address).assertSuccess();
            ops.add(logicalPermissionMapperAddress, Values.of(LEFT, permissionMapper1name).and(LOGICAL_OPERATION, XOR)
                    .and(RIGHT, permissionMapper2name)).assertSuccess();
            logicalPermissionMapperVerifier.verifyExists();

            page.navigateToPermissionMapper().selectResource(LOGICAL_PERMISSION_MAPPER_LABEL).getResourceManager()
                    .removeResource(logicalPermissionMapperName).confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(logicalPermissionMapperName));
            logicalPermissionMapperVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(logicalPermissionMapperAddress);
            ops.removeIfExists(permissionMapper1address);
            ops.removeIfExists(permissionMapper2address);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to create Elytron Custom Permission Mapper instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Custom Permission Mapper table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addCustomPermissionMapperTest() throws Exception {
        String customPermissionMapperName = randomAlphanumeric(5);
        Address customPermissionMapperAddress =
                elyOps.getElytronAddress(CUSTOM_PERMISSION_MAPPER, customPermissionMapperName);

        try {
            page.navigateToPermissionMapper()
                .selectResource(CUSTOM_PERMISSION_MAPPER_LABEL)
                .getResourceManager()
                .addResource(AddResourceWizard.class)
                .name(customPermissionMapperName)
                .text(CLASS_NAME, OptimisticCustomPermissionMapper.class.getName())
                .text(MODULE, customPermissionMapperModuleName)
                .saveWithState().assertWindowClosed();

            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(customPermissionMapperName));
            new ResourceVerifier(customPermissionMapperAddress, client).verifyExists()
                    .verifyAttribute(CLASS_NAME, OptimisticCustomPermissionMapper.class.getName())
                    .verifyAttribute(MODULE, customPermissionMapperModuleName);
        } finally {
            ops.removeIfExists(customPermissionMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Custom Permission Mapper instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editCustomPermissionMapperAttributesTest() throws Exception {
        String
            customPermissionMapperName = randomAlphanumeric(5),
            key1 = randomAlphanumeric(5),
            value1 = randomAlphanumeric(5),
            key2 = randomAlphanumeric(5),
            value2 = randomAlphanumeric(5);
        Address customPermissionMapperAddress =
                elyOps.getElytronAddress(CUSTOM_PERMISSION_MAPPER, customPermissionMapperName);

        try {
            ops.add(customPermissionMapperAddress, Values.of(CLASS_NAME, OptimisticCustomPermissionMapper.class.getName())
                    .and(MODULE, customPermissionMapperModuleName)).assertSuccess();

            page.navigateToPermissionMapper().selectResource(CUSTOM_PERMISSION_MAPPER_LABEL).getResourceManager()
                    .selectByName(customPermissionMapperName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, customPermissionMapperAddress)
                .configFragment(page.getConfigFragment())
                .edit(TEXT, CLASS_NAME, PesimisticCustomPermissionMapper.class.getName())
                .edit(TEXT, CONFIGURATION, key1 + "=" + value1 + "\n" + key2 + "=" + value2)
                .andSave().verifyFormSaved()
                .verifyAttribute(CLASS_NAME, PesimisticCustomPermissionMapper.class.getName())
                .verifyAttribute(CONFIGURATION, new ModelNodePropertiesBuilder()
                        .addProperty(key1, value1)
                        .addProperty(key2, value2)
                        .build());
        } finally {
            ops.removeIfExists(customPermissionMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Custom Permission Mapper instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Custom Permission Mapper table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeCustomPermissionMapperTest() throws Exception {
        String customPermissionMapperName = randomAlphanumeric(5);
        Address customPermissionMapperAddress =
                elyOps.getElytronAddress(CUSTOM_PERMISSION_MAPPER, customPermissionMapperName);
        ResourceVerifier customPermissionMapperVerifier = new ResourceVerifier(customPermissionMapperAddress, client);

        try {
            ops.add(customPermissionMapperAddress, Values.of(CLASS_NAME, OptimisticCustomPermissionMapper.class.getName())
                    .and(MODULE, customPermissionMapperModuleName)).assertSuccess();
            customPermissionMapperVerifier.verifyExists();

            page.navigateToPermissionMapper().selectResource(CUSTOM_PERMISSION_MAPPER_LABEL).getResourceManager()
                    .removeResource(customPermissionMapperName).confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(customPermissionMapperName));
            customPermissionMapperVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(customPermissionMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to create Elytron Constant Permission Mapper instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Constant Permission Mapper table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addConstantPermissionMapperTest() throws Exception {
        String constantPermissionMapperName = randomAlphanumeric(5);
        Address constantPermissionMapperAddress =
                elyOps.getElytronAddress(CONSTANT_PERMISSION_MAPPER, constantPermissionMapperName);

        try {
            page.navigateToPermissionMapper()
                .selectResource(CONSTANT_PERMISSION_MAPPER_LABEL)
                .getResourceManager()
                .addResource(AddResourceWizard.class)
                .name(constantPermissionMapperName)
                .saveWithState()
                .assertWindowClosed();

            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(constantPermissionMapperName));
            new ResourceVerifier(constantPermissionMapperAddress, client).verifyExists();
        } finally {
            ops.removeIfExists(constantPermissionMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Constant Permission Mapper instance in model
     * and try to add item to it's permissions list in Web Console's Elytron subsystem configuration.
     * Validate added item in the model.
     */
    @Test
    public void addPermissionsItemOfConstantPermissionMapperTest() throws Exception {
        String
            constantPermissionMapperName = randomAlphanumeric(5),
            classNameValue = randomAlphanumeric(5),
            moduleValue = randomAlphanumeric(5),
            targetNameValue = randomAlphanumeric(5),
            actionValue = randomAlphanumeric(5);
        Address constantPermissionMapperAddress =
            elyOps.getElytronAddress(CONSTANT_PERMISSION_MAPPER, constantPermissionMapperName);

        try {
            ops.add(constantPermissionMapperAddress).assertSuccess();

            page.navigateToPermissionMapper().selectResource(CONSTANT_PERMISSION_MAPPER_LABEL).getResourceManager()
                .selectByName(constantPermissionMapperName);
            page.switchToConfigAreaTab(PERMISSIONS_LABEL);

            AddResourceWizard wizard = page.getConfigAreaResourceManager().addResource(AddResourceWizard.class);
            wizard.openOptionalFieldsTab();
            wizard.name(constantPermissionMapperName)
                .text(CLASS_NAME, classNameValue)
                .text(MODULE, moduleValue)
                .text(TARGET_NAME, targetNameValue)
                .text(ACTION, actionValue)
                .saveAndDismissReloadRequiredWindowWithState().assertWindowClosed();
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInConfigAreaTable(classNameValue));
            new ResourceVerifier(constantPermissionMapperAddress, client).verifyAttribute(PERMISSIONS,
                        new ModelNodeListBuilder().addNode(new ModelNodePropertiesBuilder()
                                .addProperty(CLASS_NAME, classNameValue)
                                .addProperty(MODULE, moduleValue)
                                .addProperty(TARGET_NAME, targetNameValue)
                                .addProperty(ACTION, actionValue)
                                .build())
                        .build());
        } finally {
            ops.removeIfExists(constantPermissionMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Constant Permission Mapper instance in model
     * and try to remove item from it's permissions list in Web Console's Elytron subsystem configuration.
     * Validate added item in the model.
     */
    @Test
    public void removePermissionsItemOfConstantPermissionMapperTest() throws Exception {
        String
            constantPermissionMapperName = randomAlphanumeric(5),
            classNameValue = randomAlphanumeric(5);
        Address constantPermissionMapperAddress =
                elyOps.getElytronAddress(CONSTANT_PERMISSION_MAPPER, constantPermissionMapperName);

        try {
            ops.add(constantPermissionMapperAddress, Values.of(PERMISSIONS,
                    new ModelNodeListBuilder(new ModelNodePropertiesBuilder()
                            .addProperty(CLASS_NAME, classNameValue)
                            .build())
                    .build())).assertSuccess();

            page.navigateToPermissionMapper().selectResource(CONSTANT_PERMISSION_MAPPER_LABEL).getResourceManager()
                .selectByName(constantPermissionMapperName);
            page.switchToConfigAreaTab(PERMISSIONS_LABEL);

            page.getConfigAreaResourceManager().removeResource(classNameValue)
                    .confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Deleted resource should not be present in the table any more!",
                    page.resourceIsPresentInConfigAreaTable(classNameValue));
            new ResourceVerifier(constantPermissionMapperAddress, client).verifyAttribute(PERMISSIONS,
                        new ModelNodeListBuilder().empty().build());
        } finally {
            ops.removeIfExists(constantPermissionMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Constant Permission Mapper instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Constant Permission Mapper table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeConstantPermissionMapperTest() throws Exception {
        String constantPermissionMapperName = randomAlphanumeric(5);
        Address constantPermissionMapperAddress =
                elyOps.getElytronAddress(CONSTANT_PERMISSION_MAPPER, constantPermissionMapperName);
        ResourceVerifier constantPermissionMapperVerifier = new ResourceVerifier(constantPermissionMapperAddress, client);

        try {
            ops.add(constantPermissionMapperAddress).assertSuccess();
            constantPermissionMapperVerifier.verifyExists();

            page.navigateToPermissionMapper().selectResource(CONSTANT_PERMISSION_MAPPER_LABEL).getResourceManager()
                    .removeResource(constantPermissionMapperName).confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(constantPermissionMapperName));
            constantPermissionMapperVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(constantPermissionMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @return String representation of the array like {@code ["foo","boo"]}
     */
    private String getArrayString(String... strings) {
        if (strings.length == 0) {
            return "";
        }
        return "[\"" + String.join("\",\"", strings) + "\"]";
    }
}
