package org.jboss.hal.testsuite.test.configuration.elytron.securityrealm.ldap;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.page.config.elytron.SecurityRealmPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public abstract class ElytronLDAPSecurityRealmTestCaseAbstract extends AbstractElytronTestCase {

    protected static final String DIR_CONTEXT = "dir-context";
    protected static final String LDAP_REALM = "ldap-realm";
    protected static final String URL = "url";
    protected static final String IDENTITY_MAPPING = "identity-mapping";
    protected static final String ATTRIBUTE_MAPPING = "attribute-mapping";
    protected static final String FROM = "from";
    protected static final String TO = "to";
    protected static final String RDN_IDENTIFIER = "rdn-identifier";
    protected static final String LIST_ADD = "list-add";

    @Page
    protected SecurityRealmPage page;

    protected void createLDAPSecurityRealm(Address realmAddress, String dirContextName) throws IOException, TimeoutException, InterruptedException {
        ops.add(realmAddress, Values.of(DIR_CONTEXT, dirContextName)
                .and(IDENTITY_MAPPING, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                        .addProperty(ATTRIBUTE_MAPPING,
                                new ModelNodeGenerator.ModelNodeListBuilder()
                                        .addNode(
                                                new ModelNodeGenerator.ModelNodePropertiesBuilder()
                                                        .addProperty(FROM, RandomStringUtils.randomAlphanumeric(7))
                                                        .addProperty(TO, RandomStringUtils.randomAlphanumeric(7))
                                                        .build())
                                        .build())
                        .addProperty(RDN_IDENTIFIER, org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(7))
                        .build())).assertSuccess();
        adminOps.reloadIfRequired();
    }

    protected void createDirContext(Address dirContextAddress) throws InterruptedException, TimeoutException, IOException {
        ops.add(dirContextAddress, Values.of(URL, RandomStringUtils.randomAlphanumeric(7))).assertSuccess();
        adminOps.reloadIfRequired();
    }
}
