package org.jboss.hal.testsuite.test.runtime.hosts;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.dmr.AddressTemplate;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 23.10.15.
 */
@RunWith(Arquillian.class)
@Category(Domain.class)
public class HostPropertiesTestCase extends PropertiesTestCaseAbstract {

    @Override
    protected void navigate() {
        page.navigate();
        page.viewDefaultHostProperties();
    }

    @Override
    protected void verifyOnServer(String propertyName, boolean shouldExist) {
        AddressTemplate template = AddressTemplate.of("/host=" + ConfigUtils.getDefaultHost() + "/system-property=*");
        verifier.verifyResource(template.resolve(context, propertyName), shouldExist);
    }
}
