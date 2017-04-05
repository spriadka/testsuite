package org.jboss.hal.testsuite.test.configuration.elytron.securityrealm;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.elytron.securityrealm.AddFilesystemSecurityRealmWizard;
import org.jboss.hal.testsuite.page.config.elytron.SecurityRealmPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;

@RunWith(Arquillian.class)
public class FilesystemSecurityRealmTestCase extends AbstractElytronTestCase {

    private static final String
            PATH = "path",
            LEVELS = "levels",
            RELATIVE_TO = "relative-to",
            FILESYSTEM_REALM = "filesystem-realm";

    @Page
    private SecurityRealmPage page;

    @Test
    public void testAddPropertiesSecurityRealm() throws Exception {
        final Address realmAddress = elyOps.getElytronAddress(FILESYSTEM_REALM, RandomStringUtils.randomAlphabetic(7));
        final String pathValue = RandomStringUtils.randomAlphanumeric(7);

        page.navigate();
        page.switchToFilesystemRealms();

        try {
            page.getResourceManager().addResource(AddFilesystemSecurityRealmWizard.class)
                    .name(realmAddress.getLastPairValue())
                    .path(pathValue)
                    .saveWithState()
                    .assertWindowClosed();

            Assert.assertTrue("Resource should be present in table!",
                    page.getResourceManager().isResourcePresent(realmAddress.getLastPairValue()));

            new ResourceVerifier(realmAddress, client)
                    .verifyExists()
                    .verifyAttribute(PATH, pathValue);
        } finally {
            ops.removeIfExists(realmAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editLevels() throws Exception {
        final Address securityRealmAddress = elyOps.getElytronAddress(FILESYSTEM_REALM, RandomStringUtils.randomAlphanumeric(7));

        try {
            createFilesystemSecurityRealm(securityRealmAddress);

            page.navigate();
            page.switchToFilesystemRealms()
                    .getResourceManager()
                    .selectByName(securityRealmAddress.getLastPairValue());

            final int value = 42;
            new ConfigChecker.Builder(client, securityRealmAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, LEVELS, value)
                    .verifyFormSaved()
                    .verifyAttribute(LEVELS, value);
        } finally {
            ops.removeIfExists(securityRealmAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editPath() throws Exception {
        final Address securityRealmAddress = elyOps.getElytronAddress(FILESYSTEM_REALM, RandomStringUtils.randomAlphanumeric(7));

        try {
            createFilesystemSecurityRealm(securityRealmAddress);

            page.navigate();
            page.switchToFilesystemRealms()
                    .getResourceManager()
                    .selectByName(securityRealmAddress.getLastPairValue());

            final String value = RandomStringUtils.randomAlphanumeric(7);
            new ConfigChecker.Builder(client, securityRealmAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, PATH, value)
                    .verifyFormSaved()
                    .verifyAttribute(PATH, value);
        } finally {
            ops.removeIfExists(securityRealmAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editRelativeTo() throws Exception {
        final Address securityRealmAddress = elyOps.getElytronAddress(FILESYSTEM_REALM, RandomStringUtils.randomAlphanumeric(7));

        try {
            createFilesystemSecurityRealm(securityRealmAddress);

            page.navigate();
            page.switchToFilesystemRealms()
                    .getResourceManager()
                    .selectByName(securityRealmAddress.getLastPairValue());

            final String value = RandomStringUtils.randomAlphanumeric(7);
            new ConfigChecker.Builder(client, securityRealmAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, RELATIVE_TO, value)
                    .verifyFormSaved()
                    .verifyAttribute(RELATIVE_TO, value);
        } finally {
            ops.removeIfExists(securityRealmAddress);
            adminOps.reloadIfRequired();
        }
    }

    private Address createFilesystemSecurityRealm(Address securityRealmAddress) throws IOException {
        if (securityRealmAddress == null) {
            securityRealmAddress = elyOps.getElytronAddress(FILESYSTEM_REALM, RandomStringUtils.randomAlphanumeric(7));
        }

        ops.add(securityRealmAddress, Values.of(PATH, RandomStringUtils.randomAlphanumeric(5))).assertSuccess();

        return securityRealmAddress;
    }

}
