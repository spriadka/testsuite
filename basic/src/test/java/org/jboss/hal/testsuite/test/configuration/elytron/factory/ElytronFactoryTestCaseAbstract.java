package org.jboss.hal.testsuite.test.configuration.elytron.factory;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.page.config.elytron.FactoryPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;


public abstract class ElytronFactoryTestCaseAbstract extends AbstractElytronTestCase {
    protected static final String PROVIDER_LOADER_NAME_1 = RandomStringUtils.randomAlphanumeric(5);
    protected static final String PROVIDER_LOADER_NAME_2 = RandomStringUtils.randomAlphanumeric(5);
    protected static final String ARCHIVE_NAME = "elytron.customer.credential.security.factory.jar";

    @Page
    protected FactoryPage page;

}
