package org.jboss.hal.testsuite.test.configuration.undertow;

import org.wildfly.extras.creaper.core.online.operations.Address;

public interface UndertowConstants {

    Address UNDERTOW_ADDRESS = Address.subsystem("undertow");
    Address UNDERTOW_FILTERS_ADDRESS = UNDERTOW_ADDRESS.and("configuration", "filter");

}
