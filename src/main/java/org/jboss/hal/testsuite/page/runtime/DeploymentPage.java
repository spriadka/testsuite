package org.jboss.hal.testsuite.page.runtime;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.fragment.runtime.StandaloneDeploymentsArea;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.BasePage;
import org.jboss.hal.testsuite.page.config.ConfigurationPage;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

/**
 * @author jcechace
 */
@Location("#standalone-deployments")

public class DeploymentPage extends ConfigurationPage {

    private static final By CONTENT = By.className(PropUtils.get("page.content.gwt-layoutpanel"));

    public StandaloneDeploymentsArea getDeploymentContent() {
        WebElement content =  getContentRoot().findElement(CONTENT);

        return Graphene.createPageFragment(StandaloneDeploymentsArea.class, content);
    }

}
