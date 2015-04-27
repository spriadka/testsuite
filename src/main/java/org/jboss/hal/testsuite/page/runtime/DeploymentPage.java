package org.jboss.hal.testsuite.page.runtime;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.fragment.runtime.StandaloneDeploymentsArea;
import org.jboss.hal.testsuite.page.BasePage;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * @author jcechace
 */
@Location("#deployments")
public class DeploymentPage extends BasePage {

    public StandaloneDeploymentsArea getDeploymentContent() {
        String cssClass = PropUtils.get("page.content.rhs.class");
        By selector = By.className(cssClass);
        WebElement content = getContentRoot().findElement(selector);

        return Graphene.createPageFragment(StandaloneDeploymentsArea.class, content);
    }
}
