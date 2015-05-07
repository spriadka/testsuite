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

    private static final By CONTENT = By.className(PropUtils.get("page.content.rhs.class"));

    public StandaloneDeploymentsArea getDeploymentContent() {
        WebElement content = getContentRoot().findElement(CONTENT);

        return Graphene.createPageFragment(StandaloneDeploymentsArea.class, content);
    }
}
