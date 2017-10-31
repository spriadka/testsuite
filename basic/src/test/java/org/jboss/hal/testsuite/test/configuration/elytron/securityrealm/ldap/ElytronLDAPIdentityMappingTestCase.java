package org.jboss.hal.testsuite.test.configuration.elytron.securityrealm.ldap;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
@RunAsClient
@Category(Elytron.class)
public class ElytronLDAPIdentityMappingTestCase extends ElytronLDAPSecurityRealmTestCaseAbstract {

    private static final String FILTER_NAME = "filter-name";
    private static final String IDENTITY_MAPPING_LABEL = "Identity Mapping";
    private static final String ITERATOR_FILTER = "iterator-filter";
    private static final String NEW_IDENTITY_PARENT_DN = "new-identity-parent-dn";
    private static final String SEARCH_BASE_DN = "search-base-dn";
    private static final String USE_RECURSIVE_SEARCH = "use-recursive-search";

    /**
     * @tpTestDetails Create Elytron LDAP security realm instance in model and try to edit its identity-mapping
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editIdentityMapping() throws Exception {
        final String realmName = "ldap_security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String dirContextName = "dir_context_" + RandomStringUtils.randomAlphanumeric(7);
        final Address dirContextAddress = elyOps.getElytronAddress(DIR_CONTEXT, dirContextName);
        final Address realmAddress = elyOps.getElytronAddress(LDAP_REALM, realmName);

        final String filterNameValue = RandomStringUtils.randomAlphanumeric(7);
        final String iteratorFilterValue = RandomStringUtils.randomAlphanumeric(7);
        final String newIdentityParentDnValue = RandomStringUtils.randomAlphanumeric(7);
        final String rdnIdentifierValue = RandomStringUtils.randomAlphanumeric(7);
        final String searchBaseDnValue = RandomStringUtils.randomAlphanumeric(7);

        try {
            createDirContext(dirContextAddress);
            createLDAPSecurityRealm(realmAddress, dirContextName);
            page.navigate();
            page.switchToLDAPRealms()
                    .getResourceManager()
                    .selectByName(realmAddress.getLastPairValue());

            ModelNodeResult initialIdentityMapping = ops.readAttribute(realmAddress, IDENTITY_MAPPING);
            initialIdentityMapping.assertSuccess();
            ConfigFragment configFragment = page.getConfig().switchTo(IDENTITY_MAPPING_LABEL);
            new ConfigChecker.Builder(client, realmAddress)
                    .configFragment(configFragment)
                    .edit(ConfigChecker.InputType.TEXT, FILTER_NAME, filterNameValue)
                    .edit(ConfigChecker.InputType.TEXT, ITERATOR_FILTER, iteratorFilterValue)
                    .edit(ConfigChecker.InputType.TEXT, NEW_IDENTITY_PARENT_DN, newIdentityParentDnValue)
                    .edit(ConfigChecker.InputType.TEXT, RDN_IDENTIFIER, rdnIdentifierValue)
                    .edit(ConfigChecker.InputType.TEXT, SEARCH_BASE_DN, searchBaseDnValue)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(IDENTITY_MAPPING, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(ATTRIBUTE_MAPPING, initialIdentityMapping.value().get(ATTRIBUTE_MAPPING))
                            .addProperty(RDN_IDENTIFIER, rdnIdentifierValue)
                            .addProperty(FILTER_NAME, filterNameValue)
                            .addProperty(ITERATOR_FILTER, iteratorFilterValue)
                            .addProperty(NEW_IDENTITY_PARENT_DN, newIdentityParentDnValue)
                            .addProperty(SEARCH_BASE_DN, searchBaseDnValue)
                            .addProperty(USE_RECURSIVE_SEARCH, new ModelNode(false))
                            .build()
                    );
        } finally {
            ops.removeIfExists(realmAddress);
            ops.removeIfExists(dirContextAddress);
            adminOps.reloadIfRequired();
        }
    }

}
