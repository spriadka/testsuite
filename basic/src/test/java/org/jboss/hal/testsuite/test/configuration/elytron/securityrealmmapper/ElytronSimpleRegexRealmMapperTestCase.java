package org.jboss.hal.testsuite.test.configuration.elytron.securityrealmmapper;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
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

@Category(Elytron.class)
@RunWith(Arquillian.class)
public class ElytronSimpleRegexRealmMapperTestCase extends AbstractElytronTestCase {

    @Page
    private SecurityRealmMapperPage page;

    private static final String
            DELEGATE_REALM_MAPPER = "delegate-realm-mapper",
            PATTERN = "pattern",
            SIMPLE_REGEX_REALM_MAPPER = "simple-regex-realm-mapper";

    /**
     * @tpTestDetails Try to create Elytron Simple regex realm mapper in Web Console's Elytron subsystem configuration.
     * Validate created resource is visible in Simple regex realm mapper table.
     * Validate created resource is present in model.
     * Validate attributes of created resource in model.
     */
    @Test
    public void testAddSimpleRegexRealmMapper() throws Exception {
        final Address realmMapperAddress = elyOps.getElytronAddress(SIMPLE_REGEX_REALM_MAPPER, RandomStringUtils.randomAlphanumeric(7));
        final String patternValue = "(" + RandomStringUtils.randomAlphanumeric(7) + ")";

        try {
            page.navigate();
            page.switchToSimpleRegexRealmMappers()
                    .getResourceManager()
                    .addResource(AddMappedRegexRealmMapperWizard.class)
                    .name(realmMapperAddress.getLastPairValue())
                    .pattern(patternValue)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();

            Assert.assertTrue(page.getResourceManager().isResourcePresent(realmMapperAddress.getLastPairValue()));

            new ResourceVerifier(realmMapperAddress, client).verifyExists()
                    .verifyAttribute(PATTERN, patternValue);
        } finally {
            ops.removeIfExists(realmMapperAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Simple regex realm mapper instance in model and try to remove it in Web Console's
     * Elytron subsystem configuration.
     * Validate the resource is not any more visible in Simple regex realm mapper table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeSimpleRegexRealmMapper() throws Exception {
        final Address realmMapperAddress = createSimpleRegexRealmMapper();

        try {
            page.navigate();
            page.switchToSimpleRegexRealmMappers()
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
     * @tpTestDetails Create Elytron Simple regex realm mapper instance in model and try to edit its pattern attribute
     * value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editPattern() throws Exception {
        final Address realmMapperAddress = createSimpleRegexRealmMapper();

        final String patternValue = "(" + RandomStringUtils.randomAlphanumeric(7) + ")";

        try {
            page.navigate();
            page.switchToSimpleRegexRealmMappers()
                    .getResourceManager()
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
     * @tpTestDetails Create Elytron Simple regex realm mapper instance in model and try to edit its delegate-realm
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editDelegateRealmMapper() throws Exception {
        final RealmMapperOperations realmMapperOps = new RealmMapperOperations(client);

        final Address realmMapperAddress = createSimpleRegexRealmMapper();
        Address delegateRealmMapper = null,
                delegateRealmMapperRealm = null;

        try {
            delegateRealmMapperRealm = realmMapperOps.createIdentityRealm();
            delegateRealmMapper = realmMapperOps.createConstantRealmMapper(delegateRealmMapperRealm);

            page.navigate();
            page.switchToSimpleRegexRealmMappers()
                    .getResourceManager()
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

    private Address createSimpleRegexRealmMapper() throws IOException {
        final Address realmMapperAddress = elyOps.getElytronAddress(SIMPLE_REGEX_REALM_MAPPER, RandomStringUtils.randomAlphanumeric(7));
        ops.add(realmMapperAddress, Values.of(PATTERN, "(" + RandomStringUtils.randomAlphanumeric(7) + ")"))
                .assertSuccess();
        return realmMapperAddress;
    }
}
