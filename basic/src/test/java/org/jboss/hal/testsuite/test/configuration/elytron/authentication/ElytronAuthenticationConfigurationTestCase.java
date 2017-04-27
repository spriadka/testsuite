package org.jboss.hal.testsuite.test.configuration.elytron.authentication;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.authentication.AddAuthenticationConfigurationWizard;
import org.jboss.hal.testsuite.page.config.elytron.ElytronAuthenticationPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.test.configuration.undertow.UndertowElytronOperations;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.Console;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.foundation.online.SnapshotBackup;
import org.wildfly.extras.creaper.core.online.operations.Address;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(Arquillian.class)
public class ElytronAuthenticationConfigurationTestCase extends AbstractElytronTestCase {

    @Page
    private ElytronAuthenticationPage page;

    private static final String
            AUTHENTICATION_CONFIGURATION = "authentication-configuration",
            ALLOW_ALL_MECHANISMS = "allow-all-mechanisms",
            ALLOW_SASL_MECHANISMS = "allow-sasl-mechanisms",
            ANONYMOUS = "anonymous",
            AUTHENTICATION_NAME = "authentication-name",
            AUTHORIZATION_NAME = "authorization-name",
            EXTENDS = "extends",
            FORBID_SASL_MECHANISMS = "forbid-sasl-mechanisms",
            HOST = "host",
            MECHANISM_PROPERTIES = "mechanism-properties",
            PORT = "port",
            PROTOCOL = "protocol",
            REALM = "realm",
            SECURITY_DOMAIN = "security-domain";

    /**
     * @tpTestDetails Try to create Elytron Authentication configuration instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Authentication configuration table.
     * Validate created resource is present in model.
     */
    @Test
    public void testAddAuthenticationConfigurationTestCase() throws Exception {
        final Address authenticationConfigurationAddress = elyOps.getElytronAddress(AUTHENTICATION_CONFIGURATION, RandomStringUtils.randomAlphanumeric(7));

        page.navigate();

        try {
            page.switchToAuthenticationConfiguration()
                    .getResourceManager()
                    .addResource(AddAuthenticationConfigurationWizard.class)
                    .name(authenticationConfigurationAddress.getLastPairValue())
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();

            Assert.assertTrue(page.getResourceManager().isResourcePresent(authenticationConfigurationAddress.getLastPairValue()));

            new ResourceVerifier(authenticationConfigurationAddress, client).verifyExists();
        } finally {
            ops.removeIfExists(authenticationConfigurationAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication configuration instance in model and try to remove it in Web Console's
     * Elytron subsystem configuration.
     * Validate the resource is not any more visible in Authentication configuration table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void testRemoveAuthenticationConfigurationTestCase() throws Exception {
        final Address authenticationConfigurationAddress = createAuthenticationConfiguration();

        page.navigate();

        try {
            page.switchToAuthenticationConfiguration()
                    .getResourceManager()
                    .removeResource(authenticationConfigurationAddress.getLastPairValue())
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();

            Assert.assertFalse(page.getResourceManager().isResourcePresent(authenticationConfigurationAddress.getLastPairValue()));

            new ResourceVerifier(authenticationConfigurationAddress, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(authenticationConfigurationAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication configuration instance in model and try to edit its
     * allow-all-mechanisms attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void toggleAllowAllMechanisms() throws Exception {
        final SnapshotBackup snapshotBackup = new SnapshotBackup();
        client.apply(snapshotBackup.backup());

        final Address authenticationConfigurationAddress = createAuthenticationConfiguration();
        final boolean initialValue = false;

        try {
            page.navigate();
            page.switchToAuthenticationConfiguration()
                    .getResourceManager()
                    .selectByName(authenticationConfigurationAddress.getLastPairValue());
            new ConfigChecker.Builder(client, authenticationConfigurationAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, ALLOW_ALL_MECHANISMS, !initialValue)
                    .verifyFormSaved()
                    .verifyAttribute(ALLOW_ALL_MECHANISMS, !initialValue);

            Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();

            new ConfigChecker.Builder(client, authenticationConfigurationAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, ALLOW_ALL_MECHANISMS, initialValue)
                    .verifyFormSaved()
                    .verifyAttribute(ALLOW_ALL_MECHANISMS, initialValue);
        } finally {
            client.apply(snapshotBackup.restore());
        }

    }

    /**
     * @tpTestDetails Create Elytron Authentication configuration instance in model and try to edit its
     * allow-sasl-mechanisms attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editAllowSASLMechanisms() throws Exception {
        final SnapshotBackup snapshotBackup = new SnapshotBackup();
        client.apply(snapshotBackup.backup());

        final Address authenticationConfigurationAddress = createAuthenticationConfiguration();

        final String[] value = new String[]{
                RandomStringUtils.randomAlphabetic(7),
                RandomStringUtils.randomAlphabetic(7)
        };

        try {
            page.navigate();
            page.switchToAuthenticationConfiguration()
                    .getResourceManager()
                    .selectByName(authenticationConfigurationAddress.getLastPairValue());
            new ConfigChecker.Builder(client, authenticationConfigurationAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, ALLOW_SASL_MECHANISMS, String.join("\n", value))
                    .verifyFormSaved()
                    .verifyAttribute(ALLOW_SASL_MECHANISMS,
                            new ModelNodeGenerator.ModelNodeListBuilder().addAll(value).build());
        } finally {
            client.apply(snapshotBackup.restore());
        }
    }


    /**
     * @tpTestDetails Create Elytron Authentication configuration instance in model and try to edit its anonymous
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void toggleAnonymous() throws Exception {
        final SnapshotBackup snapshotBackup = new SnapshotBackup();
        client.apply(snapshotBackup.backup());

        final Address authenticationConfigurationAddress = createAuthenticationConfiguration();
        final boolean initialValue = false;

        try {
            page.navigate();
            page.switchToAuthenticationConfiguration()
                    .getResourceManager()
                    .selectByName(authenticationConfigurationAddress.getLastPairValue());
            new ConfigChecker.Builder(client, authenticationConfigurationAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, ANONYMOUS, !initialValue)
                    .verifyFormSaved()
                    .verifyAttribute(ANONYMOUS, !initialValue);

            Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();

            new ConfigChecker.Builder(client, authenticationConfigurationAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, ANONYMOUS, initialValue)
                    .verifyFormSaved()
                    .verifyAttribute(ANONYMOUS, initialValue);
        } finally {
            client.apply(snapshotBackup.restore());
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication configuration instance in model and try to edit its
     * authentication-name attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editAuthenticationName() throws Exception {
        final SnapshotBackup snapshotBackup = new SnapshotBackup();
        client.apply(snapshotBackup.backup());

        final Address authenticationConfigurationAddress = createAuthenticationConfiguration();

        final String value = RandomStringUtils.randomAlphabetic(7);


        try {
            page.navigate();
            page.switchToAuthenticationConfiguration()
                    .getResourceManager()
                    .selectByName(authenticationConfigurationAddress.getLastPairValue());
            new ConfigChecker.Builder(client, authenticationConfigurationAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, AUTHENTICATION_NAME, value)
                    .verifyFormSaved()
                    .verifyAttribute(AUTHENTICATION_NAME, value);
        } finally {
            client.apply(snapshotBackup.restore());
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication configuration instance in model and try to edit its
     * authorization-name attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editAuthorizationName() throws Exception {
        final SnapshotBackup snapshotBackup = new SnapshotBackup();
        client.apply(snapshotBackup.backup());

        final Address authenticationConfigurationAddress = createAuthenticationConfiguration();

        final String value = RandomStringUtils.randomAlphabetic(7);

        try {
            page.navigate();
            page.switchToAuthenticationConfiguration()
                    .getResourceManager()
                    .selectByName(authenticationConfigurationAddress.getLastPairValue());
            new ConfigChecker.Builder(client, authenticationConfigurationAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, AUTHORIZATION_NAME, value)
                    .verifyFormSaved()
                    .verifyAttribute(AUTHORIZATION_NAME, value);
        } finally {
            client.apply(snapshotBackup.restore());
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication configuration instance in model and try to edit its extends
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editExtends() throws Exception {
        final SnapshotBackup snapshotBackup = new SnapshotBackup();
        client.apply(snapshotBackup.backup());

        Address authenticationConfigurationAddress,
                extendsConfigurationAddress;

        try {
            authenticationConfigurationAddress = createAuthenticationConfiguration();
            extendsConfigurationAddress = createAuthenticationConfiguration();
            page.navigate();
            page.switchToAuthenticationConfiguration()
                    .getResourceManager()
                    .selectByName(authenticationConfigurationAddress.getLastPairValue());
            new ConfigChecker.Builder(client, authenticationConfigurationAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, EXTENDS, extendsConfigurationAddress.getLastPairValue())
                    .verifyFormSaved()
                    .verifyAttribute(EXTENDS, extendsConfigurationAddress.getLastPairValue());
        } finally {
            client.apply(snapshotBackup.restore());
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication configuration instance in model and try to edit its
     * forbid-sasl-mechanisms attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editForbidSASLMechanisms() throws Exception {
        final SnapshotBackup snapshotBackup = new SnapshotBackup();
        client.apply(snapshotBackup.backup());

        final Address authenticationConfigurationAddress = createAuthenticationConfiguration();

        final String[] value = new String[]{
                RandomStringUtils.randomAlphabetic(7),
                RandomStringUtils.randomAlphabetic(7)
        };

        try {
            page.navigate();
            page.switchToAuthenticationConfiguration()
                    .getResourceManager()
                    .selectByName(authenticationConfigurationAddress.getLastPairValue());
            new ConfigChecker.Builder(client, authenticationConfigurationAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, FORBID_SASL_MECHANISMS, String.join("\n", value))
                    .verifyFormSaved()
                    .verifyAttribute(FORBID_SASL_MECHANISMS,
                            new ModelNodeGenerator.ModelNodeListBuilder().addAll(value).build());
        } finally {
            client.apply(snapshotBackup.restore());
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication configuration instance in model and try to edit its host attribute
     * value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editHost() throws Exception {
        final SnapshotBackup snapshotBackup = new SnapshotBackup();
        client.apply(snapshotBackup.backup());

        final Address authenticationConfigurationAddress = createAuthenticationConfiguration();

        final String value = RandomStringUtils.randomAlphabetic(7);

        try {
            page.navigate();
            page.switchToAuthenticationConfiguration()
                    .getResourceManager()
                    .selectByName(authenticationConfigurationAddress.getLastPairValue());
            new ConfigChecker.Builder(client, authenticationConfigurationAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, HOST, value)
                    .verifyFormSaved()
                    .verifyAttribute(HOST, value);
        } finally {
            client.apply(snapshotBackup.restore());
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication configuration instance in model and try to edit its
     * mechanism-properties attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editMechanismProperties() throws Exception {
        final SnapshotBackup snapshotBackup = new SnapshotBackup();
        client.apply(snapshotBackup.backup());

        final Address authenticationConfigurationAddress = createAuthenticationConfiguration();

        final Map<String, ModelNode> value = new HashMap<String, ModelNode>() {
            {
                put(RandomStringUtils.randomAlphabetic(7), new ModelNode(RandomStringUtils.randomAlphanumeric(7)));
                put(RandomStringUtils.randomAlphabetic(7), new ModelNode(RandomStringUtils.randomAlphanumeric(7)));
            }
        };

        try {
            page.navigate();
            page.switchToAuthenticationConfiguration()
                    .getResourceManager()
                    .selectByName(authenticationConfigurationAddress.getLastPairValue());
            new ConfigChecker.Builder(client, authenticationConfigurationAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, MECHANISM_PROPERTIES, value.entrySet()
                            .stream()
                            .map(entry -> entry.getKey() + '=' + entry.getValue().asString())
                            .collect(Collectors.joining("\n")))
                    .verifyFormSaved()
                    .verifyAttribute(MECHANISM_PROPERTIES,
                            new ModelNodeGenerator().createObjectNodeWithPropertyChildren(value));
        } finally {
            client.apply(snapshotBackup.restore());
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication configuration instance in model and try to edit its port attribute
     * value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editPort() throws Exception {
        final SnapshotBackup snapshotBackup = new SnapshotBackup();
        client.apply(snapshotBackup.backup());

        final Address authenticationConfigurationAddress = createAuthenticationConfiguration();

        final int value = 9993;

        try {
            page.navigate();
            page.switchToAuthenticationConfiguration()
                    .getResourceManager()
                    .selectByName(authenticationConfigurationAddress.getLastPairValue());
            new ConfigChecker.Builder(client, authenticationConfigurationAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, PORT, String.valueOf(value))
                    .verifyFormSaved()
                    .verifyAttribute(PORT, value);
        } finally {
            client.apply(snapshotBackup.restore());
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication configuration instance in model and try to edit its protocol
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editProtocol() throws Exception {
        final SnapshotBackup snapshotBackup = new SnapshotBackup();
        client.apply(snapshotBackup.backup());

        final Address authenticationConfigurationAddress = createAuthenticationConfiguration();

        final String value = RandomStringUtils.randomAlphabetic(7);

        try {
            page.navigate();
            page.switchToAuthenticationConfiguration()
                    .getResourceManager()
                    .selectByName(authenticationConfigurationAddress.getLastPairValue());
            new ConfigChecker.Builder(client, authenticationConfigurationAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, PROTOCOL, value)
                    .verifyFormSaved()
                    .verifyAttribute(PROTOCOL, value);
        } finally {
            client.apply(snapshotBackup.restore());
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication configuration instance in model and try to edit its realm attribute
     * value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editRealm() throws Exception {
        final SnapshotBackup snapshotBackup = new SnapshotBackup();
        client.apply(snapshotBackup.backup());

        final Address authenticationConfigurationAddress = createAuthenticationConfiguration();

        final String value = RandomStringUtils.randomAlphabetic(7);

        try {
            page.navigate();
            page.switchToAuthenticationConfiguration()
                    .getResourceManager()
                    .selectByName(authenticationConfigurationAddress.getLastPairValue());
            new ConfigChecker.Builder(client, authenticationConfigurationAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, REALM, value)
                    .verifyFormSaved()
                    .verifyAttribute(REALM, value);
        } finally {
            client.apply(snapshotBackup.restore());
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication configuration instance in model and try to edit its security-domain
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editSecurityDomain() throws Exception {
        final SnapshotBackup snapshotBackup = new SnapshotBackup();
        client.apply(snapshotBackup.backup());

        Address authenticationConfigurationAddress,
                securityDomainAddress;

        try {
            authenticationConfigurationAddress = createAuthenticationConfiguration();

            final UndertowElytronOperations undertowElytronOperations = new UndertowElytronOperations(client);
            securityDomainAddress = undertowElytronOperations
                    .createSecurityDomain(undertowElytronOperations.createSecurityRealm().getLastPairValue());

            page.navigate();
            page.switchToAuthenticationConfiguration()
                    .getResourceManager()
                    .selectByName(authenticationConfigurationAddress.getLastPairValue());
            new ConfigChecker.Builder(client, authenticationConfigurationAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, SECURITY_DOMAIN, securityDomainAddress.getLastPairValue())
                    .verifyFormSaved()
                    .verifyAttribute(SECURITY_DOMAIN, securityDomainAddress.getLastPairValue());
        } finally {
            client.apply(snapshotBackup.restore());
        }
    }

    private Address createAuthenticationConfiguration() throws IOException {
        final Address address = elyOps.getElytronAddress(AUTHENTICATION_CONFIGURATION, RandomStringUtils.randomAlphanumeric(7));
        ops.add(address).assertSuccess();
        return address;
    }

}
