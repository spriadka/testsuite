package org.jboss.hal.testsuite.test.configuration.elytron;

import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.AddResourceWizard;
import org.jboss.hal.testsuite.page.config.elytron.ElytronOtherOtherPage;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Ignore("Ignored until https://issues.jboss.org/browse/WFLY-9025 is resolved")
@RunWith(Arquillian.class)
public class ElytronPolicyTestCase extends AbstractElytronTestCase {

    private static final String
            JACC_POLICY = "jacc-policy",
            CUSTOM_POLICY = "custom-policy",
            CUSTOM_POLICY_LABEL = "Custom Policy",
            CLASS_NAME = "class-name",
            DEFAULT_POLICY = "default-policy",
            JACC = "jacc",
            POLICY = "policy",
            POLICY_LABEL = "Policy",
            LIST_ADD = "list-add";

    @Page
    private ElytronOtherOtherPage page;

    /**
     * @tpTestDetails Try to create Policy instance in Web Console's Elytron subsystem configuration.
     * Validate created resource is visible in Policy table.
     * Validate created resource is present in model.
     * Validate attributes of created resource in model.
     */
    @Test
    public void testAddPolicy() throws Exception {
        final Address policyAddress = elyOps.getElytronAddress(POLICY, JACC);
        try {
            page.navigateToApplication()
                    .switchSubTab(POLICY_LABEL);

            page.getResourceManager()
                    .addResource(AddResourceWizard.class)
                    .name(policyAddress.getLastPairValue())
                    .text(DEFAULT_POLICY, JACC)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();

            assertTrue("Probably fails because of https://issues.jboss.org/browse/WFLY-9015",
                    page.getResourceManager().isResourcePresent(policyAddress.getLastPairValue()));

            new ResourceVerifier(policyAddress, client).verifyExists();
        } finally {
            ops.removeIfExists(policyAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Policy instance in model and try to remove it in Web Console's Elytron subsystem
     * configuration.
     * Validate the resource is not any more visible in Elytron Policy table.
     * Validate removed resource is not any more present in the model.
     */
    @Test
    public void testRemovePolicy() throws Exception {
        final Address policyAddress = elyOps.getElytronAddress(POLICY, JACC);
        try {
            createPolicy(policyAddress);

            page.navigateToApplication()
                    .switchSubTab(POLICY_LABEL);

            page.getResourceManager()
                    .removeResource(policyAddress.getLastPairValue())
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();

            assertFalse(page.getResourceManager().isResourcePresent(policyAddress.getLastPairValue()));

            new ResourceVerifier(policyAddress, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(policyAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Policy instance in model and try to add its custom-policy child in Web Console's
     * configuration.
     * Validate the resource is visible in Elytron Policy's custom-policy table.
     * Validate created resource is present in the model.
     */
    @Test
    public void testAddCustomPolicy() throws Exception {
        final Address policyAddress = elyOps.getElytronAddress(POLICY, JACC);
        final String customPolicyName = randomAlphabetic(7),
                classNameValue = randomAlphabetic(7);
        try {
            createPolicy(policyAddress);

            page.navigateToApplication()
                    .switchSubTab(POLICY_LABEL);

            page.getConfig()
                    .switchTo(CUSTOM_POLICY_LABEL)
                    .getResourceManager()
                    .addResource(AddResourceWizard.class)
                    .name(customPolicyName)
                    .text(CLASS_NAME, classNameValue)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();

            assertTrue(page.getConfig()
                    .switchTo(CUSTOM_POLICY_LABEL)
                    .getResourceManager()
                    .isResourcePresent(customPolicyName));

            new ResourceVerifier(policyAddress, client)
                    .verifyListAttributeContainsValue(CUSTOM_POLICY, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                                    .addProperty(NAME, customPolicyName)
                                    .addProperty(CLASS_NAME, classNameValue)
                                    .addUndefinedProperty(MODULE)
                                    .build());
        } finally {
            ops.removeIfExists(policyAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Policy instance in model and then add its custom-policy child in model. Then try to
     * remove it in Web Console's configuration.
     * Validate the resource is not visible in Elytron Policy's custom-policy table anymore.
     * Validate created resource is not present in the model.
     */
    @Test
    public void testRemoveCustomPolicy() throws Exception {
        final Address policyAddress = elyOps.getElytronAddress(POLICY, JACC);
        final String customPolicyName = randomAlphabetic(7),
                classNameValue = randomAlphabetic(7);

        final ModelNode customPolicyModelNode = new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(NAME, customPolicyName)
                .addProperty(CLASS_NAME, classNameValue)
                .addUndefinedProperty(MODULE)
                .build();
        try {
            createPolicy(policyAddress);
            ops.invoke(LIST_ADD, policyAddress, Values.empty()
                    .and(NAME, CUSTOM_POLICY)
                    .and("value", customPolicyModelNode)).assertSuccess();
            adminOps.reloadIfRequired();

            page.navigateToApplication()
                    .switchSubTab(POLICY_LABEL);

            page.getConfig()
                    .switchTo(CUSTOM_POLICY_LABEL)
                    .getResourceManager()
                    .removeResource(customPolicyName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();

            assertFalse(page.getConfig()
                    .switchTo(CUSTOM_POLICY_LABEL)
                    .getResourceManager()
                    .isResourcePresent(customPolicyName));

            new ResourceVerifier(policyAddress, client)
                    .verifyListAttributeDoesNotContainValue(CUSTOM_POLICY, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(NAME, customPolicyName)
                            .addProperty(CLASS_NAME, classNameValue)
                            .addUndefinedProperty(MODULE)
                            .build());
        } finally {
            ops.removeIfExists(policyAddress);
            adminOps.reloadIfRequired();
        }
    }

    private void createPolicy(Address address) throws IOException, TimeoutException, InterruptedException {
        ops.add(address, Values.of(JACC_POLICY, new ModelNodeGenerator.ModelNodeListBuilder(
                new ModelNodeGenerator.ModelNodePropertiesBuilder()
                        .addProperty(NAME, JACC)
                        .build()
        ).build())).assertSuccess();
        adminOps.reloadIfRequired();
    }

}
