package org.jboss.hal.testsuite.test.configuration.elytron.securityrealmmapper;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.securityrealmmapper.AddMappedRegexRealmMapperWizard;
import org.jboss.hal.testsuite.page.config.elytron.SecurityRealmMapperPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Category(Elytron.class)
@RunWith(Arquillian.class)
public class ElytronMappedRegexRealmMapperTestCase extends AbstractElytronTestCase {

    @Page
    private SecurityRealmMapperPage page;

    private static final String
            DELEGATE_REALM_MAPPER = "delegate-realm-mapper",
            PATTERN = "pattern",
            REALM_MAP = "realm-map",
            MAPPED_REGEX_REALM_MAPPER = "mapped-regex-realm-mapper";

    /**
     * @tpTestDetails Try to create Elytron Mapped regex realm mapper in Web Console's Elytron subsystem configuration.
     * Validate created resource is visible in Mapped regex realm mapper table.
     * Validate created resource is present in model.
     * Validate attributes of created resource in model.
     */
    @Test
    public void testAddMappedRegexRealmMapper() throws Exception {
        final Address realmMapperAddress = elyOps.getElytronAddress(MAPPED_REGEX_REALM_MAPPER, RandomStringUtils.randomAlphanumeric(7));
        final String patternValue = "(" + RandomStringUtils.randomAlphanumeric(7) + ")";

        final Map<String, ModelNode> realmMapValue = generateNewRealMap();

        try {
            page.navigate();
            page.getResourceManager()
                    .addResource(AddMappedRegexRealmMapperWizard.class)
                    .name(realmMapperAddress.getLastPairValue())
                    .pattern(patternValue)
                    .realmMap(realmMapValue)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();

            Assert.assertTrue(page.getResourceManager().isResourcePresent(realmMapperAddress.getLastPairValue()));

            new ResourceVerifier(realmMapperAddress, client).verifyExists()
                    .verifyAttribute(PATTERN, patternValue)
                    .verifyAttribute(REALM_MAP,
                            new ModelNodeGenerator().createObjectNodeWithPropertyChildren(realmMapValue));
        } finally {
            ops.removeIfExists(realmMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Mapped regex realm mapper instance in model and try to remove it in Web Console's
     * Elytron subsystem configuration.
     * Validate the resource is not any more visible in Mapped regex realm mapper table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeMappedRegexRealmMapper() throws Exception {
        final Address realmMapperAddress = createMappedRegexRealmMapper();

        try {
            page.navigate();
            page.getResourceManager()
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
     * @tpTestDetails Create Elytron Mapped regex realm mapper instance in model and try to edit its pattern attribute
     * value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editPattern() throws Exception {
        final Address realmMapperAddress = createMappedRegexRealmMapper();

        final String patternValue = "(" + RandomStringUtils.randomAlphanumeric(7) + ")";

        try {
            page.navigate();
            page.getResourceManager()
                    .selectByName(realmMapperAddress.getLastPairValue());

            new ConfigChecker.Builder(client, realmMapperAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, PATTERN, patternValue)
                    .verifyFormSaved()
                    .verifyAttribute(PATTERN, patternValue);
        } finally {
            ops.removeIfExists(realmMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Mapped regex realm mapper instance in model and try to edit its realm-map attribute
     * value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editRealmMap() throws Exception {
        final Address realmMapperAddress = createMappedRegexRealmMapper();

        final Map<String, ModelNode> realmMapValue = generateNewRealMap();

        try {
            page.navigate();
            page.getResourceManager()
                    .selectByName(realmMapperAddress.getLastPairValue());

            new ConfigChecker.Builder(client, realmMapperAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, REALM_MAP, realmMapValue.entrySet().stream()
                            .map(entry -> entry.getKey() + "=" + entry.getValue().asString())
                            .collect(Collectors.joining("\n")))
                    .verifyFormSaved()
                    .verifyAttribute(REALM_MAP, new ModelNodeGenerator().createObjectNodeWithPropertyChildren(realmMapValue));
        } finally {
            ops.removeIfExists(realmMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Mapped regex realm mapper instance in model and try to edit its delegate-realm
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editDelegateRealmMapper() throws Exception {
        final RealmMapperOperations realmMapperOps = new RealmMapperOperations(client);

        final Address realmMapperAddress = createMappedRegexRealmMapper();
        Address delegateRealmMapper = null,
                delegateRealmMapperRealm = null;

        try {
            delegateRealmMapperRealm = realmMapperOps.createIdentityRealm();
            delegateRealmMapper = realmMapperOps.createConstantRealmMapper(delegateRealmMapperRealm);

            page.navigate();
            page.getResourceManager()
                    .selectByName(realmMapperAddress.getLastPairValue());

            new ConfigChecker.Builder(client, realmMapperAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, DELEGATE_REALM_MAPPER, delegateRealmMapper.getLastPairValue())
                    .verifyFormSaved()
                    .verifyAttribute(DELEGATE_REALM_MAPPER, delegateRealmMapper.getLastPairValue());
        } finally {
            ops.removeIfExists(realmMapperAddress);
            if (delegateRealmMapper != null) {
                ops.removeIfExists(delegateRealmMapper);
            }
            if (delegateRealmMapperRealm != null) {
                ops.removeIfExists(delegateRealmMapperRealm);
            }
            adminOps.reloadIfRequired();
        }
    }

    private Address createMappedRegexRealmMapper() throws IOException {
        final Address realmMapperAddress = elyOps.getElytronAddress(MAPPED_REGEX_REALM_MAPPER, RandomStringUtils.randomAlphanumeric(7));
        ops.add(realmMapperAddress, Values.of(PATTERN, "(" + RandomStringUtils.randomAlphanumeric(7) + ")")
                .and(REALM_MAP, new ModelNodeGenerator().createObjectNodeWithPropertyChildren(generateNewRealMap())))
                .assertSuccess();
        return realmMapperAddress;
    }

    private Map<String, ModelNode> generateNewRealMap() {
        return new HashMap<String, ModelNode>() { {
            put(RandomStringUtils.randomAlphabetic(7), new ModelNode(RandomStringUtils.randomAlphabetic(7)));
            put(RandomStringUtils.randomAlphabetic(7), new ModelNode(RandomStringUtils.randomAlphabetic(7)));
            put(RandomStringUtils.randomAlphabetic(7), new ModelNode(RandomStringUtils.randomAlphabetic(7)));
        } };
    }

}
