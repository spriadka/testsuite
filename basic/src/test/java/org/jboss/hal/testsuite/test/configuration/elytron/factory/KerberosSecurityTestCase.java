package org.jboss.hal.testsuite.test.configuration.elytron.factory;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddKerberosSecurityWizard;
import org.jboss.hal.testsuite.test.configuration.elytron.factory.validator.AddKerberosSecurityValidator;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@RunAsClient
@Category(Elytron.class)
public class KerberosSecurityTestCase extends ElytronFactoryTestCaseAbstract {

    private static final String KERBEROS_SECURITY_LABEL = "Kerberos Security";
    private static final String KERBEROS_SECURITY_FACTORY = "kerberos-security-factory";
    private static final String MECHANISM_OIDS = "mechanism-oids";
    private static final String PATH = "path";
    private static final String PRINCIPAL = "principal";
    private static final String REQUEST_LIFETIME = "request-lifetime";
    private static final String SERVER = "server";

    @BeforeClass
    public static void beforeClass() throws IOException {
        elyOps.addProviderLoader(PROVIDER_LOADER_NAME_1);
        elyOps.addProviderLoader(PROVIDER_LOADER_NAME_2);
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, ARCHIVE_NAME);
        jar.addClasses(BubuCustomCredentialFactory.class, ChachaCustomCredentialFactory.class);
    }

    @AfterClass
    public static void afterClass() throws IOException, InterruptedException, TimeoutException {
        elyOps.removeProviderLoader(PROVIDER_LOADER_NAME_1);
        elyOps.removeProviderLoader(PROVIDER_LOADER_NAME_2);
        adminOps.reloadIfRequired();
    }

    @Test
    public void addKerberosSecurityTest() throws Exception {
        final String factoryName = "factoryName_" + RandomStringUtils.randomAlphanumeric(5);
        final String pathValue = "path_" + RandomStringUtils.randomAlphanumeric(5);
        final String principalValue = "principal_" + RandomStringUtils.randomAlphanumeric(5);
        final Address factoryAddress = elyOps.getElytronAddress(KERBEROS_SECURITY_FACTORY, factoryName);
        try {
            page.navigateToApplication()
                    .selectFactory(KERBEROS_SECURITY_LABEL)
                    .getResourceManager()
                    .addResource(AddKerberosSecurityWizard.class)
                    .name(factoryName)
                    .path(pathValue)
                    .principal(principalValue)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created factory should be present in the table!",
                    page.resourceIsPresentInMainTable(factoryName));
            new ResourceVerifier(factoryAddress, client).verifyExists()
                    .verifyAttribute(PATH, pathValue)
                    .verifyAttribute(PRINCIPAL, principalValue);

        } finally {
            ops.removeIfExists(factoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addKerberosSecurityInvalidFieldsTest() {
        page.navigateToApplication().selectFactory(KERBEROS_SECURITY_LABEL);
        AddKerberosSecurityWizard wizard = page.getResourceManager().addResource(AddKerberosSecurityWizard.class);
        new AddKerberosSecurityValidator(page.getWindowFragment()).testInvalidCombinationsAndAssert(wizard);

    }

    @Test
    public void editKerberosSecurityTest() throws Exception {
        final int newRequestLifetimeValue = 7532159;
        final String factoryName = "factoryName_" + RandomStringUtils.randomAlphanumeric(5);
        final String oid1 = "1.2.840.113554.1.2.2";
        final String oid2 = "1.3.6.1.5.5.2";
        final String oid3 = "1.2.3.4.5.6.7";
        final String newOidsSeparatedByNewLine = oid2 + "\n" + oid3;
        final String pathValue = "path_" + RandomStringUtils.randomAlphanumeric(5);
        final String principalValue = "principal_" + RandomStringUtils.randomAlphanumeric(5);
        final Address factoryAddress = elyOps.getElytronAddress(KERBEROS_SECURITY_FACTORY, factoryName);
        final ModelNode initialOidsNode = new ModelNodeGenerator.ModelNodeListBuilder().addNode(new ModelNode(oid1))
                .addNode(new ModelNode(oid2)).build(),
                expectedEditedOidsNode = new ModelNodeGenerator.ModelNodeListBuilder().addNode(new ModelNode(oid2))
                        .addNode(new ModelNode(oid3)).build();

        try {
            ops.add(factoryAddress,
                    Values.of(MECHANISM_OIDS, initialOidsNode).and(PATH, pathValue).and(PRINCIPAL, principalValue))
                    .assertSuccess();

            page.navigateToApplication()
                    .selectFactory(KERBEROS_SECURITY_LABEL)
                    .getResourceManager()
                    .selectByName(factoryName);

            new ConfigChecker.Builder(client, factoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, MECHANISM_OIDS, newOidsSeparatedByNewLine).verifyFormSaved()
                    .verifyAttribute(MECHANISM_OIDS, expectedEditedOidsNode);

            new ConfigChecker.Builder(client, factoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, REQUEST_LIFETIME, String.valueOf(newRequestLifetimeValue))
                    .verifyFormSaved().verifyAttribute(REQUEST_LIFETIME, newRequestLifetimeValue);

            new ConfigChecker.Builder(client, factoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, SERVER, false).verifyFormSaved()
                    .verifyAttribute(SERVER, false);
        } finally {
            ops.removeIfExists(factoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void removeKerberosSecurityTest() throws Exception {
        final String factoryName = "factoryName_" + RandomStringUtils.randomAlphanumeric(5);
        final String oid1 = "1.2.840.113554.1.2.2";
        final String oid2 = "1.3.6.1.5.5.2";
        final String pathValue = "path_" + RandomStringUtils.randomAlphanumeric(5);
        final String principalValue = "principal_" + RandomStringUtils.randomAlphanumeric(5);
        final Address factoryAddress = elyOps.getElytronAddress(KERBEROS_SECURITY_FACTORY, factoryName);
        final ModelNode oidsNode = new ModelNodeGenerator.ModelNodeListBuilder().addNode(new ModelNode(oid1))
                .addNode(new ModelNode(oid2)).build();
        final ResourceVerifier factoryVerifier = new ResourceVerifier(factoryAddress, client);

        try {
            ops.add(factoryAddress,
                    Values.of(MECHANISM_OIDS, oidsNode).and(PATH, pathValue).and(PRINCIPAL, principalValue))
                    .assertSuccess();
            factoryVerifier.verifyExists();
            page.navigateToApplication()
                    .selectFactory(KERBEROS_SECURITY_LABEL)
                    .getResourceManager()
                    .removeResource(factoryName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Removed factory should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(factoryName));
            factoryVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(factoryAddress);
            adminOps.reloadIfRequired();
        }
    }
}

