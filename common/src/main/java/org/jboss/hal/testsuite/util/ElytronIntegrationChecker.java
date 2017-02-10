package org.jboss.hal.testsuite.util;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.junit.Assert;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;

/**
 * Class which serves as an abstraction over modifying and verifying credential-reference attributes throughout HAL
 */
public class ElytronIntegrationChecker {

    private static final Logger log = LoggerFactory.getLogger(ElytronIntegrationChecker.class);

    private static final String
            CREDENTIAL_REFERENCE = "credential-reference",
            CREDENTIAL_STORE = "credential-store",
            CLEAR_TEXT = "clear-text",
            ALIAS = "alias",
            STORE = "store",
            TYPE = "type";

    private final OnlineManagementClient client;
    private final Operations operations;

    /**
     * Create new instance with client
     * @param client instance of {@link OnlineManagementClient} which is not closed
     */
    public ElytronIntegrationChecker(OnlineManagementClient client) {
        this.client = client;
        this.operations = new Operations(client);
    }

    /**
     * Sets value to clear-text field, saves form and verifies value of credential-reference against model
     * @param address address where the 'credential-reference' attribute is located
     * @param configFragment config fragment containing credential-reference input fields
     * @throws Exception
     */
    public void setClearTextCredentialReferenceAndVerify(Address address, ConfigFragment configFragment) throws Exception {
        setClearTextCredentialReferenceAndVerify(address, configFragment, CREDENTIAL_REFERENCE);
    }

    /**
     * Sets value to clear-text field, saves form and verifies value of defined attribute name against model
     * @param address address where the attribute containing credential-reference value is located
     * @param configFragment config fragment containing credential-reference input fields
     * @param credentialReferenceAttributeName name of credential reference attributes when it differs from standard
     *                                         'credential-reference' name. Use
     *                                         {@link ElytronIntegrationChecker#setClearTextCredentialReferenceAndVerify(Address, ConfigFragment)}
     *                                         when it doesn't differ.
     * @throws Exception
     */
    public void setClearTextCredentialReferenceAndVerify(Address address, ConfigFragment configFragment,
                                                         String credentialReferenceAttributeName) throws Exception {
        final String clearTextValue = "clear-text-value_" + RandomStringUtils.randomAlphanumeric(6);
        final ModelNodeResult originalValue = operations.readAttribute(address, credentialReferenceAttributeName);
        originalValue.assertSuccess();
        try {
            final Editor editor = configFragment.edit();
            clearAllInputFields(editor);
            editor.text(CLEAR_TEXT, clearTextValue);

            Assert.assertTrue("Configuration should be saved!", configFragment.save());

            final CredentialStoreModelNodeBuilder builder = new CredentialStoreModelNodeBuilder()
                    .clearText(clearTextValue);

            final ResourceVerifier verifier = new ResourceVerifier(address, client);
            try { //workaround for https://issues.jboss.org/browse/HAL-1292
                verifier.verifyAttribute(credentialReferenceAttributeName, builder.build());
            } catch (AssertionError e) {
                log.warn("Attribute has probably defined explicit undefined values, trying verifying with explicitly" +
                        "defined undefined values! See https://issues.jboss.org/browse/HAL-1292.", e);
                verifier.verifyAttribute(credentialReferenceAttributeName, builder.writeUndefinedValuesExplicitly().build());
            }

        } finally {
            operations.writeAttribute(address, credentialReferenceAttributeName, originalValue.value());
        }
    }

    /**
     * Sets value to 'store' and 'alias' fields, saves form and verifies value of credential-reference against model
     * @param address address where the 'credential-reference' attribute is located
     * @param configFragment config fragment containing credential-reference input fields
     * @throws Exception
     */
    public void setCredentialStoreCredentialReferenceAndVerify(Address address, ConfigFragment configFragment) throws Exception {
        setCredentialStoreCredentialReferenceAndVerify(address, configFragment, CREDENTIAL_REFERENCE);
    }

    /**
     * Sets value to 'store' and 'alias' fields, saves form and verifies value of defined attribute name against model
     * @param address address where the attribute containing credential-reference value is located
     * @param configFragment config fragment containing credential-reference input fields
     * @param credentialReferenceAttributeName name of credential reference attributes when it differs from standard
     *                                         'credential-reference' name. Use
     *                                         {@link ElytronIntegrationChecker#setCredentialStoreCredentialReferenceAndVerify(Address, ConfigFragment)}
     *                                         when it doesn't differ.
     * @throws Exception
     */
    public void setCredentialStoreCredentialReferenceAndVerify(Address address, ConfigFragment configFragment,
                                                               String credentialReferenceAttributeName) throws Exception {
        final String credentialStoreName = "credential-store_" + RandomStringUtils.randomAlphanumeric(6),
                credentialStoreAliasName = "credential-store-alias_" + RandomStringUtils.randomAlphanumeric(6),
                credentialStoreAliasValue = "alias-value_" + RandomStringUtils.randomAlphanumeric(6);
        final ModelNodeResult originalValue = operations.readAttribute(address, credentialReferenceAttributeName);
        originalValue.assertSuccess();
        //add credential store
        final Address credentialReferenceAddress = addCredentialStore(credentialStoreName);
        //add alias with value to credential store
        operations.add(credentialReferenceAddress.and(ALIAS, credentialStoreAliasName),
                Values.of("secret-value", credentialStoreAliasValue)).assertSuccess();
        //edit form in web console and verify against model
        try {
            final Editor editor = configFragment.edit();
            clearAllInputFields(editor);
            final WebElement aliasInput = editor.getText(ALIAS);
            sendKeysLikeHuman(aliasInput, credentialStoreAliasName);

            editor.text(STORE, credentialStoreName);

            Assert.assertTrue("Config should be saved!", configFragment.save());

            final CredentialStoreModelNodeBuilder builder = new CredentialStoreModelNodeBuilder()
                    .aliasName(credentialStoreAliasName)
                    .storeName(credentialStoreName);

            final ResourceVerifier verifier = new ResourceVerifier(address, client);

            try { //workaround for https://issues.jboss.org/browse/HAL-1292
                verifier.verifyAttribute(credentialReferenceAttributeName, builder.build());
            } catch (AssertionError e) {
                log.warn("Attribute has probably defined explicit undefined values, trying verifying with explicitly" +
                        "defined undefined values! See https://issues.jboss.org/browse/HAL-1292.", e);
                verifier.verifyAttribute(credentialReferenceAttributeName, builder.writeUndefinedValuesExplicitly().build());
            }
        } finally {
            //revert to original
            operations.writeAttribute(address, credentialReferenceAttributeName, originalValue.value());
            operations.removeIfExists(credentialReferenceAddress);
        }
    }

    private void clearAllInputFields(Editor editor) {
        editor.getText(ALIAS).clear();
        editor.getText(CLEAR_TEXT).clear();
        editor.getText(STORE).clear();
        editor.getText(TYPE).clear();
    }

    private void sendKeysLikeHuman(WebElement element, String value) {
        element.click();
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        Library.letsSleep(30);
        element.sendKeys(Keys.DELETE);
        for (char character : value.toCharArray()) {
            element.sendKeys(String.valueOf(character));
            Library.letsSleep(20);
        }
    }

    /**
     * Tests illegal combination of attributes for credential-reference
     * @param address address where the 'credential-reference' attribute is located
     * @param configFragment config fragment containing credential-reference input fields
     */
    public void testIllegalCombinationCredentialReferenceAttributes(Address address, ConfigFragment configFragment) throws IOException, OperationException {
        testIllegalCombinationCredentialReferenceAttributes(address, configFragment, CREDENTIAL_REFERENCE);
    }

    /**
     * Tests illegal combination of attributes for defined credential reference attribute name
     * @param address address where the attribute containing credential-reference value is located
     * @param configFragment config fragment containing credential-reference input fields
     * @param credentialReferenceAttributeName name of credential reference attributes when it differs from standard
     *                                         'credential-reference' name. Use
     *                                         {@link ElytronIntegrationChecker#testIllegalCombinationCredentialReferenceAttributes(Address, ConfigFragment)}
     *                                         when it doesn't differ.
     * @throws Exception
     */
    public void testIllegalCombinationCredentialReferenceAttributes(Address address, ConfigFragment configFragment,
                                                                    String credentialReferenceAttributeName) throws IOException, OperationException {
        final ModelNodeResult originalValue = operations.readAttribute(address, credentialReferenceAttributeName);
        originalValue.assertSuccess();
        final String credentialStoreName = "credential-store-name_" + RandomStringUtils.randomAlphanumeric(6);
        //add credential store (in case capabilities restriction will be in place)
        final Address credentialStoreAddress = addCredentialStore(credentialStoreName);
        try {
            Editor editor = configFragment.edit();
            editor.text(CLEAR_TEXT, RandomStringUtils.randomAlphanumeric(6));
            editor.text(STORE, credentialStoreName);
            Assert.assertFalse("Form should not be saved when both clear text and store are defined!", configFragment.save());
        } finally {
            operations.writeAttribute(address, credentialReferenceAttributeName, originalValue.value());
            operations.removeIfExists(credentialStoreAddress);
        }
    }

    private Address addCredentialStore(String credentialStoreName) throws IOException {
        final Address credentialReferenceAddress = Address.subsystem("elytron").and(CREDENTIAL_STORE, credentialStoreName);
        operations.add(credentialReferenceAddress, Values
                .of("uri", new ModelNode("cr-store://test/" + credentialStoreName + "?keyStoreType=JCEKS;modifiable=true;create=true"))
                .and("relative-to", "jboss.server.data.dir")
                .and(CREDENTIAL_REFERENCE, new ModelNode().set(new Property(CLEAR_TEXT, new ModelNode("foobar")))
                        .asObject()))
                .assertSuccess();
        return credentialReferenceAddress;
    }

    private static final class CredentialStoreModelNodeBuilder {

        private boolean writeUndefinedValuesExplicitly;
        private String clearText;
        private String storeName;
        private String aliasName;
        private String typeName;

        public CredentialStoreModelNodeBuilder writeUndefinedValuesExplicitly() {
            this.writeUndefinedValuesExplicitly = true;
            return this;
        }

        public CredentialStoreModelNodeBuilder clearText (String clearText) {
            this.clearText = clearText;
            return this;
        }

        public CredentialStoreModelNodeBuilder storeName(String storeName) {
            this.storeName = storeName;
            return this;
        }

        public CredentialStoreModelNodeBuilder aliasName(String aliasName) {
            this.aliasName = aliasName;
            return this;
        }

        public CredentialStoreModelNodeBuilder typeName(String typeName) {
            this.typeName = typeName;
            return this;
        }

        public ModelNode build() {
            ModelNodeGenerator.ModelNodePropertiesBuilder builder = new ModelNodeGenerator.ModelNodePropertiesBuilder();
            if (aliasName != null) {
                builder.addProperty(ALIAS, aliasName);
            } else if (writeUndefinedValuesExplicitly) {
                builder.addProperty(ALIAS, new ModelNode());
            }
            if (clearText != null) {
                builder.addProperty(CLEAR_TEXT, clearText);
            } else if (writeUndefinedValuesExplicitly) {
                builder.addProperty(CLEAR_TEXT, new ModelNode());
            }
            if (storeName != null) {
                builder.addProperty(STORE, storeName);
            } else if (writeUndefinedValuesExplicitly) {
                builder.addProperty(STORE, new ModelNode());
            }
            if (typeName != null) {
                builder.addProperty(TYPE, typeName);
            } else if (writeUndefinedValuesExplicitly) {
                builder.addProperty(TYPE, new ModelNode());
            }
            return builder.build();
        }
    }

}
