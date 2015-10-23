package org.jboss.hal.testsuite.test.runtime.hosts;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Domain;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 23.10.15.
 */
@RunWith(Arquillian.class)
@Category(Domain.class)
public class ServerPropertiesTestCase extends PropertiesTestCaseAbstract {

    @Override
    protected void navigate() {
        page.navigate();
        page.viewServerConfiguration("server-one");
        page.switchSubTab("System Properties");
    }
}
