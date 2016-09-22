package org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 9/19/16.
 */
public interface JMSBridgeConstants {

    String NAME_IDENTIFIER = "name",
        ADD_MESSAGE_ID_IN_HEADER_IDENTIFIER = "add-messageID-in-header",
        CLIENT_ID_IDENTIFIER = "client-id",
        FAILURE_RETRY_INTERVAL_IDENTIFIER = "failure-retry-interval",
        MAX_BATCH_SIZE_IDENTIFIER = "max-batch-size",
        MAX_BATCH_TIME_IDENTIFIER = "max-batch-time",
        MAX_RETRIES_IDENTIFIER = "max-retries",
        MODULE_IDENTIFIER = "module",
        QUALITY_OF_SERVICE_IDENTIFIER = "quality-of-service",
        SELECTOR_IDENTIFIER = "selector",
        SUBSCRIPTION_NAME_IDENTIFIER = "subscription-name",
        SOURCE_CONNECTION_FACTORY_IDENTIFIER = "source-connection-factory",
        SOURCE_CONTEXT_IDENTIFIER = "source-context",
        SOURCE_DESTINATION_IDENTIFIER = "source-destination",
        SOURCE_PASSWORD_IDENTIFIER = "source-password",
        SOURCE_USER_IDENTIFIER = "source-user",
        TARGET_CONNECTION_FACTORY_IDENTIFIER = "target-connection-factory",
        TARGET_CONTEXT_IDENTIFIER = "target-context",
        TARGET_DESTINATION_IDENTIFIER = "target-destination",
        TARGET_PASSWORD_IDENTIFIER = "target-password",
        TARGET_USER_IDENTIFIER = "target-user";
}
