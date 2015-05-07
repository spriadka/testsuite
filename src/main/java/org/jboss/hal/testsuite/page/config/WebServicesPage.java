package org.jboss.hal.testsuite.page.config;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.page.ConfigPage;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@Location("#webservices")
public class WebServicesPage extends ConfigPage {

    private static final By CONFIG_CONTENT = By.className(PropUtils.get("page.content.rhs.class"));

    public ConfigFragment config(){
        return Graphene.createPageFragment(ConfigFragment.class, getContentRoot().findElement(CONFIG_CONTENT));
    }
}
