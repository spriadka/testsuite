package org.jboss.hal.testsuite.test.configuration.noconfig;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class Jsr77SubsystemTestCase extends NoConfigSubsystemTestCaseAbstract {

    @Test
    public void verifyJsr77SubsystemIsPresent() {
        verifySubsystemIsPresentByNavigatingToIt("Jsr77");
    }

}
